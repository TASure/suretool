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
package com.sure.tool.codec;

import org.junit.Assert;
import org.junit.Test;

/**
 * HashUtil 覆盖率补测：null 守卫与 murmur 尾部分块（长度 1/2/3）分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class HashUtilGapTest {

	@Test
	public void testNullGuards() {
		Assert.assertEquals(0, HashUtil.murmur3_32((byte[]) null));
		Assert.assertEquals(0L, HashUtil.fnv1a64((String) null));
		Assert.assertEquals(0L, HashUtil.fnv1a64((byte[]) null));
	}

	@Test
	public void testMurmurRemainder() {
		Assert.assertNotEquals(0, HashUtil.murmur3_32(new byte[] {1}));
		Assert.assertNotEquals(0, HashUtil.murmur3_32(new byte[] {1, 2}));
		Assert.assertNotEquals(0, HashUtil.murmur3_32(new byte[] {1, 2, 3}));
		Assert.assertNotEquals(0, HashUtil.murmur3_32(new byte[] {1, 2, 3, 4, 5}));
		Assert.assertNotEquals(0L, HashUtil.fnv1a64("hello"));
	}
}
