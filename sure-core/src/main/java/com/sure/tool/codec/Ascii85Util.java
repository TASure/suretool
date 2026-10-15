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
package com.sure.tool.codec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Ascii85（Base85）编解码工具，参考 Apache Commons Codec 的 {@code Ascii85} 设计。
 *
 * <p>4 字节编码为 5 个 ASCII 字符（范围 {@code !}~{@code u}，即 33–117），
 * 4 个连续零字节压缩为单个 {@code z}；解码容忍 {@code <~} / {@code ~>} 包裹符。</p>
 *
 * <p>典型场景：PostScript / Adobe 二进制嵌入、紧凑文本传输。</p>
 *
 * @author suretool
 * @since 1.19.0
 */
public final class Ascii85Util {

	private static final int BASE = 85;
	private static final char ZERO_BLOCK = 'z';
	private static final String WRAP_START = "<~";
	private static final String WRAP_END = "~>";

	private Ascii85Util() {
	}

	/**
	 * 编码为 Ascii85 字符串（不带 {@code <~} / {@code ~>} 包裹）。
	 *
	 * <p>示例：{@code Ascii85Util.encode("hello".getBytes(UTF_8))} 为紧凑 Ascii85。</p>
	 *
	 * @param data 待编码字节，可为 {@code null}
	 * @return Ascii85 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(byte[] data) {
		if (data == null) {
			return null;
		}
		if (data.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder((data.length + 3) / 4 * 5);
		int i = 0;
		while (i + 4 <= data.length) {
			encodeBlock(sb, data, i, 4);
			i += 4;
		}
		int remain = data.length - i;
		if (remain > 0) {
			encodeBlock(sb, data, i, remain);
		}
		return sb.toString();
	}

	private static void encodeBlock(StringBuilder sb, byte[] data, int off, int len) {
		if (len == 4) {
			// 四字节均为 0 时压缩为 'z'
			if (data[off] == 0 && data[off + 1] == 0 && data[off + 2] == 0 && data[off + 3] == 0) {
				sb.append(ZERO_BLOCK);
				return;
			}
		}
		long value = 0;
		for (int k = 0; k < 4; k++) {
			value = (value << 8) | (k < len ? (data[off + k] & 0xFF) : 0);
		}
		int chars; // 4 字节 → 5 字符；按剩余字节数截取
		if (len == 4) {
			chars = 5;
		} else if (len == 3) {
			chars = 4;
		} else if (len == 2) {
			chars = 3;
		} else {
			chars = 2;
		}
		char[] buf = new char[5];
		for (int k = 4; k >= 0; k--) {
			buf[k] = (char) ((value % BASE) + 33);
			value /= BASE;
		}
		sb.append(buf, 0, chars);
	}

	/**
	 * 字符串按指定字符集编码为 Ascii85。
	 *
	 * @param text    字符串，可为 {@code null}
	 * @param charset 字符集
	 * @return Ascii85 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(String text, Charset charset) {
		if (text == null) {
			return null;
		}
		return encode(text.getBytes(charset == null ? StandardCharsets.UTF_8 : charset));
	}

	/**
	 * 解码 Ascii85 字符串（容忍 {@code <~} / {@code ~>} 包裹与 {@code z} 零块压缩）。
	 *
	 * <p>示例：{@code Ascii85Util.decode("<~...~>")} 返回原始字节。</p>
	 *
	 * @param ascii85 Ascii85 字符串，可为 {@code null}
	 * @return 解码字节数组；{@code null} 返回 {@code null}
	 * @throws IllegalArgumentException 含非法字符或长度无效
	 */
	public static byte[] decode(String ascii85) {
		if (ascii85 == null) {
			return null;
		}
		String s = unwrap(ascii85);
		if (s.length() == 0) {
			return new byte[0];
		}
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(s.length() * 4 / 5 + 4);
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c == ZERO_BLOCK) {
				out.write(0);
				out.write(0);
				out.write(0);
				out.write(0);
				i++;
				continue;
			}
			int end = Math.min(i + 5, s.length());
			int count = end - i;
			long value = 0;
			for (int k = 0; k < 5; k++) {
				value *= BASE;
				if (k < count) {
					char ch = s.charAt(i + k);
					if (ch < 33 || ch > 117) {
						throw new IllegalArgumentException("Invalid Ascii85 character: '" + ch + "'");
					}
					value += ch - 33;
				} else {
					value += BASE - 1; // 尾部按最大填充
				}
			}
			int bytes = count == 5 ? 4 : count - 1;
			// 从高 8 位开始依次取出 bytes 个字节
			for (int k = 0; k < bytes; k++) {
				int shift = 24 - k * 8;
				out.write((int) ((value >>> shift) & 0xFF));
			}
			i = end;
		}
		return out.toByteArray();
	}

	private static String unwrap(String s) {
		String trimmed = s.trim();
		if (trimmed.startsWith(WRAP_START)) {
			trimmed = trimmed.substring(2);
		}
		if (trimmed.endsWith(WRAP_END)) {
			trimmed = trimmed.substring(0, trimmed.length() - 2);
		}
		return trimmed;
	}

	/**
	 * 校验字符串是否为合法 Ascii85（仅字符 33–117 与 {@code z}，可带 {@code <~} / {@code ~>} 包裹）。
	 *
	 * <p>示例：{@code Ascii85Util.isAscii85("<~87cURD_*#TDfTZ)+T~>") -> true}。</p>
	 *
	 * @param ascii85 字符串，可为 {@code null}
	 * @return 合法返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isAscii85(String ascii85) {
		if (ascii85 == null || ascii85.length() == 0) {
			return false;
		}
		String s = unwrap(ascii85);
		if (s.length() == 0) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == ZERO_BLOCK) {
				continue;
			}
			if (c < 33 || c > 117) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 解码 Ascii85 字符串为字符串（UTF-8）。
	 *
	 * <p>示例：{@code Ascii85Util.decodeToString(...)}。</p>
	 *
	 * @param ascii85 Ascii85 字符串，可为 {@code null}
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String ascii85) {
		return decodeToString(ascii85, StandardCharsets.UTF_8);
	}

	/**
	 * 解码 Ascii85 字符串为字符串（指定字符集）。
	 *
	 * @param ascii85 Ascii85 字符串，可为 {@code null}
	 * @param charset 字符集
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String ascii85, Charset charset) {
		if (ascii85 == null) {
			return null;
		}
		return new String(decode(ascii85), charset == null ? StandardCharsets.UTF_8 : charset);
	}
}
