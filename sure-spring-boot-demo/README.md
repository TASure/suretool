# suretool Spring Boot Starter 示例

可运行的 Spring Boot 4.1.2 + suretool starter 示例（JDK 25），展示：

- **默认装配**（starter 必选依赖）：`core` + `captcha`，启动即可注入 `CaptchaGenerator`；
- **可选域**：显式引入 `sure-event` / `sure-cron` / `sure-cache` 后，`EventBus` / `CronLifecycle` / `Cache` 才由自动装配生效。

## 运行

```bash
cd sure-spring-boot-demo
mvn spring-boot:run
```

启动日志会打印装配结果：

```
[suretool] captcha = 200x80
[suretool] event   = EventBus bean (1 listeners)
[suretool] cache   = LruCache
[suretool] cron    = CronLifecycle running=true
[suretool] version = 1.20.0
```

## 接口

| 路径 | 说明 |
| --- | --- |
| `GET /` | 装配状态（验证码尺寸、事件总线订阅数、缓存实现、累计事件数） |
| `GET /captcha` | 返回 PNG 验证码，答案打印在应用控制台 |
| `GET /event` | 发布一个 `DemoEvent`，返回订阅者累计接收数 |

```bash
curl http://localhost:18080/
curl -o captcha.png http://localhost:18080/captcha
curl http://localhost:18080/event
```

## 配置

见 [`src/main/resources/application.yml`](src/main/resources/application.yml)：端口 18080，`suretool.captcha` 200×80，`suretool.event / cron / cache` 全部开启，缓存策略 `lru`、容量 100。

关闭某个可选域只需把对应 `suretool.<域>.enabled` 设为 `false`；`cron` / `cache` 默认即关闭，不显式开启不会启动后台线程或占用内存。

## 说明

本工程是**独立示例**，不加入主仓库 reactor，也不随 `sure-bom` 发布；依赖坐标固定为已发布的 `1.20.0`。
