# 主流 Java 工具包全景与 suretool 差异化分析

> 本文用于回答两个问题：**Java 生态里主流的工具包有哪些？** 以及 **suretool 站在它们中间，差异化空间在哪里？**
> 客观陈述各家定位与特点，不做优劣定性；数据以各项目官方仓库 / 文档为准。

---

## 一、主流工具包全景

### 1.1 通用全能型（整体性工具库）

| 工具包 | 维护方 | 定位 | 核心特点 |
| --- | --- | --- | --- |
| **Apache Commons**（lang3 / io / collections4 / codec / text / net / compress 等） | Apache | 老牌全能组件集 | 覆盖面最广、稳定性极高、20+ 年沉淀；按子模块独立引入，API 风格偏保守 |
| **Google Guava** | Google | 全能 + 数据结构 | 集合增强（Immutable、Multimap、BiMap）、缓存、并发、字符串；工程质量与 API 设计被广泛视为标杆 |
| **Hutool** | 社区（Looly） | 国产全能 | 中文文档、核心零依赖、API 按领域聚合（XxxUtil）；迭代快、中文社区影响力大 |
| **Eclipse Collections** | Eclipse 基金会 | 集合专项 | 高性能集合类型、原语集合支持，部分场景强于 JDK 集合 |
| **StreamEx / Vavr** | 社区 | 函数式增强 | Stream 增强（StreamEx）、不可变集合与函数式类型（Vavr） |
| **fastutil / Trove** | 社区 | 高性能原语集合 | 原语类型集合，省内存、速度快，适合大数据/高性能场景 |

### 1.2 领域专用型（按需引入）

| 领域 | 主流选择 | 说明 |
| --- | --- | --- |
| JSON | Jackson、Gson、Fastjson2 | Jackson 是事实标准；Fastjson 历史上有过安全争议 |
| HTTP | OkHttp、Apache HttpClient、JDK HttpClient | OkHttp 易用性好；JDK 21 内置 HttpClient 已够日常使用 |
| 缓存 | Caffeine、Guava Cache | Caffeine 是性能标杆，Guava Cache 更轻 |
| Office | Apache POI、EasyExcel | POI 全功能；EasyExcel 面向大数据量读写 |
| 时间 | java.time（JDK 内置） | Joda-Time 已完成历史使命，被 java.time 取代 |
| 二维码 / 图像 | ZXing、Thumbnailator | ZXing 是二维码事实标准 |
| 日志门面 | SLF4J + Logback / Log4j2 | 事实标准组合 |
| 并发队列 | JCTools | 无锁队列，高性能场景 |
| 加密 | Bouncy Castle、JDK JCA | BC 覆盖算法最全，但体积大 |

---

## 二、通用型工具包横向对比

| 维度 | Apache Commons | Guava | Hutool | suretool |
| --- | --- | --- | --- | --- |
| 形态 | 多个独立子模块 | 单一库 + 若干模块 | 多模块（hutool-all 聚合） | 多模块（sure-all 聚合） |
| JDK 要求 | 视版本，历史包袱重 | JDK 8+ | JDK 8+（5.x） | **仅 JDK 21+** |
| 核心依赖 | 各模块不同，部分依赖外部 | 零（核心） | 零（核心） | **零第三方运行期依赖（核心）** |
| API 风格 | 偏保守、历史兼容 | 优雅、文档极佳 | 中文文档、XxxUtil 聚合 | XxxUtil 聚合 + 现代 API |
| 时间 API | 部分基于 Date | java.time | 部分基于 Date（历史包袱） | **全部 java.time** |
| 并发支持 | 一般 | 强（ListenableFuture 等） | 中等 | **虚拟线程优先（JDK 21 独占）** |
| 文档 | 官方站点全，英文 | 官方 wiki 极佳，英文 | 中文教程丰富 | 中文文档站（GitHub Pages） |
| 生态影响力 | 行业老牌、被广泛引用 | 行业标杆 | Gitee 高 star、中文事实标准 | 起步阶段（发布 v1.0.0） |

**结论**：通用型四家中，Commons 胜在"覆盖面 + 资历"，Guava 胜在"工程与 API 质量"，Hutool 胜在"中文生态 + 聚合体验"。suretool 的差异化空间不在"复制覆盖"，而在 **"JDK 21+ 专属的现代化写法 + 默认安全基线 + 零依赖核心 + 可审计工程质量"**。

---

## 三、参照系与可借鉴实践（suretool 学习清单）

每一家都有值得 suretool 吸收的"一条长板"，按优先级排序：

| 参照系 | 借鉴点 | suretool 落地建议 |
| --- | --- | --- |
| **Guava** | 工程质量与 API 设计规范 | 1) 每个公开方法必须有中文 Javadoc + 示例；2) 参数校验统一走 `Assert`；3) 返回不可变集合作为默认值 |
| **Eclipse Collections** | 集合深度 | 在 CollUtil 上补齐 高频缺失操作（partition、zip、groupBy 多键、交集差集原地版） |
| **Commons** | 覆盖面盘点 | 用 Commons 的类/方法清单做"覆盖率对照表"，只补高频 80%，不追求全量 |
| **Hutool** | 中文生态与聚合体验 | 文档站每个工具类"一个例子讲清楚"；保持 `XxxUtil` 静态方法统一命名 |
| **Caffeine** | 性能工程 | 公开 JMH 基准（sure-benchmark 已建），关键方法有性能对照数据 |
| **Jackson** | JSON 工业级细节 | JSONPath + 流式解析已实现，继续对齐 日期/泛型/循环引用 等边界行为 |

---

## 四、suretool 差异化定位（一句话）

> **面向 JDK 21+ 的"小而全"现代 Java 工具库：零第三方运行期依赖、默认安全基线、虚拟线程优先、每批发布可审计（CI + 覆盖率门禁 + 中央仓库 + GitHub Pages 文档）。**

- **对 Hutool**：功能上"高频 80% 追平"，体验上"更现代"（java.time / 虚拟线程 / 安全默认值），工程上"质量即影响力"。
- **对 Guava / Commons**：不做它们的全集，做"零依赖 + JDK 21 独占特性"的轻量替代；覆盖它们的日常高频子集即可。
- **对领域库（Jackson / OkHttp / POI）**：sure-json / sure-http / sure-poi 定位"开箱即用的轻封装"，重型需求仍可对接领域库。

---

## 五、路线启示（对接 ROADMAP）

1. **覆盖对照表**：以 Commons lang3 + Guava + Hutool 的高频方法交集为基线，维护 `docs/coverage-map.md`，标记"已实现 / 计划 / 不做（附理由）"。
2. **质量门禁**：保持 覆盖率 ≥ 90%（推进中）、SpotBugs 全绿、每次提交 CI 通过——这是对标 Guava 的信任建设。
3. **性能透明**：sure-benchmark 对照 Hutool / Guava，把报告公开（docs/benchmark-report.md 已建），用数据说话。
4. **生态建设**：文章 / 教程 / Awesome Java 提交 / 示例项目（ROADMAP 第 14 项），对标 Hutool 的中文影响力路径。

---

## 附：参考链接

- Apache Commons：https://commons.apache.org/
- Google Guava：https://github.com/google/guava
- Hutool：https://hutool.cn / https://gitee.com/chinabugotech/hutool
- Eclipse Collections：https://github.com/eclipse-collections/eclipse-collections
- Caffeine：https://github.com/ben-manes/caffeine
- ZXing：https://github.com/zxing/zxing
- Apache POI：https://poi.apache.org/
