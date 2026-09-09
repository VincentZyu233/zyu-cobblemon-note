# 服务器性能监控

服务器已安装 `spark 1.10.109-fabric`，对应 Minecraft 1.21.1 / Fabric。它只需装在服务端，玩家客户端不用安装。每次观察问题先记录时间、在线人数、所在维度和正在发生的事，再采样，结果会比无目标地调 JVM 参数可靠得多。

::: warning 先重启一次服务端
模组 jar 放入 `mods` 后，必须通过 MCSM 重启服务端才会加载。重启后在控制台执行 `spark tps`；能返回数据就代表安装生效。
:::

## 最常用：TPS 与 MSPT

在游戏聊天框以 OP 身份输入，或直接在服务端控制台输入：

```mcfunction
spark tps
```

它会显示多个时间窗口的 TPS、tick duration（也就是 MSPT 的最小值、中位数、95% 分位和最大值）、CPU 与内存信息。

| 读数 | 含义 | 建议 |
| --- | --- | --- |
| TPS 接近 20，MSPT 稳定低于 50 ms | 每 tick 能在 1/20 秒内完成 | 正常，不用调参 |
| MSPT 偶发超过 50 ms | 偶尔有卡顿尖峰 | 用慢 tick 监控或 60 秒采样定位 |
| MSPT 持续超过 50 ms，TPS 低于 20 | 服务端追不上游戏 tick | 在复现问题时做 profiler，不要先盲删模组 |

`50 ms` 是关键线，因为 Minecraft 以 20 TPS 为目标，每 tick 只有约 50 ms 的预算。95% 分位比“最大值”更能代表日常体感；最大值偶尔很高则可能只是区块生成、自动保存或 GC 尖峰。

## 一键健康报告

```mcfunction
spark health
```

这会上传一份包含 TPS、tick 时长、CPU、内存、磁盘和环境信息的报告，并返回链接。适合刚感觉到卡、但尚未确定是否需要深入采样时使用。

报告链接可被持有链接的人查看，发到公开群前先确认其中的服务器环境信息是否适合公开。

## 卡顿时抓 60 秒 CPU 样本

问题正在发生时执行：

```mcfunction
spark profiler start --timeout 60
```

它会在 60 秒后自动停止并返回结果链接。最好在“有人进新地形”“大量宝可梦刷新”“打开某个机器”这类能复现卡顿的时段开始。报告页面会显示服务端时间主要花在哪些调用上。

若已手动启动，可用：

```mcfunction
spark profiler stop
```

只想记录真正慢的 tick，可用：

```mcfunction
spark profiler start --only-ticks-over 50 --timeout 120
```

这会只保留超过 50 ms 的 tick 样本，特别适合排查偶发卡顿。常规分析先不要加 `--thread *`；只有确认网络、异步任务或其他线程占用异常时才扩大到所有线程，否则报告噪声会更多。

## 监控单次慢 tick

```mcfunction
spark tickmonitor --threshold-tick 50
```

开启后，超过 50 ms 的 tick 会被报告。完成排查后再次运行下面的命令关闭它：

```mcfunction
spark tickmonitor
```

这适合临时观察，并不建议整天开启。它告诉你“何时出现慢 tick”，profiler 才告诉你“时间具体花在哪里”。

## 内存与 GC

```mcfunction
spark gc
spark heapsummary
```

`spark gc` 查看垃圾回收历史；`spark heapsummary` 生成堆摘要并返回查看链接。只有内存持续攀升、长时间停顿或 GC 明显异常时再用它们。`heapdump` 会产生完整堆转储，文件大且更具侵入性，未明确需要时不要执行。

## 权限与排查顺序

你是 OP 时先直接尝试上述命令。若游戏内提示无权限，可从服务端控制台执行，或检查权限模组是否限制了 `spark`、`spark.tps`、`spark.profiler` 等权限节点。

推荐顺序：

1. `spark tps` 确认是持续慢还是偶发尖峰。
2. `spark health` 保存当时的整体环境。
3. 复现时运行 `spark profiler start --timeout 60`。
4. 依据报告中占比最高的调用、实体或模组再决定改配置、减少实体、预生成区块，还是处理某个模组。

## 官方资料

- [spark 官方命令文档](https://spark.lucko.me/docs/Command-Usage)
- [spark：定位卡顿尖峰指南](https://spark.lucko.me/docs/guides/Finding-lag-spikes)
- [spark Modrinth 项目页](https://modrinth.com/mod/spark)
