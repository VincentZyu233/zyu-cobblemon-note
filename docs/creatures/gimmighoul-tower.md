# 索财灵遗迹塔：宝箱怪、可疑砂砾与遗迹钱币

<div style="display: flex; gap: 24px; align-items: end; flex-wrap: wrap; margin: 16px 0 24px;">
  <figure style="margin: 0; text-align: center;">
    <img src="https://gitlab.com/cable-mc/cobblemon/-/raw/main/common/src/main/resources/assets/cobblemon/textures/pokemon/0999_gimmighoul/gimmighoul_chest.png" alt="索财灵宝箱形态官方游戏贴图" width="128" style="image-rendering: pixelated;" />
    <figcaption>索财灵：宝箱形态</figcaption>
  </figure>
  <figure style="margin: 0; text-align: center;">
    <img src="https://gitlab.com/cable-mc/cobblemon/-/raw/main/common/src/main/resources/assets/cobblemon/textures/item/relic_coin.png" alt="遗迹钱币官方游戏物品图标" width="64" style="image-rendering: pixelated;" />
    <figcaption>遗迹钱币</figcaption>
  </figure>
  <figure style="margin: 0; text-align: center;">
    <img src="https://gitlab.com/cable-mc/cobblemon/-/raw/main/common/src/main/resources/assets/cobblemon/textures/item/relic_coin_pouch.png" alt="遗迹钱币袋官方游戏物品图标" width="64" style="image-rendering: pixelated;" />
    <figcaption>遗迹钱币袋</figcaption>
  </figure>
</div>

截图中的建筑是 Cobblemon 自带的**索财灵遗迹塔**，源码内部名为 `gimmi_tower`。你在平原看到的石砖版本对应温带变种；塔顶那个看似原版箱子的“宝箱怪”是**索财灵**（Gimmighoul）的宝箱形态，不是普通箱子，也不是其他恐怖怪物模组加入的 Mimic。

::: tip 先给结论
这座塔值得完整搜一遍：塔顶的索财灵优先收服；可疑砂砾必须用刷子处理；遗迹钱币先留着喂给自己收服的索财灵，目标是累积到 `999` 枚并进化成赛富豪。
:::

## 为什么右键“宝箱”会开战

这是野生索财灵的宝箱形态。它不是可打开的容器：右键实体会按宝可梦交互进入战斗。

- 先不要用斧头或镐砸“箱子”；它不是方块战利品箱。
- 直接右键进入战斗后，按正常流程压低血量、施加异常状态，再投球收服。
- 索财灵的捕获率只有 `45`，属于偏难抓的个体；第一只建议保守使用高级球或更强的场景球。
- 野生索财灵生成等级为 `5-30`，这座塔本身就是其主世界生成条件之一。

索财灵本体是幽灵属性，速度只有 `10`，当前形态的战斗能力一般；但它的收藏与后续进化价值非常高。

## 可疑砂砾怎么处理

塔里/塔顶看到的不是普通砂砾，而是**可疑砂砾**。索财灵塔的生成处理器会给普通砂砾挂入常见战利品表，给可疑砂砾挂入更稀有的专属战利品表。

操作只有一条：

```text
手持刷子 -> 对可疑砂砾长按右键 -> 等挖掘动画完成 -> 捡起掉落
```

::: warning 不要徒手挖，也不要用铲子
普通挖掘会直接破坏可疑砂砾，不能保留考古战利品。先把周围的普通砂砾清开，再用刷子逐个处理；塔顶、角落和半埋位置都值得检查。
:::

## 遗迹钱币是做什么的

遗迹钱币的主要用途不是交易，而是让**已收服的索财灵进化**。

| 物品 | 给索财灵增加的硬币进度 | 制作/用途 |
| --- | ---: | --- |
| 遗迹钱币 | `1` | 直接右键喂给自己的索财灵；也可熔炼成 1 个金粒 |
| 遗迹钱币袋 | `9` | 工作台 `3 x 3` 放 9 枚钱币合成 |
| 遗迹钱币袋（大） | `81` | 工作台 `3 x 3` 放 9 个钱币袋合成；可拆回 9 个钱币袋 |

### 999 枚的正确使用方法

1. 先收服一只索财灵，放进队伍。
2. 在空地按 `R` 把它放出来。
3. 手持遗迹钱币、钱币袋或大钱币袋，对**自己拥有的**索财灵右键。
4. 每次会消耗手上的一个物品，并分别累积 `1 / 9 / 81` 点；进度封顶为 `999`。
5. 到 `999` 后，让它再升任意一级，自动进化为赛富豪（Gholdengo）。

源码会检查宝可梦归属，所以不能拿钱币喂野生索财灵，也不能替朋友的索财灵加进度。钱币数可在索财灵摘要页的金色进度栏查看。

::: tip 钱币先别急着烧成金粒
每枚钱币熔炼确实能变成 `1` 个金粒，耗时 100 tick 并给 0.1 经验；但 `999` 枚是赛富豪进化的硬门槛。还没完成一只赛富豪前，钱币更适合存下来或直接投喂。
:::

## 为什么值得进化成赛富豪

赛富豪是钢/幽灵属性，基础特攻 `133`，基础防御 `95`、特防 `91`、速度 `84`。相较索财灵，它才是这条遗迹线的组队回报：能学暗影球、加农光炮、十万伏特、诡计、恢复和专属招式“淘金潮”。

因此这座塔的推荐优先级是：

1. 收服第一只宝箱形态索财灵。
2. 用刷子处理全部可疑砂砾，搜集遗迹钱币。
3. 后续再遇到遗迹塔、废墟或野生索财灵，持续补钱币。
4. 钱币到 `999` 后升级进化赛富豪。

击败索财灵也会掉落 `24-48` 枚遗迹钱币，但会失去一个可培养个体。第一只一定收服；已有培养对象且缺钱币时，才考虑击败多余个体。

## 塔楼从哪里来

索财灵塔不是单一模板。源码登记了荒废、冰冻、繁茂、扎根、炽热与温带六类 `gimmi_tower` 结构，因此不同生物群系会看到不同石材、植被和高度组合。你截图里的平原石塔属于温带路线。

主世界中，索财灵会在 `#cobblemon:ruin` 标签的遗迹附近生成；遗迹塔正是最容易同时碰到宝箱形态、可疑砂砾和钱币战利品的地点。

## 源码依据

- `tags/worldgen/structure/gimmi_tower.json`：登记六类索财灵遗迹塔，包含温带石塔。
- `worldgen/processor_list/ruins/temperate_gimmi_tower.json`：为砂砾与可疑砂砾附加索财灵塔战利品表。
- `spawn_pool_world/0999_gimmighoul.json`：遗迹内的 `Lv.5-30` 索财灵生成规则。
- `species/generation9/gimmighoul.json`：捕获率 `45`、击败掉落 `24-48` 钱币及 `999` 钱币进化要求。
- `species_features/gimmighoul_coins.json` 与 `StashHandler.kt`：钱币、钱币袋、大钱币袋分别计 `1 / 9 / 81`，右键自己的已放出索财灵会消耗物品并累积进度。
- `recipe/relic_pouch_from_coin.json`、`recipe/relic_coin_sack.json`、`gold_nugget_from_smelting_coin.json`：钱币压缩与熔炼配方。
