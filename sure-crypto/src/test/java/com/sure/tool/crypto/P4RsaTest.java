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

import java.security.KeyPair;

/**
 * P4（v0.2.0）第十四批：RSA 签名验签测试。
 */
public class P4RsaTest {

	@Test
	public void testSignVerify() {
		KeyPair keyPair = RsaUtil.generateKeyPair();
		String data = "suretool 签名测试";
		String signature = RsaUtil.sign(data, keyPair.getPrivate());
		Assert.assertNotNull(signature);
		Assert.assertTrue(RsaUtil.verify(data, keyPair.getPublic(), signature));
		Assert.assertFalse(RsaUtil.verify("篡改内容", keyPair.getPublic(), signature));

		// SecureUtil 便捷入口
		String sig2 = SecureUtil.rsaSign(data, keyPair.getPrivate());
		Assert.assertTrue(SecureUtil.rsaVerify(data, keyPair.getPublic(), sig2));
	}

	@Test
	public void testSignHex() {
		java.security.KeyPair pair = com.sure.tool.crypto.RsaUtil.generateKeyPair();
		String sign = com.sure.tool.crypto.RsaUtil.signHex("hello", pair.getPrivate());
		Assert.assertTrue(com.sure.tool.crypto.RsaUtil.verifyHex("hello", pair.getPublic(), sign));
		Assert.assertFalse(com.sure.tool.crypto.RsaUtil.verifyHex("world", pair.getPublic(), sign));
		Assert.assertFalse(com.sure.tool.crypto.RsaUtil.verifyHex("hello", pair.getPublic(), "not-hex"));
	}

	@Test
	public void testDesGenerateKey() {
		String key = com.sure.tool.crypto.DesUtil.generateKey();
		Assert.assertEquals(16, key.length());
		String data = "suretool";
		String enc = com.sure.tool.crypto.DesUtil.encryptHex(data, key);
		Assert.assertEquals(data, com.sure.tool.crypto.DesUtil.decryptHex(enc, key));
	}


}
