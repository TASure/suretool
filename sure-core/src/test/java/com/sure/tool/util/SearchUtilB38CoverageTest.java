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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

/**
 * {@link SearchUtil} 批38覆盖率补测：逆序比较器上下界、插值边界。
 */
public class SearchUtilB38CoverageTest {

	@Test
	public void upperBound逆序比较器() {
		List<Integer> list = Arrays.asList(9, 7, 5, 3, 1);
		Comparator<Integer> desc = Comparator.reverseOrder();
		// desc 序下 upperBound = 第一个严格小于 key 的位置
		assertEquals(2, SearchUtil.upperBound(list, 7, desc));
		assertEquals(1, SearchUtil.upperBound(list, 8, desc));
		assertEquals(5, SearchUtil.upperBound(list, 0, desc));
	}

	@Test
	public void upperBound逆序命中与未命中() {
		List<Integer> list = Arrays.asList(9, 7, 7, 5, 3, 1);
		Comparator<Integer> desc = Comparator.reverseOrder();
		assertEquals(3, SearchUtil.upperBound(list, 7, desc));
		assertEquals(1, SearchUtil.upperBound(list, 8, desc));
	}

	@Test
	public void lowerBound逆序全范围() {
		List<Integer> list = Arrays.asList(9, 7, 5, 3, 1);
		Comparator<Integer> desc = Comparator.reverseOrder();
		assertEquals(0, SearchUtil.lowerBound(list, 10, desc));
		assertEquals(5, SearchUtil.lowerBound(list, 0, desc));
	}

	@Test
	public void interpolationSearch非均匀键() {
		long[] sorted = { 1, 2, 1000 };
		assertEquals(1, SearchUtil.interpolationSearch(sorted, 2));
		assertEquals(-1, SearchUtil.interpolationSearch(sorted, 3));
		assertEquals(2, SearchUtil.interpolationSearch(sorted, 1000));
	}

	@Test
	public void interpolationSearch全部相同元素() {
		long[] sorted = { 5, 5, 5, 5 };
		assertEquals(0, SearchUtil.interpolationSearch(sorted, 5));
	}

	@Test
	public void exponentialSearch边界命中() {
		long[] sorted = { 10, 20, 30, 40 };
		assertEquals(0, SearchUtil.exponentialSearch(sorted, 10));
		assertEquals(3, SearchUtil.exponentialSearch(sorted, 40));
	}
}
