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

import java.util.regex.Pattern;

/**
 * HTML 工具类：转义/反转义、去标签、文本清洗，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class HtmlUtil {

	private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

	private HtmlUtil() {
	}

	/**
	 * HTML 转义（&amp; &lt; &gt; &quot; &#39;）。
	 *
	 * @param html 原文
	 * @return 转义后文本；null 返回 null
	 */
	public static String escape(String html) {
		if (html == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(html.length());
		for (int i = 0; i < html.length(); i++) {
			char c = html.charAt(i);
			switch (c) {
				case '&' -> sb.append("&amp;");
				case '<' -> sb.append("&lt;");
				case '>' -> sb.append("&gt;");
				case '"' -> sb.append("&quot;");
				case '\'' -> sb.append("&#39;");
				default -> sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * HTML 反转义（&amp; &lt; &gt; &quot; &#39; &nbsp; 等）。
	 *
	 * @param text 转义后文本
	 * @return 原文；null 返回 null
	 */
	public static String unescape(String text) {
		if (text == null) {
			return null;
		}
		String result = text
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#39;", "'")
				.replace("&nbsp;", " ")
				.replace("&amp;", "&");
		return result;
	}

	/**
	 * 去除 HTML 标签（保留文本内容）。
	 *
	 * @param html 原始 HTML
	 * @return 纯文本；null 返回 null
	 */
	public static String cleanHtmlTag(String html) {
		if (html == null) {
			return null;
		}
		return TAG_PATTERN.matcher(html).replaceAll("").trim();
	}

	/**
	 * 去除指定标签（保留其他标签）。
	 *
	 * @param html 原始 HTML
	 * @param tag  标签名（不含尖括号，如 "script"）
	 * @return 清洗后文本
	 */
	public static String removeTag(String html, String tag) {
		if (html == null || tag == null || tag.isEmpty()) {
			return html;
		}
		Pattern p = Pattern.compile("</?" + Pattern.quote(tag) + "\\s*[^>]*>", Pattern.CASE_INSENSITIVE);
		return p.matcher(html).replaceAll("");
	}
}
