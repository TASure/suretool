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
 * HexUtil 覆盖率补测：编码重载、空白解码、非法字符、前缀判断。
 *
 * @author suretool
 * @since 1.13.1
 */
public class HexUtilGapTest {

	@Test
	public void testHex() {
		Assert.assertArrayEquals(new char[] {'4', '1'}, HexUtil.encodeHex(new byte[] {65}));
		Assert.assertEquals(0, HexUtil.decodeHex("  ").length);
		Assert.assertEquals(65, HexUtil.decodeHex("41")[0]);
		try {
			HexUtil.decodeHex("zz");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertFalse(HexUtil.isHexNumber("0x"));
		Assert.assertTrue(HexUtil.isHexNumber("0x1F"));
		Assert.assertFalse(HexUtil.isHexNumber(null));
	}
}
