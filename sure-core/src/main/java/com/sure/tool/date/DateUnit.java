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
package com.sure.tool.date;

/**
 * 日期时间单位，参考 Hutool 的 {@code DateUnit} 设计。
 *
 * @author suretool
 * @since 0.1.0
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