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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.brotli.dec.BrotliInputStream;

/**
 * Brotli 解码工具门面。
 *
 * <p>基于官方 org.brotli:dec（纯 Java、Apache-2.0）提供 Brotli 流解码与格式检测。
 * 注意：Brotli 官方 Java 只发布解码器（编码器仅 C 实现，brotli4j 等需 native 库，
 * 与 suretool 零原生依赖原则冲突），故本工具不提供编码能力。
 * 典型场景：HTTP {@code Content-Encoding: br} 响应体解码、.br 资源解包。</p>
 *
 * @since 1.4.0
 */
public final class BrotliUtil {

	private BrotliUtil() {
	}

	/**
	 * 解压 Brotli 字节数组。
	 *
	 * @param data Brotli 压缩字节
	 * @return 原始字节
	 * @throws IllegalArgumentException data 为 null
	 * @throws CompressRuntimeException  数据非法或损坏
	 */
	public static byte[] decompress(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		if (data.length == 0) {
			return new byte[0];
		}
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try (BrotliInputStream in = new BrotliInputStream(new ByteArrayInputStream(data))) {
				in.transferTo(bos);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			throw new CompressRuntimeException("Brotli 解压失败", e);
		}
	}

	/**
	 * 解压 Brotli 文件为原始文件。
	 *
	 * @param src  源 Brotli 文件
	 * @param dest 目标文件
	 * @throws CompressRuntimeException 解压失败
	 * @throws IllegalArgumentException  参数不合法
	 */
	public static void decompress(Path src, Path dest) {
		if (src == null || dest == null) {
			throw new IllegalArgumentException("src and dest must not be null");
		}
		if (!Files.isRegularFile(src)) {
			throw new IllegalArgumentException("src must be an existing file: " + src);
		}
		try (InputStream in = new BrotliInputStream(Files.newInputStream(src));
				OutputStream out = Files.newOutputStream(dest)) {
			in.transferTo(out);
		} catch (IOException e) {
			throw new CompressRuntimeException("Brotli 文件解压失败: " + src + " -> " + dest, e);
		}
	}

	/**
	 * 判断字节数组是否为 Brotli 流（魔数检测）。
	 *
	 * <p>Brotli 首字节编码：{@code (WBITS - 12) << 3 | 0b011}，
	 * WBITS ∈ [10, 24]，即高 5 位加 12 须落在 [10,24]，低 3 位须为 {@code 0b011}。
	 * 该规则可排除 gzip（0x1F）、zip（0x50）、PNG（0x89）等常见格式。</p>
	 *
	 * @param data 待检测字节
	 * @return true 表示疑似 Brotli 流
	 * @throws IllegalArgumentException data 为 null
	 */
	public static boolean isBrotli(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("data must not be null");
		}
		if (data.length < 2) {
			return false;
		}
		int b = data[0] & 0xFF;
		int wbits = (b >> 3) + 12;
		return wbits >= 10 && wbits <= 24 && (b & 0x07) == 0x03;
	}

	/**
	 * 判断文件是否为 Brotli 流（读首字节魔数检测）。
	 *
	 * @param file 待检测文件
	 * @return true 表示疑似 Brotli 流
	 * @throws CompressRuntimeException 读取失败
	 */
	public static boolean isBrotli(Path file) {
		if (file == null || !Files.isRegularFile(file)) {
			return false;
		}
		try (InputStream in = Files.newInputStream(file)) {
			byte[] head = in.readNBytes(2);
			return isBrotli(head);
		} catch (IOException e) {
			throw new CompressRuntimeException("读取文件失败: " + file, e);
		}
	}
}
