# suretool

[![CI](https://img.shields.io/github/actions/workflow/status/TASure/suretool/ci.yml?branch=main&label=CI)](https://github.com/TASure/suretool/actions)
[![Coverage](https://img.shields.io/codecov/c/github/TASure/suretool)](https://codecov.io/gh/TASure/suretool)
[![Release](https://img.shields.io/github/v/release/TASure/suretool)](https://github.com/TASure/suretool/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21+-blue)](pom.xml)
[![Stars](https://img.shields.io/github/stars/TASure/suretool)](https://github.com/TASure/suretool)

[中文文档](README.md) | English

A **small but complete** Java utility library, inspired by the design philosophy of
[Hutool](https://doc.hutool.cn/pages/index/). It wraps common JDK APIs into
static utility methods to reduce boilerplate and lower development cost.

- Package prefix: `com.sure.tool`
- Language level: **Java 21+** (zero third-party runtime dependencies in the core domain; Office domain is built on Apache POI 5.5.1)
- License: Apache License 2.0
- Docs: [Class index](docs/index.md) · [Hutool comparison](docs/comparison-hutool.md) · [Releasing](docs/RELEASING.md) · [Maintaining](docs/MAINTAINING.md) · [GitHub setup](docs/github-setup.md)

## Why suretool?

- **Secure by default** — AES-GCM authenticated encryption, RSA-OAEP-SHA256, minimum 2048-bit RSA keys, deprecated DES kept only for decrypting legacy data.
- **Small but complete** — 11 business modules + BOM + examples + Spring Boot starter; import only what you need.
- **Quality-gated** — Checkstyle 0 violations, SpotBugs (Max/Medium), JaCoCo line coverage ≥ 85% on business modules, CodeQL + Dependabot enabled in CI.
- **Modern Java** — built with `--release 21`; records, sealed classes, pattern matching and virtual threads are welcome in this codebase.

## Quick start

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.sure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>0.1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-all</artifactId>
</dependency>
```

Or import individual modules (lighter):

```xml
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-core</artifactId>
</dependency>
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-json</artifactId>
</dependency>
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-crypto</artifactId>
</dependency>
```

### Examples

```java
String json = JSONUtil.toJsonStr(Map.of("name", "Alice", "age", 30));
String md5 = SecureUtil.md5("hello");
String token = JwtUtil.createToken(Map.of("uid", 1001), "secret", 3600);
String text = StrUtil.removeSuffixIgnoreCase("Hello.txt", ".TXT"); // "Hello"
```

Run all runnable demos:

```bash
# 首次运行先安装依赖模块到本地仓库，再单独运行 examples（避免 -am 在父项目执行 exec 目标）
mvn -pl sure-examples -am install -DskipTests
mvn -pl sure-examples exec:java
```

## Modules

| Module | Coordinates | Capabilities | Runtime deps |
| --- | --- | --- | --- |
| `sure-core` | `com.sure:sure-core` | util/codec/collection/date/io/lang/bean/thread core domain | none |
| `sure-json` | `com.sure:sure-json` | JSON parse/serialize/Bean conversion | core |
| `sure-http` | `com.sure:sure-http` | HTTP client / URL utilities | core |
| `sure-crypto` | `com.sure:sure-crypto` | Hash / AES / DES / RSA / HMAC | core |
| `sure-cron` | `com.sure:sure-cron` | Cron expressions / scheduler | core |
| `sure-cache` | `com.sure:sure-cache` | FIFO / LRU / LFU / Timed caches | core |
| `sure-xml` | `com.sure:sure-xml` | XML to Map/Bean conversion | core |
| `sure-poi` | `com.sure:sure-poi` | Excel / Word read & write | core + POI |
| `sure-captcha` | `com.sure:sure-captcha` | Graphic captcha (line/circle/distortion) | none |
| `sure-jwt` | `com.sure:sure-jwt` | JWT issue/verify (HS/RS) | core + crypto + json |
| `sure-dfa` | `com.sure:sure-dfa` | Sensitive-word filtering (prefix tree / stop words) | none |
| `sure-bom` | `com.sure:sure-bom` | BOM for unified version management | — |
| `sure-all` | `com.sure:sure-all` | Aggregated module (everything) | all |
| `sure-examples` | `com.sure:sure-examples` | Runnable demos (14 examples) | sure-all |
| `sure-spring-boot-starter` | `com.sure:sure-spring-boot-starter` | Spring Boot auto-configuration entry | sure-all + spring-boot |

Full class index: [docs/index.md](docs/index.md).

## Roadmap

- [x] P0/P1: core utilities of 8 high-frequency domains
- [x] P2: modularization (15 modules) + engineering gates (Checkstyle/SpotBugs/JaCoCo/License)
- [x] P3: captcha / jwt / http / dfa + sure-bom
- [x] JDK 21+ baseline
- [x] Open-source excellence: coverage ≥ 85%, docs site, examples, starter, release pipeline
- [ ] 0.1.0 release on Maven Central
- [ ] Spring Boot starter expansion (cache/crypto auto-configuration)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). All PRs must pass: Checkstyle 0 violations, SpotBugs, JaCoCo ≥ 85% (business modules), license headers.

- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) (Contributor Covenant 2.1)
- [SECURITY.md](SECURITY.md) (report a vulnerability; 24h acknowledgment, 72h assessment)
- Security scanning: CodeQL on push/PR/weekly + Dependabot auto-updates

## License

[Apache License 2.0](LICENSE). Design and API naming reference [Hutool](https://gitee.com/chinabugotech/hutool) (also Apache-2.0); implementation ideas are credited in class-level Javadoc where applicable.
