# Changelog

本项目所有值得记录的变更都会收录在本文件中。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- **P4 核心工具增强（v0.2.0-SNAPSHOT）**：
  - `CollUtil`：新增分页 `page`、就地洗牌 `shuffle`、频次统计 `countMap`、单向差集 `subtract`、
    列表转映射 `toMap`、批量追加 `addAll`、`Iterable` 判空重载；
  - 新增 `com.sure.tool.id.NanoIdUtil`：URL 安全短 ID（默认 21 字符，可定制字母表与随机源）；
  - 新增 `com.sure.tool.id.UlidUtil`：26 字符 Crockford Base32 ULID，支持单调递增模式；
  - 新增 `com.sure.tool.system.SystemInfo`：CPU 核数/负载、JVM 堆与系统内存、JVM 启动与运行时长、
    PID/命令行、OS 家族判断（Windows/Linux/macOS/Unix）；
  - `ReUtil`：新增正则转义 `escape` / 反转义 `unescape`、按分组提取 `findAll(regex, content, groupIndex)`；
  - `BeanUtil`：新增深拷贝 `deepCopy`（递归复制 Bean/Map/List/Set/数组，无第三方依赖）；
  - 新增 `P4FeaturesTest` 覆盖全部新增 API；sure-core 指令覆盖率 87.9%，全模块 SpotBugs 0 告警；
  - `GzipUtil`：GZIP 压缩/解压（字符串/字节/文件，零依赖）；
  - `SerializeUtil`：JDK 序列化与文件读写、序列化深拷贝；
  - `RateLimiter`：令牌桶限流器（平滑补充、突发容量、阻塞/非阻塞获取）；
  - `RetryUtil`：固定间隔重试与条件重试（结果条件、异常重试开关）；
  - `ExceptionUtil`：堆栈转字符串、根源异常、cause 链判定、异常包装；
  - `EnumUtil`：按名称/序号/toString 转换枚举，支持忽略大小写；
  - 新增 `P4Features2Test`（9 例）；sure-core 指令覆盖率提升至 88.2%；
  - `BloomFilterUtil`：布隆过滤器（可配置预期元素数与误判率，双独立哈希，零依赖）；
  - `UrlUtil`：URL 编码/解码、URI 解析（host/port/scheme/path）、查询参数提取与拼接；
  - `ImageUtil`：基于 ImageIO 的图像读写、缩放、裁剪、灰度、旋转、格式转换；
  - `WeightRandom`：权重随机（任意对象 + 权重项）；
  - `BoundedPriorityQueue`：有界优先队列（容量固定，自动淘汰最次元素）；
  - `Pair` / `Triple`：不可变有序二元组/三元组（JDK record）；
  - 新增 `P4Features3Test`（6 例）；sure-core 指令覆盖率提升至 88.3%；
  - `MoneyUtil`：人民币金额转中文大写（负数/角分/万亿级，财务报销场景）；
  - `CompareUtil`：基础类型与空安全比较、取最值、范围约束；
  - `DateUtil`：新增星座 `getZodiac`、农历生肖 `getChineseZodiac`；
  - `NetUtil`：新增 IPv4 与 long 互转 `ipv4ToLong`/`longToIpv4`、合法性校验 `isIpv4`；
  - `ObjectUtil`：新增 `isAllNotNull`/`isAllNull`/空安全 `compare`；
  - `HashUtil`：新增 MurmurHash3-32 `murmur3_32`、`fnv1a64`、`djb2` 通用哈希；
  - `RandomUtil`：新增 `randomEle`（随机取一个）、`randomEleSet`（随机取 N 个不重复）；
  - 新增 `P4Features4Test`（7 例）；sure-core 指令覆盖率提升至 88.8%。

- **模块化架构（P2）**：拆分为 15 个 Maven 模块，通过 `sure-all` 聚合、`sure-bom` 统一版本管理；
  `sure-core` / `sure-json` / `sure-http` / `sure-crypto` / `sure-cron` / `sure-cache` / `sure-xml` /
  `sure-poi` / `sure-captcha` / `sure-jwt` / `sure-dfa` / `sure-benchmark` / `sure-examples` /
  `sure-spring-boot-starter` / `sure-bom` / `sure-all`。
- **八大高频工具域（P1）**：字符串/集合/日期/IO/加密/JSON/HTTP/缓存等工具类，包前缀 `com.sure.tool`。
- **安全增强域（P3）**：
  - `sure-captcha`：验证码生成与校验；
  - `sure-jwt`：JWT 签发与解析（HS/RS）；
  - `sure-http`：HTTP 客户端封装（连接超时与 TLS 配置）；
  - `sure-dfa`：敏感词过滤（DFA 算法）。
- **加密工具默认安全基线**：`AesUtil` 采用 AES/GCM/NoPadding 认证加密（随机 IV + SHA-256 密钥派生）；
  `RsaUtil` 采用 RSA/OAEP-SHA256，最小密钥长度 2048 位；`DesUtil` 标记 `@Deprecated` 仅供存量数据解密。
- **工程化质量门禁**：Checkstyle（0 违规）、SpotBugs（Max-Medium）、JaCoCo 行覆盖率下限、
  License 头检查、JUnit 4 测试全覆盖。
- **全模块覆盖率 ≥ 85%**：core 85.3% / json 89.8% / cache 94.5% / crypto 87.4% / xml 93.6% /
  captcha 93.9% / cron 93.3% / dfa 91.4% / http 88.2% / jwt 92.8% / poi 88.5%。
- **CI 双版本矩阵**：GitHub Actions 在 JDK 21 与 JDK 25 上构建，覆盖率仅统计 JDK 21。
- **JDK 21+ 基线**：`maven.compiler.release=21`，移除低版本兼容 profile。
- **CodeQL 安全扫描**：新增 `codeql.yml`（push / PR / 每周定时，`security-and-quality` 查询集），历史告警清零。
- **Dependabot**：Maven 与 GitHub Actions 依赖自动升级（patch/minor 分组 PR，log4j-core 已升级至 2.25.4）。
- **发布工程化**：`-Prelease` profile（CycloneDX SBOM / GPG 签名 / sources / javadoc / OSSRH staging）+
  `distributionManagement`；`docs/RELEASING.md` 发布指南；`scripts/release.ps1` 发布辅助脚本。
- **可运行示例**：`sure-examples` 模块 14 个 Demo + `ExamplesRunner` 聚合入口（实测可运行）。
- **Spring Boot Starter**：`sure-spring-boot-starter` 自动装配骨架（`suretool.*` 配置前缀）。
- **文档站**：`docs/index.md` 类索引 + GitHub Pages 发布工作流（`pages.yml`）；
  Hutool 对比文档、维护手册、技术文章与 awesome-java 提交材料。
- **社区规范**：`CODE_OF_CONDUCT.md`（Contributor Covenant 2.1）、中英文双语主页（`README.en.md`）、
  issue / PR 模板、`SECURITY.md` 漏洞应急响应 SOP。

### Changed

- `maven.compiler.release` 固定为 21，仅支持 JDK 21 及以上版本；
- 各业务模块 JaCoCo 行覆盖率下限上调至 85%（测试代码同步补强）。

### Fixed

- 修复 `OrderedMap` / `CaseInsensitiveMap` 在 JDK 模块化环境下的 `Map.Entry` 可访问性问题；
- 修复 `AesUtil` / `DesUtil` 缺少 `com.sure.tool.codec.HexUtil` import 导致的编译失败；
- 修复 javadoc 内嵌标签中未转义花括号（`{@code {}}` 等 6 处）导致的 release 阶段
  `javadoc:jar` doclint 构建失败；
- 消除全部 CodeQL 告警（SQL 注入、路径遍历、弱加密、信息泄露类）；
- `ci.yml` JaCoCo 上传补全 `sure-captcha` / `sure-jwt` / `sure-dfa` 三个模块。

### Security

- `SECURITY.md` 明确安全响应承诺（24h 确认 / 72h 修复评估 / 14 天发布窗口）与依赖漏洞应急 SOP；
- 依赖治理：Dependabot 自动监控，安全版本统一收口至根 `pom.xml` 的 `dependencyManagement`；
  测试日志后端使用已修复 log4j 2.25.4+；
- 零运行时依赖：核心域（`sure-core`）零第三方运行期依赖，攻击面最小化。

## [0.1.0-SNAPSHOT] - 未发布

首个可运行快照，包含上述全部模块与工具类。
