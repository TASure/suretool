# Migrating from Hutool to suretool

suretool follows Hutool's "static utility methods" style, but it **targets JDK 25+ only** and keeps the
core free of third-party runtime dependencies. This guide covers dependency changes, class and method
mapping, behavioural differences, common traps, and features suretool does not provide.

## 1. Requirements

| | Hutool | suretool |
| --- | --- | --- |
| JDK | 8 / 11 / 17 / 21 | **25+** (`maven.compiler.release=25`) |
| Package prefix | `cn.hutool.*` | `com.sure.tool.*` |
| Runtime dependencies | Some modules pull in third-party jars | Core uses the JDK only; third-party features (POI, PDFBox, mail, etc.) are `optional` |
| Coordinates | `cn.hutool:hutool-all` | `io.github.tasure:sure-all`, or one module at a time via `sure-bom` |

> On JDK 17/21, keep using Hutool or Apache Commons. suretool's `module-info` and its
> Scoped Values / Class-File API wrappers require JDK 25.

## 2. Replace the dependency

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.tasure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>1.20.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Add only the modules you use; avoid sure-all by default. -->
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
</dependency>
```

With `sure-all`, third-party jars such as POI, PDFBox, jakarta.mail, commons-net, Zxing and
commons-compress are **not** pulled in transitively. Add them explicitly when you need them.

## 3. Class mapping

| Hutool | suretool | Notes |
| --- | --- | --- |
| `cn.hutool.core.util.StrUtil` | `com.sure.tool.util.StrUtil` | Emptiness, trim, case, split/join |
| `CollUtil` / `MapUtil` / `ListUtil` | `com.sure.tool.collection.*` | Collection, map and list helpers |
| `ArrayUtil` | `com.sure.tool.util.ArrayUtil` | Array creation, concat, contains |
| `ObjectUtil` | `com.sure.tool.util.ObjectUtil` | Null-safe comparison and defaults |
| `Convert` | `com.sure.tool.util.ConvertUtil` | Lenient type conversion |
| `BeanUtil` | `com.sure.tool.bean.BeanUtil` | Bean properties and map conversion |
| `ReflectUtil` / `ClassUtil` | `com.sure.tool.util.*` | Reflection and class loading |
| `DateUtil` | `com.sure.tool.date.DateUtil` | Still `java.util.Date`-based, similar signatures |
| `IoUtil` / `FileUtil` / `ZipUtil` | `com.sure.tool.io.*` | Streams, files, archives |
| `HashUtil` / `Base64Util` / `HexUtil` | `com.sure.tool.codec.*` | Digests and encodings |
| `Aes` / `SecureUtil` | `com.sure.tool.crypto.AesUtil` / `SecureUtil` | **AES-GCM**; ciphertext format differs |
| `JSONUtil` / `JSONObject` | `com.sure.tool.json.JSONUtil` | Custom parser; see number types below |
| `HttpUtil` / `HttpRequest` | `com.sure.tool.http.*` | Zero-dependency HTTP client |
| `CronUtil` | `com.sure.tool.cron.CronUtil` | Cron parsing and scheduling |
| `CacheUtil` | `com.sure.tool.cache.CacheUtil` | LRU / FIFO / timed / weak |
| `XmlUtil` | `com.sure.tool.xml.XmlUtil` | JDK XML APIs |
| `ExcelUtil` / `WordUtil` | `com.sure.tool.poi.*` | Requires explicit POI (optional) |
| `PdfUtil` | `com.sure.tool.pdf.PdfUtil` | Requires explicit PDFBox (optional) |
| `MailUtil` / `FtpUtil` | `com.sure.tool.extra.*` | Requires explicit mail / commons-net |
| `QrCodeUtil` / `CaptchaUtil` | `com.sure.tool.extra.QrCodeUtil` / `com.sure.tool.captcha.CaptchaUtil` | Images and QR codes |
| `JwtUtil` / `DfaUtil` | `com.sure.tool.jwt.*` / `com.sure.tool.dfa.*` | Dedicated modules |
| `TemplateUtil` | `com.sure.tool.template.TemplateUtil` | Add `sure-template` |

## 4. Method mapping (frequently used)

| Hutool | suretool |
| --- | --- |
| `StrUtil.isEmpty(s)` | `StrUtil.isEmpty(s)` — same semantics |
| `StrUtil.trim(s)` | `StrUtil.trim(s)` — null in, null out |
| `StrUtil.toCamelCase(s)` | `StrUtil.toCamelCase(s)` |
| `CollUtil.newArrayList(a, b)` | `CollUtil.newArrayList(a, b)` |
| `CollUtil.distinct(list)` | `CollUtil.distinct(list)` |
| `DateUtil.format(date, "yyyy-MM-dd")` | `DateUtil.format(date, "yyyy-MM-dd")` |
| `DateUtil.parse("2026-11-05")` | `DateUtil.parse("2026-11-05")` — default date pattern |
| `SecureUtil.md5(s)` / `sha256(s)` | Same method names |
| `JSONUtil.parseObj(json)` | `JSONUtil.parseObj(json)` |
| `JSONUtil.toJsonStr(obj)` | `JSONUtil.toJsonStr(obj)` |
| `JSONUtil.toBean(json, Foo.class)` | `JSONUtil.toBean(json, Foo.class)` |
| `HttpUtil.get(url)` | `HttpUtil.get(url)` |
| `HttpRequest.post(url).body(json).execute()` | Same fluent API |
| `CacheUtil.newLRUCache(cap)` | `CacheUtil.newLruCache(cap)` — casing differs |
| `CronUtil.start()` | `CronUtil.start()` |
| `ExcelUtil.write(...)` | `ExcelUtil.write(file, sheet, rows)` — simplified signature; see Javadoc |

## 5. Behavioural differences

1. **AES ciphertext is not compatible.** suretool always uses `AES/GCM/NoPadding` and emits
   `IV(12 bytes) || ciphertext+tag`. Hutool may default to AES/ECB or CBC. Data encrypted with Hutool
   cannot be decrypted by suretool directly — keep the legacy decryptor or re-encrypt during migration.
2. **JSON number types.** Decimals and exponents become `BigDecimal`; integers prefer `Long` and fall
   back to `BigInteger` on overflow. Hutool/Jackson often use `Integer`/`Double`. Casting
   `(Integer) obj.get("id")` throws `ClassCastException`; use `Number` or `toBean`.
3. **Exception semantics.** suretool fails fast: `null` arguments throw `NullPointerException`, invalid
   arguments throw `IllegalArgumentException`. It never returns magic values or swallows failures;
   Hutool is more forgiving with `null` in places.
4. **DateUtil still returns `java.util.Date`.** There are no `LocalDateTime` overloads; prefer JDK
   `java.time` for new code.
5. **HTTP is intentionally smaller.** It covers GET/POST, forms, JSON, multipart, cookie stores,
   proxies and connection pooling (`HttpClientBuilder`), but not proxy authentication or fine-grained
   HTTP/2 tuning. For heavy HTTP work use `java.net.http.HttpClient`
   or OkHttp.
6. **Null values and empty strings.** `StrUtil.trimToNull` / `trimToEmpty` match Hutool, but collection
   helpers document their own `null` behaviour — do not assume Hutool's defaults.
7. **Module boundaries.** POI, PDF, mail and FTP require explicit dependencies; when absent you get a
   compile-time error, because `optional` dependencies are not transitive.

## 6. Common traps

- **Coordinates.** Do not keep `hutool-all` and `sure-all` on the classpath together; duplicate
  utility class names lead to the wrong import.
- **Package rewrite.** A global `cn.hutool` → `com.sure.tool` replacement still needs per-method fixes
  (notably `CacheUtil` and `ExcelUtil`).
- **JDK 25.** Gradle users need `JavaVersion.VERSION_25`; Maven users need
  `maven.compiler.release=25`.
- **AES interop.** Front ends or services in other languages must parse `IV || ciphertext`, not decrypt
  the whole Base64 blob.
- **Big integers.** JSON `Long` overflow silently upgrades to `BigInteger`; do not force-cast to
  `long`.
- **Incremental migration.** Replace one module at a time (core first, then json/http) and keep Hutool
  until your tests are green.

## 7. What suretool does not provide

The following Hutool capabilities have **no suretool counterpart**. Keep Hutool or pick another
library for them:

- JDK 8 / 11 / 17 / 21 compatibility (suretool requires JDK 25+).
- Servlet / JSP / web-container adapters (`hutool-servlet` and the web parts of `hutool-extra`).
- SSH / SFTP (Hutool's Jsch wrapper).
- Chinese word segmentation and pinyin conversion (Hutool's Tokenizer / Pinyin).
- An HTTP server (`hutool-http`'s `HttpServer`).
- A `cn.hutool` compatibility bridge (suretool ships no legacy package aliases).

For modules not listed here, see the [module guide](modules.md). If you find a gap, open an issue or a
PR following [CONTRIBUTING](../CONTRIBUTING.md).
