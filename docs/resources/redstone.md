# 红石：从治疗机到 TM 工坊

<figure class="resource-hero">
  <img src="/assets/resources/redstone.png" alt="原版 Minecraft 红石矿与红石元件" />
  <figcaption>红石不只用于原版自动化；在这个包里，它连接着治疗、TM 和多种宝可梦设备。</figcaption>
</figure>

## 原版挖矿路线

红石矿生成在 `Y=-64` 到 `Y=15`，越往深处越常见。需要专门补红石时，推荐在 `Y=-54` 到 `Y=-59` 的深板岩层分支挖矿；`Y=-54` 相对更容易避开最底层岩浆与基岩阻碍。

::: tip 带铁镐以上工具
红石矿需要铁镐或更高等级工具。时运对红石很划算，后续做机器和原版自动化都会长期消耗红石粉。
:::

## Cobblemon 的优先用途

| 优先级 | 物品 | 红石的作用 |
| --- | --- | --- |
| 高 | 治疗机 | 配方需要 1 红石粉，是基地续航核心 |
| 高 | TM Machine | 配方需要 1 红石，是 TM 工坊核心 |
| 中 | 图鉴 | 可作为图鉴配方的“屏幕”材料之一 |
| 中 | 化石分析仪、复活设备链 | 设备配方会持续需要红石 |
| 原版 | 活塞、漏斗、侦测器和农场 | 可与球果园、储物区、牧场周边建设结合 |

::: warning 机器不用接红石线
治疗机源码会自行充能；TM Machine 也不是红石电路设备。红石在这些配方里是材料，不代表必须额外铺线路供电。
:::

## 本页依据

- [Minecraft Wiki: Redstone](https://minecraft.wiki/w/Redstone)：原版深层分布和采集规则。
- Cobblemon 1.8 源码：`recipe/healing_machine.json`、`recipe/tm_machine.json`、`recipe/fossil_analyzer.json`。
- 配图来自 [Minecraft Wiki 的红石资源图片](https://minecraft.wiki/w/Redstone)，按该站点标注的许可使用。
