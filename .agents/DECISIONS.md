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
