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
import java.math.RoundingMode;

/**
 * 金额工具类：人民币金额转中文大写（财务报销/合同常用），零依赖。
 * <p>
 * 支持负数、0~万亿级整数部分、角分处理；负数输出「负」前缀，无角分时输出「整」。
 *
 * @author suretool
 * @since 0.2.0
 */
public class MoneyUtil {

	private static final String[] CN_DIGITS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
	private static final String[] CN_UNITS = {"", "拾", "佰", "仟"};
	private static final String[] CN_BIG = {"", "万", "亿", "万亿"};

	private MoneyUtil() {
	}

	/**
	 * 金额转中文大写。
	 *
	 * @param amount 金额（元，支持小数，四舍五入到分）
	 * @return 中文大写，如 {@code 壹佰贰拾叁元肆角伍分}；null 返回 null
	 */
	public static String toChinese(BigDecimal amount) {
		if (amount == null) {
			return null;
		}
		boolean negative = amount.signum() < 0;
		BigDecimal value = amount.abs().setScale(2, RoundingMode.HALF_UP);

		long yuan = value.longValue();
		int jiao = (int) (value.movePointRight(1).longValue() % 10);
		int fen = (int) (value.movePointRight(2).longValue() % 10);

		StringBuilder sb = new StringBuilder();
		appendYuan(sb, yuan);
		sb.append('元');
		if (jiao == 0 && fen == 0) {
			sb.append("整");
		} else {
			if (jiao > 0) {
				sb.append(CN_DIGITS[jiao]).append('角');
			}
			if (fen > 0) {
				if (jiao == 0) {
					sb.append('零');
				}
				sb.append(CN_DIGITS[fen]).append('分');
			}
		}
		if (negative) {
			sb.insert(0, '负');
		}
		return sb.toString();
	}

	/**
	 * 金额转中文大写（double 重载，内部转 BigDecimal 避免浮点误差）。
	 *
	 * @param amount 金额（元）
	 * @return 中文大写
	 */
	public static String toChinese(double amount) {
		return toChinese(BigDecimal.valueOf(amount));
	}

	/**
	 * 金额转中文大写（字符串重载）。
	 *
	 * @param amount 金额字符串，如 {@code "123.45"}
	 * @return 中文大写；非法字符串返回 null
	 */
	public static String toChinese(String amount) {
		if (amount == null) {
			return null;
		}
		try {
			return toChinese(new BigDecimal(amount));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** 整数部分：按 4 位一节（万亿/亿/万/个）输出，处理节间与节内零。 */
	private static void appendYuan(StringBuilder sb, long value) {
		if (value == 0) {
			sb.append(CN_DIGITS[0]);
			return;
		}
		long[] sections = new long[4];
		int n = 0;
		long v = value;
		while (v > 0 && n < 4) {
			sections[n++] = v % 10_000;
			v /= 10_000;
		}
		int top = n - 1;
		boolean gapZero = false; // 自上一个已输出节以来是否出现过空节
		for (int i = top; i >= 0; i--) {
			long sec = sections[i];
			if (sec == 0) {
				gapZero = true;
				continue;
			}
			if (gapZero) {
				sb.append(CN_DIGITS[0]);
				gapZero = false;
			} else if (i < top && sec < 1000) {
				// 非最低节且不足 4 位（千位为空）→ 补零，如「一万零一」
				sb.append(CN_DIGITS[0]);
			}
			appendFour(sb, (int) sec);
			sb.append(CN_BIG[i]);
		}
	}

	/** 输出 4 位数字（千/百/十/个），处理节内零：零仅出现在已输出非零位之后、后续仍有非零位时。 */
	private static void appendFour(StringBuilder sb, int sec) {
		int[] d = {sec / 1000, sec / 100 % 10, sec / 10 % 10, sec % 10};
		boolean outputStarted = false;
		boolean zeroPending = false;
		for (int i = 0; i < 4; i++) {
			if (d[i] == 0) {
				boolean hasLater = false;
				for (int k = i + 1; k < 4; k++) {
					if (d[k] != 0) {
						hasLater = true;
						break;
					}
				}
				if (hasLater && outputStarted) {
					zeroPending = true;
				}
			} else {
				if (zeroPending) {
					sb.append(CN_DIGITS[0]);
					zeroPending = false;
				}
				sb.append(CN_DIGITS[d[i]]);
				if (i < 3) {
					sb.append(CN_UNITS[3 - i]);
				}
				outputStarted = true;
			}
		}
	}
}
