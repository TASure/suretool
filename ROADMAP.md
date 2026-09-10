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
- [ ] 推送至 GitHub（待网络恢复后执行，本地已提交）
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
- [ ] 性能基准对比 hutool-json（延至 P2 随 JMH 基准一起做）

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

#### 5. 加密安全域
- [ ] `SecureUtil`：MD5/SHA/AES/DES/RSA/HMac 一键加解密（JDK 自带实现）
- [ ] `AesUtil`/`DesUtil`/`RsaUtil`/`HmacUtil` 分门别类
- [ ] 安全实践：固定 IV、密钥派生、异常语义清晰（不吞异常）
- [ ] 与 HashUtil 统一命名（`xxxHex/xxxBase64` 双输出）

#### 6. 定时与调度域
- [ ] `CronUtil`：类 Cron 表达式解析与调度（`*/5 * * * * ?`）
- [ ] `CronPattern`/`CronTimer`：表达式校验、下次执行时间计算
- [ ] 支持秒/分/时/日/月/周 + 通配符 + 纯 JDK（Timer/ScheduledExecutor）
- [ ] 时间计算正确性测试（跨月/跨年/夏令时不做、边界日期）

#### 7. 缓存域
- [ ] `CacheUtil`：FIFO/LRU/LFU/定时过期的统一门面
- [ ] `Cache<K,V>` 接口 + 3 种实现 + `TimedCache`
- [ ] 并发安全（ConcurrentHashMap + 原子操作）、容量上限、过期策略测试

#### 8. 集合与文本增强
- [ ] `TreeUtil` + `TreeNode`：父子结构转树、树转列表
- [ ] `BiMap`/`CaseInsensitiveMap`/`OrderedMap` 等专用 Map
- [ ] `StrJoiner`/`StrSplitter`/`StrFormatter`：字符串细分工具（对齐 Hutool 文本包）
- [ ] `CsvUtil`：CSV 读写（Reader/Writer + 行模型）
- [ ] `XmlUtil`：DOM 读写、XML↔Map（JDK 内置）

**P1 验收**：新增 ≥ 40 个类、public static 方法 ≥ 600、覆盖率 ≥ 70%、`mvn verify` 全绿、每个域文档示例就绪。

### P2 模块化与工程化（与 P1 并行推进）

- [ ] 拆分多模块：`suretool-core` 起步，按依赖方向扩展 `-json/-http/-crypto/-cron/-cache/-extra`
- [ ] 模块间禁止反向依赖；核心模块保持零第三方依赖
- [ ] JaCoCo 覆盖率门禁（核心模块 ≥ 85% 后放开）
- [ ] JMH 基准套件：StrUtil/集合/日期/JSON 对比 Hutool 与 JDK 基线，输出报告
- [ ] Java 8 兼容性验证（`maven.compiler.release=8` 已在）+ 高版本 JDK 双跑 CI
- [ ] API 稳定性：`@since` 标注、破坏性变更进 minor 版本、deprecation 流程

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
| M1 | 2 周 | 60+ 类 / 700+ 方法（**当前进度：49 类 / 约 570 方法**） | CI 全绿、覆盖率 70%（**已达成：指令 71.7%**） | 仓库开源、README 门面完整 |
| M2 | 6 周 | JSON/HTTP/并发/加密/定时/缓存落地，模块化拆分（**Bean/JSON/并发 3 域已落地**） | 覆盖率 75%、JMH 报告发布 | 首篇技术文章、10+ star |
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

*编制日期：2026-09-09。Hutool 数据来源：Gitee 官方仓库 README（Star 24,353、模块列表）。*
