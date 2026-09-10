# 提交约定

- 本仓库的每次 Git 提交都必须使用 Conventional Commits 风格的标题，并在正文中简洁说明变更。
- 每次提交信息末尾必须保留以下协作者尾注，前面空一行：

  ```text
  Co-authored-by: Codex <codex@openai.com>
  ```

- Fabric 模组默认由 GitHub Actions 构建；本地构建、测试与缓存仅在仓库存在未提交的 `AGENTS.local.md` 时允许，并必须遵循其中的缓存位置和代理约定。若该文件不存在，则禁止在本机运行 Java、Gradle、Maven 或其他本地构建命令，只使用 GitHub Actions，并提醒维护者按当前设备创建该本地文件。
- Fabric 模组尽可能使用 Kotlin：业务逻辑、数据模型、命令、网络处理和测试等可选实现默认均采用 Kotlin。除非 Kotlin 无法实现或用户明确指定，否则不得新增 Java 源文件；构建脚本、资源元数据等则使用其工具链要求的原生格式。优先复用 Cobblemon/Fabric 的 Kotlin API 与惯用写法。
- 模组构建只由 `main` 分支提交信息中的关键词触发：`[build-action]` 仅构建并上传 GitHub Actions artifact；`[build-release]` 是其超集，成功构建后才创建 GitHub Release。
- 日常提交不得带上述关键词。只有用户确认实际服务器测试稳定后，才可使用 `[build-release]`、创建发布标签或发布公开下载；测试部署一律使用 `[build-action]` 的 artifact。
- 所有可部署组件遵循 `x.y.z` 语义化版本：`x` 仅用于不兼容变更，例如需要调整部署方式、配置格式、权限模型或客户端/服务端协议；`y` 用于向后兼容的新功能，例如新增指令、页面、查询能力或配置项；`z` 仅用于向后兼容的修复、样式调整、文档纠正和打包改进。一次提交如同时满足多项，以影响最大的级别递增，并将较低位归零。
- 自研 MCDR 插件的 `mcdr/mcdreforged.plugin.json` 与 `mcdr/pyproject.toml` 版本必须保持一致；打包文件名必须为 `zyu-cobblemon-web-<x.y.z>.mcdr`，版本从 manifest 自动读取，不能硬编码或省略。

## MCDR 与部署

- 自研 MCDR 插件的源码、静态网页、测试和打包脚本统一放在 `mcdr/`，与 Fabric 模组、网关和 VitePress 文档同仓库维护。
- 所有实际部署都必须使用 GitHub Actions 生成的 server artifact；远程 Git 工作树只用于同步源码、文档和部署脚本，绝不直接复制到 MCDR `plugins/`。
- MCDR Python 插件支持 Python `>=3.11,<3.14`。本机和远程均优先通过 `uv` 选择合适的稳定解释器；CI 当前以 Python 3.12 验证。部署 manifest 必须记录实际解释器版本，不能假定某个设备固定使用 3.11。
- MCDR、Games_AI、Fabric Bridge 的 API Key、密码哈希、HMAC 密钥、代理和运行时配置只能放在远程私有配置或被忽略文件中，不能进入 artifact、Git 或公开文档。

## Import 约定

- 所有 Python 与 Kotlin 源文件的 import 必须保持稳定、可读的顺序；提交前应检查本次涉及文件及对应源码目录中的全部此类文件。
- Python import 按“标准库、第三方依赖、项目内模块”分组，组间保留一个空行；每组按完整模块路径字母序排列。为了在导入项目模块前调整 `sys.path` 而出现的路径注入语句可以位于标准库组之后，但不得打乱各组顺序。
- Kotlin import 按来源分组：外部库（如 `com.*`）、Fabric/Minecraft 平台（如 `net.*`）、JDK（`java.*`/`javax.*`）；组间保留一个空行，组内按完整导入路径字母序排列。`kotlin.*` 若显式导入，单独置于 JDK 组之前并按字母序排列。

