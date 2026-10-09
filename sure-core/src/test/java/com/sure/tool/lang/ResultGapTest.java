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

import org.junit.Assert;
import org.junit.Test;

/**
 * Result 覆盖率补测：flatMap/recover 失败路径与 toString 分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ResultGapTest {

	@Test
	public void testFlatMapFailurePropagation() {
		Result<Integer> failed = Result.fail("boom");
		Result<String> r = failed.flatMap(v -> Result.ok("x"));
		Assert.assertTrue(r.isFailure());
		// mapper 抛异常
		Result<Integer> ok = Result.ok(1);
		Result<String> r2 = ok.flatMap(v -> {
			throw new IllegalStateException("mapper err");
		});
		Assert.assertTrue(r2.isFailure());
		Assert.assertEquals("Result.ok(1)", ok.toString());
		Assert.assertEquals("Result.fail(boom)", failed.toString());
	}

	@Test
	public void testRecover() {
		Result<Integer> failed = Result.fail("err");
		Assert.assertEquals(Integer.valueOf(0), failed.recover(e -> 0).throwIfFailed());
		Result<Integer> bad = Result.fail("err");
		Result<Integer> r = bad.recover(e -> {
			throw new IllegalStateException("rec err");
		});
		Assert.assertTrue(r.isFailure());
		Assert.assertEquals(Integer.valueOf(5), Result.ok(5).recover(e -> 0).throwIfFailed());
	}
}
