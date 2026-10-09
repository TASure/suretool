# 批25 PRD：虚拟线程一等公民（AsyncUtil 异步门面 + 并发原语补强）

- 状态：已批准
- 目标版本：1.12.0（@since 1.12.0）
- 模块：sure-core（com.sure.tool.thread）

## 背景与定位

v3.0 差异化领先第一棒：虚拟线程一等公民。现有设施：StructuredTaskUtil（parallel/anyOf 阻塞式）、
ThreadUtil（虚拟线程工厂/执行器）。**缺口**：① 无 CompletableFuture 风格的异步门面（fire-and-forget / 链式 future）；
② 结构化并行缺"限时聚合 + 失败取消"的一站式 API；③ sleep 吞中断（ThreadUtil.sleep 静默恢复中断位）。

对标：JDK CompletableFuture × structured concurrency × Guava Futures 高频子集。

## 迭代内容

### 1. 新增 AsyncUtil（异步门面，默认虚拟线程）
- `runAsync(Runnable)` / `runAsync(Runnable, Executor)`：异步执行，返回 Future<?>
- `supplyAsync(Supplier<T>)` → CompletableFuture<T>（虚拟线程）
- `allOf(Callable<T>...)` → CompletableFuture<List<T>>：结构化并行全部完成，任一失败取消其余并传播首个异常
- `anyOf(Callable<T>...)` → CompletableFuture<T>：首个成功即返回并取消其余
- `withTimeout(Duration, Supplier<T>)` → T：超时抛 TimeoutException
- `await(Duration, Future<T>)` → T：限时等待并解包 ExecutionException
- `joinAll(Duration, Callable<T>...)` → List<T>：限时并行聚合（joinUntil 截止）

### 2. ThreadUtil +1
- `sleepInterruptibly(Duration)`：可中断睡眠（InterruptedException 直接抛出，区别于 sleep 的吞中断）

## 验收标准

- AsyncUtilTest ≥10 例：runAsync/supplyAsync 完成性、allOf 聚合与失败取消、anyOf 首个成功与取消、withTimeout 超时、
  await 解包、joinAll 限时
- ThreadUtilBatch25Test ≥3 例：sleepInterruptibly 中断抛出
- sure-core 门禁全绿（checkstyle + SpotBugs + jacoco ≥0.90 + 全测试 + javadoc）
- 中文 Javadoc + @since 1.12.0、Apache-2.0 header、Tab 缩进、零新增第三方依赖
