# GitHub 仓库设置指引

本文档列出 suretool 发布前应在 GitHub 网页端完成的仓库设置。
这些操作涉及账号外部状态，需维护者登录 github.com/TASure/suretool 后逐项执行。

## 1. 仓库描述与 Topics（提升可发现性）

仓库主页右上角 **About → ⚙（Edit repository details）**：

- **Description**（建议，英文优先）：
  ```
  A small but complete Java utility library for JDK 21+. Secure-by-default crypto (AES-GCM, RSA-OAEP), zero-dependency core, BOM & Spring Boot starter.
  ```
- **Website**：`https://tasure.github.io/suretool/`（Pages 文档站，部署后生效）
- **Topics**（标签，建议勾选）：
  `java` · `utility` · `toolkit` · `jdk21` · `security` · `crypto` · `json` · `cache` · `jwt` · `captcha` · `dfa` · `apache-license-2.0` · `hutool`

## 2. 开启 Discussions

仓库 **Settings → General → Features**，勾选 **Discussions**（同时会要求创建公告贴）。

建议在 Discussions 建三个置顶分类/话题：
1. `Announcements`（发布与公告）；
2. `Q&A`（使用问题）；
3. `Roadmap & Ideas`（需求收集，供 ROADMAP 优先级投票）。

## 3. Codecov 覆盖率徽章

README 已含 Codecov 徽章，但上传需要 token：

1. 登录 [codecov.io](https://codecov.io)（用 GitHub OAuth），找到 `TASure/suretool` 仓库；
2. 在 Settings → General 复制 **Repository Upload Token**（`CODECOV_TOKEN`）；
3. 回到 GitHub 仓库 **Settings → Secrets and variables → Actions → New repository secret**：
   - Name：`CODECOV_TOKEN`
   - Value：粘贴 token
4. 下次 CI 运行（JDK 21 job）即会上传覆盖率，徽章变为绿色。

> 未配置 token 时 CI 仍正常（`fail_ci_if_error: false`），只是不上传覆盖率。

## 4. 首次发布后的仓库操作

按 `docs/RELEASING.md` 完成 OSSRH Release 后：

1. **打标签**：`powershell scripts\release.ps1 -Tag v0.1.0`（或 `git tag v0.1.0 && git push origin v0.1.0`）；
2. **创建 Release**：GitHub 仓库 **Releases → Draft a new release**：
   - Tag：`v0.1.0`；
   - 标题与正文：直接复制 `docs/release-notes-0.1.0.md`；
3. **更新徽章**：README 中 Release 徽章随 tag 自动更新；Maven Central 徽章可在发布后追加。

## 5. 其他建议

- **开启自动删除分支**：Settings → General → Pull Requests → 勾选
  "Automatically delete head branches"；
- **默认分支保护**：Settings → Branches → Add rule（`main`）：
  - Require status checks to pass（勾选 CI 与 CodeQL 检查）→ 防止未过门禁的合并；
  - Require pull request reviews before merging（1 人即可）；
- **社区健康文件**：CODE_OF_CONDUCT / CONTRIBUTING / SECURITY / issue 模板均已就绪，
  仓库页会自动展示对应徽章与入口。
