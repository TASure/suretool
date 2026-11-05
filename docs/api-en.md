# Top 50 APIs — English Javadoc coverage

Each class below carries an English summary paragraph in its class-level Javadoc (generated docs:
[javadoc.io](https://javadoc.io/doc/io.github.tasure/sure-core/latest/index.html)).
The list is the 50 most-used public entry points across modules.

## Core — strings, collections, objects

| API | English summary |
| --- | --- |
| `StrUtil` | String utilities: emptiness checks, trimming, splitting, joining, casing and formatting. |
| `CollUtil` | Collection helpers for creating, filtering, joining and comparing lists, sets and maps. |
| `MapUtil` | Map construction and lookup helpers with typed getters and null-safe operations. |
| `ListUtil` | List-specific helpers such as partitioning, splitting and safe indexing. |
| `IterUtil` | Iterator and stream convenience methods, including null-safe traversal and joining. |
| `ArrayUtil` | Array creation, concatenation, containment and conversion helpers. |
| `ObjectUtil` | Null-safe object comparison, defaulting and type inspection helpers. |
| `ConvertUtil` | Best-effort type conversion between strings, numbers, booleans and enums. |
| `BeanUtil` | JavaBean property reading, writing and map-to-bean conversion. |
| `ReflectUtil` | Reflection helpers for fields, methods and constructors with null-safe lookups. |
| `ClassUtil` | Class loading, type checks and package-name helpers. |

## Core — numbers, characters, identity

| API | English summary |
| --- | --- |
| `NumberUtil` | Number parsing, rounding, formatting and range checks. |
| `BooleanUtil` | Boolean parsing and conversion helpers that treat null as false where appropriate. |
| `CharUtil` | Character classification and conversion helpers. |
| `CharsetUtil` | Charset lookup and encoding/decoding helpers. |
| `RandomUtil` | Random string, number and element selection helpers. |
| `IdUtil` | Identifier generation: UUIDs, snowflake IDs and ObjectIds. |
| `Snowflake` | A thread-safe snowflake ID generator with configurable worker and datacenter IDs. |
| `ValidatorUtil` | Common input validators for phone numbers, emails, URLs and more. |
| `IdcardUtil` | Chinese ID-card validation and field extraction helpers. |
| `DesensitizedUtil` | Data masking helpers for phone numbers, IDs, emails and bank cards. |

## Core — dates, IO, text

| API | English summary |
| --- | --- |
| `DateUtil` | Date and time parsing, formatting and arithmetic for `java.util.Date` values. |
| `StopWatch` | A monotonic stopwatch for measuring elapsed time in milliseconds and formatted strings. |
| `FileUtil` | File creation, reading, copying, moving and deletion helpers with clear null and exception semantics. |
| `IoUtil` | Stream and reader/writer helpers for copying, reading and closing resources safely. |
| `ZipUtil` | ZIP compression and extraction helpers. |
| `ResourceUtil` | Classpath and file-system resource loading helpers. |
| `ReUtil` | Regular-expression extraction, matching and replacement helpers. |
| `PatternPool` | Reusable, precompiled regex patterns for common formats. |

## Core — codecs, crypto primitives

| API | English summary |
| --- | --- |
| `HashUtil` | MD5, SHA-1, SHA-256 and HMAC hashing helpers. |
| `Base64Util` | Base64 encoding and decoding for strings and byte arrays. |
| `HexUtil` | Hexadecimal encoding and decoding helpers. |
| `EncodeUtil` | URL and HTML encoding/decoding helpers. |

## Core — network, system, threads

| API | English summary |
| --- | --- |
| `NetUtil` | Network helpers for local addresses, IP checks, port checks and CIDR matching. |
| `UrlUtil` | URL parsing, encoding and query-parameter helpers. |
| `SystemUtil` | Runtime and operating-system information helpers. |
| `RuntimeUtil` | Process execution helpers that capture output as a string or lines. |
| `ThreadUtil` | Thread creation, sleeping, joining and virtual-thread helpers. |
| `ExecutorBuilder` | A fluent builder for configuring thread-pool executors. |
| `LockUtil` | Lock and synchronization helpers that always release safely. |
| `SyncFinisher` | A fork/join-style task coordinator with a completion barrier. |

## Modules — JSON, HTTP, crypto, scheduling, cache, XML, captcha

| API | English summary |
| --- | --- |
| `JSONUtil` | JSON parsing and serialization to and from strings, maps and POJOs. |
| `HttpUtil` | A zero-dependency HTTP client facade for GET, POST and form requests. |
| `HttpRequest` | A fluent HTTP request builder with headers, query parameters and body handling. |
| `SecureUtil` | Unified entry point for secure-by-default cryptography providers. |
| `AesUtil` | AES-GCM encryption and decryption with authenticated ciphertext. |
| `CronUtil` | Cron expression parsing and scheduling helpers. |
| `CacheUtil` | Factory and convenience methods for the built-in cache implementations. |
| `XmlUtil` | XML parsing and serialization helpers built on the JDK XML APIs. |
| `CaptchaUtil` | Captcha generation helpers for line, circle and shear styles. |
