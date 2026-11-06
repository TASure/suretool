# suretool

[![CI](https://img.shields.io/github/actions/workflow/status/TASure/suretool/ci.yml?branch=main&label=CI)](https://github.com/TASure/suretool/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.tasure/sure-core?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.tasure/sure-core)
[![Coverage](https://img.shields.io/endpoint?url=https://tasure.github.io/suretool/coverage/badge.json)](https://codecov.io/gh/TASure/suretool)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-25%2B-blue)](https://openjdk.org/projects/jdk/25/)

A zero-dependency Java toolkit for **JDK 25+**. Its 29 modules turn everyday work — strings, collections, dates, IO, concurrency, JSON, HTTP, crypto — into static methods. The core domain needs nothing but the JDK, and you can pull in one module at a time.

**If your project still runs on JDK 17 or 21, this project is not for you** — stay on [Hutool](https://hutool.cn/) or [Apache Commons](https://commons.apache.org/). Every module here is compiled with `--release 25`, and there is no lower-version build. That is deliberate: we would rather use records, sealed types, virtual threads and structured concurrency than stay compatible with older runtimes.

[中文](README.md) · [Docs](docs/index-en.md) · [Modules & API](docs/modules.md) · [Examples](sure-examples) · [Contributing](CONTRIBUTING.md) · [Migrating from Hutool](docs/migration-from-hutool.en.md)

## 30-second tour

`sure-core` has no compile dependencies. Add this to a Maven project:

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
    <version>1.20.0</version>
</dependency>
```

Save it as `src/main/java/Demo.java` and run the `main` method in your IDE (or `mvn -q compile` to verify it builds):

```java
import com.sure.tool.codec.HashUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.DesensitizedUtil;
import com.sure.tool.util.StrUtil;
import com.sure.tool.util.ValidatorUtil;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        System.out.println(StrUtil.format("Hello, {}!", "suretool"));            // Hello, suretool!
        System.out.println(StrUtil.toCamelCase("user_name"));                    // userName
        System.out.println(DesensitizedUtil.mobilePhone("13812345678"));         // 138****5678
        System.out.println(ValidatorUtil.isEmail("dev@example.com"));            // true
        System.out.println(DateUtil.formatDate(DateUtil.parse("2000-05-20")));   // 2000-05-20
        System.out.println(CollUtil.union(List.of(1, 2), List.of(2, 3)));         // [1, 2, 3]
        System.out.println(HashUtil.sha256Hex("abc"));                           // 64 hex chars
    }
}
```

Output:

```text
Hello, suretool!
userName
138****5678
true
2000-05-20
[1, 2, 3]
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
```

## Why suretool

1. **No third-party runtime dependencies in the core.** `sure-core` compiles against the JDK only. Heavy libraries such as POI, PDFBox and Mail live in separate modules, so they never reach your classpath unless you ask for them.
2. **Coverage is a hard gate.** `sure-core` has a 0.98 line-coverage floor and currently measures 98.23% (10,227 of 10,411 lines). Every code module is gated at 0.98 (0.97 for `sure-socket`). Below the threshold, `mvn verify` fails.
3. **Three-OS CI.** Ubuntu, macOS and Windows run the same `mvn -B verify` on JDK 25. Today that means 290 test classes and 2,419 `@Test` methods; one full run executed 2,419 tests with zero failures (the benchmark and examples modules carry no tests).
4. **23 JPMS modules.** 23 of the 29 child modules ship a `module-info.java` and can be used on the module path.
5. **44 property tests.** Five jqwik test classes check round-trips and invariants across codecs, collections, strings and dates — not just hand-picked examples.

## Modules

| Module | Responsibility | Key dependencies |
| --- | --- | --- |
| `sure-core` | Strings / collections / dates / IO / reflection / concurrency / graphs / statistics | JDK only |
| `sure-concurrent` | JDK 25-only capabilities: Scoped Values / Class-File API | JDK only |
| `sure-log` | Logging facade: delegates to SLF4J when present, falls back to console | core |
| `sure-db` | JDBC access: Entity / SqlRunner / pool / transactions / paging | core |
| `sure-script` | JSR-223 script facade (engine probing / compiled cache) | core |
| `sure-template` | Template engine (Template / SimpleTemplate / bean rendering) | core |
| `sure-aop` | Zero-dependency AOP (Aspect / ProxyUtil / method matching) | core |
| `sure-pdf` | PDF read/write facade | core + PDFBox 3 (optional) |
| `sure-process` | Child processes, timeout kill | core |
| `sure-math` | Math, BigDecimal, statistics, random numbers | core |
| `sure-compress` | 7z / LZMA2 / Brotli compression facade | core + commons-compress (optional) |
| `sure-socket` | TCP client / server | core |
| `sure-event` | Event bus (synchronous / virtual-thread async) | core |
| `sure-json` | JSON parsing / serialization / bean mapping / JSONPath | core |
| `sure-http` | HTTP client (cookies / proxy / pool / multipart) | core |
| `sure-crypto` | Hash / AES-GCM / RSA-OAEP / HKDF / ML-KEM / ECDH | core |
| `sure-cron` | Cron expressions and scheduler | core |
| `sure-cache` | FIFO / LRU / LFU / Timed / Weak caches | core |
| `sure-xml` | XML to Map / bean conversion | core |
| `sure-poi` | Excel / Word read and write | core + POI (optional) |
| `sure-extra` | Mail / FTP / QR codes / image processing | core + jakarta.mail + commons-net + zxing (optional) |
| `sure-captcha` | Graphic captcha (line / circle / shear) | JDK only |
| `sure-jwt` | JWT signing and verification (HS / RS) | core + crypto + json |
| `sure-dfa` | Sensitive-word filtering (prefix tree / stop words) | JDK only |
| `sure-bom` | BOM for unified module versions | — |
| `sure-all` | Aggregator: every module in one dependency | all modules |
| `sure-examples` | Runnable demos (20 examples) | sure-all |
| `sure-benchmark` | JMH benchmarks | core + json + math + event |
| `sure-spring-boot-starter` | Spring Boot auto-configuration | core + captcha (event / cron / cache optional) |

Full coordinates, packages and javadoc links: [Modules & API portal](docs/modules.md).

## Install and versions

### BOM (recommended)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.tasure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>1.20.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

After that, any module can be declared without a version:

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-json</artifactId>
</dependency>
```

### Spring Boot starter

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-spring-boot-starter</artifactId>
    <version>1.20.0</version>
</dependency>
```

The starter currently brings in `sure-core` and `sure-captcha` only. `sure-event`, `sure-cron` and `sure-cache` are optional: add the module you need and the matching auto-configuration activates via `@ConditionalOnClass`. The starter itself is built against Spring Boot 4.1.2.

Heavy third-party libraries do not travel with `sure-all`: when you use PDF, Excel, mail or 7z features, declare the library yourself (for example `org.apache.poi:poi-ooxml`).

### Versions

23 versions are published to Maven Central. The latest is `1.20.0` (2026-11-05), and all modules share the same version number.

## Quality and engineering evidence

| Area | Fact | Where to check |
| --- | --- | --- |
| Test suite | 290 test classes / 2,419 static `@Test` methods; the full `mvn -B verify` run on 2026-11-05 executed 2,419 tests with 0 failures, 0 errors and 0 skipped (`sure-core`: 1,430) | CI logs |
| Coverage | `sure-core` line coverage 98.23% (10,227/10,411), gate 0.98; every code module gated at 0.98 (0.97 for `sure-socket`) | `sure-core/pom.xml` · JaCoCo |
| CI matrix | Ubuntu / macOS / Windows × JDK 25, all running `mvn -B verify` | `.github/workflows/ci.yml` |
| Static analysis | Checkstyle fails on violations; SpotBugs at effort=Max with failOnError; CodeQL; OSV Scanner | `pom.xml` · workflows |
| Binary compatibility | 27 of the 28 jar modules run japicmp against 1.19.1 at verify (`sure-concurrent` ships for the first time and joins the baseline next release), with `breakBuildOnBinaryIncompatibleModifications=true` | root `pom.xml` |
| License headers | license-maven-plugin `check` bound to the verify phase | `pom.xml` |
| SBOM | CycloneDX `makeAggregateBom` generated during `verify` under `-Prelease` | `pom.xml` |
| Docs links | The `docs-links` job validates relative links and anchors in README and docs | `.github/scripts/check_docs_links.py` |

## Boundaries and limitations

- **JDK 25+ only.** It will not run on JDK 17 or 21, and there is no downgraded build.
- **Heavy libraries are opt-in.** `sure-all` aggregates only suretool modules. POI, PDFBox, Mail, commons-net, Zxing and commons-compress are marked `optional` in their own modules and are no longer pulled through `sure-all`. Declare them yourself when you use those domains.
- **Cross-platform stability is still being watched.** Some tests used to be flaky on Windows and macOS. They were fixed by additional coverage (all three platforms green on 2026-10-30), but the strict three-platform gate stays on.
- **Not a drop-in Hutool replacement.** The project covers common use cases only; we do not add APIs just to match Hutool's method count.

## Docs / examples / contributing

- Docs: [Class index](docs/index.md) · [Modules & API](docs/modules.md) · [5-minute quickstart](docs/posts/quickstart.md) · [Comparison with Hutool](docs/comparison-hutool.md)
- Examples: [sure-examples](sure-examples) — 20 runnable demos, including `FileTailer` log rotation and `WatchDir` directory events. Run with `mvn -pl sure-examples -am install -DskipTests`, then `mvn -pl sure-examples exec:java`. For Spring Boot wiring, see [sure-spring-boot-demo](sure-spring-boot-demo)
- Contributing: [CONTRIBUTING.md](CONTRIBUTING.md) · [Bug template](.github/ISSUE_TEMPLATE/bug_report.md) · [Feature request template](.github/ISSUE_TEMPLATE/feature_request.md) · [PR template](.github/PULL_REQUEST_TEMPLATE.md)
- Security: [SECURITY.md](SECURITY.md) — report vulnerabilities through GitHub Security Advisories
- License: [Apache License 2.0](LICENSE)
