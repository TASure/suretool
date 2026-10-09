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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * CollUtil 覆盖率补测：null / 空集合 / 负索引 / 异常排序分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class CollUtilGapTest {

	/** 用于 sortByProperty 的临时 Bean，age 可为 null 以命中 null 比较分支。 */
	public static class Pojo {
		private final String name;
		private final Integer age;

		public Pojo(String name, Integer age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public Integer getAge() {
			return age;
		}
	}

	@Test
	public void testContainsAny() {
		Assert.assertFalse(CollUtil.containsAny(null, 1));
		Assert.assertFalse(CollUtil.containsAny(Arrays.asList(1, 2), (Object[]) null));
		Assert.assertTrue(CollUtil.containsAny(Arrays.asList(1, 2), 9, 1));
		Assert.assertFalse(CollUtil.containsAny(Arrays.asList(1, 2), 9, 8));
	}

	@Test
	public void testIntersectionDisjunction() {
		Assert.assertTrue(CollUtil.intersection(null, Arrays.asList(1)).isEmpty());
		Assert.assertTrue(CollUtil.intersection(Arrays.asList(1), null).isEmpty());
		Assert.assertEquals(Arrays.asList(2), CollUtil.intersection(Arrays.asList(1, 2), Arrays.asList(2, 3)));
		Assert.assertTrue(CollUtil.disjunction(null, Arrays.asList(1)).isEmpty());
		Assert.assertEquals(Arrays.asList(1), CollUtil.disjunction(Arrays.asList(1, 2), Arrays.asList(2, 3)));
		Assert.assertEquals(Arrays.asList(1), CollUtil.disjunction(Arrays.asList(1), null));
	}

	@Test
	public void testToListVariants() {
		Assert.assertTrue(CollUtil.toList((Iterable<String>) null).isEmpty());
		// 非 Collection 的 Iterable
		Iterable<String> iter = () -> Arrays.asList("a", "b").iterator();
		Assert.assertEquals(Arrays.asList("a", "b"), CollUtil.toList(iter));
		Assert.assertEquals(Arrays.asList("a"), CollUtil.toList(Arrays.asList("a")));
		Assert.assertTrue(CollUtil.toList((Iterator<String>) null).isEmpty());
		Assert.assertEquals(Arrays.asList("x"), CollUtil.toList(Collections.enumeration(Arrays.asList("x"))));
	}

	@Test
	public void testGetNonListAndNegative() {
		Collection<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
		Assert.assertNull(CollUtil.get(null, 0));
		Assert.assertEquals("a", CollUtil.get(set, 0));
		Assert.assertEquals("c", CollUtil.get(set, -1));
		Assert.assertNull(CollUtil.get(set, -10));
		Assert.assertNull(CollUtil.get(set, 10));
		Assert.assertEquals("def", CollUtil.get(set, 10, "def"));
		Assert.assertNull(CollUtil.getFirst(null));
		Assert.assertNull(CollUtil.getLast(new ArrayList<>()));
	}

	@Test
	public void testFilterMapGroupBy() {
		Assert.assertTrue(CollUtil.filter(null, x -> true).isEmpty());
		Assert.assertTrue(CollUtil.filter(Arrays.asList(1, 2), null).isEmpty());
		Assert.assertEquals(Arrays.asList(2), CollUtil.filter(Arrays.asList(1, 2), x -> x % 2 == 0));
		Assert.assertTrue(CollUtil.map(null, x -> x).isEmpty());
		Assert.assertTrue(CollUtil.map(Arrays.asList(1), null).isEmpty());
		Assert.assertEquals(Arrays.asList("1", "2"), CollUtil.map(Arrays.asList(1, 2), Object::toString));
		Assert.assertTrue(CollUtil.groupByKey(null, x -> x).isEmpty());
		Assert.assertTrue(CollUtil.groupByKey(Arrays.asList(1), null).isEmpty());
		Map<Integer, List<Integer>> g = CollUtil.groupByKey(Arrays.asList(1, 2, 3), x -> x % 2);
		Assert.assertEquals(2, g.size());
	}

	@Test
	public void testSubEdge() {
		Assert.assertTrue(CollUtil.sub(null, 0, 1).isEmpty());
		Assert.assertEquals(Arrays.asList(3, 4), CollUtil.sub(Arrays.asList(1, 2, 3, 4, 5), -3, -1));
		Assert.assertTrue(CollUtil.sub(Arrays.asList(1, 2, 3), -10, 2).size() == 2);
		Assert.assertTrue(CollUtil.sub(Arrays.asList(1, 2, 3), 5, 1).isEmpty());
		Assert.assertNull(CollUtil.reverse(null));
	}

	@Test
	public void testEmptyIfNull() {
		Assert.assertEquals(Collections.emptyList(), CollUtil.emptyIfNull(null));
		List<String> l = new ArrayList<>(Arrays.asList("a"));
		Assert.assertSame(l, CollUtil.emptyIfNull(l));
		Assert.assertEquals(Arrays.asList("a"), CollUtil.emptyIfNull(new LinkedHashSet<>(Arrays.asList("a"))));
	}

	@Test
	public void testSortByPropertyNullBranches() {
		List<Pojo> data = new ArrayList<>();
		data.add(new Pojo("b", null));
		data.add(new Pojo("a", 5));
		data.add(new Pojo("c", null));
		data.add(new Pojo("d", 3));
		List<Pojo> sorted = CollUtil.sortByProperty(data, "age", true);
		Assert.assertEquals(4, sorted.size());
		// null 元素之间 cmp=0，非 null 升序
		Assert.assertEquals(Integer.valueOf(3), sorted.get(2).getAge());
		Assert.assertEquals(Integer.valueOf(5), sorted.get(3).getAge());
		// 降序
		List<Pojo> desc = CollUtil.sortByPropertyDesc(data, "age");
		Assert.assertEquals(Integer.valueOf(5), desc.get(0).getAge());
		// 属性不存在 -> 抛 RuntimeException
		try {
			CollUtil.sortByProperty(data, "notExist", true);
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e.getCause());
		}
		Assert.assertTrue(CollUtil.sortByProperty(new ArrayList<>(), "age", true).isEmpty());
	}

	@Test
	public void testMinMaxNulls() {
		Assert.assertEquals("a", CollUtil.min(Arrays.asList("a", null, "b")));
		Assert.assertEquals(Integer.valueOf(1), CollUtil.min(Arrays.asList(null, 3, 1)));
		Assert.assertNull(CollUtil.max(new ArrayList<String>()));
		Assert.assertEquals(Integer.valueOf(3), CollUtil.max(Arrays.asList(null, 3, 1)));
	}

	@Test
	public void testListToMap() {
		Assert.assertTrue(CollUtil.listToMap(new ArrayList<>(), x -> x).isEmpty());
		Map<String, Integer> m = CollUtil.listToMap(Arrays.asList(1, 2), Object::toString);
		Assert.assertEquals(Integer.valueOf(2), m.get("2"));
	}

	@Test
	public void testMinByMaxBy() {
		Assert.assertNull(CollUtil.<Integer, Integer>minBy(null, x -> x));
		Assert.assertNull(CollUtil.maxBy(new ArrayList<Integer>(), x -> x));
		Assert.assertEquals(Integer.valueOf(1), CollUtil.<Integer, Integer>minBy(Arrays.asList(3, 1, 2), x -> x));
		Assert.assertEquals(Integer.valueOf(3), CollUtil.<Integer, Integer>maxBy(Arrays.asList(3, 1, 2), x -> x));
	}

	@Test
	public void testZip() {
		Assert.assertTrue(CollUtil.zip(null, Arrays.asList(1)).isEmpty());
		Assert.assertTrue(CollUtil.zip(Arrays.asList("a"), null).isEmpty());
		Map<String, Integer> m = CollUtil.zip(Arrays.asList("a", "b"), Arrays.asList(1, 2, 3));
		Assert.assertEquals(2, m.size());
	}

	@Test
	public void testRemoveNull() {
		Assert.assertTrue(CollUtil.removeNull(null).isEmpty());
		Assert.assertTrue(CollUtil.removeNull(new ArrayList<>()).isEmpty());
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.removeNull(Arrays.asList(1, null, 2)));
	}

	@Test
	public void testIndexOfLastIndexOf() {
		Assert.assertEquals(-1, CollUtil.indexOf(null, 1));
		Assert.assertEquals(1, CollUtil.indexOf(Arrays.asList(1, 2, 2), 2));
		Assert.assertEquals(-1, CollUtil.lastIndexOf(null, 1));
		Assert.assertEquals(2, CollUtil.lastIndexOf(Arrays.asList(1, 2, 2), 2));
		Assert.assertEquals(-1, CollUtil.lastIndexOf(Arrays.asList(1, 2), 9));
	}

	@Test
	public void testRandomItem() {
		try {
			CollUtil.randomItem(null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertEquals("a", CollUtil.randomItem(Arrays.asList("a")));
		Assert.assertEquals("a", CollUtil.randomItem(new LinkedHashSet<>(Arrays.asList("a"))));
		List<Integer> sampled = CollUtil.randomItems(Arrays.asList(1, 2, 3), 5);
		Assert.assertEquals(3, sampled.size());
		Assert.assertTrue(CollUtil.randomItems(Arrays.asList(1, 2), 0).isEmpty());
	}

	@Test
	public void testToImmutable() {
		Assert.assertEquals(Collections.emptyList(), CollUtil.toImmutable(null));
		Assert.assertEquals(Collections.emptyList(), CollUtil.toImmutable(new ArrayList<>()));
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.toImmutable(Arrays.asList(1, 2)));
	}

	@Test
	public void testRemoveEmptyRemoveAny() {
		Assert.assertNull(CollUtil.removeEmpty(null));
		Collection<Object> c = new ArrayList<>(Arrays.asList(null, "", "x", new ArrayList<>(), new HashMap<>(), 1));
		CollUtil.removeEmpty(c);
		Assert.assertEquals(Arrays.asList("x", 1), new ArrayList<>(c));
		Assert.assertSame(c, CollUtil.removeAny(c, null));
		Assert.assertSame(c, CollUtil.removeAny(c, new Object[0]));
		CollUtil.removeAny(c, "x");
		Assert.assertFalse(c.contains("x"));
	}

	@Test
	public void testIsAllEmptyNotNull() {
		Assert.assertFalse(CollUtil.isAllEmpty());
		Assert.assertFalse(CollUtil.isAllEmpty((Collection<?>[]) null));
		Assert.assertTrue(CollUtil.isAllEmpty(null, new ArrayList<>()));
		Assert.assertFalse(CollUtil.isAllEmpty(null, Arrays.asList(1)));
		Assert.assertFalse(CollUtil.isAllNotNull());
		Assert.assertFalse(CollUtil.isAllNotNull((Collection<?>[]) null));
		Assert.assertFalse(CollUtil.isAllNotNull(Arrays.asList(1), new ArrayList<>()));
		Assert.assertTrue(CollUtil.isAllNotNull(Arrays.asList(1), Arrays.asList(2)));
	}

	@Test
	public void testChunkRotate() {
		Assert.assertThrows(IllegalArgumentException.class, () -> CollUtil.chunk(Arrays.asList(1), 0));
		Assert.assertTrue(CollUtil.chunk(null, 2).isEmpty());
		List<List<Integer>> parts = CollUtil.chunk(Arrays.asList(1, 2, 3, 4, 5), 2);
		Assert.assertEquals(3, parts.size());
		List<Integer> r = new ArrayList<>(Arrays.asList(1, 2, 3));
		CollUtil.rotate(r, 1);
		Assert.assertEquals(Arrays.asList(3, 1, 2), r);
	}
}
