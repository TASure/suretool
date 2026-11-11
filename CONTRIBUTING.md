# 贡献指南（CONTRIBUTING）

欢迎为 suretool 贡献代码、文档、示例或 issue。本指南给出可直接执行的步骤，照着做即可通过 CI。

## 1. 环境要求

- **JDK 25**（`mvn.cmd` 使用 `maven.compiler.release=25`，JDK 17/21 无法编译本仓库）。
- **Maven 3.9+**，Git。
- CI 在 Ubuntu / macOS / Windows 三平台 × JDK 25 上执行 `mvn -B verify`，本地默认只需跑通一个平台。

## 2. 五分钟跑通构建

```bash
git clone https://github.com/TASure/suretool.git
cd suretool
mvn -B -pl sure-core verify     # 单模块：测试 + Checkstyle + SpotBugs + License + JaCoCo + japicmp
mvn -B verify                   # 全量：29 个模块的全部门禁（首次较慢）
```

## 3. 分支与提交

- 从最新 `main` 建分支：`feat/xxx`、`fix/xxx`、`docs/xxx`、`build/xxx`。
- 提交信息：`type(scope): 描述`，type ∈ `feat` / `fix` / `docs` / `test` / `build` / `refactor` / `perf`。
- 提交前至少跑通受影响模块的 `mvn -B -pl <模块> verify`。
- 用户可见行为 / 公共 API 变更，在 `CHANGELOG.md` 的 `Unreleased` 节追加一行；破坏性变更标注 `Breaking`。

## 4. Good First Issue（新手任务）

- **如何找**：Issues 页筛选 `good first issue`，任务会写明改动范围与验收标准。
- **如何认领**：在 issue 下评论"我想认领"，维护者分配。
- **完成标准**：满足 issue 验收条件，PR 通过 CI（三平台）后合并。

## 5. 质量门禁（CI 会逐项检查）

| 门禁 | 要求 | 自查命令 / 位置 |
| --- | --- | --- |
| 测试 | 全量通过；**不得删除或禁用测试** | `mvn -B verify` |
| 覆盖率 | 各代码模块行覆盖 ≥ **0.98**（`sure-socket` 为 0.97） | 见模块 `pom.xml` 的 `<jacoco.line.min>` |
| 新模块覆盖率 | 必须显式声明 `<jacoco.line.min>0.98</jacoco.line.min>`，否则回落到根默认 0.70 | 新模块 `pom.xml` |
| 方法命中 | 新增 public/protected 方法至少一个用例执行到 | `python3 .github/scripts/method_audit.py` |
| 二进制兼容 | japicmp 对比 `1.19.1` 基线，不兼容修改直接失败 | `mvn -B -pl <模块> verify`（`breakBuildOnBinaryIncompatibleModifications=true`） |
| 模块描述符 | 新增包必须在 `module-info.java` 补充 `exports` | `python3 .github/scripts/check_moduleinfo.py` |
| 依赖契约 | 核心模块零第三方运行期依赖；第三方库（POI / PDFBox / Mail / commons-net / Zxing / commons-compress 等）必须 `optional` | enforcer `bannedDependencies`，根 `pom.xml` |
| 静态检查 | Checkstyle（Tab 缩进、import 顺序）、SpotBugs effort=Max 且 failOnError、License 头 | `mvn -B verify` |

测试框架：JUnit 4.13.2；属性测试用 jqwik（当前仅在 `sure-core` 启用）。测试类放在模块 `src/test/java`，命名 `*Test`。

### 5.1 数量口径防漂移（强制）

**新增/删除模块，或大幅增删测试（单次 PR 增删 ≥ 10 个 `@Test`）的那次 PR，必须在同一个 PR 内同步更新以下文件中的模块数 / 测试数 / 版本数 / 覆盖率：**

- `ROADMAP.md`（现状基线与阶段验收行）
- `docs/modules.md`（模块表与模块总数）
- `README.md` 与 `README.en.md`（模块速览、质量证据表）

数字口径（一律实测，不得沿用旧值）：

| 指标 | 实测方式 |
| --- | --- |
| 模块数 | 根 `pom.xml` 中 `<module>` 的条数（历史基线行保留时须标注日期） |
| 测试类数 / `@Test` 数 | 统计各模块 `src/test/java` 下含 `@Test` 的类与注解条数 |
| 执行用例数 | `mvn -B verify` 日志中模块汇总的 `Tests run:` 行之和（不要用 surefire XML 汇总） |
| 覆盖率 | 各模块 `target/site/jacoco/jacoco.csv` 的 `LINE_COVERED / (LINE_COVERED + LINE_MISSED)` |
| 已发布版本数 | Maven Central `maven-metadata.xml` 的 `<versions>` 条数 |

历史基线可以保留，但必须写明测量日期；**禁止把旧数字写成「当前」**。

## 6. 运行示例

**sure-examples（19 个独立 Demo + 1 个文件监控 Demo，共 20 个）**

```bash
# 修改过核心模块时先安装到本地仓库（否则会用 Central 上的 1.20.0）
mvn -B -pl sure-examples -am install -DskipTests

mvn -B -pl sure-examples exec:java                                                    # 全部示例
mvn -B -pl sure-examples exec:java -Dexec.mainClass=com.sure.tool.example.FileWatchDemo  # 单个示例
mvn -B -pl sure-examples exec:java -Dexec.mainClass=com.sure.tool.example.PoiDemo
```

`FileWatchDemo` 演示 `FileTailer` 的日志追加 → rename 轮转 → 新文件续写（打印 `[rotate]` 与 `[done] rotations=1`），以及 `WatchDir` 的 CREATE / MODIFY / DELETE 事件，全程约 2 秒。

**sure-spring-boot-demo（独立工程，不参与 reactor 发布）**

```bash
cd sure-spring-boot-demo
mvn spring-boot:run
# 另开终端
curl http://localhost:18080/          # 装配状态
curl -o captcha.png http://localhost:18080/captcha
curl http://localhost:18080/event     # 发布事件，返回累计接收数
```

该示例展示 starter 的 `core + captcha` 默认装配，以及显式引入 `sure-event` / `sure-cron` / `sure-cache` 后可选域生效。

## 7. 提交 PR

1. Fork 仓库，基于最新 `main` 建分支开发；
2. push 到你的 fork，按 [PR 模板](.github/PULL_REQUEST_TEMPLATE.md) 填写动机、影响范围与验证证据；
3. 等待 CI（三平台 × JDK 25）全绿——**CI 未全绿不合并**；
4. 公共 API 变更会触发 japicmp 二进制兼容检查（见 `docs/versioning.md`）；
5. 合并使用 squash，保持 `main` 线性历史。

## 8. AI 可读 JavaDoc 规范（必读）

- **类级示例**：public 工具类/门面类的类级 Javadoc 必须含 `<pre>{@code ...}</pre>`（1–5 行真实调用链）。
- **null 语义**：显式写明「null 入参抛 NPE」或「null 安全（返回默认/空）」，禁止含糊。
- **异常语义**：可能抛出的受检/运行时异常写明触发条件。
- **@since 硬性**：新 API 必须带 `@since 1.x.0`，与版本发布一一对应。
- **零依赖约束**：核心模块 Javadoc 不得 {@link} 第三方类。

> 示范类：`com.sure.tool.lang.NullUtil` / `Result` / `Option` / `Results`、`com.sure.tool.thread.AsyncUtil`。

## 9. 版本与 @since

- `@since` 标注**下一个未发布版本窗口**（见 `CHANGELOG.md` 顶部 `[Unreleased]`）。
- 无 `@since` 的 API 视为发布前内部 API。
- 行为变更（即使差异微小）在 CHANGELOG 标注 `Breaking`。

## 10. Issue SLA 与分诊

### 10.1 首响承诺

| 渠道 | 承诺 | 说明 |
| --- | --- | --- |
| Issue（bug / enhancement / question / documentation） | **工作日 48 小时内**首次响应 | 时区 Asia/Shanghai，法定节假日顺延 |
| PR | 5 个工作日内给出 review 结论或明确排期 | 与 CI 状态无关，CI 红时优先要求修复 |
| 安全漏洞 | 不走公开 issue，见 [SECURITY.md](SECURITY.md) | 走私密通道，按该文档时限处理 |

「首次响应」= 维护者至少留下一条有效评论（确认问题、请求补充信息、或转交负责人），**不等于修复完成**。

### 10.2 标签体系

| 维度 | 标签 | 含义 |
| --- | --- | --- |
| 类型 | `bug` / `enhancement` / `documentation` / `question` / `dependencies` | 每个 issue 至少一个 |
| 状态 | `needs-triage` | 新建 issue 的默认状态，尚未归类 |
| 状态 | `needs-repro` | 缺少最小复现，等待报告者补充 |
| 状态 | `needs-design` | 方案未定，需要先讨论 API/行为 |
| 状态 | `blocked` | 被外部依赖或其他 issue 阻塞 |
| 优先级 | `priority:p0` / `priority:p1` / `priority:p2` | p0=安全或发布阻塞；p1=下个 minor；p2=backlog |
| 参与 | `good first issue` / `help wanted` | 适合新手 / 需要社区协助 |
| 关闭 | `duplicate` / `invalid` / `wontfix` | 关闭原因，必须与一条说明性评论同时使用 |

### 10.3 分诊流程

1. **进入队列**：新 issue 落到 `needs-triage`（见 10.4 的现状说明）。
2. **48 小时内分诊**：复现 → 判定类型 → 打优先级 → 必要时追加 `needs-repro` / `needs-design`。
3. **移出队列**：去掉 `needs-triage`；能立即修的直接关联 PR，暂不修的进入 milestone 或留在 backlog（保留 `priority:p2`）。
4. **修复与验证**：PR 正文写 `Fixes #<issue>`；合并时 issue 自动关闭。
5. **超期处理**：`stale.yml` 会对长期无响应的 issue 打标记并最终关闭，关闭前会留评论。

### 10.4 自动打标签的现状（未启用，不假装可用）

- **当前状态**：仓库**没有**任何 issue 自动打标签 workflow，新 issue 需人工加 `needs-triage`。唯一相关的自动化是已有的 `stale.yml`（长时间无活动后提醒/关闭）。
- **要启用自动打标签，需要**：
  1. 在 Settings → Labels 先创建上表中的标签（GitHub 不会自动创建，写入不存在的标签会被静默忽略）；
  2. 增加一个 workflow，声明 `permissions: issues: write`，用内置 `GITHUB_TOKEN` 调用 `issues.addLabels`；**不需要 GitHub App**；
  3. 若要做基于内容的自动分类（按堆栈/关键词判模块），同样只需 `issues: write` 权限与一份关键词映射；
  4. 若要跨仓库同步或自动写入 Projects 字段，则需要 GitHub App 或带 `project` scope 的 PAT。
- 在完成第 1、2 步之前，请勿在文档或模板中假设标签会自动出现。

## 11. 需要帮助？

- 文档：[类索引](docs/index.md) · [模块与 API](docs/modules.md) · [构建与测试](README.md#构建与测试) · [发布指南](docs/RELEASING.md)
- 提问：GitHub Discussions 或 `question` issue 模板；安全漏洞走私密通道（见 [SECURITY.md](SECURITY.md)）。
