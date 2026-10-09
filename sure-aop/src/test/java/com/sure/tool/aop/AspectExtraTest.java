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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.junit.Test;

/**
 * ProxyUtil / Aspect 边界补测：默认 afterException 空实现、hashCode 与未知 Object 方法分支。
 */
public class AspectExtraTest {

	/** 被测接口 */
	public interface Greeter {

		/** @param name 名字
		 *  @return 问候 */
		String greet(String name);

		/** @return 无意义 */
		int add();

		/** @param msg 消息
		 *  @return 永不返回 */
		String fail(String msg);
	}

	/** 会抛异常的实现 */
	private static Greeter throwingTarget() {
		return new Greeter() {
			@Override
			public String greet(String name) {
				return "";
			}

			@Override
			public int add() {
				return 0;
			}

			@Override
			public String fail(String msg) {
				throw new IllegalStateException(msg);
			}
		};
	}

	@Test
	public void 未覆写afterException走默认空实现() {
		final Greeter proxy = ProxyUtil.proxy(throwingTarget(), new Aspect() {
		});
		final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> proxy.fail("boom"));
		assertEquals("boom", ex.getMessage());
	}

	@Test
	public void 代理hashCode走默认语义() {
		final Greeter proxy = ProxyUtil.proxy(throwingTarget(), new Aspect() {
		});
		assertEquals(System.identityHashCode(proxy), proxy.hashCode());
	}

	@Test
	public void 未知Object方法走default返回null() throws Throwable {
		final Greeter proxy = ProxyUtil.proxy(throwingTarget(), new Aspect() {
		});
		final InvocationHandler handler = Proxy.getInvocationHandler(proxy);
		final Object result = handler.invoke(proxy, Object.class.getMethod("notify"), null);
		assertNull(result);
	}
}
