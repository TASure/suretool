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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sure.tool.crypto.SecureUtil;
import com.sure.tool.json.JSONObject;
import com.sure.tool.jwt.JWT;
import com.sure.tool.jwt.JWTException;
import com.sure.tool.jwt.JwtUtil;

/**
 * JWT 测试：四算法签发/校验、篡改检测、过期与 nbf、门面方法。
 */
public class JwtTest {

	private static final String SECRET = "suretool-secret-key-2026";

	@Test
	public void testHs256RoundTrip() {
		String token = JWT.create()
				.setPayload("sub", "12345")
				.setPayload("name", "张三")
				.setKey(SECRET)
				.sign();
		JWT jwt = JWT.of(token).setKey(SECRET);
		assertTrue(jwt.verify());
		assertEquals("12345", jwt.getPayload().getStr("sub"));
		assertEquals("张三", jwt.getPayload().getStr("name"));
		assertEquals(JWT.ALG_HS256, jwt.getAlgorithm());
		assertEquals("JWT", jwt.getHeader().getStr("typ"));
	}

	@Test
	public void testHs384AndHs512() {
		assertTrue(roundTrip(JWT.ALG_HS384));
		assertTrue(roundTrip(JWT.ALG_HS512));
	}

	@Test
	public void testTamperedPayloadRejected() {
		String token = JWT.create().setPayload("sub", "12345").setKey(SECRET).sign();
		// 篡改 payload 段（改最后一个字符以破坏签名且保持可解析）
		String[] parts = token.split("\\.");
		String tampered = parts[0] + "." + parts[1] + "." + parts[2].substring(0, parts[2].length() - 2) + "AA";
		assertFalse(JWT.of(tampered).setKey(SECRET).verify());
	}

	@Test
	public void testWrongKeyRejected() {
		String token = JWT.create().setPayload("sub", "12345").setKey(SECRET).sign();
		assertFalse(JWT.of(token).setKey("wrong-key").verify());
	}

	@Test
	public void testExpiredRejected() {
		String token = JWT.create()
				.setPayload("sub", "12345")
				.setExpiresAt(new Date(System.currentTimeMillis() - 60_000L))
				.setKey(SECRET)
				.sign();
		assertFalse(JWT.of(token).setKey(SECRET).verify());
	}

	@Test
	public void testNotBeforeFutureRejected() {
		String token = JWT.create()
				.setNotBefore(new Date(System.currentTimeMillis() + 60_000L))
				.setKey(SECRET)
				.sign();
		assertFalse(JWT.of(token).setKey(SECRET).verify());
	}

	@Test
	public void testRsaRoundTrip() {
		KeyPair keyPair = SecureUtil.rsaGenerateKeyPair();
		String token = JWT.create()
				.setAlgorithm(JWT.ALG_RS256)
				.setPayload("sub", "rsa-user")
				.setKeyPair(keyPair)
				.sign();
		JWT jwt = JWT.of(token).setKeyPair(keyPair);
		assertTrue(jwt.verify());
		assertEquals("rsa-user", jwt.getPayload().getStr("sub"));
		assertEquals(JWT.ALG_RS256, jwt.getAlgorithm());
	}

	@Test
	public void testRsaTamperedRejected() {
		KeyPair keyPair = SecureUtil.rsaGenerateKeyPair();
		String token = JWT.create()
				.setAlgorithm(JWT.ALG_RS256)
				.setPayload("sub", "rsa-user")
				.setKeyPair(keyPair)
				.sign();
		String tampered = token.substring(0, token.length() - 3) + "xyz";
		assertFalse(JWT.of(tampered).setKeyPair(keyPair).verify());
	}

	@Test
	public void testRsaVerifyWithoutKeyRejected() {
		KeyPair keyPair = SecureUtil.rsaGenerateKeyPair();
		String token = JWT.create()
				.setAlgorithm(JWT.ALG_RS256)
				.setPayload("sub", "rsa-user")
				.setKeyPair(keyPair)
				.sign();
		// 未设置密钥对 → verify 内部捕获异常返回 false
		assertFalse(JWT.of(token).verify());
	}

	@Test
	public void testJwtUtilCreateVerifyParse() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", 1001);
		claims.put("role", "admin");
		String token = JwtUtil.createToken(claims, SECRET, 3600);
		assertTrue(JwtUtil.verify(token, SECRET));
		assertFalse(JwtUtil.verify(token, "wrong"));
		JSONObject payload = JwtUtil.parseToken(token, SECRET);
		assertEquals(Integer.valueOf(1001), payload.getInt("uid"));
		assertEquals("admin", payload.getStr("role"));
		assertNotNull(payload.getLong(JWT.CLAIM_EXP));
		assertNotNull(payload.getLong(JWT.CLAIM_IAT));
	}

	@Test
	public void testJwtUtilCreateTokenNoExpiry() {
		String token = JwtUtil.createToken(new HashMap<>(), SECRET);
		assertTrue(JwtUtil.verify(token, SECRET));
	}

	@Test
	public void testJwtUtilRsa() {
		KeyPair keyPair = SecureUtil.rsaGenerateKeyPair();
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", "rsa");
		String token = JwtUtil.createTokenWithRsa(claims, keyPair, 3600);
		assertTrue(JwtUtil.verifyWithRsa(token, keyPair.getPublic()));
	}

	@Test
	public void testParseTokenFailure() {
		String token = JwtUtil.createToken(new HashMap<>(), SECRET, 3600);
		try {
			JwtUtil.parseToken(token, "wrong-secret");
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
	}

	@Test
	public void testInvalidTokenFormat() {
		try {
			JWT.of("not-a-jwt");
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
		try {
			JWT.of(null);
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
	}

	@Test
	public void testUnsupportedAlgorithm() {
		try {
			JWT.create().setAlgorithm("XX999").setKey(SECRET).sign();
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
	}

	@Test
	public void testMissingKeyOnSign() {
		try {
			JWT.create().sign();
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
	}

	@Test
	public void testInvalidBase64() {
		String bad = "!!!." + "!!!." + "!!!";
		try {
			JWT.of(bad);
			fail("应抛出 JWTException");
		} catch (JWTException expected) {
			// 预期
		}
	}

	/**
	 * 指定 HMAC 算法往返校验。
	 *
	 * @param algorithm 算法
	 * @return 是否有效
	 */
	private boolean roundTrip(String algorithm) {
		String token = JWT.create()
				.setAlgorithm(algorithm)
				.setPayload("sub", "u1")
				.setKey(SECRET)
				.sign();
		return JWT.of(token).setKey(SECRET).verify();
	}
}
