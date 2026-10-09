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
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FileUtil 覆盖率补测：null/不存在守卫、目录递归大小、可读大小、规范化路径分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class FileUtilGapTest {

	private Path tempDir;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("fileutil-gap");
	}

	@After
	public void tearDown() throws Exception {
		FileUtil.delete(tempDir.toFile());
	}

	@Test
	public void testNullGuards() throws Exception {
		Assert.assertNull(FileUtil.touch((File) null));
		Assert.assertNull(FileUtil.getParent((File) null));
		Assert.assertTrue(FileUtil.listFileNames("/no/such/dir/xyz").isEmpty());
		Assert.assertNull(FileUtil.copy(null, new File("x")));
		Assert.assertNull(FileUtil.copy(new File("/no/such/src"), new File("x")));
		Assert.assertNull(FileUtil.move(null, new File("x")));
		Assert.assertNull(FileUtil.copyDir(null, new File("x")));
		Assert.assertNull(FileUtil.copyDir(new File("/no/such/src"), new File("x")));
		FileUtil.clean(new File("/no/such/dir"));
	}

	@Test
	public void testSizeAndReadable() throws Exception {
		File sub = new File(tempDir.toFile(), "sub");
		Assert.assertTrue(sub.mkdir());
		File f = new File(sub, "a.txt");
		Assert.assertTrue(f.createNewFile());
		Assert.assertTrue(FileUtil.size(tempDir.toFile()) >= 0L);
		Assert.assertEquals("0 B", FileUtil.readableFileSize(-1L));
		Assert.assertEquals("512 B", FileUtil.readableFileSize(512L));
		Assert.assertEquals("1 KB", FileUtil.readableFileSize(1024L));
		Assert.assertTrue(FileUtil.readableFileSize(Long.MAX_VALUE).endsWith("TB"));
	}

	@Test
	public void testNormalize() {
		Assert.assertNull(FileUtil.normalize(null));
		Assert.assertEquals("", FileUtil.normalize(""));
		Assert.assertEquals("/", FileUtil.normalize("/"));
		Assert.assertEquals("../a", FileUtil.normalize("../a/b/.."));
		Assert.assertEquals("a/b", FileUtil.normalize("a/./b"));
	}

	@Test
	public void testWalkAndDelete() throws Exception {
		Path sub = Files.createDirectory(tempDir.resolve("s"));
		Files.createFile(sub.resolve("x.txt"));
		Assert.assertEquals(1, FileUtil.walkFiles(sub).size());
		Assert.assertEquals(0, FileUtil.walkFiles(tempDir.resolve("no-such")).size());
		Assert.assertTrue(FileUtil.delete(sub.toFile()));
	}
}
