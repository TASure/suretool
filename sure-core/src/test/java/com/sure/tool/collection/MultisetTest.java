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

import java.util.ArrayList;
import java.util.List;

/**
 * 批20：Multiset 计数集合测试。
 */
public class MultisetTest {

	/**
	 * add 计数累加与 size/uniqueSize 语义。
	 */
	@Test
	public void testAddAndSize() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a");
		ms.add("a");
		ms.add("b");
		Assert.assertEquals(3, ms.size());
		Assert.assertEquals(2, ms.uniqueSize());
		Assert.assertEquals(2, ms.count("a"));
		Assert.assertEquals(1, ms.count("b"));
		Assert.assertEquals(0, ms.count("c"));
	}

	/**
	 * add(element, occurrences) 批量计数与链式。
	 */
	@Test
	public void testAddBatch() {
		Multiset<String> ms = new Multiset<>();
		Multiset<String> same = ms.add("x", 5);
		Assert.assertSame(ms, same);
		Assert.assertEquals(5, ms.count("x"));
		Assert.assertEquals(5, ms.size());
		Assert.assertThrows(IllegalArgumentException.class, () -> ms.add("x", 0));
		Assert.assertThrows(IllegalArgumentException.class, () -> ms.add("x", -1));
	}

	/**
	 * remove 单元素：计数归零删除元素。
	 */
	@Test
	public void testRemove() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 3);
		Assert.assertTrue(ms.remove("a"));
		Assert.assertEquals(2, ms.count("a"));
		Assert.assertTrue(ms.remove("a"));
		Assert.assertTrue(ms.remove("a"));
		Assert.assertEquals(0, ms.count("a"));
		Assert.assertEquals(0, ms.uniqueSize());
		Assert.assertFalse(ms.remove("a"));
	}

	/**
	 * remove(element, occurrences) 返回实际移除数（受已有计数限制）。
	 */
	@Test
	public void testRemoveBatch() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 3);
		Assert.assertEquals(2, ms.remove("a", 2));
		Assert.assertEquals(1, ms.count("a"));
		Assert.assertEquals(1, ms.remove("a", 5));
		Assert.assertEquals(0, ms.count("a"));
		Assert.assertEquals(0, ms.remove("missing", 1));
	}

	/**
	 * setCount 设置/清零。
	 */
	@Test
	public void testSetCount() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 2);
		ms.setCount("a", 4);
		Assert.assertEquals(4, ms.count("a"));
		Assert.assertEquals(4, ms.size());
		ms.setCount("a", 0);
		Assert.assertEquals(0, ms.count("a"));
		Assert.assertThrows(IllegalArgumentException.class, () -> ms.setCount("a", -1));
	}

	/**
	 * entrySet 条目含 element 与 count。
	 */
	@Test
	public void testEntrySet() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 2);
		ms.add("b", 1);
		int entries = 0;
		for (Multiset.Entry<String> e : ms.entrySet()) {
			entries++;
			if ("a".equals(e.getElement())) {
				Assert.assertEquals(2, e.getCount());
			} else {
				Assert.assertEquals(1, e.getCount());
			}
		}
		Assert.assertEquals(2, entries);
	}

	/**
	 * iterator 按计数展开。
	 */
	@Test
	public void testIteratorExpands() {
		Multiset<String> ms = new Multiset<>();
		ms.add("a", 2);
		ms.add("b", 1);
		List<String> expanded = new ArrayList<>();
		ms.iterator().forEachRemaining(expanded::add);
		Assert.assertEquals(3, expanded.size());
		Assert.assertEquals(2, expanded.stream().filter("a"::equals).count());
		Assert.assertEquals(1, expanded.stream().filter("b"::equals).count());
	}

	/**
	 * null 元素允许计数。
	 */
	@Test
	public void testNullElement() {
		Multiset<String> ms = new Multiset<>();
		ms.add(null, 2);
		Assert.assertEquals(2, ms.count(null));
		Assert.assertEquals(1, ms.uniqueSize());
	}

	/**
	 * clear / isEmpty。
	 */
	@Test
	public void testClear() {
		Multiset<String> ms = new Multiset<>();
		Assert.assertTrue(ms.isEmpty());
		ms.add("a");
		Assert.assertFalse(ms.isEmpty());
		ms.clear();
		Assert.assertTrue(ms.isEmpty());
		Assert.assertEquals(0, ms.size());
		Assert.assertEquals(0, ms.uniqueSize());
	}
}
