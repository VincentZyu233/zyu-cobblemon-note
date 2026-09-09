# 第一天到第一队

## 进服后的 15 分钟

选好御三家后，先按原版生存的节奏拿到木头、食物、石器和床。宝可梦很重要，但夜晚没有床、没有食物时，探索效率会很低。

按 `Esc -> 选项 -> 控制`，搜索 `Cobblemon`，把按键确认一遍。`R` 是最常用的键：放出/收回当前选中的宝可梦，也用于许多交互。默认用上、下方向键切换选中的队伍成员；按键冲突时，以这里显示的实际绑定为准。具体操作见[按键、物资与资料](/reference#指定放出的宝可梦)。

## 第一项专属资源：球果

球果树是开局最值得寻找的东西。它不是原版树的掉落物，而是 Cobblemon 独立生成的树种：浅色的球果原木、同色树叶和挂在树叶侧面的果实，是最容易辨认的特征。球果是大多数精灵球的核心材料，所以第一次外出不必跑太远，优先拿到几种颜色并开始扩种。

<figure class="apricorn-hero">
  <img src="/assets/apricorn/red-tree.png" alt="一棵带有红色球果的 Cobblemon 球果树" />
  <figcaption>野外看到彩色树冠、浅色原木，以及挂在叶片侧面的果实，就找对了。</figcaption>
</figure>

### 往哪里找最快

本版本源码将以下地区标为球果树的**高密度**生成区：森林、丛林、针叶林、积雪森林、积雪针叶林，以及沙漠和恶地。丘陵、草原、灌木地和稀疏丛林是普通密度；冻原则稀疏。因此开局最舒服的路线是沿森林或针叶林的边缘走，能顺手获得木材、食物和草地宝可梦；找不到时再把沙漠、恶地当作备选远征目标。

不同颜色不是按生物群系固定分配的。源码的世界生成器会在七种颜色中随机选择树种，因此找到一片高密度区域后，沿边缘多走几个区块，比为了某种颜色频繁换地形更有效。

### 先认树，再收集颜色

七种球果树的结构相同，树冠与果实颜色不同。刚起步时不必强求集齐，见到一种新颜色就至少采一颗成熟果实；拿到种子后再带回基地扩种。颜色不是群系专属，所以发现球果林后，多绕几圈通常比跨地图换群系更划算。

<div class="apricorn-tree-grid">
  <figure><img src="/assets/apricorn/red-tree.png" alt="红色球果树" /><figcaption>红色</figcaption></figure>
  <figure><img src="/assets/apricorn/yellow-tree.png" alt="黄色球果树" /><figcaption>黄色</figcaption></figure>
  <figure><img src="/assets/apricorn/green-tree.png" alt="绿色球果树" /><figcaption>绿色</figcaption></figure>
  <figure><img src="/assets/apricorn/blue-tree.png" alt="蓝色球果树" /><figcaption>蓝色</figcaption></figure>
  <figure><img src="/assets/apricorn/pink-tree.png" alt="粉色球果树" /><figcaption>粉色</figcaption></figure>
  <figure><img src="/assets/apricorn/white-tree.png" alt="白色球果树" /><figcaption>白色</figcaption></figure>
  <figure><img src="/assets/apricorn/black-tree.png" alt="黑色球果树" /><figcaption>黑色</figcaption></figure>
</div>

### 怎样采才不会亏

球果有 4 个生长阶段。只有最后一阶段体积最大、颜色最明显时才采；对成熟果实右键，或直接徒手击打，都能收获 1 个球果，果实会重置为第 0 阶芽并留在原树上继续长。源码和官方 Wiki 都确认：收获时有 **10%** 几率掉落同色种子。

不要挖掉果实本体或把整棵树砍掉当作主要采集手段：未成熟果实被破坏不会掉物品。第一次到一片球果林时，正确动作是绕树逐个收成熟果实，顺手记坐标，隔一段时间再回来。

### 成熟判断：等它长满再右键

下图以红球果为例。前三张是未成熟阶段，最后一张才是可收获的成熟状态。成熟果实体积最大、颜色最完整；采下后会回到第 0 阶并继续生长。对果实右键即可采收，不要为了拿果实把整棵树砍掉。

<div class="apricorn-growth-grid">
  <figure><img src="/assets/apricorn/stage-0.png" alt="球果第 0 阶生长状态" /><figcaption>第 0 阶：刚重置</figcaption></figure>
  <figure><img src="/assets/apricorn/stage-1.png" alt="球果第 1 阶生长状态" /><figcaption>第 1 阶：未成熟</figcaption></figure>
  <figure><img src="/assets/apricorn/stage-2.png" alt="球果第 2 阶生长状态" /><figcaption>第 2 阶：未成熟</figcaption></figure>
  <figure><img src="/assets/apricorn/stage-3-mature.png" alt="成熟的红色球果" /><figcaption>成熟：可以采收</figcaption></figure>
</div>

### 从野外采集变成自己的球果园

### 先分清：能扩果实，也能种树

野外发现的树应当视为长期保留的**母树**，不要砍掉。成熟采收时出现的同色球果种子有两种用法：对球果树叶侧面使用，会长出一个新的果实位；对草方块或泥土顶面使用，则会种成对应颜色的球果树苗。

因此，绿色球果的正确循环是“保留绿树 -> 收成熟绿球果 -> 等 10% 概率获得绿种子 -> 种在草地扩成新树，或贴到树叶上增加果实位”。树苗可用骨粉催长。

1. 每种颜色先留至少 1 个种子，不要全拿去做球。
2. 想扩种时，把种子用在草方块或泥土顶面；想提高一棵现有树的产量时，把种子贴到球果树叶侧面。
3. 球果通常约需一个 Minecraft 日从芽长到成熟。源码显示自然生长每次随机刻有 20% 概率推进一阶段；对未成熟果实用骨粉会稳定推进 1 阶，急需做球时很有用。
4. 用不同颜色的告示牌或箱子分开保存。基础红球果配铜可做 Poké Ball；后面 Great Ball、Ultra Ball 和功能球会要求混色与铁、金等材料。

::: tip 小服里的高效做法
第一次发现球果树的人，把成熟果实、颜色和坐标报到聊天里即可。大家轮流采收同一片天然树林，再优先把种子带回公共球果园，通常比每个人单独找一片林子更快形成稳定产量。
:::

### 代码依据

- [ApricornTreeFeature.kt](https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/world/feature/ApricornTreeFeature.kt)：高/中/低密度倍率分别为 `10`、`1`、`0.1`，树种从 7 色中随机选择，生成时果实成熟度随机。
- [ApricornBlock.kt](https://gitlab.com/cable-mc/cobblemon/-/blob/main/common/src/main/kotlin/com/cobblemon/mod/common/block/ApricornBlock.kt)：4 阶生长、成熟收获后复位、随机生长和骨粉逻辑。
- [Apricorn 官方 Wiki](https://wiki.cobblemon.com/index.php/Apricorn)：10% 种子掉率、约一个游戏日的常规生长时间与配方。
- 本节球果树与生长阶段图片来自 [Cobblemon Wiki 的 Apricorn 词条](https://wiki.cobblemon.com/index.php/Apricorn)，按该站点标注的 CC BY 4.0 许可使用。

::: warning 不要按旧视频背配方
Cobblemon 的精灵球体系在较新版本中改过。这个包是 1.8，打开 EMI/JEI 查配方永远比照搬旧教程可靠。
:::

## 第一支队伍怎么组

不需要一开始就找“最强六只”。先让队伍能处理不同地形和属性即可：

- 选一只你最愿意长期用的主力，等级不要远超其余成员。
- 再找一只水、火、草、电中的缺口属性。
- 需要一只偏耐久或能施加异常状态的成员。
- 见到有用的飞行、地面、岩石属性先扫描或捕获，探索时很有价值。

你可以在野外试战，但遇到明显高等级的宝可梦就撤。早期失败的代价是赶路和资源，而不是面子。

## 第一座家应该放什么

先留出四个区域：床和储物、球果园、熔炼区、宝可梦设施预留区。之后会陆续加入 PC、治疗设施、图鉴和 TM Machine；前期不要把家盖得太满。

完成这些后，继续看 [抓捕与图鉴](/catching)。
