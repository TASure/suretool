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
package com.sure.tool.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 流式解析器（零第三方依赖）。以事件驱动方式逐 token 解析，适用于大文件与
 * 内存受限场景，支持 RFC 8259 的字符串转义、Unicode 转义、数字、true/false/null。
 *
 * <p>解析失败抛 {@link JSONException}，IO 异常原样抛出。
 *
 * @author suretool
 * @since 1.1.0
 */
public final class StreamJsonParser {

	private static final int MAX_DEPTH = 256;

	private StreamJsonParser() {
	}

	/**
	 * 从输入流解析 JSON（UTF-8）。
	 *
	 * @param in      输入流
	 * @param handler 事件回调
	 * @throws IOException    IO 错误
	 * @throws JSONException JSON 语法错误或超深嵌套
	 */
	public static void parse(InputStream in, JsonHandler handler) throws IOException {
		parse(new InputStreamReader(in, StandardCharsets.UTF_8), handler);
	}

	/**
	 * 从 Reader 解析 JSON。
	 *
	 * @param reader  Reader
	 * @param handler 事件回调
	 * @throws IOException    IO 错误
	 * @throws JSONException JSON 语法错误或超深嵌套
	 */
	public static void parse(Reader reader, JsonHandler handler) throws IOException {
		PushbackReader in = new PushbackReader(reader, 1);
		skipWhitespace(in);
		int first = in.read();
		if (first < 0) {
			throw new JSONException("空输入");
		}
		in.unread(first);
		parseValue(in, handler, 0);
		skipWhitespace(in);
		if (in.read() >= 0) {
			throw new JSONException("JSON 末尾存在多余内容");
		}
	}

	/**
	 * 便捷方法：解析 JSON 字符串，收集所有顶层值（内部使用）。
	 *
	 * @param json JSON 文本
	 * @return 顶层值列表
	 */
	public static List<Object> parseToList(String json) {
		List<Object> values = new ArrayList<>();
		try {
			parse(new java.io.StringReader(json), new JsonHandler() {
				@Override
				public void onString(String value) {
					values.add(value);
				}

				@Override
				public void onNumber(String raw) {
					values.add(raw);
				}

				@Override
				public void onBoolean(boolean value) {
					values.add(value);
				}

				@Override
				public void onNull() {
					values.add(null);
				}
			});
		} catch (IOException e) {
			throw new JSONException("流式解析失败", e);
		}
		return values;
	}

	private static void parseValue(PushbackReader in, JsonHandler handler, int depth) throws IOException {
		if (depth > MAX_DEPTH) {
			throw new JSONException("JSON 嵌套深度超过 " + MAX_DEPTH);
		}
		int c = peekNonWhitespace(in);
		switch (c) {
		case '{':
			parseObject(in, handler, depth);
			break;
		case '[':
			parseArray(in, handler, depth);
			break;
		case '"':
			handler.onString(readString(in));
			break;
		case 't':
			readLiteral(in, "true");
			handler.onBoolean(true);
			break;
		case 'f':
			readLiteral(in, "false");
			handler.onBoolean(false);
			break;
		case 'n':
			readLiteral(in, "null");
			handler.onNull();
			break;
		default:
			if (c == '-' || (c >= '0' && c <= '9')) {
				// c 已通过 peek 留在流中，readNumber 直接读取
				handler.onNumber(readNumber(in));
			} else {
				throw new JSONException("无法识别的 JSON 值，字符: " + (char) c);
			}
		}
	}

	private static void parseObject(PushbackReader in, JsonHandler handler, int depth) throws IOException {
		expect(in, '{');
		handler.onStartObject();
		skipWhitespace(in);
		int first = in.read();
		if (first == '}') {
			handler.onEndObject();
			return;
		}
		in.unread(first);
		while (true) {
			skipWhitespace(in);
			String key = readString(in);
			handler.onKey(key);
			skipWhitespace(in);
			expect(in, ':');
			parseValue(in, handler, depth + 1);
			skipWhitespace(in);
			int c = in.read();
			if (c == ',') {
				continue;
			}
			if (c == '}') {
				break;
			}
			throw new JSONException("对象缺少 , 或 }");
		}
		handler.onEndObject();
	}

	private static void parseArray(PushbackReader in, JsonHandler handler, int depth) throws IOException {
		expect(in, '[');
		handler.onStartArray();
		skipWhitespace(in);
		int first = in.read();
		if (first == ']') {
			handler.onEndArray();
			return;
		}
		in.unread(first);
		while (true) {
			parseValue(in, handler, depth + 1);
			skipWhitespace(in);
			int c = in.read();
			if (c == ',') {
				continue;
			}
			if (c == ']') {
				break;
			}
			throw new JSONException("数组缺少 , 或 ]");
		}
		handler.onEndArray();
	}

	private static String readString(PushbackReader in) throws IOException {
		expect(in, '"');
		StringBuilder sb = new StringBuilder();
		while (true) {
			int c = in.read();
			if (c < 0) {
				throw new JSONException("字符串未闭合");
			}
			if (c == '"') {
				return sb.toString();
			}
			if (c != '\\') {
				sb.append((char) c);
				continue;
			}
			int esc = in.read();
			switch (esc) {
			case '"':
				sb.append('"');
				break;
			case '\\':
				sb.append('\\');
				break;
			case '/':
				sb.append('/');
				break;
			case 'b':
				sb.append('\b');
				break;
			case 'f':
				sb.append('\f');
				break;
			case 'n':
				sb.append('\n');
				break;
			case 'r':
				sb.append('\r');
				break;
			case 't':
				sb.append('\t');
				break;
			case 'u':
				sb.append((char) readHex4(in));
				break;
			default:
				throw new JSONException("非法转义字符: \\" + (char) esc);
			}
		}
	}

	private static int readHex4(PushbackReader in) throws IOException {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int c = in.read();
			int digit;
			if (c >= '0' && c <= '9') {
				digit = c - '0';
			} else if (c >= 'a' && c <= 'f') {
				digit = c - 'a' + 10;
			} else if (c >= 'A' && c <= 'F') {
				digit = c - 'A' + 10;
			} else {
				throw new JSONException("非法 Unicode 转义: \\u" + (char) c);
			}
			value = value * 16 + digit;
		}
		return value;
	}

	private static String readNumber(PushbackReader in) throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean intPart = false;
		while (true) {
			int c = in.read();
			if (c >= '0' && c <= '9') {
				sb.append((char) c);
				intPart = true;
			} else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
				sb.append((char) c);
			} else {
				if (c >= 0) {
					in.unread(c);
				}
				break;
			}
		}
		if (!intPart) {
			throw new JSONException("非法数字");
		}
		return sb.toString();
	}

	private static void readLiteral(PushbackReader in, String literal) throws IOException {
		for (int i = 0; i < literal.length(); i++) {
			int c = in.read();
			if (c != literal.charAt(i)) {
				throw new JSONException("非法字面量，期望: " + literal);
			}
		}
	}

	private static void expect(PushbackReader in, char expected) throws IOException {
		int c = in.read();
		if (c != expected) {
			throw new JSONException("期望字符 " + expected + "，实际: " + (char) c);
		}
	}

	private static void skipWhitespace(PushbackReader in) throws IOException {
		while (true) {
			int c = in.read();
			if (c < 0) {
				return;
			}
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
				in.unread(c);
				return;
			}
		}
	}

	private static int peekNonWhitespace(PushbackReader in) throws IOException {
		skipWhitespace(in);
		int c = in.read();
		if (c >= 0) {
			in.unread(c);
		}
		return c;
	}
}
