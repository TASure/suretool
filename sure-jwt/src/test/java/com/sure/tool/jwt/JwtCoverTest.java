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
package com.sure.tool.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * JWT 覆盖率二轮补强：RSA 往返、错误密钥、无/非法 exp、构造态校验与异常分支。
 */
public class JwtCoverTest {

	private static KeyPair rsaPair() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		return gen.generateKeyPair();
	}

	private static Map<String, Object> claims() {
		Map<String, Object> m = new HashMap<>();
		m.put("sub", "user-1");
		return m;
	}

	/** 构造态（未解析）调用 verifySignature 直接返回 false。 */
	@Test
	public void verifySignature_builtNotParsed() {
		assertFalse(JWT.create().verifySignature());
	}

	/** RS256 无私钥直接签发抛异常。 */
	@Test
	public void sign_rsaWithoutPrivateKey() {
		try {
			JWT.create().setAlgorithm(JWT.ALG_RS256).setPayload("a", 1).sign();
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			assertTrue(expected.getMessage().contains("RSA 私钥"));
		}
	}

	/** 用 EC 私钥强制走 RSA 签名，触发算法不匹配的内部异常包装。 */
	@Test
	public void sign_rsaWithWrongKeyType() throws Exception {
		KeyPair ec = KeyPairGenerator.getInstance("EC").generateKeyPair();
		try {
			JWT.create().setAlgorithm(JWT.ALG_RS256).setKeyPair(ec).setPayload("a", 1).sign();
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			assertTrue(expected.getMessage().contains("RSA 签名失败"));
		}
	}

	/** RS256 完整往返（私钥重载）。 */
	@Test
	public void rsaRoundTrip_privateKeyOverload() throws Exception {
		KeyPair pair = rsaPair();
		String token = JwtUtil.createTokenWithRsa(claims(), pair.getPrivate(), 3600);
		assertNotNull(token);
		assertTrue(JwtUtil.verifyWithRsa(token, pair.getPublic()));
	}

	/** RS256 公钥重载往返。 */
	@Test
	public void rsaRoundTrip_keyPair() throws Exception {
		KeyPair pair = rsaPair();
		String token = JwtUtil.createTokenWithRsa(claims(), pair, 3600);
		assertTrue(JwtUtil.verifyWithRsa(token, pair.getPublic()));
	}

	/** getClaim 错误密钥返回 null。 */
	@Test
	public void getClaim_wrongSecret() {
		String token = JwtUtil.createToken(claims(), "right-secret");
		assertNull(JwtUtil.getClaim(token, "wrong-secret", "sub"));
		assertEquals("user-1", JwtUtil.getClaim(token, "right-secret", "sub"));
	}

	/** getExpireTime 错误密钥返回 null。 */
	@Test
	public void getExpireTime_wrongSecret() {
		String token = JwtUtil.createToken(claims(), "right-secret", 3600);
		assertNull(JwtUtil.getExpireTime(token, "wrong-secret"));
	}

	/** getExpireTime 无 exp claim 返回 null。 */
	@Test
	public void getExpireTime_noExp() {
		String token = JwtUtil.createToken(claims(), "secret");
		assertNull(JwtUtil.getExpireTime(token, "secret"));
		assertFalse(JwtUtil.isExpired(token, "secret"));
		assertEquals(0L, JwtUtil.getExpireSecondsLeft(token, "secret"));
	}

	/** getExpireTime 的 exp 非数字时返回 null。 */
	@Test
	public void getExpireTime_expNotNumber() {
		String token = JWT.create().setPayload("exp", "oops").setKey("secret").sign();
		assertNull(JwtUtil.getExpireTime(token, "secret"));
	}

	/** 篡改签名后 RS256 验签失败路径。 */
	@Test
	public void rsaVerify_tamperedSignature() throws Exception {
		KeyPair pair = rsaPair();
		String token = JwtUtil.createTokenWithRsa(claims(), pair, 3600);
		String[] parts = token.split("\\.");
		// 截断签名段为畸形字节，触发底层 RSA 验签异常被包装
		byte[] sig = java.util.Base64.getUrlDecoder().decode(parts[2]);
		byte[] tiny = new byte[8];
		System.arraycopy(sig, 0, tiny, 0, 8);
		String bad = parts[0] + "." + parts[1] + "."
				+ java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tiny);
		assertFalse(JwtUtil.verifyWithRsa(bad, pair.getPublic()));
	}

	/** 非法 token 三段数不对。 */
	@Test
	public void of_badPartCount() {
		try {
			JWT.of("only.two");
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			assertTrue(expected.getMessage().contains("三段式"));
		}
	}

	/** 空 token。 */
	@Test
	public void of_emptyToken() {
		try {
			JWT.of("");
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			assertTrue(expected.getMessage().contains("不能为空"));
		}
	}

	/** 过期 token 校验失败。 */
	@Test
	public void expiredToken_rejected() {
		String token = JwtUtil.createToken(claims(), "secret", -10);
		assertFalse(JwtUtil.verify(token, "secret"));
		assertTrue(JwtUtil.isExpired(token, "secret"));
	}
}
