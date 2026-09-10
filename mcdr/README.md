# Zyu Cobblemon Web

这是 `zyu-cobblemon-note` 同仓库维护的 MCDReforged 插件。网页使用 NiceGUI 3.16.0 构建，数据只通过 Fabric 模组的 HMAC loopback bridge 读取；不会解析世界存档、加载区块或修改游戏内容。

## 打包与部署

- 由根目录 GitHub Actions 在 `[build-action]` 构建中打包为 `zyu-cobblemon-web.mcdr`。
- 部署时只能安装 CI artifact，不能把此源码目录直接复制进 MCDR `plugins/`。
- MCDR 会在 `config/zyu_cobblemon_web/config.json` 自动生成运行时配置。该文件包含密钥与登录配置，不可提交。

## 运行时配置

至少填入与 Fabric 模组一致的 `bridge_secret`。网页默认公开只读状态与物资查询；AI 请求需要登录且仅在安装了含 `ask_readonly()` API 的 `Games_AI` Cobblemon 分支后可用。

`auth.password_hash` 使用 PBKDF2-SHA256 格式。部署前使用下方独立工具生成：

```bash
uv run python create_password_hash.py '替换为真实密码'
```

将输出填入私有配置。公网 HTTP 会明文传输登录会话，当前仅适合小型私服；不要复用重要密码。
