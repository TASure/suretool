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

import java.util.Date;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Provide;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;
import org.junit.Assert;

/**
 * 批30 模糊测试（属性测试）：日期格式化-解析往返不变量。
 */
public class DateUtilPropertyTest {

	@Provide
	public static Arbitrary<String> validPatterns() {
		return Arbitraries.of(
				"yyyy-MM-dd HH:mm:ss",
				"yyyy-MM-dd",
				"yyyy/MM/dd HH:mm",
				"yyyyMMddHHmmss");
	}

	@Property
	public void formatParseRoundTrip(@ForAll("validPatterns") String pattern,
			@ForAll @LongRange(min = 0, max = 4_102_444_800_000L) long epochMillis) {
		// 格式化幂等不变量：parse(format(d)) 后重新 format 与原串一致
		Date d = new Date(epochMillis);
		String once = DateUtil.format(d, pattern);
		Date parsed = DateUtil.parse(once, pattern);
		Assert.assertEquals(once, DateUtil.format(parsed, pattern));
	}

	@Property
	public void formatDateFixedLength(@ForAll @LongRange(min = 0, max = 4_102_444_800_000L) long epochMillis) {
		// 1970-2100 年间 yyyy-MM-dd 恒为 10 字符
		String s = DateUtil.formatDate(new Date(epochMillis));
		Assert.assertEquals(10, s.length());
	}
}
