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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * RetryUtil 覆盖率补测：参数校验与重试直到满足条件分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class RetryUtilGapTest {

	@Test
	public void testValidation() {
		try {
			RetryUtil.retry(null, 1, Duration.ofMillis(1), true);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		} catch (Exception e) {
			Assert.fail("不应抛 " + e);
		}
		try {
			RetryUtil.retryUntil(() -> 1, x -> true, 0, Duration.ofMillis(1));
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		} catch (Exception e) {
			Assert.fail("不应抛 " + e);
		}
	}

	@Test
	public void testRetryUntil() throws Exception {
		AtomicInteger count = new AtomicInteger();
		Integer r = RetryUtil.retryUntil(count::incrementAndGet, x -> x >= 3, 5, Duration.ofMillis(1));
		Assert.assertEquals(Integer.valueOf(3), r);
		// 永不满足 -> 返回最后一次结果
		Integer last = RetryUtil.retryUntil(() -> 1, x -> false, 2, Duration.ofMillis(1));
		Assert.assertEquals(Integer.valueOf(1), last);
		Assert.assertTrue(RetryUtil.retry(() -> Boolean.TRUE, 1, Duration.ofMillis(1), true));
		Assert.assertFalse(RetryUtil.retry(() -> Boolean.FALSE, 1, Duration.ofMillis(1), false));
	}
}
