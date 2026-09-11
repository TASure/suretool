# Sonatype Central Portal 发布注册指引（原 OSSRH 申请，已随官方迁移）

> **重要变更**：Sonatype 已于 2024 年停止 issues.sonatype.org JIRA 工单申请，
> 全面迁移到 [Sonatype Central Portal](https://central.sonatype.com) 自助注册。
> 本文件即新流程的完整操作指引，不再需要提交工单。

## 已定稿坐标

- **groupId（namespace）**：`io.github.tasure`
- **证明方式**：GitHub 登录用户可直接认领 `io.github.<用户名>` namespace（TASure 即所有权证明，无需 DNS/域名）。
- **Java 包名**：`com.sure.tool`（保持不变，与 groupId 相互独立）。

## 操作步骤（约 10 分钟）

### 1. 注册/登录

1. 打开 https://central.sonatype.com
2. 点击右上角 **Sign In**
3. 选择 **GitHub 登录**（使用 TASure 账号；Sonatype 将读取该 GitHub 账号的邮箱与用户名）

### 2. 创建 Namespace

1. 登录后进入个人页面，找到 **Create Namespace**（或在 User Menu → Namespaces）；
2. Namespace 填写 `io.github.tasure`；
3. 系统检测到 GitHub 登录账号 `TASure`，**自动验证所有权**（无需额外证明）；
4. 若页面提示等待校验，一般几分钟内完成。

### 3. 生成发布凭证（User Token）

1. 进入 **Account → Generate User Token**；
2. 生成形如 `<用户名>:<密码>` 的凭证，**仅显示一次，请立即保存**；
3. 将 User Token 填入 `~/.m2/settings.xml`：

```xml
<server>
  <id>ossrh</id>
  <username>你的 Portal User Token 用户名</username>
  <password>你的 Portal User Token 密码</password>
</server>
```

### 4. 发布

工程已切换至官方 OSSRH Staging API 兼容端点（`ossrh-staging-api.central.sonatype.com`），
`nexus-staging-maven-plugin` 官方兼容，无需改动插件：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\release.ps1
```

上传后登录 https://central.sonatype.com → **Deployments** → 检查制品 → **Publish** → 等待同步 Maven Central。

完整流程与故障排查见 [RELEASING.md](RELEASING.md)。

## 常见问题

| 问题 | 处理 |
| --- | --- |
| 没有 GitHub 账号或不想用 GitHub 登录 | 可用邮箱注册，但 `io.github.tasure` namespace 需额外所有权证明，推荐 GitHub 登录 |
| User Token 丢失 | Account 页重新生成（旧 token 立即失效） |
| 创建 namespace 时提示已被占用 | `io.github.tasure` 需与 GitHub 用户名完全一致（TASure），否则换用 `io.github.TASure` 并在 pom 同步修改 |
