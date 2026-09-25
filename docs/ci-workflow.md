# CI 工作流与本地发布脚本的职责边界（P2-3）

> 目标：明确"本地 scripts/ 脚本"与"GitHub Actions"各自的职责，防止同一件事有两套做法造成漂移。
> 规则：**凡是 Actions 已覆盖的检查，本地不再重复实现；凡是本地才有的凭据操作，Actions 不再手工模拟。**

## 1. 总览

| 入口 | 触发 | 职责 | 产物/结果 |
| --- | --- | --- | --- |
| **ci.yml** | push/PR 到 main | 三平台（ubuntu/macOS/Windows）质量门禁：编译 + 全量测试 + Checkstyle + JaCoCo 覆盖率（≥0.90） | 门禁通过/失败 |
| **codeql.yml** | push/PR/定时 | GitHub CodeQL 静态安全扫描（Java） | SARIF 安全告警 |
| **osv-scanner.yml** | push/定时 | 第三方依赖漏洞扫描（OSV） | 漏洞告警 |
| **benchmark.yml** | push/PR（核心模块变更）/定时 | 性能基准门禁：JMH 跑 sure vs hutool 关键项，比率 >1.5 报警 | 基准回归报告 |
| **pages.yml** | push main | 构建文档站：javadoc（api/）+ 覆盖率徽章 + 文档页 | GitHub Pages 站点 |
| **release.yml** | 推送 `v*` 标签 / 手动 | 发布到 Maven Central：全量门禁 → 构建（含源码/javadoc/SBOM）→ GPG 签名 → Central Portal 上传 → 轮询 repo1 → 创建 GitHub Release | Central 制品 + Release |
| **scripts/release.ps1** | 本地手动（Windows） | **备选发布通道**：本地 GPG 签名 + 本地 ~/.m2/settings.xml 凭据 → deploy 到 OSSRH | Central 制品 |

## 2. 职责边界（防漂移规则）

### 质量检查（唯一入口 = Actions）

- 编译 / 测试 / Checkstyle / 覆盖率 / CodeQL / OSV / 基准门禁：**全部由 CI 执行**，本地不重复实现检查脚本。
- 本地提交前如需快速自检，只运行 `mvn -pl sure-core verify`（等价于 ci.yml 的 ubuntu 单机部分），**不新建本地检查脚本**。

### 发布（双通道，语义互补）

- **推荐通道（GitHub Actions）**：`release.yml`，推送 `vX.Y.Z` 标签即触发。签名与凭据放在仓库 Secrets（GPG_PRIVATE_KEY/GPG_PASSPHRASE/SONATYPE_USERNAME/SONATYPE_PASSWORD），不在任何仓库文件中。
- **备选通道（本地）**：`scripts/release.ps1`，适用于 CI 不可用/密钥不便上云的场景。前置条件：`~/.m2/settings.xml`（ossrh server + gpg.keyname）+ 本机 GPG 密钥。
- 两条通道产出相同的制品（GPG 签名 + 源码/javadoc），**同一版本号只允许发布一次**，发布后不可覆盖（Maven Central 规则）。

### 文档站（唯一入口 = Actions）

- `pages.yml` 全自动构建并部署 javadoc 与覆盖率徽章；本地**不手工维护** `_site/` 或 `api/` 目录。

## 3. 变更纪律

- 新增检查：优先以"新增 workflow / 扩展现有 workflow"方式落地，并在本文件登记一行；禁止在本地脚本里重复实现 CI 检查。
- 修改发布流程：改 `release.yml` 或 `scripts/release.ps1` 时，需同步更新 `docs/RELEASING.md` 与 `docs/ci-workflow.md`。
- 每次版本发布按 `docs/versioning.md` 的流程：`mvn versions:set` → CHANGELOG → 打 `v*` 标签 → push（触发 release.yml）。

## 4. 排障速查

| 现象 | 排查入口 |
| --- | --- |
| CI 失败 | Actions 对应 workflow 日志；本地 `mvn -pl <模块> verify` 复现 |
| 覆盖率门禁失败 | 本地 jacoco 报告 `target/site/jacoco/`，补测试后重跑 |
| 发布失败（Actions） | Secrets 是否配置齐全；GPG key 是否与本地一致；版本号是否已发布过 |
| 发布失败（本地） | `~/.m2/settings.xml` 凭据与 GPG 配置；`scripts/release.ps1 -DryRun` 演练 |
| 文档站不更新 | pages.yml 是否成功；pages 分支/目录设置是否正确 |
