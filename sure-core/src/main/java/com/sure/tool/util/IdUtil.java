package com.sure.tool.util;

import com.sure.tool.lang.Snowflake;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成工具类，参考 Hutool 的 {@code IdUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class IdUtil {

	private static final AtomicInteger COUNTER = new AtomicInteger(ThreadLocalRandom.current().nextInt());

	private IdUtil() {
	}

	/**
	 * 随机 UUID（含连字符）。
	 *
	 * @return UUID 字符串
	 */
	public static String randomUUID() {
		return RandomUtil.randomUUID();
	}

	/**
	 * 随机 UUID（不含连字符）。
	 *
	 * @return UUID 字符串
	 */
	public static String simpleUUID() {
		return RandomUtil.simpleUUID();
	}

	/**
	 * 生成 MongoDB 风格的 24 位十六进制 ObjectId（时间戳 + 随机数 + 自增计数）。
	 *
	 * @return ObjectId 字符串
	 */
	public static String objectId() {
		int time = (int) (System.currentTimeMillis() / 1000L);
		int random = ThreadLocalRandom.current().nextInt();
		int counter = COUNTER.incrementAndGet() & 0xFFFFFF;
		String hex = Integer.toHexString(time) + Integer.toHexString(random) + Integer.toHexString(counter);
		return StrUtil.padPre(hex, 24, '0');
	}

	/**
	 * 创建雪花 ID 生成器。
	 *
	 * @param workerId      工作机器 ID（0-31）
	 * @param datacenterId  数据中心 ID（0-31）
	 * @return 雪花 ID 生成器
	 */
	public static Snowflake createSnowflake(long workerId, long datacenterId) {
		return new Snowflake(workerId, datacenterId);
	}
}
