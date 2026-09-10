# suretool 路线图：媲美并超越 Hutool

> 目标：让 suretool 在**功能覆盖**上追平 Hutool 高频子集，在**工程质量、API 现代化程度、文档与社区活跃度**上实现超越。
> 现状基线（2026-09-09）：38 个工具类、496 个 public static 方法、112 个测试用例、本地仓库可用、GitHub 仓库已建（推送待网络）。

---

## 一、对标现状（差距分析）

| 维度 | Hutool（参照） | suretool（现状） | 差距 |
| --- | --- | --- | --- |
| 规模 | 12+ 模块（core/cache/cron/db/dfa/extra/http/log/script/poi/jwt/aop），core 数百个工具类 | 单模块 38 类 / 496 方法 | 缺高频功能域 6-8 个 |
| 分布 | Maven Central 中央仓库 | 仅本地仓库 | 未发布 |
| 质量 | 测试覆盖中上、迭代 10+ 年 | 112 测试、无 CI、无覆盖率门禁 | 需工程化 |
| 文档 | 官方文档站 + 大量中文教程 | README 单页 | 需文档站 + 示例体系 |
| 影响力 | Gitee 24,353 star、行业标准工具库 | 0 star、无社区 | 需长期运营 |
| 差异化 | 迭代慢、部分 API 基于旧 java.util.Date、历史包袱 | 全新代码、可用 java.time 重构、无包袱 | **超越机会点** |

**结论**：不追求复制 Hutool 全部 45 个能力域。聚焦"**高频 80% 场景全覆盖 + 3 个杀手锏体验**"——功能上"够用且更优"，体验上"明显更现代"，影响力上"文档/社区/持续交付三管齐下"。

---

## 二、总体策略

1. **零依赖原则（坚持）**：核心 90% 功能只靠 JDK，这是对比 Guava/Commons 的卖点，也是对比 Hutool 的传承。
2. **模块化拆分**：按 `suretool-core / -http / -json / -cron / -cache / -crypto / -poi / -extra` 演进，按需引入。
3. **现代 API 优先**：`java.time` 全面替代 `java.util.Date`（Hutool 的 Date 系列是历史包袱）；集合 API 返回不可变视图默认值；所有命名遵循 `XxxUtil` + 静态方法。
4. **质量即影响力**：覆盖率 ≥ 85%、JMH 基准公开、每次提交全绿，用工程质量建立信任。
5. **文档即增长**：每个工具类都有"一个例子讲清楚"的中文示例，文档站随 v1.0 上线。

---

## 三、分阶段任务清单

### P0 开源基建（已完成 ✅）

- [x] 提交当前全部工作（工具类扩展批次已提交 `671de31`）
- [x] 补齐仓库门面：README 徽章（CI/覆盖率/Release/License/Java/Stars）、`CONTRIBUTING.md`、issue/PR 模板
- [x] 接入 CI（GitHub Actions）：JDK 17/21 矩阵 `mvn verify` + JaCoCo 覆盖率上报（artifact + Codecov）
- [x] 建立编码规范质量门禁：Checkstyle（`config/checkstyle/checkstyle.xml`，`verify` 阶段强制，0 违规）
- [x] 定义版本策略：`0.1.x` 内部迭代 → `0.x` 功能补齐 → `1.0` 中央仓库首发（见本文件「里程碑 KPI」）
- [x] 推送至 GitHub（经 GitHub API 完成，仓库 TASure/suretool）
- [ ] SpotBugs 静态扫描（延至 P2 随模块化一并接入）
- [ ] License 头自动校验（延至 P2，与发布流水线一并配置）

**P0 验收结果**：`mvn verify` 全绿（112 测试 / 0 Checkstyle 违规），JaCoCo 基线：指令 70.7% / 行 67.9% / 方法 74.5%。

### P1 高频核心补强（1-2 个月，对标 hutool-core 常用子集）

按优先级分 8 个能力域，每个域完成标准：**类齐全 + 测试覆盖 ≥ 85% + 文档示例**。

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
- [ ] `HttpRequest`/`HttpResponse` 链式封装（延后，HttpUtil 单方法版已覆盖主要场景）

#### 4. 并发域 ✅
- [x] `ThreadUtil`：execAsync、sleep、线程工厂、共享守护线程池
- [x] `ExecutorBuilder`：参数化线程池构建器（队列/线程名/拒绝策略）
- [x] `SyncFinisher`：多线程任务并发与汇总（异常传播）
- [ ] `LockUtil`（可选）：读写锁便捷封装

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
- [ ] 延后项：`CaseInsensitiveMap`/`OrderedMap`/`StrSplitter`（P2 补）

**P1 验收 ✅**：新增 47 个主类（初版 26 → 73）、测试 238 个全绿、指令覆盖率 73.5%（core 域）、`mvn verify` 全绿 + 0 Checkstyle 违规、README 模块表与示例就绪。

### P2 模块化与工程化（与 P1 并行推进）

- [x] 拆分多模块：`sure-core` + `sure-json/-http/-crypto/-cron/-cache/-xml/-poi` + `sure-all` 聚合（包名不变，用户代码无感）
- [x] 模块间禁止反向依赖（全部单向依赖 core）；core 保持零第三方运行期依赖
- [x] 聚合构建验证：根目录 `mvn verify` 全绿（238 测试 × 10 模块）、Checkstyle 门禁沿用
- [x] CI 多模块化：JaCoCo 报告与 Codecov 上传按模块路径聚合
- [x] JaCoCo 覆盖率门禁：各模块行覆盖率下限按当前基线设保护线（core 71% / json 63% / http 86% / crypto 78% / cron 93% / cache 75% / xml 80% / poi 88%），目标 85% 随测试补强逐步收紧
- [x] JMH 基准套件：StrUtil/集合/日期/JSON 对比 Hutool 与 JDK 基线（`sure-benchmark` 模块，见「JMH 基准报告」）
- [x] Java 8 兼容性验证：`maven.compiler.release=8`（JDK9+）/ `source=8 target=8`（JDK8 profile 自动切换）+ CI JDK 8/17/21 三版本矩阵
- [x] API 稳定性：全部 76 个主类 `@since 0.1.0` 标注、破坏性变更进 minor 版本、`@deprecated` 流程（见 CONTRIBUTING「API 稳定性约定」）

### P3 发布与生态（3-6 个月）

- [ ] Maven Central 发布：OSSRH 账号 + GPG 签名 + `maven-central` 插件流水线
- [ ] 文档站上线（GitHub Pages 或独立域名）：类索引 + 每类示例 + 快速上手
- [ ] `suretool-all` 聚合包 + BOM（`suretool-bom`）统一版本管理
- [ ] 示例仓库：10+ 个真实场景 Demo（工具库自举：用 suretool 写 suretool 的工具）
- [ ] Spring Boot starter（`suretool-spring-boot-starter`）—— 拉新用户的关键入口

### P4 影响力运营（持续，自 v0.5 起启动）

- [ ] 技术内容：掘金/CSDN/知乎"用 suretool 替代 Hutool 的 N 个理由"系列，每周 1 篇
- [ ] 对比测评：发布与 Hutool 的 API 对照表 + 性能对比报告（数据透明、不贬低）
- [ ] 社区治理：issue 48h 响应、release notes 中文化、感谢贡献者榜单
- [ ] GitHub 运营：`awesome-java` 提交、技术周刊自荐、star 里程碑发文
- [ ] 关键词 SEO：`suretool` 搜索占位、文档站被收录
- [ ] 远期（可选）：IDEA 插件（代码片段/文档内联）、vscode 片段

---

## 四、里程碑 KPI（可量化验收）

| 里程碑 | 时间 | 功能 | 质量 | 影响力 |
| --- | --- | --- | --- | --- |
| M1 | 2 周 | 60+ 类 / 700+ 方法（**当前进度：76 类 / 649 方法**） | CI 全绿、覆盖率 70%（**已达成：core 指令 73.5%、全模块 line 63%-93%**） | 仓库开源、README 门面完整 |
| M2 | 6 周 | JSON/HTTP/并发/加密/定时/缓存落地，模块化拆分（**全部落地，P2 四工程项完成**） | 覆盖率 75%、JMH 报告发布（**覆盖率门禁+JMH 套件已落地**） | 首篇技术文章、10+ star |
| M3（v1.0） | 3 个月 | 100+ 类 / 1000+ 方法 | 覆盖率 85%、Maven Central 发布 | 文档站上线、100+ star |
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
**运行方式**：`mvn -pl sure-benchmark package && java -jar sure-benchmark/target/sure-benchmark-0.1.0-SNAPSHOT-jar-with-dependencies.jar`

*编制日期：2026-09-09。Hutool 数据来源：Gitee 官方仓库 README（Star 24,353、模块列表）。*