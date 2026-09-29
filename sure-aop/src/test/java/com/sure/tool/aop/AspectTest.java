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

package com.sure.tool.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * ProxyUtil / Aspect 测试：通知顺序、方法匹配、异常语义与边界。
 */
public class AspectTest {

	/** 被测接口 */
	public interface Greeter {
		String greet(String name);

		int add(int a, int b);

		String fail(String msg);
	}

	/** 记录通知事件 */
	private static final class Recorder {
		final List<String> events = new ArrayList<>();
	}

	@Test
	public void 前置后置通知按序触发且返回值透传() {
		final Recorder recorder = new Recorder();
		final Aspect aspect = new Aspect() {
			@Override
			public void before(Object target, Method method, Object[] args) {
				recorder.events.add("before:" + method.getName());
			}

			@Override
			public void after(Object target, Method method, Object[] args, Object result) {
				recorder.events.add("after:" + result);
			}
		};
		final Greeter proxy = ProxyUtil.proxy(new Greeter() {
			@Override
			public String greet(String name) {
				return "hi " + name;
			}

			@Override
			public int add(int a, int b) {
				return a + b;
			}

			@Override
			public String fail(String msg) {
				throw new IllegalStateException(msg);
			}
		}, aspect);

		assertEquals("hi sure", proxy.greet("sure"));
		assertEquals(List.of("before:greet", "after:hi sure"), recorder.events);
		assertEquals(5, proxy.add(2, 3));
	}

	@Test
	public void match为false时直接调用不触发通知() {
		final Recorder recorder = new Recorder();
		final Aspect aspect = new Aspect() {
			@Override
			public boolean match(Method method) {
				return method.getName().startsWith("greet");
			}

			@Override
			public void before(Object target, Method method, Object[] args) {
				recorder.events.add(method.getName());
			}
		};
		final Greeter proxy = ProxyUtil.proxy(new Greeter() {
			@Override
			public String greet(String name) {
				return "hi";
			}

			@Override
			public int add(int a, int b) {
				return 99;
			}

			@Override
			public String fail(String msg) {
				return "f";
			}
		}, aspect);

		proxy.greet("x");
		proxy.add(1, 1);
		assertEquals("仅 greet 触发通知", List.of("greet"), recorder.events);
	}

	@Test
	public void 异常触发afterException且向外传播() {
		final Recorder recorder = new Recorder();
		final Aspect aspect = new Aspect() {
			@Override
			public void afterException(Object target, Method method, Object[] args, Throwable e) {
				recorder.events.add("ex:" + e.getMessage());
			}
		};
		final Greeter proxy = ProxyUtil.proxy(new Greeter() {
			@Override
			public String greet(String name) {
				return "";
			}

			@Override
			public int add(int a, int b) {
				return 0;
			}

			@Override
			public String fail(String msg) {
				throw new IllegalStateException(msg);
			}
		}, aspect);

		final IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> proxy.fail("boom"));
		assertEquals("boom", ex.getMessage());
		assertEquals(List.of("ex:boom"), recorder.events);
	}

	@Test
	public void 非接口目标抛异常且提示字节码库() {
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ProxyUtil.proxy(new PlainClass(), new Aspect() {
				}));
		assertTrue("提示应含字节码库指引", ex.getMessage().contains("CGLIB"));
	}

	@Test
	public void 接口代理无目标触发通知返回null() {
		final Recorder recorder = new Recorder();
		final Aspect aspect = new Aspect() {
			@Override
			public void before(Object target, Method method, Object[] args) {
				recorder.events.add("before:" + method.getName());
			}

			@Override
			public void after(Object target, Method method, Object[] args, Object result) {
				assertNull("无目标模式返回值应为 null", result);
			}
		};
		final Greeter proxy = ProxyUtil.proxy(Greeter.class, aspect);
		assertNull(proxy.greet("x"));
		assertEquals(List.of("before:greet"), recorder.events);
	}

	@Test
	public void null参数校验() {
		assertThrows(IllegalArgumentException.class, () -> ProxyUtil.proxy((Object) null, new Aspect() {
		}));
		assertThrows(IllegalArgumentException.class, () -> ProxyUtil.proxy(new PlainClass(), null));
		assertThrows(IllegalArgumentException.class, () -> ProxyUtil.proxy((Class<?>) null, new Aspect() {
		}));
		assertThrows(IllegalArgumentException.class, () -> ProxyUtil.proxy(PlainClass.class, new Aspect() {
		}));
	}

	@Test
	public void Object通用方法默认语义() {
		final Greeter proxy = ProxyUtil.proxy(new Greeter() {
			@Override
			public String greet(String name) {
				return "";
			}

			@Override
			public int add(int a, int b) {
				return 0;
			}

			@Override
			public String fail(String msg) {
				return "";
			}
		}, new Aspect() {
		});
		assertNotNull(proxy.toString());
		assertFalse(proxy.equals(null));
		assertTrue(proxy.equals(proxy));
	}

	/** 无接口普通类，用于边界测试 */
	public static class PlainClass {
	}
}
