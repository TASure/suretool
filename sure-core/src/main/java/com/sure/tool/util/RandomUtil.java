package com.sure.tool.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机工具类，参考 Hutool 的 {@code RandomUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class RandomUtil {

	/** 数字字符集 */
	public static final String BASE_NUMBER = "0123456789";
	/** 英文字符集 */
	public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** 字母数字字符集 */
	public static final String BASE_CHAR_NUMBER = BASE_CHAR + BASE_NUMBER;

	private RandomUtil() {
	}

	/**
	 * 随机 int。
	 *
	 * @return 随机 int
	 */
	public static int randomInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	/**
	 * 随机 int，范围 {@code [min, max)}。
	 *
	 * @param min 最小值（含）
	 * @param max 最大值（不含）
	 * @return 随机 int
	 */
	public static int randomInt(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max 必须大于 min");
		}
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	/**
	 * 随机 int，范围 {@code [0, limit)}。
	 *
	 * @param limit 上限（不含）
	 * @return 随机 int
	 */
	public static int randomInt(int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("limit 必须大于 0");
		}
		return ThreadLocalRandom.current().nextInt(limit);
	}

	/**
	 * 随机 long。
	 *
	 * @return 随机 long
	 */
	public static long randomLong() {
		return ThreadLocalRandom.current().nextLong();
	}

	/**
	 * 随机 long，范围 {@code [min, max)}。
	 *
	 * @param min 最小值（含）
	 * @param max 最大值（不含）
	 * @return 随机 long
	 */
	public static long randomLong(long min, long max) {
		if (min >= max) {
			throw new IllegalArgumentException("max 必须大于 min");
		}
		return ThreadLocalRandom.current().nextLong(min, max);
	}

	/**
	 * 随机 double，范围 {@code [0, 1)}。
	 *
	 * @return 随机 double
	 */
	public static double randomDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * 随机 double，范围 {@code [min, max)}。
	 *
	 * @param min 最小值（含）
	 * @param max 最大值（不含）
	 * @return 随机 double
	 */
	public static double randomDouble(double min, double max) {
		if (min >= max) {
			throw new IllegalArgumentException("max 必须大于 min");
		}
		return ThreadLocalRandom.current().nextDouble(min, max);
	}

	/**
	 * 随机 double，范围 {@code [min, max)}，保留指定小数位。
	 *
	 * @param min   最小值（含）
	 * @param max   最大值（不含）
	 * @param scale 小数位
	 * @return 随机 double
	 */
	public static double randomDouble(double min, double max, int scale) {
		double value = randomDouble(min, max);
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * 随机布尔值。
	 *
	 * @return 随机布尔值
	 */
	public static boolean randomBoolean() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	/**
	 * 随机字符串（字母 + 数字）。
	 *
	 * @param length 长度
	 * @return 随机字符串
	 */
	public static String randomString(int length) {
		return randomString(BASE_CHAR_NUMBER, length);
	}

	/**
	 * 从指定字符集中随机字符串。
	 *
	 * @param baseStr 字符集
	 * @param length  长度
	 * @return 随机字符串
	 */
	public static String randomString(String baseStr, int length) {
		if (baseStr == null || baseStr.isEmpty()) {
			throw new IllegalArgumentException("字符集不能为空");
		}
		if (length < 0) {
			throw new IllegalArgumentException("长度不能为负数");
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(baseStr.charAt(randomInt(baseStr.length())));
		}
		return sb.toString();
	}

	/**
	 * 随机数字字符串。
	 *
	 * @param length 长度
	 * @return 随机数字字符串
	 */
	public static String randomNumbers(int length) {
		return randomString(BASE_NUMBER, length);
	}

	/**
	 * 随机字节数组。
	 *
	 * @param count 字节数
	 * @return 随机字节数组
	 */
	public static byte[] randomBytes(int count) {
		byte[] bytes = new byte[count];
		ThreadLocalRandom.current().nextBytes(bytes);
		return bytes;
	}

	/**
	 * 随机 UUID（含连字符）。
	 *
	 * @return UUID 字符串
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 随机 UUID（不含连字符）。
	 *
	 * @return UUID 字符串
	 */
	public static String simpleUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
