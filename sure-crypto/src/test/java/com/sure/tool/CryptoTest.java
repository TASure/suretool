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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Test;

import com.sure.tool.codec.HexUtil;
import com.sure.tool.crypto.AesUtil;
import com.sure.tool.crypto.CryptoException;
import com.sure.tool.crypto.DesUtil;
import com.sure.tool.crypto.HmacUtil;
import com.sure.tool.crypto.RsaUtil;
import com.sure.tool.crypto.SecureUtil;

/**
 * 加密工具类测试：AES-GCM 随机 IV、RSA-OAEP 密钥校验、DES 兼容、HMAC RFC 向量、门面一致性。
 */
public class CryptoTest {

	@Test
	public void testAesRoundTrip() {
		String data = "hello aes 中文";
		String key = "my-secret-key";
		assertEquals(data, AesUtil.decryptHex(AesUtil.encryptHex(data, key), key));
		assertEquals(data, AesUtil.decryptBase64(AesUtil.encryptBase64(data, key), key));
	}

	@Test
	public void testAesWrongKeyFails() {
		String key1 = "key-one";
		String key2 = "key-two";
		String hex = AesUtil.encryptHex("secret", key1);
		try {
			AesUtil.decryptHex(hex, key2);
			fail("错误密钥应解密失败");
		} catch (CryptoException e) {
			// 预期
		}
	}

	@Test
	public void testAesDifferentKeysDifferentCipher() {
		String c1 = AesUtil.encryptHex("same", "key-a");
		String c2 = AesUtil.encryptHex("same", "key-b");
		assertNotEquals(c1, c2);
	}

	@Test
	public void testAesRandomIvPerEncrypt() {
		// GCM 随机 IV：同密钥同明文两次加密，密文必须不同
		String c1 = AesUtil.encryptHex("same", "key-a");
		String c2 = AesUtil.encryptHex("same", "key-a");
		assertNotEquals(c1, c2);
	}

	@Test
	public void testAesGenerateKey() {
		String key = AesUtil.generateKey();
		assertNotNull(key);
		String data = "round";
		assertEquals(data, AesUtil.decryptBase64(AesUtil.encryptBase64(data, key), key));
	}

	@Test
	public void testDesRoundTrip() {
		String key = "des-key";
		String data = "legacy system data";
		String hex = DesUtil.encryptHex(data, key);
		assertEquals(data, DesUtil.decryptHex(hex, key));

		String base64 = DesUtil.encryptBase64(data, key);
		assertEquals(data, DesUtil.decryptBase64(base64, key));
	}

	@Test
	public void testRsaRoundTripHex() {
		KeyPair keyPair = RsaUtil.generateKeyPair();
		String data = "rsa message 中文";
		String hex = RsaUtil.encryptHex(data, keyPair.getPublic());
		assertEquals(data, RsaUtil.decryptHex(hex, keyPair.getPrivate()));
	}

	@Test
	public void testRsaRoundTripBase64() {
		KeyPair keyPair = RsaUtil.generateKeyPair();
		String data = "rsa base64 message";
		String base64 = RsaUtil.encryptBase64(data, keyPair.getPublic());
		assertEquals(data, RsaUtil.decryptBase64(base64, keyPair.getPrivate()));
	}

	@Test
	public void testRsaKeySerialization() {
		KeyPair keyPair = RsaUtil.generateKeyPair();
		String publicKeyB64 = RsaUtil.getPublicKeyBase64(keyPair);
		String privateKeyB64 = RsaUtil.getPrivateKeyBase64(keyPair);

		PublicKey publicKey = RsaUtil.parsePublicKey(publicKeyB64);
		PrivateKey privateKey = RsaUtil.parsePrivateKey(privateKeyB64);

		String data = "serialized keys";
		String base64 = RsaUtil.encryptBase64(data, publicKey);
		assertEquals(data, RsaUtil.decryptBase64(base64, privateKey));
	}

	@Test
	public void testRsaKeySizeValidation() {
		try {
			RsaUtil.generateKeyPair(1024);
			fail("低于 2048 的密钥应被拒绝");
		} catch (IllegalArgumentException e) {
			// 预期
		}
	}

	@Test
	public void testRsaEncryptTooLong() {
		KeyPair keyPair = RsaUtil.generateKeyPair(2048);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			sb.append('a');
		}
		try {
			RsaUtil.encrypt(sb.toString().getBytes(StandardCharsets.UTF_8), keyPair.getPublic());
			fail("超过长度限制应失败");
		} catch (CryptoException e) {
			// 预期
		}
	}

	@Test
	public void testHmacSha256Rfc4231() {
		// RFC 4231 Test Case 1：key = 0x0b * 20，data = "Hi There"
		byte[] key = HexUtil.decodeHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
		byte[] data = "Hi There".getBytes(StandardCharsets.UTF_8);
		String mac = HexUtil.encodeHexStr(HmacUtil.hmac("HmacSHA256", data, key));
		assertEquals("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7", mac);
	}

	@Test
	public void testHmacSha1Rfc2202() {
		// RFC 2202 Test Case 1：key = 0x0b * 20，data = "Hi There"
		byte[] key = HexUtil.decodeHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
		byte[] data = "Hi There".getBytes(StandardCharsets.UTF_8);
		String mac = HexUtil.encodeHexStr(HmacUtil.hmac("HmacSHA1", data, key));
		assertEquals("b617318655057264e28bc0b6fb378c8ef146be00", mac);
	}

	@Test
	public void testHmacStringApi() {
		String mac1 = HmacUtil.hmacSha256Hex("data", "key");
		String mac2 = HmacUtil.hmacSha256Hex("data", "key");
		String mac3 = HmacUtil.hmacSha256Hex("data", "other-key");
		assertEquals(64, mac1.length());
		assertEquals(mac1, mac2);
		assertNotEquals(mac1, mac3);
		assertEquals(40, HmacUtil.hmacSha1Hex("data", "key").length());
		assertEquals(32, HmacUtil.hmacMd5Hex("data", "key").length());
		assertEquals(128, HmacUtil.hmacSha512Hex("data", "key").length());
	}

	@Test
	public void testSecureUtilFacade() {
		String data = "facade test 中文";
		String key = "facade-key";
		assertEquals(SecureUtil.aesDecryptHex(SecureUtil.aesEncryptHex(data, key), key), data);
		assertEquals(SecureUtil.aesDecryptBase64(SecureUtil.aesEncryptBase64(data, key), key), data);
		assertEquals(SecureUtil.desDecryptHex(SecureUtil.desEncryptHex(data, key), key), data);
		assertEquals(32, SecureUtil.md5("x").length());
		assertEquals(64, SecureUtil.sha256("x").length());
		assertEquals(64, SecureUtil.hmacSha256("x", "k").length());

		KeyPair keyPair = SecureUtil.rsaGenerateKeyPair();
		String enc = SecureUtil.rsaEncryptBase64(data, keyPair.getPublic());
		assertEquals(data, SecureUtil.rsaDecryptBase64(enc, keyPair.getPrivate()));
	}

	@Test
	public void testRandomSecret() {
		String secret = SecureUtil.randomSecret(16);
		assertEquals(24, secret.length());
		assertNotEquals(secret, SecureUtil.randomSecret(16));
	}

	@Test
	public void testAesKeyIsBase64() {
		String key = AesUtil.generateKey();
		assertTrue(com.sure.tool.codec.Base64Util.isBase64(key));
	}
}
