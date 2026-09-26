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

## 5. benchmark 门禁经验（2026-09 沉淀）

### 5.1 门禁结构与判定

`benchmark.yml`（job: benchmark-gate）按以下顺序执行：

1. **Build benchmark jar**：`mvn -pl sure-benchmark -am package`，显式选取 shade fat jar（排除 `original-*` 残留，避免通配符多匹配导致 `java -jar` 选错）。
2. **Run JMH benchmarks (attempt 1)**：`java -jar "$JAR"` 输出到 `/tmp/bench.log`。
3. **Check regression thresholds (attempt 1)**：`benchmark_gate.py /tmp/bench.log` 解析 sure/hutool 配对，**ratio = sure/hutool > 1.5 判 FAIL**；日志含 JMH `<failure` 标记时立即 FAIL（诚实门禁，防静默漏判）。
4. **Confirm regression (attempt 2, on noise)**：首次 FAIL 时**自动重跑一次**再判，第二次仍 FAIL 才判定回归（job 失败）。此机制用于消除共享 runner 时序噪声误报。

### 5.2 已知误报场景与根因（重要）

- **症状**：`Format` 项 ratio 虚高（历史出现 1.84 / 2.69，均判 FAIL），其余项 PASS。
- **根因**：GitHub Actions 共享 runner 上，sure/hutool 的 JMH fork 可能落在**不同的 CPU 负载窗口**，比值失真。**不是真实性能回归**——本地独立微基准实测 sure vs hutool 真实差距仅 1.07~1.11，本地 CPU 满载模拟下 ratio 稳定 1.14。
- **对策**：① BenchmarkRunner forks **1 → 3**（多 fork 聚合抗噪）；② fork JVM 内存 512m → 256m（低配 runner 内存压力）；③ gate 超阈值自动重跑确认一次。
- **反模式警示**：不要仅凭 CI 单次 ratio 就改业务实现；先本地复现（见 5.3）确认是否存在真实差距，再决定是调实现还是调门禁。

### 5.3 本地复现命令

```bash
# 本机若默认 JDK 不是 21，先切到 JDK 21（否则报 UnsupportedClassVersionError）
export JAVA_HOME=<jdk21 路径>; export PATH=$JAVA_HOME/bin:$PATH
mvn -B -pl sure-benchmark -am package -DskipTests
java -jar sure-benchmark/target/sure-benchmark-<version>-jar-with-dependencies.jar   # 全量
java -jar sure-benchmark/target/sure-benchmark-<version>-jar-with-dependencies.jar ".*DateUtilBenchmark.*"  # 单类
python3 .github/scripts/benchmark_gate.py /tmp/bench.log   # 本地跑 gate
```

### 5.4 触发规则

- **paths 过滤**：`sure-core/**`、`sure-json/**`、`sure-benchmark/**`、`pom.xml`、`.github/workflows/benchmark.yml`、`.github/scripts/benchmark_gate.py` —— **workflow/脚本自身变更也会自动触发**，无需手动 dispatch。
- 另有 `schedule: 0 5 * * 1`（每周一 05:00 UTC）与 `workflow_dispatch` 兜底。

### 5.5 pages.yml 文档站构建的两个坑（2026-09-26 修复实录）

- **坑 1：`_site` 属主为 root，后续写入 Permission denied**。`actions/jekyll-build-pages@v1` 在容器内以 root 构建，生成的 `_site/` 属主为 root；紧接着在同一 job 里 `mkdir -p _site/api` 会报 `Permission denied`，导致 pages 部署连续失败（站点停留在更早的成功构建，新增文档 404）。
  - **对策**：写入 `_site` 前先 `sudo chmod -R a+rwX _site` 解锁。
  - **注意**：此问题会让"代码修复已 push 但线上不更新"——文档站排查先看 pages workflow 是否绿，再看站点内容。
- **坑 2：javadoc:aggregate 产物路径在 CI 与本地不一致**。GitHub runner 自带 Maven 执行 `mvn -B javadoc:aggregate` 输出到 `target/reports/apidocs`，本地 Maven 输出到 `target/site/apidocs`（插件 3.6.3 默认 site，runner 环境差异导致）。`cp -r target/site/apidocs/. _site/api/` 在 CI 上报 `No such file or directory`。
  - **对策**：不硬编码路径，动态定位：
    ```bash
    APIDOCS=$(find target -maxdepth 4 -type d -name apidocs 2>/dev/null | head -1)
    [ -n "$APIDOCS" ] || { echo "未找到聚合 javadoc 产物"; exit 1; }
    cp -r "$APIDOCS/." _site/api/
    ```
- **连带修复**：README.md 源文件曾被历史提交写入反斜杠污染（`\\\\<dependency>`、`\&#x20;`），GitHub Pages 首页由 jekyll-readme-index 渲染 README.md，导致首页代码块显示乱码。修复 = 清理 README.md 代码块内全部反斜杠转义（保留徽章 URL 的合法 `\&`）。
