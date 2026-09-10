package com.sure.tool.util;

import com.sure.tool.lang.PatternPool;

/**
 * 身份证号码工具类（中国大陆 15/18 位），参考 Hutool 的 {@code IdcardUtil} 设计。
 * 支持合法性校验、15 位转 18 位、出生日期与性别解析。
 *
 * @author suretool
 */
public class IdcardUtil {

	/** 校验码加权因子 */
	private static final int[] WEIGHT = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/** 校验码映射表（余数 0-10 对应的校验码） */
	private static final char[] CHECK_CODE = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

	private IdcardUtil() {
	}

	/**
	 * 校验身份证号码是否合法（支持 15/18 位）。
	 *
	 * @param idCard 身份证号码
	 * @return 是否合法
	 */
	public static boolean isValidCard(String idCard) {
		if (idCard == null) {
			return false;
		}
		if (PatternPool.ID_CARD_18.matcher(idCard).matches()) {
			return isValidCard18(idCard);
		}
		return PatternPool.ID_CARD_15.matcher(idCard).matches() && isValidCard15(idCard);
	}

	/**
	 * 校验 18 位身份证（含校验码校验）。
	 *
	 * @param idCard 身份证号码
	 * @return 是否合法
	 */
	public static boolean isValidCard18(String idCard) {
		if (idCard == null || !PatternPool.ID_CARD_18.matcher(idCard).matches()) {
			return false;
		}
		return Character.toUpperCase(idCard.charAt(17)) == calculateCheckCode(idCard.substring(0, 17));
	}

	/**
	 * 校验 15 位身份证（仅校验出生日期合法性，15 位无校验码）。
	 *
	 * @param idCard 身份证号码
	 * @return 是否合法
	 */
	public static boolean isValidCard15(String idCard) {
		if (idCard == null || !PatternPool.ID_CARD_15.matcher(idCard).matches()) {
			return false;
		}
		return isValidBirthDate("19" + idCard.substring(6, 12));
	}

	/**
	 * 15 位身份证转 18 位。
	 *
	 * @param idCard 15 位身份证号码
	 * @return 18 位身份证号码
	 * @throws IllegalArgumentException 号码不合法时抛出
	 */
	public static String convert15To18(String idCard) {
		if (idCard == null || !PatternPool.ID_CARD_15.matcher(idCard).matches()) {
			throw new IllegalArgumentException("15 位身份证号码不合法: " + idCard);
		}
		String prefix = idCard.substring(0, 6) + "19" + idCard.substring(6);
		return prefix + calculateCheckCode(prefix);
	}

	/**
	 * 根据前 17 位计算校验码。
	 *
	 * @param idCard17 前 17 位
	 * @return 校验码（0-9 或 X）
	 * @throws IllegalArgumentException 长度不是 17 位时抛出
	 */
	public static char calculateCheckCode(String idCard17) {
		if (idCard17 == null || idCard17.length() != 17) {
			throw new IllegalArgumentException("前 17 位长度必须为 17");
		}
		int sum = 0;
		for (int i = 0; i < 17; i++) {
			sum += (idCard17.charAt(i) - '0') * WEIGHT[i];
		}
		return CHECK_CODE[sum % 11];
	}

	/**
	 * 获取出生日期（{@code yyyy-MM-dd}）。
	 *
	 * @param idCard 身份证号码
	 * @return 出生日期，号码不合法返回 {@code null}
	 */
	public static String getBirthDate(String idCard) {
		if (idCard == null) {
			return null;
		}
		String birth;
		if (PatternPool.ID_CARD_18.matcher(idCard).matches()) {
			birth = idCard.substring(6, 14);
		} else if (PatternPool.ID_CARD_15.matcher(idCard).matches()) {
			birth = "19" + idCard.substring(6, 12);
		} else {
			return null;
		}
		if (!isValidBirthDate(birth)) {
			return null;
		}
		return birth.substring(0, 4) + "-" + birth.substring(4, 6) + "-" + birth.substring(6, 8);
	}

	/**
	 * 获取性别（倒数第二位奇数男、偶数女）。
	 *
	 * @param idCard 身份证号码
	 * @return "男" / "女"，号码不合法返回 {@code null}
	 */
	public static String getGender(String idCard) {
		if (idCard == null) {
			return null;
		}
		if (PatternPool.ID_CARD_18.matcher(idCard).matches()) {
			return ((idCard.charAt(16) - '0') % 2 == 1) ? "男" : "女";
		}
		if (PatternPool.ID_CARD_15.matcher(idCard).matches()) {
			return ((idCard.charAt(14) - '0') % 2 == 1) ? "男" : "女";
		}
		return null;
	}

	/**
	 * 获取省份编码（前两位）。
	 *
	 * @param idCard 身份证号码
	 * @return 省份编码，号码不合法返回 {@code null}
	 */
	public static String getProvinceCode(String idCard) {
		if (idCard == null) {
			return null;
		}
		if (PatternPool.ID_CARD_18.matcher(idCard).matches() || PatternPool.ID_CARD_15.matcher(idCard).matches()) {
			return idCard.substring(0, 2);
		}
		return null;
	}

	private static boolean isValidBirthDate(String birth) {
		if (birth == null || birth.length() != 8) {
			return false;
		}
		int year = Integer.parseInt(birth.substring(0, 4));
		int month = Integer.parseInt(birth.substring(4, 6));
		int day = Integer.parseInt(birth.substring(6, 8));
		if (month < 1 || month > 12 || day < 1) {
			return false;
		}
		int maxDay;
		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				maxDay = 31;
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				maxDay = 30;
				break;
			default:
				maxDay = isLeapYear(year) ? 29 : 28;
				break;
		}
		return day <= maxDay;
	}

	private static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}
}
