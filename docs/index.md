# suretool 文档

suretool 是一个**小而全**的 Java 工具类库，参考 [Hutool](https://hutool.cn) 设计，
仅支持 **JDK 21+**，包前缀 `com.sure.tool`。

- [项目主页（GitHub）](https://github.com/TASure/suretool)
- [贡献指南](../CONTRIBUTING.md)
- [安全策略](../SECURITY.md)
- [发布指南](RELEASING.md)
- [与 Hutool 对比](comparison-hutool.md)
- [维护者手册](MAINTAINING.md)

## 模块与类索引

### sure-core（基础核心，52 个工具类）

| 分类 | 类 |
| --- | --- |
| Bean / 反射 | BeanDesc、BeanUtil、FieldUtil、PropDesc、ReflectUtil、ClassUtil |
| 编码 / 哈希 | Base64Util、EncodeUtil、HashUtil、HexUtil、EscapeUtil |
| 集合 | BiMap、CaseInsensitiveMap、CollUtil、CsvUtil、ListUtil、MapUtil、OrderedMap、StrJoiner、TreeNode、TreeUtil |
| 日期 / 时间 | DateUnit、DateUtil、StopWatch |
| IO / 文件 | FileUtil、IoUtil、ZipUtil |
| 断言 / 控制台 | Assert、Console、Dict |
| 正则 / ID | PatternPool、ReUtil、IdcardUtil、IdUtil |
| 并发 / 线程 | ExecutorBuilder、LockUtil、SyncFinisher、ThreadUtil |
| 类型转换 / 基础 | ArrayUtil、BooleanUtil、CharsetUtil、CharUtil、ConvertUtil、NumberUtil、ObjectUtil、StrSplitter、StrUtil、SystemUtil |
| 脱敏 / 校验 / 其他 | DesensitizedUtil、NetUtil、RandomUtil、RuntimeUtil、Snowflake、ValidatorUtil |

### sure-json（JSON 解析与序列化）

`JSONObject`、`JSONArray`、`JSONUtil`、`JSONException`

### sure-http（HTTP 客户端）

`HttpRequest`、`HttpResponse`、`HttpUtil`、`URLUtil`、`HttpException`

### sure-crypto（加密解密，默认安全基线）

`AesUtil`（AES-GCM）、`DesUtil`（仅兼容存量，@Deprecated）、`HmacUtil`、`RsaUtil`（RSA-OAEP）、
`SecureUtil`（统一入口）、`CryptoException`

### sure-cron（定时任务）

`CronPattern`、`CronUtil`

### sure-cache（缓存实现）

`Cache`（接口）、`CacheUtil`、`FifoCache`、`LfuCache`、`LruCache`、`TimedCache`

### sure-xml（XML 互转）

`XmlUtil`、`XmlException`

### sure-poi（Office 文档，基于 Apache POI 5.5.1）

`ExcelUtil`、`WordUtil`、`PoiUtil`

### sure-captcha（验证码）

`Captcha`、`AbstractCaptcha`、`CaptchaUtil`、`CircleCaptcha`、`LineCaptcha`、`ShearCaptcha`

### sure-jwt（JWT）

`JwtUtil`、`JWT`、`JWTException`

### sure-dfa（敏感词过滤）

`DfaUtil`、`WordTree`、`FoundWord`

## 快速开始

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-all</artifactId>
    <version>0.1.0</version>
</dependency>
```

```java
import com.sure.tool.util.StrUtil;

String joined = StrUtil.join(",", "a", "b", "c"); // a,b,c
```

完整用法示例见 [sure-examples](../sure-examples) 模块。
