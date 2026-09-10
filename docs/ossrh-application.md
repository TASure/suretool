# OSSRH groupId 申请工单文案

发布到 Maven Central 需先在 Sonatype OSSRH 申请 groupId。
本文件提供可直接复制到 [issues.sonatype.org](https://issues.sonatype.org) 新建 Issue 的工单内容。

## 已定稿方案（免证明）

- **groupId**：`io.github.tasure`
- **证明方式**：无需额外域名证明。GitHub 用户名 `TASure` 即所有权证明（提交本仓库链接即可）。
- **坐标**：所有模块已统一切换为 `io.github.tasure:sure-*`（17 个 pom 已改，全量回归通过）；
  Java 包名 `com.sure.tool` 保持不变（groupId 与包名相互独立）。

## 工单模板（复制到 JIRA）

```
Project: OSSRH
Issue Type: New Project

Summary: Publish suretool (Java utility library) to Maven Central

Description:
Hi,

I would like to publish an open-source Java utility library to Maven Central.

- GroupId: io.github.tasure
- ArtifactId(s): sure-core, sure-json, sure-http, sure-crypto, sure-cron,
  sure-cache, sure-xml, sure-poi, sure-captcha, sure-jwt, sure-dfa,
  sure-bom, sure-all, sure-examples, sure-spring-boot-starter
- Project name: suretool
- Project URL: https://github.com/TASure/suretool
- SCM URL: https://github.com/TASure/suretool.git
- License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
- Publishing user: TASure
- Do you have permission to publish to this groupId? Yes
  (GitHub account TASure owns the repository TASure/suretool, which
   verifies ownership of the io.github.tasure groupId per Sonatype rules)

This is a small-but-complete Java utility library for JDK 21+:
secure-by-default crypto (AES-GCM, RSA-OAEP), zero-dependency core domain,
11 business modules plus BOM and Spring Boot starter, all gated by
Checkstyle / SpotBugs / JaCoCo (≥85%) / CodeQL in CI.

Thanks!
```

## 提交后

1. 等待 OSSRH 回复（通常 1-2 个工作日）；
2. 通过后按 [RELEASING.md](RELEASING.md) 配置 `~/.m2/settings.xml`（ossrh server + gpg）；
3. 执行 `powershell scripts\release.ps1` 完成首次发布。
