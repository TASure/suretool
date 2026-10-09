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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Results 组合子批26 测试。
 */
public class ResultsTest {

	@Test
	public void testAllOfAllSuccess() {
		Result<List<Integer>> r = Results.allOf(Result.ok(1), Result.ok(2), Result.ok(3));
		Assert.assertTrue(r.isSuccess());
		Assert.assertEquals(List.of(1, 2, 3), r.get());
	}

	@Test
	public void testAllOfCarriesFirstFailure() {
		Result<List<Integer>> r = Results.allOf(Result.ok(1), Result.<Integer>fail("e2"), Result.<Integer>fail("e3"));
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals("e2", r.getErrorMessage());
	}

	@Test
	public void testAnyOfFirstSuccess() {
		Result<Integer> r = Results.anyOf(Result.<Integer>fail("e1"), Result.ok(2), Result.ok(3));
		Assert.assertTrue(r.isSuccess());
		Assert.assertEquals(Integer.valueOf(2), r.get());
	}

	@Test
	public void testAnyOfAllFailure() {
		Result<Integer> r = Results.anyOf(Result.<Integer>fail("e1"), Result.<Integer>fail("e2"));
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals("e2", r.getErrorMessage());
	}

	@Test
	public void testSequence() {
		Result<List<Integer>> r = Results.sequence(List.of(Result.ok(1), Result.ok(2)));
		Assert.assertEquals(List.of(1, 2), r.get());
		Result<List<Integer>> failed = Results.sequence(List.of(Result.ok(1), Result.<Integer>fail("x")));
		Assert.assertTrue(failed.isFailure());
		Assert.assertEquals("x", failed.getErrorMessage());
	}
}
