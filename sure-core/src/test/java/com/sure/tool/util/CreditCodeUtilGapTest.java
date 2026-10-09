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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * CreditCodeUtil 覆盖率补测：非法字符分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class CreditCodeUtilGapTest {

	@Test
	public void testInvalidChar() {
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode("91350100M8K4HX!24X"));
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode(null));
	}
}
