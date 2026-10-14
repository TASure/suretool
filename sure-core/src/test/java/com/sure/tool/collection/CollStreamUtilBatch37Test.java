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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;

import org.junit.Test;

/**
 * {@link CollStreamUtil} 测试：分组 / 聚合 / 统计 / 过滤 / 去重 / 拼接 / 映射。
 */
public class CollStreamUtilBatch37Test {

	private static class Item {
		final String group;
		final long score;

		Item(String group, long score) {
			this.group = group;
			this.score = score;
		}
	}

	private final List<Item> data = Arrays.asList(
			new Item("a", 1L), new Item("a", 2L),
			new Item("b", 10L), new Item("b", 20L),
			new Item("c", 100L));

	@Test
	public void 分组保序与同键聚合() {
		Map<String, List<Item>> g = CollStreamUtil.groupBy(data, i -> i.group);
		assertEquals(Arrays.asList("a", "b", "c"), new java.util.ArrayList<>(g.keySet()));
		assertEquals(2, g.get("a").size());
		assertEquals(2, g.get("b").size());
		assertEquals(1, g.get("c").size());
	}

	@Test
	public void 分组null安全() {
		assertTrue(CollStreamUtil.<Item, String>groupBy(null, i -> i.group).isEmpty());
		assertTrue(CollStreamUtil.groupBy(data, i -> i.group, LinkedHashMap::new) instanceof LinkedHashMap);
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.groupBy(data, null));
		assertThrows(IllegalArgumentException.class,
				() -> CollStreamUtil.groupBy(data, i -> i.group, (java.util.function.Supplier<Map<String, List<Item>>>) null));
	}

	@Test
	public void 自定义容器生效() {
		Map<String, List<Item>> g = CollStreamUtil.groupBy(data, i -> i.group, HashMap::new);
		assertTrue(g instanceof HashMap);
		assertEquals(3, g.size());
	}

	@Test
	public void 按键聚合值后者覆盖() {
		Map<String, Long> m = CollStreamUtil.groupByKeyValue(data, i -> i.group, i -> i.score);
		assertEquals(Long.valueOf(2L), m.get("a"));
		assertEquals(Long.valueOf(20L), m.get("b"));
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.groupByKeyValue(data, null, i -> i.score));
		assertThrows(IllegalArgumentException.class,
				() -> CollStreamUtil.groupByKeyValue(data, i -> i.group, null));
	}

	@Test
	public void toMap唯一键语义() {
		Map<String, Long> m = CollStreamUtil.toMap(Arrays.asList("x", "y"), s -> s, s -> (long) s.length());
		assertEquals(Long.valueOf(1L), m.get("x"));
		// 重复键抛异常
		assertThrows(IllegalArgumentException.class,
				() -> CollStreamUtil.toMap(data, i -> i.group, i -> i.score));
	}

	@Test
	public void toMap冲突合并() {
		Map<String, Long> m = CollStreamUtil.toMap(data, i -> i.group, i -> i.score, Long::sum);
		assertEquals(Long.valueOf(3L), m.get("a"));
		assertEquals(Long.valueOf(30L), m.get("b"));
	}

	@Test
	public void toMap空与null() {
		assertTrue(CollStreamUtil.<Item, String, Long>toMap(null, i -> i.group, i -> i.score).isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> CollStreamUtil.toMap(data, null, i -> i.score));
	}

	@Test
	public void 数值统计() {
		LongSummaryStatistics stats = CollStreamUtil.summaryStatistics(data, i -> i.score);
		assertEquals(5, stats.getCount());
		assertEquals(133L, stats.getSum());
		assertEquals(100L, stats.getMax());
		assertEquals(1L, stats.getMin());
		// null 提取函数抛异常；null 数据返回全零统计
		LongSummaryStatistics empty = CollStreamUtil.<Item>summaryStatistics(null, i -> i.score);
		assertEquals(0, empty.getCount());
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.summaryStatistics(data, null));
	}

	@Test
	public void 过滤保序() {
		List<Item> r = CollStreamUtil.filter(data, i -> i.score >= 10L);
		assertEquals(3, r.size());
		assertEquals("b", r.get(0).group);
		assertTrue(CollStreamUtil.filter(null, i -> true).isEmpty());
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.filter(data, null));
	}

	@Test
	public void 按键去重首现保留() {
		List<Item> r = CollStreamUtil.distinct(data, i -> i.group);
		assertEquals(3, r.size());
		assertEquals(1L, r.get(0).score);
		assertTrue(CollStreamUtil.<Item, String>distinct(null, i -> i.group).isEmpty());
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.distinct(data, null));
	}

	@Test
	public void 拼接跳过null与空数据() {
		List<String> withNull = Arrays.asList("a", null, "b", null);
		assertEquals("a,b", CollStreamUtil.join(withNull, s -> s, ","));
		assertEquals("", CollStreamUtil.join(null, s -> s, ","));
		assertEquals("ab", CollStreamUtil.join(Arrays.asList("a", "b"), s -> s, null));
		assertEquals("a", CollStreamUtil.join(Arrays.asList("a"), s -> s, ","));
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.join(data, null, ","));
	}

	@Test
	public void 映射跳过null结果() {
		List<Integer> r = CollStreamUtil.map(data, i -> i.score > 10L ? null : (int) i.score);
		assertEquals(3, r.size());
		assertTrue(CollStreamUtil.<Item, String>map(null, i -> i.group).isEmpty());
		assertThrows(IllegalArgumentException.class, () -> CollStreamUtil.map(data, null));
	}
}
