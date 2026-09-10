# sure-benchmark（JMH 基准套件）

> 对标 ROADMAP P2：**JMH 基准套件：StrUtil/集合/日期/JSON 对比 Hutool 与 JDK 基线，输出报告**。

## 运行

```bash
mvn -pl sure-benchmark exec:java
```

或打可执行 jar：

```bash
mvn -pl sure-benchmark package
java -jar sure-benchmark/target/sure-benchmark-0.1.0-jar-with-dependencies.jar
```

## 覆盖路径

| 基准类 | 对比项 |
| --- | --- |
| `StrUtilBenchmark` | isBlank / isEmpty / trim / join / sub（suretool vs Hutool StrUtil） |
| `CollUtilBenchmark` | isEmpty / join / newArrayList / contains（suretool vs Hutool CollUtil） |
| `DateUtilBenchmark` | format / parse（suretool SimpleDateFormat 线程缓存实现 vs Hutool 实现） |
| `JsonBenchmark` | parseObj / toJsonStr / parse（suretool 零依赖递归下降 vs Hutool JSONUtil） |

## 基准配置

每个基准 `@Fork(1)`、warmup 1s × 3 轮、measurement 1s × 5 轮，输出平均耗时（ns/op）。

## 报告

完整运行后，将结果追加到 ROADMAP「JMH 基准报告」章节；本模块仅承担执行与代码，不参与测试与覆盖率门禁。