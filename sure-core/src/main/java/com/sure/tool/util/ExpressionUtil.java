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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 算术表达式求值工具（零运行期依赖）。
 *
 * <p>支持文法：{@code + - * / %}、括号、一元负号、整数/小数/科学计数法、变量替换。
 * 实现为 LL(1) 递归下降 + {@link BigDecimal} 高精度运算；不支持函数、幂、逻辑比较与字符串。
 *
 * <p>示例：{@code eval("(1 + 2) * 3 - 4 / 2", map)}
 *
 * @author suretool
 * @since 1.9.0
 */
public class ExpressionUtil {

	private static final MathContext DIVIDE_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

	private ExpressionUtil() {
	}

	/**
	 * 求值无变量算术表达式，返回 double。
	 *
	 * @param expression 表达式
	 * @return 计算结果
	 * @throws IllegalArgumentException 表达式非法或除零
	 */
	public static double eval(String expression) {
		return evalNumber(expression, null).doubleValue();
	}

	/**
	 * 求值算术表达式（支持变量替换）。
	 *
	 * @param expression 表达式
	 * @param variables  变量表（值为 Number 或可解析为数字的字符串）
	 * @return 计算结果
	 * @throws IllegalArgumentException 表达式非法、变量缺失或除零
	 */
	public static double eval(String expression, Map<String, Object> variables) {
		return evalNumber(expression, variables).doubleValue();
	}

	/**
	 * 求值表达式，返回高精度 {@link BigDecimal}。
	 *
	 * @param expression 表达式
	 * @param variables  变量表，可为 {@code null}
	 * @return 计算结果
	 * @throws IllegalArgumentException 表达式非法、变量缺失或除零
	 */
	public static BigDecimal evalNumber(String expression, Map<String, Object> variables) {
		if (expression == null || expression.isBlank()) {
			throw new IllegalArgumentException("表达式不能为空");
		}
		Parser parser = new Parser(expression, variables);
		BigDecimal result = parser.parse();
		parser.skipSpaces();
		if (!parser.atEnd()) {
			throw parser.error("存在无法解析的尾部内容");
		}
		return result;
	}

	/**
	 * 校验表达式是否合法（不含变量求值）。
	 *
	 * @param expression 表达式
	 * @return 合法返回 {@code true}；为 null/空白/文法错误/除零返回 {@code false}
	 */
	public static boolean check(String expression) {
		try {
			evalNumber(expression, null);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/** 递归下降解析器。 */
	private static final class Parser {

		private final String source;
		private final Map<String, Object> variables;
		private int pos;

		Parser(String source, Map<String, Object> variables) {
			this.source = source;
			this.variables = variables;
		}

		BigDecimal parse() {
			skipSpaces();
			return expr();
		}

		private BigDecimal expr() {
			BigDecimal value = term();
			while (true) {
				skipSpaces();
				if (match('+')) {
					value = value.add(term());
				} else if (match('-')) {
					value = value.subtract(term());
				} else {
					return value;
				}
			}
		}

		private BigDecimal term() {
			BigDecimal value = factor();
			while (true) {
				skipSpaces();
				if (match('*')) {
					value = value.multiply(factor());
				} else if (match('/')) {
					BigDecimal divisor = factor();
					if (divisor.compareTo(BigDecimal.ZERO) == 0) {
						throw error("除数为 0");
					}
					value = value.divide(divisor, DIVIDE_CONTEXT);
				} else if (match('%')) {
					BigDecimal divisor = factor();
					if (divisor.compareTo(BigDecimal.ZERO) == 0) {
						throw error("取模除数为 0");
					}
					value = value.remainder(divisor);
				} else {
					return value;
				}
			}
		}

		private BigDecimal factor() {
			skipSpaces();
			if (match('-')) {
				return factor().negate();
			}
			if (match('+')) {
				return factor();
			}
			if (match('(')) {
				BigDecimal value = expr();
				skipSpaces();
				if (!match(')')) {
					throw error("缺少右括号 ')'");
				}
				return value;
			}
			if (pos < source.length()) {
				char c = source.charAt(pos);
				if (Character.isDigit(c) || c == '.') {
					return parseNumber();
				}
				if (Character.isLetter(c) || c == '_') {
					return parseVariable();
				}
			}
			throw error("期望数字、变量或 '('，实际位置 " + pos);
		}

		private BigDecimal parseNumber() {
			int start = pos;
			boolean hasDot = false;
			boolean hasExp = false;
			while (pos < source.length()) {
				char c = source.charAt(pos);
				if (Character.isDigit(c)) {
					pos++;
				} else if (c == '.' && !hasDot && !hasExp) {
					hasDot = true;
					pos++;
				} else if ((c == 'e' || c == 'E') && !hasExp) {
					hasExp = true;
					pos++;
					if (pos < source.length() && (source.charAt(pos) == '+' || source.charAt(pos) == '-')) {
						pos++;
					}
				} else {
					break;
				}
			}
			if (pos == start) {
				throw error("数字解析失败");
			}
			try {
				return new BigDecimal(source.substring(start, pos));
			} catch (NumberFormatException e) {
				throw error("非法数字: " + source.substring(start, pos));
			}
		}

		private BigDecimal parseVariable() {
			int start = pos;
			while (pos < source.length()) {
				char c = source.charAt(pos);
				if (Character.isLetterOrDigit(c) || c == '_') {
					pos++;
				} else {
					break;
				}
			}
			String name = source.substring(start, pos);
			if (variables == null || !variables.containsKey(name)) {
				throw error("变量缺失: " + name);
			}
			Object value = variables.get(name);
			if (value instanceof BigDecimal bigDecimal) {
				return bigDecimal;
			}
			if (value instanceof Number number) {
				return BigDecimal.valueOf(number.doubleValue());
			}
			try {
				return new BigDecimal(String.valueOf(value).trim());
			} catch (NumberFormatException e) {
				throw error("变量不是数字: " + name);
			}
		}

		void skipSpaces() {
			while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
				pos++;
			}
		}

		boolean match(char expected) {
			if (pos < source.length() && source.charAt(pos) == expected) {
				pos++;
				return true;
			}
			return false;
		}

		boolean atEnd() {
			return pos >= source.length();
		}

		IllegalArgumentException error(String message) {
			return new IllegalArgumentException(message + "（位置 " + pos + "）: " + source);
		}
	}
}
