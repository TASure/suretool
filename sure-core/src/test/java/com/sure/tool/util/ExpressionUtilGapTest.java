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
 * ExpressionUtil 覆盖率补测：evalNumber 与表达式解析分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ExpressionUtilGapTest {

	@Test
	public void testEvalNumber() {
		Assert.assertEquals(0, ExpressionUtil.evalNumber("1+2*3", null).compareTo(new java.math.BigDecimal("7")));
		Assert.assertTrue(ExpressionUtil.check("1+2"));
		try {
			ExpressionUtil.eval("1++");
			Assert.fail("应抛异常");
		} catch (Exception e) {
			// expected
		}
	}

	@Test
	public void testMoreBranches() {
		Assert.assertEquals(0, ExpressionUtil.evalNumber("-5", null).compareTo(new java.math.BigDecimal("-5")));
		Assert.assertEquals(0, ExpressionUtil.evalNumber("2e3", null).compareTo(new java.math.BigDecimal("2000")));
		java.util.Map<String, Object> vars = new java.util.HashMap<>();
		vars.put("x", "42");
		Assert.assertEquals(0, ExpressionUtil.evalNumber("x+1", vars).compareTo(new java.math.BigDecimal("43")));
		try {
			ExpressionUtil.evalNumber("123abc", null);
			Assert.fail("应抛异常");
		} catch (Exception e) {
			// expected
		}
		try {
			ExpressionUtil.evalNumber("(1+2", null);
			Assert.fail("应抛异常");
		} catch (Exception e) {
			// expected
		}
	}
}
