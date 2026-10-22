# 批50：CI Windows 诊断闭环（阶段 C「治理」，台账 HL-2026-10-15/16/18-01 根因修复）

> 批次：批50 · 定位：治理 · 触发：main 分支 CI 持续红（acfe2eb/56c67bd/b61b264/ab56f73 四次 push 的 Windows build 全失败）
> 台账关联：HL-2026-10-18-01（Windows WatchDir/FileTailer/CoverageMargin）、HL-2026-10-16-01、HL-2026-10-15-01（同根因）

## 1. 证据（2026-10-22 巡检快照）

| 证据 | 值 |
| --- | --- |
| main CI（最近 4 push） | Windows build **failure**，ubuntu/macos success |
| 失败测试 | `FileTailerBatch38Test` 1 例 + `WatchDirBatch38Test` 5 例 |
| 失败模式 | 4 例 `await` 15s 超时 + 1 例 0.005s 即时断言失败（`溢出处理对齐并上报`） |
| 本地 Linux 复现 | WatchDir 30 例 + FileTailer 25 例 **全绿 2.3s**（实现逻辑正确，纯平台差异） |
| 附带快照 | open issue=0、open PR=2（均 dependabot 自动升级）、repo1 已发布 **1.19.0**（Portal publish 已由用户手动完成，最大阻塞解除） |

## 2. 根因分析（Windows 平台特性）

1. **WatchDir 周期快照被饿死**（4 例 15s 超时）：`loop()` 中 `snapshotSync()` 仅在 `watchService.poll(500ms) == null`（无事件超时）时触发。Windows 上文件事件密集（测试连续写入）时 poll 持续非 null → 快照兜底永不执行 → 子目录/动态注册事件（Windows WatchService 原生不报告子目录）永久等待 → 15s 超时。Linux 事件稀疏侥幸触发快照所以全绿。
2. **WatchDir backfill 单条目异常中断整体**（1 例 0.005s 断言失败）：`backfill()` 对 `DirectoryStream` 整体 try-catch；Windows 上枚举含刚写入文件的目录时单个子项句柄短暂占用抛 IOException → 整个枚举失败 → 全部补发丢失 → `CREATE sub/a.txt` 缺失断言失败（瞬时发生）。
3. **FileTailer TOCTOU 竞态**（1 例 15s 超时）：`tailLoop()` 中 `Files.exists(file)` 检查后 `Files.readAttributes(file)` 之间文件被 move/替换（Windows 轮转测试模拟 rename+新建），抛 `NoSuchFileException` 逃逸至外层 → 轮转检测路径中断 → 新文件行永不读取。

## 3. 修复方案（实现侧加固，测试断言零改动）

| # | 文件 | 修复 |
| --- | --- | --- |
| 1 | `WatchDir.loop()` | 周期快照改为**无条件每 1s 执行**（从 `poll==null` 分支移出），事件密集时快照兜底持续生效 |
| 2 | `WatchDir.backfill()` | `DirectoryStream` 迭代内**逐项 try-catch**：单条目失败 continue，不中断整体枚举 |
| 3 | `FileTailer.tailLoop()` | `exists` + `readAttributes` 合并为原子读（直接 `readAttributes` 包 try-catch）：`NoSuchFileException` → 按 vanished 处理（下一轮重现即触发轮转） |

## 4. 验收标准

1. 本地 Linux `mvn -pl sure-core verify` 全绿（覆盖铁律 ≥98% 不受影响——本批仅 io 包实现加固）
2. 推送后 GitHub Actions **windows-latest build 转绿**（FileTailer 25 例 + WatchDir 30 例全过）——本批核心验收（跨日对账：CI 绿才标台账闭环）
3. checkstyle/spotbugs 全绿
4. CHANGELOG 批50 条目 + 台账更新（HL-2026-10-18-01 等 3 🟡 → 🟢 待 CI 验证）

## 5. 范围边界

- 不改测试断言/超时（弱化测试不可接受；15s 超时保持不变）
- 不改发布制品；不创建定时任务
- dependabot PR#19/#20 待 CI 修复生效后由 dependabot 自动 rebase 重跑；若 Windows 仍红需复核
- repo1 1.19.0 已发布（用户手动 Portal publish）：台账 v1.19.0 发布项标记已闭环，另需验证 `sure-core-1.19.0.pom` 200（已确认）并核对 CHANGELOG/tag/Release 一致性
