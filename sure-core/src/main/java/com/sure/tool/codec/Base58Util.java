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
 * Base58 编解码工具，使用 Bitcoin 字母表
 * {@code 123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz}。
 *
 * <p>与 Base64 相比，Base58 去除了易混淆的 {@code 0}/{@code O}/{@code I}/{@code l}
 * 以及 {@code +}/{@code /}，适合短链接、钱包地址、邀请码等需要人工抄写的场景。
 * 编码保留原始字节的前导零字节（输出为等数量的 {@code 1}），解码时原样恢复。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String encoded = Base58Util.encode("hello".getBytes(StandardCharsets.UTF_8)); // "Cn8eVZg"
 * String plain = Base58Util.decodeToString("Cn8eVZg");                          // "hello"
 * boolean valid = Base58Util.isBase58("Cn8eVZg");                               // true
 * }</pre>
 *
 * <p>null 语义：入参为 {@code null} 时返回 {@code null}（空安全）；
 * 解码遇到非 Base58 字母表字符抛 {@link IllegalArgumentException}。</p>
 *
 * @author suretool
 * @since 1.19.1
 */
public final class Base58Util {

	private static final char[] ALPHABET =
			"123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	private static final int[] LOOKUP = new int[128];
	private static final BigInteger RADIX = BigInteger.valueOf(58);

	static {
		Arrays.fill(LOOKUP, -1);
		for (int i = 0; i < ALPHABET.length; i++) {
			LOOKUP[ALPHABET[i]] = i;
		}
	}

	private Base58Util() {
	}

	/**
	 * 编码字节数组为 Base58 字符串。
	 *
	 * <p>示例：{@code Base58Util.encode("hello".getBytes(UTF_8)) -> "Cn8eVZg"}</p>
	 *
	 * @param data 待编码字节，可为 {@code null}
	 * @return Base58 字符串；{@code null} 返回 {@code null}
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
	 * 编码字符串为 Base58 字符串（默认 UTF-8）。
	 *
	 * @param text    待编码字符串，可为 {@code null}
	 * @param charset 字符集，{@code null} 时使用 UTF-8
	 * @return Base58 字符串；{@code null} 返回 {@code null}
	 */
	public static String encode(String text, Charset charset) {
		if (text == null) {
			return null;
		}
		return encode(text.getBytes(charset == null ? StandardCharsets.UTF_8 : charset));
	}

	/**
	 * 解码 Base58 字符串为字节数组。
	 *
	 * <p>示例：{@code Base58Util.decode("Cn8eVZg") -> "hello" 的 UTF-8 字节}</p>
	 *
	 * @param base58 Base58 字符串，可为 {@code null}
	 * @return 解码字节数组；{@code null} 返回 {@code null}，空串返回空数组
	 * @throws IllegalArgumentException 含非 Base58 字符
	 */
	public static byte[] decode(String base58) {
		if (base58 == null) {
			return null;
		}
		if (base58.isEmpty()) {
			return new byte[0];
		}
		int zeros = 0;
		while (zeros < base58.length() && base58.charAt(zeros) == ALPHABET[0]) {
			zeros++;
		}
		BigInteger value = BigInteger.ZERO;
		for (int i = 0; i < base58.length(); i++) {
			char c = base58.charAt(i);
			int index = c < LOOKUP.length ? LOOKUP[c] : -1;
			if (index < 0) {
				throw new IllegalArgumentException("Illegal Base58 character: " + c);
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
	 * 解码 Base58 字符串为 UTF-8 字符串。
	 *
	 * @param base58 Base58 字符串，可为 {@code null}
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base58) {
		return decodeToString(base58, StandardCharsets.UTF_8);
	}

	/**
	 * 解码 Base58 字符串为字符串。
	 *
	 * @param base58  Base58 字符串，可为 {@code null}
	 * @param charset 字符集，{@code null} 时使用 UTF-8
	 * @return 解码字符串；{@code null} 返回 {@code null}
	 */
	public static String decodeToString(String base58, Charset charset) {
		if (base58 == null) {
			return null;
		}
		return new String(decode(base58), charset == null ? StandardCharsets.UTF_8 : charset);
	}

	/**
	 * 判断字符串是否为合法的 Base58 文本。
	 *
	 * @param text 待校验文本，可为 {@code null}
	 * @return 非空且所有字符均在 Bitcoin Base58 字母表内返回 {@code true}
	 */
	public static boolean isBase58(CharSequence text) {
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
