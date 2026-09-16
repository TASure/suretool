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

import java.util.Map;

import com.sure.tool.util.StrUtil;
import com.sure.tool.collection.MapUtil;

/**
 * P4（v0.2.0）第十九批测试。
 */
public class P4Features14Test {

	@Test
	public void testBatch19() {
		// MapUtil.builder 链式
		Map<String, Integer> map = MapUtil.<String, Integer>builder().put("a", 1).put("b", 2).build();
		Assert.assertEquals(2, map.size());
		Assert.assertEquals(Integer.valueOf(1), map.get("a"));
		Assert.assertEquals(Integer.valueOf(2), map.get("b"));

		// StrUtil.fillBefore / fillAfter
		Assert.assertEquals("007", StrUtil.fillBefore("7", 3, '0'));
		Assert.assertEquals("7", StrUtil.fillBefore("7", 1, '0'));
		Assert.assertEquals("700", StrUtil.fillAfter("7", 3, '0'));
		Assert.assertEquals("7", StrUtil.fillAfter("7", 1, '0'));

		// ArrayUtil.wrap
		Assert.assertArrayEquals(new Integer[] {1, 2, 3}, com.sure.tool.util.ArrayUtil.wrap(new int[] {1, 2, 3}));
		Assert.assertArrayEquals(new Long[] {1L, 2L}, com.sure.tool.util.ArrayUtil.wrap(new long[] {1L, 2L}));
		Assert.assertArrayEquals(new Double[] {1.5, 2.5}, com.sure.tool.util.ArrayUtil.wrap(new double[] {1.5, 2.5}));
		Assert.assertNull(com.sure.tool.util.ArrayUtil.wrap((int[]) null));

		// DateUtil.toDate / beginOfHour / endOfHour
		java.util.Date d = com.sure.tool.date.DateUtil.parse("2026-09-16 10:30:00", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals(d, com.sure.tool.date.DateUtil.toDate(com.sure.tool.date.DateUtil.toCalendar(d)));
		java.util.Date bh = com.sure.tool.date.DateUtil.beginOfHour(d);
		Assert.assertEquals("2026-09-16 10:00:00",
				com.sure.tool.date.DateUtil.format(bh, "yyyy-MM-dd HH:mm:ss"));
		java.util.Date eh = com.sure.tool.date.DateUtil.endOfHour(d);
		Assert.assertEquals("2026-09-16 10:59:59",
				com.sure.tool.date.DateUtil.format(eh, "yyyy-MM-dd HH:mm:ss"));

		// NumberUtil.percent
		Assert.assertEquals("34.56%", com.sure.tool.util.NumberUtil.percent(0.3456, 2));
		Assert.assertEquals("100.00%", com.sure.tool.util.NumberUtil.percent(1.0, 2));
	}
}
