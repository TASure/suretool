# 批29 PRD：覆盖率巩固（v4.0 生态治理第一棒）

- 状态：已批准
- 目标版本：1.13.0（测试与门禁变更，无 API 变更）

## 背景

method_audit 审计 sure-core 1196 个方法中 9 个未命中 → 补测 8 例覆盖（Multiset.elementSet/iterator 分支、Props.getObj、DateUtil 缓存初始化、DiGraph.addVertexIfAbsent、FileUtil.walkFiles visitFile 回调、ProcessInfo 三 Optional 字段 + 空父 PID），运行时命中 1194/1196（剩余 2 为匿名内部类方法，静态审计局限，实际已被测试触发）。

## 门禁提升（实测覆盖率 → 目标）

| 模块 | 实测行覆盖 | 原门禁 | 新门禁 |
|---|---|---|---|
| sure-json | 83.9% | 0.63 | 0.78 |
| sure-crypto | 87.2% | 0.78 | 0.84 |
| sure-cache | 92.1% | 0.75 | 0.85 |
| sure-core | 90.5% | 0.90 | 0.90（达标） |
| sure-http / sure-cron | 87.8% / 94.2% | 0.86 / 0.93 | 保持（近红线防 CI 抖动） |

## 验收标准

- method_audit 未命中降至 ≤2（匿名类局限）
- 全量 `mvn -am verify` 各模块 BUILD SUCCESS（含新门禁）
- 零 API 变更、零新增依赖
