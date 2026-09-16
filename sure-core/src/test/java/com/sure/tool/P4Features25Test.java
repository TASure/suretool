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
package com.sure.tool;

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第三十批测试。
 */
public class P4Features25Test {

	@Test
	public void testBatch30() {
		// IdcardUtil
		Assert.assertTrue(com.sure.tool.util.IdcardUtil.isValidCard("11010519900315021X"));
		Assert.assertTrue(com.sure.tool.util.IdcardUtil.isValidCard("110105200107070201"));
		Assert.assertFalse(com.sure.tool.util.IdcardUtil.isValidCard("12345"));
		Assert.assertFalse(com.sure.tool.util.IdcardUtil.isValidCard(null));
		Assert.assertEquals("1990-03-15", com.sure.tool.util.IdcardUtil.getBirthDate("11010519900315021X"));
		Assert.assertEquals("男", com.sure.tool.util.IdcardUtil.getGender("11010519900315021X"));
		Assert.assertEquals("女", com.sure.tool.util.IdcardUtil.getGender("110105200107070201"));
		Assert.assertNull(com.sure.tool.util.IdcardUtil.getBirthDate("12345"));
		Assert.assertNull(com.sure.tool.util.IdcardUtil.getGender(null));
		Assert.assertTrue(com.sure.tool.util.IdcardUtil.getAge("11010519900315021X") >= 36);

		// DateUtil.beginOfSecond / endOfSecond
		java.util.Date d = com.sure.tool.date.DateUtil.parse("2026-09-16 10:20:30", "yyyy-MM-dd HH:mm:ss");
		Assert.assertEquals("2026-09-16 10:20:30.000",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.beginOfSecond(d), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertEquals("2026-09-16 10:20:30.999",
				com.sure.tool.date.DateUtil.format(com.sure.tool.date.DateUtil.endOfSecond(d), "yyyy-MM-dd HH:mm:ss.SSS"));
		Assert.assertNull(com.sure.tool.date.DateUtil.beginOfSecond(null));
		Assert.assertNull(com.sure.tool.date.DateUtil.endOfSecond(null));
	}



}
