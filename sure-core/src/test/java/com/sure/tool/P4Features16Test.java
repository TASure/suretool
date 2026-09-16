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

import org.junit.Assert;
import org.junit.Test;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十一批测试。
 */
public class P4Features16Test {

	@Test
	public void testBatch21() {
		// TreeUtil.findNode
		com.sure.tool.collection.TreeNode<String> root = new com.sure.tool.collection.TreeNode<String>("1", null, "root");
		com.sure.tool.collection.TreeNode<String> child = new com.sure.tool.collection.TreeNode<String>("2", "1", "child");
		root.addChild(child);
		child.addChild(new com.sure.tool.collection.TreeNode<String>("3", "2", "leaf"));
		Assert.assertEquals("3", com.sure.tool.collection.TreeUtil.findNode(root, "3").getId());
		Assert.assertNull(com.sure.tool.collection.TreeUtil.findNode(root, "99"));

		// StrUtil.maxLength
		Assert.assertEquals("abc", StrUtil.maxLength("abc", 5));
		Assert.assertEquals("abc...", StrUtil.maxLength("abcdefgh", 6));
		Assert.assertEquals("abc", StrUtil.maxLength("abcdefgh", 3));

		// ArrayUtil.isSorted
		Assert.assertTrue(com.sure.tool.util.ArrayUtil.isSorted(new Integer[] {1, 2, 3}));
		Assert.assertFalse(com.sure.tool.util.ArrayUtil.isSorted(new Integer[] {1, 3, 2}));
		Assert.assertTrue(com.sure.tool.util.ArrayUtil.isSorted(new Integer[] {3, 2, 1}, false));
		Assert.assertTrue(com.sure.tool.util.ArrayUtil.isSorted(new Integer[] {5}));

		// CsvUtil 流式读写
		try {
			java.io.StringWriter writer = new java.io.StringWriter();
			com.sure.tool.collection.CsvUtil.write(writer, java.util.List.of(
					java.util.List.of("a", "b"), java.util.List.of("1", "2")));
			java.util.List<java.util.List<String>> rows = com.sure.tool.collection.CsvUtil.read(
					new java.io.ByteArrayInputStream(writer.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)),
					java.nio.charset.StandardCharsets.UTF_8);
			Assert.assertEquals(2, rows.size());
			Assert.assertEquals("a", rows.get(0).get(0));
			Assert.assertEquals("2", rows.get(1).get(1));
		} catch (java.io.IOException e) {
			Assert.fail(e.getMessage());
		}
	}
}
