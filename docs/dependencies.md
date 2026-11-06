# 第三方依赖清单

> 维护说明：每次依赖变更后更新本表。
> 自 1.20.0 起，所有第三方运行期依赖均标记为 `optional`：`sure-all` 只聚合 suretool 模块，**不会把第三方 jar 传递进使用方 classpath**。使用对应域时需显式声明依赖。

## 1. 可选运行期依赖

| 模块 | 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- | --- |
| `sure-poi` | `org.apache.poi:poi-ooxml-full` | 6.0.0 | Apache-2.0 | Excel / Word 完整 schema |
| `sure-poi` | `org.apache.poi:poi-ooxml` | 6.0.0 | Apache-2.0 | XSSF / XWPF 实现 |
| `sure-pdf` | `org.apache.pdfbox:pdfbox` | 3.0.8 | Apache-2.0 | PDF 读写 |
| `sure-extra` | `jakarta.mail:jakarta.mail-api` | 2.1.5 | EPL-2.0 / GPL-2.0+CE | 邮件 API |
| `sure-extra` | `org.eclipse.angus:angus-mail` | 2.0.5 | EPL-2.0 / GPL-2.0+CE | 邮件实现 |
| `sure-extra` | `commons-net:commons-net` | 3.13.0 | Apache-2.0 | FTP |
| `sure-extra` | `com.google.zxing:core` | 3.5.4 | Apache-2.0 | 二维码编码 |
| `sure-extra` | `com.google.zxing:javase` | 3.5.4 | Apache-2.0 | 二维码图像输出 |
| `sure-compress` | `org.apache.commons:commons-compress` | 1.28.0 | Apache-2.0 | 7z / LZMA2 |
| `sure-compress` | `org.tukaani:xz` | 1.12 | 0BSD | XZ 解码 |
| `sure-compress` | `org.brotli:dec` | 0.1.2 | MIT | Brotli 解码 |

以上依赖的传递依赖（如 POI 的 xmlbeans、log4j-api，PDFBox 的 fontbox）同样不会通过 `sure-all` 传递。

## 2. 其余模块

`sure-core` / `sure-log` / `sure-db` / `sure-script` / `sure-template` / `sure-aop` / `sure-process` / `sure-math` / `sure-socket` / `sure-event` / `sure-json` / `sure-http` / `sure-crypto` / `sure-cron` / `sure-cache` / `sure-xml` / `sure-captcha` / `sure-jwt` / `sure-dfa` 在运行期只依赖 JDK 25+。

## 3. 测试期依赖（不进入制品）

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `junit:junit` | 4.13.2 | EPL-1.0 | 单元测试 |
| `net.jqwik:jqwik` | 1.10.2 | EPL-2.0 | 属性测试 |
| `org.apache.logging.log4j:log4j-core` | 2.26.1 | Apache-2.0 | 测试日志后端（安全基线） |

## 4. 基准测试依赖（sure-benchmark，不进入制品）

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `org.openjdk.jmh:jmh-core` | 1.37 | GPLv2+CE | JMH 基准框架 |
| `cn.hutool:hutool-core` / `hutool-json` | 5.8.48 | Apache-2.0 | 性能对比基线（仅基准） |

## 5. Spring Boot Starter

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `org.springframework.boot:spring-boot-autoconfigure` | 4.1.2 | Apache-2.0 | 自动装配（由使用方应用引入运行时） |
| `org.springframework.boot:spring-boot-configuration-processor` | 4.1.2 | Apache-2.0 | 配置元数据（optional） |

## 6. 构建期插件（不进入制品）

| 插件 | 版本 | 用途 |
| --- | --- | --- |
| maven-checkstyle-plugin | 3.3.1 | 代码规范 |
| spotbugs-maven-plugin | 4.10.x | 静态缺陷扫描 |
| jacoco-maven-plugin | 0.8.15 | 覆盖率门禁 |
| license-maven-plugin | 4.x | License 头校验 |
| cyclonedx-maven-plugin | 2.9.3 | SBOM 生成（release profile） |
| maven-gpg-plugin | 3.2.4 | 制品签名（release profile） |
| maven-javadoc-plugin | 3.6.3 | javadoc.jar（release profile） |
| maven-source-plugin | 3.3.1 | sources.jar（release profile） |
| nexus-staging-maven-plugin | 1.7.0 | OSSRH 发布（release profile） |

## 许可证合规核对

- 运行期可选依赖均为 Apache-2.0 / EPL / MIT / 0BSD，兼容 Apache-2.0 分发；
- JMH 为 GPLv2+Classpath Exception，仅用于基准模块且**不随制品分发**（sure-benchmark 不发布）；
- 完整三方声明见仓库根目录 [NOTICE](../NOTICE)。
