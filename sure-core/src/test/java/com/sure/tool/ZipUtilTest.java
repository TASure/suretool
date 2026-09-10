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

import com.sure.tool.io.FileUtil;
import com.sure.tool.io.ZipUtil;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil 单元测试。
 */
public class ZipUtilTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testZipUnzipRoundTrip() throws Exception {
		File srcDir = tempFolder.newFolder("src");
		FileUtil.writeUtf8String("hello", new File(srcDir, "a.txt"));
		FileUtil.writeUtf8String("world", new File(srcDir, "b.txt"));
		File sub = new File(srcDir, "sub");
		sub.mkdirs();
		FileUtil.writeUtf8String("nested", new File(sub, "c.txt"));

		File zipFile = new File(tempFolder.getRoot(), "out.zip");
		ZipUtil.zip(srcDir, zipFile);
		Assert.assertTrue(zipFile.exists());
		Assert.assertTrue(zipFile.length() > 0);

		File outDir = tempFolder.newFolder("out");
		ZipUtil.unzip(zipFile, outDir);

		File extractedDir = new File(outDir, srcDir.getName());
		Assert.assertEquals("hello", FileUtil.readUtf8String(new File(extractedDir, "a.txt")));
		Assert.assertEquals("world", FileUtil.readUtf8String(new File(extractedDir, "b.txt")));
		Assert.assertEquals("nested", FileUtil.readUtf8String(FileUtil.file(extractedDir.getAbsolutePath(), "sub", "c.txt")));
	}

	@Test
	public void testZipSingleFile() throws Exception {
		File file = new File(tempFolder.getRoot(), "single.txt");
		FileUtil.writeUtf8String("single", file);
		File zipFile = new File(tempFolder.getRoot(), "single.zip");
		ZipUtil.zip(file, zipFile);
		File outDir = tempFolder.newFolder("out2");
		ZipUtil.unzip(zipFile, outDir);
		Assert.assertEquals("single", FileUtil.readUtf8String(new File(outDir, "single.txt")));
	}

	@Test
	public void testZipStringPath() throws Exception {
		File srcDir = tempFolder.newFolder("src2");
		FileUtil.writeUtf8String("data", new File(srcDir, "d.txt"));
		String zipPath = new File(tempFolder.getRoot(), "str.zip").getAbsolutePath();
		ZipUtil.zip(srcDir.getAbsolutePath(), zipPath);
		File outDir = tempFolder.newFolder("out3");
		ZipUtil.unzip(zipPath, outDir.getAbsolutePath());
		Assert.assertEquals("data", FileUtil.readUtf8String(FileUtil.file(outDir.getAbsolutePath(), "src2", "d.txt")));
	}

	@Test
	public void testUnzipInvalidZipNoOutput() throws Exception {
		File bad = new File(tempFolder.getRoot(), "bad.zip");
		char[] chars = new char[100];
		java.util.Arrays.fill(chars, 'x');
		FileUtil.writeUtf8String(new String(chars), bad);
		File outDir = tempFolder.newFolder("out4");
		// JDK 17 的 ZipInputStream 对无效 zip 静默视为空，不抛异常且无产物
		ZipUtil.unzip(bad, outDir);
		Assert.assertEquals(0, FileUtil.loopFiles(outDir).size());
	}

	@Test
	public void testZipSlipProtection() throws Exception {
		File zipFile = new File(tempFolder.getRoot(), "evil.zip");
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
			zos.putNextEntry(new ZipEntry("../evil.txt"));
			zos.write("pwned".getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}
		try {
			ZipUtil.unzip(zipFile, tempFolder.newFolder("out5"));
			Assert.fail("应抛出 zip-slip 路径穿越异常");
		} catch (IOException expected) {
			// 预期行为
		}
		Assert.assertFalse(new File(tempFolder.getRoot().getParentFile(), "evil.txt").exists());
	}
}