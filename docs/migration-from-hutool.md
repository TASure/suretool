# 从 Hutool 迁移到 suretool

suretool 借鉴 Hutool 的“静态工具方法”形态，但**只支持 JDK 25+**，并且核心域零第三方依赖。
本文给出坐标替换、类/方法映射、行为差异、已知坑与能力缺口。

## 1. 前置条件

| 项 | Hutool | suretool |
| --- | --- | --- |
| JDK | 8 / 11 / 17 / 21 | **25+**（`maven.compiler.release=25`） |
| 包名 | `cn.hutool.*` | `com.sure.tool.*` |
| 运行期依赖 | 每个模块可能有第三方 | 核心域仅 JDK；第三方能力（POI/PDFBox/mail 等）为 `optional` |
| 坐标 | `cn.hutool:hutool-all` | `io.github.tasure:sure-all`（见 `sure-bom` 逐模块引入） |

> JDK 17/21 项目请继续使用 Hutool 或 Apache Commons；suretool 的 `module-info` 与
> Scoped Values / Class-File API 封装要求 JDK 25。

## 2. 依赖替换

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.tasure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>1.19.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 按需引入，不要默认引入 sure-all -->
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
</dependency>
```

引入 `sure-all` 时，POI / PDFBox / jakarta.mail / commons-net / Zxing / commons-compress 等
第三方 jar 不会随传递依赖进入 classpath；用到哪个就显式引入哪个。

## 3. 类映射

| Hutool | suretool | 说明 |
| --- | --- | --- |
| `cn.hutool.core.util.StrUtil` | `com.sure.tool.util.StrUtil` | 空判断、trim、大小写、分割/拼接 |
| `CollUtil` / `MapUtil` / `ListUtil` | `com.sure.tool.collection.*` | 集合、Map、List 辅助 |
| `ArrayUtil` | `com.sure.tool.util.ArrayUtil` | 数组创建、合并、包含 |
| `ObjectUtil` | `com.sure.tool.util.ObjectUtil` | 空安全比较、默认值 |
| `Convert` | `com.sure.tool.util.ConvertUtil` | 宽松类型转换 |
| `BeanUtil` | `com.sure.tool.bean.BeanUtil` | Bean 属性读写、Map 互转 |
| `ReflectUtil` / `ClassUtil` | `com.sure.tool.util.*` | 反射与类加载 |
| `DateUtil` | `com.sure.tool.date.DateUtil` | 仍以 `java.util.Date` 为主，方法签名接近 |
| `IoUtil` / `FileUtil` / `ZipUtil` | `com.sure.tool.io.*` | 流、文件、压缩 |
| `HashUtil` / `Base64Util` / `HexUtil` | `com.sure.tool.codec.*` | 摘要与编码 |
| `Aes` / `SecureUtil` | `com.sure.tool.crypto.AesUtil` / `SecureUtil` | **AES-GCM**，密文格式不同 |
| `JSONUtil` / `JSONObject` | `com.sure.tool.json.JSONUtil` | 自研解析器，数字类型见下 |
| `HttpUtil` / `HttpRequest` | `com.sure.tool.http.*` | 零依赖 HTTP 客户端 |
| `CronUtil` | `com.sure.tool.cron.CronUtil` | Cron 解析与调度 |
| `CacheUtil` | `com.sure.tool.cache.CacheUtil` | LRU / FIFO / Timed / Weak |
| `XmlUtil` | `com.sure.tool.xml.XmlUtil` | JDK XML API 封装 |
| `ExcelUtil` / `WordUtil` | `com.sure.tool.poi.*` | 需显式引入 POI（optional） |
| `PdfUtil` | `com.sure.tool.pdf.PdfUtil` | 需显式引入 PDFBox（optional） |
| `MailUtil` / `FtpUtil` | `com.sure.tool.extra.*` | 需显式引入 mail / commons-net |
| `QrCodeUtil` / `CaptchaUtil` | `com.sure.tool.captcha.*` / `com.sure.tool.extra.QrCodeUtil` | 图形/二维码 |
| `JwtUtil` / `DfaUtil` | `com.sure.tool.jwt.*` / `com.sure.tool.dfa.*` | 独立模块 |
| `TemplateUtil` | `com.sure.tool.template.TemplateUtil` | 需引入 `sure-template` |

## 4. 方法映射（高频）

| Hutool 写法 | suretool 写法 |
| --- | --- |
| `StrUtil.isEmpty(s)` | `StrUtil.isEmpty(s)`（语义一致） |
| `StrUtil.trim(s)` | `StrUtil.trim(s)`（null → null） |
| `StrUtil.toCamelCase(s)` | `StrUtil.toCamelCase(s)` |
| `CollUtil.newArrayList(a, b)` | `CollUtil.newArrayList(a, b)` |
| `CollUtil.distinct(list)` | `CollUtil.distinct(list)` |
| `DateUtil.format(date, "yyyy-MM-dd")` | `DateUtil.format(date, "yyyy-MM-dd")` |
| `DateUtil.parse("2026-11-05")` | `DateUtil.parse("2026-11-05")`（默认日期格式） |
| `SecureUtil.md5(s)` / `sha256(s)` | 同名方法 |
| `JSONUtil.parseObj(json)` | `JSONUtil.parseObj(json)` |
| `JSONUtil.toJsonStr(obj)` | `JSONUtil.toJsonStr(obj)` |
| `JSONUtil.toBean(json, Foo.class)` | `JSONUtil.toBean(json, Foo.class)` |
| `HttpUtil.get(url)` | `HttpUtil.get(url)` |
| `HttpRequest.post(url).body(json).execute()` | 同名链式 API |
| `CacheUtil.newLRUCache(cap)` | `CacheUtil.newLruCache(cap)`（大小写差异） |
| `CronUtil.start()` | `CronUtil.start()` |
| `ExcelUtil.write(...)` | `ExcelUtil.write(file, sheet, rows)`（签名简化，见 Javadoc） |

## 5. 行为差异（必读）

1. **AES 密文格式不兼容**：suretool 固定 `AES/GCM/NoPadding`，密文为 `IV(12B) || ciphertext+tag`；
   Hutool 默认可能是 AES/ECB 或 CBC。已用 Hutool 加密的历史数据不能直接解开，需保留旧实现解密或做数据迁移。
2. **JSON 数字类型**：带小数点/指数的解析为 `BigDecimal`，整数优先 `Long`，溢出为 `BigInteger`；
   Hutool/Jackson 常见为 `Integer`/`Double`。`(Integer) obj.get("id")` 会抛 `ClassCastException`，请用 `Number` 或 `toBean`。
3. **异常语义**：suretool 对 null 入参统一快速失败（`NullPointerException`），非法参数抛
   `IllegalArgumentException`；不返回“魔法值”也不静默吞异常。Hutool 部分方法对 null 更宽容。
4. **DateUtil 仍是 `java.util.Date`**：没有 `java.time` 的 `LocalDateTime` 重载；新代码建议直接用 JDK `java.time`。
5. **HTTP 能力更窄**：覆盖常规 GET/POST、表单、JSON、multipart、Cookie 容器、代理与连接池
   （`HttpClientBuilder`），但不含代理认证、HTTP/2 细粒度调优等高级能力；重度 HTTP 场景请用
   `java.net.http.HttpClient` 或 OkHttp。
6. **null 绑定值与空字符串**：`StrUtil.trimToNull` / `trimToEmpty` 与 Hutool 对齐；但集合工具
   对 null 集合的处理以各方法 Javadoc 为准，不要按 Hutool 经验盲猜。
7. **模块边界**：POI/PDF/Mail/FTP 等能力需要显式依赖；缺少依赖时是编译期错误（`optional` 不传递）。

## 6. 已知坑

- **坐标迁移**：`hutool-all` 与 `sure-all` 不要同时留在 classpath，否则同名工具类容易误 import。
- **包名重写**：IDE 批量替换 `cn.hutool` → `com.sure.tool` 后仍需逐个修方法签名（尤其 `CacheUtil`、`ExcelUtil`）。
- **JDK 25**：Gradle 需 `JavaVersion.VERSION_25`；Maven 确保 `maven.compiler.release=25`。
- **AES 互操作**：与前端/其他语言对接时，对方必须按 `IV || ciphertext` 解析，不能按 Base64 直接解整段。
- **JSON 大整数**：`Long` 溢出会自动升级 `BigInteger`，序列化回字符串时不要强转 `long`。
- **灰度迁移**：建议按模块替换（先 core，再 json/http），保留 Hutool 直到测试覆盖完成。

## 7. suretool 没有的能力

以下 Hutool 能力在 suretool 中**没有对应实现**，迁移时需保留 Hutool 或换其他库：

- JDK 8 / 11 / 17 / 21 兼容（suretool 仅 JDK 25+）。
- Servlet / JSP / Web 容器适配（`hutool-servlet`、`hutool-extra` 的 Web 部分）。
- SSH / SFTP（`hutool-extra` 的 Jsch 封装）。
- 中文分词、拼音转换（`hutool-extra` 的 Tokenizer / Pinyin）。
- HTTP 服务端（`hutool-http` 的 `HttpServer`）。
- 兼容 Hutool 旧版 API 的桥接层（本项目不提供 `cn.hutool` 兼容包）。

未列出的模块请查阅 [模块与 API](modules.md)；若发现缺口，欢迎按 [CONTRIBUTING](../CONTRIBUTING.md) 提 issue 或 PR。
