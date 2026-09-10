# OSSRH groupId 申请工单文案

发布到 Maven Central 需先在 Sonatype OSSRH 申请 groupId。
本文件提供可直接复制到 [issues.sonatype.org](https://issues.sonatype.org) 新建 Issue 的工单内容。

## 申请前须知（重要）

Sonatype 校验规则：**groupId 需能证明域名所有权**。

| 方案 | groupId | 证明方式 | 适用 |
| --- | --- | --- | --- |
| **A（推荐，免证明）** | `io.github.tasure` | 无需额外证明，GitHub 用户名 `TASure` 即所有权证明（提交仓库链接即可） | 无 `sure.com` 域名时 |
| B | `com.sure` | 需持有并验证 `sure.com` 域名（DNS/TXT 记录） | 持有该域名时 |

> 若选方案 A，请同步修改根 pom 的 `<groupId>` 为 `io.github.tasure`（子模块自动继承），
> 并全局替换 pom 内 `com.sure:sure-*` 依赖坐标。Java 包名 `com.sure.tool` 不变。
> 修改方式：`git grep -l "com.sure" -- "*.xml"` 逐一替换后执行 `mvn -q verify` 回归。

## 工单模板（复制到 JIRA）

```
Project: OSSRH
Issue Type: New Project

Summary: Publish suretool (Java utility library) to Maven Central

Description:
Hi,

I would like to publish an open-source Java utility library to Maven Central.

- GroupId: <com.sure 或 io.github.tasure（见上述方案）>
- ArtifactId(s): sure-core, sure-json, sure-http, sure-crypto, sure-cron,
  sure-cache, sure-xml, sure-poi, sure-captcha, sure-jwt, sure-dfa,
  sure-bom, sure-all, sure-examples, sure-spring-boot-starter
- Project name: suretool
- Project URL: https://github.com/TASure/suretool
- SCM URL: https://github.com/TASure/suretool.git
- License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
- Publishing user: TASure
- Do you have permission to publish to this groupId? Yes
  (GitHub account TASure owns the repository TASure/suretool;
   if groupId is com.sure, I own the domain sure.com and can verify ownership)

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
