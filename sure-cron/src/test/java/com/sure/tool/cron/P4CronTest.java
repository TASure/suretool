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

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * P4（v0.2.0）第十三批：CronUtil 便捷入口测试。
 */
public class P4CronTest {

	@Test
	public void testNextTimeAfter() {
		Date base = new Date(2026 - 1900, 8, 16, 10, 0, 0); // 2026-09-16 10:00:00
		// 每分钟执行
		Date next = CronUtil.nextTimeAfter("0 * * * * ?", base);
		Assert.assertNotNull(next);
		Assert.assertTrue(next.getTime() > base.getTime());
		// 每天 12:00
		Date noon = CronUtil.nextTimeAfter("0 0 12 * * ?", base);
		Assert.assertNotNull(noon);
		Assert.assertEquals(12, noon.getHours());
		// 非法表达式
		try {
			CronUtil.parse("invalid");
			Assert.fail("非法表达式应抛异常");
		} catch (Exception expected) {
			// expected
		}
	}
}
