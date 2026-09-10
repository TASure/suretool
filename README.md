# suretool

![CI](https://img.shields.io/github/actions/workflow/status/TASure/suretool/ci.yml?branch=main&label=CI)
![Coverage](https://img.shields.io/codecov/c/github/TASure/suretool)
![Release](https://img.shields.io/github/v/release/TASure/suretool)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Java](https://img.shields.io/badge/Java-8+-orange)
![Stars](https://img.shields.io/github/stars/TASure/suretool)

一个**小而全**的 Java 工具类库，参考 [Hutool](https://doc.hutool.cn/pages/index/) 的设计理念，通过静态方法封装常用 JDK API，减少重复造轮子、降低开发成本。

- 包前缀：`com.sure.tool`
- 语言：Java 8+（无任何第三方运行期依赖，仅测试依赖 JUnit）
- 许可：Apache License 2.0

## 快速开始

### Maven 引入

```xml
<dependency>
    <groupId>com.sure</groupId>
    <artifactId>suretool</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

先本地安装：

```bash
mvn clean install
```

### 模块与工具类

| 包 | 工具类 | 说明 |
| --- | --- | --- |
| `com.sure.tool.util` | `StrUtil` | 字符串判空/去空白/截取/拼接/格式化/驼峰转换 |
| | `CharUtil` | 字符分类判断（字母/数字/空白/十六进制） |
| | `ArrayUtil` | 数组判空/包含/拼接/反转/去重/截取（支持基本类型） |
| | `NumberUtil` | 安全数值解析/高精度四则运算/四舍五入/格式化 |
| | `BooleanUtil` | 多种字符串形式解析布尔值（true/yes/1/是…） |
| | `ObjectUtil` | 对象判空/深比较/克隆/序列化 |
| | `RandomUtil` | 随机数/随机字符串/随机 UUID |
| | `ReUtil` | 正则匹配/提取分组/查找/替换 |
| | `IdUtil` | UUID、ObjectId、雪花 ID |
| | `ClassUtil` | 类加载/实例化/基本类型判断 |
| | `ReflectUtil` | 反射获取/设置字段、调用方法 |
| | `ConvertUtil` | 类型转换（字符串↔基础类型/数组/集合） |
| | `DesensitizedUtil` | 手机号/身份证/银行卡/邮箱/姓名脱敏 |
| | `CharsetUtil` | 字符集常量与编码转换 |
| | `ValidatorUtil` | 邮箱/手机号/身份证/IP/URL/车牌/邮编等校验 |
| | `IdcardUtil` | 身份证校验、15↔18 位转换、生日/性别解析 |
| | `EscapeUtil` | HTML 转义与反转义（防注入） |
| | `SystemUtil` | 系统属性与 JVM 内存信息 |
| | `RuntimeUtil` | 执行系统命令并读取输出 |
| | `NetUtil` | 本机 IP/主机名、内网地址判断、端口检查 |
| `com.sure.tool.codec` | `Base64Util` | Base64 / URL 安全 Base64 编解码 |
| | `HexUtil` | 十六进制编解码 |
| | `HashUtil` | MD5 / SHA-1 / SHA-256 / SHA-512 / CRC32 |
| | `EncodeUtil` | URL 百分号编解码 |
| `com.sure.tool.collection` | `CollUtil` | 集合判空/交并差/分组/过滤/映射 |
| | `ListUtil` | 列表切分/分页/反转 |
| | `MapUtil` | Map 创建/取值/反转/拼接 |
| `com.sure.tool.date` | `DateUtil` | 日期格式化/解析/偏移/年龄/区间 |
| | `DateUnit` | 日期时间单位枚举 |
| `com.sure.tool.io` | `FileUtil` | 文件读写/复制/移动/删除/大小/路径规范化 |
| | `IoUtil` | 流复制/读取/写入/安静关闭 |
| | `ZipUtil` | ZIP 压缩/解压（含 zip-slip 防护） |
| `com.sure.tool.lang` | `Assert` | 断言工具（非空/为真/正则匹配） |
| | `Snowflake` | 雪花算法 ID 生成器 |
| | `PatternPool` | 常用正则模式池 |
| | `StopWatch` | 秒表计时器 |
| | `Console` | 控制台格式化打印 |
| | `Dict` | 便捷字典（类型化取值） |
| `com.sure.tool.bean` | `BeanUtil` | 属性拷贝（含类型转换）/Bean↔Map/属性读写 |
| | `BeanDesc` | Bean 属性描述（getter/setter/字段扫描缓存） |
| | `FieldUtil` | 字段遍历/查找/常量读取 |
| `com.sure.tool.json` | `JSONUtil` | 零依赖 JSON 解析/序列化/Bean 互转 |
| | `JSONObject` | JSON 对象（链式设置、类型化读取） |
| | `JSONArray` | JSON 数组（类型化读取） |
| `com.sure.tool.thread` | `ThreadUtil` | 异步执行/休眠/线程工厂 |
| | `ExecutorBuilder` | 线程池构造器 |
| | `SyncFinisher` | 并发同步器（固定线程并发+等待） |
| `com.sure.tool.http` | `HttpUtil` | 零依赖 HTTP 客户端（GET/POST/JSON/下载/超时） |
| | `URLUtil` | URL 域名/路径/参数提取与拼接 |
| | `HttpException` | HTTP 异常（携带状态码） |
| `com.sure.tool.crypto` | `SecureUtil` | 安全门面：哈希/AES/DES/RSA/HMAC/随机密钥 |
| | `AesUtil` | AES-CBC 加解密（hex/Base64 双输出） |
| | `DesUtil` | DES 加解密（兼容旧系统） |
| | `RsaUtil` | RSA 密钥对生成/加解密/密钥序列化 |
| | `HmacUtil` | HMAC-MD5/SHA1/SHA256/SHA512 |

## 使用示例

```java
import com.sure.tool.collection.CollUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.io.FileUtil;
import com.sure.tool.lang.Snowflake;
import com.sure.tool.util.*;

import java.util.Date;

public class Demo {

    public static void main(String[] args) throws Exception {
        // 字符串
        StrUtil.isBlank("  ");              // true
        StrUtil.toCamelCase("user_name");   // userName
        StrUtil.format("你好，{}", "世界");    // 你好，世界

        // 数值
        NumberUtil.div(10, 3, 2);           // 3.33
        NumberUtil.round(3.14159, 2);       // 3.14

        // 日期
        DateUtil.format(DateUtil.now());    // 2026-09-09 16:30:00
        DateUtil.age(DateUtil.parse("2000-05-20"));  // 26

        // 集合
        CollUtil.union(CollUtil.newArrayList(1, 2), CollUtil.newArrayList(2, 3));  // [1, 2, 3]

        // 脱敏
        DesensitizedUtil.mobilePhone("13812345678");   // 138****5678

        // 加密
        HashUtil.sha256Hex("abc");

        // 文件
        FileUtil.writeUtf8String("hello", new java.io.File("/tmp/a.txt"));
        FileUtil.readUtf8String(new java.io.File("/tmp/a.txt"));

        // ID
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        snowflake.nextId();  // 雪花 ID

        // 校验
        ValidatorUtil.isEmail("test@example.com");   // true
        ValidatorUtil.isMobile("13800138000");       // true

        // 身份证
        IdcardUtil.getBirthDate("11010119900307123X");  // 1990-03-07

        // 压缩
        ZipUtil.zip("/tmp/dir", "/tmp/out.zip");
        ZipUtil.unzip("/tmp/out.zip", "/tmp/out");
    }
}
```

## 构建与测试

```bash
# 编译并运行全部单元测试
mvn test

# 安装到本地仓库
mvn clean install
```

测试覆盖：字符串、数值、集合、日期、文件 IO、编解码、反射、断言、雪花 ID、校验、压缩、计时、字典等全部工具类。

## 设计参考

本项目的 API 设计参考 [Hutool](https://doc.hutool.cn/pages/index/)（[Gitee 仓库](https://gitee.com/chinabugotech/hutool)），实现为独立编写的原始代码，无代码复制。

## License

[Apache License 2.0](LICENSE)
