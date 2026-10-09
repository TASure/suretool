# 模块与 API 门户（Modules & API Portal）

> 28 个模块全览：坐标 / 主包 / 职责 / 对标 / 关键类。
> API 文档：javadoc.io 直链（随 Maven Central 自动生成）；源码：GitHub `main` 分支。
> 使用约定：`sure-bom` 统一版本管理；`sure-all` 聚合全部模块；按需引入更轻。

| 模块 Module | 坐标 Coordinates | 主包 Package | 能力 Capability | 对标 Reference | API (javadoc.io) |
| --- | --- | --- | --- | --- | --- |
| sure-core | `io.github.tasure:sure-core` | `com.sure.tool` | 核心工具域：字符串/集合/IO/日期/反射/并发/图/统计 | Hutool core / Guava | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-core/latest/index.html) |
| sure-log | `io.github.tasure:sure-log` | `com.sure.tool.log` | 零依赖日志门面（SLF4J 自动委托 / Console 兜底） | slf4j 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-log/latest/index.html) |
| sure-db | `io.github.tasure:sure-db` | `com.sure.tool.db` | JDBC 数据访问（Entity / SqlRunner / 连接池 / 事务 / 分页） | commons-dbutils | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-db/latest/index.html) |
| sure-script | `io.github.tasure:sure-script` | `com.sure.tool.script` | JSR-223 脚本门面（引擎探测 / 编译缓存 / 快速求值） | commons-jexl 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-script/latest/index.html) |
| sure-template | `io.github.tasure:sure-template` | `com.sure.tool.template` | 模板引擎（Template / SimpleTemplate / Bean 渲染） | commons-text StrSubstitutor | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-template/latest/index.html) |
| sure-aop | `io.github.tasure:sure-aop` | `com.sure.tool.aop` | 零依赖 AOP（Aspect / ProxyUtil / 方法级匹配） | Spring AOP 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-aop/latest/index.html) |
| sure-pdf | `io.github.tasure:sure-pdf` | `com.sure.tool.pdf` | PDF 轻量门面（PdfWriter / PdfUtil，PDFBox 3） | Apache PDFBox 门面 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-pdf/latest/index.html) |
| sure-process | `io.github.tasure:sure-process` | `com.sure.tool.process` | 进程门面（多重载 / 超时强杀 / 虚拟线程防管道死锁） | commons-exec | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-process/latest/index.html) |
| sure-math | `io.github.tasure:sure-math` | `com.sure.tool.math` | 数学与统计（MathUtil / BigDecimalUtil / StatUtil / RandomUtil） | commons-math 高频子集 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-math/latest/index.html) |
| sure-compress | `io.github.tasure:sure-compress` | `com.sure.tool.compress` | 高压缩率门面（SevenZ/LZMA2 防路径穿越 / Brotli 解码检测） | commons-compress | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-compress/latest/index.html) |
| sure-socket | `io.github.tasure:sure-socket` | `com.sure.tool.socket` | TCP 门面（SocketUtil / SocketServer 虚拟线程 / SocketClient） | Netty 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-socket/latest/index.html) |
| sure-event | `io.github.tasure:sure-event` | `com.sure.tool.event` | 事件总线（函数式/注解注册、同步/虚拟线程异步、DeadEvent） | Guava EventBus | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-event/latest/index.html) |
| sure-json | `io.github.tasure:sure-json` | `com.sure.tool.json` | JSON 解析 / 序列化 / Bean 互转 / JSONPath | Gson/Jackson 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-json/latest/index.html) |
| sure-http | `io.github.tasure:sure-http` | `com.sure.tool.http` | HTTP 客户端（Cookie / 代理 / 连接池 / multipart 多文件） | Apache HttpClient 门面 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-http/latest/index.html) |
| sure-crypto | `io.github.tasure:sure-crypto` | `com.sure.tool.crypto` | 哈希 / AES-GCM / RSA-OAEP / HMAC / HKDF / ML-KEM / ECDH | bouncycastle 安全基线 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-crypto/latest/index.html) |
| sure-cron | `io.github.tasure:sure-cron` | `com.sure.tool.cron` | Cron 七段表达式 / 虚拟线程调度器 | Quartz 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-cron/latest/index.html) |
| sure-cache | `io.github.tasure:sure-cache` | `com.sure.tool.cache` | FIFO / LRU / LFU / Timed / Weak 本地缓存 | Caffeine 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-cache/latest/index.html) |
| sure-xml | `io.github.tasure:sure-xml` | `com.sure.tool.xml` | XML 与 Map / Bean 互转 | commons-jxpath 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-xml/latest/index.html) |
| sure-poi | `io.github.tasure:sure-poi` | `com.sure.tool.poi` | Excel / Word 读写（POI 5.5 门面） | Apache POI 门面 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-poi/latest/index.html) |
| sure-extra | `io.github.tasure:sure-extra` | `com.sure.tool.extra` | 邮件 SMTP / FTP / 二维码 / 图像处理 | commons-email 等门面 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-extra/latest/index.html) |
| sure-captcha | `io.github.tasure:sure-captcha` | `com.sure.tool.captcha` | 图形验证码（线段 / 圆圈 / 扭曲） | hutool-captcha 对齐 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-captcha/latest/index.html) |
| sure-jwt | `io.github.tasure:sure-jwt` | `com.sure.tool.jwt` | JWT 签发 / 校验（HS / RS） | jjwt 轻量替代 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-jwt/latest/index.html) |
| sure-dfa | `io.github.tasure:sure-dfa` | `com.sure.tool.dfa` | 敏感词过滤（前缀树 / 停用词） | hutool-dfa 对齐 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-dfa/latest/index.html) |
| sure-bom | `io.github.tasure:sure-bom` | — | BOM 统一版本管理 | Spring Boot BOM 模式 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-bom/latest/index.html) |
| sure-all | `io.github.tasure:sure-all` | — | 聚合模块（全部依赖） | hutool-all | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-all/latest/index.html) |
| sure-benchmark | `io.github.tasure:sure-benchmark` | `com.sure.tool.benchmark` | JMH 基准测试（性能指标 vs Hutool/Guava） | JMH | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-benchmark/latest/index.html) |
| sure-examples | `io.github.tasure:sure-examples` | `com.sure.tool.examples` | 可运行示例（19+ Demo） | — | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-examples/latest/index.html) |
| sure-spring-boot-starter | `io.github.tasure:sure-spring-boot-starter` | `com.sure.tool.spring.boot` | Spring Boot 自动装配入口 | spring-boot-starter 模式 | [javadoc](https://javadoc.io/doc/io.github.tasure/sure-spring-boot-starter/latest/index.html) |

## 链接速查（Links）

| 资源 | 地址 |
| --- | --- |
| 项目主页 | https://github.com/TASure/suretool |
| 文档站（Pages） | https://tasure.github.io/suretool/ |
| 聚合 API（自托管） | https://tasure.github.io/suretool/api/ |
| Maven Central | https://central.sonatype.com/artifact/io.github.tasure/sure-core |
| mvnrepository | https://mvnrepository.com/artifact/io.github.tasure/sure-core |
| 教程（5 分钟上手） | [docs/posts/quickstart.md](posts/quickstart.md) |
| 版本发布说明 | [CHANGELOG](../CHANGELOG.md) |
