# suretool 性能基准报告

> 生成时间：2026-09-30 · 环境：Linux VM，JDK 25.0.12，JMH forks=3 × 5×1s 测量
> 门禁规则：sureX ↔ hutoolX 配对 ratio ≤ 1.5 通过（.github/scripts/benchmark_gate.py）

## 门禁配对（sure vs hutool）

| 基准 | sure ns/op | hutool ns/op | ratio | 结论 |
|---|---|---|---|---|
| CollUtilBenchmark.sureContains | 16.92 | 17.15 | 0.99 | PASS |
| CollUtilBenchmark.sureIsEmpty | 0.56 | 0.55 | 1.01 | PASS |
| CollUtilBenchmark.sureJoin | 83.85 | 486.04 | 0.17 | PASS |
| CollUtilBenchmark.sureNewArrayList | 16.96 | 18.20 | 0.93 | PASS |
| DateUtilBenchmark.sureFormat | 124.88 | 271.63 | 0.46 | PASS |
| DateUtilBenchmark.sureParse | 519.80 | 2435.34 | 0.21 | PASS |
| JsonBenchmark.sureParse | 441.09 | 2435.34 | 0.18 | PASS |
| JsonBenchmark.sureParseObj | 449.07 | 2557.61 | 0.18 | PASS |
| JsonBenchmark.sureToJsonStr | 164.64 | 1918.47 | 0.09 | PASS |
| MathBenchmark.sureMathIsPrime | 353.89 | 327.95 | 1.08 | PASS |
| MathBenchmark.sureRandomString | 173.84 | 165.71 | 1.05 | PASS |
| StrUtilBenchmark.sureIsBlank | 1.00 | 0.99 | 1.01 | PASS |
| StrUtilBenchmark.sureIsEmpty | 0.33 | 0.55 | 0.60 | PASS |
| StrUtilBenchmark.sureJoin | 52.62 | 486.04 | 0.11 | PASS |
| StrUtilBenchmark.sureSub | 6.89 | 7.02 | 0.98 | PASS |
| StrUtilBenchmark.sureTrim | 11.67 | 15.59 | 0.75 | PASS |

## 展示项（不参与门禁）

| 基准 | ns/op | 说明 |
|---|---|---|
| CollUtilBenchmark.guavaContains | 16.74 | 展示项 |
| CollUtilBenchmark.guavaNewArrayList | 20.20 | 展示项 |
| EventBenchmark.guavaEventPost | 168.56 | 展示项 |
| MathBenchmark.plainBigDecimalRound | 30.54 | 展示项 |
| StrUtilBenchmark.guavaJoin | 50.47 | 展示项 |

## 环境与复现

```bash
mvn -pl sure-benchmark -am package -DskipTests
java -jar sure-benchmark/target/sure-benchmark-1.4.1-SNAPSHOT-jar-with-dependencies.jar
python3 .github/scripts/benchmark_gate.py <log>
```

> 注：fat jar 请显式指定 `1.4.1-SNAPSHOT` 文件，`ls | head` 会误选旧版本包。