# suretool 版本策略（SemVer + 兼容承诺）

> 生效版本：v1.1.0 起 · 维护者：TASure（软件研发小组架构师/产品经理评审）
> 本策略对标 Apache Commons Lang、Guava 的兼容性承诺，为引用方提供可预期的升级路径。

## 1. 版本模型

严格遵循 [Semantic Versioning 2.0.0](https://semver.org/)，格式 `MAJOR.MINOR.PATCH`：

| 段位 | 触发条件 | 示例 |
| --- | --- | --- |
| **MAJOR** | 存在破坏性变更（二进制/源码/行为不兼容） | 1.0.0 → 2.0.0 |
| **MINOR** | 向后兼容地新增功能/API；标记 deprecated 但未移除 | 1.0.0 → 1.1.0 |
| **PATCH** | 向后兼容的缺陷修复、安全补丁、文档与构建修正 | 1.0.0 → 1.0.1 |

- **0.x（0.1.0 → 0.9.x）**：公开 API 不稳定期，MINOR 允许破坏性变更，但必须写入 CHANGELOG 并提供迁移说明。**已过 1.0.0，不再适用。**
- **SNAPSHOT**：`x.y.z-SNAPSHOT` 仅代表开发中版本，可随时变化，引用方禁止依赖 SNAPSHOT 进入生产。

## 2. 兼容承诺（四层）

| 层级 | 承诺 | 破坏即触发 MAJOR |
| --- | --- | --- |
| **源码兼容** | 已有源码不加修改即可重新编译 | 移除/重命名 public 方法、改变方法签名 |
| **二进制兼容** | 已编译的 class 无需重编译即可运行（JPMS 描述符稳定） | 方法签名变化、类被移除、`module-info` 导出集变化 |
| **行为兼容** | 同输入下输出语义不变（缺陷修复导致的行为修正除外，须在 CHANGELOG 声明） | 改变返回语义、异常类型、默认值 |
| **模块兼容** | JPMS 模块名（`sure.core`/`sure.json`/…）永不改变；导出包路径 `com.sure.tool.*` 稳定 | 模块更名、导出包变更 |

**零依赖承诺**：`sure-core` 等核心模块不新增运行时第三方依赖；新增可选能力一律放 `sure-extra`。

## 3. 破坏性变更流程（Deprecation Policy）

1. **标记**：计划移除的 API 在 `@Deprecated`（Javadoc `@since` 注明引入版本），并在 CHANGELOG 的 `### Deprecated` 节登记。
2. **周期**：标记后至少保留 **两个 MINOR 版本**（或 6 个月）才允许移除；`sure-extra` 等扩展模块可缩短至一个 MINOR。
3. **迁移**：移除时 CHANGELOG `### Removed` 必须给出等价替代 API 与迁移示例。
4. **例外**：安全漏洞修复若无法保持行为兼容，属 `PATCH` 例外，须在发布说明显著声明。

## 4. 发布节奏

| 类型 | 频率 | 内容 | 验收 |
| --- | --- | --- | --- |
| PATCH | 按需（安全补丁即时） | 缺陷/安全修复 | 全量 `mvn verify` 绿；覆盖率门禁 ≥0.90 |
| MINOR | 约 1-2 月 | 新工具类/增强 | 方法-用例审计通过；benchmark 门禁通过 |
| MAJOR | 有计划（≤1 年） | 破坏性演进 | 迁移指南 + 双版本并存说明 |

每次发布执行 `scripts/release.sh`（或 Actions `release.yml`）流水线：`mvn verify` → 版本 bump → 打 tag → 发布 Central → 更新文档站。

## 5. 版本号管理

- 单一事实来源：根 `pom.xml` 的 `<version>`；`sure-bom` 与各模块版本由 `${project.version}` 驱动，**禁止手工改子模块版本**。
- 发布后立即将主版本 bump 为下一 `-SNAPSHOT`（如 1.1.0 发布后 → 1.1.1-SNAPSHOT）。
- git tag 与 Maven 版本严格一一对应：tag `v1.1.0` ↔ 坐标 `io.github.tasure:sure-core:1.1.0`。

## 6. 变更记录纪律

- 每次提交的 CHANGELOG 登记遵循 Keep a Changelog：`Added / Changed / Deprecated / Removed / Fixed / Security`。
- 每个 MINOR/MAJOR 发布的 CHANGELOG 必须有"升级指引"小节：列出依赖升级、行为变化、迁移步骤。
- 破坏性变更在 PR 描述与 CHANGELOG 中**双重显式标注**（`[BREAKING]`）。

## 7. 决策权

| 事项 | 决策者 |
| --- | --- |
| 破坏性变更提案 | 架构师评审 + 产品经理批准 |
| 新工具类进入核心模块 | 架构师评审（API 设计、命名、与既有类边界） |
| 安全补丁 | 质检官（OSV/CodeQL 告警）→ 工程师修复 → 维护者发布 |
| 依赖升级（第三方） | 架构师核对兼容性 + 质检官跑全量回归 |

## 8. 对引用方的建议

- 生产环境使用最新 MINOR 或 PATCH，跟随 MAJOR 前先读"升级指引"。
- 使用 `sure-bom` 统一版本，避免手工对齐多模块版本。
- 升级前跑一次 `mvn verify`（含 jdeps/jacoco 门禁）确认无行为回归。
