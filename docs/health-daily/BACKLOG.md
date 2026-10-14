<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the LICENSE file.
-->
# suretool GitHub 健康治理 · 问题台账（单一事实源）

> 由「suretool GitHub 健康治理日报」每日定时任务维护。状态：🟢 已闭环 / 🟡 处理中 / 🔴 积压。
> 规则：每日跨日对账；"已解决"必须验证通过才可标闭环；红色项日报顶部置顶直至状态变化。

| ID | 来源 | 标题 | 提出日期 | 状态 | 解决日期 | 验证结果 | 关联 PR/Release |
|---|---|---|---|---|---|---|---|
| HL-2026-10-11-01 | CI | Windows 构建连续失败：`SettingUtil.resolve()` 仅查 `HOME`，Windows runner 无 `HOME` → 回落到文件值。修复链：①SettingUtil HOME→USERPROFILE 回退 + `envLookup` 钩子（136124b）；②ci.yml jacoco 报告按平台上传、`always()` 执行、全模块 glob（00a4708/e4e420b/4661b2d）；③sure-process readStream 反射确定性补测，Linux/Windows 100%（ebbe269）；④sure-compress 不可读文件跨平台注入——Windows icacls 拒绝读取（23b5f63） | 2026-10-11 | 🟢 已闭环 | 2026-10-11 | run 38123477632：ubuntu/macos/windows/docs-links 全绿；三平台 jacoco 报告均产出 | commit 136124b/00a4708/e4e420b/4661b2d/ebbe269/23b5f63 |
| HL-2026-10-11-02 | Dependabot | 4 个依赖升级 PR：PR #16 maven-patch 组、PR #14 codeql-action 3→4、PR #13 setup-java 5→6、PR #11 deploy-pages 4→5 | 2026-10-11 | 🟢 已闭环 | 2026-10-11 | 4 个 PR 全部 squash 合并（de8f4a0），合并后 CI 各平台全绿 | PR #16/#14/#13/#11 → commit de8f4a0 |
| HL-2026-10-14-01 | Dependabot | PR #18：actions/stale 9→11（GitHub Action 升级，仅改 .github/workflows/stale.yml） | 2026-10-14 | 🟢 已闭环 | 2026-10-14 | 当日 10-12 创建超 48h 未处理；审查无风险后 squash 合并（424f8e2），合并后 CI 全绿 | PR #18 → commit 424f8e2 |
| HL-2026-10-14-02 | Dependabot | PR #17：maven-minor 组 8 依赖升级（commons-net 3.11.1→3.13.0、commons-compress 1.27.1→1.28.0、xz 1.10→1.12【修 LZMA 编码器 AIOOBE】、surefire-junit4 3.1.2→3.5.6、surefire-junit-platform 3.1.2→3.6.0、japicmp 0.23.1→0.26.3、spotbugs-annotations 4.8.6→4.10.4、guava 33.4.0-jre→33.7.2-jre【修 CVE-2026-102554 反序列化过度内存分配】）；仅改 5 个 pom，无源码变更 | 2026-10-14 | 🟢 已闭环 | 2026-10-14 | 本地全量 verify BUILD SUCCESS（覆盖率门禁全绿）后 squash 合并（5c0fc83）；合并后 Windows CI 暴露 ProcessUtilExtraTest「读取输出被中断」时序 flaky（依赖 carrier 抢占），改为阻塞流+反射+定时中断确定性实现（e851045）后 CI 三平台全绿 | PR #17 → commit 5c0fc83 + e851045 |
