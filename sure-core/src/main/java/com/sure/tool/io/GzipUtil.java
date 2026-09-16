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

import com.sure.tool.util.CharsetUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP 压缩工具类：字符串/字节数组/文件压缩与解压，基于 JDK 内置 {@code GZIP} 实现，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class GzipUtil {

	private GzipUtil() {
	}

	/**
	 * 压缩字节数组。
	 *
	 * @param data 原始字节
	 * @return GZIP 压缩后字节
	 * @throws IOException IO 异常
	 */
	public static byte[] gzip(byte[] data) throws IOException {
		if (data == null || data.length == 0) {
			return data;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 2);
		try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
			gzip.write(data);
		}
		return out.toByteArray();
	}

	/**
	 * 解压 GZIP 字节数组。
	 *
	 * @param data GZIP 压缩字节
	 * @return 原始字节
	 * @throws IOException IO 异常
	 */
	public static byte[] ungzip(byte[] data) throws IOException {
		if (data == null || data.length == 0) {
			return data;
		}
		try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
				ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 2)) {
			IoUtil.copy(gzip, out);
			return out.toByteArray();
		}
	}

	/**
	 * 压缩字符串为 GZIP 字节（UTF-8）。
	 *
	 * @param content 字符串
	 * @return GZIP 字节
	 * @throws IOException IO 异常
	 */
	public static byte[] gzip(String content) throws IOException {
		return gzip(content, CharsetUtil.UTF_8);
	}

	/**
	 * 压缩字符串为 GZIP 字节（指定字符集）。
	 *
	 * @param content 字符串
	 * @param charset 字符集
	 * @return GZIP 字节
	 * @throws IOException IO 异常
	 */
	public static byte[] gzip(String content, Charset charset) throws IOException {
		if (content == null) {
			return null;
		}
		return gzip(content.getBytes(charset));
	}

	/**
	 * 解压 GZIP 字节为字符串（UTF-8）。
	 *
	 * @param data GZIP 字节
	 * @return 原始字符串
	 * @throws IOException IO 异常
	 */
	public static String ungzipString(byte[] data) throws IOException {
		return ungzipString(data, CharsetUtil.UTF_8);
	}

	/**
	 * 解压 GZIP 字节为字符串（指定字符集）。
	 *
	 * @param data    GZIP 字节
	 * @param charset 字符集
	 * @return 原始字符串
	 * @throws IOException IO 异常
	 */
	public static String ungzipString(byte[] data, Charset charset) throws IOException {
		if (data == null) {
			return null;
		}
		return new String(ungzip(data), charset);
	}

	/**
	 * 压缩文件为 GZIP 文件。
	 *
	 * @param src  源文件
	 * @param dest 目标 .gz 文件
	 * @throws IOException IO 异常
	 */
	public static void gzipFile(Path src, Path dest) throws IOException {
		try (InputStream in = Files.newInputStream(src); OutputStream out = new GZIPOutputStream(Files.newOutputStream(dest))) {
			IoUtil.copy(in, out);
		}
	}

	/**
	 * 解压 GZIP 文件为普通文件。
	 *
	 * @param src  .gz 源文件
	 * @param dest 目标文件
	 * @throws IOException IO 异常
	 */
	public static void ungzipFile(Path src, Path dest) throws IOException {
		try (InputStream in = new GZIPInputStream(Files.newInputStream(src)); OutputStream out = Files.newOutputStream(dest)) {
			IoUtil.copy(in, out);
		}
	}
}
