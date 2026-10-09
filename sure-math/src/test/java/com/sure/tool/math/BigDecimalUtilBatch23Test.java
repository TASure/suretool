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
package com.sure.tool.math;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * BigDecimalUtil 批23 增强测试。
 */
public class BigDecimalUtilBatch23Test {

	/**
	 * percent 百分比计算。
	 */
	@Test
	public void testPercent() {
		Assert.assertEquals(new BigDecimal("50.0"),
				BigDecimalUtil.percent(BigDecimal.valueOf(50), BigDecimal.valueOf(100), 1));
		Assert.assertEquals(new BigDecimal("33.33"),
				BigDecimalUtil.percent(BigDecimal.valueOf(1), BigDecimal.valueOf(3), 2));
		// 分母为 0 返回 0
		Assert.assertEquals(BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP),
				BigDecimalUtil.percent(BigDecimal.ONE, BigDecimal.ZERO, 2));
	}

	/**
	 * isZero 忽略精度。
	 */
	@Test
	public void testIsZero() {
		Assert.assertTrue(BigDecimalUtil.isZero(BigDecimal.ZERO));
		Assert.assertTrue(BigDecimalUtil.isZero(new BigDecimal("0.00")));
		Assert.assertTrue(BigDecimalUtil.isZero(null));
		Assert.assertFalse(BigDecimalUtil.isZero(BigDecimal.ONE));
	}

	/**
	 * compare 与 toPlainString。
	 */
	@Test
	public void testCompareAndPlain() {
		Assert.assertTrue(BigDecimalUtil.compare(BigDecimal.ONE, BigDecimal.TEN) < 0);
		Assert.assertTrue(BigDecimalUtil.compare(BigDecimal.TEN, BigDecimal.ONE) > 0);
		Assert.assertEquals(0, BigDecimalUtil.compare(BigDecimal.ONE, new BigDecimal("1.0")));
		Assert.assertEquals("0.0000000001", BigDecimalUtil.toPlainString(new BigDecimal("1E-10")));
		Assert.assertNull(BigDecimalUtil.toPlainString(null));
	}
}
