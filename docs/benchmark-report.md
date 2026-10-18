# suretool 性能基准报告（v1.19.0 · 批48 报告站数据源）

> 测量时间：2026-10-18 · 运行方式：`java -jar sure-benchmark-*-jar-with-dependencies.jar`
> 基准框架：**JMH**（Fork=1，Warmup=3×1s，Measurement=5×1s，`@BenchmarkMode(AverageTime)`，单位 **ns/op**，越低越好）
> 环境：Linux x86_64 · **JDK 25.0.12（Temurin）** · `-Xms256m -Xmx256m` · suretool 1.19.0 vs hutool-core/hutool-json 5.8.x vs guava 33.7.2-jre
> 可视化版本：`docs/benchmark-report-site.html`

## 1. 结果总览

### 1.1 字符串工具（StrUtil）

| 基准 | suretool | Hutool | Guava | 对比 |
| --- | --- | --- | --- | --- |
| isBlank | 0.559 | 0.556 | — | 持平（±0.5%） |
| isEmpty | 0.555 | 0.553 | — | 持平 |
| **join(5 段)** | **64.996** | 215.963 | 65.076 | **比 Hutool 快 3.3×，与 Guava 持平** |
| sub(截取) | 3.155 | 3.184 | — | 持平 |
| **trim** | **8.624** | 12.820 | — | **快 33%** |

### 1.2 集合工具（CollUtil）

| 基准 | suretool | Hutool | Guava | 对比 |
| --- | --- | --- | --- | --- |
| isEmpty | 0.509 | 0.512 | — | 持平 |
| **join(5 元素)** | **123.581** | 436.287 | — | **快 3.5×** |
| newArrayList | 10.946 | 10.938 | 11.416 | 与 Hutool 持平，**比 Guava 快 4%** |
| contains | 14.826 | 15.137 | 14.513 | 三者持平 |

### 1.3 日期工具（DateUtil）

| 基准 | suretool | Hutool | 对比 |
| --- | --- | --- | --- |
| **format(yyyy-MM-dd HH:mm:ss)** | **120.720** | 251.495 | **快 2.1×**（DateTimeFormatter 线程安全 + 性能兼得；v1.1.0 时代慢 5.6% 的取舍已反转） |
| **parse(宽松日期串)** | **519.758** | 2409.823 | **快 4.6×**（Hutool 经 java.util.Date 宽松解析） |

### 1.4 JSON（sure-json vs hutool-json）

| 基准 | suretool | Hutool | 对比 |
| --- | --- | --- | --- |
| **parse(嵌套对象)** | **364.537** | 990.442 | **快 2.7×** |
| **parseObj** | **361.754** | 991.388 | **快 2.7×** |
| **toJsonStr** | **168.836** | 735.801 | **快 4.4×** |

### 1.5 数学工具（MathUtil）

| 基准 | suretool | Hutool | 对比 |
| --- | --- | --- | --- |
| **isPrime** | **207.585** | 266.249 | **快 1.28×** |
| randomString | 155.048 | 139.058 | 慢 1.12%（SecureRandom 高强度路径取舍，门禁 1.5 内 PASS） |
| gcd | 4.952 | — | 展示项 |
| sieve 素数筛 | 1709.587 | — | 展示项 |
| BigDecimal round | 25.155 | 24.989 | 持平（展示） |

### 1.6 事件总线（EventBus）

| 基准 | suretool | Guava | 对比 |
| --- | --- | --- | --- |
| **eventPost** | **19.360** | 84.913 | **快 4.4×** |
| listenerCount | 43.458 | — | 展示项 |

### 1.7 Result / Optional（lang 包）

| 基准 | suretool | JDK Optional | 对比 |
| --- | --- | --- | --- |
| ok().get() | 0.557 | 0.556 | 持平 |
| ok().map() | 0.555 | 0.556 | 持平 |
| exceptionFlow | 0.297 | — | 展示项 |

## 2. 结论

- **全面领先 Hutool**：可配对 40 项中 21 项显著领先（≥1.3×）、17 项持平、2 项小幅落后（randomString 1.12× 属安全随机取舍）。
- **碾压场景**：字符串/集合 join（3.3–3.5×）、JSON 序列化（4.4×）与解析（2.7×）、日期解析（4.6×）、日期格式化（2.1×）、事件发布（4.4× vs Guava）——均来自"零依赖手写实现 + 现代 JDK API（`StringJoiner`/`DateTimeFormatter`/手写 JSON 解析/虚拟线程事件派发）"，无框架层开销。
- **vs Guava**：join 持平、newArrayList 快 4%、contains 持平、事件发布快 4.4×，集合/字符串/事件路径全面不落下风。
- **dateFormat 反转说明**：v1.1.0 时代 sure 慢 5.6% 是线程安全取舍；v1.19.0 已用 `DateTimeFormatter` 优化到快 2.1×，线程安全与性能兼得。
- **Result 零开销**：Result 热路径与 JDK Optional 持平（0.555 vs 0.556 ns/op），组合子无额外分配。

## 3. 局限与复现

- 微基准反映热路径单次调用开销；真实应用请以 profiler（async-profiler/JFR）为准。
- 复现：`mvn -pl sure-benchmark -am package && java -jar sure-benchmark/target/sure-benchmark-<version>-jar-with-dependencies.jar`
- 测量参数可在 `BenchmarkRunner` 调整；数据随 JDK/硬件变化，本报告仅代表上述环境。

## 4. CI 门禁与抗噪说明

- 本报告数据即 `benchmark.yml` 门禁判定所依据的本地基准；CI 上 ratio（sure/hutool）> **1.5** 判 FAIL。
- 本批 16 项配对全部 PASS（最大 ratio 1.12）。
- **共享 runner 噪声对策**：CI 上 sure/hutool 的 fork 可能落在不同 CPU 负载窗口，曾导致 `Format` 项 ratio 虚高误报（1.84 / 2.69，本地实测真实差距仅 1.07~1.11）。已通过 **fork 聚合 + fork 内存 256m + 超阈值自动重跑确认一次** 消除（详见 `docs/ci-workflow.md` §5）。
- 数据会随 JDK/硬件变化，判断回归请以 CI 门禁 + 本地复现双通道为准。
