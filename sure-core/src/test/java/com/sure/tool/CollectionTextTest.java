package com.sure.tool.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * 集合文本增强测试：树构建、双向映射、字符串拼接、CSV。
 */
public class CollectionTextTest {

	// ---------- TreeUtil ----------

	@Test
	public void testTreeBuild() {
		List<TreeNode<Integer>> nodes = new ArrayList<>();
		nodes.add(new TreeNode<>(1, null, "root"));
		nodes.add(new TreeNode<>(2, 1, "child1"));
		nodes.add(new TreeNode<>(3, 1, "child2"));
		nodes.add(new TreeNode<>(4, 2, "grand"));
		List<TreeNode<Integer>> roots = TreeUtil.build(nodes, null);
		assertEquals(1, roots.size());
		TreeNode<Integer> root = roots.get(0);
		assertEquals(Integer.valueOf(1), root.getId());
		assertEquals(2, root.getChildren().size());
		assertEquals(1, root.getChildren().get(0).getChildren().size());
		assertEquals(3, TreeUtil.depth(root));

		final AtomicInteger count = new AtomicInteger();
		TreeUtil.walk(root, node -> count.incrementAndGet());
		assertEquals(4, count.get());
		assertEquals(4, TreeUtil.flatten(roots).size());
	}

	@Test
	public void testTreeBuildWithRootId() {
		List<TreeNode<String>> nodes = new ArrayList<>();
		nodes.add(new TreeNode<>("r", null, "root"));
		nodes.add(new TreeNode<>("a", "r", "a"));
		nodes.add(new TreeNode<>("b", "r", "b"));
		TreeNode<String> root = TreeUtil.buildTree(nodes, "r");
		assertEquals("root", root.getName());
		assertEquals(2, root.getChildren().size());
	}

	@Test
	public void testTreeEmpty() {
		assertTrue(TreeUtil.build(null, null).isEmpty());
		assertNull(TreeUtil.buildTree(new ArrayList<>(), "x"));
		assertEquals(0, TreeUtil.depth(null));
	}

	// ---------- BiMap ----------

	@Test
	public void testBiMapBasic() {
		BiMap<String, Integer> biMap = new BiMap<>();
		biMap.put("a", 1);
		biMap.put("b", 2);
		assertEquals(Integer.valueOf(1), biMap.get("a"));
		assertEquals("a", biMap.getKey(1));
		assertTrue(biMap.containsKey("a"));
		assertTrue(biMap.containsValue(2));
		assertEquals(2, biMap.size());
	}

	@Test
	public void testBiMapOverrideCleansOld() {
		BiMap<String, Integer> biMap = new BiMap<>();
		biMap.put("a", 1);
		biMap.put("b", 1);
		assertNull("重复值应移除旧键", biMap.get("a"));
		assertEquals("b", biMap.getKey(1));

		biMap.put("c", 2);
		biMap.put("c", 3);
		assertNull("重复键应移除旧值反查", biMap.getKey(2));
		assertEquals(Integer.valueOf(3), biMap.get("c"));
	}

	@Test
	public void testBiMapRemove() {
		BiMap<String, Integer> biMap = new BiMap<>();
		biMap.put("a", 1);
		assertEquals(Integer.valueOf(1), biMap.remove("a"));
		assertNull(biMap.getKey(1));
		assertTrue(biMap.isEmpty());
	}

	// ---------- StrJoiner ----------

	@Test
	public void testStrJoinerBasic() {
		StrJoiner joiner = StrJoiner.of("-");
		joiner.append("a").append("b").append("c");
		assertEquals("a-b-c", joiner.toString());
		assertTrue(joiner.hasContent());
	}

	@Test
	public void testStrJoinerNullSkipped() {
		assertEquals("a-c", StrJoiner.of("-").append("a").append((Object) null).append("c").toString());
	}

	@Test
	public void testStrJoinerPrefixSuffix() {
		StrJoiner joiner = StrJoiner.of(", ", "[", "]");
		joiner.appendAll(Arrays.asList(1, 2, 3));
		assertEquals("[1, 2, 3]", joiner.toString());
	}

	@Test
	public void testStrJoinerEmpty() {
		StrJoiner joiner = StrJoiner.of(",");
		assertEquals("", joiner.toString());
		assertFalse(joiner.hasContent());
		joiner.appendAll(new Object[] {});
		assertEquals("", joiner.toString());
	}

	// ---------- CsvUtil ----------

	@Test
	public void testCsvRoundTrip() {
		List<List<String>> rows = new ArrayList<>();
		rows.add(Arrays.asList("name", "age", "note"));
		rows.add(Arrays.asList("sure", "18", "a,b"));
		rows.add(Arrays.asList("he said \"hi\"", "", ""));
		String csv = CsvUtil.toCsv(rows);
		List<List<String>> parsed = CsvUtil.read(csv);
		assertEquals(rows, parsed);
	}

	@Test
	public void testCsvQuotedField() {
		List<List<String>> rows = CsvUtil.read("\"a,b\",c\r\n\"x\"\"y\",z");
		assertEquals(Arrays.asList("a,b", "c"), rows.get(0));
		assertEquals(Arrays.asList("x\"y", "z"), rows.get(1));
	}

	@Test
	public void testCsvFile() throws Exception {
		File file = File.createTempFile("suretool-csv", ".csv");
		try {
			CsvUtil.write(file, Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("1", "2")));
			List<List<String>> rows = CsvUtil.read(file);
			assertEquals(Arrays.asList("a", "b"), rows.get(0));
			assertEquals(Arrays.asList("1", "2"), rows.get(1));
		} finally {
			file.delete();
		}
	}
}
