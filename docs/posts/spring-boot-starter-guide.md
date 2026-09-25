# 用 sure-spring-boot-starter 开箱即用

> 目标：Spring Boot 3.x 项目中一行配置接入 suretool 的常用组件（验证码等）。

## 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. 默认装配

引入依赖后，自动配置生效，可直接注入：

```java
import com.sure.tool.boot.CaptchaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    @Autowired
    private CaptchaGenerator captchaGenerator;

    public String createCaptcha() {
        return captchaGenerator.generate().getImageBase64();  // 生成 Base64 验证码图片
    }

    public boolean verifyCaptcha(String userInputCode) {
        return captchaGenerator.generate().verify(userInputCode); // 校验（忽略大小写）
    }
}
```

## 3. 配置项（application.yml）

```yaml
suretool:
  captcha:
    width: 320          # 默认 200
    height: 120         # 默认 100
    enabled: true       # false 时关闭自动装配（Bean 不注册）
```

## 4. 关闭自动装配

不需要 starter 组件时，关闭即可（Bean 不再注册）：

```yaml
suretool:
  captcha:
    enabled: false
```

## 5. 示例工程

完整可运行示例见 [sure-examples](../../sure-examples/README.md)：

```bash
# 克隆仓库后直接运行
cd sure-examples
mvn spring-boot:run
```

## 6. 冒烟保障

starter 有自动化冒烟测试（`SureToolAutoConfigurationTest`）：
- 上下文加载：默认装配 `CaptchaGenerator`
- 属性绑定：`width=320/height=120` 配置生效
- 条件装配：`enabled=false` 时 Bean 不存在

> 接入即得质量保障，配置错误会在启动时暴露而非运行期。
