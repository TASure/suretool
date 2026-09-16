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

/**
 * P4（v0.2.0）第三十二批测试：字符串包裹、文件目录/MIME、日期差值、Map/Coll 便捷。
 */
public class P4Features27Test {

	@Test
	public void testStrWrap() {
		Assert.assertTrue(com.sure.tool.util.StrUtil.isWrap("[abc]", "[", "]"));
		Assert.assertTrue(com.sure.tool.util.StrUtil.isWrap("<tag>", "<", ">"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.isWrap("abc", "[", "]"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.isWrap("[abc", "[", "]"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.isWrap(null, "[", "]"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.isWrap("[abc]", "", "]"));
		Assert.assertFalse(com.sure.tool.util.StrUtil.isWrap("[abc]", "[", null));

		Assert.assertEquals("[abc]", com.sure.tool.util.StrUtil.wrap("abc", "[", "]"));
		Assert.assertEquals("|abc|", com.sure.tool.util.StrUtil.wrap("abc", "|", "|"));
		Assert.assertNull(com.sure.tool.util.StrUtil.wrap(null, "[", "]"));
		Assert.assertEquals("abc", com.sure.tool.util.StrUtil.wrap("abc", null, null));

		Assert.assertEquals("abc", com.sure.tool.util.StrUtil.unWrap("[abc]", "[", "]"));
		Assert.assertEquals("abc", com.sure.tool.util.StrUtil.unWrap("abc", "[", "]"));
		Assert.assertEquals("", com.sure.tool.util.StrUtil.unWrap("[]", "[", "]"));
		Assert.assertNull(com.sure.tool.util.StrUtil.unWrap(null, "[", "]"));
		Assert.assertEquals("[ab]c", com.sure.tool.util.StrUtil.unWrap("[ab]c", "[", "]"));
	}

	@Test
	public void testFileMimeAndDir() throws Exception {
		Assert.assertEquals("image/png", com.sure.tool.io.FileUtil.getMimeType("a.PNG"));
		Assert.assertEquals("application/json", com.sure.tool.io.FileUtil.getMimeType("config.json"));
		Assert.assertEquals("text/plain", com.sure.tool.io.FileUtil.getMimeType("readme.md"));
		Assert.assertEquals("application/pdf", com.sure.tool.io.FileUtil.getMimeType("doc.pdf"));
		Assert.assertEquals("application/octet-stream", com.sure.tool.io.FileUtil.getMimeType("file.xyz"));
		Assert.assertNull(com.sure.tool.io.FileUtil.getMimeType("noext"));
		Assert.assertNull(com.sure.tool.io.FileUtil.getMimeType(null));
		Assert.assertEquals("application/zip", com.sure.tool.io.FileUtil.getMimeType("a.zip"));

		java.io.File dir = new java.io.File(com.sure.tool.io.FileUtil.getTmpDir(),
				"sure-p4-test-" + System.nanoTime());
		com.sure.tool.io.FileUtil.mkdir(dir);
		java.io.File sub = new java.io.File(dir, "sub");
		com.sure.tool.io.FileUtil.mkdir(sub);
		java.io.File f1 = com.sure.tool.io.FileUtil.writeUtf8String("hello", new java.io.File(sub, "a.txt"));
		Assert.assertTrue(f1.exists());

		java.io.File dest = new java.io.File(com.sure.tool.io.FileUtil.getTmpDir(),
				"sure-p4-dest-" + System.nanoTime());
		java.io.File copied = com.sure.tool.io.FileUtil.copyDir(dir, dest);
		Assert.assertNotNull(copied);
		Assert.assertTrue(new java.io.File(dest, "sub/a.txt").exists());

		com.sure.tool.io.FileUtil.clean(dir);
		Assert.assertTrue(dir.exists());
		Assert.assertEquals(0, com.sure.tool.io.FileUtil.size(dir));
		com.sure.tool.io.FileUtil.delete(dir);
		com.sure.tool.io.FileUtil.delete(dest);
	}

	@Test
	public void testDateBetween() {
		java.util.Date start = com.sure.tool.date.DateUtil.parse("2026-09-09 10:00:00");
		java.util.Date end = com.sure.tool.date.DateUtil.parse("2026-09-09 10:00:05");
		Assert.assertEquals(5000L, com.sure.tool.date.DateUtil.betweenMs(start, end));
		Assert.assertEquals(5L, com.sure.tool.date.DateUtil.betweenSeconds(start, end));
		Assert.assertEquals(-5000L, com.sure.tool.date.DateUtil.betweenMs(end, start));
		try {
			com.sure.tool.date.DateUtil.betweenMs(null, end);
			Assert.fail("应当抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void testMapAndColl() {
		java.util.Map<String, Integer> m = new java.util.HashMap<>();
		m.put("b", 2);
		m.put("a", 1);
		m.put("c", 3);
		java.util.Map<String, Integer> sorted = com.sure.tool.collection.MapUtil.sortByKey(m);
		java.util.List<String> keys = new java.util.ArrayList<>(sorted.keySet());
		Assert.assertEquals("a", keys.get(0));
		Assert.assertEquals("b", keys.get(1));
		Assert.assertEquals("c", keys.get(2));
		Assert.assertNull(com.sure.tool.collection.MapUtil.sortByKey(null));
		java.util.Map<String, Integer> empty = new java.util.HashMap<>();
		Assert.assertSame(empty, com.sure.tool.collection.MapUtil.sortByKey(empty));

		java.util.Map<String, Object> kv = new java.util.HashMap<>();
		kv.put("name", "Sure");
		kv.put("n", 42);
		Assert.assertEquals("Sure", com.sure.tool.collection.MapUtil.getStr(kv, "name"));
		Assert.assertNull(com.sure.tool.collection.MapUtil.getStr(kv, "missing"));
		Assert.assertNull(com.sure.tool.collection.MapUtil.getStr(null, "name"));
		Assert.assertEquals("42", com.sure.tool.collection.MapUtil.getStr(kv, "n"));

		java.util.List<String> list = java.util.List.of("a", "b", "c");
		Assert.assertEquals("b", com.sure.tool.collection.CollUtil.get(list, 1, "d"));
		Assert.assertEquals("d", com.sure.tool.collection.CollUtil.get(list, 99, "d"));
		Assert.assertEquals("d", com.sure.tool.collection.CollUtil.get(list, -99, "d"));
		Assert.assertEquals("c", com.sure.tool.collection.CollUtil.get(list, -1, "d"));
		Assert.assertEquals("d", com.sure.tool.collection.CollUtil.get(null, 0, "d"));
		Assert.assertEquals("b", com.sure.tool.collection.CollUtil.get(new java.util.LinkedHashSet<>(list), 1, "d"));
	}


}
