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
package com.sure.tool.lang;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

/**
 * Snowflake 覆盖率补测：构造参数校验与时钟回拨路径（反射改 lastTimestamp）。
 *
 * @author suretool
 * @since 1.13.1
 */
public class SnowflakeGapTest {

	private Snowflake newSnowflake() {
		return new Snowflake(1, 1, 1288834974657L);
	}

	private void setLastTimestamp(Snowflake s, long ts) throws Exception {
		Field f = Snowflake.class.getDeclaredField("lastTimestamp");
		f.setAccessible(true);
		f.setLong(s, ts);
	}

	@Test
	public void testCtorValidation() {
		try {
			new Snowflake(-1, 1, 1288834974657L);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			new Snowflake(1, 99, 1288834974657L);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			new Snowflake(1, 1, 0L);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testBackwardClock() throws Exception {
		Snowflake s = newSnowflake();
		long id1 = s.nextId();
		Assert.assertTrue(id1 != 0L);
		// 回拨超过 5 秒 -> 抛异常
		setLastTimestamp(s, System.currentTimeMillis() + 6000L);
		try {
			s.nextId();
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			// expected
		}
		// 小幅回拨 -> 等待追平后正常生成
		setLastTimestamp(s, System.currentTimeMillis() + 5L);
		long id2 = s.nextId();
		Assert.assertTrue(id2 != 0L);
	}
}
