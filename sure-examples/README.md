# suretool 示例（sure-examples）

纯 JDK 运行示例，覆盖核心与扩展模块常用 API。运行任一示例：

```bash
mvn -pl sure-examples -am compile
java -cp sure-examples/target/classes:$(cat sure-examples/target/cp.txt 2>/dev/null || echo "见下方") com.sure.tool.example.CollUtilDemo
```

> 简便方式：在 IDE 中直接运行 `com.sure.tool.example.*Demo` 的 `main` 方法。

## 示例清单

| 示例 | 模块 | 说明 |
| --- | --- | --- |
| `CollUtilDemo` | sure-core | 集合工具链式操作 |
| `DateUtilDemo` | sure-core | 日期解析/格式化/时间差 |
| `CryptoDemo` | sure-crypto | SM3/SM4/AES 加解密 |
| `CaptchaDemo` | sure-captcha | 线条验证码生成与校验 |
| `CacheDemo` | sure-cache | 本地缓存/过期策略 |
| `DfaDemo` | sure-dfa | 敏感词过滤（DFA） |
| `ExamplesRunner` | 全部 | 一键顺序运行全部示例 |

## Spring Boot 集成（sure-spring-boot-starter）

在 Spring Boot 3.x 应用中引入 starter 后自动装配验证码服务：

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

`application.yml`：

```yaml
suretool:
  captcha:
    enabled: true      # 默认开启
    width: 200
    height: 80
```

注入使用：

```java
@Service
public class RegisterService {
    private final CaptchaGenerator captchaGenerator;   // com.sure.tool.boot.CaptchaGenerator

    public RegisterService(CaptchaGenerator captchaGenerator) {
        this.captchaGenerator = captchaGenerator;
    }

    public LineCaptcha createCaptcha() {
        return captchaGenerator.generate();
    }
}
```

关闭验证码 Bean：`suretool.captcha.enabled=false`。
