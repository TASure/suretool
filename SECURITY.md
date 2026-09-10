# 安全策略（Security Policy）

suretool 作为可被其他项目引用的 Java 工具库，将安全视为最高优先级。本文档说明受支持版本、漏洞上报渠道与响应承诺。

## 受支持版本

| 版本 | 支持状态 |
| --- | --- |
| 0.1.x | ✅ 受支持（当前迭代线） |
| < 0.1.0 | ❌ 不受支持（请升级） |

## 上报安全漏洞

**请勿在公开 issue 中披露漏洞细节。** 请通过以下任一渠道私密上报：

1. **首选**：GitHub Security Advisories → `https://github.com/TASure/suretool/security/advisories/new`（"Report a vulnerability"）
2. **备选**：向维护者发送私密邮件（仓库页面可见的 maintainer 邮箱）

上报时请尽量包含：

- 受影响模块与版本（如 `sure-core 0.1.0`）
- 漏洞类型与危害描述
- 复现步骤或最小 PoC
- 建议的修复方式（可选）

## 响应承诺

| 阶段 | 时限 |
| --- | --- |
| 确认收到 | 24 小时内 |
| 严重性评估与修复计划 | 72 小时内 |
| 高危/严重漏洞修复版本 | 确认后 14 天内 |
| 修复公告 | 版本发布时同步披露（含 CVE 编号如适用） |

## 内置安全实践

- **加密现代化**：AES 使用 GCM + 随机 IV；RSA 使用 OAEP-SHA256 且强制 2048 位以上密钥；DES 已标记 `@deprecated`（仅兼容旧系统解密）。
- **依赖治理**：Dependabot 自动监控依赖漏洞，安全版本统一收敛至根 `pom.xml` 的 `dependencyManagement`；测试日志后端使用已修复 log4j 2.25.4+。
- **零运行时依赖**：核心域（`sure-core`）零第三方运行期依赖，攻击面最小化。

## 依赖与供应链

- 第三方运行期依赖仅存在于专项模块（如 `sure-poi` 基于 Apache POI），并固定版本、随 Dependabot 更新。
- 发布前执行 `mvn verify` 全量门禁（Checkstyle / SpotBugs / JaCoCo / License 头）。

## 公告渠道

安全更新随版本发布同步说明（GitHub Releases + CHANGELOG）。

## 依赖漏洞应急响应（SOP）

当 Dependabot / CodeQL / 安全公告触发漏洞告警时，维护者按以下顺序处置：

| 步骤 | 动作 | 时限 |
| --- | --- | --- |
| 1 | 确认影响面：该依赖是否进入运行期制品（区分 compile / test / optional 作用域） | 24h |
| 2 | 评估可利用性：结合 suretool 调用链判断是否可达（加密 / HTTP / 输入解析面优先） | 24h |
| 3 | 修复：升级到修复版本或移除受影响依赖；API 变化时同步更新模块与文档 | 48h |
| 4 | 回归：`mvn verify` 全量门禁（Checkstyle / SpotBugs / JaCoCo / License） | 修复后立即 |
| 5 | 发布：按 RELEASING.md 发布补丁版本 | 高危 7 天内 |
| 6 | 披露：GitHub Release + CHANGELOG +（如适用）CVE 引用 | 发布时同步 |

测试 / 可选作用域依赖告警可降级处理，但须在对应 PR 中注明"不受影响"的理由，便于审计。

## 持续安全扫描

- **CodeQL**：push / PR / 每周定时扫描（`security-and-quality` 查询集），告警须在合入前清零；
- **Dependabot**：Maven 与 GitHub Actions 依赖每周自动检查并生成升级 PR；
- **覆盖率门禁**：业务模块行覆盖率 ≥ 85%，防止安全相关代码"裸奔"。
