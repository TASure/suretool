# 参与贡献指南

感谢你对 suretool 的关注！在提交 PR 前请阅读以下约定。

## 项目约定

- **包结构**：所有工具类位于 `com.sure.tool.*`，按能力域分包（`util/codec/collection/date/io/lang` 等）。
- **命名**：工具类统一 `XxxUtil` 命名，全部方法为 `public static`，类提供私有构造器。
- **设计原则**：参考 [Hutool](https://doc.hutool.cn/pages/index/) 的静态方法封装风格，但：
  - 日期一律基于 `java.time`，不使用 `java.util.Date` 做新 API；
  - 空输入默认不抛 NPE（除 `Assert` 语义外）；
  - 集合工具默认返回不可变视图。
- **编码风格**：Tab 缩进、UTF-8、单行单语句，由 Checkstyle（`config/checkstyle/checkstyle.xml`）在 `verify` 阶段强制检查。
- **覆盖率门禁**：每个模块在 `verify` 阶段受 JaCoCo 行覆盖率下限约束（`jacoco.line.min`，各模块 pom 配置），提交不得使其降低。

## API 稳定性约定

suretool 遵循语义化版本：`0.x` 阶段允许破坏性变更但需在 ROADMAP 记录；进入 `1.0` 后破坏性变更只允许在 `minor` 版本发布。

- **`@since` 必填**：类级 Javadoc 必须标注引入版本（当前基线 `0.1.0`）；新增 public 方法建议同时标注 `@since`。
- **`@deprecated` 流程**：计划移除的 API 先标记 `@deprecated` 并注明替代方案，至少保留一个 `minor` 版本后再移除。
- **不静默改名/改签名**：重命名或变更签名必须同步更新测试与 README/ROADMAP 示例，并在提交信息中说明。
- **新增公共方法检查**：纳入下方清单，缺 `@since` 视为未完成。

## 提交规范

提交信息使用以下前缀：

| 前缀 | 用途 |
| --- | --- |
| `feat:` | 新工具类 / 新方法 |
| `fix:` | 缺陷修复 |
| `docs:` | 文档、示例、注释 |
| `test:` | 测试 |
| `build:` / `ci:` | 构建、CI 配置 |
| `refactor:` | 重构（不改变行为） |

## 开发流程

1. Fork 本仓库并克隆到本地。
2. 创建特性分支：`git checkout -b feat/xxx-util`。
3. **测试先行**：为新增/修改的方法编写 JUnit 4 测试，覆盖正常、边界与异常路径。
4. 本地验证全绿后提交：

   ```bash
   mvn -B verify
   ```

   通过标准：`Tests run` 全部通过、Checkstyle 无违规、JaCoCo 覆盖率不低于门禁下限。

5. 推送分支并发起 PR，关联对应 issue（如有）。

## 新增工具类检查清单

- [ ] 类注释说明用途，并标注 `@author` 与 `@since`
- [ ] 私有构造器（工具类不可实例化）
- [ ] 公开方法均有中文 Javadoc（`@param` / `@return` / `@throws`）
- [ ] 测试覆盖：正常路径 + 边界（`null`、空、越界）+ 异常路径
- [ ] 无第三方运行期依赖（核心模块）
- [ ] `mvn -B verify` 全绿