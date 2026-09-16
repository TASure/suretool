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
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ULID 生成器：26 字符 Crockford Base32 编码，48 位毫秒时间戳 + 80 位随机，按字典序近似时间有序。
 * <p>
 * 参考 {@code ulid/spec}（如 {@code 01ARZ3NDEKTSV4RRFFQ69G5FAV}）。可选单调递增模式（同一毫秒内保证递增），
 * 适合作为数据库主键、消息 ID 等需要时间有序且不可枚举的业务 ID。
 *
 * @author suretool
 * @since 0.2.0
 */
public class UlidUtil {

	/** Crockford Base32 字母表（不含 I/L/O/U）。 */
	private static final char[] CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/** 单调模式下的上一次时间戳与随机部分。 */
	private static volatile long lastTimestamp = -1;
	private static volatile long lastRandom1;
	private static volatile int lastRandom2;

	private UlidUtil() {
	}

	/**
	 * 生成 ULID（非单调）。
	 *
	 * @return 26 字符 ULID
	 */
	public static String ulid() {
		return ulid(System.currentTimeMillis(), SECURE_RANDOM.nextLong(),
				ThreadLocalRandom.current().nextInt(0x10000));
	}

	/**
	 * 生成单调递增 ULID：同一毫秒内连续调用保证字典序递增。
	 *
	 * @return 26 字符 ULID
	 */
	public static String monotonicUlid() {
		long ts = System.currentTimeMillis();
		long r1;
		int r2;
		synchronized (UlidUtil.class) {
			if (ts == lastTimestamp) {
				// 同一毫秒：随机部分 +1（低位进位），溢出时时间戳 +1
				lastRandom2++;
				if (lastRandom2 > 0xFFFF) {
					lastRandom2 = 0;
					lastRandom1++;
					if (lastRandom1 == 0) {
						lastTimestamp++;
					}
				}
			} else {
				lastTimestamp = ts;
				lastRandom1 = SECURE_RANDOM.nextLong();
				lastRandom2 = ThreadLocalRandom.current().nextInt(0x10000);
			}
			r1 = lastRandom1;
			r2 = lastRandom2;
		}
		return encode(r1, r2, lastTimestamp);
	}

	/**
	 * 基于指定时间戳生成 ULID。
	 *
	 * @param instant 时间点
	 * @return 26 字符 ULID
	 */
	public static String ulid(Instant instant) {
		return ulid(instant.toEpochMilli(), SECURE_RANDOM.nextLong(),
				ThreadLocalRandom.current().nextInt(0x10000));
	}

	private static String ulid(long timestamp, long r1, int r2) {
		return encode(r1, r2, timestamp);
	}

	/**
	 * 编码为 26 字符：48 位时间戳 + 64 位随机（r1）+ 16 位随机（r2）= 128 位。
	 */
	private static String encode(long r1, int r2, long timestamp) {
		byte[] bytes = new byte[17]; // 末尾 0 填充，避免最后字符编码越界
		bytes[0] = (byte) (timestamp >>> 40);
		bytes[1] = (byte) (timestamp >>> 32);
		bytes[2] = (byte) (timestamp >>> 24);
		bytes[3] = (byte) (timestamp >>> 16);
		bytes[4] = (byte) (timestamp >>> 8);
		bytes[5] = (byte) timestamp;
		bytes[6] = (byte) (r1 >>> 56);
		bytes[7] = (byte) (r1 >>> 48);
		bytes[8] = (byte) (r1 >>> 40);
		bytes[9] = (byte) (r1 >>> 32);
		bytes[10] = (byte) (r1 >>> 24);
		bytes[11] = (byte) (r1 >>> 16);
		bytes[12] = (byte) (r1 >>> 8);
		bytes[13] = (byte) r1;
		bytes[14] = (byte) (r2 >>> 8);
		bytes[15] = (byte) r2;

		StringBuilder sb = new StringBuilder(26);
		for (int i = 0; i < 26; i++) {
			int bit = i * 5;
			int index = bit / 8;
			int offset = bit % 8;
			int value;
			switch (offset) {
				case 0 -> value = (bytes[index] >>> 3) & 0x1F;
				case 1 -> value = (bytes[index] >>> 2) & 0x1F;
				case 2 -> value = (bytes[index] >>> 1) & 0x1F;
				case 3 -> value = bytes[index] & 0x1F;
				case 4 -> value = ((bytes[index] & 0x0F) << 1) | ((bytes[index + 1] >>> 7) & 0x01);
				case 5 -> value = ((bytes[index] & 0x07) << 2) | ((bytes[index + 1] >>> 6) & 0x03);
				case 6 -> value = ((bytes[index] & 0x03) << 3) | ((bytes[index + 1] >>> 5) & 0x07);
				default -> value = ((bytes[index] & 0x01) << 4) | ((bytes[index + 1] >>> 4) & 0x0F);
			}
			sb.append(CROCKFORD[value]);
		}
		return sb.toString();
	}

	/**
	 * 校验是否为合法 ULID。
	 *
	 * @param value 候选字符串
	 * @return 是否合法（26 字符且全部属于 Crockford 字母表）
	 */
	public static boolean isValid(String value) {
		if (value == null || value.length() != 26) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (indexOf(value.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	private static int indexOf(char c) {
		for (int i = 0; i < CROCKFORD.length; i++) {
			if (CROCKFORD[i] == c) {
				return i;
			}
		}
		return -1;
	}
}
