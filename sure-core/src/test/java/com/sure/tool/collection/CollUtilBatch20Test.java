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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 批20：CollUtil 增补方法测试（newTreeSet/newConcurrentHashSet/flatMap/swap/toArray/maxCount/containsAny）。
 */
public class CollUtilBatch20Test {

	/**
	 * newTreeSet：比较器 + 变参填充。
	 */
	@Test
	public void testNewTreeSet() {
		TreeSet<Integer> set = CollUtil.newTreeSet(Comparator.reverseOrder(), 3, 1, 2);
		Assert.assertEquals(List.of(3, 2, 1), new java.util.ArrayList<>(set));
		TreeSet<String> natural = CollUtil.newTreeSet(null, "b", "a");
		Assert.assertEquals(List.of("a", "b"), new java.util.ArrayList<>(natural));
	}

	/**
	 * newConcurrentHashSet：并发 Set 基础行为。
	 */
	@Test
	public void testNewConcurrentHashSet() {
		Set<String> set = CollUtil.newConcurrentHashSet("a", "b", "a");
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains("a"));
		set.add("c");
		Assert.assertEquals(3, set.size());
	}

	/**
	 * flatMap 展平一层嵌套。
	 */
	@Test
	public void testFlatMap() {
		List<List<Integer>> nested = Arrays.asList(List.of(1, 2), List.of(3), null, List.of());
		List<Integer> flat = CollUtil.flatMap(nested);
		Assert.assertEquals(List.of(1, 2, 3), flat);
		Assert.assertTrue(CollUtil.flatMap(null).isEmpty());
	}

	/**
	 * swap 交换位置并返回原列表。
	 */
	@Test
	public void testSwap() {
		List<String> list = new java.util.ArrayList<>(List.of("a", "b", "c"));
		List<String> same = CollUtil.swap(list, 0, 2);
		Assert.assertSame(list, same);
		Assert.assertEquals(List.of("c", "b", "a"), list);
		Assert.assertThrows(IndexOutOfBoundsException.class, () -> CollUtil.swap(list, 0, 9));
	}

	/**
	 * toArray 反射建数组。
	 */
	@Test
	public void testToArray() {
		List<String> list = List.of("x", "y");
		String[] array = CollUtil.toArray(list, String.class);
		Assert.assertEquals(2, array.length);
		Assert.assertEquals("x", array[0]);
		Assert.assertNull(CollUtil.toArray(null, String.class));
		Integer[] nums = CollUtil.toArray(List.of(1, 2, 3), Integer.class);
		Assert.assertEquals(3, nums.length);
	}

	/**
	 * maxCount 众数（并列取先出现）。
	 */
	@Test
	public void testMaxCount() {
		Assert.assertEquals("b", CollUtil.maxCount(List.of("a", "b", "b", "c")));
		Assert.assertEquals("a", CollUtil.maxCount(List.of("a", "b")));
		Assert.assertNull(CollUtil.maxCount(List.of()));
		Assert.assertNull(CollUtil.maxCount(null));
	}

	/**
	 * containsAny 集合版交集判断。
	 */
	@Test
	public void testContainsAnyCollection() {
		Assert.assertTrue(CollUtil.containsAny(List.of(1, 2), List.of(2, 3)));
		Assert.assertFalse(CollUtil.containsAny(List.of(1, 2), List.of(3, 4)));
		Assert.assertFalse(CollUtil.containsAny(List.of(), List.of(1)));
		Assert.assertFalse(CollUtil.containsAny(null, List.of(1)));
	}
}
