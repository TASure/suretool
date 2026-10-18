/*
 * Copyright (c) 2026 suretool contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sure.tool.thread;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * 批45（v1.19.0 候选，阶段 B「好用」）：Actor 轻量模型测试。
 *
 * <p>覆盖：顺序保序 / 并发投递全消费 / 有界邮箱满 / trySend 超时与成功 /
 * 错误隔离与缺省忽略 / 优雅关闭 drain / close 后拒绝 / 幂等 / pendingCount /
 * 虚拟线程 / null 与构造边界。</p>
 */
public class ActorBatch45Test {

	private static final long AWAIT_MILLIS = 5_000L;

	@Test
	public void testOrderedDelivery() throws Exception {
		List<Integer> received = new CopyOnWriteArrayList<>();
		Actor<Integer> actor = Actor.start(received::add);
		for (int i = 0; i < 10; i++) {
			actor.send(i);
		}
		awaitUntil(() -> received.size() == 10);
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals(Integer.valueOf(i), received.get(i));
		}
		actor.close();
	}

	@Test
	public void testConcurrentSendAllConsumed() throws Exception {
		AtomicInteger count = new AtomicInteger();
		Actor<Integer> actor = Actor.start(n -> count.incrementAndGet());
		int threads = 8;
		int perThread = 100;
		Thread[] senders = new Thread[threads];
		for (int t = 0; t < threads; t++) {
			senders[t] = new Thread(() -> {
				for (int i = 0; i < perThread; i++) {
					actor.send(i);
				}
			});
			senders[t].start();
		}
		for (Thread sender : senders) {
			sender.join();
		}
		awaitUntil(() -> count.get() == threads * perThread);
		Assert.assertEquals(threads * perThread, count.get());
		actor.close();
	}

	@Test
	public void testBoundedMailboxFullThrows() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		Actor<Integer> actor = Actor.start(n -> {
			try {
				gate.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, 1);
		actor.send(1);
		awaitUntil(() -> actor.pendingCount() == 0); // 第一条已被取出并阻塞
		actor.send(2); // 第二条入队，容量 1 已满
		try {
			actor.send(3);
			Assert.fail("有界邮箱已满应抛出 IllegalStateException");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("邮箱已满"));
		}
		gate.countDown();
		actor.close();
	}

	@Test
	public void testTrySendTimeoutFalse() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		Actor<Integer> actor = Actor.start(n -> {
			try {
				gate.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, 1);
		actor.send(1);
		awaitUntil(() -> actor.pendingCount() == 0); // 第一条已被取出并阻塞
		actor.send(2); // 第二条入队，容量 1 已满
		boolean accepted = actor.trySend(3, Duration.ofMillis(50));
		Assert.assertFalse("邮箱满且等待超时应返回 false", accepted);
		gate.countDown();
		actor.close();
	}

	@Test
	public void testTrySendSuccess() throws Exception {
		AtomicInteger count = new AtomicInteger();
		Actor<String> actor = Actor.start(s -> count.incrementAndGet());
		boolean accepted = actor.trySend("x", Duration.ofSeconds(1));
		Assert.assertTrue(accepted);
		awaitUntil(() -> count.get() == 1);
		actor.close();
	}

	@Test
	public void testErrorIsolation() throws Exception {
		List<Integer> received = new CopyOnWriteArrayList<>();
		List<Throwable> errors = new CopyOnWriteArrayList<>();
		Actor<Integer> actor = Actor.start(n -> {
			if (n == 1) {
				throw new RuntimeException("boom-" + n);
			}
			received.add(n);
		}, 0, errors::add);
		actor.send(1);
		actor.send(2);
		actor.send(3);
		actor.send(4);
		awaitUntil(() -> received.size() == 3 && errors.size() == 1);
		Assert.assertEquals(List.of(2, 3, 4), received);
		Assert.assertEquals("boom-1", errors.get(0).getMessage());
		actor.close();
	}

	@Test
	public void testErrorHandlerAbsentIgnored() throws Exception {
		List<Integer> received = new CopyOnWriteArrayList<>();
		Actor<Integer> actor = Actor.start(n -> {
			if (n == 1) {
				throw new RuntimeException("boom");
			}
			received.add(n);
		});
		actor.send(1);
		actor.send(2);
		actor.send(3);
		actor.send(4);
		awaitUntil(() -> received.size() == 3);
		Assert.assertEquals(List.of(2, 3, 4), received);
		actor.close();
	}

	@Test
	public void testGracefulCloseDrainsRemaining() throws Exception {
		List<Integer> received = new CopyOnWriteArrayList<>();
		Actor<Integer> actor = Actor.start(received::add);
		for (int i = 0; i < 5; i++) {
			actor.send(i);
		}
		actor.close();
		awaitUntil(() -> received.size() == 5 && actor.isClosed());
		Assert.assertEquals(List.of(0, 1, 2, 3, 4), received);
	}

	@Test
	public void testSendAfterCloseThrows() throws Exception {
		Actor<String> actor = Actor.start(s -> { });
		actor.close();
		try {
			actor.send("x");
			Assert.fail("关闭后 send 应抛出 IllegalStateException");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("已关闭"));
		}
		try {
			actor.trySend("x", Duration.ofMillis(10));
			Assert.fail("关闭后 trySend 应抛出 IllegalStateException");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("已关闭"));
		}
	}

	@Test
	public void testCloseIdempotent() {
		Actor<String> actor = Actor.start(s -> { });
		actor.close();
		actor.close();
		Assert.assertTrue(actor.isClosed());
	}

	@Test
	public void testPendingCount() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		Actor<Integer> actor = Actor.start(n -> {
			try {
				gate.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, 2);
		actor.send(1);
		awaitUntil(() -> actor.pendingCount() == 0); // 第一条被取出
		actor.send(2);
		Assert.assertEquals(1, actor.pendingCount()); // 第二条待处理
		gate.countDown();
		awaitUntil(() -> actor.pendingCount() == 0);
		actor.close();
	}

	@Test
	public void testErrorHandlerThrowsIgnored() throws Exception {
		List<Integer> received = new CopyOnWriteArrayList<>();
		Actor<Integer> actor = Actor.start(n -> {
			if (n == 1) {
				throw new RuntimeException("boom");
			}
			received.add(n);
		}, 0, t -> {
			throw new RuntimeException("err-handler-boom");
		});
		actor.send(1);
		actor.send(2);
		awaitUntil(() -> received.size() == 1);
		Assert.assertEquals(List.of(2), received);
		actor.close();
	}

	@Test
	public void testTrySendInterruptedReturnsFalse() throws Exception {
		CountDownLatch gate = new CountDownLatch(1);
		Actor<Integer> actor = Actor.start(n -> {
			try {
				gate.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}, 1);
		actor.send(1);
		awaitUntil(() -> actor.pendingCount() == 0); // 第一条被取出并阻塞
		actor.send(2); // 邮箱满
		AtomicBoolean result = new AtomicBoolean(true);
		Thread sender = new Thread(() -> {
			try {
				result.set(actor.trySend(3, Duration.ofSeconds(10)));
			} catch (IllegalStateException ignored) {
				// 关闭竞态，忽略
			}
		});
		sender.start();
		Thread.sleep(200); // 让 sender 阻塞在 offer 等待
		sender.interrupt();
		sender.join();
		Assert.assertFalse("被中断后 trySend 应返回 false", result.get());
		gate.countDown();
		actor.close();
	}

	@Test
	public void testLoopInterruptedExits() throws Exception {
		AtomicInteger count = new AtomicInteger();
		Actor<String> actor = Actor.start(s -> count.incrementAndGet());
		Assert.assertNotNull(actor.loopThread);
		Assert.assertTrue(actor.loopThread.isVirtual());
		actor.loopThread.interrupt();
		Thread.sleep(200); // 循环退出
		actor.send("x");
		Thread.sleep(200);
		Assert.assertEquals(0, count.get()); // 循环已退出，消息未被消费
		Assert.assertFalse(actor.isClosed());
	}

	@Test
	public void testVirtualThreadExecution() throws Exception {
		AtomicInteger virtualCount = new AtomicInteger();
		Actor<String> actor = Actor.start(s -> {
			if (Thread.currentThread().isVirtual()) {
				virtualCount.incrementAndGet();
			}
		});
		actor.send("a");
		actor.send("b");
		awaitUntil(() -> virtualCount.get() == 2);
		actor.close();
	}

	@Test
	public void testNullAndBoundaryDefense() throws Exception {
		// handler null → IAE
		try {
			Actor.start(null);
			Assert.fail("handler 为 null 应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			Actor.start(null, 16);
			Assert.fail("handler 为 null 应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		try {
			Actor.start(null, 16, t -> { });
			Assert.fail("handler 为 null 应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// ok
		}
		// message null → NPE
		Actor<String> actor = Actor.start(s -> { });
		try {
			actor.send(null);
			Assert.fail("message 为 null 应抛出 NullPointerException");
		} catch (NullPointerException expected) {
			// ok
		}
		try {
			actor.trySend(null, Duration.ofMillis(10));
			Assert.fail("message 为 null 应抛出 NullPointerException");
		} catch (NullPointerException expected) {
			// ok
		}
		// capacity ≤ 0 视为无界
		AtomicInteger count = new AtomicInteger();
		Actor<Integer> zeroCap = Actor.start(n -> count.incrementAndGet(), 0);
		Actor<Integer> negativeCap = Actor.start(n -> count.incrementAndGet(), -5);
		zeroCap.send(1);
		negativeCap.send(2);
		awaitUntil(() -> count.get() == 2);
		zeroCap.close();
		negativeCap.close();
	}

	private static void awaitUntil(BooleanSupplier condition) throws InterruptedException {
		long deadline = System.currentTimeMillis() + AWAIT_MILLIS;
		while (!condition.getAsBoolean()) {
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError("等待条件超时（" + AWAIT_MILLIS + "ms）");
			}
			Thread.sleep(10);
		}
	}
}
