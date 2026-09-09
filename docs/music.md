# 一起听歌与全服点歌

本服使用 Concerto 2.1.0。它支持网易云音乐、QQ 音乐、酷狗、直链和本地音频；本服已经开启全服 KTV 队列。想听歌的玩家都需要在客户端安装同版本 Concerto。

## 打开客户端界面

进入服务器后，默认按键如下。可在“选项 -> 控制”里搜索 `Concerto` 重新绑定。

| 按键 | 功能 |
| --- | --- |
| `I` | 打开 Concerto 主界面 |
| `U` | 打开歌单 |
| `N` | 下一首 |
| `P` | 暂停或继续 |

在主界面选择网易云或 QQ 音乐后，可以搜索歌曲、歌单、专辑和歌手。没有加入音乐房间或全服队列时，点击“播放”只会在自己客户端播放。

## 全服 KTV 点歌

这是让全服同步听同一首歌的推荐方式。每个想听歌的人先执行：

```mcfunction
/musicroom agent join
```

然后由点歌的人按 `I` 打开音乐界面，搜索并选中歌曲。加入队列后，歌曲页底部的“播放”会变为“服务器点歌”；点击它即可加入全服播放队列。

也可以把当前正在播放的歌曲送入队列：

```mcfunction
/musicroom agent add
```

本服限制同一玩家两次点歌至少间隔 60 秒。队列内玩家可以发起跳过投票，其他队列成员再表态：

```mcfunction
/musicroom agent vote
/musicroom agent vote true
/musicroom agent vote false
```

离开全服队列：

```mcfunction
/musicroom agent quit
```

> 全服队列为空时不会自动播放背景电台。需要播放时，重新加入队列并点歌即可。

## 小范围音乐房间

只想和几个人同步听歌时，由 OP 创建房间：

```mcfunction
/musicroom create 我的房间
```

房间 UUID 会复制到创建者剪贴板。朋友执行下面命令加入：

```mcfunction
/musicroom join <房间UUID>
```

房主可查看成员、授权他人控制播放，或解散房间：

```mcfunction
/musicroom members
/musicroom op <玩家名>
/musicroom remove
```

本服目前只有 OP 可以创建音乐房间；普通玩家可以加入已有房间和全服 KTV 队列。

## 直接分享一首歌

把当前歌曲分享给指定玩家：

```mcfunction
/sharemusic to <玩家名>
```

广播给所有在线玩家：

```mcfunction
/sharemusic to @a
```

全服广播会进入 OP 审核队列。服主先查看，再批准对应 UUID：

```mcfunction
/concerto-server audit list 1
/concerto-server audit <UUID>
```

`/sharemusic` 是一次性分享，不会像全服 KTV 那样维护可继续点歌的队列。

## 出问题时

- 找不到 Concerto 界面：确认客户端安装的是 `Concerto-mc1.21.1-fabric-2.1.0.jar`，并在控制设置里搜索 Concerto。
- 只能自己听到：确认其他玩家也装了 Concerto，并且都执行过 `/musicroom agent join`。
- QQ 音乐或网易云无法解析：平台登录状态或版权限制可能失效；重新登录对应平台后重试。不要将 Cookie 文件发给其他玩家。
- 不想再听全服音乐：执行 `/musicroom agent quit`。

继续阅读 [多人服务器玩法](/multiplayer)。
