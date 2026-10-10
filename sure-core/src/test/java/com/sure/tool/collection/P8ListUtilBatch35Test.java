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
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批35（v1.16.0）：ListUtil 深度方法全分支测试。
 */
public class P8ListUtilBatch35Test {

	// ================= get =================

	@Test
	public void testGet() {
		List<String> list = Arrays.asList("a", "b", "c");
		Assert.assertEquals("a", ListUtil.get(list, 0));
		Assert.assertEquals("c", ListUtil.get(list, -1));
		Assert.assertEquals("b", ListUtil.get(list, -2));
		Assert.assertNull(ListUtil.get(list, 5));
		Assert.assertNull(ListUtil.get(list, -5));
		Assert.assertNull(ListUtil.get(null, 0));
		Assert.assertNull(ListUtil.get(new ArrayList<>(), 0));
	}

	// ================= subListSafe =================

	@Test
	public void testSubListSafe() {
		List<String> list = Arrays.asList("a", "b", "c", "d");
		Assert.assertEquals(Arrays.asList("b", "c"), ListUtil.subListSafe(list, 1, 3));
		Assert.assertEquals(Arrays.asList("a", "b", "c", "d"), ListUtil.subListSafe(list, -5, 99));
		Assert.assertTrue(ListUtil.subListSafe(list, 3, 1).isEmpty());
		Assert.assertTrue(ListUtil.subListSafe(null, 0, 1).isEmpty());
		Assert.assertTrue(ListUtil.subListSafe(new ArrayList<>(), 0, 1).isEmpty());
	}

	// ================= distinct =================

	@Test
	public void testDistinct() {
		Assert.assertEquals(Arrays.asList("a", "b", "c"), ListUtil.distinct(Arrays.asList("a", "b", "a", "c", "b")));
		Assert.assertTrue(ListUtil.distinct(null).isEmpty());
		Assert.assertTrue(ListUtil.distinct(new ArrayList<>()).isEmpty());
	}

	// ================= map / filter / flatMap =================

	@Test
	public void testMap() {
		Assert.assertEquals(Arrays.asList(2, 4, 6), ListUtil.map(Arrays.asList(1, 2, 3), (Integer n) -> n * 2));
		Assert.assertTrue(ListUtil.map(null, (Integer n) -> n * 2).isEmpty());
		Assert.assertEquals(Arrays.asList("1", null), ListUtil.map(Arrays.asList(1, null), n -> n == null ? null : String.valueOf(n)));
	}

	@Test
	public void testFilter() {
		Assert.assertEquals(Arrays.asList(2, 4), ListUtil.filter(Arrays.asList(1, 2, 3, 4), n -> n % 2 == 0));
		Assert.assertTrue(ListUtil.filter(Arrays.asList(1, 3), n -> n % 2 == 0).isEmpty());
		Assert.assertEquals(Arrays.asList(1, 2), ListUtil.filter(Arrays.asList(1, 2), n -> true));
		Assert.assertTrue(ListUtil.filter(null, n -> true).isEmpty());
	}

	@Test
	public void testFlatMap() {
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4), ListUtil.flatMap(
				Arrays.asList("ab", "cd"), s -> Arrays.asList(s.charAt(0) - 'a' + 1, s.charAt(1) - 'a' + 1)));
		Assert.assertEquals(Arrays.asList(1, 2), ListUtil.flatMap(
				Arrays.asList("a", "b", "c"), s -> s.equals("c") ? null : Arrays.asList(s.charAt(0) - 'a' + 1)));
		Assert.assertEquals(Arrays.asList(1), ListUtil.flatMap(
				Arrays.asList("a", "b"), s -> s.equals("b") ? new ArrayList<>() : Arrays.asList(1)));
		Assert.assertTrue(ListUtil.flatMap(null, s -> Arrays.asList(1)).isEmpty());
	}

	// ================= zip =================

	@Test
	public void testZip() {
		List<List<Object>> z1 = ListUtil.zip(Arrays.asList("a", "b", "c"), Arrays.asList(1, 2, 3));
		Assert.assertEquals(3, z1.size());
		Assert.assertEquals(Arrays.asList("a", 1), z1.get(0));

		List<List<Object>> z2 = ListUtil.zip(Arrays.asList("a", "b", "c"), Arrays.asList(1));
		Assert.assertEquals(1, z2.size());

		Assert.assertTrue(ListUtil.zip(Arrays.asList("a"), null).isEmpty());
		Assert.assertTrue(ListUtil.zip(null, Arrays.asList(1)).isEmpty());
		Assert.assertTrue(ListUtil.zip(new ArrayList<>(), new ArrayList<>()).isEmpty());
	}

	// ================= shuffle / shuffleCopy / sample =================

	@Test
	public void testShuffle() {
		Random fixed = new Random(42);
		List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
		List<Integer> same = ListUtil.shuffle(list, fixed);
		Assert.assertSame(list, same);
		Assert.assertEquals(3, list.size());

		List<Integer> one = new ArrayList<>(Arrays.asList(7));
		ListUtil.shuffle(one, fixed);
		Assert.assertEquals(Arrays.asList(7), one);

		Assert.assertNull(ListUtil.shuffle(null, fixed));
	}

	@Test
	public void testShuffleCopy() {
		Random fixed = new Random(42);
		List<Integer> original = new ArrayList<>(Arrays.asList(1, 2, 3));
		List<Integer> copy = ListUtil.shuffleCopy(original, fixed);
		Assert.assertNotSame(original, copy);
		Assert.assertEquals(3, copy.size());
		Assert.assertEquals(3, original.size());
		Assert.assertTrue(ListUtil.shuffleCopy(null, fixed).isEmpty());
	}

	@Test
	public void testSample() {
		Random fixed = new Random(7);
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
		Assert.assertTrue(ListUtil.sample(list, 2).size() == 2);
		Assert.assertTrue(ListUtil.sample(list, 0).isEmpty());
		Assert.assertEquals(5, ListUtil.sample(list, 10).size());
		Assert.assertTrue(ListUtil.sample(list, 5).size() == 5);
		Assert.assertTrue(ListUtil.sample(null, 2).isEmpty());
	}

	// ================= chunk =================

	@Test
	public void testChunk() {
		List<List<Integer>> c = ListUtil.chunk(Arrays.asList(1, 2, 3, 4, 5), 2);
		Assert.assertEquals(3, c.size());
		Assert.assertEquals(Arrays.asList(5), c.get(2));
		Assert.assertEquals(Arrays.asList(1, 2), ListUtil.chunk(Arrays.asList(1, 2), 2).get(0));
		Assert.assertTrue(ListUtil.chunk(null, 3).isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChunkInvalidSize() {
		ListUtil.chunk(Arrays.asList(1), 0);
	}

	// ================= union / intersection / subtract =================

	@Test
	public void testUnion() {
		Assert.assertEquals(Arrays.asList("a", "b", "c"), ListUtil.union(Arrays.asList("a", "b"), Arrays.asList("b", "c")));
		Assert.assertEquals(Arrays.asList("a", "b"), ListUtil.union(Arrays.asList("a", "b"), null));
		Assert.assertEquals(Arrays.asList("a", "b"), ListUtil.union(null, Arrays.asList("a", "b")));
	}

	@Test
	public void testIntersection() {
		Assert.assertEquals(Arrays.asList("b"), ListUtil.intersection(Arrays.asList("a", "b", "b"), Arrays.asList("b", "c")));
		Assert.assertTrue(ListUtil.intersection(Arrays.asList("a"), Arrays.asList("b")).isEmpty());
		Assert.assertTrue(ListUtil.intersection(null, Arrays.asList("b")).isEmpty());
		Assert.assertTrue(ListUtil.intersection(Arrays.asList("a"), null).isEmpty());
		Assert.assertTrue(ListUtil.intersection(new ArrayList<>(), Arrays.asList("b")).isEmpty());
	}

	@Test
	public void testSubtract() {
		Assert.assertEquals(Arrays.asList("a"), ListUtil.subtract(Arrays.asList("a", "b", "b"), Arrays.asList("b", "c")));
		Assert.assertEquals(Arrays.asList("a", "b"), ListUtil.subtract(Arrays.asList("a", "b"), null));
		Assert.assertTrue(ListUtil.subtract(null, Arrays.asList("a")).isEmpty());
		Assert.assertTrue(ListUtil.subtract(new ArrayList<>(), Arrays.asList("a")).isEmpty());
		Assert.assertEquals(Arrays.asList("a", "b"), ListUtil.subtract(Arrays.asList("a", "b"), new ArrayList<>()));
	}

	// ================= min / max =================

	@Test
	public void testMinMaxNatural() {
		Assert.assertEquals(Integer.valueOf(1), ListUtil.min(Arrays.asList(3, 1, 2)));
		Assert.assertEquals(Integer.valueOf(3), ListUtil.max(Arrays.asList(3, 1, 2)));
	}

	@Test(expected = NoSuchElementException.class)
	public void testMinEmpty() {
		ListUtil.min(new ArrayList<Integer>());
	}

	@Test(expected = NoSuchElementException.class)
	public void testMaxEmpty() {
		ListUtil.max(new ArrayList<Integer>());
	}

	@Test
	public void testMinMaxComparator() {
		Comparator<String> byLength = Comparator.comparingInt(String::length);
		Assert.assertEquals("a", ListUtil.min(Arrays.asList("aaa", "a", "bb"), byLength));
		Assert.assertEquals("aaa", ListUtil.max(Arrays.asList("aaa", "a", "bb"), byLength));
	}

	@Test(expected = NoSuchElementException.class)
	public void testMinComparatorEmpty() {
		ListUtil.min(new ArrayList<Integer>(), Comparator.naturalOrder());
	}

	@Test(expected = NoSuchElementException.class)
	public void testMaxComparatorEmpty() {
		ListUtil.max(new ArrayList<Integer>(), Comparator.naturalOrder());
	}

	// ================= sum / average =================

	@Test
	public void testSum() {
		Assert.assertEquals(6L, ListUtil.sum(Arrays.asList(1, 2, 3)));
		Assert.assertEquals(5L, ListUtil.sum(Arrays.asList(1, null, 4)));
		Assert.assertEquals(0L, ListUtil.sum(null));
		Assert.assertEquals(0L, ListUtil.sum(new ArrayList<>()));
	}

	@Test
	public void testAverage() {
		Assert.assertEquals(2.0, ListUtil.average(Arrays.asList(1, 2, 3)), 1e-9);
		Assert.assertEquals(2.5, ListUtil.average(Arrays.asList(1, null, 4)), 1e-9);
		Assert.assertEquals(0.0, ListUtil.average(null), 1e-9);
		Assert.assertEquals(0.0, ListUtil.average(new ArrayList<>()), 1e-9);
		Assert.assertEquals(0.0, ListUtil.average(Arrays.asList(null, null)), 1e-9);
	}

	// ================= move =================

	@Test
	public void testMove() {
		List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
		Assert.assertSame(list, ListUtil.move(list, 0, 2));
		Assert.assertEquals(Arrays.asList("b", "c", "a", "d"), list);

		List<String> back = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
		ListUtil.move(back, 3, 0);
		Assert.assertEquals(Arrays.asList("d", "a", "b", "c"), back);

		List<String> same = new ArrayList<>(Arrays.asList("a", "b"));
		ListUtil.move(same, 1, 1);
		Assert.assertEquals(Arrays.asList("a", "b"), same);
	}
}
