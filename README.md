# 拾光 · 纪念日 / 倒计时

> 把重要的日子留在这里。过去的日子一天天累积，未来的日子一天天临近。

一个纯净简洁、带艺术气质的 Android 纪念日/倒计时应用：
自定义任意日期 —— **过去**即纪念日（“已 N 天”），**未来**即倒计时（“还 N 天”），
支持**每年重复**（生日、周年），并配有**桌面小组件**。

## ✨ 功能

- **多种展示样式**（详情页底部一键切换，选择会记忆）
  - **经典**：一枚安静的衬线大数字
  - **多米诺**：小木棍代表被切分的日子，随时间一根根倒下；进入页面会重放“倒下”的过程
  - **长条**：一根长条由长慢慢变短（倒计时）/ 由短慢慢填满（纪念日）
  - **圆环**：一圈一年，圆环慢慢走完
- **桌面小组件**
  - 2×2 单日样式、4×2 带进度条样式
  - 添加时弹出配置页选择日子，底色跟随事件色板
  - 每天零点由闹钟精确刷新“还 N 天”，平时由系统周期兜底
- **细节**
  - 每年重复的日期自动推算下一个周年（含 2 月 29 日 → 2 月 28 日的收敛）
  - 进度起点可自定义（默认从添加当天开始）
  - 8 色低饱和粉笔色板，浅色/深色主题自适应，Android 13+ 支持主题图标（Monochrome）

## 🎨 设计

- 图标：三根依次倾倒的“时间木棍”与一轮小太阳，置于暖纸色渐变之上 ——
  呼应应用内的多米诺样式；矢量绘制，任意分辨率无损。
- 视觉基调：暖纸底色 + 墨色文字 + 陶土主色；大数字用衬线体（Light），
  安静、有纸感。

## 🛠 技术栈

| 项 | 选择 |
| --- | --- |
| 语言 / UI | Kotlin 2.0 + Jetpack Compose（Material 3） |
| 存储 | Room（KSP） |
| 架构 | MVVM（ViewModel + StateFlow） |
| 小组件 | RemoteViews（AppWidgetProvider + 配置 Activity + AlarmManager 午夜刷新） |
| 构建 | Gradle 8.11.1 / AGP 8.7.3 / compileSdk 35 / minSdk 26 |

核心日期逻辑集中在 [DateMath.kt](app/src/main/java/com/shiguang/app/logic/DateMath.kt)，
纯 JVM 可测试，配套单元测试见
[DateMathTest.kt](app/src/test/java/com/shiguang/app/logic/DateMathTest.kt)。

## 📱 使用

1. 用 Android Studio（Ladybug 或更新，含 JDK 17）打开本目录，等待 Sync 完成；
2. `Run ▶` 安装到设备/模拟器；
3. 添加小组件：长按桌面 → 小组件 → 拾光 → 拖入 → 选择要展示的日子。

命令行构建：

```bash
./gradlew assembleDebug        # 出包 app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # 跑日期逻辑单元测试
```

## 📂 结构速览

```
app/src/main/java/com/shiguang/app/
├── MainActivity.kt            # 入口与页面导航（主页/详情/编辑）
├── data/                      # Room：Entity / DAO / Database
├── logic/                     # DateMath 日期核心逻辑 + EventUi 展示模型
├── ui/
│   ├── HomeScreen.kt          # 首页列表
│   ├── EventCard.kt           # 列表卡片
│   ├── DetailScreen.kt        # 详情页（样式切换）
│   ├── EditScreen.kt          # 新建/编辑
│   ├── styles/                # 经典 / 多米诺 / 长条 / 圆环 四种样式
│   └── theme/                 # 配色、字体、主题
└── widget/                    # 小组件 Provider、配置页、午夜刷新、渲染器
```

**每一个文件的详细功能说明见 [docs/FILEMAP.md](docs/FILEMAP.md)。**

## 🤖 仓库架构：pipeline-ops 多 Agent 流水线

本仓库按 **pipeline-ops** 协议组织（无人值守流水线 + 多 AI Agent 交接）：

- **权威状态**：`pipeline_state.json` 是唯一事实源（仅编排器可写）；人读快照在 `.agents/STATE.md`
- **协作区**：`.agents/`（协议 `AGENTS.md`、决策流水、claims 文件锁、交接与事故归档）
- **write-once 产物**：`runs/<时间戳>_<环节>_<序号>/`，每次执行一个 run，验收票（gate_report.json）机器可读，失败保留原位
- **交接锚点**：commit 即交接——接手 Agent 用 `git diff` 精确知道上一位改了什么

流水线共三环，每环验收门槛可计算 PASS/FAIL：

| 环节 | 内容 | 验收门槛（gate） |
|---|---|---|
| A `logic-test` | 日期核心逻辑单元测试 | 12 个用例，failures=0 |
| B `package` | assembleDebug 打包 | 退出码 0 且 APK 产出 |
| C `acceptance` | aapt 清单与组件验收 | 含 app-widget、启动 Activity、2 个 widget receiver |

跑全链（A→B→C，含纳米预演语义：本链全预算即最小预算）：

```bash
./gradlew testDebugUnitTest assembleDebug
$ANDROID_HOME/build-tools/35.0.0/aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep app-widget
```

Agent 开工/收工纪律见 `.agents/AGENTS.md`。
