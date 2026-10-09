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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

/**
 * IoUtil 覆盖率补测：null 参数校验、安静关闭、流内容比较分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class IoUtilGapTest {

	@Test
	public void testNullParamChecks() throws Exception {
		try {
			IoUtil.copy((java.io.InputStream) null, new java.io.File("/tmp/x"));
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			IoUtil.copy(new java.io.File("/tmp/x"), (java.io.OutputStream) null);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			IoUtil.writeLines(new ArrayList<>(), null, StandardCharsets.UTF_8);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			IoUtil.readLines(null, StandardCharsets.UTF_8, new ArrayList<>());
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testCloseQuietly() {
		IoUtil.closeQuietly((java.io.Closeable[]) null);
		java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[] {1});
		IoUtil.closeQuietly(in, null);
	}

	@Test
	public void testContentEquals() throws Exception {
		Assert.assertTrue(IoUtil.contentEquals(
				new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)),
				new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));
		Assert.assertFalse(IoUtil.contentEquals(
				new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)),
				new ByteArrayInputStream("abd".getBytes(StandardCharsets.UTF_8))));
		Assert.assertFalse(IoUtil.contentEquals(
				new ByteArrayInputStream("ab".getBytes(StandardCharsets.UTF_8)),
				new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8))));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IoUtil.toOutput(new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)), out);
		Assert.assertEquals("x", out.toString(StandardCharsets.UTF_8));
	}
}
