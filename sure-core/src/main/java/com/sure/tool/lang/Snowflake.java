package com.sure.tool.lang;

/**
 * 雪花算法 ID 生成器，参考 Hutool 的 {@code Snowflake} 设计。
 *
 * <p>64 位 long：1 位符号位 + 41 位毫秒时间戳 + 5 位数据中心 + 5 位工作机器 + 12 位序列号。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class Snowflake {

	/** 起始时间戳：2010-11-04 09:42:54 UTC */
	private static final long DEFAULT_EPOCH = 1288834974657L;
	private static final long WORKER_ID_BITS = 5L;
	private static final long DATACENTER_ID_BITS = 5L;
	private static final long SEQUENCE_BITS = 12L;
	private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
	private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);
	private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
	private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
	private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
	private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

	private final long workerId;
	private final long datacenterId;
	private final long epoch;
	private long sequence = 0L;
	private long lastTimestamp = -1L;

	/**
	 * 构造雪花 ID 生成器。
	 *
	 * @param workerId     工作机器 ID（0-31）
	 * @param datacenterId 数据中心 ID（0-31）
	 */
	public Snowflake(long workerId, long datacenterId) {
		this(workerId, datacenterId, DEFAULT_EPOCH);
	}

	/**
	 * 构造雪花 ID 生成器（自定义起始时间戳）。
	 *
	 * @param workerId     工作机器 ID（0-31）
	 * @param datacenterId 数据中心 ID（0-31）
	 * @param epoch        起始时间戳（毫秒）
	 */
	public Snowflake(long workerId, long datacenterId, long epoch) {
		if (workerId > MAX_WORKER_ID || workerId < 0) {
			throw new IllegalArgumentException("workerId 必须在 0-" + MAX_WORKER_ID + " 之间");
		}
		if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
			throw new IllegalArgumentException("datacenterId 必须在 0-" + MAX_DATACENTER_ID + " 之间");
		}
		if (epoch <= 0 || epoch > System.currentTimeMillis()) {
			throw new IllegalArgumentException("epoch 必须大于 0 且不超过当前时间");
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
		this.epoch = epoch;
	}

	/**
	 * 生成下一个 ID。
	 *
	 * @return 雪花 ID
	 */
	public synchronized long nextId() {
		long timestamp = System.currentTimeMillis();
		if (timestamp < lastTimestamp) {
			long offset = lastTimestamp - timestamp;
			if (offset > 5000) {
				throw new IllegalStateException("时钟回拨超过 5 秒（" + offset + "ms），拒绝生成 ID");
			}
			// 等待时钟追平
			while (timestamp < lastTimestamp) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("等待时钟追平时线程被中断", e);
				}
				timestamp = System.currentTimeMillis();
			}
		}
		if (timestamp == lastTimestamp) {
			sequence = (sequence + 1) & SEQUENCE_MASK;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}
		lastTimestamp = timestamp;
		return ((timestamp - epoch) << TIMESTAMP_LEFT_SHIFT)
				| (datacenterId << DATACENTER_ID_SHIFT)
				| (workerId << WORKER_ID_SHIFT)
				| sequence;
	}

	/**
	 * 生成下一个 ID（字符串形式）。
	 *
	 * @return 雪花 ID 字符串
	 */
	public String nextIdStr() {
		return Long.toString(nextId());
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}
		return timestamp;
	}

	/**
	 * 获取工作机器 ID。
	 *
	 * @return 工作机器 ID
	 */
	public long getWorkerId() {
		return workerId;
	}

	/**
	 * 获取数据中心 ID。
	 *
	 * @return 数据中心 ID
	 */
	public long getDatacenterId() {
		return datacenterId;
	}
}
