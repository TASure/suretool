# Release Notes — suretool 0.1.0

> 发布时复制到 GitHub Releases（正文），标题建议：`suretool 0.1.0 — 小而全、默认安全的 Java 工具库（JDK 21+）`
> 完整变更见 [CHANGELOG.md](../CHANGELOG.md)。

## 中文摘要

**suretool 0.1.0** — 参考 Hutool 设计理念、专为 JDK 21+ 打造的"小而全"Java 工具库。

### ✨ 亮点
- **11 个业务模块 + BOM + 示例 + Spring Boot Starter**：按需引入，`sure-bom` 统一版本；
- **默认安全**：AES-GCM 认证加密、RSA-OAEP-SHA256、最小 2048 位密钥、DES 仅兼容存量；
- **质量门禁**：全模块行覆盖率 ≥ 85%，Checkstyle 0 违规，SpotBugs 通过，CodeQL 告警清零；
- **零第三方运行期依赖**（核心域），攻击面最小化；
- **现代 Java**：`--release 21`，record / 密封类 / 虚拟线程自由使用。

### 📦 模块
`sure-core`（52 个工具类）/ `sure-json` / `sure-http` / `sure-crypto` / `sure-cron` /
`sure-cache` / `sure-xml` / `sure-poi` / `sure-captcha` / `sure-jwt` / `sure-dfa` /
`sure-examples` / `sure-spring-boot-starter` / `sure-bom` / `sure-all`

### 🚀 快速开始

```xml
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-all</artifactId>
    <version>0.1.0</version>
</dependency>
```

```java
String json = JSONUtil.toJsonStr(Map.of("name", "Alice", "age", 30));
String token = JwtUtil.createToken(Map.of("uid", 1001), "secret", 3600);
```

### 🔒 安全说明
- 漏洞报告走 GitHub Security Advisories，响应承诺：24h 确认 / 72h 评估 / 14 天发布窗口；
- CodeQL（push/PR/每周）+ Dependabot 自动扫描持续运行。

---

## English Summary

**suretool 0.1.0** — a small-but-complete Java utility library for JDK 21+, inspired by Hutool.

- **Secure by default**: AES-GCM, RSA-OAEP-SHA256, min 2048-bit keys, DES kept for legacy only.
- **Quality-gated**: line coverage ≥ 85% across all business modules, Checkstyle 0 violations,
  SpotBugs clean, CodeQL zero alerts.
- **Zero third-party runtime deps** in the core domain.
- **15 modules**: core / json / http / crypto / cron / cache / xml / poi / captcha / jwt / dfa /
  examples / spring-boot-starter / bom / all.

```xml
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-all</artifactId>
    <version>0.1.0</version>
</dependency>
```

Report vulnerabilities via GitHub Security Advisories (24h acknowledgment / 72h assessment / 14-day fix window).
