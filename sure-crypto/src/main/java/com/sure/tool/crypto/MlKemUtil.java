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

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 后量子密钥工具，基于 JDK 25 正式特性 JEP 496（Quantum-Resistant Module-Lattice-Based
 * Key Encapsulation Mechanism，ML-KEM / Kyber）。
 *
 * <p>提供 ML-KEM（Kyber）密钥对生成与 X.509 / PKCS8 编解码。JDK 25 当前提供密钥对生成能力；
 * 封装/解封装 API（JEP 452）尚未进入 JDK 25，待未来 JDK 支持后由 {@code encapsulate/decapsulate}
 * 扩展。零第三方运行期依赖。
 *
 * @author suretool contributors
 * @since 1.7.0
 */
public final class MlKemUtil {

	/** ML-KEM-512（Kyber-512 后量子标准参数） */
	public static final String ML_KEM_512 = "ML-KEM-512";
	/** ML-KEM-768（Kyber-768 后量子标准参数，推荐） */
	public static final String ML_KEM_768 = "ML-KEM-768";
	/** ML-KEM-1024（Kyber-1024 后量子标准参数） */
	public static final String ML_KEM_1024 = "ML-KEM-1024";

	private MlKemUtil() {
	}

	/**
	 * 生成指定参数的 ML-KEM 密钥对。
	 *
	 * @param algorithm 算法名（{@link #ML_KEM_512} / {@link #ML_KEM_768} / {@link #ML_KEM_1024}）
	 * @return 生成的密钥对
	 * @throws IllegalArgumentException 算法不存在或生成失败
	 */
	public static KeyPair generateKeyPair(String algorithm) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的 ML-KEM 算法：" + algorithm, e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("ML-KEM 密钥对生成失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 将公钥编码为 X.509 Base64 字符串。
	 *
	 * @param key 公钥
	 * @return Base64 编码字符串
	 */
	public static String publicKeyToBase64(PublicKey key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * 从 X.509 Base64 字符串还原公钥。
	 *
	 * @param base64    Base64 编码的公钥
	 * @param algorithm 算法名（与生成时一致）
	 * @return 还原的公钥
	 * @throws IllegalArgumentException 编码非法或还原失败
	 */
	public static PublicKey publicKeyFromBase64(String base64, String algorithm) {
		try {
			byte[] encoded = Base64.getDecoder().decode(base64);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
			return KeyFactory.getInstance(algorithm).generatePublic(spec);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Base64 解码失败", e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("公钥还原失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 将私钥编码为 PKCS8 Base64 字符串。
	 *
	 * @param key 私钥
	 * @return Base64 编码字符串
	 */
	public static String privateKeyToBase64(PrivateKey key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * 从 PKCS8 Base64 字符串还原私钥。
	 *
	 * @param base64    Base64 编码的私钥
	 * @param algorithm 算法名（与生成时一致）
	 * @return 还原的私钥
	 * @throws IllegalArgumentException 编码非法或还原失败
	 */
	public static PrivateKey privateKeyFromBase64(String base64, String algorithm) {
		try {
			byte[] encoded = Base64.getDecoder().decode(base64);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
			return KeyFactory.getInstance(algorithm).generatePrivate(spec);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Base64 解码失败", e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("私钥还原失败：" + e.getMessage(), e);
		}
	}
}
