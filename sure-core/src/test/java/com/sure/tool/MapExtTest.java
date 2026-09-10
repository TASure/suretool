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
package com.sure.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.sure.tool.collection.CaseInsensitiveMap;
import com.sure.tool.collection.OrderedMap;

/**
 * CaseInsensitiveMap / OrderedMap 测试。
 */
public class MapExtTest {

	@Test
	public void testCaseInsensitivePutGet() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>();
		map.put("Name", 1);
		map.put("age", 2);
		assertEquals(Integer.valueOf(1), map.get("name"));
		assertEquals(Integer.valueOf(1), map.get("NAME"));
		assertEquals(Integer.valueOf(2), map.get("AGE"));
		assertTrue(map.containsKey("Name"));
		assertTrue(map.containsKey("NAME"));
		assertTrue(map.containsKey("age"));
		assertFalse(map.containsKey("none"));
	}

	@Test
	public void testCaseInsensitiveOverwrite() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>();
		map.put("Key", 1);
		map.put("key", 2);
		assertEquals(Integer.valueOf(2), map.get("KEY"));
		assertEquals(1, map.size());
	}

	@Test
	public void testCaseInsensitiveNullKey() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>();
		map.put(null, 1);
		assertEquals(Integer.valueOf(1), map.get(null));
		assertTrue(map.containsKey(null));
		assertEquals(Integer.valueOf(1), map.remove(null));
		assertFalse(map.containsKey(null));
	}

	@Test
	public void testCaseInsensitiveRemoveAndClear() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>();
		map.put("A", 1);
		map.put("B", 2);
		assertEquals(Integer.valueOf(1), map.remove("a"));
		assertFalse(map.containsKey("A"));
		map.clear();
		assertTrue(map.isEmpty());
	}

	@Test
	public void testCaseInsensitiveFromMap() {
		java.util.LinkedHashMap<String, Integer> source = new java.util.LinkedHashMap<>();
		source.put("Foo", 1);
		source.put("bar", 2);
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>(source);
		assertEquals(Integer.valueOf(1), map.get("foo"));
		assertEquals(Integer.valueOf(2), map.get("BAR"));
	}

	@Test
	public void testCaseInsensitiveInitialCapacity() {
		CaseInsensitiveMap<String, Integer> map = new CaseInsensitiveMap<>(8);
		map.put("x", 1);
		assertEquals(Integer.valueOf(1), map.get("X"));
	}

	@Test
	public void testOrderedMapOrder() {
		OrderedMap<String, Integer> map = new OrderedMap<>();
		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 3);
		assertEquals("a", map.firstKey());
		assertEquals("c", map.lastKey());
		assertEquals(Integer.valueOf(1), map.get(0));
		assertEquals(Integer.valueOf(3), map.get(2));
		assertNull(map.get(5));
		assertNull(map.get(-1));
	}

	@Test
	public void testOrderedMapRemoveFirstLast() {
		OrderedMap<String, Integer> map = new OrderedMap<>();
		map.put("a", 1);
		map.put("b", 2);
		Map.Entry<String, Integer> first = map.removeFirst();
		assertEquals("a", first.getKey());
		assertEquals(Integer.valueOf(1), first.getValue());
		assertEquals(1, map.size());
		Map.Entry<String, Integer> last = map.removeLast();
		assertEquals("b", last.getKey());
		assertEquals(Integer.valueOf(2), last.getValue());
		assertTrue(map.isEmpty());
	}

	@Test
	public void testOrderedMapEmpty() {
		OrderedMap<String, Integer> map = new OrderedMap<>();
		assertNull(map.firstKey());
		assertNull(map.lastKey());
		assertNull(map.removeFirst());
		assertNull(map.removeLast());
		assertNull(map.get(0));
	}

	@Test
	public void testOrderedMapFromMapAndCapacity() {
		java.util.LinkedHashMap<String, Integer> source = new java.util.LinkedHashMap<>();
		source.put("x", 10);
		OrderedMap<String, Integer> map = new OrderedMap<>(source);
		assertEquals("x", map.firstKey());
		assertEquals(Integer.valueOf(10), map.get("x"));
		OrderedMap<String, Integer> map2 = new OrderedMap<>(4);
		map2.put("y", 20);
		assertEquals(Integer.valueOf(20), map2.get("y"));
	}
}
