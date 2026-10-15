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

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 39：IoUtil 新增高频方法测试（缓冲包装 / 精确读写 / 行迭代器 / 跨流复制）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class IoUtilBatch39Test {

	@Test
	public void testToBufferedInputStream() {
		Assert.assertNull(IoUtil.toBufferedInputStream(null));
		InputStream plain = new ByteArrayInputStream(new byte[] {1});
		BufferedInputStream wrapped = IoUtil.toBufferedInputStream(plain);
		Assert.assertTrue(wrapped instanceof BufferedInputStream);
		// 已缓冲的不再二次包装
		Assert.assertSame(wrapped, IoUtil.toBufferedInputStream(wrapped));
	}

	@Test
	public void testToBufferedReader() {
		Assert.assertNull(IoUtil.toBufferedReader(null));
		BufferedReader br = IoUtil.toBufferedReader(new StringReader("abc"));
		Assert.assertNotNull(br);
		Assert.assertSame(br, IoUtil.toBufferedReader(br));
	}

	@Test
	public void testReadFully() throws IOException {
		byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
		byte[] buf = new byte[5];
		IoUtil.readFully(new ByteArrayInputStream(data), buf, 0, 5);
		Assert.assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), buf);
		// 读不满应抛 IOException
		try {
			IoUtil.readFully(new ByteArrayInputStream(data), new byte[20], 0, 20);
			Assert.fail("应当抛 IOException");
		} catch (IOException expected) {
			// 预期异常
		}
	}

	@Test
	public void testSkipFully() throws IOException {
		byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		IoUtil.skipFully(in, 6);
		Assert.assertEquals("world", new String(IoUtil.readBytes(in), StandardCharsets.UTF_8));
		// 不足应抛 IOException
		try {
			IoUtil.skipFully(new ByteArrayInputStream(data), 999);
			Assert.fail("应当抛 IOException");
		} catch (IOException expected) {
			// 预期异常
		}
		try {
			IoUtil.skipFully(in, -1);
			Assert.fail("应当抛 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 预期异常
		}
		// 恰好跳过
		ByteArrayInputStream exact = new ByteArrayInputStream(data);
		IoUtil.skipFully(exact, data.length);
		Assert.assertEquals(-1, exact.read());
	}

	@Test
	public void testToStringReader() throws IOException {
		Assert.assertEquals("hello", IoUtil.toString(new StringReader("hello")));
		Assert.assertNull(IoUtil.toString(null));
		Assert.assertEquals("", IoUtil.toString(new StringReader("")));
	}

	@Test
	public void testLineIteratorReader() throws IOException {
		try (IoUtil.LineIterator it = IoUtil.lineIterator(new StringReader("a\nb\r\nc\nd"))) {
			List<String> lines = new ArrayList<>();
			while (it.hasNext()) {
				lines.add(it.next());
			}
			Assert.assertEquals(java.util.Arrays.asList("a", "b", "c", "d"), lines);
		}
		try (IoUtil.LineIterator it = IoUtil.lineIterator(new StringReader(""))) {
			Assert.assertFalse(it.hasNext());
		}
		try {
			IoUtil.LineIterator empty = IoUtil.lineIterator(new StringReader(""));
			empty.next();
			Assert.fail("应当抛 NoSuchElementException");
		} catch (NoSuchElementException expected) {
			// 预期异常
		}
	}

	@Test
	public void testLineIteratorInputStream() throws IOException {
		try (IoUtil.LineIterator it = IoUtil.lineIterator(
				new ByteArrayInputStream("x\ny\nz".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)) {
			Assert.assertEquals("x", it.next());
			Assert.assertEquals("y", it.next());
			Assert.assertEquals("z", it.next());
			Assert.assertFalse(it.hasNext());
		}
	}

	@Test
	public void testContentEqualsIgnoreEOL() throws IOException {
		Assert.assertTrue(IoUtil.contentEqualsIgnoreEOL(
				new StringReader("a\r\nb"), new StringReader("a\nb")));
		Assert.assertFalse(IoUtil.contentEqualsIgnoreEOL(
				new StringReader("a"), new StringReader("b")));
		Assert.assertFalse(IoUtil.contentEqualsIgnoreEOL(
				new StringReader("a\n"), new StringReader("a\nb")));
		Assert.assertTrue(IoUtil.contentEqualsIgnoreEOL(null, null));
		Assert.assertFalse(IoUtil.contentEqualsIgnoreEOL(new StringReader("a"), null));
	}

	@Test
	public void testCopyReaderToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long copied = IoUtil.copy(new StringReader("你好"), out, StandardCharsets.UTF_8);
		Assert.assertEquals("你好", out.toString(StandardCharsets.UTF_8.name()));
		// 返回值为复制的字符数
		Assert.assertEquals(2L, copied);
		Assert.assertEquals(0L, IoUtil.copy(null, out, StandardCharsets.UTF_8));
	}

	@Test
	public void testCopyInputStreamToWriter() throws IOException {
		StringWriter writer = new StringWriter();
		long copied = IoUtil.copy(
				new ByteArrayInputStream("hi".getBytes(StandardCharsets.UTF_8)), writer, StandardCharsets.UTF_8);
		Assert.assertEquals("hi", writer.toString());
		Assert.assertEquals(2L, copied);
		Assert.assertEquals(0L, IoUtil.copy(null, writer, StandardCharsets.UTF_8));
	}

	@Test
	public void testCloseQuietly() {
		IoUtil.closeQuietly((java.io.Closeable) null);
	}
}
