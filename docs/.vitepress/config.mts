import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

const base = '/zyu-cobblemon-note/'

export default withMermaid(defineConfig({
  base,
  title: 'Cobblemon 1.8 指南',
  description: 'Zyu 的 Cobblemon 1.8 Fabric 整合包个人游玩笔记',
  lang: 'zh-CN',
  cleanUrls: true,
  head: [
    ['meta', { name: 'theme-color', content: '#b5363d' }],
    ['meta', { name: 'keywords', content: 'Cobblemon, 宝可梦, Minecraft, Fabric, 1.8' }],
    ['link', { rel: 'icon', type: 'image/png', href: `${base}assets/icon.png` }]
  ],
  themeConfig: {
    logo: `${base}assets/icon.png`,
    nav: [
      { text: '从这里开始', link: '/' },
      { text: '新手开局', link: '/getting-started' },
      { text: '进阶推进', link: '/progression' },
      { text: '一起听歌', link: '/music' },
      { text: '资料', link: '/reference' }
    ],
    sidebar: [
      {
        text: '游玩路线',
        items: [
          { text: '总览', link: '/' },
          { text: '第一天到第一队', link: '/getting-started' },
          { text: '抓捕与图鉴', link: '/catching' },
          { text: '精灵球：按场景选择', link: '/pokeballs' },
          { text: '专题：寻找与收服皮卡丘', link: '/pikachu' },
          { text: '实战：收服圈圈熊', link: '/ursaring' },
          { text: '培养、升级与队伍成型', link: '/training' },
          { text: '设备与机器专题', link: '/machines/' },
          { text: '据点与资源循环', link: '/settlement' },
          { text: '树果肥料：种植与变异', link: '/fertilizers' },
          { text: '宝可梦钓竿与鱼饵', link: '/fishing' },
          { text: '牧场：资源、宠物与安全经营', link: '/ranch' },
          { text: '同行、肩扛与坐骑', link: '/movement' },
          { text: '交换、赠送与换主', link: '/trading' },
          { text: '中后期推进', link: '/progression' },
          { text: '进化之石：别急着乱用', link: '/evolution-stones' },
          { text: '多人服务器玩法', link: '/multiplayer' }
        ]
      },
      {
        text: '设备与机器',
        items: [
          { text: '总览：基地设备怎么分工', link: '/machines/' },
          { text: '治疗仪：整队恢复', link: '/machines/healing-machine' },
          { text: '电脑：宝可梦盒子', link: '/machines/pc' },
          { text: '牧场方块：放牧与互动', link: '/machines/pasture' },
          { text: '营火锅：药品与料理', link: '/machines/campfire-pot' },
          { text: '复原培养皿：化石 DNA 槽', link: '/machines/restoration-tank' },
          { text: '数据显示器：化石机控制台', link: '/machines/monitor' },
          { text: '化石分析仪：放入化石', link: '/machines/fossil-analyzer' },
          { text: '招式学习器机：制作 TM', link: '/machines/tm-machine' }
        ]
      },
      {
        text: '服务器扩展',
        items: [
          { text: '一起听歌与全服点歌', link: '/music' },
          { text: '服务器性能监控', link: '/performance' },
          { text: '定时备份与异地恢复', link: '/server-backup' },
          { text: '便利指令与权限计划', link: '/server-command-plan' },
          { text: 'AI 集成：实时进度助手', link: '/ai' }
        ]
      },
      {
        text: '常见宝可梦专题',
        items: [
          { text: '总览：遇到后该抓还是该打', link: '/creatures/' },
          { text: '三蜜蜂与蜂女王：基地蜂场', link: '/creatures/combee-vespiquen' },
          { text: '水母家族：玛瑙水母与毒刺水母', link: '/creatures/tentacool-tentacruel' },
          { text: '索财灵遗迹塔：宝箱怪与遗迹钱币', link: '/creatures/gimmighoul-tower' }
        ]
      },
      {
        text: '资源专题',
        items: [
          { text: '紫水晶洞与 TM 矿场', link: '/resources/amethyst' },
          { text: '铜：球与设备', link: '/resources/copper' },
          { text: '铁：捕获与设施', link: '/resources/iron' },
          { text: '金：金阶精灵球', link: '/resources/gold' },
          { text: '红石：治疗与 TM 工坊', link: '/resources/redstone' },
          { text: '钻石：后期持有物', link: '/resources/diamond' },
          { text: '属性宝石母岩与电之宝石', link: '/resources/type-gems' }
        ]
      },
      {
        text: '查阅',
        items: [
          { text: '按键、物资与外部资料', link: '/reference' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/VincentZyu233/zyu-cobblemon-note' },
      { icon: 'gitlab', link: 'https://gitlab.com/cable-mc/cobblemon' }
    ],
    outline: { label: '本页内容', level: [2, 3] },
    docFooter: { prev: '上一页', next: '下一页' },
    footer: { message: '基于 Cobblemon 1.8.0 官方公开资料整理', copyright: 'Minecraft 与 Pokemon 相关权利归各自权利人所有。' }
  }
}))
