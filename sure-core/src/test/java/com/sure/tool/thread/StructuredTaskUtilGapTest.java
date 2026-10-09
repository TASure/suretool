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

import java.time.Duration;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * StructuredTaskUtil 覆盖率补测：中断分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class StructuredTaskUtilGapTest {

	@Test
	public void testInterruptedBranches() {
		Thread.currentThread().interrupt();
		try {
			StructuredTaskUtil.parallel(Arrays.asList(() -> {
				Thread.sleep(5000);
				return 1;
			}));
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			// expected
		} finally {
			Thread.interrupted();
		}
		Thread.currentThread().interrupt();
		try {
			StructuredTaskUtil.anyOf(() -> 1);
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			// expected
		} finally {
			Thread.interrupted();
		}
	}

	@Test
	public void testTimeout() {
		try {
			StructuredTaskUtil.parallel(Duration.ofMillis(1), () -> {
				Thread.sleep(5000);
				return 1;
			});
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			// expected
		}
	}
}
