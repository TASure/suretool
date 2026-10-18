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
package com.sure.tool.fluent;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 批47：Fluent API 全面化测试（FluentString / FluentList / FluentMap / Fluent 入口）。
 *
 * <p>覆盖：链式中间操作不断流、终端不可变断言、null 防御、防御拷贝、泛型映射。
 * 不可变断言统一用修改抛 {@link UnsupportedOperationException} 验证。</p>
 */
public class FluentTest {

	// ============ FluentString ============

	@Test
	public void testStringBasicChain() {
		Assert.assertEquals("HELLO suretool",
				Fluent.string("  hello world  ").trim().upperCase().replace("WORLD", "suretool").get());
		Assert.assertEquals("abc", Fluent.string("ABC").lowerCase().get());
		Assert.assertEquals("cba", Fluent.string("abc").reverse().get());
		Assert.assertEquals("abc", Fluent.of("  abc  ").trim().get());
	}

	@Test
	public void testStringReplace() {
		Assert.assertEquals("a-b-c", Fluent.string("a.b.c").replace(".", "-").get());
		Assert.assertEquals("a#b#", Fluent.string("a1b2").replaceAll("\\d", "#").get());
		try {
			Fluent.string("x").replace(null, "y");
			Assert.fail("oldChar null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.string("x").replace("y", null);
			Assert.fail("newChar null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.string("x").replaceAll("[", "#");
			Assert.fail("非法正则必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testStringSubstring() {
		Assert.assertEquals("tool", Fluent.string("suretool").substring(4).get());
		Assert.assertEquals("sure", Fluent.string("suretool").substring(0, 4).get());
		Assert.assertEquals("", Fluent.string("suretool").substring(8).get());
		try {
			Fluent.string("suretool").substring(9);
			Assert.fail("越界必须 StringIndexOutOfBoundsException");
		} catch (StringIndexOutOfBoundsException expected) {
		}
		try {
			Fluent.string("suretool").substring(4, 2);
			Assert.fail("begin>end 必须 StringIndexOutOfBoundsException");
		} catch (StringIndexOutOfBoundsException expected) {
		}
	}

	@Test
	public void testStringPad() {
		Assert.assertEquals("007", Fluent.string("7").padLeft(3, '0').get());
		Assert.assertEquals("700", Fluent.string("7").padRight(3, '0').get());
		Assert.assertEquals("abc", Fluent.string("abc").padLeft(2, '0').get());
		Assert.assertEquals("abc", Fluent.string("abc").padRight(3, '0').get());
		Assert.assertEquals("--ab", Fluent.string("ab").padLeft(4, '-').get());
	}

	@Test
	public void testStringRepeat() {
		Assert.assertEquals("", Fluent.string("ab").repeat(0).get());
		Assert.assertEquals("ab", Fluent.string("ab").repeat(1).get());
		Assert.assertEquals("ababab", Fluent.string("ab").repeat(3).get());
		try {
			Fluent.string("ab").repeat(-1);
			Assert.fail("count<0 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testStringStripPrefixSuffix() {
		Assert.assertEquals("github.tasure", Fluent.string("io.github.tasure").stripPrefix("io.").get());
		Assert.assertEquals("io.github.tasure", Fluent.string("io.github.tasure").stripPrefix("com.").get());
		Assert.assertEquals("report", Fluent.string("report.md").stripSuffix(".md").get());
		Assert.assertEquals("report.md", Fluent.string("report.md").stripSuffix(".txt").get());
		try {
			Fluent.string("x").stripPrefix(null);
			Assert.fail("prefix null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.string("x").stripSuffix(null);
			Assert.fail("suffix null 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testStringNotBlank() {
		Assert.assertEquals("ok", Fluent.string("  ok  ").trim().notBlank().get());
		try {
			Fluent.string("   ").notBlank();
			Assert.fail("纯空白必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.string("").notBlank();
			Assert.fail("空串必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testStringTerminal() {
		FluentString f = Fluent.string("SureTool");
		Assert.assertEquals("SureTool", f.get());
		Assert.assertEquals(8, f.length());
		Assert.assertFalse(f.isEmpty());
		Assert.assertTrue(Fluent.string("").isEmpty());
		Assert.assertFalse(f.isBlank());
		Assert.assertTrue(Fluent.string(" \t").isBlank());
		Assert.assertTrue(f.startsWith("Sure"));
		Assert.assertFalse(f.startsWith("Tool"));
		Assert.assertTrue(f.endsWith("Tool"));
		Assert.assertTrue(f.contains("ureT"));
		Assert.assertTrue(f.equalsIgnoreCase("suretool"));
		Assert.assertFalse(f.equalsIgnoreCase(null));
		Assert.assertEquals("SureTool", f.toString());
		try {
			f.startsWith(null);
			Assert.fail("prefix null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			f.contains(null);
			Assert.fail("fragment null 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testStringNullCreate() {
		try {
			Fluent.string(null);
			Assert.fail("string(null) 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.of((String) null);
			Assert.fail("of(null) 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	// ============ FluentList ============

	@Test
	public void testListCreateVariants() {
		Assert.assertTrue(Fluent.list().isEmpty());
		Assert.assertEquals(1, Fluent.listOf("a").count());
		Assert.assertEquals(Arrays.asList("a", "b"), Fluent.listOf("a", "b").toList());
		List<String> source = new ArrayList<>(Arrays.asList("x", "y"));
		FluentList<String> from = Fluent.of(source);
		source.add("z");
		Assert.assertEquals("防御拷贝：原集合修改不影响管道", Arrays.asList("x", "y"), from.toList());
		try {
			Fluent.listOf("a", null);
			Assert.fail("含 null 元素必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.of((List<String>) null);
			Assert.fail("null 集合必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testListAdd() {
		Assert.assertEquals(Arrays.asList("a", "b"),
				Fluent.<String>list().add("a").add("b").toList());
		Assert.assertEquals(Arrays.asList("a", "b", "c"),
				Fluent.listOf("a").addAll(Arrays.asList("b", "c")).toList());
		try {
			Fluent.list().add(null);
			Assert.fail("add(null) 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.list().addAll(Arrays.asList("a", null));
			Assert.fail("addAll 含 null 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testListMap() {
		List<Integer> lens = Fluent.listOf("a", "abc").map(String::length).toList();
		Assert.assertEquals(Arrays.asList(1, 3), lens);
		try {
			Fluent.listOf("a").map(null);
			Assert.fail("mapper null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.listOf("a").map(s -> null);
			Assert.fail("map 结果含 null 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testListFilterDistinctSorted() {
		Assert.assertEquals(Arrays.asList("a", "b", "c"),
				Fluent.listOf("c", "a", "b", "a").distinct().sorted().toList());
		Assert.assertEquals(Arrays.asList("b", "a"),
				Fluent.listOf("a", "b").sorted(Comparator.reverseOrder()).toList());
		Assert.assertEquals(Arrays.asList("b", "bb"),
				Fluent.listOf("a", "b", "bb").filter(s -> s.length() > 0 && !s.equals("a")).toList());
		try {
			Fluent.listOf("a").filter(null);
			Assert.fail("predicate null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.listOf("a").sorted(null);
			Assert.fail("comparator null 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testListLimitSkipReverse() {
		Assert.assertEquals(Arrays.asList(1, 2), Fluent.listOf(1, 2, 3).limit(2).toList());
		Assert.assertEquals(Arrays.asList(1, 2, 3), Fluent.listOf(1, 2, 3).limit(5).toList());
		Assert.assertEquals(Arrays.asList(3), Fluent.listOf(1, 2, 3).skip(2).toList());
		Assert.assertTrue(Fluent.listOf(1, 2).skip(5).isEmpty());
		Assert.assertEquals(Arrays.asList("b", "a"), Fluent.listOf("a", "b").reverse().toList());
		try {
			Fluent.listOf(1).limit(-1);
			Assert.fail("limit<0 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.listOf(1).skip(-1);
			Assert.fail("skip<0 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testListTerminal() {
		List<String> immutable = Fluent.listOf("a", "b").toList();
		try {
			immutable.add("c");
			Assert.fail("toList 必须不可变");
		} catch (UnsupportedOperationException expected) {
		}
		FluentList<String> pipe = Fluent.listOf("a", "b");
		Object[] arr = pipe.toArray();
		arr[0] = "z";
		Assert.assertEquals("toArray 独立副本", Arrays.asList("a", "b"), pipe.toList());
		Assert.assertEquals(2, pipe.count());
		Assert.assertFalse(pipe.isEmpty());
		Assert.assertTrue(pipe.contains("a"));
		Assert.assertFalse(pipe.contains(null));
		Assert.assertEquals("b", pipe.get(1));
		try {
			pipe.get(5);
			Assert.fail("get 越界必须 IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException expected) {
		}
		Assert.assertEquals("a-b-c", Fluent.listOf("a", "b", "c").join("-"));
		Assert.assertEquals("", Fluent.list().join(","));
		try {
			Fluent.listOf("a").join(null);
			Assert.fail("delimiter null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		Assert.assertEquals("[a, b]", pipe.toString());
	}

	@Test
	public void testListIntermediateDoesNotMutateOriginal() {
		List<String> source = new ArrayList<>(Arrays.asList("c", "a"));
		FluentList<String> pipe = Fluent.of(source);
		pipe.sorted().reverse().distinct();
		Assert.assertEquals("中间操作不修改原集合", Arrays.asList("c", "a"), source);
	}

	// ============ FluentMap ============

	@Test
	public void testMapCreateVariants() {
		Assert.assertTrue(Fluent.map().isEmpty());
		Assert.assertEquals(1, Fluent.mapOf("a", 1).size());
		Map<String, Integer> source = new LinkedHashMap<>();
		source.put("a", 1);
		source.put("b", 2);
		FluentMap<String, Integer> from = FluentMap.from(source);
		source.put("c", 3);
		Assert.assertEquals("防御拷贝：原 Map 修改不影响管道", 2, from.size());
		try {
			Fluent.mapOf(null, 1);
			Assert.fail("null 键必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.mapOf("a", null);
			Assert.fail("null 值必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			FluentMap.from(null);
			Assert.fail("null Map 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testMapPut() {
		Map<String, Integer> m = Fluent.<String, Integer>map().put("a", 1).put("b", 2).toMap();
		Assert.assertEquals(2, m.size());
		// putIfAbsent：已存在键不覆盖
		Assert.assertEquals(Integer.valueOf(1),
				Fluent.mapOf("a", 1).putIfAbsent("a", 99).get("a"));
		Assert.assertEquals(Integer.valueOf(99),
				Fluent.mapOf("a", 1).putIfAbsent("b", 99).get("b"));
		// remove
		Assert.assertFalse(Fluent.mapOf("a", 1).remove("a").containsKey("a"));
		Assert.assertEquals(1, Fluent.mapOf("a", 1).remove("zz").size());
		Assert.assertEquals(1, Fluent.mapOf("a", 1).remove(null).size());
		try {
			Fluent.map().put(null, 1);
			Assert.fail("put null 键必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.map().put("a", null);
			Assert.fail("put null 值必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testMapTransform() {
		Map<String, String> mv = Fluent.<String, Integer>map()
				.put("a", 1).mapValues(v -> "v" + v).toMap();
		Assert.assertEquals("v1", mv.get("a"));
		Map<String, Integer> fk = Fluent.<String, Integer>map()
				.put("a", 1).put("b", 2).filterKeys(k -> k.equals("a")).toMap();
		Assert.assertEquals(1, fk.size());
		Assert.assertFalse(fk.containsKey("b"));
		Map<String, Integer> fv = Fluent.<String, Integer>map()
				.put("a", 1).put("b", 2).filterValues(v -> v > 1).toMap();
		Assert.assertEquals(1, fv.size());
		Assert.assertTrue(fv.containsKey("b"));
		try {
			Fluent.map().mapValues(null);
			Assert.fail("mapper null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.mapOf("a", 1).mapValues(v -> null);
			Assert.fail("mapValues 结果 null 必须 IAE");
		} catch (IllegalArgumentException expected) {
		}
		try {
			Fluent.map().filterKeys(null);
			Assert.fail("predicate null 必须 NPE");
		} catch (NullPointerException expected) {
		}
		try {
			Fluent.map().filterValues(null);
			Assert.fail("predicate null 必须 NPE");
		} catch (NullPointerException expected) {
		}
	}

	@Test
	public void testMapTerminal() {
		FluentMap<String, Integer> pipe = Fluent.mapOf("a", 1).put("b", 2);
		Map<String, Integer> immutable = pipe.toMap();
		try {
			immutable.put("c", 3);
			Assert.fail("toMap 必须不可变");
		} catch (UnsupportedOperationException expected) {
		}
		Assert.assertEquals(Integer.valueOf(1), pipe.get("a"));
		Assert.assertNull(pipe.get("zz"));
		Assert.assertEquals(2, pipe.size());
		Assert.assertFalse(pipe.isEmpty());
		Assert.assertTrue(pipe.containsKey("a"));
		Assert.assertFalse(pipe.containsKey(null));
		Set<String> keys = pipe.keys();
		try {
			keys.add("z");
			Assert.fail("keys 必须不可变");
		} catch (UnsupportedOperationException expected) {
		}
		Assert.assertEquals(Arrays.asList("a", "b"), new ArrayList<>(keys));
		List<Integer> values = pipe.values();
		try {
			values.add(9);
			Assert.fail("values 必须不可变");
		} catch (UnsupportedOperationException expected) {
		}
		Assert.assertEquals(Arrays.asList(1, 2), values);
		Assert.assertEquals("{a=1, b=2}", pipe.toString());
	}

	@Test
	public void testMapPreservesInsertionOrder() {
		Map<String, Integer> m = Fluent.<String, Integer>map()
				.put("b", 2).put("a", 1).put("c", 3).toMap();
		Assert.assertEquals(Arrays.asList("b", "a", "c"), new ArrayList<>(m.keySet()));
	}

	// ============ Fluent 统一入口 ============

	@Test
	public void testFluentEntryDispatches() {
		Assert.assertTrue(Fluent.of("x") instanceof FluentString);
		Assert.assertTrue(Fluent.of("x").trim() instanceof FluentString);
		Assert.assertTrue(Fluent.list() instanceof FluentList);
		Assert.assertTrue(Fluent.listOf(1, 2) instanceof FluentList);
		Assert.assertTrue(Fluent.of(Arrays.asList("a")) instanceof FluentList);
		Assert.assertTrue(Fluent.map() instanceof FluentMap);
		Assert.assertTrue(Fluent.mapOf("a", 1) instanceof FluentMap);
		Assert.assertTrue(Fluent.string("x") instanceof FluentString);
	}

	@Test
	public void testFluentTypedUsage() {
		FluentList<String> typed = Fluent.<String>list().add("a");
		Assert.assertEquals(Arrays.asList("a"), typed.toList());
		FluentMap<String, Integer> map = Fluent.<String, Integer>map().put("k", 1);
		Assert.assertEquals(Integer.valueOf(1), map.get("k"));
	}
}
