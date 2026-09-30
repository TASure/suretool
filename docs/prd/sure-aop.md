# PRD：sure-aop 模块（批 6 / v1.3.0）

- 状态：已评审通过（产品经理 → 架构师 → 工程师）
- 目标版本：v1.3.0（与 sure-pdf 同批发布）
- 对标：Hutool Aop（`cn.hutool.aop`）
- 硬约束：仅 JDK25+；核心模块零第三方**运行期**依赖；XxxUtil 命名 + 私有构造器 + @since + 中文 Javadoc + Tab 缩进；新模块同步 root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG。

## 1. 背景与目标

面向切面编程（AOP）用于日志、鉴权、重试、事务等横切关注点。Hutool Aop 以 `Aspect` + `ProxyUtil` 极简模型著称（内部用 CGLIB）。

目标：提供**零第三方运行期依赖**的 AOP 门面——基于 JDK 动态代理（`java.lang.reflect.Proxy`）的 `Aspect` 切面 + `ProxyUtil` 代理工厂，方法级匹配，覆盖接口代理场景；类代理（无接口）由调用方引入 CGLIB/字节码库自行实现（文档说明）。

## 2. 模块信息

- 模块：`sure-aop`，坐标 `io.github.tasure:sure-aop`，包 `com.sure.tool.aop`
- 运行期依赖：**无第三方**（仅 JDK）
- 测试依赖：无第三方
- module-info：`module sure.aop { requires transitive sure.core; exports com.sure.tool.aop; }`

## 3. API 设计

### 3.1 Aspect（切面基类，对标 hutool Aspect）

- `public boolean match(Method method)`：方法匹配钩子，默认 `true`（全部拦截）
- 通知方法（默认空实现，子类按需覆写）：
  - `before(Object target, Method method, Object[] args)`
  - `after(Object target, Method method, Object[] args, Object result)`
  - `afterException(Object target, Method method, Object[] args, Throwable e)`

### 3.2 ProxyUtil（代理工厂，对标 hutool ProxyUtil）

- `static <T> T proxy(T target, Aspect aspect)`：为 target 创建 JDK 动态代理
  - 校验：target 非 null；target 至少实现一个接口（否则抛 IllegalArgumentException 并提示类代理需字节码库）
  - 拦截流程：match 不通过 → 直接调用原方法；通过 → before → invoke → after；异常 → afterException 后重抛
- `static <T> T proxy(Class<T> interfaceClass, Aspect aspect)`：为接口创建代理实例（无目标对象，invoke 仅触发通知，返回值 null 由调用方语义决定）
  - 说明：接口代理场景（如 mock / 拦截器），方法默认返回 null

### 3.3 线程安全与语义

- 每次 `proxy` 创建独立 Proxy 实例；Aspect 实例需线程安全（无状态推荐）
- 异常语义：afterException 执行后**继续抛出**原异常（与 Hutool 一致）

## 4. 验收标准

- A1 接口代理：before/after 按调用顺序触发，原方法返回值透传
- A2 match 返回 false 的方法不触发通知，直接调用原方法
- A3 目标方法抛异常 → afterException 触发且异常向外传播（调用方可见）
- A4 非接口目标（如普通类）→ proxy 抛 IllegalArgumentException，提示信息含"CGLIB/字节码"
- A5 接口代理（无目标）：调用触发 before/after，返回值 null
- A6 null target / null aspect → IllegalArgumentException
- A7 全量 verify 门禁：Checkstyle 0、SpotBugs 0、javadoc 0 error、sure-core 覆盖率不降
- A8 模块注册同步：root modules / dependencyManagement / sure-all / sure-bom / README / CHANGELOG

## 5. 架构评审结论

- **零依赖可行性**：JDK 动态代理为 JDK 标准能力，仅支持接口代理——通过；类代理场景在文档与异常信息中明确指引字节码库。
- **异常语义**：afterException 后重抛，保证调用方感知失败（切面不吞异常）——通过。
- **方法匹配**：match 钩子默认全拦截，子类覆写实现按名称/注解过滤（示例：方法名前缀匹配）——通过。
- **命名与包**：`com.sure.tool.aop`；ProxyUtil 私有构造器 + @since 1.3.0。
