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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * EventBus 边界补测：默认总线、空取消、失效订阅跳过、非法注解、异常处理与死事件来源。
 */
public class EventBusExtraTest {

	/** 两参注解方法，用于触发签名校验异常。 */
	public static class BadListener {

		/** @param s 忽略 */
		@EventSubscribe
		public void onTooMany(String s, int i) {
		}
	}

	/** 注解方法抛异常，用于触发反射 InvocationTargetException 分支。 */
	public static class ThrowingListener {

		/** @param s 忽略 */
		@EventSubscribe
		public void on(String s) {
			throw new IllegalStateException("boom");
		}
	}

	@Test
	public void 默认总线实例可获取() {
		assertEquals(EventBus.getDefault(), EventBus.getDefault());
	}

	@Test
	public void 未注册类型取消订阅直接返回() {
		final EventBus bus = new EventBus();
		bus.unsubscribe(String.class, s -> {
		});
	}

	@Test
	public void 已关闭订阅在分发时跳过() {
		final EventBus bus = new EventBus();
		final AtomicInteger count = new AtomicInteger();
		final Subscription sub = bus.subscribe(String.class, s -> count.incrementAndGet());
		sub.close();
		bus.post("x");
		assertEquals(0, count.get());
	}

	@Test
	public void 非法注解方法签名抛异常() {
		final EventBus bus = new EventBus();
		assertThrows(IllegalArgumentException.class, () -> bus.register(new BadListener()));
	}

	@Test
	public void 默认异常处理器被触发() {
		final EventBus bus = new EventBus();
		bus.subscribe(String.class, s -> {
			throw new IllegalStateException("boom");
		});
		bus.post("x");
	}

	@Test
	public void 传入null恢复默认异常处理器() {
		final EventBus bus = new EventBus();
		bus.setExceptionHandler((t, e) -> {
		});
		bus.setExceptionHandler(null);
		bus.subscribe(String.class, s -> {
			throw new IllegalStateException("boom");
		});
		bus.post("x");
	}

	@Test
	public void 注解监听器抛异常走反射异常分支() {
		final EventBus bus = new EventBus();
		bus.setExceptionHandler((t, e) -> {
		});
		bus.register(new ThrowingListener());
		bus.post("boom");
	}

	@Test
	public void 分发途中关闭后续订阅命中跳过分支() {
		final EventBus bus = new EventBus();
		final Subscription[] holder = new Subscription[1];
		// 第一个监听器执行时关闭第二个订阅；快照迭代仍会遍历到已关闭的它
		bus.subscribe(String.class, s -> holder[0].close());
		holder[0] = bus.subscribe(String.class, s -> {
		});
		bus.post("x");
	}

	@Test
	public void 死事件可读取投递来源() {
		final EventBus bus = new EventBus();
		final AtomicReference<Object> source = new AtomicReference<>();
		bus.subscribe(EventBus.DeadEvent.class, (EventBus.DeadEvent dead) -> source.set(dead.getSource()));
		bus.post("无人监听的消息");
		assertSame(bus, source.get());
	}
}
