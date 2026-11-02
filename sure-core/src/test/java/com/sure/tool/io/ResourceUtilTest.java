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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 批 52：ResourceUtil 测试（classpath / 文件 / URL 前缀 / 不存在 / 非法位置）。
 *
 * @author suretool
 * @since 1.20.0
 */
public class ResourceUtilTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testClasspathResource() throws IOException {
		List<String> expected = Arrays.asList("suretool-resource", "第二行");
		Assert.assertEquals(expected, ResourceUtil.readUtf8String("sure-resource-test.txt").lines().toList());
		Assert.assertEquals(expected, ResourceUtil.readUtf8String("classpath:sure-resource-test.txt").lines().toList());
		Assert.assertEquals(expected, ResourceUtil.readUtf8String("classpath:/sure-resource-test.txt").lines().toList());
		Assert.assertEquals(expected, ResourceUtil.readUtf8String("/sure-resource-test.txt").lines().toList());
		Assert.assertTrue(ResourceUtil.exists("classpath:sure-resource-test.txt"));
		Assert.assertNotNull(ResourceUtil.getResource("sure-resource-test.txt"));
		Assert.assertEquals("file", ResourceUtil.getResource("sure-resource-test.txt").getProtocol());
	}

	@Test
	public void testFileResource() throws IOException {
		File file = temporaryFolder.newFile("data.txt");
		FileUtil.writeUtf8String("文件内容", file);
		Assert.assertEquals("文件内容", ResourceUtil.readUtf8String(file.getAbsolutePath()));
		Assert.assertArrayEquals("文件内容".getBytes(StandardCharsets.UTF_8), ResourceUtil.readBytes(file.getAbsolutePath()));
		String fileUrl = file.toURI().toString();
		Assert.assertEquals("文件内容", ResourceUtil.readUtf8String(fileUrl));
		Assert.assertTrue(ResourceUtil.exists(fileUrl));
		Assert.assertEquals("文件内容", ResourceUtil.readString(fileUrl, null));
	}

	@Test
	public void testCharset() throws IOException {
		File file = temporaryFolder.newFile("utf16.txt");
		Files.writeString(file.toPath(), "字符集", StandardCharsets.UTF_16LE);
		Assert.assertEquals("字符集", ResourceUtil.readString(file.getAbsolutePath(), StandardCharsets.UTF_16LE));
	}

	@Test
	public void testGetStream() throws IOException {
		try (InputStream input = ResourceUtil.getStream("classpath:sure-resource-test.txt")) {
			Assert.assertNotNull(input);
			Assert.assertTrue(IoUtil.readBytes(input).length > 0);
		}
		Assert.assertNull(ResourceUtil.getStream(null));
		Assert.assertNull(ResourceUtil.getStream("no-such-resource-xyz"));
	}

	@Test
	public void testUrlResource() {
		Assert.assertEquals("http", ResourceUtil.getResource("http://example.com/data.txt").getProtocol());
		Assert.assertEquals("https", ResourceUtil.getResource("https://example.com/data.txt").getProtocol());
		Assert.assertTrue(ResourceUtil.exists("https://example.com"));
	}

	@Test
	public void testMissingResource() {
		Assert.assertNull(ResourceUtil.getResource(null));
		Assert.assertNull(ResourceUtil.getResource("   "));
		Assert.assertNull(ResourceUtil.getResource("classpath:"));
		Assert.assertNull(ResourceUtil.getResource("classpath:/"));
		Assert.assertNull(ResourceUtil.getResource("no-such-resource-xyz"));
		Assert.assertFalse(ResourceUtil.exists("no-such-resource-xyz"));
		try {
			ResourceUtil.readBytes("no-such-resource-xyz");
			Assert.fail("should throw");
		} catch (IOException expected) {
			// 资源不存在
		}
		try {
			ResourceUtil.readUtf8String("no-such-resource-xyz");
			Assert.fail("should throw");
		} catch (IOException expected) {
			// 资源不存在
		}
	}

	@Test
	public void testInvalidUrl() {
		try {
			ResourceUtil.getResource("http://a b");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法 URL
		}
		try {
			ResourceUtil.getResource("file://bad host/data.txt");
			Assert.fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 非法 file URL
		}
	}
}
