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

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * 国密 SM2/SM3/SM4 工具测试（含国标测试向量）。
 */
public class SmCryptoTest {

	// ---- SM3：GB/T 32905 标准向量 ----

	@Test
	public void sm3_empty_vector() {
		// 空串标准向量
		assertEquals("1ab21d8355cfa17f8e61194831e81a8f22bec8c728fefb747ed035eb5082aa2b",
				Sm3Util.sm3Hex(new byte[0]));
	}

	@Test
	public void sm3_abc_vector() {
		// "abc" 标准向量
		assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0",
				Sm3Util.sm3Hex("abc"));
	}

	@Test
	public void sm3_long_input_vector() {
		// 512 个 "abcd" 拼接（标准长消息向量）
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 512; i++) {
			sb.append("abcd");
		}
		assertEquals("0d1eb5d8b380cf4aef04f96f6eae057321dc62f4f2d506c443b75ca3a399e930",
				Sm3Util.sm3Hex(sb.toString()));
	}

	@Test
	public void sm3_base64_and_util() {
		assertEquals("Zsfw9GLu7dnR8tRr3BDk4kFnxIdc8veiKX2gK49LqOA=", Sm3Util.sm3Base64("abc"));
		assertEquals(Sm3Util.sm3Hex("abc"), SmUtil.sm3Hex("abc"));
		assertArrayEquals(Sm3Util.sm3("abc".getBytes(StandardCharsets.UTF_8)), SmUtil.sm3("abc".getBytes(StandardCharsets.UTF_8)));
	}

	// ---- SM4：GB/T 32907 标准向量 ----

	private static final byte[] SM4_KEY = hex("0123456789abcdeffedcba9876543210");
	private static final byte[] SM4_PLAIN = hex("0123456789abcdeffedcba9876543210");
	private static final String SM4_CIPHER = "681edf34d206965e86b3e94f536e4246";

	@Test
	public void sm4_ecb_standard_vector() {
		byte[] cipher = Sm4Util.encrypt(SM4_KEY, SM4_PLAIN);
		// PKCS7 填充后首块即标准向量（16 字节明文 → 首块）
		assertEquals(SM4_CIPHER, toHex(java.util.Arrays.copyOfRange(cipher, 0, 16)));
		// 解密回原值
		assertArrayEquals(SM4_PLAIN, Sm4Util.decrypt(SM4_KEY, cipher));
	}

	@Test
	public void sm4_string_roundtrip() {
		byte[] key = Sm4Util.generateKey();
		String text = "hello suretool 国密测试";
		String cipher = Sm4Util.encryptHex(key, text);
		assertEquals(text, Sm4Util.decryptStr(key, cipher));
		// 两次加密相同输入产生相同密文（ECB 确定性）
		assertEquals(cipher, Sm4Util.encryptHex(key, text));
		// 统一入口
		assertEquals(cipher, SmUtil.sm4EncryptHex(key, text));
		assertEquals(text, SmUtil.sm4DecryptStr(key, cipher));
	}

	@Test
	public void sm4_ecb_interop_vector() {
		// 向量由 gmssl(python) 独立生成，验证跨库互操作
		byte[] key = hex("0123456789abcdeffedcba9876543210");
		byte[] pt = "hello sm4 ecb test 2026!".getBytes(StandardCharsets.UTF_8);
		byte[] ct = hex("be0c34b31b1a8c3924d9ae262d3ebe366bd8bac58ca9e93edc72157f40538b31");
		assertEquals(toHex(ct), toHex(Sm4Util.encrypt(key, pt)));
		assertArrayEquals(pt, Sm4Util.decrypt(key, ct));

		byte[] key2 = hex("afd32c3b8c537b18eefffe6a6cb06736");
		byte[] pt2 = "second vector abcdef".getBytes(StandardCharsets.UTF_8);
		byte[] ct2 = hex("58d22fcf84504294a261b8f1d4687926590016b3eac38fb009350f471332de98");
		assertEquals(toHex(ct2), toHex(Sm4Util.encrypt(key2, pt2)));
		assertArrayEquals(pt2, Sm4Util.decrypt(key2, ct2));
	}

	@Test
	public void sm4_cbc_interop_vector() {
		// 向量由 gmssl(python) 独立生成，验证跨库互操作
		byte[] key = hex("0123456789abcdeffedcba9876543210");
		byte[] iv = hex("fedcba98765432100123456789abcdef");
		byte[] pt = "SM4-CBC interop check with gmssl - sure.".getBytes(StandardCharsets.UTF_8);
		byte[] ct = hex("1c40f4dfdb54ce97726daeae634fe532cf37ad2bfb2e6291a8dada246de768ce8230420eb2f4d3118c55f4deb8928102");
		assertEquals(toHex(ct), toHex(Sm4Util.encryptCbc(key, iv, pt)));
		assertArrayEquals(pt, Sm4Util.decryptCbc(key, iv, ct));
	}

	@Test
	public void sm4_padding_and_long_input() {
		byte[] key = Sm4Util.generateKey();
		byte[] data = new byte[1];
		data[0] = 0x01;
		byte[] cipher = Sm4Util.encrypt(key, data);
		assertArrayEquals(data, Sm4Util.decrypt(key, cipher));
		// 恰好 16 字节整数倍
		byte[] data16 = new byte[16];
		Arrays.fill(data16, (byte) 0x55);
		byte[] cipher16 = Sm4Util.encrypt(key, data16);
		assertEquals(32, cipher16.length);
		assertArrayEquals(data16, Sm4Util.decrypt(key, cipher16));
	}

	@Test
	public void sm4_cbc_roundtrip() {
		byte[] key = Sm4Util.generateKey();
		byte[] iv = Sm4Util.generateKey();
		byte[] data = "CBC 模式测试数据".getBytes(StandardCharsets.UTF_8);
		byte[] cipher = Sm4Util.encryptCbc(key, iv, data);
		assertArrayEquals(data, Sm4Util.decryptCbc(key, iv, cipher));
		// 同一明文相同 IV 密文一致；不同 IV 密文不同
		assertArrayEquals(cipher, Sm4Util.encryptCbc(key, iv, data));
		byte[] iv2 = Sm4Util.generateKey();
		assertFalse(Arrays.equals(cipher, Sm4Util.encryptCbc(key, iv2, data)));
	}

	@Test
	public void sm4_invalid_params() {
		assertThrows(CryptoException.class, () -> Sm4Util.encrypt(new byte[15], SM4_PLAIN));
		assertThrows(CryptoException.class, () -> Sm4Util.encrypt(SM4_KEY, new byte[0]));
		byte[] bad = Sm4Util.encrypt(SM4_KEY, SM4_PLAIN);
		bad[bad.length - 1] ^= 0x01; // 篡改密文
		assertThrows(CryptoException.class, () -> Sm4Util.decrypt(SM4_KEY, bad));
		assertThrows(CryptoException.class, () -> Sm4Util.encryptCbc(SM4_KEY, new byte[15], SM4_PLAIN));
	}

	// ---- SM2：签名/验签 + 加解密 ----

	@Test
	public void sm2_sign_verify() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] message = "suretool SM2 签名测试".getBytes(StandardCharsets.UTF_8);
		byte[] sig = Sm2Util.sign(kp, message);
		assertEquals(64, sig.length);
		assertTrue(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), message, sig));
		// 统一入口
		assertTrue(SmUtil.sm2Verify(kp.getPublicX(), kp.getPublicY(), message, SmUtil.sm2Sign(kp, message)));
	}

	@Test
	public void sm2_sign_tamper_fails() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] message = "suretool SM2 篡改测试".getBytes(StandardCharsets.UTF_8);
		byte[] sig = Sm2Util.sign(kp, message);
		// 篡改消息
		byte[] other = "suretool SM2 篡改测试!".getBytes(StandardCharsets.UTF_8);
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), other, sig));
		// 篡改签名
		byte[] badSig = sig.clone();
		badSig[0] ^= 0x01;
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), message, badSig));
		// 错误公钥
		Sm2Util.Sm2KeyPair otherKp = Sm2Util.generateKeyPair();
		assertFalse(Sm2Util.verify(otherKp.getPublicX(), otherKp.getPublicY(), message, sig));
		// 非法长度
		assertFalse(Sm2Util.verify(kp.getPublicX(), kp.getPublicY(), message, new byte[32]));
	}

	@Test
	public void sm2_encrypt_decrypt_roundtrip() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] data = "SM2 国密加密测试数据".getBytes(StandardCharsets.UTF_8);
		byte[] cipher = Sm2Util.encrypt(kp.getPublicX(), kp.getPublicY(), data);
		assertArrayEquals(data, Sm2Util.decrypt(kp, cipher));
		// 统一入口
		byte[] cipher2 = SmUtil.sm2Encrypt(kp.getPublicX(), kp.getPublicY(), data);
		assertArrayEquals(data, SmUtil.sm2Decrypt(kp, cipher2));
		// 错误私钥解密失败
		Sm2Util.Sm2KeyPair other = Sm2Util.generateKeyPair();
		assertThrows(CryptoException.class, () -> Sm2Util.decrypt(other, cipher));
	}

	@Test
	public void sm2_public_key_bytes() {
		Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
		byte[] pub = kp.getPublicKeyBytes();
		assertEquals(65, pub.length);
		assertEquals(0x04, pub[0] & 0xff);
		// 重新从坐标构造并验签一致
		Sm2Util.Sm2KeyPair kp2 = new Sm2Util.Sm2KeyPair(kp.getPrivateKey(), kp.getPublicX(), kp.getPublicY());
		byte[] message = "sm2 public key bytes".getBytes(StandardCharsets.UTF_8);
		assertTrue(Sm2Util.verify(kp2.getPublicX(), kp2.getPublicY(), message, Sm2Util.sign(kp2, message)));
	}

	@Test
	public void sm2_util_keygen() {
		Sm2Util.Sm2KeyPair kp = SmUtil.sm2KeyPair();
		assertNotNull(kp.getPrivateKey());
		assertNotNull(kp.getPublicX());
		assertNotNull(kp.getPublicY());
	}

	// ---- helpers ----

	private static byte[] hex(String s) {
		byte[] out = new byte[s.length() / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
		}
		return out;
	}

	private static String toHex(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (byte value : b) {
			sb.append(Character.forDigit((value >>> 4) & 0xf, 16));
			sb.append(Character.forDigit(value & 0xf, 16));
		}
		return sb.toString();
	}
}
