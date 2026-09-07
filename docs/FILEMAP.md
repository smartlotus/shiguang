# FILEMAP — 逐文件功能说明

> 本文档逐一说明仓库中每个文件的功能。架构协议见 `.agents/AGENTS.md`，
> 权威状态见 `pipeline_state.json`（人读版在 `.agents/STATE.md`）。

## 1. 仓库根目录

| 文件 | 功能 |
|---|---|
| `AGENTS.md` | 项目入口（AI 会话启动时自动加载）：指向协议三件套与最小铁律 |
| `README.md` | 面向人的项目说明：功能、技术栈、使用方法、构建命令 |
| `FILEMAP.md` → `docs/FILEMAP.md` | 本文档：逐文件功能说明 |
| `.gitignore` | git 排除规则：构建产物、IDE 配置、本地 SDK 路径等不入库 |
| `pipeline_state.json` | **权威状态（唯一事实源）**：流水线各环节 status/run_id/验收票路径；仅编排器可写 |
| `local.properties` | 本机 Android SDK 路径（**不入库**，每台机器各自生成） |
| `keystore.properties`、`keystores/shiguang-release.jks` | release 签名凭据与自签密钥库（**不入库**；缺失时 release 回退为未签名包） |
| `gradle.properties` | Gradle 全局参数：JVM 内存、AndroidX 开关、并行构建 |
| `settings.gradle.kts` | Gradle 设置：插件仓库（google/mavenCentral）、模块包含、项目名 ShiGuang |
| `build.gradle.kts` | 根构建脚本：声明 AGP 8.7.3 / Kotlin 2.0.21 / Compose 插件 / KSP 版本 |
| `gradlew`、`gradlew.bat` | Gradle wrapper 启动脚本（Linux/macOS 与 Windows） |
| `gradle/wrapper/gradle-wrapper.properties` | wrapper 配置：锁定 Gradle 8.11.1 发行版下载地址 |
| `gradle/wrapper/gradle-wrapper.jar` | wrapper 引导 jar（下载并运行指定版本 Gradle） |

## 2. `app/` — Android 应用模块

| 文件 | 功能 |
|---|---|
| `app/build.gradle.kts` | 应用模块构建脚本：applicationId/minSdk 26/targetSdk 35、Compose 开关、全部依赖（Compose BOM、Material3、Room、KSP、JUnit） |
| `app/proguard-rules.pro` | release 混淆规则（当前未开混淆，保留 Room 实体 keep 规则备用） |
| `app/src/main/AndroidManifest.xml` | 应用清单：MainActivity（启动器）、小组件配置 Activity、两个 widget receiver、午夜刷新 receiver |

## 3. `app/src/main/java/com/shiguang/app/` — Kotlin 源码

### 入口与导航

| 文件 | 功能 |
|---|---|
| `MainActivity.kt` | 唯一 Activity 入口：enableEdgeToEdge、预约午夜刷新、托管 `ShiGuangApp()`；`Screen` 密封类 + **AnimatedContent 导航转场**（按页面层级左右滑动+淡入淡出）、**小组件深链**（singleTop + onNewIntent 接收 `EXTRA_OPEN_EVENT_ID` 直达详情）、返回键处理 |

### 数据层 `data/`

| 文件 | 功能 |
|---|---|
| `data/EventEntity.kt` | Room 实体（表 `events`）：标题、目标日 epochDay、进度起点、每年重复、色板下标、样式下标、备注、创建时间 |
| `data/EventDao.kt` | Room DAO：Flow 观察全表、按 id 查询、增/改/删；供 ViewModel 与小组件读取 |
| `data/AppDatabase.kt` | Room 数据库单例（版本 1），库名 `shiguang.db` |

### 日期核心逻辑 `logic/`（纯 JVM，可单测）

| 文件 | 功能 |
|---|---|
| `logic/DateMath.kt` | **全 App 的日期算法核心**：下一个周年推算（含 2/29→2/28 收敛）、头条天数（还 N / 已 N）、进度比例（倒计时按起始日→目标日；纪念日按首个 365 天；每年重复按周年循环）、多米诺木棍数映射（8..40） |
| `logic/EventModel.kt` | 展示层模型 `EventUi`：由实体一次性预计算天数、前缀（还/已/今天）、显示比例等派生数据，UI 层零重复计算 |

### UI 层 `ui/`

| 文件 | 功能 |
|---|---|
| `ui/EventsViewModel.kt` | MVVM ViewModel：Room Flow → `EventUi` 的 StateFlow（**null=加载中**，避免空状态闪现）；save/setStyle/delete 后触发小组件全量刷新 |
| `ui/Formats.kt` | 日期格式化工具：中文全日期（含星期）、紧凑日期（2026.10.1）、LocalDate↔DatePicker UTC 毫秒互转 |
| `ui/HomeScreen.kt` | 首页：标题区（拾光 + 今日日期）、**加载中小圆点**、事件列表（LazyColumn + **animateItem 删除/重排动画**）、空状态（**多米诺品牌插画 DominoMark**）、ExtendedFAB「记一个日子」 |
| `ui/EventCard.kt` | 列表卡片：色点 + 标题 + 混排大数字（还/已 N 天）+ 日期/备注 + 迷你进度条 |
| `ui/DetailScreen.kt` | 详情页：整页按事件色淡染（animateColorAsState），顶部返回/编辑，中部 `AnimatedContent` 渲染当前样式（**淡入+缩放转场**），底部 SegmentedButton 切换四种样式（写入数据库记忆） |
| `ui/EditScreen.kt` | 新建/编辑页：标题、Material3 DatePicker 选目标日与进度起点、每年重复开关、8 色色板选择、备注、删除（带确认对话框） |

### 主题 `ui/theme/`

| 文件 | 功能 |
|---|---|
| `ui/theme/Color.kt` | 色彩定义：纸/墨/陶土基础色 + 8 色低饱和事件色板（雾蓝、豆绿、陶土、杏黄、藕紫、青碧、绯粉、石灰）及中文名 |
| `ui/theme/Type.kt` | 字体排印：大数字用衬线体 Light（纸感），正文默认无衬线，全 Material3 槽位 |
| `ui/theme/Theme.kt` | 明暗两套 Material3 配色方案 + `ShiGuangTheme` 组合函数 |

### 四种展示样式 `ui/styles/`

| 文件 | 功能 |
|---|---|
| `ui/styles/Common.kt` | 共享件：`accent` 色扩展属性、`dateLine()` 文案、`Headline()`（前缀 + 衬线大数字 + 日期）供多种样式复用 |
| `ui/styles/ClassicStyle.kt` | 经典样式：一枚 88sp 衬线大数字，**入场缩放浮现** |
| `ui/styles/DominoStyle.kt` | **多米诺样式**：Canvas 木棍队列，时间流逝逐根倒下（76° 旋转），进场 Animatable 重放倒下过程，**下一根待倒木棍轻微摇摆蓄力**（无限过渡）；地面线 + “已倒下 X/Y 根”文案 |
| `ui/styles/BarStyle.kt` | **长条样式**：倒计时从满长收缩到当前剩余比例（由长慢慢变短），纪念日反向填满；**进度条流光扫过**（线性渐变无限平移）；两端标注起止日期 |
| `ui/styles/RingStyle.kt` | 圆环样式：Canvas 双弧（底环 + 进度弧，圆头端帽），环心大数字，进场扫描动画 |

### 桌面小组件 `widget/`

| 文件 | 功能 |
|---|---|
| `widget/WidgetUpdater.kt` | 小组件统一渲染器：读取「appWidgetId → 事件」绑定，生成 RemoteViews（标题/天数/日期/底色/进度条/**百分比**）；**深链 PendingIntent（点击直达该日子详情）**、“就是今天”特殊显示、未绑定占位视图；设置午夜 AlarmManager |
| `widget/CountdownWidgets.kt` | 两个 AppWidgetProvider（2×2 与 4×2）：onUpdate 时预约午夜 + 全量刷新；onDeleted 清理绑定；抽象基类共享逻辑 |
| `widget/MidnightReceiver.kt` | 零点闹钟广播接收器：刷新全部小组件并预约下一个午夜，保证“还 N 天”当天翻准 |
| `widget/WidgetConfigureActivity.kt` | 添加小组件时的配置页：Compose 列出全部事件供选择，写绑定 → 立即渲染 → 返回 RESULT_OK；空列表时引导打开应用 |

## 4. 测试

| 文件 | 功能 |
|---|---|
| `app/src/test/java/com/shiguang/app/logic/DateMathTest.kt` | DateMath 单元测试 12 例：倒计时/纪念日/每年重复头条数、2/29 收敛、今天边界、进度钳制、周年循环比例、木棍映射 |

## 5. 资源 `app/src/main/res/`

| 文件 | 功能 |
|---|---|
| `layout/widget_small.xml` | 2×2 小组件布局：标题 + 前缀/大数字/天 + 日期（id：w_root/w_title/w_prefix/w_days/w_date） |
| `layout/widget_medium.xml` | 4×2 布局：标题与日期同行、大数字、**进度条 + w_percent 百分比文字**（水平排列） |
| `drawable/widget_bg_0..7.xml`（8 个） | 小组件圆角背景，对应 8 色色板 |
| `drawable/widget_progress.xml` | 组件进度条 layer-list：半透明轨道 + 深墨 clip 进度 |
| `drawable/ic_launcher_foreground.xml` | 自适应图标前景：三根依次倾倒的时间木棍 + 小太阳（矢量，呼应多米诺样式） |
| `drawable/ic_launcher_background.xml` | 自适应图标背景：暖纸色→蜜桃色对角渐变（aapt:attr 渐变） |
| `drawable/ic_launcher_monochrome.xml` | Android 13+ 主题图标（单色版前景） |
| `mipmap-anydpi-v26/ic_launcher.xml`、`ic_launcher_round.xml` | 自适应图标装配：background + foreground + monochrome |
| `values/strings.xml` | 字符串：应用名「拾光」、两个小组件标签与描述 |
| `values/colors.xml` / `values-night/colors.xml` | 窗口背景色（浅纸 / 深夜） |
| `values/themes.xml` / `values-night/themes.xml` | XML 主题：NoActionBar、windowBackground、状态栏明暗 |
| `xml/widget_small_info.xml` | 2×2 小组件声明：单元格尺寸、30 分钟系统刷新、initial/preview 布局、configure 指向配置页 |
| `xml/widget_medium_info.xml` | 4×2 小组件声明（同上，四列宽） |

## 6. pipeline-ops 多 Agent 协作区

| 文件 | 功能 |
|---|---|
| `.agents/AGENTS.md` | 多 Agent 协作协议：三条铁律、文件职责表、工作节奏、**自主 Agent 动作白名单** |
| `.agents/STATE.md` | 给人看的一屏状态快照（收工时更新；与 pipeline_state.json 冲突时后者为准） |
| `.agents/DECISIONS.md` | 决策流水（append-only：日期\|决策\|理由\|证据） |
| `.agents/claims/` | 文件/领域锁：改共享代码前先写认领，防并行覆盖 |
| `.agents/handoffs/` | 交接文档归档（模板见 templates） |
| `.agents/incidents/` | 事故复盘 + 故障交接包 |
| `.agents/templates/` | 全部模板：AGENTS/STATE/DECISIONS/交接/事故/run_manifest/gate_report/pipeline_state |
| `runs/<run_id>/` | **write-once 产物目录**：每次流水线执行一个 run，内含 manifest.json（输入指纹）+ 各环节 gate_report.json（验收票），失败保留原位标记 |
