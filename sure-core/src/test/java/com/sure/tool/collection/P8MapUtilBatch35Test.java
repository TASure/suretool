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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批35（v1.16.0）：MapUtil toMap / difference / mergeAll 全分支测试。
 */
public class P8MapUtilBatch35Test {

	// ================= toMap（唯一键） =================

	@Test
	public void testToMapUnique() {
		List<String> items = Arrays.asList("a1", "b2", "c3");
		Map<Character, Integer> m = MapUtil.toMap(items, s -> s.charAt(0), s -> s.length());
		Assert.assertEquals(3, m.size());
		Assert.assertEquals(Integer.valueOf(2), m.get('a'));
		Assert.assertTrue(MapUtil.toMap(null, s -> s, s -> s).isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToMapDuplicateKey() {
		MapUtil.toMap(Arrays.asList("a1", "a2"), s -> s.charAt(0), s -> s);
	}

	// ================= toMap（合并策略） =================

	@Test
	public void testToMapMerge() {
		List<String> items = Arrays.asList("a1", "a2", "b3");
		Map<Character, String> m = MapUtil.toMap(items, s -> s.charAt(0), s -> s,
				(v1, v2) -> v1 + "+" + v2);
		Assert.assertEquals("a1+a2", m.get('a'));
		Assert.assertEquals("b3", m.get('b'));
		Assert.assertEquals("a2", MapUtil.toMap(items, s -> s.charAt(0), s -> s, null).get('a'));
		Assert.assertTrue(MapUtil.toMap(null, s -> s, s -> s, (a, b) -> a).isEmpty());
	}

	// ================= difference =================

	@Test
	public void testDifference() {
		Map<String, Integer> left = new LinkedHashMap<>();
		left.put("a", 1);
		left.put("b", 2);
		left.put("c", 3);
		Map<String, Integer> right = new LinkedHashMap<>();
		right.put("b", 99);
		right.put("c", 3);
		right.put("d", 4);

		Difference<String, Integer> d = MapUtil.difference(left, right);
		Assert.assertEquals(1, d.onlyLeft().size());
		Assert.assertEquals(Integer.valueOf(1), d.onlyLeft().get("a"));
		Assert.assertEquals(1, d.onlyRight().size());
		Assert.assertEquals(Integer.valueOf(4), d.onlyRight().get("d"));
		Assert.assertEquals(1, d.valueDiffers().size());
		Assert.assertEquals(Integer.valueOf(2), d.valueDiffers().get("b"));

		Difference<String, Integer> same = MapUtil.difference(left, left);
		Assert.assertTrue(same.onlyLeft().isEmpty());
		Assert.assertTrue(same.onlyRight().isEmpty());
		Assert.assertTrue(same.valueDiffers().isEmpty());

		Difference<String, Integer> n = MapUtil.difference(null, null);
		Assert.assertTrue(n.onlyLeft().isEmpty());
		Assert.assertTrue(n.onlyRight().isEmpty());

		Difference<String, Integer> oneSide = MapUtil.difference(left, null);
		Assert.assertEquals(3, oneSide.onlyLeft().size());
		Assert.assertTrue(oneSide.onlyRight().isEmpty());

		Difference<String, Integer> otherSide = MapUtil.difference(null, right);
		Assert.assertEquals(3, otherSide.onlyRight().size());
	}

	// ================= mergeAll =================

	@Test
	public void testMergeAll() {
		Map<String, Integer> target = new LinkedHashMap<>();
		target.put("a", 1);
		target.put("b", 2);
		Map<String, Integer> source = new LinkedHashMap<>();
		source.put("b", 20);
		source.put("c", 30);

		Map<String, Integer> merged = MapUtil.mergeAll(target, source, (v1, v2) -> v1 + v2);
		Assert.assertSame(target, merged);
		Assert.assertEquals(Integer.valueOf(22), merged.get("b"));
		Assert.assertEquals(Integer.valueOf(30), merged.get("c"));

		Map<String, Integer> overwrite = MapUtil.mergeAll(target, source, null);
		Assert.assertEquals(Integer.valueOf(20), overwrite.get("b"));

		Assert.assertSame(target, MapUtil.mergeAll(target, null, (a, b) -> a));
		Assert.assertNull(MapUtil.mergeAll(null, source, (a, b) -> a));

		Map<String, Integer> self = new LinkedHashMap<>();
		self.put("x", 1);
		Map<String, Integer> mergedSelf = MapUtil.mergeAll(self, self, (a, b) -> a + b);
		Assert.assertEquals(Integer.valueOf(2), mergedSelf.get("x"));
	}
}
