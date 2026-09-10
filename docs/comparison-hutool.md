# suretool 与 Hutool 对比

> 本页用于帮助使用者快速判断"我的项目该选哪个"。
> Hutool 是优秀的成熟开源项目，suretool 参考其设计理念；两者定位不同，以下对比**仅陈述客观差异**，不做优劣定性。

## 一、定位差异

| 维度 | suretool | Hutool |
| --- | --- | --- |
| 定位 | 小而全的 Java 工具库，聚焦常用高频能力 | 大型工具库，覆盖面极广（含更多领域扩展） |
| JDK 要求 | **仅 JDK 21+**（`maven.compiler.release=21`） | JDK 8+（5.x 支持 JDK8+；兼容老版本） |
| 包名 | `com.sure.tool` | `cn.hutool` |
| 模块数 | 11 个业务模块 + bom/all/examples | 数十个模块（hutool-all 聚合） |
| 依赖策略 | **零第三方依赖**（除 POI 等专项模块） | 核心零依赖，部分模块带第三方依赖 |

## 二、设计取向

| 项目 | suretool 选择 | 说明 |
| --- | --- | --- |
| 加密默认值 | AES-GCM（认证加密）、RSA-OAEP、最小 2048 位密钥 | 默认安全基线，杜绝弱算法/弱模式被误用 |
| DES | `@Deprecated`，仅保留解密存量数据 | 新代码强制走 AES |
| 线程 | 静态工具方法 + 显式并发工具（ThreadUtil/ExecutorBuilder） | 提供更多高级并发组件 |
| JSON | 自带轻量实现（JSONObject/JSONArray/JSONUtil） | 自带 hutool-json（与 fastjson/gson 兼容模式不同） |
| 校验/脱敏 | ValidatorUtil、DesensitizedUtil | 功能类似 |
| 敏感词 | DFA 实现（sure-dfa） | SensitiveUtil 类似 |

## 三、使用体验对比示例

```java
// Hutool
String s = cn.hutool.core.util.StrUtil.removePrefixIgnoreCase("Hello", "he");
String md5 = cn.hutool.crypto.SecureUtil.md5("data");

// suretool
String s = com.sure.tool.util.StrUtil.removeSuffixIgnoreCase("Hello", "LLO");
String md5 = com.sure.tool.crypto.SecureUtil.md5("data");
```

两库 API 命名风格相近（工具类 + 静态方法），Hutool 用户迁移成本低。

## 四、何时选择

**选择 suretool：**
- 项目已基于 JDK 21+（虚拟线程、record、switch 模式匹配等新特性）；
- 希望依赖最小、安全默认值开箱即用；
- 需要经 CodeQL / SpotBugs / JaCoCo 门禁验证的"小而可信"组件；
- 希望以 `sure-bom` 统一版本管理、按需引入模块。

**选择 Hutool：**
- 需要兼容 JDK 8/11/17 等老版本；
- 需要 Hutool 生态中更广泛的扩展能力（如 hutool-extra 的邮件/模板等）；
- 已有团队规范与既有依赖（Hutool 已在大量生产项目验证）。

## 五、迁移建议

1. `sure-bom` 引入后按模块替换：`sure-core`（字符串/集合/日期/IO/反射）、`sure-json`、`sure-crypto` 等；
2. 加密迁移注意：suretool 的 `AesUtil` 密文格式为 `IV || ciphertext+tag`（Hex/Base64），与 Hutool 的 AES 输出不互解；
3. 若只需某个工具方法，直接复制实现（Apache-2.0 许可下合规）比整体迁移更轻。

## 六、许可与致谢

两库均基于 Apache License 2.0。suretool 的类设计与命名参考 Hutool 公开 API 风格，
部分实现思路借鉴了 Hutool（如缓存策略、DFA 过滤），并已在类注释中标注。
