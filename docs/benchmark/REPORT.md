# suretool 性能基准报告

> 生成时间：2026-10-18 · 版本：v1.19.0 · 环境：Linux VM，JDK 25.0.12，JMH forks=1 × 5×1s 测量
> 门禁规则：sureX ↔ hutoolX 配对 ratio ≤ 1.5 通过（.github/scripts/benchmark_gate.py）
> 完整可视化报告站：`docs/benchmark-report-site.html`

## 门禁配对（sure vs hutool）

| 基准 | sure ns/op | hutool ns/op | ratio | 结论 |
|---|---|---|---|---|
| CollUtilBenchmark.sureContains | 14.826 | 15.137 | 0.98 | PASS |
| CollUtilBenchmark.sureIsEmpty | 0.509 | 0.512 | 0.99 | PASS |
| CollUtilBenchmark.sureJoin | 123.581 | 436.287 | 0.28 | PASS |
| CollUtilBenchmark.sureNewArrayList | 10.946 | 10.938 | 1.00 | PASS |
| DateUtilBenchmark.sureFormat | 120.720 | 251.495 | 0.48 | PASS |
| DateUtilBenchmark.sureParse | 519.758 | 2409.823 | 0.22 | PASS |
| JsonBenchmark.sureParse | 364.537 | 990.442 | 0.37 | PASS |
| JsonBenchmark.sureParseObj | 361.754 | 991.388 | 0.37 | PASS |
| JsonBenchmark.sureToJsonStr | 168.836 | 735.801 | 0.23 | PASS |
| MathBenchmark.sureMathIsPrime | 207.585 | 266.249 | 0.78 | PASS |
| MathBenchmark.sureRandomString | 155.048 | 139.058 | 1.12 | PASS |
| StrUtilBenchmark.sureIsBlank | 0.559 | 0.556 | 1.01 | PASS |
| StrUtilBenchmark.sureIsEmpty | 0.555 | 0.553 | 1.00 | PASS |
| StrUtilBenchmark.sureJoin | 64.996 | 215.963 | 0.30 | PASS |
| StrUtilBenchmark.sureSub | 3.155 | 3.184 | 0.99 | PASS |
| StrUtilBenchmark.sureTrim | 8.624 | 12.820 | 0.67 | PASS |

## 展示项（不参与门禁）

| 基准 | ns/op | 说明 |
|---|---|---|
| CollUtilBenchmark.guavaContains | 14.513 | 展示项（三者持平） |
| CollUtilBenchmark.guavaNewArrayList | 11.416 | 展示项（sure 快 4%） |
| EventBenchmark.guavaEventPost | 84.913 | 展示项（sure 快 4.4×） |
| EventBenchmark.sureEventListenerCount | 43.458 | 展示项 |
| MathBenchmark.plainBigDecimalRound | 24.989 | 展示项（sure 25.155 持平） |
| MathBenchmark.sureMathGcd | 4.952 | 展示项 |
| MathBenchmark.sureMathSieve | 1709.587 | 展示项 |
| ResultBenchmark.resultOkGet | 0.557 | 展示项（JDK Optional 0.556 持平） |
| ResultBenchmark.resultOkMap | 0.555 | 展示项（JDK Optional 0.556 持平） |
| StrUtilBenchmark.guavaJoin | 65.076 | 展示项（sure 64.996 持平） |

## 环境与复现

```bash
mvn -pl sure-benchmark -am package -DskipTests
java -jar sure-benchmark/target/sure-benchmark-1.19.1-SNAPSHOT-jar-with-dependencies.jar
python3 .github/scripts/benchmark_gate.py <log>
```

> 注：fat jar 请显式指定当前版本文件名，`ls | head` 会误选旧版本包。
> 数据随 JDK/硬件变化，判断回归请以 CI 门禁 + 本地复现双通道为准。
