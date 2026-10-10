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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批34（v1.15.0）：CollUtil 集合深度方法全分支测试。
 */
public class P7CollectionGapTest {

	// ================= partitionBy =================

	@Test
	public void testPartitionByMixed() {
		Partition<Integer> p = CollUtil.partitionBy(Arrays.asList(1, 2, 3, 4, 5), n -> n % 2 == 0);
		Assert.assertEquals(Arrays.asList(2, 4), p.matched());
		Assert.assertEquals(Arrays.asList(1, 3, 5), p.unmatched());
	}

	@Test
	public void testPartitionByAllMatch() {
		Partition<Integer> p = CollUtil.partitionBy(Arrays.asList(1, 2, 3), n -> n > 0);
		Assert.assertEquals(Arrays.asList(1, 2, 3), p.matched());
		Assert.assertTrue(p.unmatched().isEmpty());
	}

	@Test
	public void testPartitionByNoMatch() {
		Partition<Integer> p = CollUtil.partitionBy(Arrays.asList(1, 2, 3), n -> n > 9);
		Assert.assertTrue(p.matched().isEmpty());
		Assert.assertEquals(Arrays.asList(1, 2, 3), p.unmatched());
	}

	@Test
	public void testPartitionByNullAndEmpty() {
		Partition<String> p1 = CollUtil.partitionBy(null, s -> true);
		Assert.assertTrue(p1.matched().isEmpty());
		Assert.assertTrue(p1.unmatched().isEmpty());

		Partition<String> p2 = CollUtil.partitionBy(new ArrayList<>(), s -> true);
		Assert.assertTrue(p2.matched().isEmpty());
		Assert.assertTrue(p2.unmatched().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void testPartitionByNullPredicate() {
		CollUtil.partitionBy(Arrays.asList(1), null);
	}

	// ================= groupBy2 =================

	@Test
	public void testGroupBy2Nested() {
		List<String> items = Arrays.asList("a1", "a2", "b1", "a3", "b2");
		Map<Character, Map<Integer, List<String>>> result = CollUtil.groupBy2(
				items, s -> s.charAt(0), s -> Integer.valueOf(s.substring(1)));
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(Arrays.asList("a1"), result.get('a').get(1));
		Assert.assertEquals(Arrays.asList("a2"), result.get('a').get(2));
		Assert.assertEquals(Arrays.asList("a3"), result.get('a').get(3));
		Assert.assertEquals(Arrays.asList("b1"), result.get('b').get(1));
		Assert.assertEquals(Arrays.asList("b2"), result.get('b').get(2));
	}

	@Test
	public void testGroupBy2EmptyAndNull() {
		Map<Character, Map<Integer, List<String>>> e = CollUtil.groupBy2(
				new ArrayList<>(), s -> s.charAt(0), s -> s.length());
		Assert.assertTrue(e.isEmpty());

		Map<Character, Map<Integer, List<String>>> n = CollUtil.groupBy2(
				null, s -> s.charAt(0), s -> s.length());
		Assert.assertTrue(n.isEmpty());
	}

	// ================= addAllDistinct =================

	@Test
	public void testAddAllDistinctMixed() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b"));
		Assert.assertTrue(CollUtil.addAllDistinct(target, Arrays.asList("b", "c", "a")));
		Assert.assertEquals(Arrays.asList("a", "b", "c"), target);
	}

	@Test
	public void testAddAllDistinctNoChange() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b"));
		Assert.assertFalse(CollUtil.addAllDistinct(target, Arrays.asList("b", "a")));
		Assert.assertEquals(Arrays.asList("a", "b"), target);
	}

	@Test
	public void testAddAllDistinctAllNew() {
		List<String> target = new ArrayList<>();
		Assert.assertTrue(CollUtil.addAllDistinct(target, Arrays.asList("x", "y", "y")));
		Assert.assertEquals(Arrays.asList("x", "y"), target);
	}

	@Test
	public void testAddAllDistinctNull() {
		List<String> target = new ArrayList<>(Arrays.asList("a"));
		Assert.assertFalse(CollUtil.addAllDistinct(target, null));
		Assert.assertFalse(CollUtil.addAllDistinct(null, Arrays.asList("b")));
		Assert.assertEquals(Arrays.asList("a"), target);
	}

	// ================= removeAll（原地差集） =================

	@Test
	public void testRemoveAllHit() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b", "c", "b"));
		Assert.assertTrue(CollUtil.removeAll(target, Arrays.asList("b", "z")));
		Assert.assertEquals(Arrays.asList("a", "c"), target);
	}

	@Test
	public void testRemoveAllNoHit() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b"));
		Assert.assertFalse(CollUtil.removeAll(target, Arrays.asList("x", "y")));
		Assert.assertEquals(Arrays.asList("a", "b"), target);
	}

	@Test
	public void testRemoveAllSelfSafe() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b", "c"));
		Assert.assertTrue(CollUtil.removeAll(target, target));
		Assert.assertTrue(target.isEmpty());
	}

	@Test
	public void testRemoveAllNull() {
		List<String> target = new ArrayList<>(Arrays.asList("a"));
		Assert.assertFalse(CollUtil.removeAll(target, null));
		Assert.assertFalse(CollUtil.removeAll(null, Arrays.asList("a")));
		Assert.assertEquals(Arrays.asList("a"), target);
	}

	// ================= retainAll（原地交集） =================

	@Test
	public void testRetainAllHit() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b", "c"));
		Assert.assertTrue(CollUtil.retainAll(target, Arrays.asList("b", "c", "z")));
		Assert.assertEquals(Arrays.asList("b", "c"), target);
	}

	@Test
	public void testRetainAllNoChange() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b"));
		Assert.assertFalse(CollUtil.retainAll(target, Arrays.asList("a", "b")));
		Assert.assertEquals(Arrays.asList("a", "b"), target);
	}

	@Test
	public void testRetainAllEmptySource() {
		List<String> target = new ArrayList<>(Arrays.asList("a", "b"));
		Assert.assertTrue(CollUtil.retainAll(target, new ArrayList<>()));
		Assert.assertTrue(target.isEmpty());
	}

	@Test
	public void testRetainAllNull() {
		List<String> target = new ArrayList<>(Arrays.asList("a"));
		Assert.assertFalse(CollUtil.retainAll(target, null));
		Assert.assertFalse(CollUtil.retainAll(null, Arrays.asList("a")));
		Assert.assertEquals(Arrays.asList("a"), target);
	}

	// ================= flatten =================

	@Test
	public void testFlattenNested() {
		List<List<Integer>> nested = Arrays.asList(Arrays.asList(1, 2), null, Arrays.asList(3));
		Assert.assertEquals(Arrays.asList(1, 2, 3), CollUtil.flatten(nested));
	}

	@Test
	public void testFlattenEmptyAndNull() {
		Assert.assertTrue(CollUtil.flatten(null).isEmpty());
		Assert.assertTrue(CollUtil.flatten(new ArrayList<>()).isEmpty());
		Assert.assertTrue(CollUtil.flatten(Arrays.asList(null, null)).isEmpty());
	}

	// ================= pairwise =================

	@Test
	public void testPairwise() {
		Assert.assertEquals(4, CollUtil.pairwise(Arrays.asList(1, 2, 3, 4, 5)).size());
		Assert.assertEquals(Arrays.asList(2, 3), CollUtil.pairwise(Arrays.asList(1, 2, 3, 4, 5)).get(1));
	}

	@Test
	public void testPairwiseSingleAndEmpty() {
		Assert.assertTrue(CollUtil.pairwise(Arrays.asList(1)).isEmpty());
		Assert.assertTrue(CollUtil.pairwise(new ArrayList<>()).isEmpty());
		Assert.assertTrue(CollUtil.pairwise(null).isEmpty());
	}

	@Test
	public void testPairwiseOnSet() {
		Set<String> set = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
		List<List<String>> pairs = CollUtil.pairwise(set);
		Assert.assertEquals(2, pairs.size());
		Assert.assertEquals(Arrays.asList("a", "b"), pairs.get(0));
	}

	// ================= takeWhile =================

	@Test
	public void testTakeWhilePrefix() {
		Assert.assertEquals(Arrays.asList(1, 2, 3), CollUtil.takeWhile(Arrays.asList(1, 2, 3, 5), n -> n < 4));
	}

	@Test
	public void testTakeWhileFirstFails() {
		Assert.assertTrue(CollUtil.takeWhile(Arrays.asList(5, 1, 2), n -> n < 4).isEmpty());
	}

	@Test
	public void testTakeWhileAllMatch() {
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.takeWhile(Arrays.asList(1, 2), n -> n < 4));
	}

	@Test
	public void testTakeWhileEmptyAndNull() {
		Assert.assertTrue(CollUtil.takeWhile(new ArrayList<>(), n -> true).isEmpty());
		Assert.assertTrue(CollUtil.takeWhile(null, n -> true).isEmpty());
	}
}
