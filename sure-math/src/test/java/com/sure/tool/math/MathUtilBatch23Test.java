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

import org.junit.Assert;
import org.junit.Test;

/**
 * MathUtil 批23 增强测试。
 */
public class MathUtilBatch23Test {

	/**
	 * clamp 各类型。
	 */
	@Test
	public void testClamp() {
		Assert.assertEquals(5, MathUtil.clamp(10, 0, 5));
		Assert.assertEquals(0, MathUtil.clamp(-3, 0, 5));
		Assert.assertEquals(3, MathUtil.clamp(3, 0, 5));
		Assert.assertEquals(5L, MathUtil.clamp(100L, 0L, 5L));
		Assert.assertEquals(2.5, MathUtil.clamp(2.5, 0.0, 5.0), 1e-9);
		Assert.assertEquals(5.0, MathUtil.clamp(9.9, 0.0, 5.0), 1e-9);
	}

	/**
	 * lerp 线性插值。
	 */
	@Test
	public void testLerp() {
		Assert.assertEquals(1.0, MathUtil.lerp(1.0, 3.0, 0.0), 1e-9);
		Assert.assertEquals(3.0, MathUtil.lerp(1.0, 3.0, 1.0), 1e-9);
		Assert.assertEquals(2.0, MathUtil.lerp(1.0, 3.0, 0.5), 1e-9);
	}

	/**
	 * 奇偶判断。
	 */
	@Test
	public void testEvenOdd() {
		Assert.assertTrue(MathUtil.isEven(4));
		Assert.assertFalse(MathUtil.isEven(3));
		Assert.assertTrue(MathUtil.isOdd(7));
		Assert.assertFalse(MathUtil.isOdd(8));
		Assert.assertTrue(MathUtil.isEven(0));
	}

	/**
	 * 均值。
	 */
	@Test
	public void testAverage() {
		Assert.assertEquals(2.0, MathUtil.average(new int[] { 1, 2, 3 }), 1e-9);
		Assert.assertEquals(0.0, MathUtil.average(new int[0]), 1e-9);
		Assert.assertEquals(2.0, MathUtil.average(new long[] { 1L, 2L, 3L }), 1e-9);
		Assert.assertEquals(2.0, MathUtil.average(new double[] { 1.0, 2.0, 3.0 }), 1e-9);
	}

	/**
	 * maxOf / minOf。
	 */
	@Test
	public void testMaxMin() {
		Assert.assertEquals(9L, MathUtil.maxOf(1L, 5L, 9L, 3L));
		Assert.assertEquals(1L, MathUtil.minOf(1L, 5L, 9L, 3L));
		Assert.assertEquals(0L, MathUtil.maxOf());
		Assert.assertEquals(0L, MathUtil.minOf());
	}
}
