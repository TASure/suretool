# suretool

[![CI](https://img.shields.io/github/actions/workflow/status/TASure/suretool/ci.yml?branch=main&label=CI)](https://github.com/TASure/suretool/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.tasure/sure-core?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.tasure/sure-core)
[![Coverage](https://img.shields.io/endpoint?url=https://tasure.github.io/suretool/coverage/badge.json)](https://codecov.io/gh/TASure/suretool)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-25%2B-blue)](https://openjdk.org/projects/jdk/25/)

面向 **JDK 25+** 的零依赖 Java 工具包。29 个模块把字符串、集合、日期、IO、并发、JSON、HTTP、加解密等高频操作收敛为静态方法；核心域只依赖 JDK，按需引入一个模块即可。

**如果你的项目在 JDK 17/21，本项目不适用**——请继续用 [Hutool](https://hutool.cn/) 或 [Apache Commons](https://commons.apache.org/)。本项目所有模块以 `--release 25` 编译，不提供低版本构建——不为兼容旧 JDK 而放弃 record、sealed、虚拟线程和结构化并发。

[English](README.en.md) · [文档](docs/index.md) · [模块与 API](docs/modules.md) · [示例](sure-examples) · [贡献](CONTRIBUTING.md)

## 30 秒上手

`sure-core` 的编译依赖为空。新建 Maven 工程，加入：

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
    <version>1.19.1</version>
</dependency>
```

把下面代码保存为 `src/main/java/Demo.java`，在 IDE 中运行 `main` 方法（或 `mvn -q compile` 验证编译）：

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
        System.out.println(HashUtil.sha256Hex("abc"));                           // 64 位十六进制
    }
}
```

输出：

```text
Hello, suretool!
userName
138****5678
true
2000-05-20
[1, 2, 3]
ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
```

## 为什么是它

1. **核心域零第三方运行期依赖** —— `sure-core` 的 compile 作用域只有 JDK；POI、PDFBox、Mail 等重量级依赖隔离在独立模块，不引入就不会进你的 classpath。
2. **覆盖率是硬门禁** —— `sure-core` 行覆盖下限 0.98，实测 98.23%（10227/10411 行）；各代码模块门禁 0.98（`sure-socket` 0.97），低于阈值 `mvn verify` 直接失败。
3. **三平台 CI** —— Ubuntu / macOS / Windows × JDK 25 跑同一套 `mvn -B verify`；当前 302 个测试类、2404 个 `@Test`，单次执行 2399 例、0 失败。
4. **23 个 JPMS 模块** —— 29 个子模块中有 23 个提供 `module-info.java`，可直接用于 module path。
5. **44 个属性测试** —— 5 个测试类用 jqwik `@Property` 覆盖编解码、集合、字符串、日期的往返与不变量，而不只是手工挑的样例。

## 模块速览

| 模块 | 职责 | 关键依赖 |
| --- | --- | --- |
| `sure-core` | 字符串 / 集合 / 日期 / IO / 反射 / 并发 / 图 / 统计 | 仅 JDK |
| `sure-concurrent` | JDK 25 差异化能力：Scoped Values / Class-File API | 仅 JDK |
| `sure-log` | 日志门面：有 SLF4J 时委托，无则 Console 兜底 | core |
| `sure-db` | JDBC 访问：Entity / SqlRunner / 连接池 / 事务 / 分页 | core |
| `sure-script` | JSR-223 脚本门面（引擎探测 / 编译缓存） | core |
| `sure-template` | 模板引擎（Template / SimpleTemplate / Bean 渲染） | core |
| `sure-aop` | 零依赖 AOP（Aspect / ProxyUtil / 方法匹配） | core |
| `sure-pdf` | PDF 读写门面 | core + PDFBox 3（可选） |
| `sure-process` | 子进程调用、超时强杀 | core |
| `sure-math` | 数学、BigDecimal、统计、随机数 | core |
| `sure-compress` | 7z / LZMA2 / Brotli 压缩门面 | core + commons-compress（可选） |
| `sure-socket` | TCP 客户端 / 服务端 | core |
| `sure-event` | 事件总线（同步 / 虚拟线程异步） | core |
| `sure-json` | JSON 解析 / 序列化 / Bean 互转 / JSONPath | core |
| `sure-http` | HTTP 客户端（Cookie / 代理 / 连接池 / multipart） | core |
| `sure-crypto` | 哈希 / AES-GCM / RSA-OAEP / HKDF / ML-KEM / ECDH | core |
| `sure-cron` | Cron 表达式与调度器 | core |
| `sure-cache` | FIFO / LRU / LFU / Timed / Weak 缓存 | core |
| `sure-xml` | XML 与 Map / Bean 互转 | core |
| `sure-poi` | Excel / Word 读写 | core + POI（可选） |
| `sure-extra` | 邮件 / FTP / 二维码 / 图像处理 | core + jakarta.mail + commons-net + zxing（可选） |
| `sure-captcha` | 图形验证码（线段 / 圆圈 / 扭曲） | 仅 JDK |
| `sure-jwt` | JWT 签发与校验（HS / RS） | core + crypto + json |
| `sure-dfa` | 敏感词过滤（前缀树 / 停用词） | 仅 JDK |
| `sure-bom` | BOM，统一全部模块版本 | — |
| `sure-all` | 聚合模块，一次引入全部能力 | 全部模块 |
| `sure-examples` | 可运行示例（20 个 Demo） | sure-all |
| `sure-benchmark` | JMH 基准测试 | core + json + math + event |
| `sure-spring-boot-starter` | Spring Boot 自动装配 | core + captcha（event / cron / cache 可选） |

完整坐标、包名与 javadoc 链接见 [模块与 API 门户](docs/modules.md)。

## 安装与版本

### BOM（推荐）

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.tasure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>1.19.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

之后引入任意模块都不用再写版本：

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-json</artifactId>
</dependency>
```

### Spring Boot Starter

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-spring-boot-starter</artifactId>
    <version>1.19.1</version>
</dependency>
```

Starter 当前只自带 `sure-core` + `sure-captcha`（必选）。`sure-event` / `sure-cron` / `sure-cache` 是 optional 依赖：需要时显式引入对应模块，自动装配会按 `@ConditionalOnClass` 生效。Starter 本身以 Spring Boot 4.1.2 编译。

第三方重依赖不随 `sure-all` 传递：用到 PDF / Excel / 邮件 / 7z 等能力时，按模块表自行引入对应库（如 `org.apache.poi:poi-ooxml`）。

### 版本

Maven Central 已发布 22 个版本，最新 `1.19.1`（2026-11-03）；所有模块使用同一版本号发布。

## 质量与工程证据

| 项目 | 事实 | 验证位置 |
| --- | --- | --- |
| 测试规模 | 302 个测试类 / 2404 个静态 `@Test`；单次 `mvn -B verify` 执行 2399 例，0 失败 | CI 日志 |
| 覆盖率 | `sure-core` 行覆盖 98.23%（10227/10411），门禁 0.98；各代码模块门禁 0.98（`sure-socket` 0.97） | `sure-core/pom.xml` · JaCoCo |
| CI 矩阵 | Ubuntu / macOS / Windows × JDK 25，统一执行 `mvn -B verify` | `.github/workflows/ci.yml` |
| 静态检查 | Checkstyle 违规即失败；SpotBugs effort=Max 且 failOnError；CodeQL；OSV Scanner | `pom.xml` · workflows |
| 二进制兼容 | 28 个 jar 模块中 27 个在 verify 阶段用 japicmp 对比 1.19.1（`sure-concurrent` 首次发布，下个版本纳入基线）；`breakBuildOnBinaryIncompatibleModifications=true` | 根 `pom.xml` |
| 许可头 | license-maven-plugin `check` 绑定在 verify 阶段 | `pom.xml` |
| SBOM | CycloneDX `makeAggregateBom` 在 `-Prelease` 的 verify 阶段生成 | `pom.xml` |
| 文档链接 | `docs-links` 任务校验 README 与 docs 的相对链接和锚点 | `.github/scripts/check_docs_links.py` |

## 边界与限制

- **仅支持 JDK 25+。** 在 JDK 17/21 上不能运行，也没有降级构建。
- **重依赖需显式引入。** `sure-all` 只聚合 suretool 模块；POI、PDFBox、Mail、commons-net、Zxing、commons-compress 等第三方库在对应模块中标为 `optional`，不会被 `sure-all` 传递。使用这些域时需自行声明对应依赖。
- **跨平台稳定性仍在观察。** Windows / macOS 上曾有平台相关用例不稳定，已通过补测修复（2026-10-30 三平台覆盖率全绿），CI 仍保持三平台严格门禁。
- **不是 Hutool 的替代品。** 本项目只覆盖常用场景，不为对齐 Hutool 的方法数量而扩充 API。

## 文档 / 示例 / 贡献

- 文档：[类索引](docs/index.md) · [模块与 API](docs/modules.md) · [5 分钟上手](docs/posts/quickstart.md) · [与 Hutool 对比](docs/comparison-hutool.md)
- 示例：[sure-examples](sure-examples)（20 个可运行 Demo，含 `FileTailer` 轮转与 `WatchDir` 目录监听），运行方式：`mvn -pl sure-examples -am install -DskipTests` 后 `mvn -pl sure-examples exec:java`；Spring Boot 装配示例见 [sure-spring-boot-demo](sure-spring-boot-demo)
- 贡献：[CONTRIBUTING.md](CONTRIBUTING.md) · [Bug 模板](.github/ISSUE_TEMPLATE/bug_report.md) · [功能请求模板](.github/ISSUE_TEMPLATE/feature_request.md) · [PR 模板](.github/PULL_REQUEST_TEMPLATE.md)
- 安全：[SECURITY.md](SECURITY.md)，漏洞通过 GitHub Security Advisory 报告
- 许可：[Apache License 2.0](LICENSE)
