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
 * P4（v0.2.0）第三十四批测试：SecureUtil 字节数组摘要重载。
 */
public class P4HashBytesTest {

	@Test
	public void testByteArrayDigest() {
		byte[] data = "hello suretool".getBytes(java.nio.charset.StandardCharsets.UTF_8);

		Assert.assertEquals(SecureUtil.md5(new String(data)),
				SecureUtil.md5(data));
		Assert.assertEquals(SecureUtil.sha1(new String(data)),
				SecureUtil.sha1(data));
		Assert.assertEquals(SecureUtil.sha256(new String(data)),
				SecureUtil.sha256(data));
		Assert.assertEquals(SecureUtil.sha512(new String(data)),
				SecureUtil.sha512(data));

		Assert.assertEquals(32, SecureUtil.md5(data).length());
		Assert.assertEquals(40, SecureUtil.sha1(data).length());
		Assert.assertEquals(64, SecureUtil.sha256(data).length());
		Assert.assertEquals(128, SecureUtil.sha512(data).length());
	}

	@Test
	public void testByteArrayDigestStable() {
		byte[] empty = new byte[0];
		Assert.assertEquals(SecureUtil.md5(""), SecureUtil.md5(empty));
		Assert.assertEquals(SecureUtil.sha256(""), SecureUtil.sha256(empty));
	}
}
