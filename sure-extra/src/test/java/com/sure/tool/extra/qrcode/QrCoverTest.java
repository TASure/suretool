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
package com.sure.tool.extra.qrcode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

/**
 * 二维码覆盖率二轮补强：默认配置/尺寸文件写出重载、非法入参与写出失败分支。
 */
public class QrCoverTest {

	/** 单参 generate 走默认配置。 */
	@Test
	public void generate_defaultConfig() {
		byte[] png = QrCodeUtil.generate("hello-qr");
		assertNotNull(png);
		assertTrue(png.length > 0);
	}

	/** 两参 generateFile 走默认配置并落盘。 */
	@Test
	public void generateFile_defaultConfig() throws Exception {
		File file = Files.createTempFile("qr-def-", ".png").toFile();
		try {
			assertTrue(QrCodeUtil.generateFile("data", file));
			assertTrue(file.length() > 0);
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/** 三参 generateFile 指定尺寸并落盘。 */
	@Test
	public void generateFile_withSize() throws Exception {
		File file = Files.createTempFile("qr-size-", ".png").toFile();
		try {
			assertTrue(QrCodeUtil.generateFile("data", 200, file));
			assertTrue(file.length() > 0);
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/** 空内容直接返回 false。 */
	@Test
	public void generateFile_emptyContent() throws Exception {
		File file = Files.createTempFile("qr-empty-", ".png").toFile();
		try {
			assertFalse(QrCodeUtil.generateFile("", file));
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/** 目标为目录时 FileOutputStream 失败，走 catch 返回 false。 */
	@Test
	public void generateFile_targetIsDirectory() throws Exception {
		File dir = Files.createTempDirectory("qr-dir-").toFile();
		try {
			assertFalse(QrCodeUtil.generateFile("data", 150, dir));
		} finally {
			Files.deleteIfExists(dir.toPath());
		}
	}
}
