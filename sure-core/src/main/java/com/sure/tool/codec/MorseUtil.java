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

import java.util.HashMap;
import java.util.Map;

/**
 * 摩斯电码编解码工具。
 *
 * <p>编码规则：点（{@code .}）代表短脉冲、划（{@code -}）代表长脉冲；
 * 字母间以单空格分隔、单词间以三空格分隔（或传入的自定义分隔符）。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String encoded = MorseUtil.encode("SOS");            // "... --- ..."
 * String plain   = MorseUtil.decode("... --- ...");     // "SOS"
 * String text    = MorseUtil.decode(MorseUtil.encode("Hello World"));
 * }</pre>
 *
 * <p>null 语义：入参为 {@code null} 时返回 {@code null}（空安全）；
 * 编码遇到字典不含字符时原样输出并在前后保留分隔。</p>
 *
 * @author suretool
 * @since 1.21.0
 */
public final class MorseUtil {

	private static final char DOT = '.';
	private static final char DASH = '-';
	private static final Map<Character, String> CHAR_TO_CODE = new HashMap<>(64);
	private static final Map<String, Character> CODE_TO_CHAR = new HashMap<>(64);

	static {
		char[] chars = {
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
				'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'.', ',', '?', '\'', '!', '/', '(', ')', '&', ':', ';', '=', '+', '-', '_', '"', '$', '@'
		};
		String[] codes = {
				".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--",
				"-.", "---", ".--.", "--.-", "-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..",
				"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.",
				".-.-.-", "--..--", "..--..", ".----.", "-.-.--", "-..-.", "-.--.", "-.--.-", ".-...",
				"---...", "-.-.-.", "-...-", ".-.-.", "-....-", "..--.-", ".-..-.", "...-..-", ".--.-."
		};
		for (int i = 0; i < chars.length; i++) {
			CHAR_TO_CODE.put(chars[i], codes[i]);
			CODE_TO_CHAR.put(codes[i], chars[i]);
			CHAR_TO_CODE.put(Character.toLowerCase(chars[i]), codes[i]);
		}
	}

	private MorseUtil() {
	}

	/**
	 * 原文编码为摩斯电码。
	 *
	 * @param text 原文，可为 {@code null}
	 * @return 摩斯电码（字母间单空格、单词间三空格）；{@code null} 返回 {@code null}
	 */
	public static String encode(String text) {
		if (text == null) {
			return null;
		}
		String[] words = text.trim().split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (int w = 0; w < words.length; w++) {
			if (w > 0) {
				sb.append("   "); // 单词间隔
			}
			String word = words[w];
			for (int i = 0; i < word.length(); i++) {
				char c = word.charAt(i);
				if (i > 0) {
					sb.append(' ');
				}
				String code = CHAR_TO_CODE.get(c);
				sb.append(code != null ? code : c);
			}
		}
		return sb.toString();
	}

	/**
	 * 摩斯电码解码为原文。
	 *
	 * <p>支持点划 {@code ./-} 或 {@code ●/—} 等 Unicode 变体统一归一；
	 * 单词间三空格或 {@code /} 均视作单词分隔。</p>
	 *
	 * @param morse 摩斯电码，可为 {@code null}
	 * @return 原文；{@code null} 返回 {@code null}
	 */
	public static String decode(String morse) {
		if (morse == null) {
			return null;
		}
		String normalized = morse
				.replace('●', DOT)
				.replace('○', DOT)
				.replace('—', DASH)
				.replace('─', DASH);
		String[] words = normalized.trim().split("\\s{3,}|/");
		StringBuilder sb = new StringBuilder();
		for (int w = 0; w < words.length; w++) {
			if (w > 0) {
				sb.append(' ');
			}
			String[] letters = words[w].trim().split("\\s+");
			for (String letter : letters) {
				Character ch = CODE_TO_CHAR.get(letter);
				sb.append(ch != null ? ch : letter);
			}
		}
		return sb.toString();
	}
}
