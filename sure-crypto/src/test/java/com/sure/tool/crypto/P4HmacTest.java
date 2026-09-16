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
package com.sure.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第十五批：HMAC-SHA384/512 补齐测试。
 */
public class P4HmacTest {

	@Test
	public void testHmac384() {
		String hex = HmacUtil.hmacSha384Hex("suretool", "key");
		Assert.assertNotNull(hex);
		Assert.assertEquals(96, hex.length()); // 384 bit = 96 hex chars
		Assert.assertEquals(hex, HmacUtil.hmacSha384Hex("suretool", "key"));

		String b64 = HmacUtil.hmacSha384Base64("suretool", "key");
		Assert.assertNotNull(b64);
		Assert.assertNotEquals(hex, b64);
	}

	@Test
	public void testHmac512Base64() {
		String b64 = HmacUtil.hmacSha512Base64("suretool", "key");
		Assert.assertNotNull(b64);
		Assert.assertEquals(88, b64.length()); // 512 bit = 64 bytes -> 88 base64 chars
	}
}
