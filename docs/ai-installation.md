# AI Bridge：部署与连接

## 构建与测试部署

仓库禁止本地 Gradle/Maven/Java 构建。提交信息加入 `[build-action]` 后，GitHub Actions 才会构建 Fabric Jar 并上传 artifact；日常文档提交不会触发它。

::: tip Release 规则

`[build-release]` 是 `[build-action]` 的超集，会在构建成功后创建预发布。只有已在实际服务器测试稳定的版本才能使用它。现在安装测试版时，只下载 Actions artifact。

:::

将 artifact 中的非 `-sources` Jar 放进服务端 `mods/`，再启动服务端一次，让它生成：

```text
config/zyu-cobblemon-note.json
```

填写 `sharedSecret` 后重启。这个随机长字符串必须与网关的 `BRIDGE_SECRET` 完全相同，不要发到聊天记录或提交进仓库。

## 网关配置

在 `gateway/` 中安装依赖并由部署环境运行。将 `.env.example` 复制为未提交的 `.env`，配置：

```dotenv
OPENAI_API_KEY=...
OPENAI_BASE_URL=...
OPENAI_MODEL=...
BRIDGE_SECRET=与服务端相同的随机字符串
BRIDGE_URL=http://127.0.0.1:25931
```

远程部署时，`.env` 权限应设为 `600`。它可以与服务端同机运行，两个端口均绑定 `127.0.0.1`，不需要开放公网端口。

MCP 的 stdio 通道由网关进程提供；本地 AI 客户端需要访问 HTTP bridge 时，可用 SSH 将远程 `25931` 转发到本机。不要将这两个端口配置到 NAT 或防火墙公网规则中。

## 基地登记

在服务端配置的 `bases` 中，既可写一个 `regions` 立方体扫描范围，也可在 `targets` 登记区域外的单个容器或机器。小范围优先；`maxRegionBlocks` 默认 `32768`，过大的范围会被拒绝。
