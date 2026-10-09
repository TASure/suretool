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
 * BytesUtil 覆盖率补测：长度不足异常与 null/空切片守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class BytesUtilGapTest {

	@Test
	public void testTooShort() {
		try {
			BytesUtil.bytesToLong(new byte[4]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			BytesUtil.bytesToInt(new byte[2]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			BytesUtil.bytesToShort(new byte[1]);
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testSliceReverseNull() {
		Assert.assertNull(BytesUtil.slice(null, 0, 1));
		Assert.assertEquals(0, BytesUtil.slice(new byte[] {1}, 5, 1).length);
		Assert.assertNull(BytesUtil.reverse(null));
		Assert.assertArrayEquals(new byte[] {3, 2, 1}, BytesUtil.reverse(new byte[] {1, 2, 3}));
		Assert.assertEquals(1L, BytesUtil.bytesToLong(BytesUtil.longToBytes(1L)));
	}
}
