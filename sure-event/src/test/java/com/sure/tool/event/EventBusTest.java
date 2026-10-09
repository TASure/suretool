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
package com.sure.tool.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link EventBus} 单元测试。
 */
public class EventBusTest {

	private EventBus bus;

	static class BaseEvent {
		final String value;

		BaseEvent(String value) {
			this.value = value;
		}
	}

	static class SubEvent extends BaseEvent {
		SubEvent(String value) {
			super(value);
		}
	}

	static class AnnotatedListener {
		final CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();

		@EventSubscribe
		public void onBase(BaseEvent event) {
			received.add("base:" + event.value);
		}

		@EventSubscribe
		public void onSub(SubEvent event) {
			received.add("sub:" + event.value);
		}
	}

	@Before
	public void setUp() {
		bus = new EventBus();
	}

	@After
	public void tearDown() {
		bus.clear();
	}

	@Test
	public void subscribeAndPostDeliversValue() {
		AtomicReference<String> got = new AtomicReference<>();
		bus.subscribe(BaseEvent.class, e -> got.set(e.value));
		bus.post(new BaseEvent("hello"));
		assertEquals("收到事件", "hello", got.get());
	}

	@Test
	public void parentListenerReceivesSubclassEvent() {
		CopyOnWriteArrayList<String> got = new CopyOnWriteArrayList<>();
		bus.subscribe(BaseEvent.class, e -> got.add(e.value));
		bus.post(new SubEvent("sub"));
		assertEquals("父类监听收到子类事件", 1, got.size());
		assertEquals("sub", got.get(0));
	}

	@Test
	public void closeCancelsSubscription() {
		AtomicInteger count = new AtomicInteger();
		Subscription sub = bus.subscribe(BaseEvent.class, e -> count.incrementAndGet());
		bus.post(new BaseEvent("a"));
		sub.close();
		bus.post(new BaseEvent("b"));
		assertEquals("关闭后不再收到", 1, count.get());
		assertFalse(sub.isActive());
		sub.close(); // 幂等
	}

	@Test
	public void unsubscribeByListener() {
		AtomicInteger count = new AtomicInteger();
		java.util.function.Consumer<BaseEvent> listener = e -> count.incrementAndGet();
		bus.subscribe(BaseEvent.class, listener);
		bus.unsubscribe(BaseEvent.class, listener);
		bus.post(new BaseEvent("a"));
		assertEquals("取消后不再收到", 0, count.get());
	}

	@Test
	public void multipleListenersAndNoDuplicate() {
		AtomicInteger a = new AtomicInteger();
		AtomicInteger b = new AtomicInteger();
		java.util.function.Consumer<BaseEvent> listener = e -> {
			a.incrementAndGet();
			b.incrementAndGet();
		};
		bus.subscribe(BaseEvent.class, listener);
		bus.post(new BaseEvent("x"));
		assertEquals(1, a.get());
		assertEquals(1, b.get());
	}

	@Test
	public void postAsyncRunsOffThread() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<String> thread = new AtomicReference<>();
		bus.subscribe(BaseEvent.class, e -> {
			thread.set(Thread.currentThread().getName());
			latch.countDown();
		});
		bus.postAsync(new BaseEvent("async"));
		assertTrue("异步完成", latch.await(3, TimeUnit.SECONDS));
		assertFalse("非调用线程", "main".equals(thread.get()));
	}

	@Test
	public void listenerExceptionDoesNotBlockOthers() {
		AtomicInteger ok = new AtomicInteger();
		AtomicReference<Throwable> handled = new AtomicReference<>();
		bus.setExceptionHandler((t, e) -> handled.set(t));
		bus.subscribe(BaseEvent.class, e -> {
			throw new IllegalStateException("boom");
		});
		bus.subscribe(BaseEvent.class, e -> ok.incrementAndGet());
		bus.post(new BaseEvent("x"));
		assertEquals("其他监听器正常", 1, ok.get());
		assertTrue("异常进入处理器", handled.get() instanceof IllegalStateException);
	}

	@Test
	public void annotationRegisterAndUnregister() {
		AnnotatedListener listener = new AnnotatedListener();
		int count = bus.register(listener);
		assertEquals("注册 2 个注解方法", 2, count);
		bus.post(new SubEvent("s"));
		assertTrue("子类监听触发", listener.received.contains("sub:s"));
		assertTrue("父类监听也触发", listener.received.contains("base:s"));
		bus.unregister(listener);
		listener.received.clear();
		bus.post(new SubEvent("t"));
		assertEquals("注销后不再收到", 0, listener.received.size());
		assertEquals("注销后计数归零", 0, bus.listenerCount());
	}

	@Test
	public void concurrentPostNoLoss() throws Exception {
		int threads = 10;
		int perThread = 100;
		AtomicInteger count = new AtomicInteger();
		bus.subscribe(BaseEvent.class, e -> count.incrementAndGet());
		CountDownLatch latch = new CountDownLatch(threads);
		for (int i = 0; i < threads; i++) {
			Thread.ofPlatform().start(() -> {
				for (int j = 0; j < perThread; j++) {
					bus.post(new BaseEvent("x"));
				}
				latch.countDown();
			});
		}
		assertTrue(latch.await(10, TimeUnit.SECONDS));
		assertEquals("无丢失", threads * perThread, count.get());
	}

	@Test
	public void invalidInput() {
		assertThrows(NullPointerException.class, () -> bus.subscribe(null, e -> {
		}));
		assertThrows(NullPointerException.class, () -> bus.subscribe(BaseEvent.class, null));
		assertThrows(NullPointerException.class, () -> bus.post(null));
		assertThrows(NullPointerException.class, () -> bus.register(null));
	}

	/**
	 * 无监听器事件自动投递 DeadEvent。
	 */
	@Test
	public void testDeadEventOnUnconsumed() {
		EventBus bus = new EventBus();
		java.util.List<Object> dead = new java.util.ArrayList<>();
		bus.subscribe(EventBus.DeadEvent.class, e -> dead.add(e.getEvent()));
		bus.post("no-subscriber-event");
		assertEquals(1, dead.size());
		assertEquals("no-subscriber-event", dead.get(0));
	}

	/**
	 * 有监听器时不触发 DeadEvent。
	 */
	@Test
	public void testNoDeadEventWhenConsumed() {
		EventBus bus = new EventBus();
		java.util.List<Object> dead = new java.util.ArrayList<>();
		java.util.List<String> received = new java.util.ArrayList<>();
		bus.subscribe(EventBus.DeadEvent.class, e -> dead.add(e.getEvent()));
		bus.subscribe(String.class, received::add);
		bus.post("consumed");
		assertEquals(java.util.List.of("consumed"), received);
		assertTrue(dead.isEmpty());
	}

}