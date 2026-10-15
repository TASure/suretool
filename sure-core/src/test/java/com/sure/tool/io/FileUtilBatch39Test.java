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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * 批 39：FileUtil 新增高频方法测试（校验和 / 符号链接 / 规范化路径 / 打开流 / 静默删除）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class FileUtilBatch39Test {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void testChecksumMD5() throws IOException {
		File f = tmp.newFile("md5.txt");
		FileUtil.writeUtf8String("hello", f);
		// "hello" 的已知 MD5：5d41402abc4b2a76b9719d911017c592
		Assert.assertEquals("5d41402abc4b2a76b9719d911017c592", FileUtil.checksumMD5(f));
		// 空文件 MD5：d41d8cd98f00b204e9800998ecf8427e
		File empty = tmp.newFile("empty.txt");
		Assert.assertEquals("d41d8cd98f00b204e9800998ecf8427e", FileUtil.checksumMD5(empty));
		try {
			FileUtil.checksumMD5(new File(tmp.getRoot(), "not-exist.md5"));
			Assert.fail("应当抛 IOException");
		} catch (IOException expected) {
			// 预期异常
		}
	}

	@Test
	public void testChecksumSHA1() throws IOException {
		File f = tmp.newFile("sha1.txt");
		FileUtil.writeUtf8String("hello", f);
		// "hello" 的已知 SHA-1：aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d
		Assert.assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d", FileUtil.checksumSHA1(f));
	}

	@Test
	public void testChecksumCRC32() throws IOException {
		File f = tmp.newFile("crc32.txt");
		FileUtil.writeUtf8String("hello", f);
		// "hello" 的已知 CRC32：0x3610a686 = 907060870
		Assert.assertEquals(907060870L, FileUtil.checksumCRC32(f));
		Assert.assertEquals(0L, FileUtil.checksumCRC32(new File(tmp.getRoot(), "not-exist.crc")));
	}

	@Test
	public void testIsSymlink() throws IOException {
		File target = tmp.newFile("target.txt");
		FileUtil.writeUtf8String("x", target);
		Assert.assertFalse(FileUtil.isSymlink(target));
		Assert.assertFalse(FileUtil.isSymlink((File) null));
		Assert.assertFalse(FileUtil.isSymlink((String) null));
		Assert.assertFalse(FileUtil.isSymlink(""));
		Assert.assertFalse(FileUtil.isSymlink(tmp.getRoot().getAbsolutePath() + "/no-such-file"));
		// 平台允许时创建符号链接并断言
		try {
			File link = new File(tmp.getRoot(), "link.txt");
			Files.createSymbolicLink(link.toPath(), target.toPath());
			Assert.assertTrue(FileUtil.isSymlink(link));
			Assert.assertTrue(FileUtil.isSymlink(link.getAbsolutePath()));
		} catch (UnsupportedOperationException | IOException e) {
			// 平台不支持符号链接时跳过（如部分 CI 环境）
			Assert.assertTrue(true);
		}
	}

	@Test
	public void testGetCanonicalPath() throws IOException {
		File f = tmp.newFile("c.txt");
		Assert.assertEquals(f.getCanonicalPath(), FileUtil.getCanonicalPath(f));
		Assert.assertEquals(f.getCanonicalPath(), FileUtil.getCanonicalPath(f.getAbsolutePath()));
		Assert.assertNull(FileUtil.getCanonicalPath((File) null));
		Assert.assertNull(FileUtil.getCanonicalPath((String) null));
	}

	@Test
	public void testOpenInputStream() throws IOException {
		File f = tmp.newFile("in.txt");
		FileUtil.writeUtf8String("abc", f);
		try (InputStream in = FileUtil.openInputStream(f)) {
			Assert.assertEquals(3, in.available());
		}
		try {
			FileUtil.openInputStream(new File(tmp.getRoot(), "no-such-in.txt"));
			Assert.fail("应当抛 UncheckedIOException");
		} catch (UncheckedIOException expected) {
			// 预期异常
		}
	}

	@Test
	public void testOpenOutputStream() throws IOException {
		File f = new File(tmp.getRoot(), "out.txt");
		try (OutputStream out = FileUtil.openOutputStream(f)) {
			out.write("data".getBytes(StandardCharsets.UTF_8));
		}
		Assert.assertEquals("data", FileUtil.readUtf8String(f));
		// 目录路径应抛 UncheckedIOException
		try {
			FileUtil.openOutputStream(tmp.getRoot());
			Assert.fail("应当抛 UncheckedIOException");
		} catch (UncheckedIOException expected) {
			// 预期异常
		}
	}

	@Test
	public void testDeleteQuietly() throws IOException {
		File f = tmp.newFile("del.txt");
		Assert.assertTrue(FileUtil.deleteQuietly(f));
		Assert.assertFalse(f.exists());
		Assert.assertFalse(FileUtil.deleteQuietly(f));
		Assert.assertFalse(FileUtil.deleteQuietly((File) null));
		// 非空目录静默失败
		File dir = tmp.newFolder("non-empty");
		FileUtil.writeUtf8String("x", new File(dir, "child.txt"));
		Assert.assertFalse(FileUtil.deleteQuietly(dir));
	}
}
