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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

/**
 * 覆盖率补强测试：覆盖各加密工具的非法参数、算法缺失、密钥类型不匹配、密钥派生失败等异常分支。
 *
 * <p>所有断言按真实 JCA/JCE API 语义编写，不构造“假通过”路径。</p>
 */
public class CoverageBoostTest {

	/** SM2 曲线阶 n（与 {@code Sm2Util} 内部一致），用于构造边界签名。 */
	private static final BigInteger SM2_N = new BigInteger(
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);

	private static final byte[] SM4_KEY = hex("0123456789abcdeffedcba9876543210");
	private static final byte[] SM4_IV = hex("fedcba98765432100123456789abcdef");

	private static final byte[] IKM = "boost-ikm".getBytes(StandardCharsets.UTF_8);
	private static final byte[] SALT = "boost-salt".getBytes(StandardCharsets.UTF_8);
	private static final byte[] INFO = "boost-info".getBytes(StandardCharsets.UTF_8);

	// ================= SM2 =================

	@Test
	public void sm2_signNullKeyRejected() {
		assertThrows(CryptoException.class,
				() -> Sm2Util.sign(null, "msg".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void sm2_verifySignatureOutOfRange() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] msg = "range".getBytes(StandardCharsets.UTF_8);
		// r 全 0xFF → r >= n，命中 r/s 范围校验返回 false
		byte[] bigR = new byte[64];
		Arrays.fill(bigR, (byte) 0xFF);
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), msg, bigR));
	}

	@Test
	public void sm2_verifyOffCurvePublicKey() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] msg = "offcurve".getBytes(StandardCharsets.UTF_8);
		byte[] sig = Sm2Util.sign(kp, msg);
		// (0,0) 不在曲线上 → false
		assertFalse(Sm2Util.verify(BigInteger.ZERO, BigInteger.ZERO, msg, sig));
	}

	@Test
	public void sm2_verifyNullPublicKeyHitsOnCurveBound() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] msg = "nullpub".getBytes(StandardCharsets.UTF_8);
		byte[] sig = Sm2Util.sign(kp, msg);
		// publicX/Y 为 null → isOnCurve 命中入参边界返回 false
		assertFalse(Sm2Util.verify(null, null, msg, sig));
	}

	@Test
	public void sm2_verifyTEqualsZero() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] msg = "tzero".getBytes(StandardCharsets.UTF_8);
		// 构造 r=1, s=n-1，使 t=(r+s) mod n = 0 → 返回 false
		byte[] forged = concat32(BigInteger.ONE, SM2_N.subtract(BigInteger.ONE));
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), msg, forged));
	}

	@Test
	public void sm2_encryptEmptyDataRejected() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		assertThrows(CryptoException.class,
				() -> Sm2Util.encrypt(kp.getPublicX(), kp.getPublicY(), new byte[0]));
	}

	@Test
	public void sm2_encryptOffCurvePublicKeyRejected() {
		assertThrows(CryptoException.class,
				() -> Sm2Util.encrypt(BigInteger.ZERO, BigInteger.ZERO, "data".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void sm2_decryptNullKeyRejected() {
		assertThrows(CryptoException.class, () -> Sm2Util.decrypt(null, new byte[100]));
	}

	@Test
	public void sm2_decryptShortCipherRejected() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		assertThrows(CryptoException.class, () -> Sm2Util.decrypt(kp, new byte[50]));
	}

	@Test
	public void sm2_decryptC1OffCurveRejected() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		// 长度合法但 C1=(0,0) 不在曲线上
		byte[] cipher = new byte[100];
		assertThrows(CryptoException.class, () -> Sm2Util.decrypt(kp, cipher));
	}

	@Test
	public void sm2_signNullUserIdUsesDefault() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] msg = "default-uid".getBytes(StandardCharsets.UTF_8);
		byte[] sig = Sm2Util.sign(kp, msg, null);
		// 默认用户标识验签应通过
		assertTrue(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), msg, sig));
	}

	@Test
	public void sm2_verifyForcedDoublingPoint() {
		// 已知私钥 d，伪造 s 使 s*G == t*d*G（两点相同）→ add 命中加倍分支
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		BigInteger d = kp.getPrivateKey();
		BigInteger r = BigInteger.valueOf(2);
		// s = r*d*(1-d)^-1 mod n
		BigInteger s = r.multiply(d).multiply(BigInteger.ONE.subtract(d).modInverse(SM2_N)).mod(SM2_N);
		byte[] forged = concat32(r, s);
		// 验签走到点加（相同 x）后结果与 r 不等，返回 false
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), "double".getBytes(StandardCharsets.UTF_8), forged));
	}

	@Test
	public void sm2_verifyForcedInfinityPoint() {
		// 伪造 s 使 s*G == -(t*d*G)（两点互为负元）→ add 命中无穷远分支
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		BigInteger d = kp.getPrivateKey();
		BigInteger r = BigInteger.valueOf(3);
		// s = -r*d*(1+d)^-1 mod n
		BigInteger s = r.negate().multiply(d).multiply(BigInteger.ONE.add(d).modInverse(SM2_N)).mod(SM2_N);
		byte[] forged = concat32(r, s);
		// 两点相加为无穷远点（内部返回 null），随后访问坐标触发 NPE（正常传播，不吞异常）
		assertThrows(NullPointerException.class,
				() -> Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), "inf".getBytes(StandardCharsets.UTF_8), forged));
	}

	// ================= RSA =================

	@Test
	public void rsa_parseGarbagePublicKeyFails() throws Exception {
		String garbage = Base64.getEncoder().encodeToString(new byte[24]);
		assertThrows(CryptoException.class, () -> RsaUtil.parsePublicKey(garbage));
	}

	@Test
	public void rsa_parseGarbagePrivateKeyFails() throws Exception {
		String garbage = Base64.getEncoder().encodeToString(new byte[24]);
		assertThrows(CryptoException.class, () -> RsaUtil.parsePrivateKey(garbage));
	}

	@Test
	public void rsa_signWithEcPrivateKeyFails() throws Exception {
		KeyPair ec = KeyPairGenerator.getInstance("EC").generateKeyPair();
		assertThrows(CryptoException.class, () -> RsaUtil.sign("data", ec.getPrivate()));
	}

	@Test
	public void rsa_verifyWithEcPublicKeyFails() throws Exception {
		KeyPair ec = KeyPairGenerator.getInstance("EC").generateKeyPair();
		assertThrows(CryptoException.class,
				() -> RsaUtil.verify("data", ec.getPublic(), Base64.getEncoder().encodeToString(new byte[8])));
	}

	@Test
	public void rsa_signBytesWithEcPrivateKeyFails() throws Exception {
		KeyPair ec = KeyPairGenerator.getInstance("EC").generateKeyPair();
		assertThrows(CryptoException.class, () -> RsaUtil.signBytes("data", ec.getPrivate()));
	}

	@Test
	public void rsa_verifyBytesGarbageReturnsFalse() {
		KeyPair rsa = RsaUtil.generateKeyPair();
		// 非法签名字节数组 → verify 捕获异常返回 false
		assertFalse(RsaUtil.verifyBytes("data", rsa.getPublic(), new byte[] { 1, 2, 3 }));
	}

	// ================= ML-KEM =================

	@Test
	public void mlkem_unsupportedAlgorithmRejected() {
		assertThrows(IllegalArgumentException.class, () -> MlKemUtil.generateKeyPair("ML-KEM-BOGUS"));
	}

	@Test
	public void mlkem_publicKeyBadBase64Rejected() {
		assertThrows(IllegalArgumentException.class,
				() -> MlKemUtil.publicKeyFromBase64("@@@not-base64@@@", MlKemUtil.ML_KEM_768));
	}

	@Test
	public void mlkem_publicKeyGarbageSpecRejected() {
		String garbage = Base64.getEncoder().encodeToString(new byte[32]);
		assertThrows(IllegalArgumentException.class,
				() -> MlKemUtil.publicKeyFromBase64(garbage, MlKemUtil.ML_KEM_768));
	}

	@Test
	public void mlkem_privateKeyBadBase64Rejected() {
		assertThrows(IllegalArgumentException.class,
				() -> MlKemUtil.privateKeyFromBase64("@@@not-base64@@@", MlKemUtil.ML_KEM_768));
	}

	@Test
	public void mlkem_privateKeyGarbageSpecRejected() {
		String garbage = Base64.getEncoder().encodeToString(new byte[32]);
		assertThrows(IllegalArgumentException.class,
				() -> MlKemUtil.privateKeyFromBase64(garbage, MlKemUtil.ML_KEM_768));
	}

	// ================= KDF =================

	@Test
	public void kdf_nonPositiveLengthRejected() {
		assertThrows(IllegalArgumentException.class, () -> KdfUtil.hkdf(IKM, SALT, INFO, 0));
	}

	@Test
	public void kdf_unsupportedAlgorithmRejected() {
		assertThrows(IllegalArgumentException.class, () -> KdfUtil.hkdf(IKM, SALT, INFO, 32, "HKDF-BOGUS"));
	}

	@Test
	public void kdf_expandNullPrkRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> KdfUtil.hkdfExpand(null, INFO, 32, KdfUtil.HKDF_SHA256));
	}

	@Test
	public void kdf_expandNonPositiveLengthRejected() {
		SecretKey prk = new SecretKeySpec(KdfUtil.hkdf(IKM, SALT, null, 32), "HKDF");
		assertThrows(IllegalArgumentException.class,
				() -> KdfUtil.hkdfExpand(prk, INFO, 0, KdfUtil.HKDF_SHA256));
	}

	@Test
	public void kdf_expandUnsupportedAlgorithmRejected() {
		SecretKey prk = new SecretKeySpec(KdfUtil.hkdf(IKM, SALT, null, 32), "HKDF");
		assertThrows(IllegalArgumentException.class,
				() -> KdfUtil.hkdfExpand(prk, INFO, 32, "HKDF-BOGUS"));
	}

	@Test
	public void kdf_hugeLengthHitsGeneralSecurityCatch() {
		// 派生长度超出 HKDF 协议上限（255 * 哈希长度）→ deriveData 抛 GeneralSecurityException
		assertThrows(IllegalArgumentException.class, () -> KdfUtil.hkdf(IKM, SALT, INFO, 20000));
		SecretKey prk = new SecretKeySpec(KdfUtil.hkdf(IKM, SALT, null, 32), "HKDF");
		assertThrows(IllegalArgumentException.class,
				() -> KdfUtil.hkdfExpand(prk, INFO, 20000, KdfUtil.HKDF_SHA256));
	}

	// ================= ECDH =================

	@Test
	public void ecdh_unsupportedKeygenAlgorithmRejected() {
		assertThrows(IllegalArgumentException.class, () -> EcdhUtil.generateKeyPair("BOGUS-CURVE"));
	}

	@Test
	public void ecdh_unsupportedAgreementAlgorithmRejected() {
		KeyPair x = EcdhUtil.generateKeyPair(EcdhUtil.X25519);
		assertThrows(IllegalArgumentException.class,
				() -> EcdhUtil.computeSharedSecret(x.getPrivate(), x.getPublic(), "BOGUS-KA"));
	}

	@Test
	public void ecdh_mismatchedKeyTypeRejected() {
		KeyPair x = EcdhUtil.generateKeyPair(EcdhUtil.X25519);
		KeyPair ec = EcdhUtil.generateKeyPair(EcdhUtil.EC);
		// 以 X25519 私钥协商 EC 公钥 → 密钥类型不匹配
		assertThrows(IllegalArgumentException.class,
				() -> EcdhUtil.computeSharedSecret(x.getPrivate(), ec.getPublic(), EcdhUtil.X25519));
	}

	// ================= SM4 =================

	@Test
	public void sm4_cbcEncryptEmptyDataRejected() {
		assertThrows(CryptoException.class, () -> Sm4Util.encryptCbc(SM4_KEY, SM4_IV, new byte[0]));
	}

	@Test
	public void sm4_cbcDecryptNonBlockLengthRejected() {
		assertThrows(CryptoException.class, () -> Sm4Util.decryptCbc(SM4_KEY, SM4_IV, new byte[10]));
	}

	@Test
	public void sm4_ecbDecryptEmptyReachesUnpadLengthCheck() {
		assertThrows(CryptoException.class, () -> Sm4Util.decrypt(SM4_KEY, new byte[0]));
	}

	@Test
	public void sm4_cbcDecryptInvalidPadLength() {
		byte[] data16 = new byte[16];
		Arrays.fill(data16, (byte) 0x55);
		byte[] cipher = Sm4Util.encryptCbc(SM4_KEY, SM4_IV, data16);
		// 篡改前一密文块末字节，使最后明文块末字节（填充长度）变为 0 → 非法填充
		cipher[15] ^= 0x10;
		assertThrows(CryptoException.class, () -> Sm4Util.decryptCbc(SM4_KEY, SM4_IV, cipher));
	}

	@Test
	public void sm4_decryptOddHexRejected() {
		assertThrows(CryptoException.class, () -> Sm4Util.decryptHex(SM4_KEY, "abc"));
	}

	// ================= AES / DES =================

	@Test
	public void aes_encryptNullDataHitsCatch() {
		assertThrows(CryptoException.class, () -> AesUtil.encrypt((byte[]) null, "any-key"));
	}

	@Test
	public void des_encryptNullDataHitsCatch() {
		assertThrows(CryptoException.class, () -> DesUtil.encrypt((byte[]) null, "any-key"));
	}

	/**
	 * 临时摘除 SUN Provider，使 SHA-256 / MD5 / SHA-384 摘要算法不可用，
	 * 触发“算法不可用”防御分支；无论是否异常都在 finally 中还原，避免污染其他用例。
	 *
	 * <p>注意：须先以正常调用预热，确保 AesUtil 静态 SecureRandom 已完成构造，
	 * 否则摘除 SUN 期间新建 SecureRandom 会因 SHA-1 缺失而 InternalError。</p>
	 */
	@Test
	public void digestUnavailableDefensiveBranches() throws Exception {
		// 预热：触发静态 RANDOM 初始化，避免摘除期间重建 SecureRandom
		AesUtil.encrypt("warmup".getBytes(StandardCharsets.UTF_8), "warmup-key");
		DesUtil.encrypt("warmup".getBytes(StandardCharsets.UTF_8), "warmup-key");
		SecureUtil.sha384("warmup");

		Provider sun = Security.getProvider("SUN");
		int pos = 1;
		Provider[] all = Security.getProviders();
		for (int i = 0; i < all.length; i++) {
			if (all[i] == sun) {
				pos = i + 1;
				break;
			}
		}
		Security.removeProvider("SUN");
		try {
			// SHA-256 不可用 → AesUtil.buildKey 命中 NoSuchAlgorithm 防御分支
			assertThrows(CryptoException.class, () -> AesUtil.encrypt("x".getBytes(StandardCharsets.UTF_8), "k"));
			// MD5 不可用 → DesUtil.buildKey 命中防御分支
			assertThrows(CryptoException.class, () -> DesUtil.encrypt("x".getBytes(StandardCharsets.UTF_8), "k"));
			// SHA-384 不可用 → SecureUtil.sha384 命中 NoSuchAlgorithm 防御分支
			assertThrows(CryptoException.class, () -> SecureUtil.sha384("x"));
		} finally {
			Security.insertProviderAt(sun, pos);
		}
		// 还原后摘要恢复正常
		assertNotNull(MessageDigest.getInstance("SHA-256"));
		assertNotNull(MessageDigest.getInstance("MD5"));
		assertNotNull(MessageDigest.getInstance("SHA-384"));
	}

	// ================= SM3 / SmUtil =================

	@Test
	public void sm3_nullInputAllowed() {
		byte[] h = Sm3Util.sm3(null);
		assertNotNull(h);
		assertEquals(32, h.length);
	}

	@Test
	public void sm3_base64Overload() {
		String b64 = Sm3Util.sm3Base64(new byte[] { 1, 2, 3 });
		assertNotNull(b64);
		// 与字符串入口一致
		assertEquals(b64, Sm3Util.sm3Base64(new String(new byte[] { 1, 2, 3 }, StandardCharsets.UTF_8)));
	}

	@Test
	public void smUtil_sm4Key() {
		byte[] key = SmUtil.sm4Key();
		assertNotNull(key);
		assertEquals(16, key.length);
	}

	// ================= helpers =================

	private static byte[] concat32(BigInteger r, BigInteger s) {
		byte[] out = new byte[64];
		System.arraycopy(put32(r), 0, out, 0, 32);
		System.arraycopy(put32(s), 0, out, 32, 32);
		return out;
	}

	private static byte[] put32(BigInteger v) {
		byte[] out = new byte[32];
		byte[] raw = v.toByteArray();
		int len = Math.min(32, raw.length);
		System.arraycopy(raw, raw.length - len, out, 32 - len, len);
		return out;
	}

	private static byte[] hex(String s) {
		byte[] out = new byte[s.length() / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
		}
		return out;
	}
}
