# 批45：Actor 轻量模型（阶段 B「好用」首项，独占能力）

## 一、背景与定位

超越路线图阶段 A「追平高频 90%」五项覆盖面已在批39–44 全部收口。批45 进入**阶段 B「好用」——差异化领先**，首项为「结构化并发库」：`StructuredTaskScope` 封装（批1/批21 已建 `StructuredTaskUtil`，含失败即取消、超时聚合、虚拟线程执行器）、**Actor 模型**、虚拟线程 IO 通道（批38 `FileTailer` 已落地）。

本批补足三家（Hutool / Guava / Apache Commons）**均无**的独占能力——**轻量 Actor 模型**：

- Hutool：无 Actor 抽象（只有线程池/队列工具）。
- Guava：无 Actor（仅 `ExecutorService` 与 `EventBus`，无邮箱与消息循环封装）。
- Commons：无 Actor。

Actor 是「好用」维度的差异化卖点：消息驱动的并发单元天然规避共享状态竞争，配合 JDK 虚拟线程实现低成本高并发邮箱。

## 二、API 设计（新建 `com.sure.tool.thread.Actor&lt;T&gt;`，零第三方依赖）

| 方法 | 说明 | 边界 |
|---|---|---|
| `static &lt;T&gt; Actor&lt;T&gt; start(Consumer&lt;T&gt; handler)` | 无界邮箱启动；handler 为 null 抛 IAE | 无界 `LinkedBlockingQueue` |
| `static &lt;T&gt; Actor&lt;T&gt; start(Consumer&lt;T&gt; handler, int capacity)` | 有界邮箱；capacity ≤ 0 视为无界 | `ArrayBlockingQueue` |
| `static &lt;T&gt; Actor&lt;T&gt; start(Consumer&lt;T&gt; handler, int capacity, Consumer&lt;Throwable&gt; errorHandler)` | 带消息异常回调；errorHandler 为 null 忽略异常 | 单条消息异常不中断 actor |
| `void send(T message)` | 异步投递；message 为 null 抛 NPE；已关闭抛 IllegalStateException；有界邮箱满抛 IllegalStateException | 非阻塞 |
| `boolean trySend(T message, Duration timeout)` | 带等待投递；超时未入队返回 false；null 校验同上 | timeout 为 null 视作 0 |
| `void close()` | 优雅关闭：拒绝新消息、处理完已入队消息后退出消息循环；幂等 | close 后 send/trySend 抛 IllegalStateException |
| `boolean isClosed()` | 是否已关闭 | — |
| `int pendingCount()` | 当前待处理消息数 | 诊断/监控 |

### 语义约定

1. **消息顺序**：同一 actor 的消息按投递顺序被 handler 逐个消费（单消息循环线程）。
2. **错误隔离**：handler 抛异常仅回调 errorHandler（未配置则忽略），消息循环继续处理后续消息，actor 不崩溃。
3. **消息循环线程**：每个 actor 一个虚拟线程（`Thread.ofVirtual().start`）。
4. **优雅关闭**：`close()` 后已入队消息全部处理完毕才退出循环；`pendingCount()` 归零且循环退出后 `isClosed()` 恒 true。
5. **多线程安全**：send/trySend/close/isClosed/pendingCount 全部线程安全（队列 + volatile 状态位）。

## 三、测试清单（`ActorBatch45Test`）

| 用例 | 断言 |
|---|---|
| 顺序保序 | 单线程 send 10 条，handler 收到顺序一致 |
| 并发投递全消费 | 8 线程各 send 100 条，最终计数 800 |
| 有界邮箱满抛 IAE | capacity=1 慢 handler，连续 send 第 2 条抛 IllegalStateException |
| trySend 超时返回 false | capacity=1 慢 handler，`trySend(msg, 50ms)` 返回 false |
| trySend 成功 | 空闲 actor，`trySend(msg, 1s)` 返回 true 且被消费 |
| 错误隔离 | handler 抛异常 1 次后仍处理后续 3 条；errorHandler 收到异常 |
| errorHandler 缺省忽略 | 无 errorHandler 时异常不中断、无泄漏 |
| 优雅关闭处理完剩余 | send 5 条后 close，handler 收到全部 5 条，isClosed true |
| close 后拒绝新消息 | close 后 send 抛 IllegalStateException |
| close 幂等 | 两次 close 不抛异常 |
| pendingCount | send 后 pending>0，消费后归零 |
| 虚拟线程 | handler 所在线程 `isVirtual()` 为 true |
| null 防御 | send(null) 抛 NPE；handler null 抛 IAE；capacity≤0 视为无界 |

## 四、验收标准

1. `Actor.java` 行覆盖率 ≥98%（目标 100%），sure-core 全量行覆盖率 ≥98% 门禁。
2. checkstyle / SpotBugs / license 全绿；全量测试绿（1357+ 新增）。
3. 中文 JavaDoc，每方法带可运行示例。
4. CHANGELOG [Unreleased] 新增批45 条目。
5. 推送 main 后回读验证 SHA 一致。
