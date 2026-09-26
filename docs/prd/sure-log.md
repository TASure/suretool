# PRD · sure-log：零依赖日志门面（批 3 / P1 · v1.2.0）

> 状态：已确认（产品经理 → 架构师 → 工程师）
> 所属迭代：v1.2.0 = 批 3 sure-log + SettingUtil 分层配置 + 批 4 sure-db
> 团队：研发小组五步闭环 · 第 1/2 步产物

## 1. 目标与范围

### 1.1 背景

工具库调用方需要打日志，但不应被绑定到某一具体日志框架。对标 Hutool `hutool-log`：

- 若调用方项目 classpath 中存在 **SLF4J**（业界事实标准门面），sure-log 自动委托给 SLF4J；
- 否则使用内置 **Console 输出**兜底，保证开箱即用、零配置、零依赖。

### 1.2 目标

- 提供统一日志门面 `Log`（接口）+ `LogFactory`（获取）+ `LogUtil`（静态便捷门面，符合项目 `XxxUtil` 命名约束）。
- 零第三方编译期/运行期强制依赖；SLF4J 仅运行时按 classpath **探测**接入。
- 支持：级别（trace/debug/info/warn/error）、占位符 `{}` 格式化、异常堆栈、线程安全、惰性获取。

### 1.3 不做（边界）

- 不做日志框架实现、不写文件、不轮转——全部交给底层（SLF4J 绑定或 Console）。
- 不引入 log4j/logback 等任何具体实现依赖。
- 不做 MDC/NDC、异步日志等高级能力（P2 候选）。

## 2. 用户场景

1. 工具库内部打日志：`LogUtil.debug("处理 {}", name)`，无 SLF4J 时直接打 Console。
2. 应用项目引入 slf4j-api + logback，sure-log 自动走 SLF4J，无需改代码。
3. 按类获取：`LogFactory.get(Foo.class)` 命名规则 `com.sure.tool.log.Foo`（对标 SLF4J 类名 Logger）。

## 3. 验收标准（门禁）

| # | 验收项 | 验证方式 |
| --- | --- | --- |
| A1 | 无 SLF4J 时 `LogUtil.info("x={}", 1)` 输出到 Console，含占位符替换 | JUnit：捕获 System.out/err |
| A2 | classpath 含 slf4j-api 时自动委托 SLF4J（用 slf4j-simple 绑定验证） | JUnit：绑定 slf4j-simple（test scope），断言走 SLF4J |
| A3 | 五级方法 trace/debug/info/warn/error 齐全，`isDebugEnabled` 等 isXxxEnabled 齐全 | JUnit |
| A4 | 异常参数自动追加堆栈（占位符外的 Throwable 不入消息） | JUnit |
| A5 | `LogFactory.get(Class)` / `get(String)` 命名规则正确、缓存不重复创建 | JUnit：同一 name 返回同一实例 |
| A6 | 多线程并发调用无异常、无输出错乱（仅验证不抛错） | JUnit：10 线程 × 100 次 |
| A7 | 门禁：Checkstyle 0、SpotBugs 0、javadoc 0 error、测试全绿 | `mvn -B verify` |
| A8 | 新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README 模块表 | 仓库一致性检查 |

## 4. 架构评审结论（软件架构师）

- **模块归属**：新建 `sure-log` 模块（对标 hutool-log），包 `com.sure.tool.log`。
- **依赖边界**：**零第三方编译依赖**——不声明 slf4j 依赖；运行时用 `Class.forName("org.slf4j.LoggerFactory")` 探测，命中则反射委托，未命中用 Console 实现。JPMS `module sure.log` 不 `requires` slf4j（反射接入无需编译期引用）。
- **API 形状**（符合 `XxxUtil` + 私有构造 + `@since 1.2.0`）：
  - `interface Log`：`trace/debug/info/warn/error(String, Object...)` + `isTraceEnabled()...`
  - `class LogFactory`：`get(String)` / `get(Class)`；静态缓存 Map（线程安全）；探测逻辑集中在 `detectBackend()`
  - `class LogUtil`：静态门面，`get()` 默认 `LogUtil` 自身类名；`debug/info/warn/error/trace` 静态方法直通
- **模块内结构**：`Log`（接口）→ `AbstractLog`（模板：格式化 + 异常处理）→ `Slf4jLog`（反射实现）/ `ConsoleLog`（兜底）。
- **与 sure-core 关系**：sure-log **依赖 sure-core**（复用 `StrUtil.format` 占位符格式与 `Assert`），保持与其它扩展模块一致。

## 5. 风险与回退

- P2：SLF4J 探测每次 get 一次成本极低（Boolean 缓存探测结果）。
- 若未来 SLF4J 升级 API 断裂：探测点集中 `Slf4jLog` 单类，修复面小。
