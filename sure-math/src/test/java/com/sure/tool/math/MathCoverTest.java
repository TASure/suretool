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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

/**
 * math 工具覆盖率二轮补强：参数非法、空集合、边界与异常分支。
 */
public class MathCoverTest {

	// ---------- RandomUtil ----------

	@Test
	public void randomInt_minGreaterMax() {
		try {
			RandomUtil.randomInt(5, 1);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void randomLong_minGreaterMax() {
		try {
			RandomUtil.randomLong(5L, 1L);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void randomDouble_minGreaterMax() {
		try {
			RandomUtil.randomDouble(5.0, 1.0);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void randomString_negativeLength() {
		try {
			RandomUtil.randomString(-1, "abc");
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void randomNumbers_negativeLength() {
		try {
			RandomUtil.randomNumbers(-2);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void randomElement_emptyVarargs() {
		try {
			RandomUtil.randomElement();
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void shuffle_nullList() {
		try {
			RandomUtil.shuffle(null);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void secureRandomInt_minGreaterMax() {
		try {
			RandomUtil.secureRandomInt(9, 3);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void secureRandomBytes_negativeLength() {
		try {
			RandomUtil.secureRandomBytes(-1);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void secureRandomString_negativeLength() {
		try {
			RandomUtil.secureRandomString(-1);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	// ---------- NumberUtil ----------

	@Test
	public void parseLong_badInput() {
		assertEquals(-1L, NumberUtil.parseLong("not-a-number", -1L));
	}

	@Test
	public void parseDouble_blankInput() {
		assertEquals(1.5, NumberUtil.parseDouble("   ", 1.5), 0.0);
	}

	@Test
	public void isLong_badInput() {
		assertFalse(NumberUtil.isLong("12.5"));
	}

	@Test
	public void isDouble_badInput() {
		assertFalse(NumberUtil.isDouble("abc"));
	}

	@Test
	public void min_empty() {
		try {
			NumberUtil.min();
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	// ---------- MathUtil ----------

	@Test
	public void isPrime_evenComposite() {
		assertFalse(MathUtil.isPrime(4));
		assertFalse(MathUtil.isPrime(25));
	}

	@Test
	public void nextPrime_belowTwo() {
		try {
			MathUtil.nextPrime(1);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void factorial_outOfRange() {
		try {
			MathUtil.factorial(21);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void comb_kGreaterThanN() {
		try {
			MathUtil.comb(5, 6);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void average_emptyArrays() {
		assertEquals(0.0, MathUtil.average(new long[0]), 0.0);
		assertEquals(0.0, MathUtil.average(new double[0]), 0.0);
	}

	// ---------- StatUtil ----------

	@Test
	public void stat_emptyInputs() {
		assertEquals(0.0, StatUtil.median(new double[0]), 0.0);
		assertEquals(0.0, StatUtil.max(new double[0]), 0.0);
		assertEquals(0.0, StatUtil.sum(new double[0]), 0.0);
	}

	// ---------- BigDecimalUtil ----------

	@Test
	public void div_nullMode() {
		try {
			BigDecimalUtil.div(BigDecimal.ONE, BigDecimal.ONE, 2, null);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void round_nullMode() {
		try {
			BigDecimalUtil.round(BigDecimal.ONE, 2, null);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void compare_nullArg() {
		try {
			BigDecimalUtil.compare(null, BigDecimal.ONE);
			fail("应抛异常");
		} catch (IllegalArgumentException expected) {
			// 预期
		}
	}

	@Test
	public void sanity_misc() {
		assertTrue(NumberUtil.isNumber("3.14"));
		assertEquals(1, NumberUtil.min(3, 9, 1));
		assertEquals(6L, MathUtil.factorial(3));
		assertEquals(10L, MathUtil.comb(5, 2));
		assertEquals(6.0, StatUtil.sum(new double[] { 1, 2, 3 }), 0.0);
		assertEquals(RoundingMode.HALF_UP, RoundingMode.HALF_UP);
	}
}
