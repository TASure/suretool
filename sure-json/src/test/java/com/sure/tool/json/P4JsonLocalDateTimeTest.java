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
package com.sure.tool.json;

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第四十批测试：JSONObject/JSONArray LocalDate/LocalDateTime 取值。
 */
public class P4JsonLocalDateTimeTest {

	@Test
	public void testObjectLocalDate() {
		JSONObject obj = new JSONObject()
				.set("date", "2026-09-16")
				.set("datetime", "2026-09-16 18:30:00")
				.set("ts", 1789554600000L);

		Assert.assertEquals(java.time.LocalDate.of(2026, 9, 16), obj.getLocalDate("date"));
		Assert.assertEquals(java.time.LocalDateTime.of(2026, 9, 16, 18, 30, 0), obj.getLocalDateTime("datetime"));
		Assert.assertEquals(java.time.LocalDate.of(2026, 9, 16), obj.getLocalDate("ts"));
		Assert.assertNull(obj.getLocalDate("missing"));
		Assert.assertNull(obj.getLocalDateTime("bad"));
	}

	@Test
	public void testArrayLocalDate() {
		JSONArray arr = JSONArray.parse("[\"2026-09-16\", \"2026-09-16 18:30:00\"]");
		Assert.assertEquals(java.time.LocalDate.of(2026, 9, 16), arr.getLocalDate(0));
		Assert.assertEquals(java.time.LocalDateTime.of(2026, 9, 16, 18, 30, 0), arr.getLocalDateTime(1));
		Assert.assertNull(arr.getLocalDate(99));
		Assert.assertNull(arr.getLocalDateTime(-1));
	}
}
