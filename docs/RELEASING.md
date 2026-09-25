# 发布指南（RELEASING）

本文件说明 suretool 从本地构建到发布 Maven Central 的完整流程。
仅维护者需要执行；日常开发者只需 `mvn verify`。

# suretool 发布指南（自动化流水线版）

> 推荐方式：**GitHub Actions 一键发布**（`.github/workflows/release.yml`）。
> 推送 `v*` 标签即触发：全量门禁 → 构建发布制品 → GPG 签名 → Sonatype Central Portal
> 上传发布 → 校验 repo1 可访问 → 自动创建 GitHub Release。
> 本地手动发布（`scripts/release.ps1`）保留为兜底通道，见文末第 7 节。

## 0. 自动化发布（推荐）

### 0.1 一次性配置 GitHub Secrets

在仓库 **Settings → Secrets and variables → Actions → New repository secret** 添加：

| Secret 名 | 内容 |
| --- | --- |
| `SONATYPE_USERNAME` | Sonatype Central 用户令牌 username（User Token，如 `GMFea1`） |
| `SONATYPE_PASSWORD` | Sonatype Central 用户令牌 password（User Token） |
| `GPG_PRIVATE_KEY` | GPG 签名私钥 ASCII armor 文本的 base64（`gpg --export-secret-keys --armor <KEYID> \| base64 -w0`） |
| `GPG_PASSPHRASE` | GPG 私钥口令 |

> 密钥仅在工作流运行时注入，不出现在日志；用户令牌可在
> [central.sonatype.com](https://central.sonatype.com) → Account → User Token 管理。

### 0.2 发布（日常操作只有两步）

```bash
# 1. 确认根 pom 版本为非 SNAPSHOT（如 1.1.0）并提交
# 2. 打标签并推送
git tag v1.1.0
git push origin v1.1.0
```

流水线自动完成：verify 门禁 → `-Prelease` 构建（源码/JavaDoc/SBOM）→
bundle 打包（maven 布局 + Manifest）→ 逐文件 GPG 签名（.asc）→
`POST central.sonatype.com/api/v1/publisher/upload` → 轮询 repo1 直到
`io/github/tasure/sure-core/<ver>/sure-core-<ver>.jar` 返回 200 →
创建 GitHub Release（附件 bundle.zip）。

### 0.3 手动触发（不带标签）

Actions → Release to Maven Central → Run workflow：从当前 main 分支版本发布，
标签按 `v<version>` 自动生成。

### 0.4 失败排查

| 现象 | 处理 |
| --- | --- |
| `Portal 上传失败` / 401 | 检查 `SONATYPE_USERNAME/PASSWORD` 是否为 User Token（不是账号密码） |
| `No secret key` | 重新设置 `GPG_PRIVATE_KEY`（base64）并确认与 keyserver 公钥一致 |
| `Signing failed` | 确认 `GPG_PASSPHRASE` 正确 |
| 10 分钟未同步 | 到 [central.sonatype.com](https://central.sonatype.com) → Deployments 查看发布状态，多数为排队 |
| SNAPSHOT 版本被拒 | 发布前必须将根 pom 版本改为非 SNAPSHOT 并提交 |

---

## 1. 环境要求

- JDK 21+（`maven.compiler.release=21`）
- Maven 3.9+
- GPG 密钥对（用于制品签名）
- Sonatype Central Portal 账号 + `io.github.tasure` namespace 权限

### 1.0 Central Portal 注册（一次性，替代已废弃的 OSSRH JIRA 流程）

> Sonatype 已于 2024 年停止 JIRA 工单申请（issues.sonatype.org 已下线），
> 改为 [Central Portal](https://central.sonatype.com) 自助注册：

1. 打开 https://central.sonatype.com → **Sign In**（建议直接用 **GitHub 登录**，TASure 账号即可）；
2. **Create Namespace**：填写 `io.github.tasure`（GitHub 登录用户可直接认领 `io.github.<用户名>`，免 DNS 证明）；
3. **Account → Generate User Token**：生成形如 `<用户名>:<密码>` 的发布凭证（仅显示一次，保存好）；
4. 将 User Token 填入 `~/.m2/settings.xml` 的 `ossrh` server（见 1.1）。

发布通过 OSSRH Staging API 兼容服务（`ossrh-staging-api.central.sonatype.com`），
`nexus-staging-maven-plugin` 官方测试兼容，无需更换发布插件。

详细图文步骤与常见问题见 [ossrh-application.md](ossrh-application.md)。

### 1.1 配置 `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>你的 Portal User Token 用户名</username>
      <password>你的 Portal User Token 密码</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <properties>
        <gpg.keyname>你的 GPG 密钥指纹</gpg.keyname>
        <gpg.passphrase>GPG 私钥口令</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

> 不建议把口令明文写入 settings.xml；可使用 `gpg-agent` 缓存口令，
> 并在 settings.xml 省略 `gpg.passphrase`。

## 2. 发布前检查清单

- [ ] `mvn -q verify` 全绿（Checkstyle 0 违规、SpotBugs 通过、JaCoCo ≥ 各模块下限、License 头完整）
- [ ] 各模块行覆盖率 ≥ 85%（`target/site/jacoco/` 查看）
- [ ] `CHANGELOG.md` 已更新，新增条目移入对应版本
- [ ] 版本号已从 `x.y.z-SNAPSHOT` 改为 `x.y.z`（`mvn versions:set -DnewVersion=x.y.z`）
- [ ] CodeQL / Dependabot 无未处理告警
- [ ] `docs/RELEASING.md`、`README.md` 中引用的版本一致
- [ ] GPG 公钥已上传 keyserver：`gpg --keyserver keyserver.ubuntu.com --send-keys <FINGERPRINT>`

## 3. 本地验证发布产物（不真正上传）

```bash
mvn -Prelease -DskipTests verify
```

该命令会：

1. 执行完整质量门禁（test 阶段被跳过时可去掉 `-DskipTests` 做完整验证）；
2. 生成 **CycloneDX SBOM**（聚合 reactor，输出 `target/bom.json`、`target/bom.xml`）；
3. 为每个模块附加 `-sources.jar` 与 `-javadoc.jar`；
4. 对全部制品执行 GPG 签名（产出 `.asc` 文件）。

产物检查：

```bash
# 在某个模块 target 目录下应看到
ls sure-core/target/sure-core-*.jar
ls sure-core/target/sure-core-*-sources.jar
ls sure-core/target/sure-core-*-javadoc.jar
ls sure-core/target/sure-core-*.jar.asc
```

## 4. 发布到 Sonatype Central

```bash
mvn -Prelease clean deploy
```

- 制品通过 OSSRH Staging API 兼容服务上传（`ossrh-staging-api.central.sonatype.com`），默认**不自动释放**；
- 登录 https://central.sonatype.com → **Deployments** 页面：
  1. 检查制品完整性（jar / sources / javadoc / asc / pom）；
  2. **Publish** 该 deployment（触发校验：签名、坐标、JavaDoc 等）；
  3. 校验通过后制品同步至 Maven Central；
  4. 同步约需 10 分钟 ~ 数小时，可在 [search.maven.org](https://search.maven.org) 确认。

## 5. 发布后收尾

- [ ] 创建 Git 标签：`git tag vx.y.z && git push origin vx.y.z`
- [ ] 在 GitHub Releases 中基于标签发布（附 CHANGELOG 摘要）
- [ ] 将版本号 bump 为下一个 `x.y.z-SNAPSHOT`
- [ ] 在仓库 Discussions/README 中同步最新版本徽章

## 6. 故障排查

| 现象 | 原因与处理 |
| --- | --- |
| `gpg: signing failed: No secret key` | settings.xml 未配置 `gpg.keyname`，或密钥未导入 |
| `Failed to execute goal org.sonatype.plugins:nexus-staging...` | Portal User Token 错误或 namespace 未授权，检查 `<serverId>ossrh</serverId>` 与 `io.github.tasure` 权限 |
| `Invalid User Token` / 401 | User Token 仅显示一次，重新在 Account 页生成并更新 settings.xml |
| JavaDoc 构建失败（中文注释报错） | 检查 `-Xdoclint` 提示；中文注释需保证 UTF-8 编码（项目已统一） |
| 发布后被拒：`Invalid Signature` | 重新上传公开密钥到 keyserver：`gpg --keyserver keyserver.ubuntu.com --send-keys <FINGERPRINT>` |
| SBOM 未生成 | 确认使用 `-Prelease` 且命令为 `verify` 或 `deploy` 阶段（覆盖 `makeAggregateBom` 的 `verify` 绑定） |
