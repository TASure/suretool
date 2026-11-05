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
package com.sure.tool.example;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sure.tool.compress.BrotliUtil;
import com.sure.tool.compress.SevenZUtil;

/**
 * 压缩工具示例（sure-compress）。
 */
public class CompressDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws Exception {
		System.out.println("=== CompressDemo ===");
		Path dir = Files.createTempDirectory("sure-demo");
		Path f = dir.resolve("hello.txt");
		Files.writeString(f, "hello suretool compress");
		Path out = Files.createTempFile("sure", ".7z");
		SevenZUtil.compressDir(dir, out);
		System.out.println("7z size = " + Files.size(out));

		// Brotli 官方 Java 只提供解码器：这里使用预置的合法 Brotli 流（原文：suretool brotli test 中文内容）
		byte[] compressed = {(byte) 0x1B, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x9C,
				(byte) 0x63, (byte) 0xA9, (byte) 0x9A, (byte) 0xB3, (byte) 0xF0, (byte) 0x70, (byte) 0x10,
				(byte) 0x84, (byte) 0x7F, (byte) 0x29, (byte) 0x08, (byte) 0x21, (byte) 0x0C, (byte) 0x15,
				(byte) 0xE7, (byte) 0x71, (byte) 0x78, (byte) 0xDA, (byte) 0xC8, (byte) 0x57, (byte) 0xB2,
				(byte) 0x62, (byte) 0x1E, (byte) 0x6D, (byte) 0xEC, (byte) 0x2F};
		byte[] dec = BrotliUtil.decompress(compressed);
		System.out.println("brotli roundtrip = " + new String(dec, StandardCharsets.UTF_8));
		System.out.println("isBrotli = " + BrotliUtil.isBrotli(compressed));
	}
}
