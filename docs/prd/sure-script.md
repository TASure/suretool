# PRD：sure-script 模块（批 5 / v1.3.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.3.0（与 sure-template 同批发布）
- 对标：Hutool ScriptUtil（`cn.hutool.script`）
- 硬约束：仅 JDK25+；核心模块零第三方**运行期**依赖；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG。

## 1. 背景与目标

Java 生态脚本互操作由 JSR-223（`javax.script`）标准化，但 JDK15+ 移除了内置 Nashorn 引擎，开发者需要自行理解 SPI 加载、引擎缓存与异常语义。Hutool ScriptUtil 提供了一行式脚本求值门面。

目标：提供**零第三方运行期依赖**的 JSR-223 脚本门面（引擎由调用方通过依赖提供，如 GraalVM JS / Groovy / Jython），覆盖引擎探测、编译缓存、快速求值，并在引擎缺失时给出可操作的清晰报错。

## 2. 模块信息

- 模块：`sure-script`，坐标 `io.github.tasure:sure-script`，包 `com.sure.tool.script`
- 运行期依赖：**无第三方**（仅 JDK `java.scripting`）
- 测试依赖：无第三方——用测试替身 ScriptEngineFactory 验证 SPI 探测/编译缓存/求值逻辑（JDK25 无内置引擎，属预期）
- module-info：`module sure.script { requires transitive sure.core; requires java.scripting; exports com.sure.tool.script; }`

## 3. API 设计

### 3.1 ScriptUtil（静态门面，对标 hutool ScriptUtil）

- `static ScriptEngine getScriptEngine()`：默认引擎（优先按 `.js` / `js` / `JavaScript` 探测）
- `static ScriptEngine getEngine(String name)`：按引擎名（name 匹配，忽略大小写，缓存实例）
- `static ScriptEngine getEngineByExtension(String extension)` / `getEngineByMimeType(String mimeType)`：按扩展名 / MIME 探测
- `static Object eval(String script)` / `eval(String script, Bindings bindings)`：快速求值（内部 compile 缓存）
- `static CompiledScript compile(String script)`：编译并缓存（按脚本文本）
- `static void clearCache()`：清空编译缓存与引擎缓存
- 引擎不存在：抛 `ScriptRuntimeException`，消息含引擎名与可操作提示（"请添加对应 JSR-223 引擎依赖，如 org.graalvm.js:js-scriptengine"）

### 3.2 ScriptRuntimeException

`extends RuntimeException`，构造器（message / message+throwable）。

### 3.3 线程安全与缓存

- 引擎缓存：`ConcurrentHashMap<String, ScriptEngine>`（按 name 小写 key），`ScriptEngineManager` 单例共享
- 编译缓存：`ConcurrentHashMap<String, CompiledScript>`，脚本文本为 key
- eval 流程：compile(script) → `compiled.eval(bindings)`；编译失败不写缓存

## 4. 验收标准

- A1 `ScriptUtil.getEngine("不存在")` 抛 ScriptRuntimeException 且消息含引擎名与依赖提示
- A2 测试替身引擎注册后：getEngine 返回该引擎；重复调用返回同一实例（缓存）
- A3 compile 同一脚本两次返回同一 CompiledScript（缓存生效）
- A4 eval(script, bindings) 传入变量可被引擎读取
- A5 getEngineByExtension / getEngineByMimeType 正确路由
- A6 clearCache 后 compile 重新编译（缓存失效）
- A7 全量 verify 门禁：Checkstyle 0、SpotBugs 0、javadoc 0 error、sure-core 覆盖率不降
- A8 模块注册同步：root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG

## 5. 架构评审结论

- **零依赖可行性**：javax.script 为 JDK 标准 API，SPI 由 ScriptEngineManager 加载，本模块不绑定任何具体引擎——通过。
- **引擎缺失语义**：JDK25 无内置引擎，缺失时抛带指引的异常而非 NPE——与 Hutool 语义对齐且更友好。
- **缓存一致性**：引擎与编译缓存均以并发容器承载，`ScriptEngine` 与 `CompiledScript` 线程安全性由实现方保证；并发场景由调用方按引擎文档约束。
- **命名与包**：`com.sure.tool.script` 与既有 `com.sure.tool.*` 命名一致；ScriptUtil 私有构造器 + @since 1.3.0。
