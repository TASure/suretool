package com.sure.tool.date;

/**
 * 日期时间单位，参考 Hutool 的 {@code DateUnit} 设计。
 *
 * @author suretool
 */
public enum DateUnit {

	/** 毫秒 */
	MS(1L),
	/** 秒 */
	SECOND(1000L),
	/** 分 */
	MINUTE(60_000L),
	/** 时 */
	HOUR(3_600_000L),
	/** 天 */
	DAY(86_400_000L),
	/** 周 */
	WEEK(604_800_000L);

	private final long millis;

	DateUnit(long millis) {
		this.millis = millis;
	}

	/**
	 * 获取该单位的毫秒数。
	 *
	 * @return 毫秒数
	 */
	public long getMillis() {
		return millis;
	}
}
