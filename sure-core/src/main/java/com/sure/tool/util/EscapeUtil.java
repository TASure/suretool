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
 * HTML 转义工具类，参考 Hutool 的 {@code HtmlUtil} 设计。
 * 转义 {@code & < > " '} 五个特殊字符，防止 HTML 注入与内容破坏。
 *
 * @author suretool
 * @since 0.1.0
 */
public class EscapeUtil {

	private EscapeUtil() {
	}

	/**
	 * 转义 HTML 特殊字符：{@code & < > " '}。
	 *
	 * @param html 原文
	 * @return 转义后的文本，输入为 {@code null} 时返回 {@code null}
	 */
	public static String escape(String html) {
		if (html == null || html.length() == 0) {
			return html;
		}
		StringBuilder sb = new StringBuilder(html.length() + 16);
		for (int i = 0; i < html.length(); i++) {
			char c = html.charAt(i);
			switch (c) {
				case '&':
					sb.append("&amp;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#39;");
					break;
				default:
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}

	/**
	 * 反转义 HTML 实体为原始字符。
	 *
	 * @param html 转义后的文本
	 * @return 原文，输入为 {@code null} 时返回 {@code null}
	 */
	public static String unescape(String html) {
		if (html == null || html.length() == 0) {
			return html;
		}
		return html.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'")
				.replace("&apos;", "'")
				.replace("&amp;", "&");
	}
}