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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

import com.sure.tool.json.JSONObject;

/**
 * JWT 工具门面：一键签发与校验，参考 Hutool 的 {@code JWTUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JwtUtil {

	private JwtUtil() {
	}

	/**
	 * 签发 HS256 token。
	 *
	 * @param claims payload
	 * @param secret HMAC 密钥
	 * @return token
	 */
	public static String createToken(Map<String, Object> claims, String secret) {
		return JWT.create().setPayload(claims).setKey(secret).sign();
	}

	/**
	 * 签发带过期时间的 HS256 token（exp 为当前时间 + expireSeconds）。
	 *
	 * @param claims         payload
	 * @param secret         HMAC 密钥
	 * @param expireSeconds  有效期（秒）
	 * @return token
	 */
	public static String createToken(Map<String, Object> claims, String secret, int expireSeconds) {
		Date now = new Date();
		return JWT.create()
				.setPayload(claims)
				.setIssuedAt(now)
				.setExpiresAt(new Date(now.getTime() + expireSeconds * 1000L))
				.setKey(secret)
				.sign();
	}

	/**
	 * 校验 HS256 token（签名 + 过期时间）。
	 *
	 * @param token  token
	 * @param secret HMAC 密钥
	 * @return 是否有效
	 */
	public static boolean verify(String token, String secret) {
		return JWT.of(token).setKey(secret).verify();
	}

	/**
	 * 校验并解析 token，失败抛 {@link JWTException}。
	 *
	 * @param token  token
	 * @param secret HMAC 密钥
	 * @return payload
	 */
	public static JSONObject parseToken(String token, String secret) {
		JWT jwt = JWT.of(token).setKey(secret);
		if (!jwt.verify()) {
			throw new JWTException("token 无效或已过期");
		}
		return jwt.getPayload();
	}

	/**
	 * 签发 RS256 token。
	 *
	 * @param claims        payload
	 * @param keyPair       RSA 密钥对
	 * @param expireSeconds 有效期（秒）
	 * @return token
	 */
	public static String createTokenWithRsa(Map<String, Object> claims, KeyPair keyPair, int expireSeconds) {
		Date now = new Date();
		return JWT.create()
				.setAlgorithm(JWT.ALG_RS256)
				.setPayload(claims)
				.setIssuedAt(now)
				.setExpiresAt(new Date(now.getTime() + expireSeconds * 1000L))
				.setKeyPair(keyPair)
				.sign();
	}

	/**
	 * 校验 RS256 token。
	 *
	 * @param token     token
	 * @param publicKey RSA 公钥
	 * @return 是否有效
	 */
	public static boolean verifyWithRsa(String token, PublicKey publicKey) {
		JWT jwt = JWT.of(token);
		jwt.setAlgorithm(JWT.ALG_RS256);
		KeyPair pair = new KeyPair(publicKey, null);
		jwt.setKeyPair(pair);
		return jwt.verify();
	}

	/**
	 * 签发 RS256 token（私钥）。
	 *
	 * @param claims        payload
	 * @param privateKey    RSA 私钥
	 * @param expireSeconds 有效期（秒）
	 * @return token
	 */
	public static String createTokenWithRsa(Map<String, Object> claims, PrivateKey privateKey, int expireSeconds) {
		KeyPair pair = new KeyPair(null, privateKey);
		return createTokenWithRsa(claims, pair, expireSeconds);
	}
}
