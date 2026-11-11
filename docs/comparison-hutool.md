# suretool 与 Hutool / Commons 对比（含实测数据）

> 本页用于帮助使用者判断"我的项目该选哪个"。Hutool 是优秀的成熟开源项目，suretool 参考其设计理念；
> 两者定位不同，以下对比**只陈述客观差异**，不做优劣定性。
>
> 数据采集日期：**2026-11-11**。所有数字均为本机实测，未引用任何第三方博客或厂商宣传值。
> 采集方法、样本量与局限见第二节末，任何一项测不出来的都明确写"未测"。

## 一、定位差异

| 维度 | suretool | Hutool | Commons Lang3 / IO |
| --- | --- | --- | --- |
| 定位 | 小而全的 Java 工具库，聚焦常用高频能力 | 大型工具库，覆盖面极广（含更多领域扩展） | 单一职责的工具库（Lang / IO 各一个 jar） |
| JDK 要求 | **仅 JDK 25+**（`maven.compiler.release=25`） | JDK 8+（5.x 兼容老版本） | JDK 8+ |
| 包名 | `com.sure.tool` | `cn.hutool` | `org.apache.commons.*` |
| 发布形态 | 29 个 Maven 模块，`sure-all` 聚合 24 个工具模块 | `hutool-all` 单 jar 聚合全部能力 | 每个能力一个 jar |
| 第三方依赖 | 核心模块**零必需第三方依赖**，专项能力以 `optional` 声明 | 核心零依赖，扩展模块带第三方依赖 | 零必需第三方依赖 |
| 质量门禁 | 2420 个 `@Test`、JaCoCo 行覆盖 ≥0.98、japicmp 二进制兼容、CodeQL / SpotBugs / Checkstyle | 由上游项目自行维护 | 由 ASF 维护 |

## 二、实测数据（2026-11-11）

### 2.1 采集环境

| 项 | 值 |
| --- | --- |
| CPU | 12th Gen Intel(R) Core(TM) i5-12500，6 物理核 / 12 逻辑核 |
| 内存 | 15.7 GB |
| 操作系统 | Microsoft Windows 11 Pro，build 26300 |
| JDK | OpenJDK `25.0.4.0.4`（`E:\java\jdk25`） |
| suretool 版本 | 1.20.0（本地 `mvn -B install` 构建产物） |
| 对照组版本 | `cn.hutool:hutool-all:5.8.38`、`org.apache.commons:commons-lang3:3.17.0`、`commons-io:commons-io:2.18.0` |
| 采集时间 | 2026-11-11 10:18（本机同时存在一个无关的 Maven 构建进程占用 CPU） |

### 2.2 体积与依赖

统计的是各制品自身的 class 条目与字节数（不含 sources / javadoc / tests 产物）。

| 制品 | jar 数 | 总字节 | class 条目 | 必需第三方运行期依赖 |
| --- | --- | --- | --- | --- |
| suretool（`sure-all` 传递的 24 个模块） | 24 | 746,605（约 729 KiB） | 321 | 0 |
| `hutool-all:5.8.38` | 1 | 2,715,417（约 2.59 MiB） | 1,814 | 0 |
| `commons-lang3:3.17.0` | 1 | 673,587（约 658 KiB） | 395 | 0 |
| `commons-io:2.18.0` | 1 | 538,910（约 526 KiB） | 370 | 0 |

口径说明：

- `sure-all` 自身 jar 只有 2,561 字节，内容仅 `META-INF/MANIFEST.MF` + `META-INF/maven/**/pom.xml`——它是**聚合 POM 型模块，不打包任何 class**；真实体积必须按它传递的 24 个模块求和，表中即此口径。单独引入 `sure-all` 只增加 1 个空壳依赖，class 来自 24 个模块。
- suretool 另有 15 条 `<optional>true</optional>` 第三方声明（POI、PDFBox、Jakarta Mail、commons-net、ZXing、commons-compress），只有显式引入专项模块时才需要，不计入上表。
- Hutool / Commons 三个 jar 的必需第三方依赖均为 0，这一点与 suretool 核心模块相同。

### 2.3 进程启动增量

方法：新起一个 JVM 进程，在 `main` 里只触碰一个目标类的一次调用（打印结果长度），用 `Stopwatch` 包住整个进程从启动到退出的墙钟时间；每项跑 **11 次取中位数**；再减掉空 `main`（noop）的中位数作为"加载增量"。

| 场景 | 中位耗时 | 最小值 | 最大值 | 相对 noop 的增量 |
| --- | --- | --- | --- | --- |
| noop（空 main，基线） | 258.9 ms | 237.4 ms | 1138.9 ms | — |
| suretool（`sure-core` + `sure-json`） | 296.9 ms | 279.9 ms | 326.8 ms | **+38.0 ms** |
| commons-lang3 | 332.1 ms | 314.0 ms | 389.7 ms | +73.1 ms |
| commons-io | 319.9 ms | 296.6 ms | 372.3 ms | +61.0 ms |
| hutool-all | 479.6 ms | 443.8 ms | 529.8 ms | +220.6 ms |

解读边界（重要）：这一项测的是"把某个库放进 classpath、并加载其中一个类"的代价，**不等于**完整应用的启动加速比。JDK 自身启动占 250 ms 左右，绝对值噪声大；只有"增量"列有比较意义，且仅代表本机本次测量。

### 2.4 常见操作吞吐

方法：每个操作 **预热 5000 次**，再取 **9 个样本 × 5000 次迭代**，报告 `System.nanoTime()` 得到的 ns/op 中位数；结果累加到局部变量、循环结束后写入 `volatile` 字段，避免 JIT 把调用当作死代码消除。**非 JMH**，没有 fork、没有 `-XX:+UseSerialGC` 之类的隔离措施。

固定输入：`WORDS` 为 64 个形如 `item-N` 的字符串；`JSON` 为
`{"id":123,"name":"sure","ok":true,"tags":["a","b","c"],"nested":{"x":1,"y":2}}`。

**字符串拼接（64 个元素用逗号连接）**

| 实现 | ns/op | 约 ops/s |
| --- | --- | --- |
| JDK `String.join` | 847 | 1,180,638 |
| suretool `StrUtil.join` | 1,212 | 825,083 |
| commons-lang3 `StringUtils.join` | 1,880 | 531,915 |
| hutool `StrUtil.join` | 6,636 | 150,693 |

**JSON 解析（上述 5 字段文档，取顶层字段数）**

| 实现 | ns/op | 约 ops/s |
| --- | --- | --- |
| suretool `JSONUtil.parseObj` | 870 | 1,149,425 |
| hutool `JSONUtil.parseObj` | 3,042 | 328,731 |
| commons-lang3 / commons-io | 未测 | 未测（无 JSON API） |

**集合拷贝（把 64 元素列表复制成新列表）**

| 实现 | ns/op | 约 ops/s |
| --- | --- | --- |
| suretool `CollUtil.toList` | 93 | 10,752,688 |
| hutool `CollUtil.newArrayList` | 102 | 9,803,922 |
| JDK `new ArrayList<>(list)` | 255 | 3,921,569 |
| commons-lang3 / commons-io | 未测 | 未测（无等价集合拷贝 API） |

### 2.5 局限（请连同上面的数字一起读）

1. **不是 JMH**：没有独立 fork、没有死代码消除黑名单、没有多轮 JIT 状态分离。数字适合判断"数量级差异"，不适合做 5% 级别的结论。
2. **存在并行负载**：采集时本机另有一个无关的 Maven 构建进程在跑，noop 基线最大值出现 1138.9 ms 的离群值；因此一律取中位数而非均值。
3. **单机单次**：i5-12500 + Windows 11 + JDK 25 的组合只代表这一台机器；不同 GC、不同 CPU、Linux 上数字会变。
4. **单次调用语义**：字符串拼接只测 64 元素、JSON 只测 5 字段小文档、集合拷贝只测 64 元素 `ArrayList`；大数据量、深层嵌套、并发场景均**未测**。
5. **suretool 只加载了 `sure-core` + `sure-json`**：启动项没有把 24 个模块全部加载，因此不能解读为"引入全部模块后只多 38 ms"。
6. Commons Lang3 / IO 不具备 JSON 解析与集合拷贝的等价 API，对应行明确标注"未测"，不做推算。

### 2.6 复现方式

采集程序是一个独立的单文件 `Bench.java`（不参与 Maven 构建），两个入口：

```bash
# 启动增量：内部执行一次目标调用后立即退出
java -cp "<bench-out>;<library-jars>" Bench startup sure
java -cp "<bench-out>;<library-jars>" Bench startup hutool
java -cp "<bench-out>;<library-jars>" Bench startup commons-lang3

# 吞吐：预热 5000，9 样本 × 5000 次，输出 ns/op 中位数
java -cp "<bench-out>;<library-jars>" Bench bench
```

外层用 PowerShell 对 `startup` 跑 11 次取中位数，并用空 classpath 的 `startup noop` 做基线。核心测量代码：

```java
private static long measure(Runnable op) {
	long[] samples = new long[9];
	for (int i = 0; i < 5000; i++) { op.run(); }          // 预热
	for (int s = 0; s < samples.length; s++) {
		long start = System.nanoTime();
		for (int i = 0; i < 5000; i++) { op.run(); }
		samples[s] = System.nanoTime() - start;
	}
	Arrays.sort(samples);
	return samples[samples.length / 2] / 5000;            // 中位数 ns/op
}
```

## 三、设计取向

| 项目 | suretool 选择 | 说明 |
| --- | --- | --- |
| 加密默认值 | AES-GCM（认证加密）、RSA-OAEP、最小 2048 位密钥 | 默认安全基线，杜绝弱算法/弱模式被误用 |
| DES | `@Deprecated`，仅保留解密存量数据 | 新代码强制走 AES |
| 线程 | 静态工具方法 + 显式并发工具（ThreadUtil/ExecutorBuilder） | 提供更多高级并发组件 |
| JSON | 自带轻量实现（JSONObject/JSONArray/JSONUtil） | 与 Hutool 的 JSON 实现互不兼容 |
| 校验/脱敏 | ValidatorUtil、DesensitizedUtil | 功能类似 |
| 敏感词 | DFA 实现（sure-dfa） | SensitiveUtil 类似 |

## 四、使用体验

```java
// Hutool
String s = cn.hutool.core.util.StrUtil.removePrefixIgnoreCase("Hello", "he");
String md5 = cn.hutool.crypto.SecureUtil.md5("data");

// suretool
String s = com.sure.tool.util.StrUtil.removeSuffixIgnoreCase("Hello", "LLO");
String md5 = com.sure.tool.crypto.SecureUtil.md5("data");
```

两库 API 命名风格相近（工具类 + 静态方法），Hutool 用户迁移成本低。

## 五、何时选择

**选择 suretool：**
- 项目已基于 JDK 25+（虚拟线程、record、switch 模式匹配等新特性）；
- 希望依赖最小、安全默认值开箱即用；
- 需要经 CodeQL / SpotBugs / JaCoCo / japicmp 门禁验证的"小而可信"组件；
- 希望以 `sure-bom` 统一版本管理、按需引入模块（`sure-all` 会带来 24 个模块，按需引入更省）。

**选择 Hutool：**
- 需要兼容 JDK 8/11/17 等老版本；
- 需要 Hutool 生态中更广泛的扩展能力（如 hutool-extra 的邮件/模板等）；
- 偏好单 jar 引入、不想管理多模块依赖坐标。

**选择 Commons Lang3 / IO：**
- 只需要单一职责的字符串/IO util，且希望与既有 Apache 生态版本对齐；
- 不需要 JSON、加解密、DFA、JSON→Bean 这类能力。

## 六、迁移建议

1. `sure-bom` 引入后按模块替换：`sure-core`（字符串/集合/日期/IO/反射）、`sure-json`、`sure-crypto` 等；
2. 加密迁移注意：suretool 的 `AesUtil` 密文格式为 `IV || ciphertext+tag`（Hex/Base64），与 Hutool 的 AES 输出不互解；
3. JSON 迁移注意：suretool 与 Hutool 的 `JSONObject` 类型不兼容，跨库传递需转 `String` 或 `Map`；
4. 若只需某个工具方法，直接复制实现（Apache-2.0 许可下合规）比整体迁移更轻。

## 七、许可与致谢

两库均基于 Apache License 2.0。suretool 的类设计与命名参考 Hutool 公开 API 风格，
部分实现思路借鉴了 Hutool（如缓存策略、DFA 过滤），并已在类注释中标注。
