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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批36（v1.17.0）：SetUtil 全方法全分支测试。
 */
public class P9SetUtilBatch36Test {

	// ================= 构造 =================

	@Test
	public void testConstructors() {
		Assert.assertEquals(3, SetUtil.newHashSet("a", "b", "c").size());
		Assert.assertTrue(SetUtil.newHashSet().isEmpty());
		Assert.assertEquals(2, SetUtil.newLinkedHashSet("a", "a", "b").size());
		Assert.assertEquals(new TreeSet<>(Arrays.asList(1, 2)), SetUtil.newTreeSet(2, 1, 2));
		Assert.assertTrue(SetUtil.newTreeSet().isEmpty());
	}

	@Test
	public void testOf() {
		Set<String> s = SetUtil.of("a", "b", "a");
		Assert.assertEquals(2, s.size());
		Assert.assertTrue(s.contains("a"));
		Assert.assertTrue(SetUtil.of().isEmpty());
		Set<String> withNull = SetUtil.of("a", null);
		Assert.assertTrue(withNull.contains(null));
		try {
			s.add("c");
			Assert.fail("should be immutable");
		} catch (UnsupportedOperationException expected) {
		}
	}

	// ================= union / intersection / subtract / symmetricDifference =================

	@Test
	public void testUnion() {
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b", "c"),
				SetUtil.union(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("b", "c")));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"), SetUtil.union(SetUtil.newLinkedHashSet("a", "b"), null));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"), SetUtil.union(null, SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertTrue(SetUtil.union(null, null).isEmpty());
	}

	@Test
	public void testIntersection() {
		Assert.assertEquals(SetUtil.newLinkedHashSet("b"),
				SetUtil.intersection(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("b", "c")));
		Assert.assertTrue(SetUtil.intersection(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("b")).isEmpty());
		Assert.assertTrue(SetUtil.intersection(null, SetUtil.newLinkedHashSet("a")).isEmpty());
		Assert.assertTrue(SetUtil.intersection(SetUtil.newLinkedHashSet("a"), null).isEmpty());
	}

	@Test
	public void testSubtract() {
		Assert.assertEquals(SetUtil.newLinkedHashSet("a"),
				SetUtil.subtract(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("b", "c")));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"),
				SetUtil.subtract(SetUtil.newLinkedHashSet("a", "b"), null));
		Assert.assertTrue(SetUtil.subtract(null, SetUtil.newLinkedHashSet("a")).isEmpty());
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"),
				SetUtil.subtract(SetUtil.newLinkedHashSet("a", "b"), new LinkedHashSet<>()));
	}

	@Test
	public void testSymmetricDifference() {
		Set<String> sd = SetUtil.symmetricDifference(
				SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("b", "c"));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "c"), sd);
		Assert.assertTrue(SetUtil.symmetricDifference(
				SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("a")).isEmpty());
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"),
				SetUtil.symmetricDifference(SetUtil.newLinkedHashSet("a", "b"), null));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "b"),
				SetUtil.symmetricDifference(null, SetUtil.newLinkedHashSet("a", "b")));
	}

	// ================= filter / map =================

	@Test
	public void testFilter() {
		Assert.assertEquals(SetUtil.newLinkedHashSet(2, 4),
				SetUtil.filter(SetUtil.newLinkedHashSet(1, 2, 3, 4), n -> n % 2 == 0));
		Assert.assertTrue(SetUtil.filter(SetUtil.newLinkedHashSet(1, 3), n -> n % 2 == 0).isEmpty());
		Assert.assertTrue(SetUtil.filter(null, n -> true).isEmpty());
	}

	@Test
	public void testMap() {
		Set<String> m = SetUtil.map(SetUtil.newLinkedHashSet("ab", "cd"), s -> s.substring(0, 1));
		Assert.assertEquals(SetUtil.newLinkedHashSet("a", "c"), m);
		Set<String> dup = SetUtil.map(SetUtil.newLinkedHashSet("a", "ab"), s -> s.substring(0, 1));
		Assert.assertEquals(1, dup.size());
		Assert.assertTrue(SetUtil.map(null, s -> s).isEmpty());
	}

	// ================= cartesianProduct / powerSet =================

	@Test
	public void testCartesianProduct() {
		List<List<Object>> cp = SetUtil.cartesianProduct(SetUtil.newLinkedHashSet("a", "b"),
				SetUtil.newLinkedHashSet(1, 2));
		Assert.assertEquals(4, cp.size());
		Assert.assertEquals(Arrays.asList("a", 1), cp.get(0));
		Assert.assertTrue(SetUtil.cartesianProduct(new LinkedHashSet<>(), SetUtil.newLinkedHashSet(1)).isEmpty());
		Assert.assertTrue(SetUtil.cartesianProduct(null, SetUtil.newLinkedHashSet(1)).isEmpty());
		Assert.assertTrue(SetUtil.cartesianProduct(SetUtil.newLinkedHashSet("a"), null).isEmpty());
	}

	@Test
	public void testPowerSet() {
		Set<Set<Integer>> ps = SetUtil.powerSet(SetUtil.newLinkedHashSet(1, 2, 3));
		Assert.assertEquals(8, ps.size());
		Assert.assertTrue(ps.contains(new LinkedHashSet<>()));
		Assert.assertTrue(ps.contains(SetUtil.newLinkedHashSet(1, 2, 3)));

		Set<Set<Integer>> empty = SetUtil.powerSet(new LinkedHashSet<>());
		Assert.assertEquals(1, empty.size());
		Assert.assertTrue(empty.contains(new LinkedHashSet<>()));

		Set<Set<Integer>> nil = SetUtil.powerSet(null);
		Assert.assertEquals(1, nil.size());
		Assert.assertTrue(nil.contains(new LinkedHashSet<>()));
	}

	// ================= isSubset / isSuperset / disjoint / containsAny =================

	@Test
	public void testIsSubset() {
		Assert.assertTrue(SetUtil.isSubset(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertTrue(SetUtil.isSubset(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertFalse(SetUtil.isSubset(SetUtil.newLinkedHashSet("a", "c"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertTrue(SetUtil.isSubset(null, SetUtil.newLinkedHashSet("a")));
		Assert.assertFalse(SetUtil.isSubset(SetUtil.newLinkedHashSet("a"), null));
	}

	@Test
	public void testIsSuperset() {
		Assert.assertTrue(SetUtil.isSuperset(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("a")));
		Assert.assertTrue(SetUtil.isSuperset(SetUtil.newLinkedHashSet("a", "b"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertFalse(SetUtil.isSuperset(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertTrue(SetUtil.isSuperset(null, new LinkedHashSet<>()));
		Assert.assertFalse(SetUtil.isSuperset(null, SetUtil.newLinkedHashSet("a")));
		Assert.assertTrue(SetUtil.isSuperset(SetUtil.newLinkedHashSet("a"), null));
	}

	@Test
	public void testDisjoint() {
		Assert.assertTrue(SetUtil.disjoint(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("b")));
		Assert.assertFalse(SetUtil.disjoint(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertTrue(SetUtil.disjoint(null, SetUtil.newLinkedHashSet("a")));
		Assert.assertTrue(SetUtil.disjoint(SetUtil.newLinkedHashSet("a"), null));
	}

	@Test
	public void testContainsAny() {
		Assert.assertTrue(SetUtil.containsAny(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertFalse(SetUtil.containsAny(SetUtil.newLinkedHashSet("a"), SetUtil.newLinkedHashSet("b")));
		Assert.assertFalse(SetUtil.containsAny(null, SetUtil.newLinkedHashSet("a")));
		Assert.assertFalse(SetUtil.containsAny(SetUtil.newLinkedHashSet("a"), null));
	}

	// ================= join / emptyIfNull / reverse / size =================

	@Test
	public void testJoin() {
		Assert.assertEquals("a,b", SetUtil.join(SetUtil.newLinkedHashSet("a", "b"), ","));
		Assert.assertEquals("ab", SetUtil.join(SetUtil.newLinkedHashSet("a", "b"), null));
		Assert.assertEquals("", SetUtil.join(new LinkedHashSet<>(), ","));
		Assert.assertEquals("", SetUtil.join(null, ","));
	}

	@Test
	public void testEmptyIfNull() {
		Set<String> s = SetUtil.newLinkedHashSet("a");
		Assert.assertSame(s, SetUtil.emptyIfNull(s));
		Assert.assertTrue(SetUtil.emptyIfNull(null).isEmpty());
	}

	@Test
	public void testReverse() {
		Assert.assertEquals(SetUtil.newLinkedHashSet("c", "b", "a"),
				SetUtil.reverse(SetUtil.newLinkedHashSet("a", "b", "c")));
		Assert.assertTrue(SetUtil.reverse(null).isEmpty());
	}

	@Test
	public void testSize() {
		Assert.assertEquals(2, SetUtil.size(SetUtil.newLinkedHashSet("a", "b")));
		Assert.assertEquals(0, SetUtil.size(null));
	}
}
