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

import java.nio.charset.StandardCharsets;

/**
 * 发布 v1.19.0 覆盖缺口补测：覆盖加密模块未稳定触达的异常分支
 * （非法算法名 → 平台拒绝分支 / BCrypt 长度不等短路），全部确定性、秒级完成。
 */
public class CryptoGapCoverageTest {

	@Test
	public void testHmacUnsupportedAlgorithm() {
		try {
			HmacUtil.hmac("HmacX999",
					"data".getBytes(StandardCharsets.UTF_8),
					"key".getBytes(StandardCharsets.UTF_8));
			Assert.fail("非法 HMAC 算法必须抛 CryptoException");
		} catch (CryptoException expected) {
			Assert.assertTrue(expected.getMessage().contains("HMAC"));
		}
	}

	@Test
	public void testEcdhUnsupportedAlgorithm() {
		try {
			EcdhUtil.generateKeyPair("X999");
			Assert.fail("非法协商算法必须抛 IAE");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("不支持的协商算法"));
		}
	}

	@Test
	public void testMlKemUnsupportedAlgorithm() {
		try {
			MlKemUtil.generateKeyPair("ML-KEM-999");
			Assert.fail("非法 ML-KEM 算法必须抛 IAE");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("不支持的 ML-KEM 算法"));
		}
	}

	@Test
	public void testKdfUnsupportedAlgorithm() {
		try {
			KdfUtil.hkdf(new byte[16], new byte[16], null, 32, "HKDF-X999");
			Assert.fail("非法 KDF 算法必须抛 IAE");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("不支持的 KDF 算法"));
		}
	}

	@Test
	public void testBcryptCheckDifferentLength() {
		// gensalt() 产出 29 字符（$2a$10$+22 盐），与完整 60 字符 hash 长度不等 → 短路 false
		String salt = BCryptUtil.gensalt();
		Assert.assertFalse(BCryptUtil.check("password", salt));
		// 正常路径对照：完整 hash 长度相等且口令匹配 → true
		String hash = BCryptUtil.hash("password", 4);
		Assert.assertTrue(BCryptUtil.check("password", hash));
	}
}
