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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSONPath 查询求值器（零第三方依赖），支持常用语法子集：
 *
 * <ul>
 * <li>根：{@code $}</li>
 * <li>对象属性：{@code $.a.b}、{@code $['a']['b']}</li>
 * <li>数组索引：{@code $.list[0]}</li>
 * <li>通配：{@code $.list[*]}、{@code $.*}</li>
 * <li>递归下降：{@code $..price}</li>
 * <li>过滤器：{@code $.store.book[?(@.price < 10)]}，支持比较
 * {@code == != < > <= >=}、逻辑 {@code && || !} 与存在性判断</li>
 * </ul>
 *
 * @author suretool
 * @since 1.1.0
 */
public final class JsonPath {

	private JsonPath() {
	}

	/**
	 * 按 JSONPath 查询，返回所有匹配值（无匹配时为空列表）。
	 *
	 * @param root 根对象（JSONObject / JSONArray / Map / List / 普通值）
	 * @param path JSONPath 表达式
	 * @return 匹配值列表
	 */
	public static List<Object> select(Object root, String path) {
		List<Segment> segments = compile(path);
		List<Object> results = new ArrayList<>();
		walk(root, segments, 0, results);
		return results;
	}

	/**
	 * 按 JSONPath 查询并取第一个匹配值，无匹配返回 null。
	 *
	 * @param root 根对象
	 * @param path JSONPath 表达式
	 * @return 首个匹配值，或 null
	 */
	public static Object eval(Object root, String path) {
		List<Object> results = select(root, path);
		return results.isEmpty() ? null : results.get(0);
	}

	// ================= 路径编译 =================

	private static List<Segment> compile(String path) {
		if (path == null || path.trim().isEmpty()) {
			throw new JSONException("JSONPath 不能为空");
		}
		List<Segment> segments = new ArrayList<>();
		int i = 0;
		if (path.charAt(0) == '$') {
			i = 1;
		}
		while (i < path.length()) {
			char c = path.charAt(i);
			if (c == '.') {
				if (i + 1 < path.length() && path.charAt(i + 1) == '.') {
					// 递归下降 ..name 或 ..[*]
					i += 2;
					if (i < path.length() && path.charAt(i) == '[') {
						int end = path.indexOf(']', i);
						String inner = path.substring(i + 1, end).trim();
						if ("*".equals(inner)) {
							segments.add(Segment.recursiveWildcard());
						} else {
							throw new JSONException("不支持的递归段: " + inner);
						}
						i = end + 1;
					} else if (i < path.length() && path.charAt(i) == '*') {
						segments.add(Segment.recursiveWildcard());
						i++;
					} else {
						int end = readNameEnd(path, i);
						segments.add(Segment.recursiveKey(path.substring(i, end)));
						i = end;
					}
					continue;
				}
				i++;
				if (i < path.length() && path.charAt(i) == '*') {
					segments.add(Segment.wildcard());
					i++;
				} else {
					int end = readNameEnd(path, i);
					String name = path.substring(i, end);
					// 聚合函数：.length() / .size() → 对当前值求长度
					if (("length".equals(name) || "size".equals(name)) && end < path.length()
							&& path.charAt(end) == '(' && path.indexOf(')', end) == end + 1) {
						segments.add(Segment.length());
						i = end + 2;
					} else {
						segments.add(Segment.key(name));
						i = end;
					}
				}
			} else if (c == '[') {
				int end = path.indexOf(']', i);
				if (end < 0) {
					throw new JSONException("JSONPath 缺少 ]: " + path);
				}
				String inner = path.substring(i + 1, end).trim();
				if ("*".equals(inner)) {
					segments.add(Segment.wildcard());
				} else if (inner.startsWith("?(")) {
					String filterExpr = inner.substring(2, inner.length() - 1);
					segments.add(Segment.filter(FilterParser.parse(filterExpr)));
				} else if ((inner.startsWith("'") && inner.endsWith("'"))
						|| (inner.startsWith("\"") && inner.endsWith("\""))) {
					segments.add(Segment.key(inner.substring(1, inner.length() - 1)));
				} else if (inner.chars().allMatch(Character::isDigit)) {
					segments.add(Segment.index(Integer.parseInt(inner)));
				} else {
					throw new JSONException("不支持的路径段: " + inner);
				}
				i = end + 1;
			} else {
				int end = readNameEnd(path, i);
				segments.add(Segment.key(path.substring(i, end)));
				i = end;
			}
		}
		return segments;
	}

	private static int readNameEnd(String path, int from) {
		int i = from;
		while (i < path.length()) {
			char c = path.charAt(i);
			if (c == '.' || c == '[' || c == '(') {
				break;
			}
			i++;
		}
		return i;
	}

	// ================= 求值 =================

	private static void walk(Object current, List<Segment> segments, int idx, List<Object> results) {
		if (idx >= segments.size()) {
			if (current != null) {
				results.add(current);
			}
			return;
		}
		Segment seg = segments.get(idx);
		if (seg.type == SegmentType.KEY) {
			if (current instanceof Map<?, ?> map && map.containsKey(seg.key)) {
				walk(map.get(seg.key), segments, idx + 1, results);
			}
			return;
		}
		if (seg.type == SegmentType.INDEX) {
			if (current instanceof List<?> list && seg.index >= 0 && seg.index < list.size()) {
				walk(list.get(seg.index), segments, idx + 1, results);
			}
			return;
		}
		if (seg.type == SegmentType.WILDCARD) {
			if (current instanceof Map<?, ?> map) {
				for (Object v : map.values()) {
					walk(v, segments, idx + 1, results);
				}
			} else if (current instanceof List<?> list) {
				for (Object v : list) {
					walk(v, segments, idx + 1, results);
				}
			}
			return;
		}
		if (seg.type == SegmentType.RECURSIVE_KEY || seg.type == SegmentType.RECURSIVE_WILDCARD) {
			// 递归下降：沿当前节点向下深度遍历
			collectDeep(current, segments, idx, results);
			return;
		}
		if (seg.type == SegmentType.FILTER) {
			if (current instanceof List<?> list) {
				for (Object v : list) {
					if (seg.filter.root.eval(v)) {
						walk(v, segments, idx + 1, results);
					}
				}
			}
			return;
		}
		if (seg.type == SegmentType.LENGTH) {
			if (current instanceof java.util.Collection<?> collection) {
				results.add(collection.size());
			} else if (current instanceof Map<?, ?> map) {
				results.add(map.size());
			} else if (current instanceof String str) {
				results.add(str.length());
			} else if (current != null) {
				results.add(0);
			}
			return;
		}
	}

	private static void collectDeep(Object current, List<Segment> segments, int idx, List<Object> results) {
		Segment seg = segments.get(idx);
		if (seg.type == SegmentType.RECURSIVE_KEY) {
			if (current instanceof Map<?, ?> map) {
				if (map.containsKey(seg.key)) {
					walk(map.get(seg.key), segments, idx + 1, results);
				}
				for (Object v : map.values()) {
					collectDeep(v, segments, idx, results);
				}
			} else if (current instanceof List<?> list) {
				for (Object v : list) {
					collectDeep(v, segments, idx, results);
				}
			}
		} else { // RECURSIVE_WILDCARD
			if (current instanceof Map<?, ?> map) {
				for (Object v : map.values()) {
					walk(v, segments, idx + 1, results);
					collectDeep(v, segments, idx, results);
				}
			} else if (current instanceof List<?> list) {
				for (Object v : list) {
					walk(v, segments, idx + 1, results);
					collectDeep(v, segments, idx, results);
				}
			}
		}
	}

	// ================= 内部结构 =================

	private enum SegmentType {
		KEY, INDEX, WILDCARD, RECURSIVE_KEY, RECURSIVE_WILDCARD, FILTER, LENGTH
	}

	private static final class Segment {
		final SegmentType type;
		final String key;
		final int index;
		final FilterExpr filter;

		private Segment(SegmentType type, String key, int index, FilterExpr filter) {
			this.type = type;
			this.key = key;
			this.index = index;
			this.filter = filter;
		}

		static Segment key(String key) {
			return new Segment(SegmentType.KEY, key, -1, null);
		}

		static Segment index(int index) {
			return new Segment(SegmentType.INDEX, null, index, null);
		}

		static Segment wildcard() {
			return new Segment(SegmentType.WILDCARD, null, -1, null);
		}

		static Segment recursiveKey(String key) {
			return new Segment(SegmentType.RECURSIVE_KEY, key, -1, null);
		}

		static Segment recursiveWildcard() {
			return new Segment(SegmentType.RECURSIVE_WILDCARD, null, -1, null);
		}

		static Segment filter(FilterExpr expr) {
			return new Segment(SegmentType.FILTER, null, -1, expr);
		}

		static Segment length() {
			return new Segment(SegmentType.LENGTH, null, -1, null);
		}
	}

	// ================= 过滤器表达式 =================

	/** 过滤器表达式（编译后不可变）。 */
	public static final class FilterExpr {
		final FilterNode root;

		FilterExpr(FilterNode root) {
			this.root = root;
		}
	}

	private interface FilterNode {
		boolean eval(Object current);
	}

	private static final class OrNode implements FilterNode {
		private final FilterNode left;
		private final FilterNode right;

		OrNode(FilterNode left, FilterNode right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean eval(Object current) {
			return left.eval(current) || right.eval(current);
		}
	}

	private static final class AndNode implements FilterNode {
		private final FilterNode left;
		private final FilterNode right;

		AndNode(FilterNode left, FilterNode right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean eval(Object current) {
			return left.eval(current) && right.eval(current);
		}
	}

	private static final class NotNode implements FilterNode {
		private final FilterNode child;

		NotNode(FilterNode child) {
			this.child = child;
		}

		@Override
		public boolean eval(Object current) {
			return !child.eval(current);
		}
	}

	private static final class ExistsNode implements FilterNode {
		private final String path;

		ExistsNode(String path) {
			this.path = path;
		}

		@Override
		public boolean eval(Object current) {
			Object value = readPath(current, path);
			return value != null;
		}
	}

	private static final class CompareNode implements FilterNode {
		private final String op;
		private final String path;
		private final Object literal;

		CompareNode(String op, String path, Object literal) {
			this.op = op;
			this.path = path;
			this.literal = literal;
		}

		@Override
		public boolean eval(Object current) {
			Object actual = readPath(current, path);
			int cmp = compare(actual, literal);
			switch (op) {
			case "==":
			case "=":
				return cmp == 0;
			case "!=":
				return cmp != 0;
			case "<":
				return cmp < 0;
			case "<=":
				return cmp <= 0;
			case ">":
				return cmp > 0;
			case ">=":
				return cmp >= 0;
			default:
				return false;
			}
		}
	}

	private static Object readPath(Object current, String path) {
		Object node = current;
		int i = 0;
		while (i < path.length() && node != null) {
			char c = path.charAt(i);
			if (c == '.') {
				i++;
				int end = path.indexOf('.', i);
				if (end < 0) {
					end = path.length();
				}
				String key = path.substring(i, end);
				if (node instanceof Map<?, ?> map) {
					node = map.get(key);
				} else {
					node = null;
				}
				i = end;
			} else if (c == '[') {
				int end = path.indexOf(']', i);
				String inner = path.substring(i + 1, end);
				if (node instanceof Map<?, ?> map) {
					node = map.get(inner.substring(1, inner.length() - 1));
				} else if (node instanceof List<?> list) {
					int idx = Integer.parseInt(inner);
					node = idx >= 0 && idx < list.size() ? list.get(idx) : null;
				} else {
					node = null;
				}
				i = end + 1;
			} else {
				return null;
			}
		}
		return node;
	}

	private static int compare(Object a, Object b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return -1;
		}
		if (b == null) {
			return 1;
		}
		if (a instanceof Number na && b instanceof Number nb) {
			return new BigDecimal(na.toString()).compareTo(new BigDecimal(nb.toString()));
		}
		if (a instanceof Boolean ba && b instanceof Boolean bb) {
			return Boolean.compare(ba, bb);
		}
		return String.valueOf(a).compareTo(String.valueOf(b));
	}

	private static final class FilterParser {
		private final String expr;
		private int pos;

		private FilterParser(String expr) {
			this.expr = expr;
		}

		static FilterExpr parse(String expr) {
			return new FilterExpr(new FilterParser(expr).parseOr());
		}

		private FilterNode parseOr() {
			FilterNode left = parseAnd();
			while (match("||")) {
				left = new OrNode(left, parseAnd());
			}
			return left;
		}

		private FilterNode parseAnd() {
			FilterNode left = parseUnary();
			while (match("&&")) {
				left = new AndNode(left, parseUnary());
			}
			return left;
		}

		private FilterNode parseUnary() {
			if (match("!")) {
				if (match("(")) {
					FilterNode inner = parseOr();
					if (!match(")")) {
						throw new JSONException("过滤器缺少 ) : " + expr);
					}
					return new NotNode(inner);
				}
				return new NotNode(parseUnary());
			}
			if (match("(")) {
				FilterNode inner = parseOr();
				if (!match(")")) {
					throw new JSONException("过滤器缺少 ) : " + expr);
				}
				return inner;
			}
			return parseComparison();
		}

		private FilterNode parseComparison() {
			String path = parseOperand();
			if (path == null) {
				throw new JSONException("过滤器表达式非法: " + expr);
			}
			skipSpace();
			String op = null;
			for (String candidate : new String[] {"==", "!=", "<=", ">=", "<", ">", "="}) {
				if (match(candidate)) {
					op = candidate;
					break;
				}
			}
			if (op == null) {
				return new ExistsNode(path);
			}
			Object literal = parseLiteral();
			return new CompareNode(op, path, literal);
		}

		/** 解析操作数：@.path 或 @['path']；返回相对路径串。 */
		private String parseOperand() {
			if (!match("@")) {
				throw new JSONException("过滤器操作数必须以 @ 开头: " + expr);
			}
			StringBuilder sb = new StringBuilder();
			while (pos < expr.length()) {
				char c = expr.charAt(pos);
				if (c == '.' || c == '[') {
					if (c == '[') {
						int end = expr.indexOf(']', pos);
						if (end < 0) {
							throw new JSONException("过滤器缺少 ]: " + expr);
						}
						sb.append(expr, pos, end + 1);
						pos = end + 1;
					} else {
						int end = pos + 1;
						while (end < expr.length()) {
							char nc = expr.charAt(end);
							if (nc == '.' || nc == '[' || nc == ' ' || nc == '<' || nc == '>' || nc == '='
									|| nc == '!' || nc == '&' || nc == '|' || nc == ')' ) {
								break;
							}
							end++;
						}
						sb.append(expr, pos, end);
						pos = end;
					}
				} else {
					break;
				}
			}
			if (sb.length() == 0) {
				return "@";
			}
			return sb.toString();
		}

		private Object parseLiteral() {
			skipSpace();
			if (pos >= expr.length()) {
				throw new JSONException("过滤器缺少字面量: " + expr);
			}
			char c = expr.charAt(pos);
			if (c == '\'' || c == '"') {
				char quote = c;
				pos++;
				StringBuilder sb = new StringBuilder();
				while (pos < expr.length() && expr.charAt(pos) != quote) {
					sb.append(expr.charAt(pos));
					pos++;
				}
				if (pos >= expr.length()) {
					throw new JSONException("过滤器字符串未闭合: " + expr);
				}
				pos++;
				skipSpace();
				return sb.toString();
			}
			if (expr.startsWith("true", pos)) {
				pos += 4;
				skipSpace();
				return Boolean.TRUE;
			}
			if (expr.startsWith("false", pos)) {
				pos += 5;
				skipSpace();
				return Boolean.FALSE;
			}
			if (expr.startsWith("null", pos)) {
				pos += 4;
				skipSpace();
				return null;
			}
			int end = pos;
			while (end < expr.length() && (Character.isDigit(expr.charAt(end))
					|| expr.charAt(end) == '.' || expr.charAt(end) == '-' || expr.charAt(end) == '+'
					|| expr.charAt(end) == 'e' || expr.charAt(end) == 'E')) {
				end++;
			}
			if (end == pos) {
				throw new JSONException("过滤器字面量非法: " + expr);
			}
			String num = expr.substring(pos, end);
			pos = end;
			skipSpace();
			return new BigDecimal(num);
		}

		private boolean match(String token) {
			if (expr.startsWith(token, pos)) {
				pos += token.length();
				skipSpace();
				return true;
			}
			return false;
		}

		private void skipSpace() {
			while (pos < expr.length() && expr.charAt(pos) == ' ') {
				pos++;
			}
		}
	}

	/** 便捷：构造与 JSON 结构匹配的 Map（测试辅助用，非 JSONPath 核心）。 */
	static Map<String, Object> mapOf(String key, Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put(key, value);
		return map;
	}

	/** 便捷：字符串值比较（供测试使用）。 */
	static boolean eq(Object a, Object b) {
		return Objects.equals(a, b);
	}
}
