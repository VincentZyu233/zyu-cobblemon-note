# AI 集成：实时进度助手

这套实验中的纯服务端扩展让 AI 在需要时读取真实服务器进度，而不是只根据截图猜测背包、队伍或基地资源。它不会移动物品、替玩家战斗、执行管理命令或修改 Cobblemon 数据。

```text
Minecraft Fabric 服务端
  Zyu Cobblemon Note 模组
       | 127.0.0.1 + HMAC
服务器同机的 MCP / OpenAI Gateway
       | MCP stdio
Codex、其他兼容 MCP 的 Agent（可经 SSH 转发）
```

::: warning 默认范围很宽

默认权限是 `public_full`：所有玩家可通过 `/ai` 查询在线进度与已登记的基地库存。它适合当前朋友服；加入陌生玩家前，应切换为 `self_and_admin` 或 `admin_only`。

:::

## 能读取什么

- 服务器 TPS、MSPT、在线人数。
- 在线玩家的位置、生命、饥饿、背包汇总与 Cobblemon 队伍。
- 玩家电脑中的宝可梦数量与少量样本，仅在明确查询时读取。
- 手工登记的基地范围、单个箱子、木桶、潜影盒和机器库存。

为了避免一次查询造成区块加载或磁盘 I/O，未加载的基地位置只会显示 `unloaded`。让玩家靠近基地后再查即可得到实时内容。

## Fabric、MCP、MCDR 与网页

Fabric 是数据核心，因为 Cobblemon 队伍、电脑、牧场与世界容器都在 Fabric 服务端内存中。MCP 是把这些只读能力提供给 AI 的标准协议，因此不局限于 Codex。

MCDR 负责 NiceGUI 网页面板、公共查询、登录后的 AI 入口和 `Games_AI` 适配。它通过 Fabric Bridge 读取数据，不会解析 Cobblemon 存档或重复读取箱子，因此仍只有一套数据口径。页面部署与运行时配置请看 [MCDR 网页面板](/mcdr-web)。

## 问答模型

`/ai question <问题>` 会异步发送到 OpenAI 兼容 Responses API。网关只将问题、提问者自己的进度和服务器概要作为上下文；回答通过游戏内系统消息返回。它不是当前网页或当前 Codex 对话的直接转发器。

API Key、Base URL、模型名与预算只保存在网关环境文件中，绝不提交到 GitHub。

下一步请看 [部署与连接](/ai-installation) 和 [游戏内与 MCP 用法](/ai-commands)。
