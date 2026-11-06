# suretool 路线图：媲美并超越 Hutool

> 目标：让 suretool 在**功能覆盖**上追平 Hutool 高频子集，在**工程质量、API 现代化程度、文档与社区活跃度**上实现超越。
> 现状基线（2026-09-09）：38 个工具类、496 个 public static 方法、112 个测试用例、本地仓库可用、GitHub 仓库已建（推送待网络）。
> 现状基线（2026-11-06）：29 个模块（根 `pom.xml` 的 `<module>` 实测）、280 个主类（`src/main/java` 下非 `module-info` 计数）、1947 处 `public static` 声明、290 个测试类 / 2420 个 `@Test`、单次 `mvn -B verify` 执行 2420 例（0 失败 / 0 错误 / 0 跳过）、23 个 JPMS 模块、`maven.compiler.release=25`、CI 矩阵 3 平台（ubuntu/macos/windows）× JDK 25、JaCoCo 行覆盖门禁 0.98（sure-socket 0.97，根默认 0.70）、Maven Central 已发布 22 个版本（repo1 latest/release=1.19.1）。

---

## 一、对标现状（差距分析）

| 维度 | Hutool（参照） | suretool（现状） | 差距 |
| --- | --- | --- | --- |
| 规模 | 12+ 模块（core/cache/cron/db/dfa/extra/http/log/script/poi/jwt/aop），core 数百个工具类 | 29 个模块，280 个主类 / 1947 处 public static 声明（2026-11-06 实测） | 缺 3-5 个长尾能力域 |
| 分布 | Maven Central 中央仓库 | Maven Central 已发布（v0.1.0 → v1.19.1，22 个版本，2026-11-06 实测） | 已对齐 |
| 质量 | 测试覆盖中上、迭代 10+ 年 | 2420 个 @Test（290 个测试类）、三平台 CI、JaCoCo 门禁 0.98 | 已工程化 |
| 文档 | 官方文档站 + 大量中文教程 | README + GitHub Pages 文档站 + 21 个示例类（20 个 Demo 可运行） | 文档站已上线，待补教程矩阵 |
| 影响力 | Gitee 24,353 star、行业标准工具库 | GitHub 2 star、0 fork（2026-11-03） | 需长期运营 |
| 差异化 | 迭代慢、部分 API 基于旧 java.util.Date、历史包袱 | 全新代码、可用 java.time 重构、无包袱 | **超越机会点** |

**结论**：不追求复制 Hutool 全部 45 个能力域。聚焦"**高频 80% 场景全覆盖 + 3 个杀手锏体验**"——功能上"够用且更优"，体验上"明显更现代"，影响力上"文档/社区/持续交付三管齐下"。

---

## 二、总体策略

1. **零依赖原则（坚持）**：核心 90% 功能只靠 JDK，这是对比 Guava/Commons 的卖点，也是对比 Hutool 的传承。
2. **模块化拆分**：按 `suretool-core / -http / -json / -cron / -cache / -crypto / -poi / -extra` 演进，按需引入。
3. **现代 API 优先**：`java.time` 全面替代 `java.util.Date`（Hutool 的 Date 系列是历史包袱）；集合 API 返回不可变视图默认值；所有命名遵循 `XxxUtil` + 静态方法。
4. **质量即影响力**：覆盖率 ≥ 98%（sure-core 0.98、sure-socket 0.97）、JMH 基准公开、每次提交全绿，用工程质量建立信任。
5. **文档即增长**：每个工具类都有"一个例子讲清楚"的中文示例，文档站随 v1.0 上线。

---

## 三、分阶段任务清单

### P0 开源基建（已完成 ✅）

- [x] 提交当前全部工作（工具类扩展批次已提交 `671de31`）
- [x] 补齐仓库门面：README 徽章（CI/覆盖率/Release/License/Java/Stars）、`CONTRIBUTING.md`、issue/PR 模板
- [x] 接入 CI（GitHub Actions）：历史矩阵 JDK 17/21 → 现状 3 平台（ubuntu/macos/windows）× JDK 25 全量 `mvn verify` + JaCoCo 覆盖率上报（artifact + Codecov）
- [x] 建立编码规范质量门禁：Checkstyle（`config/checkstyle/checkstyle.xml`，`verify` 阶段强制，0 违规）
- [x] 定义版本策略：`0.1.x` 内部迭代 → `0.x` 功能补齐 → `1.0` 中央仓库首发（见本文件「里程碑 KPI」）
- [x] 推送至 GitHub（经 GitHub API 完成，仓库 TASure/suretool）
- [x] SpotBugs 静态扫描（已随 P2 模块化接入，`verify` 强制，Max 力度 / Medium 阈值）
- [x] License 头自动校验（已随 P2 接入，缺失即构建失败）

**P0 验收结果（2026-09-09 基线）**：`mvn verify` 全绿（112 测试 / 0 Checkstyle 违规），JaCoCo 基线：指令 70.7% / 行 67.9% / 方法 74.5%。

### P1 高频核心补强（1-2 个月，对标 hutool-core 常用子集）

按优先级分 8 个能力域，每个域完成标准：**类齐全 + 测试覆盖 ≥ 0.98（sure-socket 0.97）+ 文档示例**。

#### 1. Bean 域（最高优先级，使用率第一）✅
- [x] `BeanUtil`：属性拷贝（支持类型转换/忽略空值/忽略指定属性）、Bean↔Map、属性读写
- [x] `BeanDesc`/`PropDesc`：Bean 属性元数据（缓存 getter/setter/字段）
- [x] `FieldUtil`：字段遍历/查找/常量读取

#### 2. JSON 域（招牌能力，必须自研零依赖）✅
- [x] `JSONObject`/`JSONArray`：基于 LinkedHashMap/ArrayList 的轻量 JSON 模型
- [x] `JSONUtil`：parse/parseObj/parseArray/toJsonStr/toBean（结合 BeanUtil 反射）
- [x] 严格 RFC 8259 解析（递归下降，非法输入抛 `JSONException`）
- [x] 测试：嵌套、转义、Unicode、数字精度（BigDecimal）、错误输入、往返一致性
- [x] 性能基准对比 hutool-json（已随 P2 JMH 基准套件完成，见「JMH 基准报告」）

#### 3. HTTP 域（零依赖 HttpURLConnection 封装）✅
- [x] `HttpUtil`：get（query 参数编码）/post 表单/postJson/原始 body/下载/字节，超时、仅 http(s) 协议校验、状态码异常语义（`HttpException` 带 statusCode）
- [x] `URLUtil`：URL 拼接、参数提取/解码、域名/路径提取
- [x] 测试：基于 JDK 内置 HttpServer 的本地服务（中文编码、表单、JSON、404、非法协议），不依赖外网
- [x] `HttpRequest`/`HttpResponse` 链式封装：链式构建（URL/头/query/表单/body/超时/重定向），非 2xx 不抛异常由 `isOk()` 判断（P3 第三轮补齐）

#### 4. 并发域 ✅
- [x] `ThreadUtil`：execAsync、sleep、线程工厂、共享守护线程池
- [x] `ExecutorBuilder`：参数化线程池构建器（队列/线程名/拒绝策略）
- [x] `SyncFinisher`：多线程任务并发与汇总（异常传播）
- [x] `LockUtil`：读写锁便捷封装（P3 第一轮补齐）

#### 5. 加密安全域 ✅
- [x] `SecureUtil`：哈希（MD5/SHA1/SHA256/SHA512）+ AES/DES/RSA/HMAC 一键加解密门面 + 随机密钥
- [x] `AesUtil`/`DesUtil`/`RsaUtil`/`HmacUtil` 分门别类（无状态静态方法、线程安全——区别于 Hutool 的对象式实现）
- [x] 安全实践：密钥 MD5 派生（任意长度 key 可用）、CBC + IV、异常语义清晰（`CryptoException` 不吞异常）
- [x] 与 HashUtil 统一命名（`xxxHex/xxxBase64` 双输出）
- [x] 测试含 RFC 4231（HMAC-SHA256）/ RFC 2202（HMAC-SHA1）官方向量校验、密钥序列化往返、错误密钥解密失败

#### 6. 定时与调度域 ✅
- [x] `CronPattern`：6 段 Quartz 风格表达式（`* ? a-b a,b,c */n a-b/n`、周名 SUN-SAT），校验/匹配/下次执行时间
- [x] `CronUtil`：注册表达式任务 + 每秒扫描调度（守护线程、任务独立线程池、同秒去重）
- [x] 纯 JDK（Calendar 实现，无第三方依赖）
- [x] 时间计算正确性测试：跨月/跨年、日/周「或」关系、每 5 秒/15 分钟、不可能日期（2 月 30 日）返回 null

#### 7. 缓存域 ✅
- [x] `Cache<K,V>` 接口 + `FifoCache`/`LruCache`/`LfuCache`/`TimedCache` 四实现
- [x] `CacheUtil`：四种策略一键创建门面
- [x] 逐出策略测试：FIFO 最旧淘汰、LRU 访问刷新、LFU 低频淘汰、TTL 惰性过期
- [x] 并发冒烟：8 线程 × 100 轮 put/get 无异常且容量不超限

#### 8. 集合与文本增强 ✅
- [x] `TreeUtil` + `TreeNode`：扁平列表建树、深度优先遍历、深度、展平
- [x] `BiMap`：双向映射（键值互查、覆盖清理旧映射）
- [x] `StrJoiner`：分隔符/前缀/后缀拼接器（null 跳过、空内容返回空串）
- [x] `CsvUtil`：CSV 读写（引号包裹、引号内逗号、双引号转义）
- [x] `XmlUtil`：Map/Bean ↔ XML（JDK DOM、转义、同名元素转 List）
- [x] 延后项：`CaseInsensitiveMap`/`OrderedMap`/`StrSplitter`（P3 第一轮补齐）

**P1 验收 ✅**：新增 47 个主类（初版 26 → 73）、测试 238 个全绿、指令覆盖率 73.5%（core 域）、`mvn verify` 全绿 + 0 Checkstyle 违规、README 模块表与示例就绪。

### P2 模块化与工程化（与 P1 并行推进）

- [x] 拆分多模块：`sure-core` + `sure-log/-db/-script/-template/-aop/-pdf/-process/-math/-compress/-socket/-event/-json/-http/-crypto/-cron/-cache/-xml/-poi/-extra/-captcha/-jwt/-dfa` + `sure-all` 聚合（P2 完成时为 28 模块，包名不变，用户代码无感；2026-11-05 已增至 **29 个**，新增 `sure-concurrent`，JDK 25 专属）
- [x] 模块间禁止反向依赖（全部单向依赖 core）；core 保持零第三方运行期依赖
- [x] 聚合构建验证：根目录 `mvn verify` 全绿（238 测试 × 10 模块）、Checkstyle 门禁沿用
- [x] CI 多模块化：JaCoCo 报告与 Codecov 上传按模块路径聚合
- [x] JaCoCo 覆盖率门禁：各模块行覆盖率下限按当前基线设保护线（2026-11-05 现状：0.98，sure-socket 0.97；根 pom 默认 0.70，benchmark/examples/bom/all/starter 5 个模块 skip）
- [x] JMH 基准套件：StrUtil/集合/日期/JSON 对比 Hutool 与 JDK 基线（`sure-benchmark` 模块，见「JMH 基准报告」）
- [x] 基线决策（2026-09-10，2026-11-03 更新）：仅支持 JDK25+，`maven.compiler.release=25`，CI 矩阵 ubuntu/macos/windows × JDK 25；写法自由使用 JDK25 特性（record/密封类/虚拟线程等），不考虑低版本兼容
- [x] API 稳定性：P2 时点 76 个主类全部 `@since 0.1.0` 标注（现状 280 个主类按实际版本标注 `@since`）、破坏性变更进 minor 版本、`@deprecated` 流程（见 CONTRIBUTING「API 稳定性约定」）

### P3 发布与生态（3-6 个月）

**P3 第一轮 · 新功能域（对齐 Hutool 高频能力，持续）**
- [x] `sure-captcha` 模块：`LineCaptcha`/`CircleCaptcha`/`ShearCaptcha` + `CaptchaUtil` 门面（纯 JDK AWT 图形验证码、无头环境兼容、零第三方依赖，对齐 Hutool captcha）
- [x] `sure-jwt`：JWT 令牌签发/校验（HS256/HS384/HS512/RS256，基于 sure-crypto 与 sure-json，纯 JDK）
- [x] `sure-http` 链式封装：`HttpRequest`/`HttpResponse`（补齐 P1 遗留）
- [x] `sure-dfa`：敏感词过滤（前缀树 + 停用词跳跃 + FoundWord 命中定位，纯 JDK，对齐 Hutool dfa）

- [x] Maven Central 发布：OSSRH 账号 + GPG 签名 + `-Prelease` 流水线（2026-11-03 完成 v1.19.1 发布，repo1 latest/release=1.19.1；截至 2026-11-05 累计 22 个版本）
- [x] 文档站上线（GitHub Pages）：类索引 + 每类示例 + 快速上手（docs/index.md + pages.yml）
- [x] `sure-all` 聚合包（jar，传递全部模块依赖）+ BOM（`sure-bom`，pom 打包 `dependencyManagement`）统一版本管理（import 后免写版本，含 POI/JUnit/JMH 配套版本）
- [x] 示例仓库：20 个真实场景 Demo（sure-examples 中 21 个 Demo 类，ExamplesRunner 注册 20 个，实测可运行）
- [x] Spring Boot starter（sure-spring-boot-starter 自动装配骨架）

### P4 影响力运营（持续，自 v0.5 起启动）

- [ ] 技术内容：掘金/CSDN/知乎"用 suretool 替代 Hutool 的 N 个理由"系列，每周 1 篇
- [ ] 对比测评：发布与 Hutool 的 API 对照表 + 性能对比报告（数据透明、不贬低）
- [ ] 社区治理：issue 48h 响应、release notes 中文化、感谢贡献者榜单
- [ ] GitHub 运营：`awesome-java` 提交、技术周刊自荐、star 里程碑发文
- [ ] 关键词 SEO：`suretool` 搜索占位、文档站被收录
- [ ] 远期（可选）：IDEA 插件（代码片段/文档内联）、vscode 片段

### P5 开源卓越（Open Source Excellence，成为可信赖组件的硬要求）

**A 安全与可信（可被企业/社区放心引用）**
- [x] Dependabot 依赖自动更新（已启用，log4j 等安全版本收敛）
- [x] CodeQL 代码扫描（0 Open / 15 Closed，2026-09-10）
- [x] Secret scanning（GitHub 默认开启）
- [x] 加密现代化：AES-GCM+随机IV / RSA-OAEP-2048 / DES 标记废弃
- [x] SECURITY.md 真实安全策略（支持版本表 + 上报渠道 + 响应承诺）
- [x] License 合规：NOTICE 文件 + docs/dependencies.md 第三方依赖清单
- [x] SBOM（CycloneDX 聚合 bom.json/bom.xml，随 -Prelease 生成）

**B 工程质量（可复现、可验证）**
- [x] CI 矩阵：ubuntu / macos / windows × JDK 25 全量 `mvn verify`（另含 docs-links、benchmark-gate、CodeQL、OSV 等独立 workflow）
- [x] 三重门禁：Checkstyle（0 违规）/ SpotBugs（Max 力度 / Medium 阈值）/ License 头校验
- [x] 290 个测试类 / 2420 个 `@Test`；2026-11-06 本地 `mvn -B verify` 执行 2420 例全绿（Failures 0 / Errors 0 / Skipped 0，sure-core 1430 例）
- [x] JMH 基准：14 项对比 13 项不慢于 Hutool（见「JMH 基准报告」）
- [x] 覆盖率收紧：全模块行覆盖率门禁 ≥ 0.98（sure-socket 0.97；5 个非工具模块 skip），sure-core 实测 98.23%（2026-11-06 本地 `mvn -B verify`，执行 1430 例）
- [x] JSON 解析 fuzz 测试（JsonFuzzTest 63 例：畸形输入 / 1000 层嵌套 / 10 万字符 / 全 unicode / 循环引用）

**C 发布与分发（可被一键引入）**
- [x] 多模块 + sure-all 聚合 + sure-bom 统一版本管理
- [x] Maven Central 发布（OSSRH + GPG + `-Prelease` 流水线；2026-11-03 v1.19.1 上线，repo1 latest/release=1.19.1）
- [x] CHANGELOG.md（Keep a Changelog 规范，0.1.0 全量条目已整理）
- [x] 发布前自动化清单（scripts/release.ps1 + RELEASING.md + github-setup.md + ossrh-application.md）

**D 文档与上手（降低试用门槛）**
- [x] README 完整门面（徽章 / 快速开始 / 模块表 / 工具类表）
- [x] CONTRIBUTING.md 贡献指南（含 JDK25+ 环境要求）
- [x] 文档站（GitHub Pages）：类索引 + Hutool 对比 + 维护/发布手册 + 技术文章
- [x] 示例仓库：20 个真实场景 Demo（sure-examples；另含 Spring Boot 装配示例 sure-spring-boot-demo）
- [x] Spring Boot starter（sure-spring-boot-starter 自动装配骨架）

**E 社区与影响力（长期运营）**
- [ ] issue SLA：48h 首次响应
- [ ] 技术内容：掘金 / CSDN / 知乎系列文章
- [ ] awesome-java 提交 + 技术周刊自荐
- [ ] 对比测评：与 Hutool API 对照表 + 性能报告（数据透明、不贬低）

---

## 四、里程碑 KPI（可量化验收）

| 里程碑 | 时间 | 功能 | 质量 | 影响力 |
| --- | --- | --- | --- | --- |
| M1 | 2 周 | 60+ 类 / 700+ 方法（**当前进度（2026-11-05）：280 主类 / 1947 public static 声明**） | CI 全绿、覆盖率 70%（**已达成：sure-core 行覆盖 98.23%，全模块门禁 0.98 / socket 0.97**） | 仓库开源、README 门面完整 |
| M2 | 6 周 | JSON/HTTP/并发/加密/定时/缓存落地，模块化拆分（**全部落地，P2 四工程项完成**） | 覆盖率 75%、JMH 报告发布（**覆盖率门禁+JMH 套件已落地**） | 首篇技术文章、10+ star |
| M3（v1.0） | 3 个月 | 100+ 类 / 1000+ 方法（**已达成：29 模块 / 280 主类**） | 覆盖率 85%、Maven Central 发布（**已达成：v1.19.1 于 2026-11-03 上线**） | 文档站上线、100+ star |
| M4（v1.x） | 6-12 个月 | 生态组件（starter/BOM/插件） | 双 JDK 兼容、长期 API 稳定 | 500+ star、10+ 外部贡献者、月下载量 10k+ |

---

## 五、差异化超越点（不复制，直接碾压）

1. **java.time 全面化**：`DateUtil` 全部基于 `LocalDateTime`，输出 `yyyy-MM-dd HH:mm:ss` 不再有 `Date` 的时区/可变坑。
2. **null-safe 与不可变默认**：集合工具默认返回不可变视图；空输入不抛 NPE、不产生垃圾对象。
3. **错误语义清晰**：`Assert` 区分参数异常与状态异常；工具方法不静默吞异常（除 `closeQuietly` 类）。
4. **类型安全**：`ConvertUtil` 泛型化、`Dict.getXxx` 返回包装类型而非强制转换。
5. **性能即文档**：JMH 基准公开，关键路径（字符串拼接、日期解析、JSON）有"不慢于 Hutool"的量化证据。
6. **文档示例即测试**：示例代码全部可编译可运行，文档站自动从 `src/test/java` 示例提取。
7. **响应式维护节奏**：Hutool 发版慢、issue 积压；suretool 承诺小步快跑（月度 minor）。

---

## 六、每周执行节奏（建议）

| 日 | 事项 |
| --- | --- |
| 周一 | 规划本周 1 个能力域 + 拆任务 |
| 周二-四 | 写类 + 测试（测试先行） |
| 周五 | 文档示例 + 覆盖率检查 + 提交 |
| 周六 | 技术文章 / 社区互动（P4 启动后） |

---

## 七、风险与对策

| 风险 | 对策 |
| --- | --- |
| 功能面铺太广导致质量稀释 | 严格按 P1 优先级，每个域"类齐全+测试 85%+文档"三件套后才算完成 |
| 一个人维护难以持续 | 高质量文档降低贡献门槛；starter/示例仓库吸引外部贡献 |
| Hutool 生态壁垒（用户习惯） | 不硬碰：提供对照表与迁移工具（`ConvertUtil` 兼容层），主打"更现代更稳" |
| GitHub 网络不稳（当前直连超时） | CI 用国内镜像兜底；文档站双托管（GitHub Pages + Gitee Pages） |

---

## 八、JMH 基准报告

> 运行方式：`mvn -pl sure-benchmark exec:java`（配置见 `sure-benchmark/README.md`）。
> 报告策略：每次发布前运行一次并回填；数据透明，只对比事实，不贬低 Hutool。

### v0.1.0 基线（2026-09-10，JDK 17.0.3，AMD，Fork=1 / warmup 1s×3 / measurement 1s×5，平均耗时 ns/op）

| 基准 | suretool (ns/op) | Hutool (ns/op) | suretool 相对 |
| --- | --- | --- | --- |
| StrUtil.isBlank | 1.361 | 1.441 | 快 1.06× |
| StrUtil.isEmpty | 0.407 | 0.435 | 快 1.07× |
| StrUtil.trim | 27.307 | 34.386 | 快 1.26× |
| StrUtil.join | 103.918 | 1348.104 | **快 13.0×** |
| StrUtil.sub | 17.851 | 20.508 | 快 1.15× |
| CollUtil.isEmpty | 0.687 | 0.692 | 持平 |
| CollUtil.join | 194.067 | 2335.375 | **快 12.0×** |
| CollUtil.newArrayList | 41.601 | 41.602 | 持平 |
| CollUtil.contains | 21.921 | 22.909 | 快 1.05× |
| DateUtil.format | 749.588 | 421.442 | 慢 1.78×（待优化，见下） |
| DateUtil.parse | 1075.150 | 7986.832 | **快 7.4×** |
| JSONUtil.parse | 872.003 | 3121.181 | **快 3.6×** |
| JSONUtil.parseObj | 901.178 | 3032.748 | **快 3.4×** |
| JSONUtil.toJsonStr | 357.497 | 3543.600 | **快 9.9×** |

**结论**：14 项中 13 项不慢于 Hutool，其中 join（12-13×）、JSON（3.4-9.9×）、日期解析（7.4×）大幅领先；唯一落后项为 `DateUtil.format`（749 vs 421 ns，亚微秒量级），已用 ThreadLocal SimpleDateFormat 缓存优化一轮（1829→750 ns），后续可引入模式缓存或与 Hutool DatePrinter 对齐。
**运行方式**：`mvn -pl sure-benchmark package && java -jar sure-benchmark/target/sure-benchmark-0.1.0-jar-with-dependencies.jar`

*编制日期：2026-09-09。Hutool 数据来源：Gitee 官方仓库 README（Star 24,353、模块列表）。*
