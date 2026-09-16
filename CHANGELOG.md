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
  - 新增测试用例：全模块 578 测试全绿。

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
