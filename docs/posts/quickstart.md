# 5 分钟上手 suretool

> 面向新用户的快速入门。目标：5 分钟内跑通第一个 suretool 程序。

## 1. 引入依赖

suretool 已发布到 Maven Central（groupId `io.github.tasure`），核心模块零第三方运行期依赖：

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

按需引入其他模块：`sure-json`（JSON）、`sure-http`（HTTP）、`sure-crypto`（加解密）、`sure-cache`（缓存）、`sure-xml`、`sure-poi`（Excel/Word）等。

## 2. 第一个例子

```java
import com.sure.tool.util.StrUtil;
import com.sure.tool.util.DateUtil;
import com.sure.tool.collection.CollUtil;
import com.sure.tool.json.JSONUtil;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) {
        // 字符串
        String name = "  Hello, suretool  ";
        System.out.println(StrUtil.trim(name));              // Hello, suretool
        System.out.println(StrUtil.isBlank(name));           // false
        System.out.println(StrUtil.toCamelCase("user_name")); // userName

        // 集合
        List<Integer> list = CollUtil.newArrayList(3, 1, 2);
        System.out.println(CollUtil.distinct(list));         // [3, 1, 2]
        System.out.println(CollUtil.isEmpty(list));           // false

        // 日期
        System.out.println(DateUtil.format(new Date(), "yyyy-MM-dd"));

        // JSON
        String json = JSONUtil.toJsonStr(list);
        System.out.println(json);                            // [3,1,2]
    }
}
```

## 3. 常用入口速查

| 领域 | 工具类 | 常用方法 |
| --- | --- | --- |
| 字符串 | `StrUtil` | trim/isBlank/sub/repeat/format/toCamelCase |
| 集合 | `CollUtil` | isEmpty/newArrayList/distinct/union/intersection/groupByKey |
| 数组 | `ArrayUtil` | isEmpty/contains/reverse/distinct/join/min/max |
| 日期 | `DateUtil` | format/parse/now/offset |
| 文件 | `FileUtil` | read/write/copy/move |
| JSON | `JSONUtil` | toJsonStr/parseObj/toBean |
| 加密 | `SecureUtil` | md5/sha256/aesEncryptHex/hmacSha256 |
| 校验 | `ValidatorUtil` | isEmail/isMobile/isIdCard |

## 4. 环境要求

- **JDK 21+**（suretool 仅支持 JDK 21 及以上，全部写法均为现代 JDK 特性，不做老版本兼容）
- Maven 3.8+ / Gradle 8+

> 完整 API 见 [文档站](https://tasure.github.io/suretool/)；示例工程见 [sure-examples](../../sure-examples/README.md)。
