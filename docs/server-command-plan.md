# 服务器便利指令与权限收紧计划

::: warning 当前状态：暂不执行

服务器仍有人游玩。本页只记录后续方案；尚未下载或安装模组，未修改 MCSM、服务端配置、`ops.json`，也未重启服务器。

:::

## 目标

- 移除所有玩家的 OP 身份，关闭原版管理指令带来的创造、给物品、改游戏规则等权限。
- 仍向普通玩家开放常用的多人便利指令：`/tpa`、`/tpahere`、`/tpaccept`、`/tpdeny`、`/home`、`/back` 和 `/suicide`。
- 每位玩家最多设置一个家。
- 不开放 `/fly`、`/invuln`、`/top`、`/day`、`/night`、全服传送点或其他会改变生存平衡的指令。

## 已核实的候选模组

核心方案使用 [Essential Commands](https://modrinth.com/mod/essential-commands) `0.35.2-mc1.21`：这是已发布的 Fabric 1.21.1 纯服务端版本，无额外依赖。

### 获取与版本核验

- [Modrinth 项目页](https://modrinth.com/mod/essential-commands)；[Fabric 1.21 / 1.21.1 的固定版本页](https://modrinth.com/mod/essential-commands/version/kev3hDqV)。该发布的文件名为 `essential_commands-0.35.2-mc1.21.jar`，版本元数据标注为 `server_only`，不要求客户端安装。
- [CurseForge 项目页](https://www.curseforge.com/minecraft/mc-mods/essential-commands)；[同一 1.21 / 1.21.1 发布文件](https://www.curseforge.com/minecraft/mc-mods/essential-commands/files/7365718)。
- [GitHub 源码与 Releases](https://github.com/John-Paul-R/Essential-Commands)。这是上述两个发布渠道对应的上游仓库，采用 MIT 许可证；源码 README 也明确说明它是纯服务端 Fabric 模组。
- [官方配置文档](https://john-paul-r.github.io/Essential-Commands/#/Config-Documentation) 与 [完整指令、权限节点表](https://john-paul-r.github.io/Essential-Commands/#/List-of-Commands-&-Permissions)。

::: warning 实施时只下载匹配的 Jar
本服是 Minecraft `1.21.1` / Fabric，因此后续只取上述 `0.35.2-mc1.21` 文件。不要因为项目页显示了更高版本，就下载给 1.21.3、1.21.4 或更新游戏版本构建的 Jar。
:::

它覆盖以下指令：

- `/tpa <玩家>`、`/tpahere <玩家>`、`/tpaccept <玩家>`、`/tpdeny <玩家>`
- `/home set <名字>`、`/home tp <名字>`、`/home delete <名字>`、`/home list`
- `/back`

计划配置：

```properties
use_permissions_api=false
home_limit=1
grant_lowest_numeric_by_default=true
enable_tpa=true
enable_back=true
enable_home=true
enable_warp=false
enable_fly=false
enable_invuln=false
enable_top=false
enable_day=false
enable_night=false
```

实际生成配置后，再以该版本写出的 `config/EssentialCommands.properties` 字段为准逐项核对；不要在未生成配置前凭空覆盖整个文件。

::: tip 为什么不立刻上 LuckPerms

这里只有一档普通玩家权限，Essential Commands 在关闭 permissions API 时已能让非 OP 玩家使用上述便利指令，并能将家数量固定为 1。后续出现管理员、会员或建筑组等多档权限需求时，再引入 LuckPerms 管理细粒度权限。

:::

## `/suicide` 的处理

当前没有找到可验证的 Fabric 1.21.1 现成 `/suicide` 模组构建：部分项目页面标注支持 1.21.1，但实际发布列表没有对应 Fabric Jar，不能直接安装。

后续采用一个极小的纯服务端 Fabric 模组：只注册 `/suicide`，仅让执行者自身死亡，不接收玩家目标参数，也不授予 `/kill`、`/give`、`/gamemode` 等权限。这样既保留便利性，也不打开作弊入口。

## 去除 OP 的实施方式

停服后，先备份服务端根目录的 `ops.json`，再将其写为：

```json
[]
```

这会移除所有 OP。不要把 `op-permission-level=0` 当作替代方案：名单中仍存在的玩家仍可能被某些模组识别为 OP。清空 `ops.json` 才是权限边界。

之后若必须进行管理，通过 MCSM 控制台临时执行 `/op <用户名>`；操作完成后立即执行 `/deop <用户名>`。控制台本身不依赖游戏内 OP 身份。

## 未来实施顺序

1. 确认所有玩家已下线，并在 MCSM 正常停服。
2. 备份 `ops.json`、`server.properties`、`mods/` 和 `config/` 中将受影响的文件。
3. 安装 Essential Commands 的 Fabric 1.21.1 版本，启动一次以生成配置，然后停服调整配置。
4. 加入仅实现 `/suicide` 的服务端小模组。
5. 清空 `ops.json`，启动服务器。
6. 用一个无 OP 测试账号逐项验证 TPA 请求、拒绝、唯一 Home、Back 与 Suicide；同时确认 `/gamemode`、`/give`、`/kill <其他玩家>` 均不可用。

::: danger 不在服务器运行时编辑权限文件

`ops.json` 与模组配置应在服务端完全停止后修改，避免内存状态覆盖文件或留下半写入数据。

:::
