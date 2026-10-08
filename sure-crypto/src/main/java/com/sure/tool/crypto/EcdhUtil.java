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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.KeyAgreement;

/**
 * 椭圆曲线 Diffie-Hellman 密钥协商工具（经典 KEM 语义）。
 *
 * <p>双方各自生成密钥对，交换公钥后通过 {@link KeyAgreement} 派生相同的共享密钥，
 * 可与 ML-KEM（后量子）互为补充。支持 X25519 与 EC（secp256r1 等）算法。
 * 零第三方运行期依赖。
 *
 * @author suretool contributors
 * @since 1.7.0
 */
public final class EcdhUtil {

	/** X25519 曲线（推荐，高效且无侧信道隐患） */
	public static final String X25519 = "X25519";
	/** EC 曲线（NIST P-256 等标准曲线） */
	public static final String EC = "EC";

	private EcdhUtil() {
	}

	/**
	 * 生成指定算法的密钥对。
	 *
	 * @param algorithm 算法名（{@link #X25519} / {@link #EC}）
	 * @return 生成的密钥对
	 * @throws IllegalArgumentException 算法不存在或生成失败
	 */
	public static KeyPair generateKeyPair(String algorithm) {
		try {
			return KeyPairGenerator.getInstance(algorithm).generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的协商算法：" + algorithm, e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("密钥对生成失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 以本地私钥与对端公钥计算共享密钥。
	 *
	 * <p>双方分别以各自私钥 + 对方公钥调用本方法，得到一致的共享密钥字节。
	 *
	 * @param privateKey 本地私钥
	 * @param publicKey  对端公钥
	 * @param algorithm  算法名（与密钥对生成时一致）
	 * @return 共享密钥字节
	 * @throws IllegalArgumentException 协商失败
	 */
	public static byte[] computeSharedSecret(PrivateKey privateKey, PublicKey publicKey, String algorithm) {
		try {
			// KeyPairGenerator 用 "EC"，KeyAgreement 注册名为 "ECDH"
			String kaAlgorithm = EC.equals(algorithm) ? "ECDH" : algorithm;
			KeyAgreement agreement = KeyAgreement.getInstance(kaAlgorithm);
			agreement.init(privateKey);
			agreement.doPhase(publicKey, true);
			return agreement.generateSecret();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的协商算法：" + algorithm, e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("共享密钥计算失败：" + e.getMessage(), e);
		}
	}
}
