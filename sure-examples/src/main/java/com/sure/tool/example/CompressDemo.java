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

		byte[] raw = "hello brotli".getBytes(StandardCharsets.UTF_8);
		byte[] dec = BrotliUtil.decompress(raw);
		System.out.println("brotli roundtrip = " + new String(dec, StandardCharsets.UTF_8));
	}
}
