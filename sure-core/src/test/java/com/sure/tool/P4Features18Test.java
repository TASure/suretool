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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十三批测试。
 */
public class P4Features18Test {

	@Test
	public void testBatch23() throws Exception {
		// StrUtil.center
		Assert.assertEquals("--ab--", StrUtil.center("ab", 6, '-'));
		Assert.assertEquals("--abc--", StrUtil.center("abc", 7, '-'));

		// DateUtil.isSameYear
		Assert.assertTrue(com.sure.tool.date.DateUtil.isSameYear(
				com.sure.tool.date.DateUtil.parse("2026-01-01"), com.sure.tool.date.DateUtil.parse("2026-12-31")));
		Assert.assertFalse(com.sure.tool.date.DateUtil.isSameYear(
				com.sure.tool.date.DateUtil.parse("2025-12-31"), com.sure.tool.date.DateUtil.parse("2026-01-01")));

		// CollUtil.zip
		Map<String, Integer> zipped = com.sure.tool.collection.CollUtil.zip(List.of("a", "b"), List.of(1, 2));
		Assert.assertEquals(2, zipped.size());
		Assert.assertEquals(Integer.valueOf(1), zipped.get("a"));
		Assert.assertEquals(1, com.sure.tool.collection.CollUtil.zip(List.of("a", "b"), List.of(1)).size());

		// RandomUtil.randomDay
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 12, 31);
		LocalDate day = com.sure.tool.util.RandomUtil.randomDay(start, end);
		Assert.assertFalse(day.isBefore(start));
		Assert.assertFalse(day.isAfter(end));

		// ZipUtil 多文件
		File dir = new File("target/p4zip");
		dir.mkdirs();
		File f1 = new File(dir, "a.txt");
		File f2 = new File(dir, "b.txt");
		com.sure.tool.io.FileUtil.writeString("hello", f1, StandardCharsets.UTF_8);
		com.sure.tool.io.FileUtil.writeString("world", f2, StandardCharsets.UTF_8);
		File zip = new File(dir, "multi.zip");
		com.sure.tool.io.ZipUtil.zip(List.of(f1, f2), zip, StandardCharsets.UTF_8);
		File out = new File(dir, "out");
		com.sure.tool.io.ZipUtil.unzip(zip, out, StandardCharsets.UTF_8);
		Assert.assertEquals("hello", com.sure.tool.io.FileUtil.readString(new File(out, "a.txt"), StandardCharsets.UTF_8));
		Assert.assertEquals("world", com.sure.tool.io.FileUtil.readString(new File(out, "b.txt"), StandardCharsets.UTF_8));
	}
}
