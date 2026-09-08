# 决策流水（append-only，不改写历史行）

格式：`日期时间 | 决策 | 理由 | 证据/链接`

---
2026-09-08 | 技术栈选 Kotlin 2.0 + Jetpack Compose + Room + MVVM | 纯 UI 型应用，Compose 达成"纯净简洁"最快；Room 提供稳定本地持久化 | runs/20260908_*_pipeline_001/
2026-09-08 | 桌面小组件用 RemoteViews 而非 Glance | RemoteViews 无额外依赖、行为可预期；动画复杂度留给应用内样式，组件只做静态天数+进度 | docs/FILEMAP.md §widget
2026-09-08 | widget receiver 声明 exported="false" | 当前官方文档写法；系统 Launcher 绑定不受导出限制影响 | app/src/main/AndroidManifest.xml
2026-09-08 | 天数翻转用 AlarmManager 午夜闹钟 + updatePeriodMillis 双保险 | 30 分钟系统周期可能延迟翻日；午夜闹钟保证当天 0 点翻准 | widget/WidgetUpdater.kt scheduleMidnight
2026-09-08 | 仓库结构对齐 pipeline-ops | 用户明确要求"自动 Agent 架构"；三环流水线 A 单测→B 打包→C 清单验收，gate 全部可机器判定 | pipeline_state.json
2026-09-08 | release 签名采用本地自签密钥（keystores/shiguang-release.jks，10000 天） | 用户要求全自动产出完整版 APK；自签零成本且可安装；凭据文件 keystore.properties 与密钥不入库（.gitignore），换机需重新生成或线下同步 | runs/20260908_0013_release_001/gate_C_acceptance.json
2026-09-08 | release 不开启 minify | 未做混淆回归真机验证，保安装稳定性优先；后续可开启并补 keep 规则 | runs/20260908_0013_release_001/gate_B_package.json
2026-09-08 | v1.1 动效方案：导航按页面层级左右滑动、列表 animateItem、经典入场缩放、多米诺下一根摇摆、长条流光、样式切换淡入缩放 | 用户要求针对动效/UI/小组件优化；全部使用标准 Compose API，无第三方动画库，预算可控 | runs/20260908_0139_optimize_001/manifest.json
2026-09-08 | 小组件深链用 singleTop + EXTRA_OPEN_EVENT_ID + Intent data 区分 PendingIntent | 点击组件直达详情是小组件体验最大提升；data uri 保证不同事件 PendingIntent 互不覆盖 | widget/WidgetUpdater.kt openAppPendingIntent
2026-09-08 | VM events 用 null 表示加载中 | 修复启动时空状态闪现（Room 首次发射前误显"还没有记录"） | ui/EventsViewModel.kt
2026-09-08 | 输入法适配用 Compose imePadding 而非调 manifest windowSoftInputMode | enableEdgeToEdge 后 API 30+ 窗口不再随 IME 收缩，adjustResize 失效；imePadding 是 edge-to-edge 下的标准解法，且保留全屏视觉 | runs/20260908_2306_imefix_001/
2026-09-08 | 修复版定 versionCode 3 / versionName 1.1.1 | 用户报告 bug 的语义化补丁号；同签名密钥可覆盖安装 | runs/20260908_2306_imefix_001/gate_B_package.json
2026-09-08 | git 远端从 https://github.com/... 切换为 ssh://git@ssh.github.com:443/... | github.com:443 持续拒连（api.github.com 正常，疑似本地网络对主站 SNI 干扰）；ssh.github.com:443 认证即通。若 GitHub HTTPS 恢复可切回，SSH 通道可长期使用 | git remote -v
