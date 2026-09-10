package com.sure.tool.util;

/**
 * 数据脱敏工具类，参考 Hutool 的 {@code DesensitizedUtil} 设计。
 *
 * @author suretool
 */
public class DesensitizedUtil {

	private DesensitizedUtil() {
	}

	/**
	 * 手机号脱敏：{@code 13812345678} → {@code 138****5678}。
	 *
	 * @param phone 手机号
	 * @return 脱敏后的手机号，长度不足 7 位返回原值
	 */
	public static String mobilePhone(String phone) {
		if (StrUtil.isBlank(phone) || phone.length() < 7) {
			return phone;
		}
		return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
	}

	/**
	 * 身份证号脱敏：保留前 3 位和后 4 位，如 {@code 110101********1234}。
	 *
	 * @param idCard 身份证号
	 * @return 脱敏后的身份证号，长度不足 8 位返回原值
	 */
	public static String idCardNum(String idCard) {
		if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
			return idCard;
		}
		return mask(idCard, 3, idCard.length() - 4);
	}

	/**
	 * 银行卡号脱敏：保留前 4 位和后 4 位，如 {@code 6222 **** **** 1234} 风格的 {@code 6222**********1234}。
	 *
	 * @param bankCard 银行卡号
	 * @return 脱敏后的银行卡号，长度不足 8 位返回原值
	 */
	public static String bankCard(String bankCard) {
		if (StrUtil.isBlank(bankCard) || bankCard.length() < 8) {
			return bankCard;
		}
		return mask(bankCard, 4, bankCard.length() - 4);
	}

	/**
	 * 邮箱脱敏：保留首字符和 @ 后的域名，如 {@code a***@example.com}。
	 *
	 * @param email 邮箱
	 * @return 脱敏后的邮箱
	 */
	public static String email(String email) {
		if (StrUtil.isBlank(email)) {
			return email;
		}
		int at = email.indexOf('@');
		if (at <= 1) {
			return email;
		}
		return email.substring(0, 1) + "***" + email.substring(at);
	}

	/**
	 * 密码脱敏：统一显示为 {@code ******}。
	 *
	 * @param password 密码
	 * @return {@code ******}
	 */
	public static String password(String password) {
		return "******";
	}

	/**
	 * 中文姓名脱敏：保留首尾字符，如 {@code 张三丰} → {@code 张*丰}，两字名如 {@code 张三} → {@code 张*}。
	 *
	 * @param name 中文姓名
	 * @return 脱敏后的姓名
	 */
	public static String chineseName(String name) {
		if (StrUtil.isBlank(name) || name.length() <= 1) {
			return name;
		}
		if (name.length() == 2) {
			return name.substring(0, 1) + "*";
		}
		return name.substring(0, 1) + StrUtil.repeat("*", name.length() - 2) + name.substring(name.length() - 1);
	}

	/**
	 * 地址脱敏：保留头部和尾部各三分之一，中间用 {@code ****} 代替。
	 *
	 * @param address 地址
	 * @return 脱敏后的地址
	 */
	public static String address(String address) {
		if (StrUtil.isBlank(address) || address.length() <= 4) {
			return address;
		}
		int head = Math.max(2, address.length() / 3);
		int tail = Math.max(2, address.length() / 3);
		if (head + tail >= address.length()) {
			return address;
		}
		return address.substring(0, head) + "****" + address.substring(address.length() - tail);
	}

	/**
	 * IP 地址脱敏：保留前两段，如 {@code 192.168.*.*}。
	 *
	 * @param ip IPv4 地址
	 * @return 脱敏后的 IP
	 */
	public static String ipv4(String ip) {
		if (StrUtil.isBlank(ip)) {
			return ip;
		}
		String[] parts = StrUtil.split(ip, '.');
		if (parts.length != 4) {
			return ip;
		}
		return parts[0] + "." + parts[1] + ".*.*";
	}

	/**
	 * 通用脱敏：将指定区间替换为 {@code *}。
	 *
	 * @param str   原字符串
	 * @param start 起始索引（含）
	 * @param end   结束索引（不含）
	 * @return 脱敏后的字符串
	 */
	public static String mask(String str, int start, int end) {
		if (StrUtil.isBlank(str)) {
			return str;
		}
		int len = str.length();
		if (start < 0) {
			start = 0;
		}
		if (end > len) {
			end = len;
		}
		if (start >= end) {
			return str;
		}
		return str.substring(0, start) + StrUtil.repeat("*", end - start) + str.substring(end);
	}
}
