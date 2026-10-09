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
import java.util.List;
import java.util.Map;

/**
 * 批20：MultiMap 多值映射测试。
 */
public class MultiMapTest {

	/**
	 * put 与 get：基本多值存储、保持插入顺序。
	 */
	@Test
	public void testPutAndGet() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		Assert.assertTrue(mm.put("a", 1));
		Assert.assertTrue(mm.put("a", 2));
		Assert.assertTrue(mm.put("b", 3));
		Assert.assertEquals(List.of(1, 2), mm.get("a"));
		Assert.assertEquals(List.of(3), mm.get("b"));
		Assert.assertEquals(2, mm.keyCount());
		Assert.assertEquals(3, mm.size());
	}

	/**
	 * 缺失键返回空不可变列表（不抛异常、不可修改）。
	 */
	@Test
	public void testGetMissingKey() {
		MultiMap<String, String> mm = new MultiMap<>();
		List<String> missing = mm.get("nope");
		Assert.assertNotNull(missing);
		Assert.assertTrue(missing.isEmpty());
		Assert.assertThrows(UnsupportedOperationException.class, () -> missing.add("x"));
	}

	/**
	 * putAll 批量添加与链式返回。
	 */
	@Test
	public void testPutAll() {
		MultiMap<String, String> mm = new MultiMap<>();
		MultiMap<String, String> same = mm.putAll("k", Arrays.asList("a", "b", "c"));
		Assert.assertSame(mm, same);
		Assert.assertEquals(List.of("a", "b", "c"), mm.get("k"));
		mm.putAll("k", List.of());
		Assert.assertEquals(List.of("a", "b", "c"), mm.get("k"));
	}

	/**
	 * removeAll 返回并移除全部值。
	 */
	@Test
	public void testRemoveAll() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		mm.put("a", 1);
		mm.put("a", 2);
		Assert.assertEquals(List.of(1, 2), mm.removeAll("a"));
		Assert.assertTrue(mm.get("a").isEmpty());
		Assert.assertTrue(mm.removeAll("a").isEmpty());
	}

	/**
	 * remove 单值：移除后键空则删除键。
	 */
	@Test
	public void testRemoveSingle() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		mm.put("a", 1);
		mm.put("a", 2);
		Assert.assertTrue(mm.remove("a", 1));
		Assert.assertFalse(mm.remove("a", 99));
		Assert.assertEquals(List.of(2), mm.get("a"));
		Assert.assertTrue(mm.remove("a", 2));
		Assert.assertFalse(mm.containsKey("a"));
	}

	/**
	 * containsKey / containsValue。
	 */
	@Test
	public void testContains() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		mm.put("a", 1);
		Assert.assertTrue(mm.containsKey("a"));
		Assert.assertFalse(mm.containsKey("b"));
		Assert.assertTrue(mm.containsValue(1));
		Assert.assertFalse(mm.containsValue(2));
	}

	/**
	 * values 扁平化、keys 顺序、entries 结构。
	 */
	@Test
	public void testViews() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		mm.put("a", 1);
		mm.put("a", 2);
		mm.put("b", 3);
		Assert.assertEquals(List.of(1, 2, 3), mm.values());
		Assert.assertEquals(List.of("a", "b"), new java.util.ArrayList<>(mm.keys()));
		Assert.assertEquals(2, mm.entries().size());
		for (Map.Entry<String, List<Integer>> e : mm.entries()) {
			Assert.assertNotNull(e.getValue());
		}
	}

	/**
	 * asMap 为不可变视图副本，修改副本不影响原对象。
	 */
	@Test
	public void testAsMapCopy() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		mm.put("a", 1);
		Map<String, List<Integer>> copy = mm.asMap();
		Assert.assertEquals(List.of(1), copy.get("a"));
		copy.put("x", List.of(9));
		Assert.assertFalse(mm.containsKey("x"));
	}

	/**
	 * null 值允许存储。
	 */
	@Test
	public void testNullValue() {
		MultiMap<String, String> mm = new MultiMap<>();
		mm.put("k", null);
		Assert.assertTrue(mm.containsKey("k"));
		Assert.assertEquals(1, mm.size());
	}

	/**
	 * clear / isEmpty。
	 */
	@Test
	public void testClearAndEmpty() {
		MultiMap<String, Integer> mm = new MultiMap<>();
		Assert.assertTrue(mm.isEmpty());
		mm.put("a", 1);
		Assert.assertFalse(mm.isEmpty());
		mm.clear();
		Assert.assertTrue(mm.isEmpty());
		Assert.assertEquals(0, mm.size());
	}

	/**
	 * 大量键值混合操作的稳定性。
	 */
	@Test
	public void testMixedOperations() {
		MultiMap<Integer, String> mm = new MultiMap<>();
		for (int i = 0; i < 100; i++) {
			mm.put(i % 10, "v" + i);
		}
		Assert.assertEquals(10, mm.keyCount());
		Assert.assertEquals(100, mm.size());
		Assert.assertEquals(10, mm.get(5).size());
		mm.removeAll(5);
		Assert.assertEquals(90, mm.size());
	}
}
