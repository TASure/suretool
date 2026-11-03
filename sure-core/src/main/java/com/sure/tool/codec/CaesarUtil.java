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

/**
 * 凯撒密码编解码工具。
 *
 * <p>原理：英文字母按指定位移量轮转，非英文字母字符原样保留，大小写分别处理。
 * 经典位移：ROT-13（shift 13）、ROT-3（shift 3，军事用途）。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String encoded = CaesarUtil.encode("Hello, World!", 3);   // "Khoor, Zruog!"
 * String plain   = CaesarUtil.decode("Khoor, Zruog!", 3);  // "Hello, World!"
 * String rot13   = CaesarUtil.encode("suretool", 13);      // "fhergbby"
 * }</pre>
 *
 * <p>null 语义：入参为 {@code null} 时返回 {@code null}（空安全）。</p>
 *
 * @author suretool
 * @since 1.19.1
 */
public final class CaesarUtil {

	/** 字母表长度 */
	private static final int ALPHABET_SIZE = 26;

	private CaesarUtil() {
	}

	/**
	 * 凯撒密码编码。
	 *
	 * <p>位移量可正可负，内部统一按 26 取模归一，保证 {@code encode(encode(s, n), -n) == s}。</p>
	 *
	 * @param text  原文，可为 {@code null}
	 * @param shift 位移量（按 26 取模）
	 * @return 密文；{@code null} 返回 {@code null}
	 */
	public static String encode(String text, int shift) {
		if (text == null) {
			return null;
		}
		int normalized = ((shift % ALPHABET_SIZE) + ALPHABET_SIZE) % ALPHABET_SIZE;
		if (normalized == 0) {
			return text;
		}
		char[] out = new char[text.length()];
		for (int i = 0; i < out.length; i++) {
			char c = text.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				out[i] = (char) ((c - 'A' + normalized) % ALPHABET_SIZE + 'A');
			} else if (c >= 'a' && c <= 'z') {
				out[i] = (char) ((c - 'a' + normalized) % ALPHABET_SIZE + 'a');
			} else {
				out[i] = c;
			}
		}
		return new String(out);
	}

	/**
	 * 凯撒密码解码（位移量取反）。
	 *
	 * @param text  密文，可为 {@code null}
	 * @param shift 编码时使用的位移量
	 * @return 原文；{@code null} 返回 {@code null}
	 */
	public static String decode(String text, int shift) {
		return encode(text, -shift);
	}
}
