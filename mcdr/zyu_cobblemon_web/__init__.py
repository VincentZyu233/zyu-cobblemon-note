from __future__ import annotations

import base64
import hashlib
import hmac
import json
import secrets
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any
from uuid import uuid4

from fastapi import FastAPI
from mcdreforged.api.all import Literal, PluginServerInterface
from nicegui import app, run, ui
import uvicorn


CONFIG_NAME = "config.json"
DEFAULT_CONFIG: dict[str, Any] = {
    "host": "0.0.0.0",
    "port": 26697,
    "bridge_url": "http://127.0.0.1:25931",
    "bridge_secret": "",
    "gateway_url": "http://127.0.0.1:25932",
    "web_ai_token": "",
    "web_session_secret": "",
    "auth": {"username": "keai", "password_hash": ""},
    "ai": {
        "enabled": True,
        "max_question_length": 500,
    },
}


def password_matches(password: str, stored: str) -> bool:
    try:
        algorithm, rounds, salt, _ = stored.split("$", 3)
        if algorithm != "pbkdf2_sha256":
            return False
        digest = hashlib.pbkdf2_hmac(
            "sha256", password.encode("utf-8"), base64.urlsafe_b64decode(salt.encode("ascii")), int(rounds)
        )
        candidate = f"pbkdf2_sha256${rounds}${salt}${base64.urlsafe_b64encode(digest).decode('ascii')}"
        return hmac.compare_digest(candidate, stored)
    except (TypeError, ValueError):
        return False


def merge_defaults(value: dict[str, Any], defaults: dict[str, Any]) -> dict[str, Any]:
    result = dict(defaults)
    for key, item in value.items():
        result[key] = merge_defaults(item, defaults[key]) if isinstance(item, dict) and isinstance(defaults.get(key), dict) else item
    return result


def signature(secret: str, timestamp: str, nonce: str, method: str, path: str) -> str:
    message = "\n".join((timestamp, nonce, method, path, "")).encode("utf-8")
    return hmac.new(secret.encode("utf-8"), message, hashlib.sha256).hexdigest()


class BridgeClient:
    def __init__(self, base_url: str, secret: str):
        self.base_url = base_url.rstrip("/")
        self.secret = secret

    def get(self, path: str) -> dict[str, Any]:
        if not self.secret:
            raise RuntimeError("尚未配置 bridge_secret")
        timestamp, nonce = str(int(time.time())), str(uuid4())
        request = urllib.request.Request(
            f"{self.base_url}{path}",
            headers={
                "X-ZCN-Timestamp": timestamp,
                "X-ZCN-Nonce": nonce,
                "X-ZCN-Signature": signature(self.secret, timestamp, nonce, "GET", path),
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=5) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            raise RuntimeError(f"Fabric Bridge 返回 HTTP {error.code}") from error


@dataclass
class DashboardState:
    server: PluginServerInterface
    config: dict[str, Any]
    uvicorn_server: uvicorn.Server | None = None
    uvicorn_thread: threading.Thread | None = None

    @property
    def bridge(self) -> BridgeClient:
        return BridgeClient(str(self.config["bridge_url"]), str(self.config["bridge_secret"]))

    def dashboard(self) -> tuple[dict[str, Any], list[dict[str, Any]]]:
        status = self.bridge.get("/v1/status")
        players: list[dict[str, Any]] = []
        for summary in self.bridge.get("/v1/players").get("players", []):
            name = summary.get("name")
            if not isinstance(name, str):
                continue
            player = self.bridge.get("/v1/player?name=" + urllib.parse.quote(name))
            try:
                player["pc"] = self.bridge.get("/v1/pc?name=" + urllib.parse.quote(name))
            except RuntimeError as error:
                player["pc_error"] = str(error)
            players.append(player)
        return status, players

    def search_inventory(self, query: str) -> tuple[list[dict[str, Any]], bool]:
        needle = query.strip().lower()
        rows: list[dict[str, Any]] = []
        incomplete = False
        for base in self.bridge.get("/v1/bases").get("bases", []):
            name = base.get("name")
            if not isinstance(name, str):
                continue
            for container in self.bridge.get("/v1/base?name=" + urllib.parse.quote(name)).get("containers", []):
                incomplete = incomplete or container.get("state") == "unloaded"
                if container.get("state") != "loaded":
                    continue
                for item in container.get("items", []):
                    item_id = str(item.get("item", ""))
                    if not needle or needle in item_id.lower():
                        rows.append({
                            "item": item_id,
                            "count": item.get("count", 0),
                            "base": name,
                            "position": container.get("position", ""),
                        })
        return rows, incomplete

    def ask_ai(self, question: str, player: str) -> str:
        ai = self.config["ai"]
        if not ai.get("enabled"):
            raise RuntimeError("AI 功能在配置中被关闭")
        if not question or len(question) > int(ai["max_question_length"]):
            raise RuntimeError("问题不能为空且最多 500 字")
        token = str(self.config["web_ai_token"])
        if not token:
            raise RuntimeError("尚未配置 web_ai_token")
        body = json.dumps({"question": question, "player": player}, ensure_ascii=False).encode("utf-8")
        request = urllib.request.Request(
            str(self.config["gateway_url"]).rstrip("/") + "/v1/web-questions",
            data=body,
            headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=45) as response:
                payload = json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            if error.code == 401:
                raise RuntimeError("AI 正在冷却、忙碌或配置凭据无效") from error
            raise RuntimeError(f"AI 网关返回 HTTP {error.code}") from error
        if not isinstance(payload.get("answer"), str):
            raise RuntimeError(str(payload.get("error", "AI 网关没有返回回答")))
        return payload["answer"]

    def start(self) -> None:
        config = uvicorn.Config(NICEGUI_APP, host=str(self.config["host"]), port=int(self.config["port"]), log_level="warning", access_log=False)
        self.uvicorn_server = uvicorn.Server(config)
        self.uvicorn_thread = threading.Thread(target=self.uvicorn_server.run, name="zyu-cobblemon-web", daemon=True)
        self.uvicorn_thread.start()
        self.server.logger.info("[ZyuCobblemonWeb] NiceGUI listening on %s:%s", self.config["host"], self.config["port"])

    def stop(self) -> None:
        if self.uvicorn_server is not None:
            self.uvicorn_server.should_exit = True
        if self.uvicorn_thread is not None:
            self.uvicorn_thread.join(timeout=5)


NICEGUI_APP = FastAPI()
STATE: DashboardState | None = None
NICEGUI_CONFIGURED = False


def current_state() -> DashboardState:
    if STATE is None:
        raise RuntimeError("MCDR plugin has not finished loading")
    return STATE


@ui.page("/")
async def index() -> None:
    state = current_state()
    ui.colors(primary="#c93435", secondary="#151515", accent="#f2c94c")
    with ui.header().classes("items-center justify-between bg-[#151515] text-white"):
        ui.label("Cobblemon 进度面板").classes("text-lg font-bold")
        with ui.row().classes("items-center gap-3"):
            refreshed = ui.label("正在读取").classes("text-sm text-gray-300")
            if app.storage.user.get("authenticated"):
                ui.button("退出 AI", on_click=lambda: (app.storage.user.pop("authenticated", None), ui.navigate.to("/"))).props("flat color=white")
            else:
                ui.button("AI 登录", on_click=lambda: ui.navigate.to("/login")).props("outline color=white")

    with ui.column().classes("w-full max-w-6xl mx-auto p-4 gap-5"):
        with ui.row().classes("w-full gap-3"):
            values: dict[str, Any] = {}
            for label in ("TPS", "MSPT", "在线"):
                with ui.card().classes("flex-1 min-w-36 p-4"):
                    ui.label(label).classes("text-gray-600 text-sm")
                    values[label] = ui.label("-").classes("text-2xl font-bold")

        with ui.card().classes("w-full p-4"):
            with ui.row().classes("w-full items-center justify-between"):
                ui.label("基地物资").classes("text-lg font-bold")
                query = ui.input(placeholder="搜索物品 ID，例如 iron_ingot").classes("w-80 max-w-full")
            inventory_note = ui.label().classes("text-sm text-gray-600")
            inventory = ui.table(columns=[
                {"name": "item", "label": "物品", "field": "item", "align": "left"},
                {"name": "count", "label": "数量", "field": "count"},
                {"name": "base", "label": "基地", "field": "base"},
                {"name": "position", "label": "位置", "field": "position"},
            ], rows=[]).classes("w-full")

        with ui.card().classes("w-full p-4"):
            ui.label("在线玩家").classes("text-lg font-bold")
            players = ui.column().classes("w-full gap-3")

        if app.storage.user.get("authenticated"):
            with ui.card().classes("w-full p-4"):
                ui.label("AI 查询").classes("text-lg font-bold")
                selected_player = ui.select(options=[], label="作为哪位在线玩家提问").classes("w-full")
                question = ui.textarea(placeholder="例如：基地里还有多少铁？").props("maxlength=500").classes("w-full")
                answer = ui.markdown().classes("w-full")

                async def ask() -> None:
                    if not selected_player.value:
                        answer.set_content("> 请先选择一位在线玩家。")
                        return
                    answer.set_content("正在查询...")
                    try:
                        answer.set_content(await run.io_bound(state.ask_ai, question.value, selected_player.value))
                    except Exception as error:
                        answer.set_content(f"> 请求失败：{error}")

                ui.button("提问", on_click=ask).props("color=primary")

    async def refresh_inventory() -> None:
        try:
            rows, incomplete = await run.io_bound(state.search_inventory, query.value)
            inventory.rows = rows
            inventory.update()
            inventory_note.text = "部分登记箱子所在区块未加载，结果不完整。" if incomplete else "所有已加载登记箱子已查询。"
        except Exception as error:
            inventory_note.text = f"物资读取失败：{error}"

    async def refresh_dashboard() -> None:
        try:
            status, details = await run.io_bound(state.dashboard)
            values["TPS"].text = str(status.get("tps", "-"))
            values["MSPT"].text = str(status.get("mspt", "-"))
            values["在线"].text = f"{status.get('onlinePlayers', 0)}/{status.get('maxPlayers', 0)}"
            refreshed.text = f"刷新于 {time.strftime('%H:%M:%S')}"
            if app.storage.user.get("authenticated"):
                names = [str(player.get("name")) for player in details if player.get("name")]
                selected_player.options = names
                if selected_player.value not in names:
                    selected_player.value = names[0] if names else None
                selected_player.update()
            players.clear()
            with players:
                if not details:
                    ui.label("当前没有在线玩家。").classes("text-gray-600")
                for player in details:
                    with ui.card().classes("w-full bg-gray-50"):
                        ui.label(f"{player.get('name', 'Unknown')}  ·  {player.get('health', '-')} HP").classes("font-bold")
                        ui.label(f"{player.get('dimension', '')}  {player.get('x', 0):.1f}, {player.get('y', 0):.1f}, {player.get('z', 0):.1f}").classes("text-sm text-gray-600")
                        items = "、".join(f"{item.get('item')} x{item.get('count')}" for item in player.get("inventory", [])) or "空"
                        ui.label(f"背包：{items}").classes("text-sm break-all")
                        party = "、".join("空位" if slot.get("empty") else f"{slot.get('pokemon', {}).get('name')} Lv.{slot.get('pokemon', {}).get('level')}" for slot in player.get("party", [])) or "暂无"
                        ui.label(f"队伍：{party}").classes("text-sm break-all")
        except Exception as error:
            refreshed.text = f"读取失败：{error}"

    query.on_value_change(lambda _: ui.timer(0.1, refresh_inventory, once=True))
    # Render first; a Fabric Bridge timeout must not hold the browser's initial page request open.
    ui.timer(0.1, refresh_dashboard, once=True)
    ui.timer(0.2, refresh_inventory, once=True)
    ui.timer(15.0, refresh_dashboard)


@ui.page("/login")
def login() -> None:
    state = current_state()
    if app.storage.user.get("authenticated"):
        ui.navigate.to("/")
        return
    with ui.column().classes("absolute-center w-96 max-w-[calc(100%-2rem)] gap-3"):
        ui.label("AI 登录").classes("text-2xl font-bold")
        ui.label("库存和在线状态公开；AI 问答需要登录。HTTP 不适合复用重要密码。").classes("text-sm text-gray-600")
        username = ui.input("账号", value=str(state.config["auth"]["username"]))
        password = ui.input("密码", password=True, password_toggle_button=True)
        error = ui.label().classes("text-red-700 text-sm")

        def submit() -> None:
            auth = state.config["auth"]
            if not auth.get("password_hash"):
                error.text = "服务端尚未配置密码哈希。"
            elif hmac.compare_digest(username.value, str(auth["username"])) and password_matches(password.value, str(auth["password_hash"])):
                app.storage.user["authenticated"] = True
                ui.navigate.to("/")
            else:
                error.text = "账号或密码错误。"

        ui.button("登录", on_click=submit).props("color=primary")
        ui.button("返回查询面板", on_click=lambda: ui.navigate.to("/")).props("flat")


def on_load(server: PluginServerInterface, old_module: Any) -> None:
    global STATE, NICEGUI_CONFIGURED
    config_path = Path(server.get_data_folder()) / CONFIG_NAME
    config_path.parent.mkdir(parents=True, exist_ok=True)
    current = json.loads(config_path.read_text("utf-8")) if config_path.exists() else {}
    config = merge_defaults(current, DEFAULT_CONFIG)
    changed = not config_path.exists()
    if not config.get("web_session_secret"):
        config["web_session_secret"] = secrets.token_urlsafe(48)
        changed = True
    if changed:
        config_path.write_text(json.dumps(config, ensure_ascii=False, indent=2) + "\n", "utf-8")
        server.logger.warning("[ZyuCobblemonWeb] generated %s; configure bridge_secret and auth.password_hash", config_path)
    if not NICEGUI_CONFIGURED:
        ui.run_with(NICEGUI_APP, title="Cobblemon 进度面板", storage_secret=str(config["web_session_secret"]), show_welcome_message=False)
        NICEGUI_CONFIGURED = True
    STATE = DashboardState(server, config)
    STATE.start()
    server.register_command(Literal("!!zcnweb").runs(lambda source: source.reply(f"Web dashboard: http://{config['host']}:{config['port']}/")))


def on_unload(server: PluginServerInterface) -> None:
    global STATE
    if STATE is not None:
        STATE.stop()
    STATE = None
