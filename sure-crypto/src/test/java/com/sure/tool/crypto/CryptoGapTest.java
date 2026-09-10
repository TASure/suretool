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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.junit.Test;

/**
 * 加解密覆盖率补强：AesUtil/DesUtil/HmacUtil/RsaUtil/SecureUtil/CryptoException 行为路径。
 */
public class CryptoGapTest {

	@Test
	public void aes_roundtripHexAndBase64() {
		String key = AesUtil.generateKey();
		assertEquals(44, key.length());
		String hex = AesUtil.encryptHex("hello suretool", key);
		assertNotNull(hex);
		assertEquals("hello suretool", AesUtil.decryptHex(hex, key));
		String base64 = AesUtil.encryptBase64("hello suretool", key);
		assertNotNull(base64);
		assertEquals("hello suretool", AesUtil.decryptBase64(base64, key));
	}

	@Test
	public void aes_byteLevel() {
		byte[] data = "aes-bytes".getBytes(StandardCharsets.UTF_8);
		byte[] encrypted = AesUtil.encrypt(data, "any-length-key");
		assertTrue(encrypted.length > data.length);
		assertArrayEquals(data, AesUtil.decrypt(encrypted, "any-length-key"));
	}

	@Test
	public void aes_errors() {
		try {
			AesUtil.decryptHex("aabb", "key");
			fail("should throw");
		} catch (CryptoException expected) {
			// 预期行为
		}
		try {
			AesUtil.decrypt(new byte[4], "key");
			fail("should throw");
		} catch (CryptoException expected) {
			// 预期行为
		}
		try {
			AesUtil.decryptHex(AesUtil.encryptHex("secret", "key-a"), "key-b");
			fail("should throw");
		} catch (CryptoException expected) {
			// 预期行为
		}
	}

	@Test
	public void des_roundtrip() {
		String hex = DesUtil.encryptHex("legacy-data", "old-key");
		assertNotNull(hex);
		assertEquals("legacy-data", DesUtil.decryptHex(hex, "old-key"));
		String base64 = DesUtil.encryptBase64("legacy-data", "old-key");
		assertEquals("legacy-data", DesUtil.decryptBase64(base64, "old-key"));
	}

	@Test
	public void des_byteLevelAndError() {
		byte[] data = "des-bytes".getBytes(StandardCharsets.UTF_8);
		byte[] encrypted = DesUtil.encrypt(data, "key");
		assertArrayEquals(data, DesUtil.decrypt(encrypted, "key"));
		try {
			DesUtil.decrypt(new byte[]{1, 2, 3}, "wrong");
			fail("should throw");
		} catch (CryptoException expected) {
			// 预期行为
		}
	}

	@Test
	public void hmac_variants() {
		String md5 = HmacUtil.hmacMd5Hex("data", "key");
		String sha1 = HmacUtil.hmacSha1Hex("data", "key");
		String sha256 = HmacUtil.hmacSha256Hex("data", "key");
		String sha512 = HmacUtil.hmacSha512Hex("data", "key");
		assertEquals(32, md5.length());
		assertEquals(40, sha1.length());
		assertEquals(64, sha256.length());
		assertEquals(128, sha512.length());
		assertNotSame(md5, sha256);
		assertNotNull(HmacUtil.hmacSha256Base64("data", "key"));
		assertNotNull(HmacUtil.hmacSha1Base64("data", "key"));
		byte[] raw = HmacUtil.hmac("HmacSHA256", "data".getBytes(StandardCharsets.UTF_8),
				"key".getBytes(StandardCharsets.UTF_8));
		assertEquals(32, raw.length);
	}

	@Test
	public void hmac_unknownAlgorithm() {
		try {
			HmacUtil.hmac("HmacNoSuch", new byte[1], new byte[1]);
			fail("should throw");
		} catch (CryptoException expected) {
			// 预期行为
		}
	}

	@Test
	public void rsa_roundtrip() {
		KeyPair pair = RsaUtil.generateKeyPair();
		PublicKey publicKey = pair.getPublic();
		PrivateKey privateKey = pair.getPrivate();
		String hex = RsaUtil.encryptHex("rsa-hello", publicKey);
		assertEquals("rsa-hello", RsaUtil.decryptHex(hex, privateKey));
		String base64 = RsaUtil.encryptBase64("rsa-hello", publicKey);
		assertEquals("rsa-hello", RsaUtil.decryptBase64(base64, privateKey));
	}

	@Test
	public void rsa_byteLevelAndKeyParse() {
		KeyPair pair = RsaUtil.generateKeyPair();
		byte[] data = "rsa-bytes".getBytes(StandardCharsets.UTF_8);
		byte[] encrypted = RsaUtil.encrypt(data, pair.getPublic());
		assertArrayEquals(data, RsaUtil.decrypt(encrypted, pair.getPrivate()));
		String publicB64 = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
		String privateB64 = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
		PublicKey parsedPublic = RsaUtil.parsePublicKey(publicB64);
		PrivateKey parsedPrivate = RsaUtil.parsePrivateKey(privateB64);
		assertEquals(publicB64, Base64.getEncoder().encodeToString(parsedPublic.getEncoded()));
		assertEquals(privateB64, Base64.getEncoder().encodeToString(parsedPrivate.getEncoded()));
	}

	@Test
	public void rsa_errors() {
		try {
			RsaUtil.generateKeyPair(1024);
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			RsaUtil.parsePublicKey("not-a-key");
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
		try {
			RsaUtil.parsePrivateKey("not-a-key");
			fail("should throw");
		} catch (IllegalArgumentException expected) {
			// 预期行为
		}
	}

	@Test
	public void secureUtil_hashesAndAes() {
		assertEquals(32, SecureUtil.md5("data").length());
		assertEquals(40, SecureUtil.sha1("data").length());
		assertEquals(64, SecureUtil.sha256("data").length());
		assertEquals(128, SecureUtil.sha512("data").length());
		String hex = SecureUtil.aesEncryptHex("secret", "key");
		assertEquals("secret", SecureUtil.aesDecryptHex(hex, "key"));
		String base64 = SecureUtil.aesEncryptBase64("secret", "key");
		assertEquals("secret", SecureUtil.aesDecryptBase64(base64, "key"));
	}

	@Test
	public void secureUtil_desAndHmacAndRsa() {
		String desHex = SecureUtil.desEncryptHex("legacy", "key");
		assertEquals("legacy", SecureUtil.desDecryptHex(desHex, "key"));
		assertNotNull(SecureUtil.hmacSha256("data", "key"));
		assertNotNull(SecureUtil.hmacSha1("data", "key"));
		KeyPair pair = SecureUtil.rsaGenerateKeyPair();
		String encrypted = SecureUtil.rsaEncryptBase64("data", pair.getPublic());
		assertEquals("data", SecureUtil.rsaDecryptBase64(encrypted, pair.getPrivate()));
		String secret = SecureUtil.randomSecret(16);
		assertEquals(16, Base64.getDecoder().decode(secret).length);
		assertEquals("", SecureUtil.randomSecret(0));
	}

	@Test
	public void cryptoException_constructors() {
		CryptoException message = new CryptoException("boom");
		assertEquals("boom", message.getMessage());
		IllegalStateException cause = new IllegalStateException("cause");
		CryptoException withCause = new CryptoException("boom", cause);
		assertEquals("boom", withCause.getMessage());
		assertEquals(cause, withCause.getCause());
		assertFalse(withCause.getCause() == null);
		assertTrue(withCause.getCause() instanceof IllegalStateException);
	}
}
