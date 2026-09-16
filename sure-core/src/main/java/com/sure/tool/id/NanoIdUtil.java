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
package com.sure.tool.id;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Nano ID 生成器：URL 安全的紧凑随机 ID（默认 21 字符），参考 {@code ai.nanolay.NanoId} 设计。
 * <p>
 * 默认字母表 {@code -_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ}
 * 不含歧义字符，适合短链、主键、文件名等场景。
 *
 * @author suretool
 * @since 0.2.0
 */
public class NanoIdUtil {

	/** 默认字母表（URL 安全，无歧义字符）。 */
	private static final char[] DEFAULT_ALPHABET =
			"-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	/** 默认长度。 */
	public static final int DEFAULT_SIZE = 21;

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private NanoIdUtil() {
	}

	/**
	 * 获取默认字母表副本（防止外部修改）。
	 *
	 * @return 默认字母表副本
	 */
	public static char[] getDefaultAlphabet() {
		return DEFAULT_ALPHABET.clone();
	}

	/**
	 * 生成默认 21 字符的 Nano ID。
	 *
	 * @return Nano ID
	 */
	public static String randomNanoId() {
		return randomNanoId(DEFAULT_SIZE, DEFAULT_ALPHABET, SECURE_RANDOM);
	}

	/**
	 * 生成指定长度的 Nano ID（默认字母表）。
	 *
	 * @param size 长度（&gt;0）
	 * @return Nano ID
	 */
	public static String randomNanoId(int size) {
		return randomNanoId(size, DEFAULT_ALPHABET, SECURE_RANDOM);
	}

	/**
	 * 使用自定义字母表生成 Nano ID。
	 *
	 * @param size    长度（&gt;0）
	 * @param alphabet 字母表（长度 1~255）
	 * @return Nano ID
	 */
	public static String randomNanoId(int size, char[] alphabet) {
		return randomNanoId(size, alphabet, SECURE_RANDOM);
	}

	/**
	 * 使用指定随机源生成 Nano ID。
	 *
	 * @param size     长度（&gt;0）
	 * @param alphabet 字母表
	 * @param random   随机源
	 * @return Nano ID
	 */
	public static String randomNanoId(int size, char[] alphabet, java.util.Random random) {
		if (size < 1) {
			throw new IllegalArgumentException("size 必须大于 0");
		}
		if (alphabet == null || alphabet.length == 0 || alphabet.length > 255) {
			throw new IllegalArgumentException("alphabet 长度必须在 1~255 之间");
		}
		int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2))) - 1;
		int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);

		StringBuilder id = new StringBuilder(size);
		byte[] bytes = new byte[step];
		while (true) {
			random.nextBytes(bytes);
			for (int i = 0; i < step; i++) {
				int index = bytes[i] & mask;
				if (index < alphabet.length) {
					id.append(alphabet[index]);
					if (id.length() == size) {
						return id.toString();
					}
				}
			}
		}
	}

	/**
	 * 快速生成（ThreadLocalRandom，非加密安全，适合高吞吐批量场景）。
	 *
	 * @param size 长度
	 * @return Nano ID
	 */
	public static String fastRandomNanoId(int size) {
		return randomNanoId(size, DEFAULT_ALPHABET, ThreadLocalRandom.current());
	}
}
