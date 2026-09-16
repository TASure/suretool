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
 * 银行卡号工具：Luhn 校验、掩码，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class BankCardUtil {

	private BankCardUtil() {
	}

	/**
	 * Luhn 算法校验银行卡号是否合法。
	 *
	 * @param cardNo 银行卡号（数字）
	 * @return 是否合法
	 */
	public static boolean isValid(String cardNo) {
		if (cardNo == null) {
			return false;
		}
		String clean = cardNo.replaceAll("\\s", "");
		if (clean.isEmpty()) {
			return false;
		}
		int sum = 0;
		boolean doubleDigit = false;
		for (int i = clean.length() - 1; i >= 0; i--) {
			char c = clean.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
			int digit = c - '0';
			if (doubleDigit) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return sum % 10 == 0;
	}

	/**
	 * 银行卡号掩码：保留前 4 位与后 4 位，中间以 * 填充。
	 *
	 * @param cardNo 卡号
	 * @return 掩码结果；长度不足 8 位原样返回
	 */
	public static String hide(String cardNo) {
		if (cardNo == null) {
			return null;
		}
		String clean = cardNo.replaceAll("\\s", "");
		if (clean.length() < 8) {
			return cardNo;
		}
		return clean.substring(0, 4) + "*".repeat(clean.length() - 8) + clean.substring(clean.length() - 4);
	}
}
