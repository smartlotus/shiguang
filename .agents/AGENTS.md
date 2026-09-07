# AGENTS.md — 本项目多 Agent 协作协议

> 任何 AI Agent 开工前必读。读完本文件 + STATE.md + pipeline_state.json 即可开工。

## 三条铁律

1. **开工先读，收工必写**：开工 = `git pull` + 读 STATE.md / pipeline_state.json；收工 = 更新 STATE.md + `git commit` + `git push`。交接靠机制（diff），不靠记忆。
2. **动文件先认领**：改共享代码前在 `.agents/claims/` 写认领文件；发现已被认领就停，不抢。
3. **产物只写 run 目录，状态只有编排器能写**：任何人不准旁路写 `pipeline_state.json`，不准覆盖历史产物。

## 文件职责

| 路径 | 职责 | 谁写 |
|---|---|---|
| `pipeline_state.json` | 权威状态（唯一事实源） | 仅编排器 |
| `.agents/STATE.md` | 给人看的一屏状态快照 | 任何 Agent 收工时 |
| `.agents/DECISIONS.md` | 决策流水（append-only） | 做决策的 Agent |
| `.agents/handoffs/` | 交接文档（一次一签） | 交接发起方 |
| `.agents/claims/` | 文件/领域锁 | 认领方 |
| `.agents/incidents/` | 事故复盘 + 故障交接包 | 处理方 |
| `runs/<run_id>/` | 产物（write-once） | 该 run 的执行者 |

## 工作节奏

开工：pull → 读状态 → 查 claims → 认领 → 干活
收工：更新 STATE.md（一屏内）→ 记决策 → 释放 claim → commit + push

## 自主 Agent 动作白名单（长期自主运行必填）

- 允许执行的脚本路径：`./gradlew`（或 `gradlew.bat`，参数限 `assembleDebug`/`testDebugUnitTest`/`lint`）、
  `%ANDROID_HOME%/build-tools/*/aapt.exe`（只读 dump）、`python .agents/scripts/*`、
  `git`（add/commit/push 到本仓库远端）、`gh repo/pr`（只读 + 创建发布物）
- 允许写入的目录：`app/src/**`、`docs/**`、`runs/**`、`.agents/{claims,handoffs,incidents}`、
  `.agents/STATE.md`、`.agents/DECISIONS.md`、`README.md`、`pipeline_state.json`（仅编排器角色，读-改-写原子替换）
- 禁止动作：杀他人进程 / 删非本 run 的 runs 文件 / 旁路直写 pipeline_state.json（非编排器角色）/ 
  `git push --force` / 修改 `.github/` 密钥与权限 / 管理员命令
- 预算上限：同一 gate 失败重试 ≤ 3 次；磁盘剩余 < 2GB 停；单次构建超 10 分钟视为失败并留故障交接包
- 遇白名单外动作：停下并生成故障交接包，不绕路
