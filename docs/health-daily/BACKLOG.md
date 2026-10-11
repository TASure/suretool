<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the LICENSE file.
-->
# suretool GitHub 健康治理 · 问题台账（单一事实源）

> 由「suretool GitHub 健康治理日报」每日定时任务维护。状态：🟢 已闭环 / 🟡 处理中 / 🔴 积压。
> 规则：每日跨日对账；"已解决"必须验证通过才可标闭环；红色项日报顶部置顶直至状态变化。

| ID | 来源 | 标题 | 提出日期 | 状态 | 解决日期 | 验证结果 | 关联 PR/Release |
|---|---|---|---|---|---|---|---|
| HL-2026-10-11-01 | CI | Windows 构建连续失败（run 38117725269 / 38118482186）：`SettingUtilTest.环境变量优先于文件` 失败——`expected:<[C:\Users\runneradmin]> but was:<[file_value]>`。根因：`SettingUtil.resolve()` 仅查精确转换名 `HOME`，Windows runner 无 `HOME` 环境变量（只有 `USERPROFILE`）→ 回落到文件值。修复计划：①源码在 `envName(key)==HOME` 且取值为 null 时回退 `USERPROFILE`（治本，Windows 用户主目录可用）；②测试改为平台感知断言（治标）；本地 `mvn -pl sure-core verify` 通过后 push | 2026-10-11 | 🟡 处理中（根因已定位，修复待执行） | — | — |
| HL-2026-10-11-02 | Dependabot | 4 个依赖升级 PR 未合并：PR #16 maven-patch 组（11 天）、PR #14 codeql-action 3→4（13 天）、PR #13 setup-java 5→6（20 天）、PR #11 deploy-pages 4→5（20 天） | 2026-10-11 | 🟡 处理中（待审查/合并） | — | CI 对上游 actions 升级均绿（ubuntu/macos） | 待用户确认合并策略 |
