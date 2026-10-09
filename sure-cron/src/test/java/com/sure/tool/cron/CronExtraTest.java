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

package com.sure.tool.cron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Test;

import com.sure.tool.date.DateUtil;

/**
 * CronPattern / CronUtil 边界补测：空参防御、月份/日不匹配、非法步进、调度幂等与停止态。
 */
public class CronExtraTest {

	private static Date at(String time) {
		return DateUtil.parse(time);
	}

	@Test
	public void 空参防御分支() {
		final CronPattern pattern = CronPattern.of("0 0 12 * * ?");
		assertFalse(pattern.match(null));
		assertNull(pattern.getNextTimeAfter(null));
	}

	@Test
	public void getExpression与toString() {
		final CronPattern pattern = CronPattern.of("0 0 12 * * ?");
		assertEquals("0 0 12 * * ?", pattern.getExpression());
		assertEquals("CronPattern{0 0 12 * * ?}", pattern.toString());
	}

	@Test
	public void 月份不匹配返回false() {
		// 3 月 1 日，匹配 5 月的时刻：年份任意、月份不符
		final CronPattern pattern = CronPattern.of("0 0 0 1 3 ?");
		assertFalse(pattern.match(at("2026-05-15 12:00:00")));
	}

	@Test
	public void 日不匹配返回false() {
		// 任意月 1 日，匹配 2 日的时刻：月份命中、日不符
		final CronPattern pattern = CronPattern.of("0 0 0 1 * ?");
		assertFalse(pattern.match(at("2026-05-02 00:00:00")));
	}

	@Test
	public void 非法步进值抛异常() {
		assertThrows(IllegalArgumentException.class, () -> CronPattern.of("0/x * * * * ?"));
	}

	@Test
	public void 调度空参校验与幂等启停() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> CronUtil.schedule(null, () -> {
		}));
		assertThrows(IllegalArgumentException.class, () -> CronUtil.schedule("0 0 12 * * ?", null));

		// 幂等：重复 start 直接返回
		CronUtil.stop();
		CronUtil.start();
		CronUtil.start();
		CronUtil.stop();

		// 停止态下 tick 直接返回（通过反射触发私有方法）
		final Method tick = CronUtil.class.getDeclaredMethod("tick");
		tick.setAccessible(true);
		tick.invoke(null);
	}
}
