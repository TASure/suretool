# suretool 项目检视报告（对标主流 Java 工具包）

> 检视方式：按「软件研发小组」五角色工作流，由产品经理、软件架构师、研发工程师、全栈代码质检官联合检视，研发小组长验收。
> 检视基线：2026-09-25，本地仓库 `/home/user/suretool`，远端 `github.com/TASure/suretool`，已发布 v1.0.0 至 Maven Central。
> 对标对象：Apache Commons、Google Guava、Hutool、Eclipse Collections（领域库 Jackson/OkHttp/Caffeine/POI 为辅）。

---

## 一、检视基线（客观数据）

| 维度 | 实测值 |
| --- | --- |
| 模块数 | 17（13 业务模块 + benchmark / examples / spring-boot-starter / bom / all） |
| sure-core 主类 | 84 个工具类 |
| public 方法（主要模块） | 1596+ 个 public 声明 |
| 测试用例 | 749 个 `@Test`（测试类约 100 个） |
| sure-core 行覆盖率 | 89.70%（covered 25391/28308） |
| 覆盖率门禁 | 根 pom 0.70 / sure-core 0.71（**与公开承诺 0.90 不符**） |
| CI | GitHub Actions：JDK 21+25 双跑、仅 ubuntu-latest、verify + JaCoCo artifact |
| 供应链 | dependabot.yml（maven + actions，每周一）✅；**无 CodeQL、无 OSV-Scanner** ❌ |
| 工程文件 | LICENSE / NOTICE / CONTRIBUTING / CODE_OF_CONDUCT / SECURITY / CHANGELOG / ROADMAP / README.en ✅ |
| 协作模板 | ISSUE_TEMPLATE（bug/feature）、PULL_REQUEST_TEMPLATE ✅ |
| 发布 | v1.0.0 已上 Central；release.yml 自动化（GPG 签名 + Portal upload + repo1 轮询 + gh release）✅ |
| 文档 | docs/ 13+ 文件、文档站 GitHub Pages ✅、benchmark-report ✅ |
| 模块化 | **module-info.java：0 个** ❌ |
| 可复现构建 | **project.build.outputTimestamp：未配置** ❌ |
| 性能基准 | JMH 报告一次生成，**无 CI 回归门禁** ⚠️ |
| README | 8 个徽章（CI/覆盖率/发布等）✅ |

---

## 二、五维对比矩阵（suretool vs 主流）

| 维度 | Guava / Commons（标杆） | Hutool（中文对标） | suretool 现状 | 差距结论 |
| --- | --- | --- | --- | --- |
| 产品能力 | 覆盖广、API 稳定 20 年 | 45 个能力域、中文文档 | 13 业务模块、高频子集 | 覆盖度够用（聚焦策略正确），但**能力边界未文档化**（缺"不做清单"） |
| 架构 | 模块化 + JPMS（module-info） | 模块化聚合 | 17 模块 + BOM + all | **缺 JPMS 模块化**，缺 SPI 扩展点，第三方依赖版本未全部进 BOM |
| 代码质量 | 覆盖率 80-90%+、API 审查制、reproducible build | 门禁 + 长期迭代 | 门禁 0.71、SpotBugs、Checkstyle 0 违规 | **门禁强度与承诺不符**；缺供应链漏洞扫描（CodeQL/OSV） |
| 测试回归 | 数千用例、OSS-Fuzz、跨平台 CI | 覆盖中上 | 749 用例、89.70% 行覆盖 | 方法级覆盖未审计；**CI 仅 Linux**；无属性/模糊测试 |
| 文档生态 | 官方 wiki、javadoc 站点 | 教程体系 + 文档站 | README + Pages 文档站 | **缺 javadoc 发布**、教程/示例内容不足（第 14 项未启动） |
| 发布治理 | ASF 发布签名/校验、兼容承诺 | 中央仓库稳定发布 | Central + GPG + 自动化流水线 | 单点：**缺可复现构建、无 semver 破坏性变更策略文档** |

---

## 三、问题清单（分级）

### P0 —— 影响"安全、优秀、可靠被引用"目标，必须尽快整改

| 编号 | 问题 | 证据 | 影响 |
| --- | --- | --- | --- |
| P0-1 | **覆盖率门禁与公开承诺不符**：sure-core 门禁 0.71，公开宣称 ≥90%，实测 89.70% | sure-core/pom.xml `jacoco.line.min=0.71`；README/ROADMAP 承诺 90% | 引用方信任受损；"优秀可靠"承诺未兑现 |
| P0-2 | **无供应链漏洞扫描**：缺 OSV-Scanner（依赖漏洞）与 CodeQL（源码安全） | workflows 无 codeql/osv job；曾有 security code-scanning 修复历史但未固化为 CI | 作为被引用组件，依赖 CVE 无人盯防，安全红线 |
| P0-3 | **无 JPMS 模块化**：JDK 21 项目零 module-info.java | `find . -name module-info.java` = 0 | Guava/Commons 均提供 module-info；无模块化影响强封装与 jlink 场景引用 |
| P0-4 | **无可复现构建**：未配置 project.build.outputTimestamp | pom.xml 无 outputTimestamp | Reproducible Builds 是开源信任标准；同一 tag 不同机器产物哈希不一致 |

### P1 —— 影响工程质量与长期可维护性，纳入下一迭代

| 编号 | 问题 | 证据 | 建议方向 |
| --- | --- | --- | --- |
| P1-1 | 方法级测试覆盖未审计：1596+ public 方法 / 749 用例，无法回答"每个方法是否都有测试" | 统计对比 | 生成方法-用例映射审计（基于 jacoco.xml 行级缺口），按类补测 |
| P1-2 | CI 仅 ubuntu-latest：无 macOS / Windows | ci.yml `runs-on: ubuntu-latest` | 补平台矩阵；文件路径、换行、字体渲染类方法需跨平台回归 |
| P1-3 | 无 javadoc 发布：文档站缺 API 文档 | pages.yml 无 javadoc 步骤 | maven-javadoc-plugin + 发布到 Pages `api/` |
| P1-4 | 无 CodeQL 安全扫描 | 同上 P0-2 | 加 codeql-analysis.yml（每周 + push） |
| P1-5 | benchmark 无 CI 回归门禁：JMH 报告一次生成后过期 | docs/benchmark-report.md 静态 | 关键基准纳入 CI 阈值（如 join 性能波动 ±30% 报警） |
| P1-6 | 第三方依赖版本未全部集中治理：jakarta.mail / commons-net / zxing / angus-mail 版本仅存于 sure-extra/pom.xml | 对比 sure-bom/pom.xml | 移入根 pom / sure-bom 的 dependencyManagement，统一升级 |
| P1-7 | 无版本策略文档：semver、破坏性变更声明缺失 | 无 docs/versioning.md | 定义 semver 承诺与破坏性变更流程（对标 Commons 兼容承诺） |
| P1-8 | sure-spring-boot-starter 无独立测试与示例 | 模块测试数 0（starter 未在统计） | 补 starter 冒烟测试 + examples 用法 |

### P2 —— 锦上添花，长期社区建设

| 编号 | 问题 | 建议方向 |
| --- | --- | --- |
| P2-1 | 无属性/模糊测试：边界全靠手写 | 引入 jqwik 或 junit-quickcheck，对 StrUtil/ArrayUtil 等生成式测试 |
| P2-2 | 教程与示例不足（第 14 项未启动） | 文章 / 教程 / Awesome Java 提交 / 示例项目 |
| P2-3 | 双 CI 一致性：本地脚本（scripts/）与 Actions 职责边界未文档化 | 文档化发布/检查入口，避免漂移 |
| P2-4 | 社区可见性：缺下载量/star 激励 | shields 加 "Maven Central downloads" 徽章；CONTRIBUTING 加 good-first-issue 指引 |

---

## 四、整改方向（按问题分组）

1. **信任工程（P0 全部）**：覆盖率达标并锁门禁 0.90 → 供应链扫描（OSV + CodeQL）→ JPMS module-info → Reproducible Build。这四项完成后，suretool 在"安全、可靠"维度具备对标 Guava/Commons 的硬证据。
2. **质量纵深（P1）**：方法级测试审计补测 → 跨平台 CI → javadoc 发布 → benchmark 门禁 → 依赖版本集中治理 → 版本策略文档。
3. **生态建设（P2）**：属性测试 → 内容与示例（对接第 14 项）→ 社区指标。

---

## 五、整改任务清单（软件研发小组五步闭环）

> 每项任务含：负责人角色 / 输入 / 产出 / 验收标准 / 依赖。按序执行，P0 优先。

| # | 优先级 | 任务 | 负责人 | 产出 | 验收标准 | 依赖 |
| --- | --- | --- | --- | --- | --- | --- |
| T1 | P0-1 | 完成 sure-core 覆盖率 ≥90%，门禁 0.71→0.90 | 研发工程师 + 质检官 | 补测代码 + pom 门禁 | `mvn -pl sure-core verify` 全绿且覆盖率 ≥90% | 第 13 项存量补测 |
| T2 | P0-2 | 接入 OSV-Scanner（依赖漏洞） | 质检官 | CI job + 报告 | push/PR 自动扫描，漏洞阻断合并 | T1 后并行 |
| T3 | P0-2 | 接入 CodeQL（源码安全） | 质检官 | codeql-analysis.yml | push 自动分析，高危阻断 | 独立 |
| T4 | P0-3 | 各模块加 module-info.java（JPMS） | 架构师 + 工程师 | 13 个 module-info | `jdeps`/`mvn package` 通过；module 名规范 | T1 后 |
| T5 | P0-4 | 配置可复现构建 | 架构师 | pom outputTimestamp | 同 tag 两次构建 jar 哈希一致 | 独立 |
| T6 | P1-1 | 方法-用例映射审计并按类补测 | 工程师 + 质检官 | 审计报告 + 补测 | sure-core 每 public 方法至少 1 用例命中 | T1 |
| T7 | P1-2 | CI 补 macOS / Windows 平台 | 质检官 | ci.yml 矩阵 | 三平台 verify 全绿 | 独立 |
| T8 | P1-3 | javadoc 发布到文档站 | 质检官 | pages.yml api/ 目录 | 文档站可访问 javadoc 首页 | 独立 |
| T9 | P1-5 | benchmark 纳入 CI 回归门禁 | 架构师 + 工程师 | 基准 CI job + 阈值 | join/parse 等关键项波动超阈值报警 | 独立 |
| T10 | P1-6 | 第三方依赖版本集中到 BOM | 架构师 | sure-bom dependencyManagement | 各模块版本单点维护 | 独立 |
| T11 | P1-7 | 版本策略文档（semver + 破坏性变更） | 产品经理 | docs/versioning.md | 含兼容承诺与变更流程 | 独立 |
| T12 | P1-8 | starter 冒烟测试与示例 | 工程师 | starter 测试 + examples | starter 上下文加载/属性绑定测试通过 | 独立 |
| T13 | P2-1 | 引入属性测试覆盖核心工具 | 工程师 | jqwik 用例 | StrUtil/ArrayUtil/CollUtil 生成式测试并入 verify | T6 |
| T14 | P2-2 | 影响力内容与示例项目（第 14 项） | 产品经理 + 工程师 | 文章/教程/Awesome Java/示例 | 文档站教程 ≥5 篇、示例仓库可用 | T1-T12 分批 |

**执行建议**：T1-T5（P0）为一期，建议本迭代优先完成；T6-T12 为二期；T13-T14 为三期（T14 即 ROADMAP 第 14 项）。每批完成按团队流程 commit + push + CHANGELOG 登记。
