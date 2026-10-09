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
package com.sure.tool.extra.image;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

/**
 * 图像覆盖率二轮补强：写出参数校验、不支持格式与空入参返回 null 分支。
 */
public class ImgCoverTest {

	private static BufferedImage sample() {
		return new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
	}

	/** 写出时 image 或 file 为 null 抛非法参数。 */
	@Test
	public void write_nullArgs() throws Exception {
		File file = Files.createTempFile("img-", ".png").toFile();
		try {
			ImgUtil.write(null, file, "png");
			fail("应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// 预期
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/** 不支持的图像格式抛 IOException。 */
	@Test
	public void write_unsupportedFormat() throws Exception {
		File file = Files.createTempFile("img-", ".png").toFile();
		try {
			ImgUtil.write(sample(), file, "definitely-unknown-format");
			fail("应抛出 IOException");
		} catch (IOException expected) {
			assertTrue(expected.getMessage().contains("不支持的图像格式"));
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/** 空图旋转返回 null。 */
	@Test
	public void rotate_nullSource() {
		assertNull(ImgUtil.rotate(null, 90));
	}

	/** 空图灰度返回 null。 */
	@Test
	public void gray_nullSource() {
		assertNull(ImgUtil.gray(null));
	}

	/** 空图圆角返回 null。 */
	@Test
	public void round_nullSource() {
		assertNull(ImgUtil.round(null, 8));
	}

	/** 读取抛异常的输入流时走 catch 返回 null。 */
	@Test
	public void read_throwingStream() {
		java.io.InputStream bad = new java.io.InputStream() {
			@Override
			public int read() throws java.io.IOException {
				throw new java.io.IOException("boom");
			}
		};
		assertNull(ImgUtil.read(bad));
	}

	/** 文件含合法签名但数据截断，解码时抛 IOException，走 read(File) catch。 */
	@Test
	public void read_truncatedFile() throws Exception {
		// PNG 签名 8 字节 + 截断数据
		byte[] pngHead = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00 };
		File file = Files.createTempFile("img-trunc-", ".png").toFile();
		try {
			Files.write(file.toPath(), pngHead);
			// 解码可能抛 IOException（被 read 捕获返回 null）或返回 null，二者皆可
			ImgUtil.read(file);
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}
}
