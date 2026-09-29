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
package com.sure.tool.compress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@link BrotliUtil} 单元测试。
 *
 * <p>测试夹具为 Node.js zlib（标准 Brotli 编码器，quality=5）生成的固定字节，
 * 原文：{@code suretool brotli test 中文内容}。</p>
 */
public class BrotliUtilTest {

	/** 预置的合法 Brotli 流（Node zlib brotliCompressSync，quality=5）。 */
	private static final byte[] FIXTURE = {(byte) 0x1B, (byte) 0x20, (byte) 0x00, (byte) 0x00,
			(byte) 0x04, (byte) 0x9C, (byte) 0x63, (byte) 0xA9, (byte) 0x9A, (byte) 0xB3,
			(byte) 0xF0, (byte) 0x70, (byte) 0x10, (byte) 0x84, (byte) 0x7F, (byte) 0x29,
			(byte) 0x08, (byte) 0x21, (byte) 0x0C, (byte) 0x15, (byte) 0xE7, (byte) 0x71,
			(byte) 0x78, (byte) 0xDA, (byte) 0xC8, (byte) 0x57, (byte) 0xB2, (byte) 0x62,
			(byte) 0x1E, (byte) 0x6D, (byte) 0xEC, (byte) 0x2F};

	private static final byte[] PLAIN = "suretool brotli test \u4e2d\u6587\u5185\u5bb9".getBytes(StandardCharsets.UTF_8);

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void decompressFixtureRecoversPlain() {
		assertArrayEquals(PLAIN, BrotliUtil.decompress(FIXTURE));
	}

	@Test
	public void emptyArrayRoundTrip() {
		assertArrayEquals(new byte[0], BrotliUtil.decompress(new byte[0]));
	}

	@Test
	public void isBrotliRecognizesFixture() {
		assertTrue(BrotliUtil.isBrotli(FIXTURE));
	}

	@Test
	public void isBrotliRejectsCommonFormats() {
		assertFalse("普通文本", BrotliUtil.isBrotli("hello".getBytes(StandardCharsets.UTF_8)));
		assertFalse("gzip 魔数", BrotliUtil.isBrotli(new byte[]{(byte) 0x1F, (byte) 0x8B}));
		assertFalse("zip 魔数", BrotliUtil.isBrotli(new byte[]{0x50, 0x4B}));
		assertFalse("PNG 魔数", BrotliUtil.isBrotli(new byte[]{(byte) 0x89, 0x50}));
		assertFalse("过短", BrotliUtil.isBrotli(new byte[]{0x1B}));
		assertFalse("不存在文件", BrotliUtil.isBrotli(tmp.getRoot().toPath().resolve("no.br")));
	}

	@Test
	public void corruptDataThrows() {
		byte[] bad = FIXTURE.clone();
		bad[bad.length - 1] ^= 0xFF;
		assertThrows(CompressRuntimeException.class, () -> BrotliUtil.decompress(bad));
	}

	@Test
	public void fileRoundTrip() throws Exception {
		Path br = tmp.getRoot().toPath().resolve("src.txt.br");
		Path back = tmp.getRoot().toPath().resolve("back.txt");
		Files.write(br, FIXTURE);
		BrotliUtil.decompress(br, back);
		assertArrayEquals(PLAIN, Files.readAllBytes(back));
		assertTrue(BrotliUtil.isBrotli(br));
		assertFalse(BrotliUtil.isBrotli(back));
	}

	@Test
	public void invalidInput() {
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.decompress((byte[]) null));
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.isBrotli((byte[]) null));
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.decompress(tmp.getRoot().toPath().resolve("no.br"), tmp.getRoot().toPath().resolve("x")));
		assertThrows(IllegalArgumentException.class, () -> BrotliUtil.decompress(null, tmp.getRoot().toPath().resolve("x")));
	}
}
