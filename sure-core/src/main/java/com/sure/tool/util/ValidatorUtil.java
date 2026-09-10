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
}
