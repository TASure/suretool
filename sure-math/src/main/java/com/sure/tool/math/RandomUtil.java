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
package com.sure.tool.math;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机数工具门面。
 *
 * <p>默认使用 {@link ThreadLocalRandom}（高性能、线程安全）；安全敏感场景（token、
 * 密码盐等）提供 SecureRandom 变体。</p>
 *
 * @since 1.4.0
 */
public final class RandomUtil {

	/** 随机字符串字符集：大小写字母与数字。 */
	public static final String BASE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private static final SecureRandom SECURE = new SecureRandom();

	private RandomUtil() {
	}

	/**
	 * 生成 [min, max] 闭区间随机整数。
	 *
	 * @param min 下界（含）
	 * @param max 上界（含）
	 * @return 随机整数
	 * @throws IllegalArgumentException min &gt; max
	 */
	public static int randomInt(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be <= max, got " + min + ", " + max);
		}
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	/**
	 * 生成 [min, max] 闭区间随机 long。
	 *
	 * @param min 下界（含）
	 * @param max 上界（含）
	 * @return 随机 long
	 * @throws IllegalArgumentException min &gt; max
	 */
	public static long randomLong(long min, long max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be <= max, got " + min + ", " + max);
		}
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	/**
	 * 生成 [min, max) 区间随机 double。
	 *
	 * @param min 下界（含）
	 * @param max 上界（不含）
	 * @return 随机 double
	 * @throws IllegalArgumentException min &gt; max
	 */
	public static double randomDouble(double min, double max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be <= max, got " + min + ", " + max);
		}
		return ThreadLocalRandom.current().nextDouble(min, max);
	}

	/**
	 * 生成指定长度的随机字符串（大小写字母 + 数字）。
	 *
	 * @param length 长度（≥ 0）
	 * @return 随机字符串
	 * @throws IllegalArgumentException length 为负
	 */
	public static String randomString(int length) {
		return randomString(length, BASE_CHAR);
	}

	/**
	 * 从给定字符集生成指定长度的随机字符串。
	 *
	 * @param length 长度（≥ 0）
	 * @param chars  字符集，非空
	 * @return 随机字符串
	 * @throws IllegalArgumentException 参数不合法
	 */
	public static String randomString(int length, String chars) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		}
		if (chars == null || chars.isEmpty()) {
			throw new IllegalArgumentException("chars must not be empty");
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
		}
		return sb.toString();
	}

	/**
	 * 生成指定长度的纯数字字符串。
	 *
	 * @param length 长度（≥ 0）
	 * @return 数字字符串
	 * @throws IllegalArgumentException length 为负
	 */
	public static String randomNumbers(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append((char) ('0' + ThreadLocalRandom.current().nextInt(10)));
		}
		return sb.toString();
	}

	/**
	 * 生成随机字节数组。
	 *
	 * @param length 长度（≥ 0）
	 * @return 随机字节数组
	 * @throws IllegalArgumentException length 为负
	 */
	public static byte[] randomBytes(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		}
		byte[] out = new byte[length];
		ThreadLocalRandom.current().nextBytes(out);
		return out;
	}

	/**
	 * 从列表随机取一个元素。
	 *
	 * @param <T> 元素类型
	 * @param list 列表，非空
	 * @return 随机元素
	 * @throws IllegalArgumentException list 为 null/空
	 */
	public static <T> T randomElement(List<T> list) {
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("list must not be null or empty");
		}
		return list.get(ThreadLocalRandom.current().nextInt(list.size()));
	}

	/**
	 * 从数组随机取一个元素。
	 *
	 * @param <T>    元素类型
	 * @param values 数组，非空
	 * @return 随机元素
	 * @throws IllegalArgumentException values 为 null/空
	 */
	@SafeVarargs
	public static <T> T randomElement(T... values) {
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("values must not be null or empty");
		}
		return values[ThreadLocalRandom.current().nextInt(values.length)];
	}

	/**
	 * 随机打乱列表（Fisher-Yates，原地修改）。
	 *
	 * @param <T> 元素类型
	 * @param list 待打乱列表
	 * @return 打乱后的同一列表（便于链式调用）
	 */
	public static <T> List<T> shuffle(List<T> list) {
		if (list == null) {
			throw new IllegalArgumentException("list must not be null");
		}
		Collections.shuffle(list);
		return list;
	}

	/**
	 * 生成 [min, max] 闭区间安全随机整数（SecureRandom）。
	 *
	 * @param min 下界（含）
	 * @param max 上界（含）
	 * @return 安全随机整数
	 * @throws IllegalArgumentException min &gt; max
	 */
	public static int secureRandomInt(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be <= max, got " + min + ", " + max);
		}
		return SECURE.nextInt(max - min + 1) + min;
	}

	/**
	 * 生成指定长度的安全随机字节数组（SecureRandom）。
	 *
	 * @param length 长度（≥ 0）
	 * @return 安全随机字节数组
	 * @throws IllegalArgumentException length 为负
	 */
	public static byte[] secureRandomBytes(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		}
		byte[] out = new byte[length];
		SECURE.nextBytes(out);
		return out;
	}

	/**
	 * 生成指定长度的安全随机字符串（SecureRandom）。
	 *
	 * @param length 长度（≥ 0）
	 * @return 安全随机字符串
	 * @throws IllegalArgumentException length 为负
	 */
	public static String secureRandomString(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be >= 0, got " + length);
		}
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(BASE_CHAR.charAt(SECURE.nextInt(BASE_CHAR.length())));
		}
		return sb.toString();
	}
}
