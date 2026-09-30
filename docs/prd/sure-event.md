# PRD：sure-event 模块（轻量事件总线）

- 状态：Accepted（2026-09-29）｜ 模块：`sure-event` ｜ 包：`com.sure.tool.event` ｜ 版本：1.4.0
- 依赖：仅 sure-core（零第三方运行时依赖）｜ JDK 25+ ｜ 模块化：`module sure.event`
- 对标：Guava EventBus 简化 + 现代 Java（虚拟线程）

## A. 目标与定位

零依赖、线程安全的事件发布/订阅总线。支持函数式注册与注解注册两种风格，
同步/异步两种投递（异步用虚拟线程）。不引入 Spring 事件或反射框架依赖。

## B. 模块 API 设计

### B1. `EventBus`
- `subscribe(Class<T> eventType, Consumer<T> listener)`：函数式注册，返回 `Subscription`（可取消）
- `unsubscribe(Subscription)` / `unsubscribe(Class, Consumer)`：取消注册
- `post(T event)`：**同步**投递；按"监听类型兼容实际类型"匹配
  （监听 `Object.class` 或父类型会收到子类事件，语义同 Guava）
- `postAsync(T event)`：**异步**投递（每事件一个虚拟线程，`Thread.ofVirtual().start`）
- `setExceptionHandler(EventExceptionHandler)`：监听器异常处理（默认打印警告）
- `register(Object bean)`：注解式注册——收集 bean 上 `@EventSubscribe` 的 public 方法
  （方法须有且仅 1 个参数），绑定为监听器
- `unregister(Object bean)`：移除该 bean 全部注解监听
- `listenerCount()` / `clear()`

### B2. `@EventSubscribe` 注解（@Retention(RUNTIME)，@Target(METHOD)）
- 标注监听方法；`register` 时按方法参数类型匹配事件

### B3. `Subscription`（AutoCloseable）：取消句柄，`close()` 即取消
### B4. `EventExceptionHandler` 函数式接口：`handle(Throwable, Object event)`

## C. 验收标准（A1-A8）
- A1 `subscribe` 后 `post`，监听器收到事件，值一致
- A2 监听父类型收到子类事件（`post(new SubEvent())` 触发 `Object`/父类监听）
- A3 `Subscription.close()` 后不再收到事件；`unsubscribe(Class, Consumer)` 同理
- A4 两个监听器都收到同一事件；重复注册同一 Consumer 只触发一次
- A5 `postAsync` 在调用返回后异步触发（用 CountDownLatch 验证完成与线程 ≠ 调用线程）
- A6 监听器抛异常不影响其他监听器，且异常进入 `EventExceptionHandler`
- A7 注解注册：`register(bean)` 后 post 触发 `@EventSubscribe` 方法；`unregister(bean)` 后停止
- A8 并发：10 线程各发 100 事件，监听计数无丢失（线程安全）

## D. 非目标
- 不做分布式/跨进程事件、不做死信队列、不做 Spring 集成（spring-boot-starter 后续统一处理）
- 异步投递不保证顺序（虚拟线程语义），需保序请用 `post` 同步
