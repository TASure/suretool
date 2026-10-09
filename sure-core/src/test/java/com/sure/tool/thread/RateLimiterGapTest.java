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
package com.sure.tool.thread;

import org.junit.Assert;
import org.junit.Test;

/**
 * RateLimiter 覆盖率补测：容量/许可参数校验与无参 acquire。
 *
 * @author suretool
 * @since 1.13.1
 */
public class RateLimiterGapTest {

	@Test
	public void testValidation() throws Exception {
		try {
			new RateLimiter(1, 0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		RateLimiter rl = new RateLimiter(1, 100);
		Assert.assertTrue(rl.tryAcquire());
		rl.acquire();
		try {
			rl.acquire(0);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
