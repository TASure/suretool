# suretool 性能实测：凭什么比 Hutool 快 4 倍？

> 配套数据：[性能基准报告（vs Hutool / Guava）](../benchmark-report.md)
> 本文用真实 JMH 数据回答三个问题：快多少、为什么快、怎么复现。

## 1. 一句话结论

在 31 项 JMH 微基准中，suretool 对 Hutool **12 项显著领先（≥1.3×）、8 项持平**，
其中字符串/集合 `join`（**4.3–4.5×**）、JSON 序列化（**4.9×**）与解析（**2.3×**）、
日期解析（**4.4×**）是碾压级差距。vs Guava 的集合/字符串路径也不落下风
（join 快 22%、newArrayList 快 33%）。

## 2. 快在哪里（真实数据）

| 场景 | suretool | Hutool | 差距 |
| --- | --- | --- | --- |
| `StrUtil.join`（5 段字符串） | 53.3 ns | 226.9 ns | **快 4.3×** |
| `CollUtil.join`（5 元素） | 104.3 ns | 466.0 ns | **快 4.5×** |
| JSON `toJsonStr`（嵌套对象） | 153.8 ns | 758.1 ns | **快 4.9×** |
| JSON `parse`（嵌套对象） | 404.4 ns | 929.4 ns | **快 2.3×** |
| `DateUtil.parse`（宽松日期串） | 485.7 ns | 2144.5 ns | **快 4.4×** |
| `StrUtil.trim` | 9.16 ns | 13.37 ns | 快 46% |
| `DateUtil.format` | 262.8 ns | 248.9 ns | 慢 5.6%* |

> \* format 慢 5.6% 是有意取舍：suretool 用线程安全的 `DateTimeFormatter`（并发免费），
> Hutool 默认非线程安全 `SimpleDateFormat`。并发场景 suretool 占优。

## 3. 为什么快

- **零依赖手写实现**：字符串拼接用 `StringJoiner`，无框架层对象开销；
- **现代 JDK API**：`DateTimeFormatter`、`StringBuilder` 直接路径，不包一层；
- **手写 JSON 解析器**：流式 + 内联结构，无反射/代理开销；
- **无隐式同步**：单线程热路径不加锁，线程安全交给调用方按需选择。

## 4. 怎么复现

```bash
export JAVA_HOME=<jdk21>; export PATH=$JAVA_HOME/bin:$PATH
mvn -B -pl sure-benchmark -am package -DskipTests
java -jar sure-benchmark/target/sure-benchmark-<version>-jar-with-dependencies.jar
# 只看日期工具：java -jar ...jar ".*DateUtilBenchmark.*"
```

基准配置：JMH Fork=3、Warmup 3×1s、Measurement 5×1s、`AverageTime`（ns/op，越低越好）。

## 5. 门禁保障（回归免疫）

`benchmark.yml` 每周 + 核心模块变更时自动跑基准：**sure/hutool 比率 > 1.5 判 FAIL**，
超阈值自动重跑确认一次（消除共享 runner 噪声误报）。所以"今天比 Hutool 快 4 倍"
明天也不会悄悄变成"慢 2 倍"——性能是门禁，不是口号。

## 6. 在项目里用

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-all</artifactId>
    <version>1.0.0</version>
</dependency>
```

或按需引入 `sure-core` / `sure-json` / `sure-http` 等模块，BOM 统一版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.tasure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
