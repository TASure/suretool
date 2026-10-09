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
package com.sure.tool.lang;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Result 批26 测试。
 */
public class ResultTest {

	@Test
	public void testOkState() {
		Result<String> r = Result.ok("v");
		Assert.assertTrue(r.isSuccess());
		Assert.assertFalse(r.isFailure());
		Assert.assertEquals("v", r.get());
		Assert.assertEquals("v", r.getOrNull());
		Assert.assertNull(r.getErrorMessage());
		Assert.assertNull(r.getCause());
	}

	@Test
	public void testFailState() {
		Result<Integer> r = Result.fail("boom");
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals("boom", r.getErrorMessage());
		Assert.assertNull(r.getOrNull());
	}

	@Test
	public void testFailWithCause() {
		IllegalStateException e = new IllegalStateException("inner");
		Result<Integer> r = Result.fail(e);
		Assert.assertEquals("inner", r.getErrorMessage());
		Assert.assertSame(e, r.getCause());
	}

	@Test
	public void testGetThrowsOnFailure() {
		try {
			Result.fail("x").get();
			Assert.fail("应抛 NoSuchElementException");
		} catch (NoSuchElementException expected) {
			Assert.assertTrue(expected.getMessage().contains("x"));
		}
	}

	@Test
	public void testGetOrElse() {
		Assert.assertEquals("v", Result.ok("v").getOrElse("d"));
		Assert.assertEquals("d", Result.fail("e").getOrElse("d"));
		Assert.assertEquals("v", Result.ok("v").getOrElseGet(() -> "d"));
		Assert.assertEquals("d", Result.fail("e").getOrElseGet(() -> "d"));
	}

	@Test
	public void testMapShortCircuitsFailure() {
		Result<Integer> r = Result.<Integer>fail("e").map(x -> x + 1);
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals("e", r.getErrorMessage());
		Assert.assertEquals(Integer.valueOf(2), Result.ok(1).map(x -> x + 1).get());
	}

	@Test
	public void testMapCapturesMapperException() {
		Result<Integer> r = Result.ok(1).map(x -> {
			throw new IllegalStateException("mapper-fail");
		});
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals("mapper-fail", r.getCause().getMessage());
	}

	@Test
	public void testFlatMap() {
		Result<Integer> r = Result.ok(2).flatMap(x -> Result.ok(x * 10));
		Assert.assertEquals(Integer.valueOf(20), r.get());
		Result<Integer> failed = Result.ok(2).flatMap(x -> Result.fail("inner-fail"));
		Assert.assertEquals("inner-fail", failed.getErrorMessage());
	}

	@Test
	public void testOnSuccessOnFailureCallbacks() {
		AtomicInteger ok = new AtomicInteger();
		AtomicInteger bad = new AtomicInteger();
		Result.ok("v").onSuccess(x -> ok.incrementAndGet());
		Result.fail("e").onSuccess(x -> ok.incrementAndGet());
		Result.ok("v").onFailure(m -> bad.incrementAndGet());
		Result.fail("e").onFailure(m -> bad.incrementAndGet());
		Assert.assertEquals(1, ok.get());
		Assert.assertEquals(1, bad.get());
	}

	@Test
	public void testRecover() {
		Assert.assertEquals(Integer.valueOf(1), Result.<Integer>fail("e").recover(m -> 1).get());
		Assert.assertEquals(Integer.valueOf(9), Result.ok(9).recover(m -> 1).get());
	}

	@Test
	public void testThrowIfFailed() {
		Assert.assertEquals("v", Result.ok("v").throwIfFailed());
		try {
			Result.<String>fail("fatal").throwIfFailed();
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			Assert.assertEquals("fatal", e.getMessage());
		}
		try {
			Result.<String>fail(new ArithmeticException("arith")).throwIfFailed();
			Assert.fail("应抛异常");
		} catch (IllegalStateException e) {
			Assert.assertEquals(ArithmeticException.class, e.getCause().getClass());
		}
	}

	@Test
	public void testToOptional() {
		Assert.assertEquals(Optional.of("v"), Result.ok("v").toOptional());
		Assert.assertEquals(Optional.empty(), Result.fail("e").toOptional());
	}
}
