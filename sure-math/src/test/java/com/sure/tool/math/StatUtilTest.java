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
 * StatUtil 基础统计测试。
 */
public class StatUtilTest {

	/**
	 * mean / sum。
	 */
	@Test
	public void testMeanAndSum() {
		Assert.assertEquals(3.0, StatUtil.mean(new double[] { 1, 2, 3, 4, 5 }), 1e-9);
		Assert.assertEquals(15.0, StatUtil.sum(new double[] { 1, 2, 3, 4, 5 }), 1e-9);
		Assert.assertEquals(0.0, StatUtil.mean(new double[0]), 1e-9);
	}

	/**
	 * median 奇偶样本。
	 */
	@Test
	public void testMedian() {
		Assert.assertEquals(3.0, StatUtil.median(new double[] { 5, 3, 1, 2, 4 }), 1e-9);
		Assert.assertEquals(2.5, StatUtil.median(new double[] { 1, 2, 3, 4 }), 1e-9);
	}

	/**
	 * variance / stdDev 样本方差。
	 */
	@Test
	public void testVarianceAndStdDev() {
		// 样本 {1,2,3,4,5}：均值 3，平方离差 10，n-1=4 → 2.5；标准差 √2.5
		Assert.assertEquals(2.5, StatUtil.variance(new double[] { 1, 2, 3, 4, 5 }), 1e-9);
		Assert.assertEquals(Math.sqrt(2.5), StatUtil.stdDev(new double[] { 1, 2, 3, 4, 5 }), 1e-9);
		// 少于 2 个样本返回 0
		Assert.assertEquals(0.0, StatUtil.variance(new double[] { 5 }), 1e-9);
	}

	/**
	 * min / max。
	 */
	@Test
	public void testMinMax() {
		Assert.assertEquals(1.0, StatUtil.min(new double[] { 5, 1, 9 }), 1e-9);
		Assert.assertEquals(9.0, StatUtil.max(new double[] { 5, 1, 9 }), 1e-9);
		Assert.assertEquals(0.0, StatUtil.min(new double[0]), 1e-9);
	}
}
