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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * {@link RandomUtil} 与 {@link NumberUtil} 单元测试。
 */
public class RandomUtilTest {

	@Test
	public void randomIntInRange() {
		for (int i = 0; i < 100; i++) {
			int v = RandomUtil.randomInt(3, 5);
			assertTrue(v >= 3 && v <= 5);
		}
	}

	@Test
	public void randomLongInRange() {
		long v = RandomUtil.randomLong(10L, 20L);
		assertTrue(v >= 10L && v <= 20L);
	}

	@Test
	public void randomDoubleInRange() {
		double v = RandomUtil.randomDouble(0.0, 1.0);
		assertTrue(v >= 0.0 && v < 1.0);
	}

	@Test
	public void randomStringLength() {
		assertEquals(8, RandomUtil.randomString(8).length());
		assertEquals(0, RandomUtil.randomString(0).length());
		assertEquals(4, RandomUtil.randomNumbers(4).length());
		assertThrows(IllegalArgumentException.class, () -> RandomUtil.randomString(-1));
		assertThrows(IllegalArgumentException.class, () -> RandomUtil.randomString(5, ""));
	}

	@Test
	public void randomBytesAndElement() {
		assertEquals(16, RandomUtil.randomBytes(16).length);
		List<String> list = List.of("a", "b", "c");
		assertTrue(list.contains(RandomUtil.randomElement(list)));
		assertTrue(list.contains(RandomUtil.randomElement("a", "b", "c")));
		assertThrows(IllegalArgumentException.class, () -> RandomUtil.randomElement(List.of()));
	}

	@Test
	public void shufflePreservesElements() {
		List<Integer> l = new java.util.ArrayList<>(List.of(1, 2, 3, 4, 5));
		RandomUtil.shuffle(l);
		assertEquals(5, l.size());
		assertTrue(l.containsAll(List.of(1, 2, 3, 4, 5)));
	}

	@Test
	public void secureRandom() {
		assertTrue(RandomUtil.secureRandomInt(0, 1) >= 0 && RandomUtil.secureRandomInt(0, 1) <= 1);
		assertEquals(8, RandomUtil.secureRandomString(8).length());
		assertEquals(8, RandomUtil.secureRandomBytes(8).length);
	}

	@Test
	public void numberUtilParse() {
		assertEquals(42, NumberUtil.parseInt("42", -1));
		assertEquals(-1, NumberUtil.parseInt("abc", -1));
		assertEquals(-1, NumberUtil.parseInt(null, -1));
		assertEquals(42L, NumberUtil.parseLong("42", -1L));
		assertEquals(1.5d, NumberUtil.parseDouble("1.5", 0d), 1e-9);
		assertEquals(0d, NumberUtil.parseDouble("x", 0d), 1e-9);
	}

	@Test
	public void numberUtilChecks() {
		assertTrue(NumberUtil.isInteger("42"));
		assertFalse(NumberUtil.isInteger("4.2"));
		assertTrue(NumberUtil.isLong("9223372036854775807"));
		assertTrue(NumberUtil.isDouble("1e3"));
		assertTrue(NumberUtil.isNumber("42"));
		assertFalse(NumberUtil.isNumber(null));
		assertEquals(0, NumberUtil.toInt("abc"));
		assertEquals(0L, NumberUtil.toLong(""));
		assertEquals(1, NumberUtil.min(3, 1, 2));
		assertEquals(9, NumberUtil.max(3, 9, 2));
		assertThrows(IllegalArgumentException.class, () -> NumberUtil.min(new int[0]));
	}
}
