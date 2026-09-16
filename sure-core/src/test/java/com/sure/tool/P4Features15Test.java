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
package com.sure.tool;

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十批测试。
 */
public class P4Features15Test {

	@Test
	public void testBatch20() {
		// ArrayUtil.wrap 补齐
		Assert.assertArrayEquals(new Float[] {1.5f, 2.5f}, com.sure.tool.util.ArrayUtil.wrap(new float[] {1.5f, 2.5f}));
		Assert.assertArrayEquals(new Short[] {1, 2}, com.sure.tool.util.ArrayUtil.wrap(new short[] {1, 2}));
		Assert.assertArrayEquals(new Byte[] {1, 2}, com.sure.tool.util.ArrayUtil.wrap(new byte[] {1, 2}));
		Assert.assertArrayEquals(new Character[] {'a', 'b'}, com.sure.tool.util.ArrayUtil.wrap(new char[] {'a', 'b'}));
		Assert.assertArrayEquals(new Boolean[] {true, false}, com.sure.tool.util.ArrayUtil.wrap(new boolean[] {true, false}));

		// DateUtil.daysBetween / getWeekOfMonth
		Assert.assertEquals(1, com.sure.tool.date.DateUtil.daysBetween(
				com.sure.tool.date.DateUtil.parse("2026-09-01", "yyyy-MM-dd"),
				com.sure.tool.date.DateUtil.parse("2026-09-02", "yyyy-MM-dd")));
		Assert.assertEquals(-1, com.sure.tool.date.DateUtil.daysBetween(
				com.sure.tool.date.DateUtil.parse("2026-09-02", "yyyy-MM-dd"),
				com.sure.tool.date.DateUtil.parse("2026-09-01", "yyyy-MM-dd")));
		Assert.assertTrue(com.sure.tool.date.DateUtil.getWeekOfMonth(
				com.sure.tool.date.DateUtil.parse("2026-09-16", "yyyy-MM-dd")) >= 1);

		// MapUtil.inverse
		java.util.Map<String, Integer> m = java.util.Map.of("a", 1, "b", 2);
		java.util.Map<Integer, String> inv = com.sure.tool.collection.MapUtil.inverse(m);
		Assert.assertEquals("a", inv.get(1));
		Assert.assertEquals("b", inv.get(2));

		// StrUtil.toUnicode
		Assert.assertEquals("abc", StrUtil.toUnicode("abc"));
		String unicode = StrUtil.toUnicode("中");
		Assert.assertEquals('\\', unicode.charAt(0));
		Assert.assertEquals('u', unicode.charAt(1));
		Assert.assertEquals(6, unicode.length());
	}
}
