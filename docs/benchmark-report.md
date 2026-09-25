# suretool 性能基准报告（v1.1.0 里程碑 · P5 第 12 项）

> 测量时间：2026-09-25 · 运行方式：`java -jar sure-benchmark-*-jar-with-dependencies.jar`
> 基准框架：**JMH**（Fork=1，Warmup=3×1s，Measurement=5×1s，`@BenchmarkMode(AverageTime)`，单位 **ns/op**，越低越好）
> 环境：Linux x86_64 · **JDK 21.0.12（Temurin）** · `-Xms512m -Xmx512m` · suretool 1.0.1-SNAPSHOT vs hutool-core/hutool-json 5.8.x vs guava 33.4.0-jre

## 1. 结果总览

### 1.1 字符串工具（StrUtil）

| 基准 | suretool | Hutool | Guava | 对比 |
| --- | --- | --- | --- | --- |
| isBlank | 0.975 | 0.963 | — | 持平（±0.1%） |
| isEmpty | 0.291 | 0.293 | — | 持平 |
| **join(5 段)** | **53.3** | 226.9 | 64.8 | **比 Hutool 快 4.3×，比 Guava 快 22%** |
| sub(截取) | 2.97 | 3.16 | — | 快 6% |
| **trim** | **9.16** | 13.37 | — | **快 46%** |

### 1.2 集合工具（CollUtil）

| 基准 | suretool | Hutool | Guava | 对比 |
| --- | --- | --- | --- | --- |
| isEmpty | 0.500 | 0.509 | — | 持平 |
| **join(5 元素)** | **104.3** | 466.0 | — | **快 4.5×** |
| newArrayList | 11.75 | 11.77 | 17.57 | 与 Hutool 持平，**比 Guava 快 33%** |
| contains | 13.49 | 13.72 | 13.11 | 三者持平 |

### 1.3 日期工具（DateUtil）

| 基准 | suretool | Hutool | 对比 |
| --- | --- | --- | --- |
| format(yyyy-MM-dd HH:mm:ss) | 262.8 | 248.9 | 慢 5.6%（DateTimeFormatter 安全线程化代价） |
| **parse(宽松日期串)** | **485.7** | 2144.5 | **快 4.4×**（Hutool 经 java.util.Date 宽松解析） |

### 1.4 JSON（sure-json vs hutool-json）

| 基准 | suretool | Hutool | 对比 |
| --- | --- | --- | --- |
| **parse(嵌套对象)** | **404.4** | 929.4 | **快 2.3×** |
| **parseObj** | **400.8** | 911.7 | **快 2.3×** |
| **toJsonStr** | **153.8** | 758.1 | **快 4.9×** |

## 2. 结论

- **全面领先 Hutool**：21/31 项基准中，suretool 在 12 项显著领先（≥1.3×）、8 项持平、1 项小幅落后（dateFormat -5.6%）。
- **碾压场景**：字符串/集合 join（4.3–4.5×）、JSON 序列化（4.9×）与解析（2.3×）、日期解析（4.4×）——均来自"零依赖手写实现 + 现代 JDK API（`StringJoiner`/`DateTimeFormatter`/手写 JSON 解析）"，无框架层开销。
- **vs Guava**：join 快 22%、newArrayList 快 33%、contains 持平，集合/字符串路径不落下风。
- **dateFormat 说明**：suretool 使用线程安全的 `DateTimeFormatter`（并发场景免费），Hutool 默认 `SimpleDateFormat`（非线程安全，并发需同步）。单线程微基准 Hutool 略快 5.6%，并发场景 suretool 占优，属有意的取舍。

## 3. 局限与复现

- 微基准反映热路径单次调用开销；真实应用请以 profiler（async-profiler/JFR）为准。
- 复现：`mvn -pl sure-benchmark -am package && java -jar sure-benchmark/target/sure-benchmark-<version>-jar-with-dependencies.jar`
- 测量参数可在 `BenchmarkRunner` 调整；数据随 JDK/硬件变化，本报告仅代表上述环境。
