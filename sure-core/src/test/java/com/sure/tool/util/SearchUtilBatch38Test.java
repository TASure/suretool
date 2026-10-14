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

package com.sure.tool.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

/**
 * {@link SearchUtil} 批38补测：上下界、范围查询、二分、插值/指数查找。
 */
public class SearchUtilBatch38Test {

	@Test
	public void lowerBound命中() {
		List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
		assertEquals(2, SearchUtil.lowerBound(list, 5));
	}

	@Test
	public void lowerBound未命中取插入点() {
		List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
		assertEquals(2, SearchUtil.lowerBound(list, 4));
	}

	@Test
	public void lowerBound小于全部() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(0, SearchUtil.lowerBound(list, 0));
	}

	@Test
	public void lowerBound大于全部() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(3, SearchUtil.lowerBound(list, 9));
	}

	@Test
	public void lowerBound重复元素取第一个() {
		List<Integer> list = Arrays.asList(1, 2, 2, 2, 3);
		assertEquals(1, SearchUtil.lowerBound(list, 2));
	}

	@Test
	public void lowerBound空列表() {
		assertEquals(0, SearchUtil.lowerBound(Collections.emptyList(), 1));
	}

	@Test
	public void lowerBound逆序比较器() {
		List<Integer> list = Arrays.asList(9, 7, 5, 3, 1);
		Comparator<Integer> desc = Comparator.reverseOrder();
		// 第一个 <= key 的位置
		assertEquals(2, SearchUtil.lowerBound(list, 5, desc));
		assertEquals(0, SearchUtil.lowerBound(list, 9, desc));
	}

	@Test
	public void lowerBound空参抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.lowerBound(null, 1));
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.lowerBound(Arrays.asList(1), 1, null));
	}

	@Test
	public void upperBound命中取最后一个之后() {
		List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
		assertEquals(3, SearchUtil.upperBound(list, 5));
	}

	@Test
	public void upperBound重复元素() {
		List<Integer> list = Arrays.asList(1, 2, 2, 2, 3);
		assertEquals(4, SearchUtil.upperBound(list, 2));
	}

	@Test
	public void upperBound未命中() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(2, SearchUtil.upperBound(list, 4));
	}

	@Test
	public void upperBound大于全部返回size() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(3, SearchUtil.upperBound(list, 9));
	}

	@Test
	public void upperBound小于全部() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(0, SearchUtil.upperBound(list, 0));
	}

	@Test
	public void upperBound空列表() {
		assertEquals(0, SearchUtil.upperBound(Collections.emptyList(), 1));
	}

	@Test
	public void range区间内容() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
		List<Integer> range = SearchUtil.range(list, 2, 5);
		assertEquals(Arrays.asList(2, 3, 4), range);
	}

	@Test
	public void range无命中为空() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
		assertTrue(SearchUtil.range(list, 7, 9).isEmpty());
	}

	@Test
	public void range全范围() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
		// [from, to) 区间：to 需大于最大元素
		assertEquals(list, SearchUtil.range(list, 1, 7));
	}

	@Test
	public void range返回不可变() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4);
		List<Integer> range = SearchUtil.range(list, 2, 4);
		assertThrows(UnsupportedOperationException.class, () -> range.add(9));
	}

	@Test
	public void binarySearch命中() {
		List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
		assertEquals(2, SearchUtil.binarySearch(list, 5, Comparator.naturalOrder()));
	}

	@Test
	public void binarySearch未命中插入点() {
		List<Integer> list = Arrays.asList(1, 3, 5, 7, 9);
		assertEquals(-3, SearchUtil.binarySearch(list, 4, Comparator.naturalOrder()));
	}

	@Test
	public void binarySearch逆序比较器() {
		List<Integer> list = Arrays.asList(9, 7, 5, 3, 1);
		assertEquals(2, SearchUtil.binarySearch(list, 5, Comparator.reverseOrder()));
	}

	@Test
	public void binarySearch空列表() {
		assertEquals(-1, SearchUtil.binarySearch(Collections.emptyList(), 1, Comparator.naturalOrder()));
	}

	@Test
	public void binarySearchNullKey() {
		List<Integer> list = Arrays.asList(1, 3, 5);
		assertEquals(-1, SearchUtil.binarySearch(list, null, Comparator.naturalOrder()));
	}

	@Test
	public void binarySearch空参抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.binarySearch(null, 1, Comparator.naturalOrder()));
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.binarySearch(Arrays.asList(1), 1, null));
	}

	@Test
	public void interpolationSearch均匀命中() {
		long[] sorted = { 1, 3, 5, 7, 9, 11, 13, 15 };
		assertEquals(3, SearchUtil.interpolationSearch(sorted, 7));
		assertEquals(0, SearchUtil.interpolationSearch(sorted, 1));
		assertEquals(7, SearchUtil.interpolationSearch(sorted, 15));
	}

	@Test
	public void interpolationSearch未命中() {
		long[] sorted = { 1, 3, 5, 7, 9 };
		assertEquals(-1, SearchUtil.interpolationSearch(sorted, 4));
	}

	@Test
	public void interpolationSearch越界() {
		long[] sorted = { 1, 3, 5, 7, 9 };
		assertEquals(-1, SearchUtil.interpolationSearch(sorted, 0));
		assertEquals(-1, SearchUtil.interpolationSearch(sorted, 10));
	}

	@Test
	public void interpolationSearch空数组() {
		assertEquals(-1, SearchUtil.interpolationSearch(new long[0], 1));
	}

	@Test
	public void interpolationSearch非均匀数组() {
		// 指数分布（插值会 miss 多次但循环收敛）
		long[] sorted = { 1, 2, 4, 8, 16, 32, 64, 128 };
		assertEquals(5, SearchUtil.interpolationSearch(sorted, 32));
		assertEquals(-1, SearchUtil.interpolationSearch(sorted, 50));
	}

	@Test
	public void interpolationSearch单元素() {
		assertEquals(0, SearchUtil.interpolationSearch(new long[] { 5 }, 5));
		assertEquals(-1, SearchUtil.interpolationSearch(new long[] { 5 }, 6));
	}

	@Test
	public void interpolationSearch空参抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.interpolationSearch(null, 1));
	}

	@Test
	public void exponentialSearch命中() {
		long[] sorted = { 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23 };
		assertEquals(2, SearchUtil.exponentialSearch(sorted, 5));
		assertEquals(10, SearchUtil.exponentialSearch(sorted, 21));
	}

	@Test
	public void exponentialSearch未命中() {
		long[] sorted = { 1, 3, 5, 7, 9 };
		assertEquals(-1, SearchUtil.exponentialSearch(sorted, 6));
	}

	@Test
	public void exponentialSearch小于首元素() {
		long[] sorted = { 5, 10, 15 };
		assertEquals(-1, SearchUtil.exponentialSearch(sorted, 1));
	}

	@Test
	public void exponentialSearch空数组() {
		assertEquals(-1, SearchUtil.exponentialSearch(new long[0], 1));
	}

	@Test
	public void exponentialSearch单元素() {
		assertEquals(0, SearchUtil.exponentialSearch(new long[] { 5 }, 5));
		assertEquals(-1, SearchUtil.exponentialSearch(new long[] { 5 }, 4));
	}

	@Test
	public void exponentialSearch首元素命中() {
		long[] sorted = { 1, 2, 3, 4 };
		assertEquals(0, SearchUtil.exponentialSearch(sorted, 1));
	}

	@Test
	public void exponentialSearch大数组() {
		long[] sorted = new long[1000];
		for (int i = 0; i < sorted.length; i++) {
			sorted[i] = i * 2L;
		}
		assertEquals(500, SearchUtil.exponentialSearch(sorted, 1000));
		assertEquals(999, SearchUtil.exponentialSearch(sorted, 1998));
		assertEquals(-1, SearchUtil.exponentialSearch(sorted, 1999));
	}

	@Test
	public void exponentialSearch空参抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.exponentialSearch(null, 1));
	}

	@Test
	public void range空参抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> SearchUtil.range(null, 1, 2));
	}

	@Test
	public void rangeString列表() {
		List<String> list = Arrays.asList("apple", "banana", "cherry", "date");
		assertEquals(Arrays.asList("banana", "cherry"), SearchUtil.range(list, "b", "d"));
	}
}
