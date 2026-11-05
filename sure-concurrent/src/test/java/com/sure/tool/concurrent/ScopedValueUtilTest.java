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
package com.sure.tool.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link ScopedValueUtil} 单元测试。
 */
public class ScopedValueUtilTest {

	/**
	 * 绑定后在作用域内可见，退出后自动解除。
	 */
	@Test
	public void whereRunnableBindsAndRestores() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		ScopedValueUtil.where(key, "req-1", () -> {
			assertTrue(ScopedValueUtil.isBound(key));
			assertEquals("req-1", key.get());
		});
		assertFalse(ScopedValueUtil.isBound(key));
	}

	/**
	 * 有返回值版本在作用域内取到绑定值。
	 */
	@Test
	public void whereSupplierReturnsValue() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		String value = ScopedValueUtil.where(key, "req-2", (java.util.function.Supplier<String>) key::get);
		assertEquals("req-2", value);
	}

	/**
	 * 嵌套绑定：内层覆盖外层，退出内层恢复外层。
	 */
	@Test
	public void nestedWhereRestoresOuterBinding() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		ScopedValueUtil.where(key, "outer", () -> {
			ScopedValueUtil.where(key, "inner", () -> assertEquals("inner", key.get()));
			assertEquals("outer", key.get());
		});
	}

	/**
	 * null 是合法绑定值。
	 */
	@Test
	public void nullValueIsValidBinding() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		ScopedValueUtil.where(key, null, () -> {
			assertTrue(ScopedValueUtil.isBound(key));
			assertNull(key.get());
			assertNull(ScopedValueUtil.orElse(key, "default"));
		});
	}

	/**
	 * orElse：未绑定返回缺省值，绑定返回实际值。
	 */
	@Test
	public void orElseReturnsDefaultOnlyWhenUnbound() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		assertEquals("default", ScopedValueUtil.orElse(key, "default"));
		assertNull(ScopedValueUtil.orElse(key, null));
		ScopedValueUtil.where(key, "bound", () -> assertEquals("bound", ScopedValueUtil.orElse(key, "default")));
	}

	/**
	 * 绑定不跨线程：新启动的虚拟线程看不到当前绑定。
	 *
	 * @throws InterruptedException 线程等待被中断
	 */
	@Test
	public void bindingIsThreadLocal() throws InterruptedException {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		boolean[] visibleInOtherThread = {true};
		ScopedValueUtil.where(key, "main", () -> {
			Thread thread = Thread.ofVirtual().start(() -> visibleInOtherThread[0] = ScopedValueUtil.isBound(key));
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			}
		});
		assertFalse(visibleInOtherThread[0]);
	}

	/**
	 * 入参为 null 抛 NullPointerException。
	 */
	@Test
	public void nullArgumentsRejected() {
		ScopedValue<String> key = ScopedValueUtil.newKey();
		expectNpe(() -> ScopedValueUtil.where(null, "v", () -> { }));
		expectNpe(() -> ScopedValueUtil.where(key, "v", (Runnable) null));
		expectNpe(() -> ScopedValueUtil.where(key, "v", (java.util.function.Supplier<String>) null));
		expectNpe(() -> ScopedValueUtil.orElse(null, "v"));
		expectNpe(() -> ScopedValueUtil.isBound(null));
	}

	private static void expectNpe(Runnable action) {
		try {
			action.run();
			throw new AssertionError("预期抛出 NullPointerException");
		} catch (NullPointerException expected) {
			// 预期路径
		}
	}
}
