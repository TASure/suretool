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
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * FileUtil 批22 增强测试。
 */
public class FileUtilBatch22Test {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * sizeFormat 各档位。
	 */
	@Test
	public void testSizeFormat() {
		Assert.assertEquals("0 B", FileUtil.sizeFormat(0));
		Assert.assertEquals("512 B", FileUtil.sizeFormat(512));
		Assert.assertEquals("1 KB", FileUtil.sizeFormat(1024));
		Assert.assertEquals("1.5 MB", FileUtil.sizeFormat(1024L * 1024L + 512L * 1024L));
		Assert.assertEquals("1 GB", FileUtil.sizeFormat(1024L * 1024L * 1024L));
		Assert.assertEquals("1 TB", FileUtil.sizeFormat(1024L * 1024L * 1024L * 1024L));
		// 负数按 0
		Assert.assertEquals("0 B", FileUtil.sizeFormat(-5));
	}

	/**
	 * size(String) 路径重载。
	 */
	@Test
	public void testSizeByPath() throws Exception {
		File file = folder.newFile("a.txt");
		FileUtil.writeUtf8String("hello", file);
		Assert.assertEquals(5, FileUtil.size(file.getAbsolutePath()));
		Assert.assertEquals(0, FileUtil.size("/not/exist/path"));
	}

	/**
	 * copy(String, String) 路径重载。
	 */
	@Test
	public void testCopyByPath() throws Exception {
		File src = folder.newFile("src.txt");
		FileUtil.writeUtf8String("content-22", src);
		String dest = folder.getRoot().getAbsolutePath() + "/sub/dest.txt";
		FileUtil.copy(src.getAbsolutePath(), dest);
		Assert.assertEquals("content-22", FileUtil.readUtf8String(new File(dest)));
	}

	/**
	 * walkFiles 深度遍历返回全部文件。
	 */
	@Test
	public void testWalkFiles() throws Exception {
		File root = folder.newFolder("root");
		FileUtil.writeUtf8String("1", new File(root, "a.txt"));
		FileUtil.writeUtf8String("2", new File(root, "b.log"));
		File sub = new File(root, "sub");
		Assert.assertTrue(sub.mkdirs());
		FileUtil.writeUtf8String("3", new File(sub, "c.txt"));
		List<Path> files = FileUtil.walkFiles(root.toPath());
		Assert.assertEquals(3, files.size());
		// 不存在的根返回空
		Assert.assertTrue(FileUtil.walkFiles(Path.of("/not/exist/root")).isEmpty());
		// null 安全
		Assert.assertTrue(FileUtil.walkFiles(null).isEmpty());
	}

	/**
	 * readLines(String) 路径重载。
	 */
	@Test
	public void testReadLinesByPath() throws Exception {
		File file = folder.newFile("lines.txt");
		FileUtil.writeUtf8Lines(List.of("a", "b", "c"), file);
		Assert.assertEquals(List.of("a", "b", "c"), FileUtil.readLines(file.getAbsolutePath()));
	}

	/**
	 * touch(String) 路径重载。
	 */
	@Test
	public void testTouchByPath() throws Exception {
		String path = folder.getRoot().getAbsolutePath() + "/t/dir/file.txt";
		File f = FileUtil.touch(path);
		Assert.assertTrue(f.exists());
		Assert.assertTrue(f.getParentFile().isDirectory());
	}

	/**
	 * lastModified 与 isNewer/isOlder。
	 */
	@Test
	public void testLastModifiedAndCompare() throws Exception {
		File oldFile = folder.newFile("old.txt");
		FileUtil.writeUtf8String("old", oldFile);
		File newFile = folder.newFile("new.txt");
		FileUtil.writeUtf8String("new", newFile);
		// 手工调整时间确保可比较
		Assert.assertTrue(newFile.setLastModified(oldFile.lastModified() + 60_000));
		Assert.assertTrue(FileUtil.isNewer(newFile, oldFile));
		Assert.assertTrue(FileUtil.isOlder(oldFile, newFile));
		Assert.assertTrue(FileUtil.lastModified(oldFile) > 0);
		Assert.assertEquals(0, FileUtil.lastModified(new File("/not/exist")));
		Assert.assertFalse(FileUtil.isNewer(newFile, new File("/not/exist")));
		Assert.assertFalse(FileUtil.isOlder(new File("/not/exist"), newFile));
	}
}
