# 服务器便利指令与权限收紧计划

::: tip 当前状态：已部署，待玩家实测

服务端已完成 CI artifact 部署：自研命令模组已更新至 `0.1.1`，Essential Commands 的 `enable_fly=true` 已写入实际配置，`ops.json` 已备份后清空。MCSM 尚需切换到本页 AI 集成章节给出的 MCDR 工作目录与启动命令；随后再用无 OP 玩家实测命令，不能把当前状态当作已经完全验收。

:::

## 目标

- 移除所有玩家的 OP 身份，关闭原版管理指令带来的创造、给物品、改游戏规则等权限。
- 仍向普通玩家开放常用的多人便利指令：`/tpa`、`/tpahere`、`/tpaccept`、`/tpdeny`、`/home`、`/back`、`/fly` 和 `/suicide`；可信玩家可使用完整原版语法的 `/goto`。
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
| `/goto` | 将指令转交给原版 `/teleport` 命令树。 | `gotoAllowedPlayers` 白名单中的名字，或临时 OP。 | 白名单只对这条命令生效，不会变成 OP。 |

### `/suicide`：只作用于自己

直接输入即可：

```mcfunction
/suicide
```

源码会取得执行者自身并调用死亡逻辑，不接受目标参数。因此 `/suicide rainyxin` 不是有效写法，也不能被用来击杀别人。

### `/goto`：原版传送语法的受控入口

`/goto` 复用原版 `/tp` 的解析与传送行为，所以可以使用常见的原版形式：

```mcfunction
/goto 120 64 -320
/goto rainyxin
/goto @s 120 64 -320 facing entity VincentZyu eyes
```

它并不默认向所有普通玩家开放：仅配置文件 `gotoAllowedPlayers` 白名单中的名字可用，临时 OP 也可用。清空 OP 前必须先把可信玩家填入该数组；这避免任何路人使用 `@a` 等选择器传送整服玩家。

`/goto` 在执行时只为这一条命令构造原版所需的权限上下文，不会赋予执行者 `/give`、`/gamemode`、`/kill` 或其他 OP 命令权限。默认配置中的白名单为空，表示停服部署后需要显式填写，不会意外开放传送。

::: warning 白名单意味着完整原版传送能力

`/goto` 刻意保留原版 `/tp` 的完整语法，而不是只允许“传送自己”。只把你信任、可以使用目标选择器与坐标传送的玩家写进 `gotoAllowedPlayers`；一般玩家使用 `/tpa`、`/home`、`/back` 即可。

:::

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
