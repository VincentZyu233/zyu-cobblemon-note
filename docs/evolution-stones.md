# 进化之石：别急着乱用

<div class="stone-strip">
  <img src="/assets/evolution/leaf_stone.png" alt="叶之石"><img src="/assets/evolution/sun_stone.png" alt="日之石"><img src="/assets/evolution/moon_stone.png" alt="月之石"><img src="/assets/evolution/dusk_stone.png" alt="暗之石"><img src="/assets/evolution/dawn_stone.png" alt="觉醒之石"><img src="/assets/evolution/fire_stone.png" alt="火之石"><img src="/assets/evolution/water_stone.png" alt="水之石"><img src="/assets/evolution/thunder_stone.png" alt="雷之石"><img src="/assets/evolution/ice_stone.png" alt="冰之石"><img src="/assets/evolution/shiny_stone.png" alt="光之石">
</div>

它们的共同提示“进化特定宝可梦”并不是泛用强化。只有物种源码中声明了对应石头的宝可梦才会响应；石头不会给任意宝可梦加属性，也不能替代升级。

::: tip 你的当前结论
你目前常用的妙蛙种子、铜镜怪、榛果球、火狐狸、电电虫都**不使用**叶/日/月/暗之石进化。先存进“进化石”箱，等抓到对应物种再用，完全不亏。
:::

## 如何使用

将进化之石拿在主手，对已放出的、属于自己的目标宝可梦右键，或在队伍摘要中使用物品并选择目标。若它符合条件，会立即开始进化；不符合时不会变强，也不应消耗石头。

分支进化务必先确认：例如臭臭花用叶之石变霸王花、用日之石变美丽花，是两条不同路线。

## 十种进化石完整对应表

| 石头 | 可进化对象（Cobblemon 1.8 源码） | 关键提醒 |
| --- | --- | --- |
| <img src="/assets/evolution/leaf_stone.png" class="item-icon"> 叶之石 | 伊布→叶伊布；臭臭花→霸王花；口呆花→大食花；蛋蛋→椰蛋树；长鼻叶→狡猾天狗；花椰猴→花椰猿 | 蛋蛋在不同地区可能得到不同地区形态。 |
| <img src="/assets/evolution/sun_stone.png" class="item-icon"> 日之石 | 臭臭花→美丽花；向日种子→向日花怪；木棉球→风妖精；百合根娃娃→裙儿小姐；伞电蜥→光电伞蜥 | 臭臭花的分支要先想清楚。 |
| <img src="/assets/evolution/moon_stone.png" class="item-icon"> 月之石 | 尼多娜→尼多后；尼多力诺→尼多王；皮皮→皮可西；胖丁→胖可丁；向尾喵→优雅猫；食梦梦→梦梦蚀 | 适合留给尼多系或皮皮系。 |
| <img src="/assets/evolution/dusk_stone.png" class="item-icon"> 暗之石 | 黑暗鸦→乌鸦头头；梦妖→梦妖魔；灯火幽灵→水晶灯火灵；双剑鞘→坚盾剑怪 | 幽灵系和恶系的高价值进化石。 |
| <img src="/assets/evolution/dawn_stone.png" class="item-icon"> 觉醒之石 | **雄性**奇鲁莉安→艾路雷朵；**雌性**雪童子→雪妖女 | 性别不符不会触发。 |
| <img src="/assets/evolution/fire_stone.png" class="item-icon"> 火之石 | 伊布→火伊布；六尾→九尾；卡蒂狗→风速狗；爆香猴→爆香猿；辣椒小兵→狠辣椒 | 想走火伊布或风速狗时再用。 |
| <img src="/assets/evolution/water_stone.png" class="item-icon"> 水之石 | 伊布→水伊布；蚊香君→蚊香泳士；大舌贝→刺甲贝；海星星→宝石海星；莲帽小童→乐天河童；冷水猴→冷水猿 | 水伊布是前中期耐久水系候选。 |
| <img src="/assets/evolution/thunder_stone.png" class="item-icon"> 雷之石 | 皮卡丘→雷丘；伊布→雷伊布；电电虫二阶(电束木)→锹农炮虫；麻麻鳗→麻麻鳗鱼王；三合一磁怪→自爆磁怪；朝北鼻→大朝北鼻；光蚪仔→电肚蛙 | 你抓到的是**电电虫**，先升为电束木，才用雷之石。 |
| <img src="/assets/evolution/ice_stone.png" class="item-icon"> 冰之石 | 伊布→冰伊布；好胜蟹→好胜毛蟹；走鲸→浩大鲸 | 冰系路线专用，前期可先收藏。 |
| <img src="/assets/evolution/shiny_stone.png" class="item-icon"> 光之石 | 波克基古→波克基斯；毒蔷薇→罗丝雷朵；泡沫栗鼠→奇诺栗鼠；花叶蒂→花洁夫人 | 偏妖精/草系的关键分支。 |

## 什么时候该立刻进化

石头进化通常带来更高种族值，但有些宝可梦在进化前会在升级时学到你想要的招式。最稳的流程是：打开 `M` 的摘要或图鉴，先看当前形态后续可学招式；想学的已经拿到，再进化。

对你现在最接近的项目是电电虫线：先培养电电虫，进化成电束木后，雷之石可进化为锹农炮虫。叶、日、月、暗之石暂时都建议保管。

## 来源与保存

进化石可由探索战利品、野生宝可梦掉落和对应矿石获得。不要把它们放在公共杂物箱，建议基地单独设“进化石与进化道具”箱，并在石头旁放告示标注想保留给哪个目标。

## 源码依据

本页完整表格由 Cobblemon 1.8 `species/**.json` 中所有 `variant: item_interact` 且 `requiredContext` 为对应进化石的条目整理；图片来自客户端 `Cobblemon-fabric-1.8.0+1.21.1.jar` 的原始物品纹理。

<style>
.stone-strip { display: flex; flex-wrap: wrap; gap: 12px; margin: 18px 0 24px; }
.stone-strip img, .item-icon { width: 42px; height: 42px; image-rendering: pixelated; object-fit: contain; vertical-align: middle; }
.item-icon { width: 28px; height: 28px; }
</style>
