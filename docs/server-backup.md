# Minecraft 定时备份：systemd、OpenList 与异地副本

游戏存档不是只要“复制一次”就算备份。可靠的方案需要同时解决三件事：让存档处于可恢复的一致状态、在本机保留一小段可快速恢复的历史、再把副本送到另一台机器或网盘。

这页给出一个可复用的 Linux 服务器方案：Minecraft 通过 RCON 进行一致性保存，本地保留压缩归档，OpenList 仅在本机回环地址提供受限 WebDAV，再上传到网盘。所有路径、地址与账号均以变量表示，复制前替换为自己的环境即可。

::: warning 不要把凭据提交到仓库
RCON 密码、网盘 Refresh Token、OpenList 管理员密码、WebDAV 密码、rclone 配置内容和 SSH 私钥都属于凭据。把它们放在 root-only 的独立文件中，并在脚本或 systemd 单元里只引用文件路径。
:::

## 目标与结构

建议至少保留三层：正在运行的服务器数据、本地归档和异地归档。异地副本能应对服务器磁盘损坏、误删、整机失联和场地事故；本地副本则能更快恢复近期误操作。

```text
Minecraft 服务端
  -> RCON: save-off / save-all flush
  -> tar + zstd 本地归档
  -> SHA-256 与 zstd 完整性检查
  -> OpenList WebDAV（仅 127.0.0.1）
  -> 网盘的受限备份目录
```

下面是一组适合中小型服务器的起点，实际按存档大小、带宽和风险承受能力调整：

| 项目 | 示例值 | 作用 |
| --- | --- | --- |
| 创建频率 | 每小时 `:30` | 限制单次可丢失的进度 |
| 本地保留 | 5 份 | 快速恢复且不长期占满 VPS 磁盘 |
| 网盘保留 | 168 份 | 约 7 天的小时级异地历史 |
| 上传扫描 | 每 10 分钟 | 上传失败后自动补传，不阻塞下一次归档 |
| 归档排除 | `logs/` | 避免不断变化且通常不参与恢复的日志 |

::: tip 先估算空间
归档阶段会同时存在源目录和新归档，本地磁盘至少要容纳源目录、一个最大归档和保留窗口。OpenList 上传临时目录可放进 tmpfs，避免额外写入系统盘；tmpfs 的容量仍需覆盖一次上传可能使用的峰值内存。
:::

## 让存档可恢复

不要直接在运行中的世界目录上打包。备份脚本应通过仅绑定到回环地址的 RCON，按这个顺序执行：

```mcfunction
save-off
save-all flush
```

随后用 `tar` 配合 zstd 创建归档，完成或失败时都必须恢复自动保存：

```mcfunction
save-on
```

脚本要用 `trap` 或等价的 `finally` 块保证 `save-on` 一定会执行。归档完成后先检查压缩流，再计算校验值：

```bash
zstd --test "<ARCHIVE_FILE>"
sha256sum "<ARCHIVE_FILE>" > "<ARCHIVE_FILE>.sha256"
```

只有两项检查都成功时，才把该归档标记为可上传。上传 `.sha256` 侧车文件，能让下载恢复时确认文件没有损坏或传错。

## OpenList：只作为本机上传入口

OpenList 与 Minecraft 服务端可部署在同一台 Linux 主机上。它的职责是把网盘驱动暴露给本机备份程序，避免备份脚本直接持有网盘令牌。

推荐将 OpenList 作为专用、非 root 用户运行；数据目录与凭据文件仅允许该用户或 root 读取。WebDAV 监听回环地址，并创建一个只允许写入备份目标目录的专用用户，不要把完整网盘根目录交给上传任务。

```ini
# /etc/systemd/system/openlist-backup.service
[Unit]
Description=OpenList for Minecraft backup uploads
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=<OPENLIST_USER>
Group=<OPENLIST_GROUP>
WorkingDirectory=<OPENLIST_DATA_DIR>
ExecStart=<OPENLIST_BINARY> server --data <OPENLIST_DATA_DIR>
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ReadWritePaths=<OPENLIST_DATA_DIR> <OPENLIST_TMPFS_DIR>

[Install]
WantedBy=multi-user.target
```

在 OpenList 对应版本的配置中将服务监听限制为 `127.0.0.1:<OPENLIST_PORT>`，不要监听全部网卡。将 `<OPENLIST_TMPFS_DIR>` 挂载为 tmpfs，或使用现有的 `/dev/shm` 子目录。临时目录只减少磁盘写入，不会减少 RAM 占用；内存不足时应降低并发、增大 swap 规划或改用磁盘临时目录。

## systemd：创建和上传解耦

归档与上传应当是两项独立任务。创建任务按时生成、检查并轮换本地归档；上传任务则扫描本地待传归档、校验远端结果并重试。网络或网盘短暂异常不会阻止下一次本地备份。

```ini
# /etc/systemd/system/minecraft-backup-create.service
[Unit]
Description=Create a consistent Minecraft archive
After=network-online.target

[Service]
Type=oneshot
User=root
ExecStart=/usr/local/lib/minecraft-backup/create.py
```

```ini
# /etc/systemd/system/minecraft-backup-create.timer
[Unit]
Description=Hourly Minecraft archive timer

[Timer]
OnCalendar=*-*-* *:30:00
Persistent=true
AccuracySec=1s
Unit=minecraft-backup-create.service

[Install]
WantedBy=timers.target
```

上传任务可由另一只 timer 每 10 分钟触发。脚本通过 root-only 的 rclone 配置访问本机 OpenList WebDAV，远端路径应限制在 `<REMOTE_BACKUP_PATH>`，并在成功确认后轮换超过保留数量的旧归档。

启用或修改单元文件后执行：

```bash
systemctl daemon-reload
systemctl enable --now minecraft-backup-create.timer
systemctl enable --now minecraft-backup-upload.timer
systemctl list-timers --all | grep minecraft-backup
```

`Persistent=true` 会在服务器停机错过触发时间后补跑一次；它适合备份，但应确认磁盘空间与服务器负载足以处理补跑任务。

## 日常检查与恢复演练

每天或每周查看计时器、最近运行日志和本地归档数量：

```bash
systemctl list-timers --all | grep minecraft-backup
journalctl -u minecraft-backup-create.service -n 50 --no-pager
journalctl -u minecraft-backup-upload.service -n 50 --no-pager
find <ARCHIVE_DIR> -maxdepth 1 -name '*.tar.zst' -printf '%f\n' | sort
```

备份真正可靠的标准不是“看到上传成功”，而是能恢复。定期从异地下载一个较早归档，验证 SHA-256 和 zstd，再解压到隔离目录检查 `level.dat`、维度目录和模组配置是否齐全。不要直接覆盖正在运行的世界目录进行演练。

常见处理方向：

- RCON 失败：不要创建归档；检查服务端、RCON 回环监听和凭据文件权限。
- `save-on` 未恢复：立即通过控制台恢复自动保存，随后修复脚本的异常清理逻辑。
- 本地空间不足：先暂停新归档，确认保留轮换是否失效；不要直接删除唯一的异地未确认副本。
- 上传失败：保留已校验的本地归档，让上传扫描任务重试；检查 OpenList 服务、WebDAV 受限路径和网盘配额。
- 网盘显示内容滞后：检查 OpenList 的目录缓存设置，上传后以文件名和大小重新查询确认。

## 公开与私有信息的边界

公开文档可以完整说明 RCON 保存指令、systemd 结构、压缩与校验方法。真实绝对路径本身通常不是密钥，但通用页面使用变量更容易复用，也不会泄露服务器目录布局。

实际源目录、归档目录、systemd 服务名、OpenList 数据目录和保留策略可以保存在仓库外的私有运维附录。即使是私有附录，也不要复制 token、密码、rclone 配置全文或 OpenList 驱动配置全文；只记录这些凭据文件的受限存放位置。
