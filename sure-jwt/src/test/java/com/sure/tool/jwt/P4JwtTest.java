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

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * P4（v0.2.0）第十五批：JWT claim 与过期语义测试。
 */
public class P4JwtTest {

	@Test
	public void testClaimAndExpire() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", 1001);
		claims.put("role", "admin");
		String secret = "suretool-secret";
		String token = JwtUtil.createToken(claims, secret, 3600);

		Assert.assertEquals(1001L, JwtUtil.getClaim(token, secret, "uid"));
		Assert.assertEquals("admin", JwtUtil.getClaim(token, secret, "role"));

		Date expire = JwtUtil.getExpireTime(token, secret);
		Assert.assertNotNull(expire);
		Assert.assertTrue(expire.getTime() > System.currentTimeMillis());
		Assert.assertFalse(JwtUtil.isExpired(token, secret));
		Assert.assertTrue(JwtUtil.getExpireSecondsLeft(token, secret) > 3500);
		Assert.assertTrue(JwtUtil.getExpireSecondsLeft(token, secret) <= 3600);
	}

	@Test
	public void testExpiredToken() {
		// 构造已过期 token（exp 秒 = 0）
		JWT jwt = JWT.create().setKey("suretool-secret").setExpiresAt(new Date(0));
		String expired = jwt.sign();
		Assert.assertTrue(JwtUtil.isExpired(expired, "suretool-secret"));
		Assert.assertTrue(JwtUtil.getExpireSecondsLeft(expired, "suretool-secret") < 0);
	}
}
