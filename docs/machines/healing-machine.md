# 基地治疗仪：从活力块到无限续航

<figure class="resource-hero">
  <img src="/assets/healing/healing-machine-charged.png" alt="充能后的 Cobblemon 治疗仪方块" />
  <figcaption>治疗仪是基地最优先的长期设施。它不需要接红石线，但需要时间自行充能。</figcaption>
</figure>

治疗仪会治疗随身队伍的 HP、异常状态和招式 PP，并能复活昏厥成员。它不是一次性消耗品：造好后放在家里，之后只需要等待充能。

::: tip 先做一台公共治疗仪
两人一起探索时，把它放在出生点或公共基地入口最划算。打完高等级野生宝可梦、下界远征或采矿回来，都可以快速恢复整队。
:::

## 怎么使用

1. 放置治疗仪，刚放下时电量是空的。
2. 等它自行充能；默认从 0 到满需要 `900 秒`，也就是约 `15 分钟`。
3. 空手右键治疗仪。它会收回你的随身宝可梦，播放治疗过程，然后恢复队伍。
4. 一次治疗会消耗已有电量。电量不足时，右键会提示所缺的百分比；等一会再试即可。

::: warning 两个边界
- 治疗仪不能在战斗中使用。
- 默认只治疗随身 6 只；电脑仓库内的宝可梦不在这次治疗范围中。
:::

## 治疗仪配方

```text
铜锭  铜锭  铜锭
铁锭  活力块 铁锭
铁锭  红石粉  铁锭
```

总材料：`3 铜锭 + 4 铁锭 + 1 红石粉 + 1 活力块`。

铜、铁和红石是常规矿产；真正卡住第一台治疗仪的是中心的**活力块**。

## 活力块：完整制作链

```text
复活草 -> 万能粉
万能粉 + 蜂蜜瓶 --营火锅--> 活力碎片
活力碎片 x2 + 活力蕾 --营火锅--> 活力块
```

因此，做一台治疗仪最终需要：**2 复活草、2 蜂蜜瓶、1 活力蕾**。

::: danger 不要把材料名看错
`活力块` 是 `max_revive`，不是元气根，也不是大根茎。它的实际配方是 **2 活力碎片 + 1 活力蕾**；活力块本身也能让一只昏厥宝可梦满血复活。
:::

## 材料 1：复活草与万能粉

<figure class="resource-hero">
  <img src="/assets/healing/revival-herb-mature.png" alt="成熟的 Cobblemon 复活草方块" />
  <figcaption>成熟复活草。优先到繁茂洞穴地表、苔藓和植被密集的区域仔细找。</figcaption>
</figure>

复活草是繁茂洞穴中的野生作物，但它也**本身就是种子**。它既能在工作台分解为 **1 万能粉**，也能直接手持右键种到湿润耕地上；二者只能二选一。做第一台治疗仪最终会消耗两株复活草，因此第一次采集时不要全分解，至少留 2 株建立田地。

成熟的自种复活草会掉落本体和额外复活草；源码的掉落表在完全成熟时给出 3 至 4 株总产出。留出一部分继续播种，剩余部分再分解成万能粉，便能稳定量产。

**远征路线：** 带剪刀、食物、火把和空背包，沿大型洞穴寻找苔藓、垂滴叶、杜鹃树与孢子花组成的繁茂洞穴；见到复活草先采，别把它当普通杂草跳过。带回基地后先在耕地上播种；骨粉可加速其 8 个生长阶段。

## 材料 2：蜂蜜瓶

<figure class="resource-hero">
  <img src="/assets/healing/beehive.png" alt="原版 Minecraft 蜂箱方块" />
  <figcaption>原版蜂巢或蜂箱蜂蜜等级满后，手持玻璃瓶右键即可取得蜂蜜瓶。</figcaption>
</figure>

有两条稳定路线：

- **原版蜂场：** 放蜂箱、花和原版蜜蜂；蜂蜜等级满后用玻璃瓶右键取蜜。
- **宝可梦牧场：** 抓到蜂女王后，放入牧场，对它手持玻璃瓶右键可取得蜂蜜瓶。该交互有约 15 分钟冷却，适合把蜂蜜接入基地的慢速资源循环。

三蜜蜂的野生行为也会参与原版蜂巢生态，但被捕获后不会成为可靠的蜂蜜机器；想量产时优先原版蜜蜂或牧场蜂女王。

将 **1 万能粉 + 1 蜂蜜瓶** 放进营火锅，得到 **1 活力碎片**。重复两次。

## 材料 3：活力蕾

<figure class="resource-hero">
  <img src="/assets/healing/vivichoke-mature.png" alt="成熟的 Cobblemon 活力蕾作物" />
  <figcaption>成熟活力蕾。拿到第一份后，优先转成种子并在基地耕地量产。</figcaption>
</figure>

活力蕾和活力蕾种子较稀有。优先检查村庄房屋、废弃矿井、沉船补给箱、地牢、丛林神庙与林地府邸箱子；部分遗迹和化石结构战利品中也会出现种子。

拿到活力蕾后，工作台可将 **1 活力蕾合成 1 活力蕾种子**。将种子种在耕地上，像普通作物一样等待成长或使用骨粉催熟。第一株不要全部吃掉或做菜，先转种子建立小田。

## 最后一步：营火锅合成活力块

在营火锅放入：

```text
活力碎片 + 活力碎片 + 活力蕾
```

得到 **1 活力块** 后，按治疗仪配方在工作台合成，带回基地放置并等待第一次充能。

::: tip 第一台之后怎么补货
复活草和活力蕾都值得留一部分做种植/储备；蜂蜜则建立原版蜂场或蜂女王牧场。治疗仪造出后，活力块不再是日常消耗，但这条药物线仍能做野外复活物品。
:::

## 源码与配图依据

- `recipe/healing_machine.json`：治疗仪的铜、铁、红石与活力块配方。
- `recipe/heal_powder.json`、`recipe/campfire_pot/revive.json`、`recipe/campfire_pot/max_revive.json`：万能粉、活力碎片、活力块的完整链条。
- `HealingMachineBlock.kt`、`HealingMachineBlockEntity.kt`：右键使用、队伍治疗和默认 900 秒充能。
- `configured_feature/revival_herb.json`：复活草野生生成；`VivichokeBlock.kt`：活力蕾为可种植作物。
- [Cobblemon Wiki: Healing Machine](https://wiki.cobblemon.com/index.php/Healing_Machine)、[Revival Herb](https://wiki.cobblemon.com/index.php/Revival_Herb)、[Vivichoke](https://wiki.cobblemon.com/index.php/Vivichoke)：治疗仪、复活草、活力蕾方块配图。
- [Minecraft Wiki: Beehive](https://minecraft.wiki/w/Beehive)：蜂箱配图与原版取蜜机制。
