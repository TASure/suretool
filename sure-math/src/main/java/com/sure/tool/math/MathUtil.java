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

import java.util.ArrayList;
import java.util.List;

/**
 * 基础数学工具门面。
 *
 * <p>覆盖素数判断与生成、最大公约数/最小公倍数、阶乘/斐波那契/组合/排列、
 * 进制转换与常用位运算。全部方法为无状态静态调用，线程安全。</p>
 *
 * @since 1.4.0
 */
public final class MathUtil {

	private MathUtil() {
	}

	/**
	 * 判断是否为素数。
	 *
	 * @param n 待判断整数（支持 long 范围）
	 * @return true 表示素数；2 及以下按约定返回 false
	 */
	public static boolean isPrime(long n) {
		if (n < 2) {
			return false;
		}
		if (n == 2 || n == 3) {
			return true;
		}
		if (n % 2 == 0 || n % 3 == 0) {
			return false;
		}
		for (long i = 5; i * i <= n; i += 6) {
			if (n % i == 0 || n % (i + 2) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 返回不小于 {@code n} 的最小素数。
	 *
	 * @param n 起始值
	 * @return 不小于 n 的最小素数
	 * @throws IllegalArgumentException n 小于 2
	 */
	public static long nextPrime(long n) {
		if (n < 2) {
			throw new IllegalArgumentException("n must be >= 2, got " + n);
		}
		long v = n;
		while (!isPrime(v)) {
			v++;
		}
		return v;
	}

	/**
	 * 使用埃氏筛生成 {@code [2, n]} 范围内的全部素数。
	 *
	 * @param n 上界（含）
	 * @return 素数列表（升序）
	 * @throws IllegalArgumentException n 小于 2
	 */
	public static List<Integer> primes(int n) {
		if (n < 2) {
			throw new IllegalArgumentException("n must be >= 2, got " + n);
		}
		boolean[] mark = new boolean[n + 1];
		List<Integer> result = new ArrayList<>();
		for (int i = 2; i <= n; i++) {
			if (!mark[i]) {
				result.add(i);
				for (long j = (long) i * i; j <= n; j += i) {
					mark[(int) j] = true;
				}
			}
		}
		return result;
	}

	/**
	 * 求两数最大公约数（欧几里得算法）。
	 *
	 * @param a 整数
	 * @param b 整数
	 * @return gcd(a, b)，恒为非负数
	 */
	public static int gcd(int a, int b) {
		a = Math.abs(a);
		b = Math.abs(b);
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/**
	 * 求一组整数（≥1 个）的最大公约数。
	 *
	 * @param values 整数序列，非空
	 * @return 全部数值的 gcd
	 * @throws IllegalArgumentException values 为空
	 */
	public static long gcd(long... values) {
		if (values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		long r = Math.abs(values[0]);
		for (int i = 1; i < values.length; i++) {
			r = gcdLong(r, values[i]);
		}
		return r;
	}

	private static long gcdLong(long a, long b) {
		a = Math.abs(a);
		b = Math.abs(b);
		while (b != 0) {
			long t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	/**
	 * 求两数最小公倍数。
	 *
	 * @param a 整数
	 * @param b 整数
	 * @return lcm(a, b)
	 * @throws ArithmeticException 结果溢出 long 范围
	 */
	public static long lcm(int a, int b) {
		if (a == 0 || b == 0) {
			return 0;
		}
		long g = gcd(a, b);
		long r = (long) a / g * b;
		if (r != (long) a / g * b) {
			throw new ArithmeticException("lcm overflow: " + a + ", " + b);
		}
		return Math.abs(r);
	}

	/**
	 * 求 n 的阶乘（long 范围，n ≤ 20）。
	 *
	 * @param n 非负整数
	 * @return n!
	 * @throws IllegalArgumentException n 为负或超过 20（long 溢出）
	 */
	public static long factorial(int n) {
		if (n < 0 || n > 20) {
			throw new IllegalArgumentException("n must be in [0, 20], got " + n);
		}
		long r = 1;
		for (int i = 2; i <= n; i++) {
			r *= i;
		}
		return r;
	}

	/**
	 * 求斐波那契数列第 n 项（long 范围，n ≤ 92）。
	 *
	 * @param n 非负索引
	 * @return fib(n)，fib(0)=0, fib(1)=1
	 * @throws IllegalArgumentException n 为负或超过 92（long 溢出）
	 */
	public static long fibonacci(int n) {
		if (n < 0 || n > 92) {
			throw new IllegalArgumentException("n must be in [0, 92], got " + n);
		}
		if (n == 0) {
			return 0;
		}
		long a = 1;
		long b = 1;
		for (int i = 3; i <= n; i++) {
			long t = a + b;
			a = b;
			b = t;
		}
		return b;
	}

	/**
	 * 求组合数 C(n, k)。
	 *
	 * @param n 总数（0 ≤ n ≤ 66，超出 long 范围抛异常）
	 * @param k 选取数（0 ≤ k ≤ n）
	 * @return C(n, k)
	 * @throws IllegalArgumentException 参数不合法
	 */
	public static long comb(int n, int k) {
		if (n < 0 || k < 0 || k > n) {
			throw new IllegalArgumentException("need 0 <= k <= n, got n=" + n + ", k=" + k);
		}
		k = Math.min(k, n - k);
		long r = 1;
		for (int i = 1; i <= k; i++) {
			r = r * (n - k + i) / i;
		}
		return r;
	}

	/**
	 * 求排列数 P(n, k)。
	 *
	 * @param n 总数
	 * @param k 排列数
	 * @return P(n, k)
	 * @throws IllegalArgumentException 参数不合法
	 */
	public static long perm(int n, int k) {
		if (n < 0 || k < 0 || k > n) {
			throw new IllegalArgumentException("need 0 <= k <= n, got n=" + n + ", k=" + k);
		}
		long r = 1;
		for (int i = 0; i < k; i++) {
			r *= (n - i);
		}
		return r;
	}

	/**
	 * 转二进制字符串。
	 *
	 * @param v 数值
	 * @return 二进制表示（不含前缀）
	 */
	public static String toBinary(long v) {
		return toBase(v, 2);
	}

	/**
	 * 转十六进制字符串（小写）。
	 *
	 * @param v 数值
	 * @return 十六进制表示（不含前缀）
	 */
	public static String toHex(long v) {
		return toBase(v, 16);
	}

	/**
	 * 转八进制字符串。
	 *
	 * @param v 数值
	 * @return 八进制表示（不含前缀）
	 */
	public static String toOctal(long v) {
		return toBase(v, 8);
	}

	/**
	 * 转任意基数（2-36）字符串，负数带负号。
	 *
	 * @param v     数值
	 * @param radix 基数（2-36）
	 * @return 对应进制字符串
	 * @throws IllegalArgumentException radix 不在 [2, 36]
	 */
	public static String toBase(long v, int radix) {
		if (radix < 2 || radix > 36) {
			throw new IllegalArgumentException("radix must be in [2, 36], got " + radix);
		}
		return Long.toString(v, radix);
	}

	/**
	 * 判断是否为 2 的幂。
	 *
	 * @param v 数值
	 * @return true 表示 v &gt; 0 且为 2 的幂
	 */
	public static boolean isPowerOfTwo(long v) {
		return v > 0 && (v & (v - 1)) == 0;
	}

	/**
	 * 返回不小于 {@code v} 的最小 2 的幂。
	 *
	 * @param v 正数
	 * @return 2 的幂（≥ v）
	 * @throws IllegalArgumentException v 小于等于 0
	 */
	public static long nextPowerOfTwo(long v) {
		if (v <= 0) {
			throw new IllegalArgumentException("v must be > 0, got " + v);
		}
		long r = 1;
		while (r < v) {
			r <<= 1;
		}
		return r;
	}
}
