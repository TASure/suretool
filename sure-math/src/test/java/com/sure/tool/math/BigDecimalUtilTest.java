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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

/**
 * {@link BigDecimalUtil} 单元测试。
 */
public class BigDecimalUtilTest {

	@Test
	public void basicArithmetic() {
		assertEquals(new BigDecimal("3"), BigDecimalUtil.add(new BigDecimal("1"), new BigDecimal("2")));
		assertEquals(new BigDecimal("-1"), BigDecimalUtil.sub(new BigDecimal("1"), new BigDecimal("2")));
		assertEquals(new BigDecimal("6"), BigDecimalUtil.mul(new BigDecimal("2"), new BigDecimal("3")));
	}

	@Test
	public void divisionWithDefaultScale() {
		BigDecimal r = BigDecimalUtil.div(new BigDecimal("1"), new BigDecimal("3"));
		assertEquals(10, r.scale());
		assertEquals(new BigDecimal("0.3333333333"), r);
	}

	@Test
	public void divisionWithCustomScale() {
		assertEquals(new BigDecimal("0.33"), BigDecimalUtil.div(new BigDecimal("1"), new BigDecimal("3"), 2));
		assertEquals(new BigDecimal("0.33"), BigDecimalUtil.div(new BigDecimal("1"), new BigDecimal("3"), 2, RoundingMode.HALF_UP));
		assertThrows(ArithmeticException.class, () -> BigDecimalUtil.div(new BigDecimal("1"), new BigDecimal("0"), 2));
		assertThrows(IllegalArgumentException.class, () -> BigDecimalUtil.div(new BigDecimal("1"), new BigDecimal("3"), -1));
	}

	@Test
	public void rounding() {
		assertEquals(new BigDecimal("1.24"), BigDecimalUtil.roundHalfUp(new BigDecimal("1.235"), 2));
		assertEquals(new BigDecimal("1.24"), BigDecimalUtil.round(new BigDecimal("1.235"), 2, RoundingMode.HALF_UP));
		assertEquals(new BigDecimal("1.23"), BigDecimalUtil.round(new BigDecimal("1.235"), 2, RoundingMode.DOWN));
		assertThrows(IllegalArgumentException.class, () -> BigDecimalUtil.round(null, 2, RoundingMode.UP));
	}

	@Test
	public void equalsIgnoreTrailingZeros() {
		assertTrue(BigDecimalUtil.equals(new BigDecimal("1.0"), new BigDecimal("1.00")));
		assertTrue(BigDecimalUtil.equals(new BigDecimal("0.5"), new BigDecimal("0.50")));
		assertFalse(BigDecimalUtil.equals(new BigDecimal("1.0"), new BigDecimal("1.1")));
		assertTrue(BigDecimalUtil.equals(null, null));
		assertFalse(BigDecimalUtil.equals(new BigDecimal("1"), null));
	}

	@Test
	public void parseAndIsNumber() {
		assertTrue(BigDecimalUtil.isNumber("123.45"));
		assertTrue(BigDecimalUtil.isNumber("-0.001"));
		assertFalse(BigDecimalUtil.isNumber("abc"));
		assertFalse(BigDecimalUtil.isNumber(""));
		assertFalse(BigDecimalUtil.isNumber(null));
		assertEquals(new BigDecimal("1.5"), BigDecimalUtil.toBigDecimal("1.5"));
		assertNull(BigDecimalUtil.toBigDecimal("  "));
		assertNull(BigDecimalUtil.toBigDecimal(null));
		assertThrows(NumberFormatException.class, () -> BigDecimalUtil.toBigDecimal("abc"));
	}

	@Test
	public void nullValidation() {
		assertThrows(IllegalArgumentException.class, () -> BigDecimalUtil.add(null, new BigDecimal("1")));
		assertThrows(IllegalArgumentException.class, () -> BigDecimalUtil.sub(new BigDecimal("1"), null));
	}
}
