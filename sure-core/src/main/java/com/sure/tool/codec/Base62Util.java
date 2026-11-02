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

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Base62 编解码工具，字母表为 {@code 0-9A-Za-z}。
 *
 * <p>Base62 常用于短链接、订单号、分享码等需要紧凑且 URL 安全的场景。
 * 编码保留原始字节的前导零字节（输出为等数量的 {@code 0}），解码时原样恢复。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String encoded = Base62Util.encode("hello".getBytes(StandardCharsets.UTF_8)); // "7tQLFHz"
 * String plain = Base62Util.decodeToString("7tQLFHz");                          // "hello"
 * boolean valid = Base62Util.isBase62("7tQLFHz");                               // true
 * }</pre>
 *
 * <p>null 语义：入参为 {@code null} 时返回 {@code null}（空安全）；
 * 解码遇到非 Base62 字母表字符抛 {@link IllegalArgumentException}。</p>
 *
 * @author suretool
 * @since 1.20.0
 */
public final class Base62Util {

	private static final char[] ALPHABET =
			"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	private static final int[] LOOKUP = new int[128];
	private static final BigInteger RADIX = BigInteger.valueOf(62);

	static {
		Arrays.fill(LOOKUP, -1);
		for (int i = 0; i < ALPHABET.length; i++) {
			LOOKUP[ALPHABET[i]] = i;
		}
	}

	private Base62Util() {
	}

	/**
	 * 编码字节数组为 Base62 字符串。
	 *
	 * <p>示例：{@code Base62Util.encode("hello".getBytes(UTF_8)) -> "7tQLFHz"}</p>
	 *
	 * @param data 待编码字节，可为 {@code null}
	 * @return Base62 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(byte[] data) {
		if (data == null) {
			return null;
		}
		if (data.length == 0) {
			return "";
		}
		int zeros = 0;
		while (zeros < data.length && data[zeros] == 0) {
			zeros++;
		}
		StringBuilder sb = new StringBuilder();
		BigInteger value = new BigInteger(1, data);
		while (value.signum() > 0) {
			BigInteger[] quotientAndRemainder = value.divideAndRemainder(RADIX);
			sb.append(ALPHABET[quotientAndRemainder[1].intValue()]);
			value = quotientAndRemainder[0];
		}
		for (int i = 0; i < zeros; i++) {
			sb.append(ALPHABET[0]);
		}
		return sb.reverse().toString();
	}

	/**
	 * 编码字符串为 Base62 字符串（默认 UTF-8）。
	 *
	 * @param text    待编码字符串，可为 {@code null}
	 * @param charset 字符集，{@code null} 时使用 UTF-8
	 * @return Base62 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(String text, Charset charset) {
		if (text == null) {
			return null;
		}
		return encode(text.getBytes(charset == null ? StandardCharsets.UTF_8 : charset));
	}

	/**
	 * 解码 Base62 字符串为字节数组。
	 *
	 * <p>示例：{@code Base62Util.decode("7tQLFHz") -> "hello" 的 UTF-8 字节}</p>
	 *
	 * @param base62 Base62 字符串，可为 {@code null}
	 * @return 解码字节数组；{@code null} 返回 {@code null}，空串返回空数组
	 * @throws IllegalArgumentException 含非 Base62 字符
	 */
	public static byte[] decode(String base62) {
		if (base62 == null) {
			return null;
		}
		if (base62.isEmpty()) {
			return new byte[0];
		}
		int zeros = 0;
		while (zeros < base62.length() && base62.charAt(zeros) == ALPHABET[0]) {
			zeros++;
		}
		BigInteger value = BigInteger.ZERO;
		for (int i = 0; i < base62.length(); i++) {
			char c = base62.charAt(i);
			int index = c < LOOKUP.length ? LOOKUP[c] : -1;
			if (index < 0) {
				throw new IllegalArgumentException("Illegal Base62 character: " + c);
			}
			value = value.multiply(RADIX).add(BigInteger.valueOf(index));
		}
		byte[] bytes = value.signum() == 0 ? new byte[0] : value.toByteArray();
		if (bytes.length > 0 && bytes[0] == 0) {
			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		}
		byte[] result = new byte[zeros + bytes.length];
		System.arraycopy(bytes, 0, result, zeros, bytes.length);
		return result;
	}

	/**
	 * 解码 Base62 字符串为 UTF-8 字符串。
	 *
	 * @param base62 Base62 字符串，可为 {@code null}
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base62) {
		return decodeToString(base62, StandardCharsets.UTF_8);
	}

	/**
	 * 解码 Base62 字符串为字符串。
	 *
	 * @param base62  Base62 字符串，可为 {@code null}
	 * @param charset 字符集，{@code null} 时使用 UTF-8
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base62, Charset charset) {
		if (base62 == null) {
			return null;
		}
		return new String(decode(base62), charset == null ? StandardCharsets.UTF_8 : charset);
	}

	/**
	 * 判断字符串是否为合法的 Base62 文本。
	 *
	 * @param text 待校验文本，可为 {@code null}
	 * @return 非空且所有字符均在 Base62 字母表内返回 {@code true}
	 */
	public static boolean isBase62(CharSequence text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c >= LOOKUP.length || LOOKUP[c] < 0) {
				return false;
			}
		}
		return true;
	}
}
