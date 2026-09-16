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
 * P4（v0.2.0）第四十一批测试：字符串完全一致判断、时间戳。
 */
public class P4Features36Test {

	@Test
	public void testIdentical() {
		Assert.assertEquals(1.0, com.sure.tool.text.StrSimilarity.identical("abc", "abc"), 1e-9);
		Assert.assertEquals(1.0, com.sure.tool.text.StrSimilarity.identical("", ""), 1e-9);
		Assert.assertTrue(com.sure.tool.text.StrSimilarity.identical("abc", "abd") < 1.0);
		Assert.assertEquals(0.0, com.sure.tool.text.StrSimilarity.identical("abc", "xyz"), 1e-9);
	}

	@Test
	public void testCurrentTimestamp() {
		long before = System.currentTimeMillis();
		long current = com.sure.tool.date.DateUtil.current();
		long after = System.currentTimeMillis();
		Assert.assertTrue(current >= before && current <= after);
		Assert.assertTrue(Math.abs(com.sure.tool.date.DateUtil.currentSeconds() - System.currentTimeMillis() / 1000) <= 1);
	}
}
