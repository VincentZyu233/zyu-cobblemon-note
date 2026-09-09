# AI Bridge：游戏内与 MCP 用法

## 游戏内命令

```mcfunction
/ai status
/ai players
/ai player 玩家名
/ai party 玩家名
/ai base 基地名
/ai question 我目前有哪些材料能做治疗仪？
```

前五个会立即返回服务器数据。`/ai question` 会先提示排队，模型完成后再返回回答。默认限制为每玩家 60 秒一次、全服单并发、每日 100 次，问题最长 500 字；已有一个问题正在生成时，新问题会被拒绝，稍后重试即可。

`accessMode` 可在服务端配置中控制游戏内读取范围：`public_full` 允许所有人读取；`self_and_admin` 只允许本人查询自己的 `player` 与 `party`，管理员仍可读取全部；`admin_only` 则仅管理员可使用读取和问答命令。`status` 始终可查看，不含玩家隐私数据。

::: warning 这是只读助手

它只读状态并发送文字回答。没有 `/ai give`、移动物品、执行 OP 命令或替玩家操控角色的功能。

:::

## MCP 工具

网关通过 stdio 提供以下工具，任何兼容 MCP 的 Agent 都可接入：

- `get_server_status`：TPS、MSPT 与在线人数。
- `list_online_players`：在线玩家基础状态。
- `get_player_progress`、`get_player_party`、`get_player_pc_summary`：指定在线玩家。
- `list_bases`、`get_base_inventory`：登记基地与库存。

当服务器未启动、SSH 隧道断开、玩家离线或基地未加载时，工具会返回错误或状态，而不是使用缓存猜测。

## 适合问什么

- “我队伍里缺什么属性覆盖？”
- “基地是否还有足够铁、铜、红石做治疗仪？”
- “当前卡顿是 TPS 还是网络问题？”
- “某位在线玩家的队伍适合抓这只野生宝可梦吗？”
