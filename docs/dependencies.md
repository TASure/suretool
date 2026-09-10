# 第三方依赖清单

> 维护说明：每次依赖变更后更新本表。目标——**运行期零依赖原则**：
> 仅 `sure-poi`（Office 域）带第三方运行期依赖，其余模块运行期只依赖 JDK 21+。

## 1. 运行期依赖（进入制品 classpath）

| 模块 | 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- | --- |
| `sure-poi` | `org.apache.poi:poi-ooxml-full` | 5.5.1 | Apache-2.0 | Excel / Word 读写 |
| `sure-poi`（传递） | `commons-codec:commons-codec` | 1.16.1 | Apache-2.0 | POI 内部编码 |
| `sure-poi`（传递） | `org.apache.commons:commons-collections4` | 4.4 | Apache-2.0 | POI 集合支持 |
| `sure-poi`（传递） | `org.apache.commons:commons-compress` | 1.26.0 | Apache-2.0 | OOXML 压缩包 |
| `sure-poi`（传递） | `org.apache.logging.log4j:log4j-api` | 2.23.1 | Apache-2.0 | POI 日志门面（无强制后端） |

> 其余全部模块（core/json/http/crypto/cron/cache/xml/captcha/jwt/dfa）运行期零第三方依赖。

## 2. 测试期依赖（不进入制品）

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `junit:junit` | 4.13.2 | EPL-1.0 | 单元测试 |
| `org.apache.logging.log4j:log4j-core` | 2.25.4 | Apache-2.0 | 测试日志后端（已修复版本） |

## 3. 基准测试依赖（sure-benchmark，不进入制品）

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `org.openjdk.jmh:jmh-core` | 1.37 | GPLv2+CE | JMH 基准框架 |
| `cn.hutool:hutool-all` | 5.8.32 | Apache-2.0 | 性能对比基线（仅基准，不引入制品） |

## 4. Spring Boot Starter（可选模块）

| 依赖 | 版本 | 许可证 | 用途 |
| --- | --- | --- | --- |
| `org.springframework.boot:spring-boot-autoconfigure` | 3.3.5 | Apache-2.0 | 自动装配（由使用方应用自身引入 spring-boot 运行时） |
| `org.springframework.boot:spring-boot-configuration-processor` | 3.3.5 | Apache-2.0 | 配置元数据生成（optional） |

## 5. 构建期插件（不进入制品）

| 插件 | 版本 | 用途 |
| --- | --- | --- |
| maven-checkstyle-plugin | 3.3.1 | 代码规范 |
| spotbugs-maven-plugin | 4.8.x | 静态缺陷扫描 |
| jacoco-maven-plugin | 0.8.12 | 覆盖率门禁 |
| license-maven-plugin | 4.x | License 头校验 |
| cyclonedx-maven-plugin | 2.8.1 | SBOM 生成（release profile） |
| maven-gpg-plugin | 3.2.4 | 制品签名（release profile） |
| maven-javadoc-plugin | 3.6.3 | javadoc.jar（release profile） |
| maven-source-plugin | 3.3.1 | sources.jar（release profile） |
| nexus-staging-maven-plugin | 1.6.13 | OSSRH 发布（release profile） |

## 许可证合规核对

- 所有运行期/测试期依赖均为 Apache-2.0 或 EPL-1.0（兼容 Apache-2.0 分发）；
- JMH 为 GPLv2+Classpath Exception，仅用于基准模块且**不随制品分发**（sure-benchmark 不发布）；
- 完整三方声明见仓库根目录 [NOTICE](../NOTICE)。
