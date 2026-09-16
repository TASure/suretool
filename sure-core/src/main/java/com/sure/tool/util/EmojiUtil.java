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
package com.sure.tool.util;

/**
 * Emoji 工具：与 {@code \\uXXXX} 转义互转，支持代理对（4 字节 emoji），零依赖。
 *
 * <p>用于 emoji 存储/传输前的统一转义，避免数据库字符集问题。</p>
 *
 * @author suretool
 * @since 0.2.0
 */
public class EmojiUtil {

	private EmojiUtil() {
	}

	/**
	 * emoji 转 \\uXXXX 转义形式（普通字符保持不变）。
	 *
	 * @param text 含 emoji 的文本
	 * @return 转义文本；null 返回 null
	 */
	public static String toUnicode(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(text.length() * 2);
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isSurrogate(c)) {
				if (i + 1 < text.length() && Character.isSurrogatePair(c, text.charAt(i + 1))) {
					sb.append("\\u").append(hex(c, 4))
							.append("\\u").append(hex(text.charAt(i + 1), 4));
					i++;
				} else {
					sb.append("\\u").append(hex(c, 4));
				}
			} else if (c > 0x7E || c < 0x20) {
				sb.append("\\u").append(hex(c, 4));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * \\uXXXX 转义还原为 emoji/字符。
	 *
	 * @param text 转义文本
	 * @return 还原文本；null 返回 null
	 */
	public static String fromUnicode(String text) {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\\' && i + 5 < text.length() + 1 && i + 1 < text.length() && text.charAt(i + 1) == 'u') {
				int end = Math.min(i + 6, text.length());
				if (end - (i + 2) == 4 && isHex(text, i + 2, end)) {
					sb.append((char) Integer.parseInt(text.substring(i + 2, end), 16));
					i += 5;
					continue;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static String hex(int v, int len) {
		String h = Integer.toHexString(v);
		return "0".repeat(len - h.length()) + h;
	}

	private static boolean isHex(String s, int from, int to) {
		for (int i = from; i < to; i++) {
			if (Character.digit(s.charAt(i), 16) < 0) {
				return false;
			}
		}
		return true;
	}
}
