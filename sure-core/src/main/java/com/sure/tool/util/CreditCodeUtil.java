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
 * 统一社会信用代码工具：合法性校验（GB 32100-2015），零依赖。
 *
 * <p>18 位：登记管理部门 1 位 + 机构类别 1 位 + 登记管理机关行政区划 6 位
 * + 主体标识码 9 位 + 校验码 1 位。不含 I、O、S、V、Z。</p>
 *
 * @author suretool
 * @since 0.2.0
 */
public class CreditCodeUtil {

	/** 加权因子（从第 1 位到第 17 位）。 */
	private static final int[] WEIGHT = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};

	/** 校验码字符表（索引即余数对应字符，不含 I/O/S/V/Z）。 */
	private static final String CHECK_STR = "0123456789ABCDEFGHJKLMNPQRTUWXY";

	private CreditCodeUtil() {
	}

	/**
	 * 校验统一社会信用代码是否合法（GB 32100-2015）。
	 *
	 * @param code 18 位代码
	 * @return 是否合法
	 */
	public static boolean isValidCreditCode(String code) {
		if (code == null || code.length() != 18) {
			return false;
		}
		String upper = code.toUpperCase();
		for (int i = 0; i < 17; i++) {
			char c = upper.charAt(i);
			if ("IOSVZ".indexOf(c) >= 0) {
				return false;
			}
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}
		int sum = 0;
		for (int i = 0; i < 17; i++) {
			int value = charValue(upper.charAt(i));
			if (value < 0) {
				return false;
			}
			sum += value * WEIGHT[i];
		}
		int mod = sum % 31;
		char expect = CHECK_STR.charAt(31 - mod == 31 ? 0 : 31 - mod);
		return expect == upper.charAt(17);
	}

	/**
	 * 掩码：隐藏第 3~14 位（行政区划+主体标识码），保留首 2 位与末 4 位。
	 *
	 * @param code 18 位代码
	 * @return 掩码结果；非法长度返回原文
	 */
	public static String hide(String code) {
		if (code == null || code.length() != 18) {
			return code;
		}
		return code.substring(0, 2) + "*".repeat(code.length() - 6) + code.substring(code.length() - 4);
	}

	private static int charValue(char c) {
		return CHECK_STR.indexOf(c);
	}
}
