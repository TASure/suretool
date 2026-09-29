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
 * {@link MathUtil} 单元测试。
 */
public class MathUtilTest {

	@Test
	public void isPrimeBasics() {
		assertFalse(MathUtil.isPrime(0));
		assertFalse(MathUtil.isPrime(1));
		assertTrue(MathUtil.isPrime(2));
		assertTrue(MathUtil.isPrime(3));
		assertFalse(MathUtil.isPrime(4));
		assertTrue(MathUtil.isPrime(97));
		assertFalse(MathUtil.isPrime(100));
	}

	@Test
	public void nextPrime() {
		assertEquals(2L, MathUtil.nextPrime(2));
		assertEquals(5L, MathUtil.nextPrime(4));
		assertEquals(101L, MathUtil.nextPrime(100));
	}

	@Test
	public void primesSieve() {
		assertEquals(List.of(2, 3, 5, 7, 11, 13), MathUtil.primes(13));
		assertEquals(List.of(2, 3), MathUtil.primes(3));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.primes(1));
	}

	@Test
	public void gcdLcm() {
		assertEquals(6, MathUtil.gcd(54, 24));
		assertEquals(1, MathUtil.gcd(17, 13));
		assertEquals(6L, MathUtil.gcd(6L, 12L, 18L));
		assertEquals(12L, MathUtil.lcm(4, 6));
		assertEquals(0L, MathUtil.lcm(0, 5));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.gcd(new long[0]));
	}

	@Test
	public void factorialFibonacci() {
		assertEquals(1L, MathUtil.factorial(0));
		assertEquals(120L, MathUtil.factorial(5));
		assertEquals(2432902008176640000L, MathUtil.factorial(20));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.factorial(21));
		assertEquals(0L, MathUtil.fibonacci(0));
		assertEquals(1L, MathUtil.fibonacci(1));
		assertEquals(55L, MathUtil.fibonacci(10));
	}

	@Test
	public void combPerm() {
		assertEquals(10L, MathUtil.comb(5, 2));
		assertEquals(1L, MathUtil.comb(5, 0));
		assertEquals(20L, MathUtil.perm(5, 2));
		assertEquals(120L, MathUtil.perm(5, 5));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.comb(3, 4));
	}

	@Test
	public void baseConversion() {
		assertEquals("1010", MathUtil.toBinary(10));
		assertEquals("ff", MathUtil.toHex(255));
		assertEquals("17", MathUtil.toOctal(15));
		assertEquals("10", MathUtil.toBase(16, 16));
		assertEquals("-101", MathUtil.toBinary(-5));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.toBase(1, 37));
	}

	@Test
	public void powerOfTwo() {
		assertTrue(MathUtil.isPowerOfTwo(1));
		assertTrue(MathUtil.isPowerOfTwo(1024));
		assertFalse(MathUtil.isPowerOfTwo(0));
		assertFalse(MathUtil.isPowerOfTwo(6));
		assertEquals(8L, MathUtil.nextPowerOfTwo(7));
		assertEquals(1L, MathUtil.nextPowerOfTwo(1));
		assertThrows(IllegalArgumentException.class, () -> MathUtil.nextPowerOfTwo(0));
	}
}
