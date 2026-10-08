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

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

/**
 * JDK25 密钥派生与密钥封装工具测试（JEP 510 KDF / JEP 496 ML-KEM）。
 */
public class P6KdfKemTest {

	private static final byte[] IKM = "super-secret-ikm-material".getBytes(StandardCharsets.UTF_8);
	private static final byte[] SALT = "salt".getBytes(StandardCharsets.UTF_8);
	private static final byte[] INFO = "suretool:session-v1".getBytes(StandardCharsets.UTF_8);

	@Test
	public void hkdfLengthAndIdempotent() {
		byte[] out1 = KdfUtil.hkdf(IKM, SALT, INFO, 32);
		byte[] out2 = KdfUtil.hkdf(IKM, SALT, INFO, 32);
		Assert.assertEquals(32, out1.length);
		Assert.assertArrayEquals(out1, out2);
	}

	@Test
	public void hkdfDifferentInfoDifferentOutput() {
		byte[] out1 = KdfUtil.hkdf(IKM, SALT, INFO, 32);
		byte[] out2 = KdfUtil.hkdf(IKM, SALT, "other-context".getBytes(StandardCharsets.UTF_8), 32);
		Assert.assertFalse(Arrays.equals(out1, out2));
	}

	@Test
	public void hkdfNullSaltInfoAllowed() {
		byte[] out = KdfUtil.hkdf(IKM, null, null, 32);
		Assert.assertEquals(32, out.length);
	}

	@Test
	public void hkdfAllAlgorithms() {
		for (String alg : new String[] { KdfUtil.HKDF_SHA256, KdfUtil.HKDF_SHA384, KdfUtil.HKDF_SHA512 }) {
			byte[] out = KdfUtil.hkdf(IKM, SALT, INFO, 48, alg);
			Assert.assertEquals(48, out.length);
		}
	}

	@Test
	public void hkdfExpandOnly() {
		SecretKey prk = new SecretKeySpec(KdfUtil.hkdf(IKM, SALT, null, 32), "HKDF");
		byte[] out = KdfUtil.hkdfExpand(prk, INFO, 32, KdfUtil.HKDF_SHA256);
		Assert.assertEquals(32, out.length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void hkdfEmptyIkmRejected() {
		KdfUtil.hkdf(new byte[0], SALT, INFO, 32);
	}

	@Test
	public void mlKemRoundTripAllParams() {
		for (String alg : new String[] { MlKemUtil.ML_KEM_512, MlKemUtil.ML_KEM_768, MlKemUtil.ML_KEM_1024 }) {
			KeyPair kp = MlKemUtil.generateKeyPair(alg);
			String pub = MlKemUtil.publicKeyToBase64(kp.getPublic());
			String priv = MlKemUtil.privateKeyToBase64(kp.getPrivate());
			Assert.assertEquals(kp.getPublic(), MlKemUtil.publicKeyFromBase64(pub, alg));
			Assert.assertEquals(kp.getPrivate(), MlKemUtil.privateKeyFromBase64(priv, alg));
		}
	}

	@Test
	public void ecdhX25519SharedSecret() {
		KeyPair alice = EcdhUtil.generateKeyPair(EcdhUtil.X25519);
		KeyPair bob = EcdhUtil.generateKeyPair(EcdhUtil.X25519);
		byte[] sa = EcdhUtil.computeSharedSecret(alice.getPrivate(), bob.getPublic(), EcdhUtil.X25519);
		byte[] sb = EcdhUtil.computeSharedSecret(bob.getPrivate(), alice.getPublic(), EcdhUtil.X25519);
		Assert.assertArrayEquals(sa, sb);
		Assert.assertEquals(32, sa.length);
	}

	@Test
	public void ecdhEcSharedSecret() {
		KeyPair alice = EcdhUtil.generateKeyPair(EcdhUtil.EC);
		KeyPair bob = EcdhUtil.generateKeyPair(EcdhUtil.EC);
		byte[] sa = EcdhUtil.computeSharedSecret(alice.getPrivate(), bob.getPublic(), EcdhUtil.EC);
		byte[] sb = EcdhUtil.computeSharedSecret(bob.getPrivate(), alice.getPublic(), EcdhUtil.EC);
		Assert.assertArrayEquals(sa, sb);
		Assert.assertTrue(sa.length > 0);
	}
}
