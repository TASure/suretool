# 批21 PRD：并发增强（v2.0 预备，对标 Guava concurrency / Hutool thread）

- 状态：已批准
- 目标版本：1.11.0（@since 1.11.0）
- 模块：sure-core（com.sure.tool.thread）

## 背景与缺口

sure-core thread 包已有：ThreadUtil（虚拟线程池/parallel/invokeAll/命名线程工厂）、StructuredTaskUtil（parallel/anyOf 扇出聚合）、RateLimiter（令牌桶）、LockUtil（按 key 全局读写锁）、ExecutorBuilder、SyncFinisher。

对标 Guava/Hutool 的缺口：
1. **分段锁**：LockUtil 按 key 无界缓存锁，key 无限增长；缺定长分段锁（Guava Striped<Lock> 语义）
2. **命名线程池便捷**：缺 `newExecutor(size, prefix)` / `newScheduledExecutor`（Hutool ThreadUtil 高频）
3. **sleep/join 便捷**：缺 `sleep(Duration/TimeUnit)` 重载与 `joinQuietly`
4. **结构化限时 varargs**：parallel/anyOf 只有 Collection 版本，缺 `(Duration, Callable...)` 直传重载

## 迭代内容

### 1. StripedLock（新类）
- 定长 ReentrantLock 数组分段锁，默认 16 段，可指定段数（>0）
- `get(Object key)`：按 key 哈希取段锁（hash 分散：`(h ^ (h >>> 16)) & (size-1)`，size 强制 2 的幂）
- `get(int index)`：直接取段锁（越界取模）
- `lockCount()`：段数
- `runLocked(Object key, Runnable)` / `runLocked(Object key, Supplier<T>)`：函数式加锁执行

### 2. ThreadUtil 增强
- `newExecutor(int size)` / `newExecutor(int size, String prefix)`：命名固定线程池（daemon，`sure-{prefix}-{n}`）
- `newScheduledExecutor(int size, String prefix)`：命名调度线程池
- `sleep(long, TimeUnit)` / `sleep(Duration)`：重载（保持中断恢复语义）
- `joinQuietly(Thread)`：join 并吞掉中断（中断标志复位）

### 3. StructuredTaskUtil 增强
- `parallel(Duration, Callable<T>...)`：限时 varargs 扇出-聚合
- `anyOf(Duration, Callable<T>...)`：限时 varargs 首成功短路

## 验收标准

- StripedLockTest ≥8 用例：段数配置/哈希取段/同 key 互斥/不同 key 并行/越界取模/函数式加锁/并发互斥计数
- ThreadUtilBatch21Test ≥8 用例：命名/daemon/调度执行/超时 sleep/joinQuietly/虚拟线程
- StructuredTaskBatch21Test ≥6 用例：限时 parallel 成功/超时取消/anyOf 短路/限时 anyOf
- sure-core 全量门禁：checkstyle + SpotBugs(effort=Max) + jacoco ≥0.90 + 全测试绿
- 每类中文 Javadoc + @since 1.11.0，Apache-2.0 header，Tab 缩进
