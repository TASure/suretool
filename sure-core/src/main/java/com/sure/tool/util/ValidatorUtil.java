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

import com.sure.tool.lang.PatternPool;

import java.util.regex.Pattern;

/**
 * 数据校验工具类，参考 Hutool 的 {@code ValidatorUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ValidatorUtil {

	private ValidatorUtil() {
	}

	/**
	 * 是否为合法邮箱。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isEmail(String value) {
		return isMatch(PatternPool.EMAIL, value);
	}

	/**
	 * 是否为合法手机号（中国大陆）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isMobile(String value) {
		return isMatch(PatternPool.MOBILE, value);
	}

	/**
	 * 是否为合法 IPv4 地址。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isIpv4(String value) {
		return isMatch(PatternPool.IPV4, value);
	}

	/**
	 * 是否为合法 URL（http/https/ftp）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isUrl(String value) {
		return isMatch(PatternPool.URL, value);
	}

	/**
	 * 是否为纯中文。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isChinese(String value) {
		return isMatch(PatternPool.CHINESE, value);
	}

	/**
	 * 是否为合法车牌号（含新能源）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isPlateNumber(String value) {
		return isMatch(PatternPool.PLATE_NUMBER, value);
	}

	/**
	 * 是否为合法邮政编码（中国大陆，6 位数字）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isPostalCode(String value) {
		return isMatch(PatternPool.POSTAL_CODE, value);
	}

	/**
	 * 是否为合法金额（正数，最多两位小数）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isMoney(String value) {
		return isMatch(PatternPool.MONEY, value);
	}

	/**
	 * 是否为通用单词（字母数字下划线）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isGeneral(String value) {
		return isMatch(PatternPool.GENERAL, value);
	}

	/**
	 * 是否为 UUID（含连字符）。
	 *
	 * @param value 值
	 * @return 是否匹配
	 */
	public static boolean isUuid(String value) {
		return isMatch(PatternPool.UUID, value);
	}

	/**
	 * 是否为合法身份证号码（15/18 位）。
	 *
	 * @param value 值
	 * @return 是否合法
	 */
	public static boolean isIdCard(String value) {
		return IdcardUtil.isValidCard(value);
	}

	/**
	 * 是否为合法端口号（1-65535）。
	 *
	 * @param port 端口号
	 * @return 是否合法
	 */
	public static boolean isValidPort(int port) {
		return port > 0 && port <= 0xFFFF;
	}

	/**
	 * 值是否匹配指定模式。
	 *
	 * @param pattern 模式
	 * @param value   值，为 {@code null} 时不匹配
	 * @return 是否匹配
	 */
	public static boolean isMatch(Pattern pattern, String value) {
		return value != null && pattern.matcher(value).matches();
	}

	/**
	 * 值是否匹配指定正则。
	 *
	 * @param regex 正则表达式
	 * @param value 值，为 {@code null} 时不匹配
	 * @return 是否匹配
	 */
	public static boolean isMatch(String regex, String value) {
		return value != null && value.matches(regex);
	}

	/**
	 * 校验统一社会信用代码。
	 *
	 * @param value 信用代码
	 * @return 是否合法
	 */
	public static boolean isCreditCode(String value) {
		return CreditCodeUtil.isValidCreditCode(value);
	}

	/**
	 * 校验日期字符串（yyyy-MM-dd，含闰年 2 月校验）。
	 *
	 * @param value 日期字符串
	 * @return 是否合法
	 */
	public static boolean isDate(String value) {
		if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) {
			return false;
		}
		try {
			java.time.LocalDate.parse(value);
			return true;
		} catch (java.time.format.DateTimeParseException e) {
			return false;
		}
	}



	/**
	 * 校验 IPv6 地址（含 IPv4 映射的简化校验）。
	 *
	 * @param value IPv6 字符串
	 * @return 是否合法
	 */
	public static boolean isIpv6(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		if (value.contains("::")) {
			return value.split("::").length <= 2;
		}
		String[] groups = value.split(":", -1);
		if (groups.length != 8) {
			return false;
		}
		for (String g : groups) {
			if (g.isEmpty() || g.length() > 4) {
				return false;
			}
			for (int i = 0; i < g.length(); i++) {
				char c = g.charAt(i);
				if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 校验 MAC 地址（xx:xx:xx:xx:xx:xx 或 xx-xx-xx-xx-xx-xx）。
	 *
	 * @param value MAC 字符串
	 * @return 是否合法
	 */
	public static boolean isMac(String value) {
		if (value == null) {
			return false;
		}
		return value.matches("([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}");
	}



	/**
	 * 校验字符串是否全部为小写字母（允许含数字）。
	 *
	 * @param value 字符串
	 * @return 是否全小写
	 */
	public static boolean isLowerCase(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		boolean hasLetter = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetter(c)) {
				hasLetter = true;
				if (!Character.isLowerCase(c)) {
					return false;
				}
			}
		}
		return hasLetter;
	}

	/**
	 * 校验字符串是否全部为大写字母（允许含数字）。
	 *
	 * @param value 字符串
	 * @return 是否全大写
	 */
	public static boolean isUpperCase(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		}
		boolean hasLetter = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetter(c)) {
				hasLetter = true;
				if (!Character.isUpperCase(c)) {
					return false;
				}
			}
		}
		return hasLetter;
	}


}