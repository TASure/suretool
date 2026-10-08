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
import java.security.NoSuchAlgorithmException;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;

/**
 * 密钥派生工具，基于 JDK 25 正式特性 JEP 510（Key Derivation Function API）。
 *
 * <p>提供 HKDF（RFC 5869）密钥派生：extract-then-expand 与 expand-only 两种模式，
 * 用于从高熵输入密钥材料（IKM）派生会话密钥、子密钥等，构造与 TLS 1.3 / WireGuard 一致。
 * 核心实现仅依赖 JDK {@link KDF}，零第三方运行期依赖。
 *
 * @author suretool contributors
 * @since 1.7.0
 */
public final class KdfUtil {

	/** HKDF-SHA256 算法名 */
	public static final String HKDF_SHA256 = "HKDF-SHA256";
	/** HKDF-SHA384 算法名 */
	public static final String HKDF_SHA384 = "HKDF-SHA384";
	/** HKDF-SHA512 算法名 */
	public static final String HKDF_SHA512 = "HKDF-SHA512";

	private KdfUtil() {
	}

	/**
	 * HKDF extract-then-expand：从输入密钥材料派生指定长度的字节材料（默认 HKDF-SHA256）。
	 *
	 * @param ikm    输入密钥材料
	 * @param salt   盐值，可为 {@code null} 或空数组（HKDF 规范允许空盐）
	 * @param info   上下文与应用信息，可为 {@code null} 或空数组
	 * @param length 派生输出长度（字节）
	 * @return 派生出的字节材料
	 * @throws IllegalArgumentException 参数非法或派生失败
	 */
	public static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length) {
		return hkdf(ikm, salt, info, length, HKDF_SHA256);
	}

	/**
	 * HKDF extract-then-expand：指定哈希算法的重载版本。
	 *
	 * @param ikm       输入密钥材料
	 * @param salt      盐值，可为 {@code null} 或空数组
	 * @param info      上下文与应用信息，可为 {@code null} 或空数组
	 * @param length    派生输出长度（字节）
	 * @param algorithm HKDF 算法名（{@link #HKDF_SHA256} / {@link #HKDF_SHA384} / {@link #HKDF_SHA512}）
	 * @return 派生出的字节材料
	 * @throws IllegalArgumentException 参数非法或派生失败
	 */
	public static byte[] hkdf(byte[] ikm, byte[] salt, byte[] info, int length, String algorithm) {
		if (ikm == null || ikm.length == 0) {
			throw new IllegalArgumentException("ikm 不能为空");
		}
		if (length <= 0) {
			throw new IllegalArgumentException("length 必须为正整数");
		}
		try {
			KDF kdf = KDF.getInstance(algorithm);
			HKDFParameterSpec spec = HKDFParameterSpec.ofExtract()
					.addIKM(ikm)
					.addSalt(salt == null ? new byte[0] : salt)
					.thenExpand(info == null ? new byte[0] : info, length);
			return kdf.deriveData(spec);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的 KDF 算法：" + algorithm, e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("HKDF 派生失败：" + e.getMessage(), e);
		}
	}

	/**
	 * HKDF expand-only：基于已提取的伪随机密钥（PRK）进行扩展（默认 HKDF-SHA256）。
	 *
	 * <p>适用于已持有 PRK 的场景，跳过 extract 阶段直接生成指定长度输出。
	 *
	 * @param prk    已提取的伪随机密钥
	 * @param info   上下文与应用信息，可为 {@code null} 或空数组
	 * @param length 派生输出长度（字节）
	 * @param algorithm HKDF 算法名
	 * @return 派生出的字节材料
	 * @throws IllegalArgumentException 参数非法或派生失败
	 */
	public static byte[] hkdfExpand(SecretKey prk, byte[] info, int length, String algorithm) {
		if (prk == null) {
			throw new IllegalArgumentException("prk 不能为空");
		}
		if (length <= 0) {
			throw new IllegalArgumentException("length 必须为正整数");
		}
		try {
			KDF kdf = KDF.getInstance(algorithm);
			HKDFParameterSpec spec = HKDFParameterSpec.expandOnly(prk, info == null ? new byte[0] : info, length);
			return kdf.deriveData(spec);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的 KDF 算法：" + algorithm, e);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("HKDF 派生失败：" + e.getMessage(), e);
		}
	}
}
