# 用 suretool 重构你的 Java 工具层：小而全、默认安全、JDK 21+

> 本文是 suretool 的发布介绍文章，可发布于掘金/CSDN/公众号等平台。

## 背景

每个 Java 项目最终都会沉淀一批工具类：字符串处理、日期转换、JSON 序列化、
加解密、缓存、HTTP 调用…… 但这些"自研工具"往往存在三个问题：

1. **质量不可控**：没有测试、没有静态检查、没有安全审计；
2. **重复造轮子**：团队之间各自实现，命名与行为不一致；
3. **默认不安全**：加密用了 ECB/MD5、密钥硬编码，问题被藏在工具里。

开源社区已有成熟方案（如 Hutool），但对部分团队来说，引入全量大型工具库
意味着依赖膨胀与老版本兼容负担。suretool 选择了另一条路：

**只做高频、只支持 JDK 21+、默认安全、零第三方依赖（核心域），并用工程化门禁把质量锁死。**

## 设计理念

### 1. 小而全：11 个业务模块按需引入

| 模块 | 能力 | 代表类 |
| --- | --- | --- |
| sure-core | 字符串/集合/日期/IO/反射/正则/校验等 52 个工具类 | StrUtil、DateUtil、CollUtil |
| sure-json | 轻量 JSON 解析与序列化 | JSONUtil、JSONObject |
| sure-http | HTTP 客户端（超时/TLS/重定向可配） | HttpUtil、HttpRequest |
| sure-crypto | AES-GCM/RSA-OAEP/HMAC/DES(兼容) | AesUtil、RsaUtil、SecureUtil |
| sure-cache | LRU/LFU/FIFO/定时缓存 | LruCache、TimedCache |
| sure-xml | Map/Bean 与 XML 互转 | XmlUtil |
| sure-poi | Excel/Word 读写（POI 5.5） | ExcelUtil、WordUtil |
| sure-captcha / sure-jwt / sure-dfa | 验证码 / JWT / 敏感词 | CaptchaUtil、JwtUtil、DfaUtil |

通过 `sure-bom` 统一版本，`sure-all` 一键全量引入。

### 2. 默认安全：把"踩坑"变成不可能

- `AesUtil` 使用 **AES/GCM/NoPadding** 认证加密，随机 IV，SHA-256 密钥派生；
- `RsaUtil` 使用 **RSA/OAEP-SHA256**，密钥长度低于 2048 直接拒绝；
- `DesUtil` 标记 `@Deprecated`，仅用于解密存量数据；
- 代码扫描（CodeQL）、静态检查（SpotBugs）、覆盖率（JaCoCo）全部进入 CI 门禁。

### 3. 只支持 JDK 21+：用上现代 Java

`maven.compiler.release=21`，意味着代码里可以放心使用 record、密封类、
switch 模式匹配、虚拟线程等特性，不需要为老版本做兼容体操。

### 4. 可验证的质量

- Checkstyle 0 违规（含测试代码）；
- SpotBugs Max/Medium 无新增告警；
- 业务模块行覆盖率 ≥ 85%（core/json/http/crypto/cache/xml 等已达标）；
- License 头、安全响应（SECURITY.md）承诺 24h 确认。

## 快速上手

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.sure</groupId>
            <artifactId>sure-bom</artifactId>
            <version>0.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>com.sure</groupId>
    <artifactId>sure-core</artifactId>
</dependency>
```

```java
String json = JSONUtil.toJsonStr(Map.of("name", "Alice", "age", 30));
String token = JwtUtil.createToken(Map.of("uid", 1001), "secret", 3600);
```

完整示例见仓库 `sure-examples` 模块（14 个可运行 Demo）。

## 路线图

- 覆盖率全模块 ≥ 85% 已达成；下一步发布 0.1.0 到 Maven Central；
- Spring Boot Starter 骨架已就绪（sure-spring-boot-starter）；
- 文档站（GitHub Pages）、对比文档、维护手册均已上线仓库 docs/。

## 结语

suretool 的目标不是替代 Hutool，而是为 **JDK 21+、追求小而可信** 的团队
提供一个开箱即用的选择。欢迎 Star、提 Issue、参与贡献。
