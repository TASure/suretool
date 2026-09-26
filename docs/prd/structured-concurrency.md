# PRD：结构化并发（StructuredTaskUtil）

> 批次：P0 · 批 1　|　负责人：软件研发小组　|　状态：已评审，进入实现
> 版本目标：v1.1.0（与批 2 现代集合合并发布）

## 1. 目标与范围

在 `sure-core` 提供基于 JDK 21 `StructuredTaskScope` 的结构化并发编排 API，兑现「JDK 21+ 独占差异化」——Guava、Hutool、Apache Commons 均无等价能力。

**范围内**：
- `StructuredTaskUtil`：并行聚合（parallel）、首成功短路（anyOf）、超时控制；
- `ThreadUtil` 补一个 `invokeAll(tasks, timeout)` 超时重载（现有无超时版本）。

**范围外（不做）**：
- 不引入任何第三方并发库（核心零依赖原则）；
- 不做任务调度/定时框架（`sure-cron` 已独立承担）；
- 不做工作窃取池等 JDK FJP 已有能力；
- 不重造 `ExecutorService`。

## 2. 用户场景

1. **扇出-聚合**：并发调用多个下游接口/IO，全部完成后统一返回（任一失败即整体失败）；
2. **首成功短路**：多副本请求，取第一个成功的（如多节点探测、多候选源）；
3. **限时编排**：整组任务必须在 deadline 内完成，超时即失败；
4. **虚拟线程友好**：每任务一线程（结构化作用域自动管理生命周期）。

## 3. 架构评审结论（第 2 步）

| 项 | 结论 |
| --- | --- |
| 模块归属 | `sure-core` → `com.sure.tool.thread`（已有 `exports com.sure.tool.thread`，无需改 module-info） |
| 类名 | `StructuredTaskUtil`（`XxxUtil` 命名规范） |
| 依赖边界 | 仅 JDK `java.base`，零第三方运行期依赖 |
| API 形状 | `public final class` + 私有构造器 + 静态方法 + `@since 1.1.0` + 中文 Javadoc + Tab 缩进 |

### 3.1 API 签名（评审通过）

```java
public final class StructuredTaskUtil {

	/** 并发执行全部任务，全部成功后按输入顺序返回结果；任一失败抛 RuntimeException（首个异常） */
	public static <T> List<T> parallel(Callable<T>... tasks);
	public static <T> List<T> parallel(Collection<Callable<T>> tasks);
	/** 限时版本：deadline 内未全部完成抛 RuntimeException(超时) */
	public static <T> List<T> parallel(Collection<Callable<T>> tasks, Duration timeout);

	/** 返回第一个成功的结果，其余任务自动取消；全部失败抛 RuntimeException */
	public static <T> T anyOf(Callable<T>... tasks);
	public static <T> T anyOf(Collection<Callable<T>> tasks, Duration timeout);
}
```

语义映射（JDK 实现）：
- `parallel` → `StructuredTaskScope.ShutdownOnFailure`：`join()`/`joinUntil()` + `throwIfFailed(mapper)` + `resultNow()` 聚合；
- `anyOf` → `StructuredTaskScope.ShutdownOnSuccess`：`join()`/`joinUntil()` + `result()`；
- 超时统一抛 `RuntimeException("...超时")`（内部 `TimeoutException`），中断恢复中断位。

## 4. 验收标准（门禁）

1. `parallel`：N 个任务全部成功后按输入顺序返回；含真实并发性证明（CountDownLatch 验证同时执行）；
2. `parallel`：任一任务抛异常 → 抛出 `RuntimeException` 且 cause 为任务异常；
3. `parallel`：空集合返回空列表不抛异常；
4. `parallel`：超时（任务 sleep > timeout）→ 抛超时异常；
5. `anyOf`：返回首个成功结果（验证慢任务被取消）；全部失败抛异常；超时抛超时异常；
6. `ThreadUtil.invokeAll(tasks, timeout)` 超时行为正确；
7. 质检门禁：`mvn -B verify` 全绿（测试 / Checkstyle 0 违规 / SpotBugs 0 bug / sure-core 覆盖率 ≥ 0.70 且不降）；
8. CHANGELOG 增加条目；commit + push。

## 5. 风险与边界

- `ShutdownOnFailure.join()` 在失败后等待所有任务完成，`parallel` 语义为「完整执行后统一失败」，与「立即取消」的 failFast 不同；本 PRD 不承诺 failFast（后续如需再单独立项）；
- `joinUntil` 超时后 scope 关闭会取消未完成任务，符合「限时编排」语义；
- 空集合 `anyOf` 视为参数错误（`IllegalArgumentException`），与 parallel 空集合返回空列表语义区分。
