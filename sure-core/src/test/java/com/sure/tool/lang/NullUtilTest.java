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
package com.sure.tool.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * NullUtil 批27 测试。
 */
public class NullUtilTest {

	@Test
	public void testIsNullIsNonNull() {
		Assert.assertTrue(NullUtil.isNull(null));
		Assert.assertFalse(NullUtil.isNull("x"));
		Assert.assertTrue(NullUtil.isNonNull("x"));
		Assert.assertFalse(NullUtil.isNonNull(null));
	}

	@Test
	public void testIsAnyNull() {
		Assert.assertTrue(NullUtil.isAnyNull("a", null, "c"));
		Assert.assertFalse(NullUtil.isAnyNull("a", "b"));
		Assert.assertTrue(NullUtil.isAnyNull((Object[]) null));
	}

	@Test
	public void testIsAllNull() {
		Assert.assertTrue(NullUtil.isAllNull(null, null));
		Assert.assertFalse(NullUtil.isAllNull("a", null));
		Assert.assertTrue(NullUtil.isAllNull(new Object[0]));
		Assert.assertTrue(NullUtil.isAllNull((Object[]) null));
	}

	@Test
	public void testFirstNonNull() {
		Assert.assertEquals("b", NullUtil.firstNonNull(null, "b", "c"));
		Assert.assertEquals("a", NullUtil.firstNonNull("a", null, "c"));
		Assert.assertNull(NullUtil.firstNonNull(null, null));
		Assert.assertNull(NullUtil.firstNonNull((Object[]) null));
	}

	@Test
	public void testLastNonNull() {
		Assert.assertEquals("c", NullUtil.lastNonNull("a", "b", "c"));
		Assert.assertEquals("b", NullUtil.lastNonNull("a", "b", null));
		Assert.assertEquals("a", NullUtil.lastNonNull("a", null, null));
		Assert.assertNull(NullUtil.lastNonNull(null, null));
		Assert.assertNull(NullUtil.lastNonNull((Object[]) null));
	}

	@Test
	public void testCoalesce() {
		Assert.assertEquals("x", NullUtil.coalesce(null, null, "x"));
		Assert.assertNull(NullUtil.coalesce(null, null));
	}

	@Test
	public void testDefaultIfNull() {
		Assert.assertEquals("v", NullUtil.defaultIfNull("v", "d"));
		Assert.assertEquals("d", NullUtil.defaultIfNull(null, "d"));
		Assert.assertEquals("v", NullUtil.defaultIfNull("v", () -> "d"));
		Assert.assertEquals("d", NullUtil.defaultIfNull(null, () -> "d"));
	}

	@Test
	public void testApplyIfNotNull() {
		Assert.assertEquals("A", NullUtil.<String, String>applyIfNotNull("a", x -> x.toUpperCase()).get());
		Assert.assertTrue(NullUtil.<String, String>applyIfNotNull(null, x -> x.toUpperCase()).isEmpty());
		Assert.assertTrue(NullUtil.applyIfNotNull("a", x -> null).isEmpty());
		// 映射函数内部 NPE 正常传播（不吞）
		try {
			NullUtil.applyIfNotNull("a", x -> {
				String s = null;
				return s.length();
			});
			Assert.fail("应传播 NPE");
		} catch (NullPointerException expected) {
			// 预期
		}
	}

	@Test
	public void testConsumeIfNotNull() {
		java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
		NullUtil.consumeIfNotNull("v", x -> count.incrementAndGet());
		NullUtil.consumeIfNotNull(null, x -> count.incrementAndGet());
		Assert.assertEquals(1, count.get());
	}

	@Test
	public void testNullSafeToString() {
		Assert.assertEquals("", NullUtil.nullSafeToString(null));
		Assert.assertEquals("42", NullUtil.nullSafeToString(42));
		Assert.assertEquals("abc", NullUtil.nullSafeToString("abc"));
	}
}
