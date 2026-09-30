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
package com.sure.tool.example;

import java.math.BigDecimal;

import com.sure.tool.math.BigDecimalUtil;
import com.sure.tool.math.MathUtil;
import com.sure.tool.math.RandomUtil;

/**
 * 数学/数值/随机工具示例（sure-math）。
 */
public class MathDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== MathDemo ===");
		System.out.println("isPrime(104729) = " + MathUtil.isPrime(104729));
		System.out.println("gcd(1071, 462) = " + MathUtil.gcd(1071, 462));
		System.out.println("primes(100).size = " + MathUtil.primes(100).size());
		BigDecimal sum = BigDecimalUtil.add(new BigDecimal("0.1"), new BigDecimal("0.2"));
		System.out.println("0.1 + 0.2 = " + sum);
		System.out.println("randomString(12) = " + RandomUtil.randomString(12));
	}
}
