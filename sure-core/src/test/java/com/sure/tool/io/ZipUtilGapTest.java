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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * ZipUtil 覆盖率补测：空源列表与 null 守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ZipUtilGapTest {

	@Test
	public void testEmptyList() {
		java.io.File zip = new java.io.File("out.zip");
		try {
			ZipUtil.zip(Collections.emptyList(), zip);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		} catch (Exception e) {
			Assert.fail("不应抛 " + e);
		}
	}

	@Test
	public void testMkdirsFails() throws Exception {
		java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("ziptest");
		java.io.File aFile = new java.io.File(tmp.toFile(), "afile");
		Assert.assertTrue(aFile.createNewFile());
		// parent 是普通文件 -> mkdirs 失败
		java.io.File badZip = new java.io.File(aFile, "out.zip");
		try {
			ZipUtil.zip(tmp.toFile(), badZip);
			Assert.fail("应抛异常");
		} catch (java.io.IOException e) {
			// expected
		}
		// unzip outDir 父路径是普通文件
		java.io.File badOut = new java.io.File(aFile, "outdir");
		try {
			ZipUtil.unzip(aFile, badOut);
			Assert.fail("应抛异常");
		} catch (java.io.IOException e) {
			// expected
		}
	}
}
