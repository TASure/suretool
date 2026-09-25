# 贡献指南（CONTRIBUTING）

欢迎为 suretool 贡献代码、文档、示例或 issue。请先阅读并遵守本指南。

## 1. 社区规范

- 所有互动遵守 [行为准则](CODE_OF_CONDUCT.md)（若尚未建立，以"尊重、专业、友善"为原则）。
- 讨论使用中文或英文均可，PR 描述建议中英双语标题。

## 2. 快速上手

```bash
git clone https://github.com/TASure/suretool.git
cd suretool
# 需要 JDK 21+ 与 Maven 3.8+
mvn -pl sure-core test        # 单模块测试
mvn test                      # 全量测试
mvn -pl sure-core verify      # 含 Checkstyle + 覆盖率门禁
```

## 3. 分支与提交

- 开发基于 `main`，请新建分支：`git checkout -b feat/xxx` 或 `fix/xxx`。
- 提交信息规范：`type(scope): 描述`，type 取 `feat` / `fix` / `docs` / `test` / `build` / `refactor` / `perf`。
- 提交前：`mvn -pl <受影响模块> verify` 必须通过；新增/修改公共 API 需补测试（见 §5）。
- 变更登记：涉及用户可见行为/API 的变更，需在 `CHANGELOG.md` 的 `Unreleased` 节追加一行。

## 4. Good First Issue（新手任务）

适合首次贡献者的任务会打 `good first issue` 标签：

- **如何找**：仓库 Issues 页筛选 `good first issue` 标签，任务通常标注"预计改动范围"与"验收标准"。
- **如何认领**：在 issue 下评论"我想认领这个任务"，维护者会分配给你。
- **完成标准**：满足 issue 描述的验收条件，PR 通过 CI（三平台）即合并。

## 5. 测试要求（质量门禁）

- **覆盖率**：sure-core 行覆盖率门禁 ≥90%，新增代码必须带测试。
- **测试框架**：默认 JUnit 4；生成式/属性测试用 jqwik（仅 sure-core 启用，见其 pom）。
- **必测路径**：新增 public/protected 方法必须至少一个用例命中（仓库有 `method_audit.py` 审计脚本可自查）：

```bash
python3 .github/scripts/method_audit.py   # 检查是否有 public/protected 方法未被测试命中
```

- **风格**：遵循 Checkstyle（Tab 缩进、import 排序、Apache License 头），`mvn verify` 会自动检查。

## 6. PR 流程

1. Fork 仓库，基于最新 main 建分支开发；
2. 提交并 push 到你的 fork；
3. 创建 PR：描述改动动机、影响范围、测试结果截图/日志；
4. 等待 CI（ubuntu/macOS/Windows 三平台）通过；
5. 维护者 review：公共 API 变更会有 API 兼容性检查（见 `docs/versioning.md`）；
6. 合并后维护者关闭对应 issue。

## 7. 需要帮助？

- 文档：[类索引](docs/index.md) · [构建与测试](README.md#构建与测试) · [发布指南](docs/RELEASING.md)
- 提问：GitHub Discussions（如已启用）或 issue 标签 `question`
