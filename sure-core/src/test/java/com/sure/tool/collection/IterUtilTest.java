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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 52：IterUtil 测试（空安全、迭代器消耗语义、过滤/转换/拼接）。
 *
 * @author suretool
 * @since 1.20.0
 */
public class IterUtilTest {

	private static List<String> list(String... values) {
		return new ArrayList<>(Arrays.asList(values));
	}

	@Test
	public void testGetIterAndEmpty() {
		List<String> values = list("a");
		Assert.assertTrue(IterUtil.getIter(values).hasNext());
		Assert.assertFalse(IterUtil.getIter((Iterable<String>) null).hasNext());
		Assert.assertFalse(IterUtil.empty().hasNext());
	}

	@Test
	public void testEmptyChecks() {
		Assert.assertTrue(IterUtil.isEmpty((Iterable<String>) null));
		Assert.assertTrue(IterUtil.isEmpty(Collections.<String>emptyList()));
		Assert.assertFalse(IterUtil.isEmpty(list("a")));
		Assert.assertTrue(IterUtil.isEmpty((Iterator<String>) null));
		Assert.assertTrue(IterUtil.isEmpty(Collections.<String>emptyList().iterator()));
		Iterator<String> iterator = list("a").iterator();
		Assert.assertFalse(IterUtil.isEmpty(iterator));
		Assert.assertTrue(iterator.hasNext());
		Assert.assertFalse(IterUtil.isNotEmpty((Iterable<String>) null));
		Assert.assertTrue(IterUtil.isNotEmpty(list("a")));
		Assert.assertFalse(IterUtil.isNotEmpty((Iterator<String>) null));
		Assert.assertTrue(IterUtil.isNotEmpty(list("a").iterator()));
	}

	@Test
	public void testNullChecks() {
		Assert.assertFalse(IterUtil.hasNull((Iterable<String>) null));
		Assert.assertFalse(IterUtil.hasNull(Collections.<String>emptyList()));
		Assert.assertTrue(IterUtil.hasNull(list("a", null)));
		Assert.assertFalse(IterUtil.hasNull(list("a", "b")));
		Assert.assertFalse(IterUtil.hasNull((Iterator<String>) null));
		Assert.assertTrue(IterUtil.hasNull(list((String) null).iterator()));
		Assert.assertFalse(IterUtil.isAllNull((Iterable<String>) null));
		Assert.assertFalse(IterUtil.isAllNull(Collections.<String>emptyList()));
		Assert.assertTrue(IterUtil.isAllNull(list(null, null)));
		Assert.assertFalse(IterUtil.isAllNull(list(null, "a")));
		Assert.assertFalse(IterUtil.isAllNull((Iterator<String>) null));
		Assert.assertTrue(IterUtil.isAllNull(list((String) null).iterator()));
	}

	@Test
	public void testSize() {
		Assert.assertEquals(0, IterUtil.size((Iterable<String>) null));
		Assert.assertEquals(2, IterUtil.size(list("a", "b")));
		Iterable<String> iterable = () -> list("a", "b", "c").iterator();
		Assert.assertEquals(3, IterUtil.size(iterable));
		Assert.assertEquals(0, IterUtil.size((Iterator<String>) null));
		Assert.assertEquals(2, IterUtil.size(list("a", "b").iterator()));
	}

	@Test
	public void testToList() {
		Assert.assertEquals(list("a", "b"), IterUtil.toList(list("a", "b")));
		Assert.assertEquals(Collections.emptyList(), IterUtil.toList((Iterable<String>) null));
		Assert.assertEquals(list("a", "b"), IterUtil.toList(list("a", "b").iterator()));
		Assert.assertEquals(Collections.emptyList(), IterUtil.toList((Iterator<String>) null));
	}

	@Test
	public void testAsIterator() {
		Enumeration<String> enumeration = Collections.enumeration(list("a", "b"));
		Iterator<String> iterator = IterUtil.asIterator(enumeration);
		Assert.assertEquals("a", iterator.next());
		Assert.assertEquals("b", iterator.next());
		Assert.assertFalse(iterator.hasNext());
		Assert.assertFalse(IterUtil.asIterator(null).hasNext());
	}

	@Test
	public void testGetFirst() {
		Assert.assertEquals("a", IterUtil.getFirst(list("a", "b")));
		Assert.assertNull(IterUtil.getFirst((Iterable<String>) null));
		Assert.assertNull(IterUtil.getFirst(Collections.<String>emptyList()));
		Iterator<String> iterator = list("a", "b").iterator();
		Assert.assertEquals("a", IterUtil.getFirst(iterator));
		Assert.assertEquals("b", iterator.next());
		Assert.assertNull(IterUtil.getFirst((Iterator<String>) null));
		Assert.assertNull(IterUtil.getFirst(Collections.<String>emptyList().iterator()));
	}

	@Test
	public void testGetFirstNoneNull() {
		Assert.assertEquals("a", IterUtil.getFirstNoneNull(list(null, "a", "b")));
		Assert.assertNull(IterUtil.getFirstNoneNull(list((String) null)));
		Assert.assertNull(IterUtil.getFirstNoneNull((Iterable<String>) null));
		Assert.assertEquals("a", IterUtil.getFirstNoneNull(list(null, "a").iterator()));
		Assert.assertNull(IterUtil.getFirstNoneNull((Iterator<String>) null));
		Assert.assertNull(IterUtil.getFirstNoneNull(list((String) null).iterator()));
	}

	@Test
	public void testFilter() {
		Assert.assertEquals(list("aa", "bbb", "bbbb"),
				IterUtil.filter(list("a", "aa", "bbb", "bbbb"), value -> value.length() >= 2));
		Assert.assertEquals(Collections.emptyList(), IterUtil.filter((Iterable<String>) null, value -> true));
		Assert.assertEquals(list("a", "aa"),
				IterUtil.filterToList(list("a", "b", "aa").iterator(), value -> !value.equals("b")));
		Assert.assertEquals(Collections.emptyList(), IterUtil.filterToList(null, value -> true));
		try {
			IterUtil.filter(list("a"), null);
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 条件不可为 null
		}
		try {
			IterUtil.filterToList(list("a").iterator(), null);
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 条件不可为 null
		}
	}

	@Test
	public void testTrans() {
		Iterator<Integer> iterator = IterUtil.trans(list("a", "bb").iterator(), String::length);
		Assert.assertEquals(1, iterator.next().intValue());
		Assert.assertEquals(2, iterator.next().intValue());
		Assert.assertFalse(iterator.hasNext());
		Assert.assertFalse(IterUtil.trans((Iterator<String>) null, String::length).hasNext());
		try {
			IterUtil.trans(list("a").iterator(), null);
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 转换函数不可为 null
		}
	}

	@Test
	public void testJoin() {
		Assert.assertEquals("a,b", IterUtil.join(list("a", "b").iterator(), ","));
		Assert.assertEquals("ab", IterUtil.join(list("a", "b").iterator(), null));
		Assert.assertEquals("", IterUtil.join((Iterator<String>) null, ","));
		Assert.assertEquals("", IterUtil.join(Collections.<String>emptyList().iterator(), ","));
		Assert.assertEquals("a,null,b", IterUtil.join(list("a", null, "b").iterator(), ","));
	}

	@Test
	public void testForEach() {
		List<String> result = new ArrayList<>();
		IterUtil.forEach(list("a", "b").iterator(), result::add);
		Assert.assertEquals(list("a", "b"), result);
		IterUtil.<String>forEach(null, result::add);
		try {
			IterUtil.forEach(list("a").iterator(), null);
			Assert.fail("should throw");
		} catch (NullPointerException expected) {
			// 消费操作不可为 null
		}
	}

	@Test
	public void testClear() {
		Iterator<String> iterator = list("a", "b").iterator();
		IterUtil.clear(iterator);
		Assert.assertFalse(iterator.hasNext());
		IterUtil.clear(null);
	}
}
