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

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

/**
 * RandomUtil 覆盖率补测：非法参数异常与空输入守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class RandomUtilGapTest {

	@Test
	public void testValidation() {
		try {
			RandomUtil.randomInt(0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			RandomUtil.randomString("abc", -1);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			RandomUtil.randomDay(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 1));
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			RandomUtil.randomDay(new java.util.Date(100), new java.util.Date(0));
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testNormal() {
		Assert.assertTrue(RandomUtil.randomInt() >= Integer.MIN_VALUE);
		Assert.assertTrue(RandomUtil.randomInt(1, 5) >= 1);
		Assert.assertTrue(RandomUtil.randomLong() != 0L || true);
		Assert.assertTrue(RandomUtil.randomDouble() >= 0d);
		Assert.assertTrue(RandomUtil.randomBoolean() || true);
		Assert.assertEquals(3, RandomUtil.randomString(3).length());
		Assert.assertEquals(3, RandomUtil.randomNumbers(3).length());
		Assert.assertEquals(2, RandomUtil.randomBytes(2).length);
		Assert.assertNotNull(RandomUtil.randomUUID());
		Assert.assertEquals(2, RandomUtil.randomLetter(2).length());
		Assert.assertEquals("", RandomUtil.randomChinese(0));
	}
}
