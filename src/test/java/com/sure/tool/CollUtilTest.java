package com.sure.tool;

import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.ListUtil;
import com.sure.tool.collection.MapUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CollUtil / ListUtil / MapUtil 单元测试。
 */
public class CollUtilTest {

	@Test
	public void testIsEmptyAndCreate() {
		Assert.assertTrue(CollUtil.isEmpty((List<?>) null));
		Assert.assertFalse(CollUtil.isEmpty(CollUtil.newArrayList(1, 2)));
		List<String> list = CollUtil.newArrayList("a", "b");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, CollUtil.newHashSet("a", "a", "b").size());
	}

	@Test
	public void testContains() {
		List<Integer> list = Arrays.asList(1, 2, 3);
		Assert.assertTrue(CollUtil.contains(list, 2));
		Assert.assertTrue(CollUtil.containsAny(list, 9, 3));
		Assert.assertFalse(CollUtil.containsAny(list, 8, 9));
	}

	@Test
	public void testSetOperations() {
		List<Integer> c1 = Arrays.asList(1, 2, 3, 4);
		List<Integer> c2 = Arrays.asList(3, 4, 5);
		Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), CollUtil.union(c1, c2));
		Assert.assertEquals(Arrays.asList(3, 4), CollUtil.intersection(c1, c2));
		Assert.assertEquals(Arrays.asList(1, 2), CollUtil.disjunction(c1, c2));
		Assert.assertEquals(Arrays.asList(1, 2, 5), CollUtil.xor(c1, c2));
	}

	@Test
	public void testJoinAndGet() {
		Assert.assertEquals("a-b-c", CollUtil.join(Arrays.asList("a", "b", "c"), "-"));
		Assert.assertEquals("a", CollUtil.getFirst(Arrays.asList("a", "b")));
		Assert.assertEquals("b", CollUtil.getLast(Arrays.asList("a", "b")));
		Assert.assertEquals("a", CollUtil.get(Arrays.asList("a", "b", "c"), -3));
		Assert.assertNull(CollUtil.get(Arrays.asList("a"), 5));
	}

	@Test
	public void testFunctional() {
		List<Integer> list = Arrays.asList(1, 2, 3, 4);
		Assert.assertEquals(Arrays.asList(2, 4), CollUtil.filter(list, i -> i % 2 == 0));
		Assert.assertEquals(Arrays.asList("1", "2"), CollUtil.map(Arrays.asList(1, 2), String::valueOf));
		Map<String, List<Integer>> grouped = CollUtil.groupByKey(list, i -> i % 2 == 0 ? "even" : "odd");
		Assert.assertEquals(2, grouped.get("even").size());
		Assert.assertEquals(2, grouped.get("odd").size());
		Assert.assertEquals(Arrays.asList(1, 2, 3), CollUtil.sort(Arrays.asList(3, 1, 2), Integer::compareTo));
		Assert.assertEquals(Arrays.asList(1, 2, 3), CollUtil.distinct(Arrays.asList(1, 2, 1, 3, 2)));
	}

	@Test
	public void testSubAndReverse() {
		Assert.assertEquals(Arrays.asList(2, 3), CollUtil.sub(Arrays.asList(1, 2, 3, 4), 1, 3));
		Assert.assertEquals(Arrays.asList(3, 2, 1), CollUtil.reverse(CollUtil.newArrayList(1, 2, 3)));
	}

	// ---------------- ListUtil ----------------

	@Test
	public void testListUtil() {
		List<Integer> list = ListUtil.toList(1, 2, 3);
		Assert.assertEquals(3, list.size());
		List<List<Integer>> parts = ListUtil.partition(Arrays.asList(1, 2, 3, 4, 5), 2);
		Assert.assertEquals(3, parts.size());
		Assert.assertEquals(Arrays.asList(1, 2), parts.get(0));
		Assert.assertEquals(Arrays.asList(5), parts.get(2));
		Assert.assertEquals(Arrays.asList(3, 4), ListUtil.page(1, 2, Arrays.asList(1, 2, 3, 4, 5)));
		Assert.assertEquals(0, ListUtil.page(9, 2, Arrays.asList(1, 2)).size());
		Assert.assertEquals(Arrays.asList(3, 2, 1), ListUtil.reverseNew(Arrays.asList(1, 2, 3)));
		Assert.assertEquals(Arrays.asList(1, 2, 3), ListUtil.sub(Arrays.asList(1, 2, 3, 4), 0, 3));
	}

	// ---------------- MapUtil ----------------

	@Test
	public void testMapUtil() {
		Map<String, Integer> map = MapUtil.of("a", 1, "b", 2);
		Assert.assertEquals(2, map.size());
		Assert.assertFalse(MapUtil.isEmpty(map));
		Assert.assertTrue(MapUtil.isEmpty((Map<?, ?>) null));
		Assert.assertEquals(1, (int) MapUtil.get(map, "a", 0));
		Assert.assertEquals(9, (int) MapUtil.get(map, "x", 9));
		Assert.assertEquals("1", MapUtil.getStr(map, "a", ""));
		Assert.assertEquals(2, MapUtil.getInt(map, "b", 0));
		Assert.assertEquals("a=1,b=2", MapUtil.join(map, ",", "="));
		Map<Integer, String> reversed = MapUtil.reverse(map);
		Assert.assertEquals("a", reversed.get(1));
		Assert.assertTrue(MapUtil.containsKey(map, "a"));
	}
}
