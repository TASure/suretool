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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.sure.tool.bean.BeanUtil;
import com.sure.tool.date.DateUtil;

/**
 * JSON 解析与序列化工具类，零第三方依赖的自研实现（RFC 8259 子集），参考 Hutool 的 {@code JSONUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JSONUtil {

	private JSONUtil() {
	}

	/**
	 * 序列化任意对象为 JSON 字符串（支持 Bean、Map、集合、数组、日期等）。
	 *
	 * @param obj 对象
	 * @return JSON 字符串
	 */
	public static String toJsonStr(Object obj) {
		StringBuilder sb = new StringBuilder(64);
		writeValue(obj, sb);
		return sb.toString();
	}

	/**
	 * 解析 JSON 字符串为 Java 对象（对象为 {@link JSONObject}，数组为 {@link JSONArray}，其余为 String/Number/Boolean/null）。
	 *
	 * @param json JSON 字符串
	 * @return 解析结果
	 */
	public static Object parse(String json) {
		return new JsonParser(json).parse();
	}

	/**
	 * 解析 JSON 对象。
	 *
	 * @param json JSON 字符串
	 * @return JSON 对象
	 */
	public static JSONObject parseObj(String json) {
		Object value = parse(json);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		throw new JSONException("JSON 不是对象: " + preview(json));
	}

	/**
	 * 解析 JSON 数组。
	 *
	 * @param json JSON 字符串
	 * @return JSON 数组
	 */
	public static JSONArray parseArray(String json) {
		Object value = parse(json);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		throw new JSONException("JSON 不是数组: " + preview(json));
	}

	/**
	 * JSON 字符串转 Bean。
	 *
	 * @param json      JSON 字符串
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 实例
	 */
	public static <T> T toBean(String json, Class<T> beanClass) {
		return toBean(parseObj(json), beanClass);
	}

	/**
	 * JSON 对象转 Bean。
	 *
	 * @param jsonObject JSON 对象
	 * @param beanClass  Bean 类
	 * @param <T>        Bean 类型
	 * @return Bean 实例
	 */
	public static <T> T toBean(JSONObject jsonObject, Class<T> beanClass) {
		return BeanUtil.mapToBean(jsonObject, beanClass);
	}

	/**
	 * 是否为 JSON 对象或数组（以 {@code {} 或 [] 开头）。
	 *
	 * @param json 字符串
	 * @return 是否 JSON
	 */
	public static boolean isJson(String json) {
		if (json == null) {
			return false;
		}
		String trimmed = json.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		char c = trimmed.charAt(0);
		return c == '{' || c == '[';
	}

	private static String preview(String json) {
		String trimmed = json == null ? "" : json.trim();
		return trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed;
	}

	/**
	 * 写入值。
	 *
	 * @param value 值
	 * @param sb    输出
	 */
	private static void writeValue(Object value, StringBuilder sb) {
		if (value == null) {
			sb.append("null");
		} else if (value instanceof CharSequence || value instanceof Character) {
			writeString(value.toString(), sb);
		} else if (value instanceof Number) {
			sb.append(value.toString());
		} else if (value instanceof Boolean) {
			sb.append(value);
		} else if (value instanceof Map) {
			writeObject((Map<?, ?>) value, sb);
		} else if (value instanceof Collection) {
			writeArray((Collection<?>) value, sb);
		} else if (value.getClass().isArray()) {
			writeArray(Array.getLength(value), new ArrayIterator(value), sb);
		} else if (value instanceof Iterable) {
			writeArray(iteratorToList(((Iterable<?>) value).iterator()), sb);
		} else if (value instanceof Date) {
			writeString(DateUtil.format((Date) value), sb);
		} else if (BeanUtil.isBean(value.getClass())) {
			writeObject(BeanUtil.beanToMap(value, false), sb);
		} else {
			writeString(value.toString(), sb);
		}
	}

	/**
	 * 写入 JSON 对象。
	 *
	 * @param map Map
	 * @param sb  输出
	 */
	private static void writeObject(Map<?, ?> map, StringBuilder sb) {
		sb.append('{');
		boolean first = true;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			writeString(String.valueOf(entry.getKey()), sb);
			sb.append(':');
			writeValue(entry.getValue(), sb);
		}
		sb.append('}');
	}

	/**
	 * 写入 JSON 数组。
	 *
	 * @param collection 集合
	 * @param sb         输出
	 */
	private static void writeArray(Collection<?> collection, StringBuilder sb) {
		sb.append('[');
		boolean first = true;
		for (Object item : collection) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			writeValue(item, sb);
		}
		sb.append(']');
	}

	/**
	 * 写入 JSON 数组（按长度与迭代器）。
	 *
	 * @param length 长度
	 * @param it     迭代器
	 * @param sb     输出
	 */
	private static void writeArray(int length, Iterator<Object> it, StringBuilder sb) {
		sb.append('[');
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			writeValue(it.next(), sb);
		}
		sb.append(']');
	}

	private static void writeString(String value, StringBuilder sb) {
		sb.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				default:
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		sb.append('"');
	}

	private static java.util.List<Object> iteratorToList(Iterator<?> it) {
		java.util.List<Object> list = new java.util.ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	/**
	 * 数组迭代器（支持基本类型数组）。
	 */
	private static class ArrayIterator implements Iterator<Object> {

		private final Object array;
		private int index;

		ArrayIterator(Object array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < Array.getLength(array);
		}

		@Override
		public Object next() {
			if (!hasNext()) {
				throw new java.util.NoSuchElementException();
			}
			return Array.get(array, index++);
		}
	}

	/**
	 * 递归下降 JSON 解析器。
	 */
	private static class JsonParser {

		private final String json;
		private int pos;

		JsonParser(String json) {
			if (json == null) {
				throw new JSONException("JSON 字符串不能为 null");
			}
			this.json = json;
		}

		Object parse() {
			skipWhitespace();
			Object value = parseValue();
			skipWhitespace();
			if (pos < json.length()) {
				throw error("存在多余内容");
			}
			return value;
		}

		private Object parseValue() {
			if (pos >= json.length()) {
				throw error("内容为空");
			}
			char c = json.charAt(pos);
			switch (c) {
				case '{':
					return parseObject();
				case '[':
					return parseArray();
				case '"':
					return parseString();
				case 't':
					expect("true");
					return Boolean.TRUE;
				case 'f':
					expect("false");
					return Boolean.FALSE;
				case 'n':
					expect("null");
					return null;
				default:
					return parseNumber();
			}
		}

		private JSONObject parseObject() {
			JSONObject object = new JSONObject();
			pos++;
			skipWhitespace();
			if (peek() == '}') {
				pos++;
				return object;
			}
			while (true) {
				skipWhitespace();
				if (peek() != '"') {
					throw error("对象键必须是字符串");
				}
				String key = parseString();
				skipWhitespace();
				if (peek() != ':') {
					throw error("缺少冒号");
				}
				pos++;
				skipWhitespace();
				Object value = parseValue();
				object.put(key, value);
				skipWhitespace();
				char c = peek();
				if (c == ',') {
					pos++;
				} else if (c == '}') {
					pos++;
					return object;
				} else {
					throw error("对象缺少逗号或右花括号");
				}
			}
		}

		private JSONArray parseArray() {
			JSONArray array = new JSONArray();
			pos++;
			skipWhitespace();
			if (peek() == ']') {
				pos++;
				return array;
			}
			while (true) {
				skipWhitespace();
				array.add(parseValue());
				skipWhitespace();
				char c = peek();
				if (c == ',') {
					pos++;
				} else if (c == ']') {
					pos++;
					return array;
				} else {
					throw error("数组缺少逗号或右方括号");
				}
			}
		}

		private String parseString() {
			if (peek() != '"') {
				throw error("缺少双引号");
			}
			pos++;
			StringBuilder sb = new StringBuilder();
			while (true) {
				if (pos >= json.length()) {
					throw error("字符串未闭合");
				}
				char c = json.charAt(pos);
				if (c == '"') {
					pos++;
					return sb.toString();
				}
				if (c == '\\') {
					pos++;
					if (pos >= json.length()) {
						throw error("转义符不完整");
					}
					char escape = json.charAt(pos);
					switch (escape) {
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
							sb.append(parseUnicode());
							break;
						default:
							throw error("非法转义符 \\" + escape);
					}
					pos++;
				} else {
					sb.append(c);
					pos++;
				}
			}
		}

		private char parseUnicode() {
			if (pos + 4 >= json.length()) {
				throw error("\\u 转义不完整");
			}
			int code = 0;
			for (int i = 1; i <= 4; i++) {
				char c = json.charAt(pos + i);
				int digit = Character.digit(c, 16);
				if (digit < 0) {
					throw error("\\u 转义包含非十六进制字符");
				}
				code = (code << 4) | digit;
			}
			pos += 4;
			return (char) code;
		}

		private Number parseNumber() {
			int start = pos;
			while (pos < json.length() && "+-0123456789.eE".indexOf(json.charAt(pos)) >= 0) {
				pos++;
			}
			String number = json.substring(start, pos);
			if (number.isEmpty() || "-".equals(number) || ".".equals(number)) {
				throw error("非法数字");
			}
			try {
				if (number.indexOf('.') >= 0 || number.indexOf('e') >= 0 || number.indexOf('E') >= 0) {
					return new BigDecimal(number);
				}
				try {
					return Long.valueOf(number);
				} catch (NumberFormatException e) {
					return new BigInteger(number);
				}
			} catch (NumberFormatException e) {
				throw error("非法数字: " + number);
			}
		}

		private void expect(String literal) {
			if (!json.startsWith(literal, pos)) {
				throw error("非法字符");
			}
			pos += literal.length();
		}

		private char peek() {
			if (pos >= json.length()) {
				return '\0';
			}
			return json.charAt(pos);
		}

		private void skipWhitespace() {
			while (pos < json.length()) {
				char c = json.charAt(pos);
				if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
					pos++;
				} else {
					return;
				}
			}
		}

		private JSONException error(String message) {
			return new JSONException("JSON 解析失败（位置 " + pos + "）: " + message + "，附近内容: " + preview(json));
		}
	}
}
