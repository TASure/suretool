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
package com.sure.tool;

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第三十九批测试：原始摘要字节、快速 UUID。
 */
public class P4Features34Test {

	@Test
	public void testDigestBytes() {
		byte[] data = "suretool".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		Assert.assertEquals(16, com.sure.tool.codec.HashUtil.md5(data).length);
		Assert.assertEquals(20, com.sure.tool.codec.HashUtil.sha1(data).length);
		Assert.assertEquals(32, com.sure.tool.codec.HashUtil.sha256(data).length);
		Assert.assertEquals(64, com.sure.tool.codec.HashUtil.sha512(data).length);

		Assert.assertEquals(com.sure.tool.codec.HexUtil.encodeHexStr(com.sure.tool.codec.HashUtil.md5(data)),
				com.sure.tool.codec.HashUtil.md5Hex(data));
		Assert.assertEquals(com.sure.tool.codec.HexUtil.encodeHexStr(com.sure.tool.codec.HashUtil.sha256(data)),
				com.sure.tool.codec.HashUtil.sha256Hex(data));

		byte[] empty = new byte[0];
		Assert.assertEquals(16, com.sure.tool.codec.HashUtil.md5(empty).length);
	}

	@Test
	public void testFastSimpleUuid() {
		String uuid = com.sure.tool.util.IdUtil.fastSimpleUUID();
		Assert.assertEquals(32, uuid.length());
		Assert.assertFalse(uuid.contains("-"));
		Assert.assertNotEquals(uuid, com.sure.tool.util.IdUtil.fastSimpleUUID());
	}
}
