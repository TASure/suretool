# suretool Actor 并发模型指南

> 适用版本：v1.19.0+ · 模块：sure-core · 包：`com.sure.tool.thread`
> 一句话：**虚拟线程邮箱消息循环**——顺序保序、错误隔离、零第三方依赖。Hutool / Guava / Apache Commons 均无此独占能力。

## 1. 为什么需要 Actor

多线程直接共享状态要自己加锁、处理竞态；JDK 的 `ExecutorService` 只解决「执行」，不解决「谁先谁后、出错怎么办」。

Actor 模型给出一个更简单的约定：**每个 Actor 独占一个邮箱（队列），消息按投递顺序串行消费**——
同一 Actor 内天然无竞态，你不需要锁；单条消息处理异常不影响后续消息（错误隔离）。

suretool 的 `Actor` 把邮箱循环跑在**虚拟线程**上（JDK 21+），一个 Actor 只占一个轻量线程，
可以创建成千上万个，内存与调度开销都远小于平台线程。

## 2. 快速开始

```java
import com.sure.tool.thread.Actor;

// 启动一个处理 String 消息的 Actor
Actor<String> logger = Actor.start(msg -> System.out.println("收到: " + msg));

logger.send("hello");   // 异步投递，立即返回
logger.send("world");

logger.close();         // 优雅关闭：处理完队列中剩余消息后停止循环
```

输出（保序）：

```text
收到: hello
收到: world
```

## 3. 三种启动方式

| 工厂方法 | 邮箱 | 适用 |
| --- | --- | --- |
| `start(handler)` | 无界 | 消息量小、无需背压 |
| `start(handler, capacity)` | 有界（容量 ≤ 0 视为无界） | 需要限流，避免内存堆积 |
| `start(handler, capacity, errorHandler)` | 有界/无界 + 错误处理 | 生产环境：出错要记录且不中断循环 |

### 3.1 有界邮箱 + 限时投递（背压）

```java
import com.sure.tool.thread.Actor;
import java.time.Duration;

// 容量 100：队列满时 send 会阻塞等待
Actor<String> worker = Actor.start(this::process, 100);

// trySend 限时投递：1 秒内没位置就放弃，不阻塞调用方
boolean accepted = worker.trySend("task-1", Duration.ofSeconds(1));
if (!accepted) {
	// 队列满：走降级（记日志 / 丢队列 / 返回重试）
}
```

### 3.2 错误隔离（handler 抛异常不中断循环）

```java
Actor<String> robust = Actor.start(
	msg -> processMessage(msg),          // 处理逻辑
	50,                                  // 邮箱容量
	err -> System.err.println("消息处理失败: " + err)   // errorHandler：只记录，不中断
);

robust.send("bad-data");   // 这条会触发 errorHandler
robust.send("good-data");  // 仍会被正常处理（循环未中断）
```

## 4. 状态查询

| 方法 | 语义 |
| --- | --- |
| `isClosed()` | 是否已关闭 |
| `pendingCount()` | 队列中待处理消息数（有界邮箱背压水位） |
| `close()` | 优雅关闭：停止接受新消息，等队列清空后结束循环（幂等，重复调用无副作用） |

```java
Actor<Integer> counter = Actor.start(n -> total.addAndGet(n));
for (int i = 0; i < 1000; i++) {
	counter.send(i);
}
counter.close();
System.out.println("积压消息数: " + counter.pendingCount()); // 0（close 已等队列清空）
```

## 5. 实战场景

### 5.1 日志收集器（顺序保序）

```java
Actor<String> logSink = Actor.start(line -> {
	// 单线程顺序落盘，天然无竞态
	Files.writeString(logFile, line + "\n", StandardCharsets.UTF_8,
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
});
```

多个生产者线程并发 `send`，落盘顺序 = 投递顺序，无需外部同步。

### 5.2 事件总线卸载（削峰）

```java
Actor<Runnable> dispatchQueue = Actor.start(Runnable::run, 500, err -> { /* 静默单个失败事件 */ });

// 发布事件：立即返回，实际执行在虚拟线程队列中排队
dispatchQueue.send(() -> listeners.forEach(l -> l.onEvent(e)));
```

### 5.3 无界 vs 有界：生产建议

| 场景 | 建议 | 原因 |
| --- | --- | --- |
| 低频消息、可容忍短暂堆积 | 无界 `start(handler)` | 简单，无投递失败 |
| 高频突发、内存敏感 | 有界 + `trySend` 超时 | 队列满走降级，避免 OOM |
| 关键路径、必须不丢消息 | 有界大容量 + send（阻塞） | send 阻塞即天然背压 |

## 6. 设计要点（与手写线程池的区别）

1. **顺序保序**：同一 Actor 消息按 FIFO 串行消费，`send` 顺序即消费顺序——共享 `ExecutorService` 无此保证。
2. **错误隔离**：单条 handler 异常被 `errorHandler` 捕获，循环继续；线程池中一个任务抛异常不影响池本身，但任务边界外的状态损坏要靠自己兜。
3. **虚拟线程**：邮箱循环占虚拟线程，`Actor` 实例可大规模创建；对比平台线程池每个线程 ~1MB 栈，成本低一个量级。
4. **零依赖**：仅 JDK（`BlockingQueue` + 虚拟线程 + `AtomicInteger`），无 Akka 等框架依赖。

## 7. 与结构化并发的关系

suretool 的并发三件套（`docs/posts/` 教程体系）：

| 工具 | 场景 | 包 |
| --- | --- | --- |
| `Actor` | 异步消息循环、顺序处理、错误隔离 | `com.sure.tool.thread` |
| `StructuredTaskUtil` | 结构化并发：任务组失败即取消 / 超时聚合 | `com.sure.tool.thread` |
| 虚拟线程一等公民 | 阻塞型 IO 大规模并发 | 全库默认 |

## 8. 完整示例（可直接运行）

```java
import com.sure.tool.thread.Actor;
import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ActorDemo {

	public static void main(String[] args) {
		// 生产：3 个线程并发投递 3000 条
		Actor<Integer> aggregator = Actor.start(
				n -> sum.addAndGet(n),
				200,
				err -> System.err.println("失败: " + err));

		Thread[] producers = new Thread[3];
		for (int t = 0; t < producers.length; t++) {
			final int base = t * 1000;
			producers[t] = Thread.ofPlatform().start(() -> {
				for (int i = 1; i <= 1000; i++) {
					while (!aggregator.trySend(base + i, Duration.ofMillis(100))) {
						// 队列满：重试（演示背压）
					}
				}
			});
		}
		for (Thread p : producers) {
			p.join();
		}
		aggregator.close();
		System.out.println("总和 = " + sum.get());   // 1+2+...+3000 = 4501500
	}

	private static final AtomicInteger sum = new AtomicInteger();
}
```

## 9. 参考

- API 详情：`com.sure.tool.thread.Actor` 中文 JavaDoc（每个方法带示例）
- 规格文档：`docs/prd/sure-actor-batch45.md`
- 治理：Actor 相关基准见 Benchmark 报告站（批48）
