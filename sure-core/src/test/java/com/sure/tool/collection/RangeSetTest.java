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

import java.util.List;

/**
 * 批20：RangeSet 区间集合测试。
 */
public class RangeSetTest {

	/**
	 * 基本添加与包含判定。
	 */
	@Test
	public void testAddAndContains() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 5);
		Assert.assertTrue(rs.contains(1));
		Assert.assertTrue(rs.contains(3));
		Assert.assertTrue(rs.contains(4));
		Assert.assertFalse(rs.contains(5));
		Assert.assertFalse(rs.contains(0));
	}

	/**
	 * 首尾相接区间自动合并（[1,3) + [3,5) → [1,5)）。
	 */
	@Test
	public void testMergeAdjacent() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 3);
		rs.add(3, 5);
		Assert.assertEquals(1, rs.ranges().size());
		RangeSet.Range<Integer> merged = rs.ranges().get(0);
		Assert.assertEquals(1, merged.getLower().intValue());
		Assert.assertEquals(5, merged.getUpper().intValue());
		Assert.assertTrue(rs.contains(4));
		Assert.assertFalse(rs.contains(5));
	}

	/**
	 * 重叠区间合并（1-5 + 3-7 → 1-7）。
	 */
	@Test
	public void testMergeOverlap() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 5);
		rs.add(3, 7);
		Assert.assertEquals(1, rs.ranges().size());
		Assert.assertEquals(1, rs.ranges().get(0).getLower().intValue());
		Assert.assertEquals(7, rs.ranges().get(0).getUpper().intValue());
	}

	/**
	 * 包含关系合并（2-4 ⊂ 1-5 吸收）。
	 */
	@Test
	public void testMergeContained() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 5);
		rs.add(2, 4);
		Assert.assertEquals(1, rs.ranges().size());
		Assert.assertEquals(1, rs.ranges().get(0).getLower().intValue());
		Assert.assertEquals(5, rs.ranges().get(0).getUpper().intValue());
	}

	/**
	 * 多个离散区间保持分离。
	 */
	@Test
	public void testDisjointRanges() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 2);
		rs.add(10, 20);
		Assert.assertEquals(2, rs.ranges().size());
		Assert.assertFalse(rs.contains(5));
		Assert.assertTrue(rs.contains(15));
	}

	/**
	 * 非法参数：lower &gt; upper。
	 */
	@Test
	public void testInvalidRange() {
		RangeSet<Integer> rs = new RangeSet<>();
		Assert.assertThrows(IllegalArgumentException.class, () -> rs.add(5, 1));
		Assert.assertThrows(IllegalArgumentException.class, () -> rs.remove(5, 1));
	}

	/**
	 * remove 拆出两个子区间（删除中间段）。
	 */
	@Test
	public void testRemoveSplits() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 10);
		rs.remove(4, 6);
		Assert.assertEquals(2, rs.ranges().size());
		Assert.assertTrue(rs.contains(3));
		Assert.assertFalse(rs.contains(4));
		Assert.assertFalse(rs.contains(5));
		Assert.assertTrue(rs.contains(6));
		Assert.assertTrue(rs.contains(7));
	}

	/**
	 * remove 删除整段与首尾裁剪。
	 */
	@Test
	public void testRemoveEdge() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 10);
		rs.remove(1, 3);
		Assert.assertEquals(1, rs.ranges().size());
		Assert.assertTrue(rs.contains(3));
		Assert.assertTrue(rs.contains(9));
		rs.remove(3, 10);
		Assert.assertTrue(rs.isEmpty());
	}

	/**
	 * span 覆盖全集；空集返回 null。
	 */
	@Test
	public void testSpan() {
		RangeSet<Integer> rs = new RangeSet<>();
		Assert.assertNull(rs.span());
		rs.add(1, 3);
		rs.add(7, 9);
		RangeSet.Range<Integer> span = rs.span();
		Assert.assertEquals(1, span.getLower().intValue());
		Assert.assertEquals(9, span.getUpper().intValue());
	}

	/**
	 * clear / isEmpty / ranges 只读。
	 */
	@Test
	public void testClearAndReadonly() {
		RangeSet<Integer> rs = new RangeSet<>();
		rs.add(1, 5);
		Assert.assertFalse(rs.isEmpty());
		List<RangeSet.Range<Integer>> ranges = rs.ranges();
		Assert.assertThrows(UnsupportedOperationException.class, () -> ranges.add(new RangeSet.Range<>(9, 9)));
		rs.clear();
		Assert.assertTrue(rs.isEmpty());
		Assert.assertEquals(0, rs.ranges().size());
	}
}
