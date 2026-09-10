## Zyu Cobblemon Note 测试构建

这是由 `[build-release]` 显式触发的预发布构建，不代表已在正式服务器长期验证稳定。

- 适用：Minecraft 1.21.1、Fabric、Cobblemon 1.8.x。
- 内容：Fabric Jar、MCDR Web 插件和 Node 网关部署包。
- 安装：将 Jar 放入服务端 `mods/`，保持 Fabric Language Kotlin 与 Cobblemon 已安装；MCDR 和网关均使用对应 CI artifact 的包。
- 不含 OpenAI Key、基地坐标、SSH 信息或任何玩家数据。

GitHub Actions artifact 是日常测试部署的首选。只有完成实际服务器验证后，才应使用 `[build-release]` 创建本页对应的预发布。
