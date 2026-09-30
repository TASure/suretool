# PRD：Benchmark 对齐（sure-benchmark 覆盖批 7/批 8）

- 状态：Accepted（2026-09-30）｜ 模块：`sure-benchmark` ｜ 版本：1.5.0
- 依赖：jmh-core/jmh-generator-annprocess、hutool-core、hutool-json、guava（均 test/benchmark 域）

## A. 目标

现有基准仅覆盖 core/json 四个工具类（CollUtil/DateUtil/Json/StrUtil）。
本批把批 7/批 8 新增模块的**确定性热点路径**纳入 JMH 门禁与报告，
保持「suretool ≤ 1.5× hutool」回归门禁，同时用 Guava 对照展示零依赖事件总线的性能。

## B. 新增基准类（方法名必须以 sure/hutool/guava 前缀开头以被 gate 解析）

### B1. `MathBenchmark`
- `sureMathIsPrime` vs `hutoolMathIsPrime`（hutool-core MathUtil.isPrime）——门禁配对
- `sureMathGcd`（MathUtil.gcd，无 hutool 对应，单独展示）
- `sureBigDecimalRound`（BigDecimalUtil 10 位 HALF_UP）vs `plainBigDecimalRound`（原生 setScale，仅展示）
- `sureRandomString` vs `hutoolRandomString`（hutool RandomUtil.randomString）——门禁配对

### B2. `EventBenchmark`
- `sureEventPost`（EventBus 同步发布/订阅吞吐，1 listener）
- `guavaEventPost`（Guava EventBus 同场景对照，仅展示不参与门禁）

### B3. `SocketBenchmark`（不新增）
网络 IO 在共享 CI runner 上噪声极大且无可控对标基准，PRD 明确**不做**（文档记录）。

## C. 工程改动
- sure-benchmark pom：新增依赖 sure-math、sure-event
- benchmark.yml paths：追加 `sure-math/**`、`sure-event/**`
- 本地全量跑一次，产出 `docs/benchmark/REPORT.md`（含与 v1.3.0 报告的基线对比）

## D. 验收
- A1 本地 `mvn -pl sure-benchmark exec:java` 全部基准完成无 FAILURE
- A2 gate 脚本能解析新基准（sure/hutool 配对 ≥2 对）
- A3 报告落盘 docs/benchmark/REPORT.md，含每基准 ns/op 与 ratio
- A4 CI paths 触发条件覆盖 math/event 源码变更

## E. 非目标
- 不做网络/进程/压缩 IO 类基准（噪声无意义）
- 不调整现有 4 类基准与门禁阈值
