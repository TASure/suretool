# Changelog

本项目所有值得记录的变更都会收录在本文件中。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- **模块化架构（P2）**：拆分为 14 个 Maven 模块，通过 `sure-all` 聚合、`sure-bom` 统一版本管理：
  `sure-core` / `sure-json` / `sure-http` / `sure-crypto` / `sure-cron` / `sure-cache` / `sure-xml` /
  `sure-poi` / `sure-captcha` / `sure-jwt` / `sure-dfa` / `sure-benchmark` / `sure-bom` / `sure-all`。
- **八大高频工具域（P1）**：字符串/集合/日期/IO/加密/JSON/HTTP/缓存等工具类，包前缀 `com.sure.tool`。
- **安全增强域（P3）**：
  - `sure-captcha`：验证码生成与校验；
  - `sure-jwt`：JWT 签发与解析；
  - `sure-http`：HTTP 客户端封装（含连接超时与 TLS 配置）；
  - `sure-dfa`：敏感词过滤（DFA 算法）。
- **加密工具默认安全基线**：`AesUtil` 采用 AES/GCM/NoPadding 认证加密（随机 IV + SHA-256 密钥派生）；
  `RsaUtil` 采用 RSA/OAEP-SHA256，最小密钥长度 2048 位；`DesUtil` 标记 `@Deprecated` 仅供存量数据解密。
- **工程化质量门禁**：Checkstyle（0 违规）、SpotBugs（Max-Medium）、JaCoCo 行覆盖率下限 70%、
  License 头检查、JUnit 4 测试全覆盖。
- **CI 双版本矩阵**：GitHub Actions 在 JDK 21 与 JDK 25 上构建，覆盖率仅统计 JDK 21。
- **JDK 21+ 基线**：`maven.compiler.release=21`，移除低版本兼容 profile。
- **CodeQL 安全扫描**：启用默认分析，历史告警全部清零（15 Closed / 0 Open）。
- **Dependabot**：依赖自动升级（log4j-core 已升级至 2.25.4）。

### Changed

- `maven.compiler.release` 固定为 21，仅支持 JDK 21 及以上版本。

### Security

- 修复 `OrderedMap` / `CaseInsensitiveMap` 在 JDK 模块化环境下的 `Map.Entry` 可访问性问题。
- 消除全部 CodeQL 告警（含 SQL 注入、路径遍历、弱加密、信息泄露类）。
- `SECURITY.md` 明确安全响应承诺（24h 确认 / 72h 修复评估 / 14 天发布窗口）。

## [0.1.0-SNAPSHOT] - 未发布

首个可运行快照，包含上述全部模块与工具类。
