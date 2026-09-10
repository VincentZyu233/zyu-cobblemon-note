from __future__ import annotations

import json
import logging
import socket
import sys
import tempfile
import time
import urllib.request
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import zyu_cobblemon_web as plugin


class FakeServer:
    def __init__(self, data_folder: Path):
        self.data_folder = data_folder
        self.logger = logging.getLogger("zyu-cobblemon-web-smoke")

    def get_data_folder(self) -> str:
        return str(self.data_folder)

    def register_command(self, _command) -> None:
        pass


def available_port() -> int:
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def main() -> None:
    with tempfile.TemporaryDirectory() as temp:
        folder = Path(temp)
        port = available_port()
        (folder / "config.json").write_text(json.dumps({
            "host": "127.0.0.1",
            "port": port,
            "bridge_secret": "test-secret",
            "web_session_secret": "test-session-secret",
        }), "utf-8")
        server = FakeServer(folder)
        plugin.on_load(server, None)
        try:
            for _ in range(30):
                try:
                    with urllib.request.urlopen(f"http://127.0.0.1:{port}/", timeout=1) as response:
                        page = response.read().decode("utf-8")
                        assert response.status == 200
                        assert "Cobblemon" in page
                        break
                except OSError:
                    time.sleep(0.1)
            else:
                raise RuntimeError("NiceGUI server did not become ready")
        finally:
            plugin.on_unload(server)


if __name__ == "__main__":
    main()
