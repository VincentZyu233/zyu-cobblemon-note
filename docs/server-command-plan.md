# 服务器便利指令与权限收紧计划

::: tip 当前状态：`0.2.0` 已实现，待 CI Artifact 部署

已部署版本仍为 `0.1.1`。本次会通过 CI 生成 `0.2.0` Artifact，新增 OP 专用的基地容器范围登记；Essential Commands 的 `enable_fly=true` 已写入实际配置，`ops.json` 已备份后清空。当前有人在线，不能直接替换；MCSM 尚需切换到本页 AI 集成章节给出的 MCDR 工作目录与启动命令，随后再用无 OP 玩家实测命令，不能把当前状态当作已经完全验收。

:::

## 目标

- 移除所有玩家的 OP 身份，关闭原版管理指令带来的创造、给物品、改游戏规则等权限。
- 仍向普通玩家开放常用的多人便利指令：`/tpa`、`/tpahere`、`/tpaccept`、`/tpdeny`、`/home`、`/back`、`/fly` 和 `/suicide`；可信玩家可用 `/goto` 将自己传送到在线玩家或坐标。
- 每位玩家最多设置一个家。
- 不开放 `/invuln`、`/top`、`/day`、`/night`、全服传送点或其他会改变生存平衡的指令。

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
- `/fly`：切换自身飞行；由 Essential Commands 配置启用。

计划配置：

```properties
use_permissions_api=false
home_limit=1
grant_lowest_numeric_by_default=true
enable_tpa=true
enable_back=true
enable_home=true
enable_warp=false
enable_fly=true
enable_invuln=false
enable_top=false
enable_day=false
enable_night=false
```

实际生成配置后，再以该版本写出的 `config/EssentialCommands.properties` 字段为准逐项核对；不要在未生成配置前凭空覆盖整个文件。

::: tip 为什么不立刻上 LuckPerms

这里只有一档普通玩家权限，Essential Commands 在关闭 permissions API 时已能让非 OP 玩家使用上述便利指令，并能将家数量固定为 1。后续出现管理员、会员或建筑组等多档权限需求时，再引入 LuckPerms 管理细粒度权限。

:::

## 自研 Kotlin 命令：`/suicide` 与 `/goto`

当前没有找到可验证的 Fabric 1.21.1 现成 `/suicide` 模组构建：部分项目页面标注支持 1.21.1，但实际发布列表没有对应 Fabric Jar，不能直接安装。因此这两个指令来自同一个自研的纯服务端 Fabric Kotlin 模组：`Zyu Cobblemon Note`（模组 ID：`zyu_cobblemon_note`），不是 Essential Commands 提供的功能。

源码入口为 [`ZyuCobblemonNoteMod.kt`](https://github.com/VincentZyu233/zyu-cobblemon-note/blob/main/mod/src/main/kotlin/io/github/vincentzyu233/cobblemonnote/ZyuCobblemonNoteMod.kt)。它仅安装在服务端，客户端无需新增模组。

| 指令 | 做什么 | 谁能用 | 不会获得什么 |
| --- | --- | --- | --- |
| `/suicide` | 在左下角显示 `ouch.... that looks hurt` 后，仅让执行者自身死亡。 | 所有实际在线玩家；可通过 `suicideEnabled` 关闭。 | 不能指定其他玩家，不会取得 `/kill`、`/give`、`/gamemode` 权限。 |
| `/goto` | 将执行者自身传送到在线玩家或三维坐标。 | `gotoAllowedPlayers` 白名单中的名字，或临时 OP。 | 白名单只对这条命令生效，不能传送其他玩家，也不会变成 OP。 |

### `/suicide`：只作用于自己

直接输入即可：

```mcfunction
/suicide
```

源码会取得执行者自身并调用死亡逻辑，不接受目标参数。因此 `/suicide rainyxin` 不是有效写法，也不能被用来击杀别人。

### `/goto`：带补全的受控传送

旧实现重定向到原版 `/tp`，但无 OP 玩家拿不到原版受权限保护的参数树，客户端因而没有玩家名与坐标的补全。`0.2.4` 改为自研参数树：

```mcfunction
/goto 120 64 -320
/goto rainyxin
/goto @s 120 64 -320
```

输入 `/goto ` 后会补全当前在线玩家；坐标参数支持原版坐标形式，例如 `~ ~ ~`。`/goto @s <x> <y> <z>` 兼容 Xaero 地图的“传送自己到标记坐标”命令模板。它并不默认向所有普通玩家开放：仅配置文件 `gotoAllowedPlayers` 白名单中的名字可用，临时 OP 也可用。清空 OP 前必须先把可信玩家填入该数组。

`/goto` 只会移动执行者自身，不会赋予执行者 `/give`、`/gamemode`、`/kill` 或其他 OP 命令权限。默认配置中的白名单为空，表示停服部署后需要显式填写，不会意外开放传送。

::: warning 这是“自己去哪里”，不是管理员传送

`/goto @s <x> <y> <z>` 只接受执行者本人；`/goto @a ...` 或用其他玩家名字再接坐标都会被拒绝，不能借此把整服玩家传送，也不包含 `facing` 等管理员语法。需要与其他玩家协商传送时，使用 `/tpa`、`/tpahere`、`/tpaccept`。

:::

## 自研 Kotlin：基地容器范围登记

网页、MCP 与 `/ai base` 只能读取已登记的基地容器。`0.2.0` 新增两种并行登记方式：直接维护私有 JSON，或由临时 OP 在游戏内用两点选区登记。后者默认用金锄头，方便在不手算坐标的情况下圈出一块基地。

::: warning 仅临时 OP 可操作

选区和 `/zcn base` 全部要求 OP。需要登记时在 MCSM 控制台执行 `/op <用户名>`，完成后立即 `/deop <用户名>`。普通玩家手持金锄头仍按原版行为执行，不会触发选区。

:::

### 游戏内两点选区流程

1. 执行 `/zcn base create <名称>` 创建并选中一个基地；已有基地则执行 `/zcn base select <名称>`。
2. 主手持默认的金锄头，左键目标方块设置第一角，右键目标方块设置第二角。
3. 执行 `/zcn base status` 核对基地名、两角和体积。
4. 确认后执行 `/zcn base add` 才会保存为扫描范围；`/zcn base clear` 只清除尚未保存的两个角。

两次点击会刻意拦截破坏、开箱、耕地等原版交互，因此务必在确认目标方块后点击。两角必须在同一维度；默认最大体积由 `maxRegionBlocks=32768` 限制。`add` 后会立即原子写入配置，并清空本次选点，可以继续圈下一块区域。

默认工具在私有配置 `zyu-cobblemon-note.json` 中是：

```json
{
  "regionWandItem": "minecraft:golden_hoe",
  "maxRegionBlocks": 32768
}
```

可把 `regionWandItem` 改成任意有效物品 ID，例如 `minecraft:wooden_hoe`。配置会在服务端启动时读取，因此停服修改后再启动；不要在运行中修改 JSON 后立刻操作命令。

### 手改 JSON 仍然可用

原来的 `bases`、`regions` 与单个 `targets` 配置没有被替代，适合精确输入坐标或批量维护。它与金锄头选区可以并行使用，但有一个重要边界：模组不热重载运行中的外部 JSON 修改，且 `/zcn base create`、`/zcn base add` 会将当前内存配置写回文件。

因此手改 JSON 时应完全停服，修改、检查 JSON 格式后再启动。已登记范围中未加载的区块仍会返回 `unloaded`，查询端必须把它理解为“结果不完整”，而不是空库存。

## 去除 OP 的实施方式

停服后，先备份服务端根目录的 `ops.json`，再将其写为：

```json
[]
```

这会移除所有 OP。不要把 `op-permission-level=0` 当作替代方案：名单中仍存在的玩家仍可能被某些模组识别为 OP。清空 `ops.json` 才是权限边界。

之后若必须进行管理，通过 MCSM 控制台临时执行 `/op <用户名>`；操作完成后立即执行 `/deop <用户名>`。控制台本身不依赖游戏内 OP 身份。

## 已完成与验收顺序

1. 已完成：停服备份 `ops.json`、`server.properties`、启动脚本与旧网关单元；部署 artifact 后移动到 MCDR 根目录。
2. 已完成：安装 Essential Commands `0.35.2-mc1.21`，通过首次启动生成真实配置，再关闭不在范围内的便利/生存破坏指令。
3. 已完成：加入自研 `/suicide` 与受白名单保护的 `/goto`；私有配置暂时允许 `rainyxin` 和 `VincentZyu` 使用 `/goto`，后续新增可信玩家时只改该数组。
4. 已完成：清空 `ops.json`。
5. 待完成：在 MCSM 改为 MCDR 启动后，用无 OP 账号逐项验证 TPA 请求、拒绝、唯一 Home、Back、Fly、Suicide 与 Goto；同时确认 `/gamemode`、`/give`、`/kill <其他玩家>` 均不可用，并确认不在 `gotoAllowedPlayers` 的账号无法使用 `/goto`。

::: danger 不在服务器运行时编辑权限文件

`ops.json` 与模组配置应在服务端完全停止后修改，避免内存状态覆盖文件或留下半写入数据。

:::
