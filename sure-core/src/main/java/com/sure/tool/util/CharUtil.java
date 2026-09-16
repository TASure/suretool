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
 * 字符工具类。
 *
 * <p>提供字符分类判断、空白判断等静态方法。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class CharUtil {

	private CharUtil() {
	}

	/**
	 * 是否为英文字母或 Unicode 字母。
	 *
	 * @param c 字符
	 * @return 是否为字母
	 */
	public static boolean isLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isLetter(c);
	}

	/**
	 * 是否为数字字符（0-9）。
	 *
	 * @param c 字符
	 * @return 是否为数字
	 */
	public static boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * 是否为字母或数字。
	 *
	 * @param c 字符
	 * @return 是否为字母或数字
	 */
	public static boolean isLetterOrNumber(char c) {
		return isLetter(c) || isNumber(c);
	}

	/**
	 * 是否为大写字母。
	 *
	 * @param c 字符
	 * @return 是否为大写字母
	 */
	public static boolean isUpperCase(char c) {
		return c >= 'A' && c <= 'Z';
	}

	/**
	 * 是否为小写字母。
	 *
	 * @param c 字符
	 * @return 是否为小写字母
	 */
	public static boolean isLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}

	/**
	 * 是否为空白字符（空格、制表符、换行、回车、BOM 等）。
	 *
	 * @param c 字符
	 * @return 是否为空白
	 */
	public static boolean isBlankChar(char c) {
		return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\ufeff' || c == '\u200b';
	}

	/**
	 * 是否为十六进制字符（0-9、a-f、A-F）。
	 *
	 * @param c 字符
	 * @return 是否为十六进制字符
	 */
	public static boolean isHexChar(char c) {
		return isNumber(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	/**
	 * 字符转数字值，支持 0-9、a-f、A-F。
	 *
	 * @param c 字符
	 * @return 数字值，非数字字符返回 -1
	 */
	public static int digitValue(char c) {
		if (isNumber(c)) {
			return c - '0';
		}
		if (c >= 'a' && c <= 'f') {
			return c - 'a' + 10;
		}
		if (c >= 'A' && c <= 'F') {
			return c - 'A' + 10;
		}
		return -1;
	}

	/**
	 * 字符是否相等。
	 *
	 * @param c1 字符 1
	 * @param c2 字符 2
	 * @return 是否相等
	 */
	public static boolean equals(char c1, char c2) {
		return c1 == c2;
	}

	/**
	 * 是否为文件路径分隔符（'/' 或 '\\'）。
	 *
	 * @param c 字符
	 * @return 是否分隔符
	 */
	public static boolean isFileSeparator(char c) {
		return c == '/' || c == '\\';
	}

	/**
	 * 转为大写（仅字母）。
	 *
	 * @param c 字符
	 * @return 大写字符
	 */
	public static char toUpper(char c) {
		if (isLowerCase(c)) {
			return (char) (c - 32);
		}
		return c;
	}

	/**
	 * 转为小写（仅字母）。
	 *
	 * @param c 字符
	 * @return 小写字符
	 */
	public static char toLower(char c) {
		if (isUpperCase(c)) {
			return (char) (c + 32);
		}
		return c;
	}

	/**
	 * 字符转字符串。
	 *
	 * @param c 字符
	 * @return 单字符字符串
	 */
	public static String toString(char c) {
		return String.valueOf(c);
	}

	/**
	 * 判断字符是否为 Emoji（BMP 内常见 Emoji 区段）。
	 *
	 * @param c 字符
	 * @return 是否为 Emoji 字符
	 */
	public static boolean isEmoji(char c) {
		return (c >= 0x2600 && c <= 0x27BF)
				|| (c >= 0xFE00 && c <= 0xFE0F)
				|| (c >= 0x2190 && c <= 0x21FF)
				|| (c >= 0x2B00 && c <= 0x2BFF);
	}

	/**
	 * 判断码点是否为 Emoji（支持补充平面 Emoji，如 U+1F600）。
	 *
	 * @param codePoint Unicode 码点
	 * @return 是否为 Emoji 码点
	 */
	public static boolean isEmoji(int codePoint) {
		return (codePoint >= 0x1F300 && codePoint <= 0x1FAFF)
				|| (codePoint >= 0x2600 && codePoint <= 0x27BF)
				|| (codePoint >= 0xFE00 && codePoint <= 0xFE0F)
				|| (codePoint >= 0x2190 && codePoint <= 0x21FF)
				|| (codePoint >= 0x2B00 && codePoint <= 0x2BFF);
	}

}