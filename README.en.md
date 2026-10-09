# suretool



![CI](https://img.shields.io/github/actions/workflow/status/TASure/suretool/ci.yml?branch=main\&label=CI)



![Coverage](https://img.shields.io/codecov/c/github/TASure/suretool)



![Release](https://img.shields.io/github/v/release/TASure/suretool)



![License](https://img.shields.io/badge/license-Apache%202.0-blue)



![Java](https://img.shields.io/badge/Java-25+-blue)



![Stars](https://img.shields.io/github/stars/TASure/suretool)

[中文文档](README.md) | English

A **small but complete** Java utility library, inspired by the design philosophy of

[Hutool](https://doc.hutool.cn/pages/index/). It wraps common JDK APIs into

static utility methods to reduce boilerplate and lower development cost.



* Package prefix: `com.sure.tool`

* Language level: **Java 21+** (zero third-party runtime dependencies in the core domain; Office domain is built on Apache POI 5.5.1)

* License: Apache License 2.0

* Docs: [Modules & API portal (28 modules + javadoc.io)](docs/modules.md) · [Class index](docs/index.md) · [5-minute quickstart](docs/posts/quickstart.md) · [Hutool comparison](docs/comparison-hutool.md) · [Benchmark report](docs/benchmark-report.md) · [Releasing](docs/RELEASING.md) · [Maintaining](docs/MAINTAINING.md) · [GitHub setup](docs/github-setup.md) · [Dependencies](docs/dependencies.md)

## Why suretool?



* **Secure by default** — AES-GCM authenticated encryption, RSA-OAEP-SHA256, minimum 2048-bit RSA keys, deprecated DES kept only for decrypting legacy data.

* **Small but complete** — 28 modules + BOM + examples + Spring Boot starter; import only what you need.

* **Quality-gated** — Checkstyle 0 violations, SpotBugs (Max), JaCoCo line coverage ≥ 90% on sure-core, CodeQL + OSV-Scanner + Dependabot enabled in CI.

* **Modern Java** — built with `--release 25`; virtual threads, records, sealed classes, pattern matching and structured concurrency are first-class.

## Quick start

### Maven



```
\<dependencyManagement>

&#x20;   \<dependencies>

&#x20;       \<dependency>

&#x20;           \<groupId>io.github.tasure\</groupId>

&#x20;           \<artifactId>sure-bom\</artifactId>

&#x20;           \<version>1.0.0\</version>

&#x20;           \<type>pom\</type>

&#x20;           \<scope>import\</scope>

&#x20;       \</dependency>

&#x20;   \</dependencies>

\</dependencyManagement>

\<dependency>

&#x20;   \<groupId>io.github.tasure\</groupId>

&#x20;   \<artifactId>sure-all\</artifactId>

\</dependency>
```

Or import individual modules (lighter):



```
\<dependency>

&#x20;   \<groupId>io.github.tasure\</groupId>

&#x20;   \<artifactId>sure-core\</artifactId>

\</dependency>

\<dependency>

&#x20;   \<groupId>io.github.tasure\</groupId>

&#x20;   \<artifactId>sure-json\</artifactId>

\</dependency>

\<dependency>

&#x20;   \<groupId>io.github.tasure\</groupId>

&#x20;   \<artifactId>sure-crypto\</artifactId>

\</dependency>
```

### Examples



```
String json = JSONUtil.toJsonStr(Map.of("name", "Alice", "age", 30));

String md5 = SecureUtil.md5("hello");

String text = StrUtil.removeSuffixIgnoreCase("Hello.txt", ".TXT"); // "Hello"

EventBus bus = new EventBus();                              // DeadEvent on unconsumed events
bus.subscribe(EventBus.DeadEvent.class, e -> log(e.getEvent()));

DiGraph<String> g = new DiGraph<>();                        // graph algorithms
g.addEdge("A", "B");
List<String> order = GraphUtil.topologicalSort(g).orElseThrow();

double std = StatUtil.stdDev(new double[] { 1, 2, 3, 4, 5 }); // statistics
```

Run all runnable demos:



```
\# 首次运行先安装依赖模块到本地仓库，再单独运行 examples（避免 -am 在父项目执行 exec 目标）

mvn -pl sure-examples -am install -DskipTests

mvn -pl sure-examples exec:java
```

## Modules



| Module                     | Coordinates                                 | Capabilities                                               | Runtime deps           |
| -------------------------- | ------------------------------------------- | ---------------------------------------------------------- | ---------------------- |
| `sure-core`                | `io.github.tasure:sure-core`                | util/codec/collection/date/io/lang/bean/thread core domain | none                   |
| `sure-json`                | `io.github.tasure:sure-json`                | JSON parse/serialize/Bean conversion                       | core                   |
| `sure-http`                | `io.github.tasure:sure-http`                | HTTP client / URL utilities                                | core                   |
| `sure-crypto`              | `io.github.tasure:sure-crypto`              | Hash / AES / DES / RSA / HMAC                              | core                   |
| `sure-cron`                | `io.github.tasure:sure-cron`                | Cron expressions / scheduler                               | core                   |
| `sure-cache`               | `io.github.tasure:sure-cache`               | FIFO / LRU / LFU / Timed caches                            | core                   |
| `sure-xml`                 | `io.github.tasure:sure-xml`                 | XML to Map/Bean conversion                                 | core                   |
| `sure-poi`                 | `io.github.tasure:sure-poi`                 | Excel / Word read & write                                  | core + POI             |
| `sure-captcha`             | `io.github.tasure:sure-captcha`             | Graphic captcha (line/circle/distortion)                   | none                   |
| `sure-jwt`                 | `io.github.tasure:sure-jwt`                 | JWT issue/verify (HS/RS)                                   | core + crypto + json   |
| `sure-dfa`                 | `io.github.tasure:sure-dfa`                 | Sensitive-word filtering (prefix tree / stop words)        | none                   |
| `sure-bom`                 | `io.github.tasure:sure-bom`                 | BOM for unified version management                         | —                      |
| `sure-all`                 | `io.github.tasure:sure-all`                 | Aggregated module (everything)                             | all                    |
| `sure-examples`            | `io.github.tasure:sure-examples`            | Runnable demos (14 examples)                               | sure-all               |
| `sure-log`                | `io.github.tasure:sure-log`                | Zero-dep logging facade (SLF4J delegate / Console fallback) | core                   |
| `sure-db`                 | `io.github.tasure:sure-db`                 | JDBC data access (Entity / SqlRunner / pool / tx / paging) | core                   |
| `sure-script`             | `io.github.tasure:sure-script`             | JSR-223 script facade (engine probe / compile cache)       | core                   |
| `sure-template`           | `io.github.tasure:sure-template`           | Template engine (Template / SimpleTemplate / Bean render)  | core                   |
| `sure-aop`                | `io.github.tasure:sure-aop`                | Zero-dep AOP (Aspect / ProxyUtil / method matching)        | core                   |
| `sure-pdf`                | `io.github.tasure:sure-pdf`                | PDF facade (PdfWriter / PdfUtil, PDFBox 3)                 | core + pdfbox          |
| `sure-process`            | `io.github.tasure:sure-process`            | Process facade (timeout kill / virtual-thread pipe)        | core                   |
| `sure-math`               | `io.github.tasure:sure-math`               | Math & statistics (MathUtil / BigDecimalUtil / StatUtil)   | core                   |
| `sure-compress`           | `io.github.tasure:sure-compress`           | High-ratio compression (SevenZ/LZMA2 anti-traversal / Brotli)| core + compress       |
| `sure-socket`             | `io.github.tasure:sure-socket`             | TCP facade (SocketUtil / virtual-thread SocketServer)      | core                   |
| `sure-event`              | `io.github.tasure:sure-event`              | Event bus (functional/annotation, sync/virtual-thread async, DeadEvent) | core |
| `sure-extra`              | `io.github.tasure:sure-extra`              | Mail SMTP / FTP / QR code / image processing               | core + jakarta.mail + commons-net + zxing |
| `sure-benchmark`          | `io.github.tasure:sure-benchmark`          | JMH benchmarks (vs Hutool / Guava)                        | sure-all               |
| `sure-spring-boot-starter` | `io.github.tasure:sure-spring-boot-starter` | Spring Boot auto-configuration entry                       | sure-all + spring-boot |

Full module & API portal (28 modules + javadoc.io links): [docs/modules.md](docs/modules.md).

## Roadmap



* [x] P0/P1/P2: modularization (28 modules) + engineering gates (Checkstyle/SpotBugs/JaCoCo/License)

* [x] JDK 25 baseline (v1.6.0)

* [x] Released v1.0.0 → v1.10.0 on Maven Central (11 releases, CI-driven)

* [x] v2.0 batch 21/22/23: concurrency (StripedLock/StructuredTaskUtil) · IO/text (FileUtil/IoUtil/StrUtil) · event+graph+math (DeadEvent/DiGraph/GraphUtil/StatUtil)

* [x] Docs portal: 7 tutorials, javadoc.io links, bilingual README

* [ ] v1.11.0 release (batch 21-24)

* [ ] v2.0 batch 24+ / v3.0 differentiated leadership (virtual threads first-class, Fluent API)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). All PRs must pass: Checkstyle 0 violations, SpotBugs, JaCoCo ≥ 90% (sure-core), license headers.



* [CODE\_OF\_CONDUCT.md](CODE_OF_CONDUCT.md) (Contributor Covenant 2.1)

* [SECURITY.md](SECURITY.md) (report a vulnerability; 24h acknowledgment, 72h assessment)

* Security scanning: CodeQL on push/PR/weekly + Dependabot auto-updates

## License

[Apache License 2.0](LICENSE). Design and API naming reference [Hutool](https://gitee.com/chinabugotech/hutool) (also Apache-2.0); implementation ideas are credited in class-level Javadoc where applicable.