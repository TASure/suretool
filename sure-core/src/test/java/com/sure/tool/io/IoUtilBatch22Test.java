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
package com.sure.tool.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * IoUtil 批22 增强测试。
 */
public class IoUtilBatch22Test {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * copy(InputStream, File)：流写文件。
	 */
	@Test
	public void testCopyStreamToFile() throws Exception {
		File file = new File(folder.getRoot(), "sub/out.bin");
		byte[] data = "stream-data".getBytes(StandardCharsets.UTF_8);
		long written = IoUtil.copy(new ByteArrayInputStream(data), file);
		Assert.assertEquals(data.length, written);
		Assert.assertArrayEquals(data, FileUtil.readBytes(file));
	}

	/**
	 * copy(File, OutputStream)：文件读流。
	 */
	@Test
	public void testCopyFileToStream() throws Exception {
		File file = folder.newFile("in.bin");
		byte[] data = "file-data".getBytes(StandardCharsets.UTF_8);
		FileUtil.writeBytes(data, file);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long written = IoUtil.copy(file, out);
		Assert.assertEquals(data.length, written);
		Assert.assertArrayEquals(data, out.toByteArray());
	}

	/**
	 * writeLines：逐行写出。
	 */
	@Test
	public void testWriteLines() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IoUtil.writeLines(java.util.Arrays.asList("a", null, "c"), out, StandardCharsets.UTF_8);
		String text = out.toString(StandardCharsets.UTF_8);
		String sep = System.lineSeparator();
		Assert.assertEquals("a" + sep + sep + "c" + sep, text);
	}

	/**
	 * readLines 追加式读取返回行数与内容。
	 */
	@Test
	public void testReadLinesAppend() throws Exception {
		String content = "x" + System.lineSeparator() + "y" + System.lineSeparator() + "z";
		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		List<String> lines = new ArrayList<>();
		int count = IoUtil.readLines(in, StandardCharsets.UTF_8, lines);
		Assert.assertEquals(3, count);
		Assert.assertEquals(List.of("x", "y", "z"), lines);
	}
}
