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
 * Base32 编解码工具，参考 Apache Commons Codec 的 {@code Base32} 设计。
 *
 * <p>标准 Base32 字母表 {@code A-Z 2-7}，每 5 字节编码为 8 字符，默认带
 * {@code =} 填充；解码容忍缺失填充与空白字符。</p>
 *
 * <p>典型场景：TOTP 密钥、短链接、无歧义编码（无 0/O/1/I/l）。</p>
 *
 * @author suretool
 * @since 1.19.0
 */
public final class Base32Util {

	private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
	private static final int[] LOOKUP = new int[128];

	static {
		java.util.Arrays.fill(LOOKUP, -1);
		for (int i = 0; i < ALPHABET.length; i++) {
			LOOKUP[ALPHABET[i]] = i;
		}
	}

	private Base32Util() {
	}

	/**
	 * 编码为标准 Base32 字符串（带 {@code =} 填充）。
	 *
	 * <p>示例：{@code Base32Util.encode("hello".getBytes(UTF_8)) -> "NBSWY3DP"}</p>
	 *
	 * @param data 待编码字节，可为 {@code null}
	 * @return Base32 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(byte[] data) {
		return encode(data, true);
	}

	/**
	 * 编码为 Base32 字符串，可选择是否保留 {@code =} 填充。
	 *
	 * <p>示例：{@code Base32Util.encode(bytes, false)} 尾部无填充。</p>
	 *
	 * @param data    待编码字节，可为 {@code null}
	 * @param padding 是否保留 {@code =} 填充
	 * @return Base32 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(byte[] data, boolean padding) {
		if (data == null) {
			return null;
		}
		if (data.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder((data.length + 4) / 5 * 8);
		int i = 0;
		while (i + 5 <= data.length) {
			appendBlock(sb, data, i, 5);
			i += 5;
		}
		int remain = data.length - i;
		if (remain > 0) {
			appendBlock(sb, data, i, remain);
			int pad = (5 - remain) * 8 / 5;
			if (padding) {
				for (int p = 0; p < pad; p++) {
					sb.append('=');
				}
			}
		}
		return sb.toString();
	}

	private static void appendBlock(StringBuilder sb, byte[] data, int off, int len) {
		long bits = 0;
		for (int k = 0; k < 5; k++) {
			bits <<= 8;
			if (k < len) {
				bits |= (data[off + k] & 0xFF);
			}
		}
		// 从最高位开始按 5 位取索引（对齐 Base32 标准位序）
		int chars = (len * 8 + 4) / 5;
		for (int k = 0; k < chars; k++) {
			int idx = (int) ((bits >>> (40 - (k + 1) * 5)) & 0x1F);
			sb.append(ALPHABET[idx]);
		}
	}

	/**
	 * 字符串按指定字符集编码为 Base32。
	 *
	 * <p>示例：{@code Base32Util.encode("hello", StandardCharsets.UTF_8) -> "NBSWY3DP"}</p>
	 *
	 * @param text    字符串，可为 {@code null}
	 * @param charset 字符集
	 * @return Base32 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(String text, Charset charset) {
		if (text == null) {
			return null;
		}
		return encode(text.getBytes(charset == null ? StandardCharsets.UTF_8 : charset));
	}

	/**
	 * 解码 Base32 字符串为字节数组（容忍缺失填充与空白字符）。
	 *
	 * <p>示例：{@code Base32Util.decode("NBSWY3DP") -> "hello" 的 UTF-8 字节}</p>
	 *
	 * @param base32 Base32 字符串，可为 {@code null}
	 * @return 解码字节数组；{@code null} 返回 {@code null}
	 * @throws IllegalArgumentException 含非法字符或长度无效
	 */
	public static byte[] decode(String base32) {
		if (base32 == null) {
			return null;
		}
		String s = stripPadding(base32);
		int len = s.length();
		if (len == 0) {
			return new byte[0];
		}
		// 8 字符 = 5 字节；余 2/4/5/7 字符合法
		int fullGroups = len / 8;
		int remain = len % 8;
		if (remain == 1 || remain == 3 || remain == 6) {
			throw new IllegalArgumentException("Invalid Base32 length: " + len);
		}
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(len * 5 / 8 + 4);
		for (int i = 0; i < fullGroups; i++) {
			decodeBlock(out, s, i * 8, 8, 5);
		}
		if (remain > 0) {
			int bytes = remain * 5 / 8;
			decodeBlock(out, s, fullGroups * 8, remain, bytes);
		}
		return out.toByteArray();
	}

	private static void decodeBlock(java.io.ByteArrayOutputStream out, String s, int off, int chars, int bytes) {
		long bits = 0;
		for (int k = 0; k < 8; k++) {
			bits <<= 5;
			if (k < chars) {
				char c = s.charAt(off + k);
				if (c >= 128 || LOOKUP[c] < 0) {
					throw new IllegalArgumentException("Invalid Base32 character: '" + c + "'");
				}
				bits |= LOOKUP[c];
			}
		}
		// 数据位于 40 位窗口的最高 chars*5 位，从高位逐字节提取
		for (int k = 0; k < bytes; k++) {
			int shift = 40 - (k + 1) * 8;
			out.write((int) ((bits >>> shift) & 0xFF));
		}
	}

	private static String stripPadding(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '=') {
				break;
			}
			if (Character.isWhitespace(c)) {
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * 校验字符串是否为合法 Base32（仅字母 A-Z、数字 2-7 与尾部 {@code =} 填充）。
	 *
	 * <p>示例：{@code Base32Util.isBase32("NBSWY3DP") -> true}。</p>
	 *
	 * @param base32 字符串，可为 {@code null}
	 * @return 合法返回 {@code true}；{@code null} 或空串返回 {@code false}
	 */
	public static boolean isBase32(String base32) {
		if (base32 == null || base32.length() == 0) {
			return false;
		}
		boolean padSeen = false;
		for (int i = 0; i < base32.length(); i++) {
			char c = base32.charAt(i);
			if (c == '=') {
				padSeen = true;
				continue;
			}
			if (padSeen) {
				return false;
			}
			if (Character.isWhitespace(c)) {
				continue;
			}
			if (c >= 128 || LOOKUP[c] < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 解码 Base32 字符串为字符串（UTF-8）。
	 *
	 * <p>示例：{@code Base32Util.decodeToString("NBSWY3DP") -> "hello"}。</p>
	 *
	 * @param base32 Base32 字符串，可为 {@code null}
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base32) {
		return decodeToString(base32, StandardCharsets.UTF_8);
	}

	/**
	 * 解码 Base32 字符串为字符串（指定字符集）。
	 *
	 * @param base32  Base32 字符串，可为 {@code null}
	 * @param charset 字符集
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base32, Charset charset) {
		if (base32 == null) {
			return null;
		}
		return new String(decode(base32), charset == null ? StandardCharsets.UTF_8 : charset);
	}
}
