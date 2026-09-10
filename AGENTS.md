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

