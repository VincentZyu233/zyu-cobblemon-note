# MCDR 网页面板：公开查询与 AI 登录

`zyu-cobblemon-note` 除了 Fabric 模组与 MCP 网关，还带有一个独立的 MCDReforged 插件。它用 NiceGUI 构建页面，负责对外展示网页，但实时数据仍只来自 Fabric Bridge。

```text
浏览器
  | 公开：状态、玩家、登记基地物资
  | 登录：AI 问答
MCDR Web 插件
  | HMAC + loopback
Fabric Bridge -> Minecraft / Cobblemon 内存数据
```

::: warning HTTP 的边界

状态、玩家和基地物资页可公开访问。AI 页面要求登录，但 HTTP 本身会明文传输登录会话，只适合当前两三人的朋友服，也不要复用重要密码。

:::

## 能查到什么

- TPS、MSPT、在线人数。
- 在线玩家的位置、背包汇总、队伍与电脑摘要。
- `bases` 中已登记且当前加载的容器；可按 `iron_ingot`、`cobblemon:poke_ball` 这类物品 ID 搜索。

未加载区块会明确标注为不完整，绝不会被当作空箱子。让玩家靠近基地、区块被服务端加载后再刷新，才能得到当下内容。

## 安装方式

自研插件源码位于仓库 `mcdr/`，但不要把该目录直接放进 MCDR。带 `[build-action]` 的 GitHub Actions 会先使用 `uv` 安装锁定依赖、导入检查 NiceGUI/MCDR 插件，再生成 `zyu-cobblemon-web.mcdr`。部署时只使用该 artifact。

MCDR 的运行时配置会自动生成在：

```text
config/zyu_cobblemon_web/config.json
```

在其中填写与 Fabric 模组相同的 `bridge_secret`。`auth.password_hash` 必须是密码哈希，不能明文保存密码或提交到 Git。

## AI 接入状态

网页 AI 只在 `Games_AI` 的 Cobblemon 分支提供 `ask_readonly()` 接口后启用。该接口只允许读取 Cobblemon 状态、基地物资、玩家进度与源码；它不能控制 Bot、白名单、技能文件或服务端。

回答涉及 Cobblemon 机制时，运行时技能会先检索远程源码目录 `/data/data1/aaa_from_git_aaa/cobblemon`。没有找到当前版本源码依据时，回答必须说明未确认，而不是猜测。
