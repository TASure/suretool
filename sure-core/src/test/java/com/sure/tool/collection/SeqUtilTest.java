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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P0 批 2（v1.1.0）：SeqUtil 现代集合测试（JDK 21+）。
 */
public class SeqUtilTest {

	/**
	 * 倒序快照顺序正确。
	 */
	@Test
	public void testReversedOrder() {
		List<String> source = new ArrayList<>(List.of("a", "b", "c"));
		List<String> reversed = SeqUtil.reversed(source);
		Assert.assertEquals(List.of("c", "b", "a"), reversed);
		Assert.assertEquals(List.of("a", "b", "c"), source);
	}

	/**
	 * 倒序空集合返回空列表。
	 */
	@Test
	public void testReversedEmpty() {
		Assert.assertEquals(List.of(), SeqUtil.reversed(List.of()));
		Assert.assertEquals(List.of(), SeqUtil.reversed(null));
	}

	/**
	 * 首尾元素安全访问。
	 */
	@Test
	public void testFirstLastOrNull() {
		Assert.assertEquals("a", SeqUtil.firstOrNull(List.of("a", "b", "c")));
		Assert.assertEquals("c", SeqUtil.lastOrNull(List.of("a", "b", "c")));
		Assert.assertNull(SeqUtil.firstOrNull(List.of()));
		Assert.assertNull(SeqUtil.lastOrNull(null));
	}

	/**
	 * withFirst/withLast 返回不可变新列表，原集合不变。
	 */
	@Test
	public void testWithFirstLastImmutable() {
		List<String> source = new ArrayList<>(List.of("b", "c"));
		List<String> withFirst = SeqUtil.withFirst(source, "a");
		List<String> withLast = SeqUtil.withLast(source, "d");
		Assert.assertEquals(List.of("a", "b", "c"), withFirst);
		Assert.assertEquals(List.of("b", "c", "d"), withLast);
		Assert.assertEquals(List.of("b", "c"), source);
		try {
			withFirst.add("x");
			Assert.fail("withFirst 结果应为不可变");
		} catch (UnsupportedOperationException e) {
			// 预期
		}
	}

	/**
	 * 非 Sequenced 集合（自定义 Collection）走 fallback 路径。
	 */
	@Test
	public void testNonSequencedFallback() {
		java.util.Collection<String> custom = new java.util.AbstractCollection<>() {
			@Override
			public java.util.Iterator<String> iterator() {
				return java.util.List.of("x", "y").iterator();
			}

			@Override
			public int size() {
				return 2;
			}
		};
		Assert.assertEquals(java.util.List.of("y", "x"), SeqUtil.reversed(custom));
		Assert.assertEquals("x", SeqUtil.firstOrNull(custom));
		Assert.assertEquals("y", SeqUtil.lastOrNull(custom));
	}

	/**
	 * withFirst/withLast 对 null 输入的兜底。
	 */
	@Test
	public void testWithFirstLastNull() {
		Assert.assertEquals(java.util.List.of("a"), SeqUtil.withFirst(null, "a"));
		Assert.assertEquals(java.util.List.of("a"), SeqUtil.withLast(null, "a"));
	}

	/**
	 * 有序 Map 首尾键值访问。
	 */
	@Test
	public void testMapFirstLast() {
		Map<String, Integer> map = new LinkedHashMap<>();
		map.put("first", 1);
		map.put("middle", 2);
		map.put("last", 3);
		Assert.assertEquals("first", SeqUtil.firstKeyOrNull(map));
		Assert.assertEquals("last", SeqUtil.lastKeyOrNull(map));
		Assert.assertEquals(Integer.valueOf(1), SeqUtil.firstEntryOrNull(map).getValue());
		Assert.assertEquals(Integer.valueOf(3), SeqUtil.lastEntryOrNull(map).getValue());
		Assert.assertNull(SeqUtil.firstKeyOrNull(new LinkedHashMap<>()));
		Assert.assertNull(SeqUtil.lastEntryOrNull(null));
	}
}
