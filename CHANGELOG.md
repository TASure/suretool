# Changelog

本项目所有值得记录的变更都会收录在本文件中。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- **模块化架构（P2）**：拆分为 15 个 Maven 模块，通过 `sure-all` 聚合、`sure-bom` 统一版本管理；
  `sure-core` / `sure-json` / `sure-http` / `sure-crypto` / `sure-cron` / `sure-cache` / `sure-xml` /
  `sure-poi` / `sure-captcha` / `sure-jwt` / `sure-dfa` / `sure-benchmark` / `sure-examples` /
  `sure-spring-boot-starter` / `sure-bom` / `sure-all`。
- **八大高频工具域（P1）**：字符串/集合/日期/IO/加密/JSON/HTTP/缓存等工具类，包前缀 `com.sure.tool`。
- **安全增强域（P3）**：
  - `sure-captcha`：验证码生成与校验；
  - `sure-jwt`：JWT 签发与解析（HS/RS）；
  - `sure-http`：HTTP 客户端封装（连接超时与 TLS 配置）；
  - `sure-dfa`：敏感词过滤（DFA 算法）。
- **加密工具默认安全基线**：`AesUtil` 采用 AES/GCM/NoPadding 认证加密（随机 IV + SHA-256 密钥派生）；
  `RsaUtil` 采用 RSA/OAEP-SHA256，最小密钥长度 2048 位；`DesUtil` 标记 `@Deprecated` 仅供存量数据解密。
- **工程化质量门禁**：Checkstyle（0 违规）、SpotBugs（Max-Medium）、JaCoCo 行覆盖率下限、
  License 头检查、JUnit 4 测试全覆盖。
- **全模块覆盖率 ≥ 85%**：core 85.3% / json 89.8% / cache 94.5% / crypto 87.4% / xml 93.6% /
  captcha 93.9% / cron 93.3% / dfa 91.4% / http 88.2% / jwt 92.8% / poi 88.5%。
- **CI 双版本矩阵**：GitHub Actions 在 JDK 21 与 JDK 25 上构建，覆盖率仅统计 JDK 21。
- **JDK 21+ 基线**：`maven.compiler.release=21`，移除低版本兼容 profile。
- **CodeQL 安全扫描**：新增 `codeql.yml`（push / PR / 每周定时，`security-and-quality` 查询集），历史告警清零。
- **Dependabot**：Maven 与 GitHub Actions 依赖自动升级（patch/minor 分组 PR，log4j-core 已升级至 2.25.4）。
- **发布工程化**：`-Prelease` profile（CycloneDX SBOM / GPG 签名 / sources / javadoc / OSSRH staging）+
  `distributionManagement`；`docs/RELEASING.md` 发布指南；`scripts/release.ps1` 发布辅助脚本。
- **可运行示例**：`sure-examples` 模块 14 个 Demo + `ExamplesRunner` 聚合入口（实测可运行）。
- **Spring Boot Starter**：`sure-spring-boot-starter` 自动装配骨架（`suretool.*` 配置前缀）。
- **文档站**：`docs/index.md` 类索引 + GitHub Pages 发布工作流（`pages.yml`）；
  Hutool 对比文档、维护手册、技术文章与 awesome-java 提交材料。
- **社区规范**：`CODE_OF_CONDUCT.md`（Contributor Covenant 2.1）、中英文双语主页（`README.en.md`）、
  issue / PR 模板、`SECURITY.md` 漏洞应急响应 SOP。

### Changed

- `maven.compiler.release` 固定为 21，仅支持 JDK 21 及以上版本；
- 各业务模块 JaCoCo 行覆盖率下限上调至 85%（测试代码同步补强）。

### Fixed

- 修复 `OrderedMap` / `CaseInsensitiveMap` 在 JDK 模块化环境下的 `Map.Entry` 可访问性问题；
- 修复 `AesUtil` / `DesUtil` 缺少 `com.sure.tool.codec.HexUtil` import 导致的编译失败；
- 修复 javadoc 内嵌标签中未转义花括号（`{@code {}}` 等 6 处）导致的 release 阶段
  `javadoc:jar` doclint 构建失败；
- 消除全部 CodeQL 告警（SQL 注入、路径遍历、弱加密、信息泄露类）；
- `ci.yml` JaCoCo 上传补全 `sure-captcha` / `sure-jwt` / `sure-dfa` 三个模块。

### Security

- `SECURITY.md` 明确安全响应承诺（24h 确认 / 72h 修复评估 / 14 天发布窗口）与依赖漏洞应急 SOP；
- 依赖治理：Dependabot 自动监控，安全版本统一收口至根 `pom.xml` 的 `dependencyManagement`；
  测试日志后端使用已修复 log4j 2.25.4+；
- 零运行时依赖：核心域（`sure-core`）零第三方运行期依赖，攻击面最小化。

## [0.1.0-SNAPSHOT] - 未发布

首个可运行快照，包含上述全部模块与工具类。
