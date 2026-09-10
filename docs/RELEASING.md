# 发布指南（RELEASING）

本文件说明 suretool 从本地构建到发布 Maven Central 的完整流程。
仅维护者需要执行；日常开发者只需 `mvn verify`。

## 1. 环境要求

- JDK 21+（`maven.compiler.release=21`）
- Maven 3.9+
- GPG 密钥对（用于制品签名）
- Sonatype OSSRH 账号（suretool 的 `io.github.tasure` groupId）

### 1.1 配置 `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>你的 OSSRH 用户名</username>
      <password>你的 OSSRH 密码/token</password>
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

## 4. 发布到 OSSRH 暂存仓库

```bash
mvn -Prelease clean deploy
```

- 制品上传至 Sonatype staging（`s01.oss.sonatype.org`），默认**不自动释放**；
- 登录 OSSRH 在 Staging Repositories 中：
  1. 检查制品完整性（jar / sources / javadoc / asc / pom）；
  2. `Close` 暂存仓库（触发校验：签名、坐标、JavaDoc 等）；
  3. 校验通过后 `Release`，制品将同步至 Maven Central；
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
| `Failed to execute goal org.sonatype.plugins:nexus-staging...` | OSSRH 账号/仓库名错误，检查 `<serverId>ossrh</serverId>` 与账号权限 |
| JavaDoc 构建失败（中文注释报错） | 检查 `-Xdoclint` 提示；中文注释需保证 UTF-8 编码（项目已统一） |
| 发布后被拒：`Invalid Signature` | 重新上传公开密钥到 keyserver：`gpg --keyserver keyserver.ubuntu.com --send-keys <FINGERPRINT>` |
| SBOM 未生成 | 确认使用 `-Prelease` 且命令为 `verify` 或 `deploy` 阶段（覆盖 `makeAggregateBom` 的 `verify` 绑定） |
