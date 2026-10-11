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
