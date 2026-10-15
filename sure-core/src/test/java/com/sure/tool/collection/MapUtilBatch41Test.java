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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 41：MapUtil 值变换 / 条目变换 / 唯一索引 / asMap 测试（对标 Guava Maps）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class MapUtilBatch41Test {

	@Test
	public void testTransformValues() {
		Map<String, Integer> map = MapUtil.of("a", 1, "b", 2);
		Map<String, String> transformed = MapUtil.transformValues(map, v -> "v" + v);
		Assert.assertEquals("v1", transformed.get("a"));
		Assert.assertEquals("v2", transformed.get("b"));
		Assert.assertEquals(2, transformed.size());
		Assert.assertTrue(MapUtil.transformValues(null, v -> "x").isEmpty());
	}

	@Test
	public void testTransformEntries() {
		Map<String, Integer> map = MapUtil.of("a", 1, "b", 2);
		Map<String, String> transformed = MapUtil.transformEntries(map, (k, v) -> k + "=" + v);
		Assert.assertEquals("a=1", transformed.get("a"));
		Assert.assertEquals("b=2", transformed.get("b"));
		Assert.assertTrue(MapUtil.transformEntries(null, (k, v) -> "x").isEmpty());
	}

	@Test
	public void testUniqueIndex() {
		Set<String> set = new LinkedHashSet<>(Arrays.asList("apple", "banana", "cherry"));
		Map<String, String> index = MapUtil.uniqueIndex(set, s -> s.substring(0, 1));
		Assert.assertEquals(3, index.size());
		Assert.assertEquals("apple", index.get("a"));
		Assert.assertEquals("cherry", index.get("c"));
		Assert.assertTrue(MapUtil.uniqueIndex(null, s -> s).isEmpty());

		try {
			MapUtil.uniqueIndex(Arrays.asList("aa", "ab"), s -> s.substring(0, 1));
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 重复键
		}
	}

	@Test
	public void testAsMap() {
		Set<Integer> keys = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
		Map<Integer, String> map = MapUtil.asMap(keys, k -> "n" + k);
		Assert.assertEquals("n1", map.get(1));
		Assert.assertEquals("n3", map.get(3));
		Assert.assertEquals(3, map.size());
		Assert.assertTrue(MapUtil.asMap(null, k -> "x").isEmpty());
		Assert.assertTrue(MapUtil.asMap(Collections.emptySet(), k -> "x").isEmpty());
	}
}
