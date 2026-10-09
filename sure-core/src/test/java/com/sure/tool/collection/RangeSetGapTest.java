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
package com.sure.tool.collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * RangeSet 覆盖率补测：区间包含、合并、toString。
 *
 * @author suretool
 * @since 1.13.1
 */
public class RangeSetGapTest {

	@Test
	public void testContainsAndMerge() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(0, 10);
		rs.add(5, 15);
		Assert.assertTrue(rs.contains(7));
		Assert.assertFalse(rs.contains(20));
		Assert.assertEquals("[0, 15)", rs.ranges().get(0).toString());
		Assert.assertTrue(rs.toString().contains("0"));
		rs.add(20, 30);
		Assert.assertEquals(2, rs.ranges().size());
		rs.clear();
		Assert.assertTrue(rs.isEmpty());
	}

	@Test
	public void testOverlapMergeAndRemove() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(0, 10);
		rs.add(20, 30);
		// 跨两个区间合并：floor 扩展 + ceiling 扩展 newUpper
		rs.add(5, 25);
		Assert.assertTrue(rs.contains(22));
		Assert.assertEquals(1, rs.ranges().size());
		// 删除中间，尾部保留 [25,30)
		rs.remove(5, 20);
		Assert.assertFalse(rs.contains(10));
		Assert.assertTrue(rs.contains(28));
		// 完全删除一个区间
		RangeSet<Integer> rs2 = new RangeSet<>();
		rs2.add(0, 10);
		rs2.add(20, 22);
		rs2.remove(5, 25);
		Assert.assertFalse(rs2.contains(21));
		Assert.assertTrue(rs2.contains(2));
	}
}
