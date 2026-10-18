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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyPair;

/**
 * 批46：默认安全基线防回归审计测试。
 *
 * <p>审计 sure-crypto 全部加密/哈希/令牌 API 的默认安全值：
 * AES-GCM 认证加密 + 随机 IV、RSA OAEP-SHA256 + 最小 2048 位、BCrypt 默认 cost=10 且拒绝超长密码、
 * 弱参数拒绝、安全指南文档存在且承诺明确。任何回归都会使本类失败。</p>
 */
public class SecurityBaselineTest {

	@Test
	public void testAesDefaultAuthenticatedEncryption() {
		// AES/GCM/NoPadding：密文 = 12 字节 IV + GCM 密文+tag，解密自动解析
		String key = AesUtil.generateKey();
		String plain = "默认安全基线测试-认证加密";
		String cipherBase64 = AesUtil.encryptBase64(plain, key);
		String cipherHex = AesUtil.encryptHex(plain, key);
		// 密文含 IV 头，长度大于明文
		byte[] cipherBytes = java.util.Base64.getDecoder().decode(cipherBase64);
		Assert.assertTrue("GCM 密文含 12 字节 IV 头", cipherBytes.length > plain.getBytes(StandardCharsets.UTF_8).length + 12);
		Assert.assertEquals("解密往返", plain, AesUtil.decryptBase64(cipherBase64, key));
		Assert.assertEquals("Hex 往返", plain, AesUtil.decryptHex(cipherHex, key));
	}

	@Test
	public void testAesRandomIvPerEncryption() {
		// 同明文同密钥两次加密结果不同 → IV 随机
		String key = AesUtil.generateKey();
		String plain = "random-iv-check";
		String c1 = AesUtil.encryptBase64(plain, key);
		String c2 = AesUtil.encryptBase64(plain, key);
		Assert.assertNotEquals("IV 必须每次随机", c1, c2);
	}

	@Test
	public void testRsaDefaultOaepRoundtrip() {
		KeyPair pair = RsaUtil.generateKeyPair();
		byte[] data = "RSA-OAEP-SHA256 基线".getBytes(StandardCharsets.UTF_8);
		byte[] cipher = RsaUtil.encrypt(data, pair.getPublic());
		byte[] back = RsaUtil.decrypt(cipher, pair.getPrivate());
		Assert.assertArrayEquals("OAEP 往返", data, back);
	}

	@Test
	public void testRsaMinKeySizeEnforced() {
		Assert.assertEquals("最小密钥 2048 位", 2048, RsaUtil.MIN_KEY_SIZE);
		try {
			RsaUtil.generateKeyPair(1024);
			Assert.fail("1024 位弱密钥必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testBCryptDefaultCost() {
		Assert.assertEquals("默认 cost=10", 10, BCryptUtil.DEFAULT_COST);
		Assert.assertTrue(BCryptUtil.hash("baseline-password").startsWith("$2a$10$"));
	}

	@Test
	public void testBCryptRejectsTruncation() {
		// 与 Hutool 静默截断不同：超过 72 字节必须显式拒绝
		try {
			BCryptUtil.hash("b".repeat(73));
			Assert.fail("73 字节密码必须拒绝（禁止静默截断）");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
	}

	@Test
	public void testSecurityGuideDocumented() {
		// 安全指南必须存在于仓库 docs/ 下且包含默认安全承诺（surefire 工作目录为模块目录，向上探测仓库根）
		File guide = null;
		for (String candidate : new String[]{"../docs/security-guide.md", "docs/security-guide.md", "../../docs/security-guide.md"}) {
			File f = new File(candidate);
			if (f.isFile()) {
				guide = f;
				break;
			}
		}
		Assert.assertNotNull("docs/security-guide.md 必须存在", guide);
		try {
			String content = new String(Files.readAllBytes(guide.toPath()), StandardCharsets.UTF_8);
			Assert.assertTrue("指南须包含 AES-GCM 承诺", content.contains("AES-GCM") || content.contains("GCM"));
			Assert.assertTrue("指南须包含 BCrypt 承诺", content.contains("BCrypt"));
			Assert.assertTrue("指南须包含 RSA 承诺", content.contains("OAEP") || content.contains("2048"));
		} catch (java.io.IOException e) {
			Assert.fail("安全指南读取失败: " + e.getMessage());
		}
	}

	@Test
	public void testNoWeakParameterFallback() {
		// 弱参数被拒，不存在静默降级路径
		Assert.assertFalse("无短密钥常量", AesUtil.generateKey().length() < 32);
		try {
			BCryptUtil.gensalt(2);
			Assert.fail("cost=2 必须拒绝");
		} catch (IllegalArgumentException expected) {
			// 期望行为
		}
		try {
			BCryptUtil.hash(null);
			Assert.fail("null 密码必须拒绝");
		} catch (NullPointerException expected) {
			// 期望行为
		}
	}
}
