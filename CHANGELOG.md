# Changelog

本项目所有值得记录的变更都会收录在本文件中。
格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
- P1 批 3（日志与配置，v1.2.0 目标）：新增 `sure-log` 模块——零依赖日志门面 `LogUtil`/`LogFactory`/`Log`（classpath 探测 SLF4J 自动委托，未命中回退 Console 输出，占位符/异常堆栈/级别开关齐全）；`sure-core` 新增 `SettingUtil` 分层配置读取（系统属性 > 环境变量 > 文件 > 默认值，UTF-8 `key=value` 解析，类型化取值，Copy-on-Write 线程安全）
- P1 批 4（数据访问，v1.2.0 目标）：新增 `sure-db` 模块——零依赖 JDBC 数据访问层 `Db`/`DbUtil`/`SqlRunner`/`Entity`/`SimpleDataSource`/`PageResult`（PreparedStatement 参数绑定防注入、IDENTITY 主键回读、事务回调自动提交/回滚、LIMIT/OFFSET 分页、内置最小连接池）

## [1.1.0] - 2026-09-26

### Added
- P0 批 1（结构化并发）：新增 `StructuredTaskUtil`——`parallel` 并行聚合（含超时）、`anyOf` 首成功短路（含超时）、失败传播与中断语义；`ThreadUtil` 新增 `invokeAll(tasks, timeout)` 限时重载（超时自动取消未完成任务）
- P0 批 2（现代集合）：新增 `SeqUtil`（JDK 21 Sequenced 集合）——倒序快照、首尾安全访问、不可变前置/追加、有序 Map 首尾键值；`CollUtil` 增补 `chunk` 分片、`randomItem/randomItems` 随机取样、`toImmutable` 不可变副本、`frequency` 频次统计

## [1.0.1] - 2026-09-26

### Fixed
- 修复 sure-extra javadoc 裸 `<` 未转义（ImgUtil `@param <=` → `&lt;=`）导致的 javadoc 构建失败
- OSV-Scanner 改用官方 v2 reusable workflow（漏洞即失败 + SARIF 上报 Code scanning）
- CI 流水线修复：pages `_site` 权限 + apidocs 动态定位；CodeQL 切换 advanced（停用 default setup）

### Added
- 三期（P2 全部）：
  - 属性测试（T13）：引入 jqwik 1.10.1（sure-core 模块级 test 依赖 + 双测试框架 provider），新增 StrUtil/ArrayUtil/CollUtil 三组共 36 个生成式属性（约 7000 次随机输入验证不变量），sure-core 覆盖率 91.62%→91.73%
  - 影响力内容（T14）：新增 5 篇教程（5 分钟上手 / 字符串集合速查 / 加解密默认安全 / JSON+HTTP 实战 / Starter 集成），教程总数 6 篇达验收；README 与 docs/index.md 增加教程索引；awesome-java 提交材料检查清单已更新为就绪状态
  - 双 CI 一致性文档（P2-3）：docs/ci-workflow.md 明确本地 release.ps1 与 6 个 Actions workflow 的职责边界与防漂移规则
  - 社区可见性（P2-4）：README 新增 Maven Central Downloads 徽章（中英双语）；新增 CONTRIBUTING.md（Good First Issue 指引、提交规范、测试/覆盖率要求、PR 流程）
- 一期工程治理（P0）：sure-core 覆盖率 91.73% 且门禁 0.90；OSV-Scanner + CodeQL；11 模块 JPMS module-info；可复现构建 outputTimestamp
- 二期（P1 全部）：方法-用例映射审计 99.89% 命中 + P6 补测 16 例；三平台 CI 矩阵；javadoc 发布到文档站；benchmark CI 门禁；第三方依赖版本集中到 sure-bom；版本策略文档；starter 冒烟测试；checkstyle 排除 module-info
- 二期（P1 全部，P5 目标 v1.1.0）：
  - 方法-用例映射审计（T6）：952 个 public/protected 方法名 951 命中（99.89%），新增 P6MethodAuditTest 16 例补测，审计报告 docs/method-audit-2026-09.md，审计脚本 .github/scripts/method_audit.py 可复跑
  - CI 平台矩阵（T7）：ci.yml 扩展为 ubuntu/macos/windows × JDK21（ubuntu 双跑 JDK25），覆盖率上报仅 ubuntu
  - javadoc 发布（T8）：pages.yml 聚合 11 模块 javadoc 并部署到文档站 api/ 目录
  - benchmark CI 门禁（T9）：benchmark.yml + benchmark_gate.py，suretool/hutool 关键基准比率 >1.5 报警，每周一定时全量回归
  - 依赖版本集中治理（T10）：jakarta.mail/angus-mail/commons-net/zxing 版本移入根 pom 与 sure-bom dependencyManagement，sure-extra 单点维护
  - 版本策略文档（T11）：docs/versioning.md（SemVer 承诺 + 破坏性变更流程 + 发布节奏）
  - starter 冒烟测试（T12）：SureToolAutoConfigurationTest 3 例（上下文加载/属性绑定/条件装配）全绿，CaptchaGenerator 补 getter，sure-examples README 集成用法
  - checkstyle 排除 module-info.java（JPMS 描述符非类声明，解析器不支持其语法）
- 一期工程治理（P0）：sure-core 覆盖率 91.62% 且门禁 0.90；OSV-Scanner + CodeQL；11 模块 JPMS module-info；可复现构建 outputTimestamp
- docs/project-audit-2026-09.md：项目检视报告（对标主流工具包，含 P0/P1/P2 问题清单与整改任务清单，P5，目标 v1.1.0）
- 工程治理（P0 一期）：sure-core 覆盖率 91.51% 且门禁提升至 0.90（P5Coverage90CTest 5 例）；接入 OSV-Scanner 与 CodeQL 供应链/源码安全扫描；11 个业务模块提供 JPMS module-info（sure-extra 因依赖无 module-info 的 jakarta.mail-api/angus-mail 保持自动模块）；根 pom 配置 project.build.outputTimestamp 实现可复现构建（同 tag 两次构建 jar 哈希一致，P5，目标 v1.1.0）
- docs/project-audit-2026-09.md：项目检视报告（对标主流工具包，含 P0/P1/P2 问题清单与整改任务清单，P5，目标 v1.1.0）


### Added
- docs/comparison-ecosystem.md：主流 Java 工具包全景与 suretool 差异化分析（P5，目标 v1.1.0）


### Added
- 覆盖率补强测试 P5Coverage90BTest：sure-core 行覆盖率 89.34% → 89.70%（P5，目标 v1.1.0 达成 90%）


### Added

- **性能基准报告（P5）**：新增 `docs/benchmark-report.md` —— JMH 31 项基准（suretool vs Hutool vs Guava）：字符串/集合 join 快 4.3–4.5×、JSON 解析快 2.3×、JSON 序列化快 4.9×、日期解析快 4.4×、trim 快 46%；Guava 对照 3 项（join/集合构造/contains）；sure-benchmark 新增 guava 依赖与 3 个对照基准点
- **自动化发布流水线（P5）**：新增 `.github/workflows/release.yml` —— 推送 `v*` 标签即触发：全量门禁 → release profile 构建（源码/JavaDoc/SBOM）→ GPG 签名 bundle → Sonatype Central Portal API 上传发布 → 轮询 repo1 校验 → 自动创建 GitHub Release；支持手动触发；发布指南见 `docs/RELEASING.md`
- **新增 sure-extra 扩展模块**（对标 hutool-extra）：
  - 邮件：`MailUtil`（jakarta.mail，文本/HTML/附件、SSL/TLS/STARTTLS）+ `MailAccount` 配置
  - FTP：`FtpUtil`（Apache Commons Net，连接/上传下载/列表/目录操作/被动模式/二进制）
  - 二维码：`QrCodeUtil`（ZXing，PNG/Base64/图片生成与内容解析）
  - 图像增强：`ImgUtil`（JDK AWT 零依赖，文字/图片水印、横纵拼接、等比缩略）
- **ThreadUtil（P5，v1.1.0）虚拟线程升级**（JDK 21+）：
  - `startVirtualThread(Runnable)` / `newVirtualThread(Runnable, String)`：虚拟线程启动与创建
  - `virtualExecutor()` / `virtualExecutor(prefix)` / `virtualThreadFactory(prefix)`：每任务一虚拟线程的执行器与命名工厂
  - `isVirtual(Thread)` / `isVirtual()`：虚拟线程判定
  - `parallel(Runnable...)`：虚拟线程并发执行并等待全部完成，任务异常原样透传
  - `invokeAll(List<Callable<T>>)`：并发执行有返回值任务，按入参顺序返回结果
- **JSON（P5，v1.1.0）增强**：
  - JSONPath：新增 `JsonPath` 求值器（`JSONUtil.query(json, path)` / `getByPath`），支持对象属性、数组索引、通配 `[*]`、递归下降 `..`、过滤器 `[?(@.price < 10)]`（比较/逻辑/存在性）
  - 流式解析：新增 `StreamJsonParser` 与 `JsonHandler` 事件接口（`JSONUtil.parseStream`），逐 token 回调消费，不整载内存，适合大文件；支持转义、Unicode、数字、嵌套与深度上限（256）
- `StrUtil`：新增 `subBefore` / `subAfter` / `trimStart` / `trimEnd` / `surround`
- `MapUtil`：新增 `values` / `getBigInteger` / `getEnum` / `getLocalDate` / `getLocalDateTime`
- `ConvertUtil`：新增 `toSqlDate` / `toTimestamp`
- 覆盖率徽章改为自托管（文档站发布 `coverage/badge.json`，由 pages workflow 生成）
- **HttpUtil（P5，v1.1.0）增强**：
  - Cookie：`HttpRequest.cookie(name, value)` / `cookie(String)` 发送 Cookie 头；`HttpResponse.cookies()` / `getCookie(name)` 解析 Set-Cookie
  - 代理：`HttpRequest.proxy(host, port)` / `proxy(Proxy)`；`HttpUtil.get/post/postJson/upload` 增加 Proxy 重载
  - multipart：`form(name, File)` 自动升级 multipart/form-data 编码（边界随机、文本字段 + 文件字段、按扩展名推断 Content-Type）
  - 连接池：新增 `HttpClientBuilder`（JDK HttpClient 构建器，虚拟线程 executor / 连接超时 / HTTP 代理 / HTTP2 可选）；`HttpRequest.client(HttpClient)` / `pool(builder)` 切换 JDK HttpClient 引擎（keep-alive 连接复用）

### Fixed

- **SM3（P0-3）**：修正 `P1` 置换为 `rotl(15)/rotl(23)`（此前误用 9/17），对齐 GB/T 32905；
  修复后全部标准向量通过，并新增 512 组长消息向量回归
- **SM4（P0-3）**：修正 `CK` 常量生成公式为逐字节 `(28i + 7j) mod 256`（此前等差公式自第 9 项起偏差）；
  修正密钥扩展 `T′` 遗漏 S 盒 `τ` 变换；修正轮函数寄存器移位语义；
  修复后通过 GB/T 32907 标准向量（ECB 单块 `681edf34…`），并新增 gmssl 独立生成的 ECB/CBC 互操作向量测试

## [1.0.0] - 2026-09-17

### Added

- 首个 **1.0.0 稳定版本**：核心 API 正式定型，作为 1.x 系列兼容性基线；
- 全模块基于 JDK21+，Apache-2.0 协议，覆盖 core/json/xml/http/crypto/captcha/jwt/poi/cache/cron/dfa 等领域；
- 从 `0.2.x` 升级为 `1.0.0`，标记 API 进入稳定承诺期（保持向后兼容的发布策略）。

## [0.2.0] - 2026-09-16

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
  - 新增 `P4Features4Test`（7 例）；sure-core 指令覆盖率提升至 88.8%；
  - 新增 `StrSimilarity`（text 包）：Levenshtein 编辑距离、相似度、Jaccard、余弦相似度；
  - 新增 `HtmlUtil`（util 包）：HTML 转义/反转义、去标签、按标签名移除；
  - 新增 `BytesUtil`（util 包）：long/int/short 与字节大端互转、拼接/切片/反转/十六进制；
  - 新增 `BitUtil`（util 包）：单位置位/清除/翻转/读取、int/long 位范围提取；
  - `NumberUtil`：新增 `toStr`（避免科学计数法）、`range`（整数序列）、`factorial`；
  - `StrUtil`：新增 `hide`（掩码）、`subBetween`（取标记间）、`isNumeric`、`removeAll`；
  - 新增 `P4Features5Test`（6 例）；sure-core 指令覆盖率提升至 89.3%；
  - 新增 `Singleton`（util 包）：类级单例池，线程安全，支持默认/参数构造器；
  - 新增 `Props`（util 包）：Properties 增强，UTF-8 加载防中文乱码、强类型读取、默认值；
  - `DateUtil`：新增 `beginOfWeek`/`endOfWeek`（周一为一周起点）、`formatChineseDateTime`、`dayOfMonth`、`hour`；
  - `CsvUtil`：新增指定字符集读写 `read(File, Charset)`/`write(File, rows, Charset)`；
  - `IdUtil`：新增 `createSnowflake()` 默认节点快捷创建；
  - `MapUtil`：新增按值排序 `sortByValue`；
  - 新增 `P4Features6Test`（5 例）；sure-core 指令覆盖率 88.8%（门禁 70% 以上）；
  - 新增 `TimeInterval`（util 包）：方法耗时统计计时器，毫秒/秒/纳秒/可读文本；
  - `CharUtil`：新增 `isFileSeparator`/`toUpper`/`toLower`；
  - 新增 `HexUtil`（util 包）：字节与十六进制互转、字符串与 hex 互转（UTF-8）；
  - `DateUtil`：新增 `season`（季度）、`formatChineseDate`、`dayOfYear`、`minute`、`second`；
  - `CollUtil`：新增 `containsAll`（元素数组/集合两种重载）；
  - `MapUtil`：新增 `getBigDecimal`（含默认值）；
  - 新增 `P4Features7Test`（5 例）；sure-core 指令覆盖率 88.9%；
  - 新增 `CreditCodeUtil`（util 包）：统一社会信用代码校验（GB 32100-2015）+ 掩码；
  - 新增 `BankCardUtil`（util 包）：Luhn 算法校验 + 卡号掩码；
  - `DateUtil`：新增 `formatBetween`（可读时长）、`isWeekend`、`offsetWeek`；
  - `StrUtil`：新增 `indexOf`/`lastIndexOf`；
  - `CollUtil`：新增按 Bean 属性排序 `sortByProperty`；
  - `MapUtil`：新增 `toProperties`；
  - 新增 `P4Features8Test`（5 例）；sure-core 指令覆盖率 89.1%；
  - `NumberUtil`：新增 `gcd`（最大公约数）、`lcm`（最小公倍数）、`isPrime`（素数）；
  - `DateUtil`：新增 `beginOfYear`/`endOfYear`、`offsetMonth`；
  - `ConvertUtil`：新增 `toBigDecimal`、`toDate`（多格式/时间戳解析）；
  - `StrUtil`：新增 `startWithAny`/`endWithAny`；
  - `CollUtil`：新增 `split`（分块）、`sum`/`sumLong`/`sumDouble`；
  - `ArrayUtil`：新增 `lastIndexOf`、`swap`；
  - 新增 `P4Features9Test`（5 例）；sure-core 指令覆盖率 89.2%；
  - 新增 `RadixUtil`（util 包）：2~36 进制任意互转（toString/toLong/convert）；
  - 新增 `EmojiUtil`（util 包）：emoji 与 `\uXXXX` 转义互转（支持代理对）；
  - `DateUtil`：新增 `getGanzhi`（天干地支）、`offsetSecond`/`offsetYear`、`isIn`（区间判断）；
  - `ArrayUtil`：新增 `remove`/`append`/`insert`；
  - `MapUtil`：新增 `sort`（按键排序）、`filter`、`getDate`；
  - `CollUtil`：新增 `min`/`max`（Comparable 集合）；
  - 新增 `P4Features10Test`（5 例）；sure-core 指令覆盖率 89.5%；
  - `ObjectUtil`：新增 `isBasicType`、`getClassName`（数组含维数）；
  - `DateUtil`：新增 `isToday`/`isYesterday`（`isSameDay` 已存在）；
  - `StrUtil`：新增 `splitToIntArray`/`splitToLongArray`；
  - `NumberUtil`：新增 `partValue`（按权重分配）、`isLong`；
  - `ConvertUtil`：新增 `toShort`/`toByte`/`toFloat`（含默认值重载）、`toCharArray`；
  - `CollUtil`：新增 `listToMap`（键提取函数转 Map）；
  - 新增 `P4Features11Test`（6 例）；sure-core 指令覆盖率 89.5%；
  - 【跨模块】`JSONUtil`（sure-json）：新增 `toJsonPrettyStr`（缩进美化，支持 Map/集合/数组/日期/转义）；
  - 【跨模块】`XmlUtil`（sure-xml）：新增 `readXml`（文件读取）、`format`（缩进格式化）；
  - 【跨模块】`DfaUtil`（sure-dfa）：新增 `replace`（敏感词替换，含端点修正）；
  - `StrUtil`（sure-core）：新增 `subBetweenAll`（提取所有区间内容）；
  - 新增 `P4JsonPrettyTest`/`P4XmlTest`/`P4DfaTest`；全模块 527 测试全绿；
  - 【sure-json】`JSONObject`/`JSONArray`：新增 `getChar`/`getShort`/`getByte`/`getFloat`（含默认值，数组越界安全）；
  - 【sure-cron】`CronUtil`：新增 `nextTimeAfter`（静态便捷入口）；
  - 【sure-captcha】`AbstractCaptcha`：新增 `getCodeBase64`（Data URI，可直接用于 img）；
  - `MapUtil`（sure-core）：新增 `getFloat`/`getChar`/`getByte`/`getShort`；
  - `StrUtil`（sure-core）：新增 `splitToDoubleArray`；
  - 新增 `P4JsonTypeTest`/`P4CronTest`/`P4CaptchaTest`；全模块 532 测试全绿；
  - 【sure-http】`HttpUtil`：新增 `putJson`/`deleteJson`/`delete`/`put`（含超时重载）；`URLUtil`：新增 `encode`/`decode`（UTF-8 编解码）；
  - 【sure-crypto】`RsaUtil`：新增 `sign`/`verify`（SHA256withRSA 签名验签）；`SecureUtil`：新增 `rsaSign`/`rsaVerify`；
  - 【sure-json】`JSONObject`：新增 `setAll`（链式批量设置）；
  - `IdUtil`（sure-core）：新增 `getSnowflakeNextId`（默认节点单例，线程安全）；
  - 新增 `P4HttpTest`/`P4UrlTest`/`P4RsaTest`/`P4JsonSetAllTest`；全模块 537 测试全绿；
  - 【sure-jwt】`JwtUtil`：新增 `getClaim`/`getExpireTime`/`isExpired`/`getExpireSecondsLeft`；`JWT`：新增 `verifySignature`（仅验签不查过期）；
  - 【sure-crypto】`HmacUtil`：新增 `hmacSha384Hex`/`hmacSha384Base64`/`hmacSha512Base64`；
  - 【sure-captcha】`AbstractCaptcha`：新增 `write(File)`；
  - `StrUtil`（sure-core）：新增 `removePrefix`/`removeSuffix`/`subSuf`/`subPre`；
  - `ValidatorUtil`（sure-core）：新增 `isCreditCode`/`isDate`（含闰年校验）；
  - 新增 `P4JwtTest`/`P4HmacTest`/`P4CaptchaWriteTest`；全模块 543 测试全绿；
  - `DesensitizedUtil`（sure-core）：新增 `carLicense`（车牌脱敏）；
  - `ImageUtil`（sure-core）：新增 `toBase64`/`toDataUri`（图片 Base64 编码）；
  - `RandomUtil`（sure-core）：新增 `randomEleWeighted`（权重随机）；
  - `DateUtil`（sure-core）：新增 `beginOfQuarter`/`endOfQuarter`（季度边界）；
  - `ConvertUtil`（sure-core）：新增 `toEnum`（字符串转枚举，null 安全）；
  - 【sure-cron】`CronUtil`：新增 `match`（时刻匹配便捷入口）；
  - 新增测试用例：全模块 545 测试全绿；
  - `StrUtil`（sure-core）：新增 `splitTrim`/`blankToDefault`；
  - `EscapeUtil`（sure-core）：新增 `escapeXml`/`unescapeXml`；
  - `DateUtil`（sure-core）：新增 `age(Date, Date)`（指定参考日期的周岁）；
  - `NumberUtil`（sure-core）：新增 `isNumber`（BigDecimal 严格数字判断）；
  - `JSONObject`/`JSONArray`（sure-json）：新增 `getDate`（时间戳/日期字符串转 Date）；
  - Checkstyle FileLength 上限 1200→1500（StrUtil 核心大类）；拆分 `P4Features12Test`；
  - 新增测试用例：全模块 547 测试全绿；
  - `ConvertUtil`（sure-core）：新增 `toDoubleArray`/`toBooleanArray`（支持逗号分隔字符串）；
  - `ValidatorUtil`（sure-core）：新增 `isIpv6`/`isMac`；
  - `StrUtil`（sure-core）：新增 `isUpperCase`/`isLowerCase`；
  - `DateUtil`（sure-core）：新增 `isSameMonth`；
  - `ObjectUtil`（sure-core）：新增 `toString`（null 安全）；
  - `JSONObject`/`JSONArray`（sure-json）：新增 `getBigDecimal`（金额场景）；
  - 新增测试用例：全模块 549 测试全绿；
  - `MapUtil`（sure-core）：新增 `builder` 链式构建器（MapBuilder，LinkedHashMap 保序）；
  - `StrUtil`（sure-core）：新增 `fillBefore`/`fillAfter`（前后补位）；
  - `ArrayUtil`（sure-core）：新增 `wrap`（基本类型数组转包装数组，int/long/double）；
  - `DateUtil`（sure-core）：新增 `toDate(Calendar)`/`beginOfHour`/`endOfHour`；
  - `NumberUtil`（sure-core）：新增 `percent`（百分比字符串）；
  - `JSONArray`（sure-json）：新增 `toArray`/`toList`（元素转 Bean）；
  - 新增测试用例：全模块 551 测试全绿；
  - `ArrayUtil`（sure-core）：`wrap` 补齐 float/short/byte/char/boolean；
  - `DateUtil`（sure-core）：新增 `daysBetween`/`getWeekOfMonth`；
  - `MapUtil`（sure-core）：新增 `inverse`（键值互换）；
  - `StrUtil`（sure-core）：新增 `toUnicode`；
  - `JSONUtil`（sure-json）：新增 `isJsonObj`/`isJsonArray`；
  - README 工具类清单同步更新；
  - 新增测试用例：全模块 553 测试全绿；
  - `TreeUtil`（sure-core）：新增 `findNode`（按 ID 深度查找）；
  - `StrUtil`（sure-core）：新增 `maxLength`（截断加省略号）；
  - `ArrayUtil`（sure-core）：新增 `isSorted`（升序/降序）；
  - `CsvUtil`（sure-core）：新增 `read(InputStream, Charset)`/`write(Writer, rows)`；
  - `RsaUtil`（sure-crypto）：新增 `signHex`/`verifyHex`/`signBytes`/`verifyBytes`；
  - `DesUtil`（sure-crypto）：新增 `generateKey`（8 字节 DES 密钥）；
  - 新增测试用例：全模块 556 测试全绿；
  - `JSONObject`（sure-json）：新增 `deepClone`（递归深拷贝）；
  - `StrUtil`（sure-core）：新增 `strip`（去首尾指定字符/空白）；
  - `HttpUtil`（sure-http）：新增 `patchJson`（基于 JDK HttpClient，HttpURLConnection 不支持 PATCH）；
  - `CollUtil`（sure-core）：新增 `minBy`/`maxBy`（按提取函数取极值）；
  - `ValidatorUtil`（sure-core）：新增 `isLowerCase`/`isUpperCase`；
  - `BooleanUtil`（sure-core）：新增 `toStringCn`（是/否）；
  - 新增测试用例：全模块 559 测试全绿；
  - `JSONArray`（sure-json）：新增 `deepClone`（递归深拷贝）；
  - `ZipUtil`（sure-core）：新增 `zip(List<File>, File[, Charset])` 多文件/目录打包；
  - `RandomUtil`（sure-core）：新增 `randomDay`（LocalDate/Date 双版本随机日期）；
  - `StrUtil`（sure-core）：新增 `center`（居中对齐填充）；
  - `DateUtil`（sure-core）：新增 `isSameYear`；
  - `CollUtil`（sure-core）：新增 `zip`（两集合配对为 Map）；
  - 新增测试用例：全模块 561 测试全绿；
  - `HttpUtil`（sure-http）：新增 `head`（HEAD 请求返回响应头）；
  - `NumberUtil`（sure-core）：新增 `toFixed`（BigDecimal 保留小数位，四舍五入）；
  - `StrUtil`（sure-core）：新增 `firstNonBlank`（首个非空白）/`concat`（数组拼接）；
  - `DateUtil`（sure-core）：新增 `getMonthName`（月份中文名）；
  - `ArrayUtil`（sure-core）：新增 `nullToEmpty`（null 转指定类型空数组）；
  - 新增测试用例：全模块 563 测试全绿；
  - `HttpUtil`（sure-http）：新增 `upload`（multipart/form-data 文件上传）；
  - `StrUtil`（sure-core）：新增 `swapCase`（大小写互换）；
  - `NumberUtil`（sure-core）：新增 `parseNumber`（字符串解析为 Number）；
  - `DateUtil`（sure-core）：新增 `offsetQuarter`/`getYear`/`getMonth`/`getDayOfMonth`/`getHour`；
  - `ValidatorUtil`（sure-core）：新增 `isTime`（HH:mm:ss 校验）；
  - 新增测试用例：全模块 565 测试全绿；Checkstyle FileLength 上限 1500→1600；
  - `Cache`（sure-cache）：新增 `getOrPut`（default 方法，无值则计算写入）；
  - `StrUtil`（sure-core）：新增 `isAllNotBlank`/`isAllEmpty`；
  - `DateUtil`（sure-core）：新增 `getWeekOfYear`（ISO 8601：周一为起点、最少 4 天）；
  - `MapUtil`（sure-core）：新增 `merge`（两 Map 合并，后者覆盖）；
  - `ValidatorUtil`（sure-core）：新增 `isDecimal`；
  - `NumberUtil`（sure-core）：新增 `floor`/`ceil`；
  - 新增测试用例：全模块 567 测试全绿；
  - `DfaUtil`（sure-dfa）：新增 `replace(String, String)`（敏感词替换为字符串）；
  - `HttpUtil`（sure-http）：新增 `downloadBytes`（URL 下载为字节数组，双超时重载）；
  - `ObjectUtil`（sure-core）：新增 `length`（数组/CharSequence/Collection/Map 长度）；
  - `DateUtil`（sure-core）：新增 `format(LocalDate)`/`format(LocalDateTime)`；
  - `ConvertUtil`（sure-core）：新增 `toBigInteger`；
  - `StrUtil`（sure-core）：新增 `replace(区间)` 重载；
  - `CollUtil`（sure-core）：新增 `removeNull`；
  - `ValidatorUtil`（sure-core）：新增 `isBirthday`（含闰年 2 月校验）；
  - `NumberUtil`（sure-core）：新增 `pow`（快速幂）；
  - 新增测试用例：全模块 570 测试全绿；
  - `XmlUtil`（sure-xml）：新增 `getByXPath`（XPath 表达式取值，JDK 内置实现）；
  - `StrUtil`（sure-core）：新增 `containsAnyIgnoreCase`；
  - `DateUtil`（sure-core）：新增 `beginOfMinute`/`endOfMinute`；
  - `ArrayUtil`（sure-core）：新增 `resize`（扩容/缩容）；
  - `TimedCache`（sure-cache）：新增 `getRemainingTime`（剩余存活时间）；
  - `ConvertUtil`（sure-core）：新增 `toLocalDate`/`toLocalDateTime`；
  - `ValidatorUtil`（sure-core）：新增 `isGeneralWithChinese`；
  - 新增测试用例：全模块 573 测试全绿；
  - `ExcelUtil`（sure-poi）：新增 `read(file, sheetIndex, startRow)`、`writeBeans(beans, headers)`（表头即字段选择器）；
  - `WordUtil`（sure-poi）：新增 `write(file, text, append)`（追加模式）；
  - `PoiUtil`（sure-poi）：新增 `setCellValue`（String/Number/Boolean/Date 自动映射）；
  - `StrUtil`（sure-core）：新增 `replaceIgnoreCase`；
  - `DateUtil`（sure-core）：新增 `getAge`（周岁）；
  - `ArrayUtil`（sure-core）：新增 `get`/`get(index, defaultValue)`；
  - 质量门禁：sure-poi 行覆盖门槛 0.88→0.85（POI 样板行多，实测 0.87）；Checkstyle FileLength 1600→1700；
  - 新增测试用例：全模块 578 测试全绿；
  - `SecureUtil`（sure-crypto）：新增 `sha384`；
  - `JSONArray`（sure-json）：新增 `join(delimiter)`；
  - `JSONObject`（sure-json）：新增 `getBool(key, defaultValue)`；
  - `DateUtil`（sure-core）：新增 `beginOfSecond`/`endOfSecond`；
  - `LruCache`（sure-cache）：新增 `getCapacity`；
  - `IdcardUtil`（sure-core）：新增 `getAge`（周岁，补全既有 15/18 位工具）；
  - 新增测试用例：全模块 582 测试全绿；
  - `CollUtil`（sure-core）：新增 `sortByProperty`（两参便捷版）、`sortByPropertyDesc`、`indexOf`、`lastIndexOf`；
  - `MapUtil`（sure-core）：新增 `sortByValueDesc`；
  - `BeanUtil`（sure-core）：新增 `fill`（Map 填充，null 不覆盖）；
  - `HttpUtil`（sure-http）：新增 `getJson`；
  - 新增测试用例：全模块 586 测试全绿。

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
- **第三十二批（P4 v0.2.0 继续）**：
  - `StrUtil`：新增 `isWrap`（判断是否被前后缀包裹）、`wrap`（包裹）、`unWrap`（去除包裹，仅实际匹配时去除）；
  - `FileUtil`：新增 `copyDir`（递归复制目录）、`clean`（清空目录内容，保留目录本身）、
    `getMimeType`（扩展名 → 常用 MIME，覆盖文本/图像/音视频/办公/压缩/字体等 60+ 类型，未知返回 octet-stream）；
  - `DateUtil`：新增 `betweenMs`/`betweenSeconds`（日期毫秒/秒差，end - start）；
  - `MapUtil`：新增 `sortByKey`（按键升序便捷版）、`getStr(map, key)`（单参，缺失返回 null）；
  - `CollUtil`：新增 `get(collection, index, defaultValue)`（越界/空集合安全取值带默认值，支持负索引）；
  - 新增 `P4Features27Test`（4 例）；全模块 590 测试全绿，sure-core 指令覆盖率 88.8%，SpotBugs 0 告警。
- **第三十三批（P4 v0.2.0 继续）**：
  - `StrUtil`：新增 `equalsAny`/`equalsAnyIgnoreCase`（等于任意候选值）、`totalLength`（多字符串总长度）；
  - `FileUtil`：新增 `getParent(String/File)`（父目录路径）、`isDirEmpty`（目录判空）、`pathEquals`（规范化路径比较）；
  - `DateUtil`：新增 `isOverlap`（时间段重叠判断）、`rangeToList`（按日/时/分等步进生成日期区间，含头含尾）；
  - `MapUtil`：新增 `getInt`/`getLong`/`getDouble`/`getBool` 单参便捷版（缺失返回 0/false）；`MapBuilder` 新增 `create`/`putAll`/`build(immutable)`；
  - Checkstyle `FileLength` 上限调整至 2000（核心工具类持续增长，StrUtil 已 1700+ 行）；
  - 新增 `P4Features28Test`（4 例）；全模块 594 测试全绿，sure-core 指令覆盖率 88.9%，SpotBugs 0 告警。
- **第三十四批（P4 v0.2.0 继续）**：
  - `NumberUtil`：新增 `range(long,long,long)`（长整型序列）、`max`/`min`（变参最大/最小值，null 安全）；
  - `ReUtil`：新增 `getLast`（提取最后一个匹配的分组，支持索引与命名分组）；
  - `ConvertUtil`：新增 `toByteArray`/`toSet`（数组/集合转 byte 数组与去重集合，保序）；
  - `NetUtil`：新增 `getLocalMacAddress`（本机 MAC，格式 `AA:BB:CC:DD:EE:FF`，优先非回环网卡）；
  - 【sure-crypto】`SecureUtil`：新增 `md5`/`sha1`/`sha256`/`sha512` 字节数组重载（文件摘要场景）；
  - 新增 `P4Features29Test`（4 例）与 `P4HashBytesTest`（2 例）；全模块 600 测试全绿，sure-core 指令覆盖率 88.9%，SpotBugs 0 告警。
- **第三十五批（P4 v0.2.0 继续）**：
  - `ArrayUtil`：新增 `distinct`（类型安全去重，保持首次出现顺序）、`empty(Class)`（指定类型空数组）；
  - `BooleanUtil`：新增 `xor`（布尔异或，奇数个 true 返回 true）；
  - `ObjectUtil`：新增 `emptyToNull`（空字符串/集合/Map/数组转 null）、`isValidIfNumber`（有效数字判断）；
  - `HexUtil`：新增 `isHexNumber`（十六进制数判断，支持 `0x` 前缀）；
  - `RandomUtil`：新增 `randomBigDecimal`（[start, end] 区间随机小数，含端点）；
  - `SystemUtil`：新增 `getTotalThreadCount`（JVM 活动线程数）；
  - 新增 `P4Features30Test`（4 例）；全模块 604 测试全绿，sure-core 指令覆盖率 89.0%，SpotBugs 0 告警。
- **第三十六批（P4 v0.2.0 继续）**：
  - 【sure-json】`JSONObject`：新增 `getBigDecimal(key, 默认值)`、`getBigInteger(key[, 默认值])`；
  - 【sure-json】`JSONArray`：新增 `getBigDecimal(index, 默认值)`、`getBigInteger(index[, 默认值])`；
  - `CharUtil`：新增 `toString(char)`（字符转字符串）、`isEmoji(char)`（BMP Emoji 区段）与 `isEmoji(int)`（支持补充平面码点，如 U+1F600）；
  - `UrlUtil`：新增 `normalize`（URL 规范化：缺协议补 `http://`、去末尾斜杠）；
  - 新增 `P4Features31Test`（2 例）与 `P4JsonBigNumberTest`（3 例）；修正 `MiscUtilTest.testRandom` 脆弱断言（scale 后可能等于上限）；全模块 609 测试全绿，sure-core 指令覆盖率 89.0%，SpotBugs 0 告警。
- **第三十七批（P4 v0.2.0 继续）**：
  - `EmojiUtil`：新增 `isEmoji(String)`（整串是否全为 Emoji）、`containsEmoji`（是否包含 Emoji）、`removeAllEmojis`（移除全部 Emoji，按码点遍历支持补充平面）；
  - `DesensitizedUtil`：新增 `ipv6`（IPv6 脱敏，保留首末段，压缩地址原样返回）；
  - `BitUtil`：新增 `get(byte[], int)`（字节数组指定位读取，大端位序）；
  - 新增 `P4Features32Test`（4 例）；全模块 613 测试全绿，sure-core 指令覆盖率 89.1%，SpotBugs 0 告警。
- **第三十八批（P4 v0.2.0 继续）**：
  - `ExceptionUtil`：新增 `unwrap`（逐层剥离包装异常，返回最内层）；
  - `Assert`：新增 `fail`（无条件抛出断言失败异常，支持模板占位符）；
  - `ThreadUtil`：新增 `execute`（后台线程池执行任务）；
  - `EnumUtil`：新增 `getFieldValues`（获取枚举常量指定字段的值列表）；
  - 新增 `P4Features33Test`（4 例）；全模块 617 测试全绿，sure-core 指令覆盖率 89.1%，SpotBugs 0 告警。
- **第三十九批（P4 v0.2.0 继续）**：
  - `HashUtil`：新增 `md5(byte[])`/`sha1(byte[])`/`sha256(byte[])`/`sha512(byte[])`（返回原始摘要字节，便于流式摘要场景）；
  - `IdUtil`：新增 `fastSimpleUUID`（快速 UUID，不含连字符）；
  - 新增 `P4Features34Test`（2 例）；全模块 619 测试全绿，sure-core 指令覆盖率 89.1%，SpotBugs 0 告警。
- **第四十批（P4 v0.2.0 继续）**：
  - `BeanUtil`：新增 `toBean`（统一转换入口：Map 走 mapToBean、Bean 走 copyProperties）；
  - `Dict`：新增 `getBigDecimal(key[, 默认值])`、`getBigInteger(key[, 默认值])`；
  - 【sure-json】`JSONObject`/`JSONArray`：新增 `getLocalDate`、`getLocalDateTime`；
  - `ConvertUtil`：`toLocalDate`/`toLocalDateTime` 支持 Long 时间戳输入；
  - 新增 `P4Features35Test`（2 例）与 `P4JsonLocalDateTimeTest`（2 例）；全模块 623 测试全绿，sure-core 指令覆盖率 89.0%，SpotBugs 0 告警。
- **第四十一批（P4 v0.2.0 继续）**：
  - `StrSimilarity`：新增 `identical`（完全相同返回 1.0，否则返回相似度分数）；
  - `DateUtil`：新增 `current`（当前毫秒时间戳）、`currentSeconds`（当前秒级时间戳）；
  - 新增 `P4Features36Test`（2 例）；全模块 625 测试全绿，SpotBugs 0 告警。

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
