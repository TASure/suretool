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

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sure.tool.json.JSONObject;
import com.sure.tool.json.JSONUtil;

/**
 * JWT 令牌：链式构建与解析校验，支持 HS256/HS384/HS512/RS256，参考 Hutool 的 {@code JWT} 设计。
 * <p>
 * 三段式 {@code header.payload.signature}，payload 与 header 使用 sure-json 序列化，签名基于 sure-crypto 同源 JDK 算法。
 *
 * @author suretool
 * @since 0.1.0
 */
public class JWT {

	/**
	 * HS256 算法名。
	 */
	public static final String ALG_HS256 = "HS256";

	/**
	 * HS384 算法名。
	 */
	public static final String ALG_HS384 = "HS384";

	/**
	 * HS512 算法名。
	 */
	public static final String ALG_HS512 = "HS512";

	/**
	 * RS256 算法名。
	 */
	public static final String ALG_RS256 = "RS256";

	/**
	 * 过期时间 claim。
	 */
	public static final String CLAIM_EXP = "exp";

	/**
	 * 签发时间 claim。
	 */
	public static final String CLAIM_IAT = "iat";

	/**
	 * 生效时间 claim。
	 */
	public static final String CLAIM_NBF = "nbf";

	private static final String HEADER_ALG = "alg";
	private static final String HEADER_TYP = "typ";

	private final JSONObject header = new JSONObject();
	private final JSONObject payload = new JSONObject();
	private String algorithm = ALG_HS256;
	private byte[] key;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	// 解析态：原始 Base64URL 段与签名
	private String rawHeaderPart;
	private String rawPayloadPart;
	private byte[] signature;

	private JWT() {
	}

	/**
	 * 创建构建器。
	 *
	 * @return JWT 构建器
	 */
	public static JWT create() {
		return new JWT();
	}

	/**
	 * 解析既有 token（header/payload 立即可读，校验须设置密钥后调 {@link #verify()}）。
	 *
	 * @param token JWT 字符串
	 * @return JWT 对象
	 */
	public static JWT of(String token) {
		if (token == null || token.isEmpty()) {
			throw new JWTException("token 不能为空");
		}
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new JWTException("token 必须为 header.payload.signature 三段式");
		}
		JWT jwt = new JWT();
		jwt.rawHeaderPart = parts[0];
		jwt.rawPayloadPart = parts[1];
		JSONObject header = JSONUtil.parseObj(new String(urlDecode(parts[0]), StandardCharsets.UTF_8));
		JSONObject payload = JSONUtil.parseObj(new String(urlDecode(parts[1]), StandardCharsets.UTF_8));
		jwt.header.putAll(header);
		jwt.payload.putAll(payload);
		jwt.algorithm = header.getStr(HEADER_ALG, ALG_HS256);
		jwt.signature = urlDecode(parts[2]);
		return jwt;
	}

	/**
	 * 设置签名算法。
	 *
	 * @param algorithm 算法名（HS256/HS384/HS512/RS256）
	 * @return this
	 */
	public JWT setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	/**
	 * 设置 HMAC 密钥（字符串按 UTF-8 编码）。
	 *
	 * @param secret 密钥
	 * @return this
	 */
	public JWT setKey(String secret) {
		return setKey(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 设置 HMAC 密钥。
	 *
	 * @param key 密钥字节
	 * @return this
	 */
	public JWT setKey(byte[] key) {
		this.key = key == null ? null : key.clone();
		return this;
	}

	/**
	 * 设置 RSA 密钥对（签名用私钥、校验用公钥）。
	 *
	 * @param keyPair 密钥对
	 * @return this
	 */
	public JWT setKeyPair(KeyPair keyPair) {
		this.privateKey = keyPair.getPrivate();
		this.publicKey = keyPair.getPublic();
		return this;
	}

	/**
	 * 添加 payload claim。
	 *
	 * @param claim claim 名
	 * @param value 值
	 * @return this
	 */
	public JWT setPayload(String claim, Object value) {
		payload.set(claim, value);
		return this;
	}

	/**
	 * 批量添加 payload claims。
	 *
	 * @param claims claim 集合
	 * @return this
	 */
	public JWT setPayload(Map<String, Object> claims) {
		if (claims != null) {
			payload.putAll(claims);
		}
		return this;
	}

	/**
	 * 设置过期时间（写入 exp，Unix 秒）。
	 *
	 * @param expiresAt 过期时间
	 * @return this
	 */
	public JWT setExpiresAt(Date expiresAt) {
		if (expiresAt != null) {
			payload.set(CLAIM_EXP, expiresAt.getTime() / 1000);
		}
		return this;
	}

	/**
	 * 设置签发时间（写入 iat，Unix 秒）。
	 *
	 * @param issuedAt 签发时间
	 * @return this
	 */
	public JWT setIssuedAt(Date issuedAt) {
		if (issuedAt != null) {
			payload.set(CLAIM_IAT, issuedAt.getTime() / 1000);
		}
		return this;
	}

	/**
	 * 设置生效时间（写入 nbf，Unix 秒）。
	 *
	 * @param notBefore 生效时间
	 * @return this
	 */
	public JWT setNotBefore(Date notBefore) {
		if (notBefore != null) {
			payload.set(CLAIM_NBF, notBefore.getTime() / 1000);
		}
		return this;
	}

	/**
	 * 签发并返回 token。
	 *
	 * @return JWT 字符串
	 */
	public String sign() {
		header.set(HEADER_ALG, algorithm);
		header.set(HEADER_TYP, "JWT");
		String headerPart = urlEncode(header.toJsonString().getBytes(StandardCharsets.UTF_8));
		String payloadPart = urlEncode(payload.toJsonString().getBytes(StandardCharsets.UTF_8));
		String signingInput = headerPart + "." + payloadPart;
		byte[] sig = sign(signingInput.getBytes(StandardCharsets.UTF_8));
		return signingInput + "." + urlEncode(sig);
	}

	/**
	 * 校验签名与时间有效性（exp/nbf）。
	 *
	 * @return 是否有效
	 */
	public boolean verify() {
		try {
			if (rawHeaderPart == null || rawPayloadPart == null || signature == null) {
				return false;
			}
			String signingInput = rawHeaderPart + "." + rawPayloadPart;
			byte[] data = signingInput.getBytes(StandardCharsets.UTF_8);
			if (ALG_RS256.equals(algorithm)) {
				if (!rsaVerify(data, signature)) {
					return false;
				}
			} else {
				byte[] expected = sign(data);
				if (!MessageDigest.isEqual(expected, signature)) {
					return false;
				}
			}
			long now = System.currentTimeMillis() / 1000;
			Long exp = payload.getLong(CLAIM_EXP);
			if (exp != null && now >= exp) {
				return false;
			}
			Long nbf = payload.getLong(CLAIM_NBF);
			if (nbf != null && now < nbf) {
				return false;
			}
			return true;
		} catch (JWTException e) {
			return false;
		}
	}

	/**
	 * 获取 payload。
	 *
	 * @return payload JSON 对象
	 */
	public JSONObject getPayload() {
		return payload;
	}

	/**
	 * 获取 header。
	 *
	 * @return header JSON 对象
	 */
	public JSONObject getHeader() {
		return header;
	}

	/**
	 * 获取算法名。
	 *
	 * @return 算法名
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * 按算法执行签名。
	 *
	 * @param data 待签数据
	 * @return 签名
	 */
	private byte[] sign(byte[] data) {
		switch (algorithm) {
			case ALG_HS256:
				return hmac("HmacSHA256", data);
			case ALG_HS384:
				return hmac("HmacSHA384", data);
			case ALG_HS512:
				return hmac("HmacSHA512", data);
			case ALG_RS256:
				return rsaSign(data);
			default:
				throw new JWTException("不支持的算法: " + algorithm);
		}
	}

	/**
	 * HMAC 签名。
	 *
	 * @param algorithm HMAC 算法名
	 * @param data      数据
	 * @return 签名
	 */
	private byte[] hmac(String algorithm, byte[] data) {
		if (key == null) {
			throw new JWTException("缺少 HMAC 密钥，请先 setKey");
		}
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return mac.doFinal(data);
		} catch (Exception e) {
			throw new JWTException("HMAC 签名失败", e);
		}
	}

	/**
	 * RSA 签名。
	 *
	 * @param data 数据
	 * @return 签名
	 */
	private byte[] rsaSign(byte[] data) {
		if (privateKey == null) {
			throw new JWTException("缺少 RSA 私钥，请先 setKeyPair");
		}
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (Exception e) {
			throw new JWTException("RSA 签名失败", e);
		}
	}

	/**
	 * RSA 验签。
	 *
	 * @param data 数据
	 * @param sig  签名
	 * @return 是否匹配
	 */
	private boolean rsaVerify(byte[] data, byte[] sig) {
		if (publicKey == null) {
			throw new JWTException("缺少 RSA 公钥，请先 setKeyPair");
		}
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sig);
		} catch (Exception e) {
			throw new JWTException("RSA 校验失败", e);
		}
	}

	/**
	 * Base64URL 编码（无 padding）。
	 *
	 * @param data 数据
	 * @return 编码串
	 */
	private static String urlEncode(byte[] data) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
	}

	/**
	 * Base64URL 解码。
	 *
	 * @param text 编码串
	 * @return 解码字节
	 */
	private static byte[] urlDecode(String text) {
		try {
			return Base64.getUrlDecoder().decode(text);
		} catch (IllegalArgumentException e) {
			throw new JWTException("token 段 Base64 解码失败", e);
		}
	}
}
