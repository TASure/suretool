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

/**
 * P4（v0.2.0）第三十四批测试：长整型序列/最大最小值、正则末匹配、转换便捷、本地 MAC。
 */
public class P4Features29Test {

	@Test
	public void testNumberRangeAndMinMax() {
		long[] seq = com.sure.tool.util.NumberUtil.range(1L, 7L, 2L);
		Assert.assertEquals(3, seq.length);
		Assert.assertEquals(1L, seq[0]);
		Assert.assertEquals(3L, seq[1]);
		Assert.assertEquals(5L, seq[2]);

		Assert.assertEquals(0, com.sure.tool.util.NumberUtil.range(5L, 1L, 1L).length);
		try {
			com.sure.tool.util.NumberUtil.range(1L, 5L, 0L);
			Assert.fail("步长为 0 应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}

		Assert.assertEquals(9, com.sure.tool.util.NumberUtil.max(1, 9, 5).intValue());
		Assert.assertEquals(2.5D, com.sure.tool.util.NumberUtil.max(1.0, 2.5, 2.0).doubleValue(), 0.0001D);
		Assert.assertEquals(-3, com.sure.tool.util.NumberUtil.min(1, -3, 5).intValue());
		Assert.assertNull(com.sure.tool.util.NumberUtil.max((Number[]) null));
		Assert.assertNull(com.sure.tool.util.NumberUtil.min(new Number[] { null, null }));
		Assert.assertEquals(7, com.sure.tool.util.NumberUtil.max(null, 7, null).intValue());
	}

	@Test
	public void testReGetLast() {
		String content = "id=1;id=2;id=3";
		Assert.assertEquals("3", com.sure.tool.util.ReUtil.getLast("id=(\\d+)", content, 1));
		Assert.assertEquals("id=3", com.sure.tool.util.ReUtil.getLast("id=(\\d+)", content, 0));
		Assert.assertEquals("3", com.sure.tool.util.ReUtil.getLast("id=(?<v>\\d+)", content, "v"));
		Assert.assertNull(com.sure.tool.util.ReUtil.getLast("xyz=(\\d+)", content, 1));
		Assert.assertNull(com.sure.tool.util.ReUtil.getLast("id=(\\d+)", null, 1));
		Assert.assertNull(com.sure.tool.util.ReUtil.getLast("id=(\\d+)", "no match here", 1));
	}

	@Test
	public void testConvertMore() {
		byte[] bytes = com.sure.tool.util.ConvertUtil.toByteArray(new int[] { 1, 2, 3 });
		Assert.assertEquals(3, bytes.length);
		Assert.assertEquals((byte) 1, bytes[0]);
		Assert.assertEquals((byte) 3, bytes[2]);
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toByteArray(null));

		java.util.Set<Object> set = com.sure.tool.util.ConvertUtil.toSet(new String[] { "a", "b", "a" });
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains("a"));
		Assert.assertTrue(set.contains("b"));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toSet(null));

		Assert.assertEquals(3.5F, com.sure.tool.util.ConvertUtil.toFloat(3.5D), 0.0001F);
		Assert.assertEquals(0F, com.sure.tool.util.ConvertUtil.toFloat(null), 0.0001F);
		Assert.assertEquals(2F, com.sure.tool.util.ConvertUtil.toFloat("2"), 0.0001F);
	}

	@Test
	public void testLocalMac() {
		String mac = com.sure.tool.util.NetUtil.getLocalMacAddress();
		if (mac != null) {
			Assert.assertTrue("MAC 格式不正确: " + mac, mac.matches("([0-9A-F]{2}:){5}[0-9A-F]{2}"));
		}
	}


}
