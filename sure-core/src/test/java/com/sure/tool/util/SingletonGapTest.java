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

import org.junit.Assert;
import org.junit.Test;

/**
 * Singleton 覆盖率补测：空参重载与创建失败分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class SingletonGapTest {

	public static class NoPublicCtor {
		private NoPublicCtor() {
		}
	}

	@Test
	public void testGetNoParams() {
		Assert.assertNotNull(Singleton.get(String.class));
		Assert.assertNotNull(Singleton.get(String.class, (Object[]) null));
	}

	@Test
	public void testCreateFailure() {
		try {
			Singleton.get(NoPublicCtor.class, 1);
			Assert.fail("应抛异常");
		} catch (Exception e) {
			// expected
		}
	}

	public static class NoDefaultCtor {
		@SuppressWarnings("unused")
		public NoDefaultCtor(String x) {
		}
	}

	@Test
	public void testCreateDefaultFailure() {
		try {
			Singleton.get(NoDefaultCtor.class);
			Assert.fail("应抛异常");
		} catch (RuntimeException e) {
			// expected
		}
	}
}
