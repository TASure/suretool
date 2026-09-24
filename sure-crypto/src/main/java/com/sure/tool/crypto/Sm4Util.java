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

/**
 * 国密 SM4 分组密码算法工具类（纯 JDK 实现，零第三方依赖）。
 *
 * <p>SM4 为 128 位分组、128 位密钥的对称密码算法，支持 ECB / CBC 模式与
 * PKCS#7 填充，实现遵循 GB/T 32907-2016。</p>
 *
 * <p>用法示例：</p>
 * <pre>{@code
 * byte[] key = Sm4Util.generateKey();
 * String cipher = Sm4Util.encryptHex(key, "明文");
 * String plain  = Sm4Util.decryptStr(key, cipher);
 * }</pre>
 *
 * @author suretool
 * @since 1.0.1
 */
public final class Sm4Util {

	private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

	private static final int BLOCK = 16;

	private static final int[] FK = {0xa3b1bac6, 0x56aa3350, 0x677d9197, 0xb27022dc};

	private static final int[] SBOX = {
			0xd6, 0x90, 0xe9, 0xfe, 0xcc, 0xe1, 0x3d, 0xb7, 0x16, 0xb6, 0x14, 0xc2, 0x28, 0xfb, 0x2c, 0x05,
			0x2b, 0x67, 0x9a, 0x76, 0x2a, 0xbe, 0x04, 0xc3, 0xaa, 0x44, 0x13, 0x26, 0x49, 0x86, 0x06, 0x99,
			0x9c, 0x42, 0x50, 0xf4, 0x91, 0xef, 0x98, 0x7a, 0x33, 0x54, 0x0b, 0x43, 0xed, 0xcf, 0xac, 0x62,
			0xe4, 0xb3, 0x1c, 0xa9, 0xc9, 0x08, 0xe8, 0x95, 0x80, 0xdf, 0x94, 0xfa, 0x75, 0x8f, 0x3f, 0xa6,
			0x47, 0x07, 0xa7, 0xfc, 0xf3, 0x73, 0x17, 0xba, 0x83, 0x59, 0x3c, 0x19, 0xe6, 0x85, 0x4f, 0xa8,
			0x68, 0x6b, 0x81, 0xb2, 0x71, 0x64, 0xda, 0x8b, 0xf8, 0xeb, 0x0f, 0x4b, 0x70, 0x56, 0x9d, 0x35,
			0x1e, 0x24, 0x0e, 0x5e, 0x63, 0x58, 0xd1, 0xa2, 0x25, 0x22, 0x7c, 0x3b, 0x01, 0x21, 0x78, 0x87,
			0xd4, 0x00, 0x46, 0x57, 0x9f, 0xd3, 0x27, 0x52, 0x4c, 0x36, 0x02, 0xe7, 0xa0, 0xc4, 0xc8, 0x9e,
			0xea, 0xbf, 0x8a, 0xd2, 0x40, 0xc7, 0x38, 0xb5, 0xa3, 0xf7, 0xf2, 0xce, 0xf9, 0x61, 0x15, 0xa1,
			0xe0, 0xae, 0x5d, 0xa4, 0x9b, 0x34, 0x1a, 0x55, 0xad, 0x93, 0x32, 0x30, 0xf5, 0x8c, 0xb1, 0xe3,
			0x1d, 0xf6, 0xe2, 0x2e, 0x82, 0x66, 0xca, 0x60, 0xc0, 0x29, 0x23, 0xab, 0x0d, 0x53, 0x4e, 0x6f,
			0xd5, 0xdb, 0x37, 0x45, 0xde, 0xfd, 0x8e, 0x2f, 0x03, 0xff, 0x6a, 0x72, 0x6d, 0x6c, 0x5b, 0x51,
			0x8d, 0x1b, 0xaf, 0x92, 0xbb, 0xdd, 0xbc, 0x7f, 0x11, 0xd9, 0x5c, 0x41, 0x1f, 0x10, 0x5a, 0xd8,
			0x0a, 0xc1, 0x31, 0x88, 0xa5, 0xcd, 0x7b, 0xbd, 0x2d, 0x74, 0xd0, 0x12, 0xb8, 0xe5, 0xb4, 0xb0,
			0x89, 0x69, 0x97, 0x4a, 0x0c, 0x96, 0x77, 0x7e, 0x65, 0xb9, 0xf1, 0x09, 0xc5, 0x6e, 0xc6, 0x84,
			0x18, 0xf0, 0x7d, 0xec, 0x3a, 0xdc, 0x4d, 0x20, 0x79, 0xee, 0x5f, 0x3e, 0xd7, 0xcb, 0x39, 0x48
	};

	private static final int[] CK = new int[32];

	static {
		// GB/T 32907 附录：CK[i] 的第 j 个字节（从最高字节起）= (28i + 7j) mod 256
		for (int i = 0; i < 32; i++) {
			int v = 0;
			for (int j = 0; j < 4; j++) {
				v = (v << 8) | ((28 * i + 7 * j) & 0xff);
			}
			CK[i] = v;
		}
	}

	private Sm4Util() {
	}

	/**
	 * 生成 16 字节（128 位）随机 SM4 密钥。
	 *
	 * @return 16 字节密钥
	 */
	public static byte[] generateKey() {
		byte[] key = new byte[16];
		SECURE_RANDOM.nextBytes(key);
		return key;
	}

	/**
	 * SM4-ECB 加密（PKCS#7 填充）。
	 *
	 * @param key  16 字节密钥
	 * @param data 明文
	 * @return 密文
	 * @throws CryptoException 密钥长度非法时抛出
	 */
	public static byte[] encrypt(byte[] key, byte[] data) {
		if (data == null || data.length == 0) {
			throw new CryptoException("SM4 输入数据不能为空");
		}
		int[] rk = expandKey(key, true);
		return cryptBlocks(rk, pad(data), false);
	}

	/**
	 * SM4-ECB 解密（去除 PKCS#7 填充）。
	 *
	 * @param key  16 字节密钥
	 * @param data 密文
	 * @return 明文
	 * @throws CryptoException 密钥长度非法或密文非法时抛出
	 */
	public static byte[] decrypt(byte[] key, byte[] data) {
		int[] rk = expandKey(key, false);
		return cryptBlocks(rk, data, true);
	}

	/**
	 * SM4-CBC 加密（PKCS#7 填充）。
	 *
	 * @param key  16 字节密钥
	 * @param iv   16 字节初始向量
	 * @param data 明文
	 * @return 密文
	 * @throws CryptoException 参数非法时抛出
	 */
	public static byte[] encryptCbc(byte[] key, byte[] iv, byte[] data) {
		checkIv(iv);
		if (data == null || data.length == 0) {
			throw new CryptoException("SM4 输入数据不能为空");
		}
		byte[] padded = pad(data);
		byte[] out = new byte[padded.length];
		byte[] work = iv.clone();
		for (int off = 0; off < padded.length; off += BLOCK) {
			byte[] block = new byte[BLOCK];
			for (int i = 0; i < BLOCK; i++) {
				block[i] = (byte) (padded[off + i] ^ work[i]);
			}
			byte[] enc = encryptNoPad(key, block);
			System.arraycopy(enc, 0, out, off, BLOCK);
			System.arraycopy(enc, 0, work, 0, BLOCK);
		}
		return out;
	}

	/**
	 * SM4-CBC 解密（去除 PKCS#7 填充）。
	 *
	 * @param key  16 字节密钥
	 * @param iv   16 字节初始向量
	 * @param data 密文
	 * @return 明文
	 * @throws CryptoException 参数非法时抛出
	 */
	public static byte[] decryptCbc(byte[] key, byte[] iv, byte[] data) {
		checkIv(iv);
		if (data == null || data.length == 0 || data.length % BLOCK != 0) {
			throw new CryptoException("SM4-CBC 密文长度必须为 16 的倍数");
		}
		byte[] out = new byte[data.length];
		byte[] work = iv.clone();
		for (int off = 0; off < data.length; off += BLOCK) {
			byte[] block = new byte[BLOCK];
			System.arraycopy(data, off, block, 0, BLOCK);
			byte[] dec = decryptNoPad(key, block);
			for (int i = 0; i < BLOCK; i++) {
				out[off + i] = (byte) (dec[i] ^ work[i]);
			}
			System.arraycopy(block, 0, work, 0, BLOCK);
		}
		return unpad(out);
	}

	/**
	 * SM4-ECB 加密，密文以十六进制返回。
	 *
	 * @param key  16 字节密钥
	 * @param data 明文
	 * @return 十六进制密文
	 */
	public static String encryptHex(byte[] key, byte[] data) {
		return toHex(encrypt(key, data));
	}

	/**
	 * SM4-ECB 解密，输入十六进制密文。
	 *
	 * @param key 16 字节密钥
	 * @param hex 十六进制密文
	 * @return 明文
	 */
	public static byte[] decryptHex(byte[] key, String hex) {
		return decrypt(key, fromHex(hex));
	}

	/**
	 * SM4-ECB 加密字符串（UTF-8），密文以十六进制返回。
	 *
	 * @param key  16 字节密钥
	 * @param text 明文字符串
	 * @return 十六进制密文
	 */
	public static String encryptHex(byte[] key, String text) {
		return encryptHex(key, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * SM4-ECB 解密十六进制密文为字符串（UTF-8）。
	 *
	 * @param key 16 字节密钥
	 * @param hex 十六进制密文
	 * @return 明文字符串
	 */
	public static String decryptStr(byte[] key, String hex) {
		return new String(decryptHex(key, hex), StandardCharsets.UTF_8);
	}

	// ---- 内部实现 ----

	private static byte[] encryptNoPad(byte[] key, byte[] block) {
		int[] rk = expandKey(key, true);
		return cryptBlock(rk, block);
	}

	private static byte[] decryptNoPad(byte[] key, byte[] block) {
		int[] rk = expandKey(key, false);
		return cryptBlock(rk, block);
	}

	private static byte[] cryptBlock(int[] rk, byte[] block) {
		byte[] out = new byte[BLOCK];
		int[] x = new int[4];
		for (int i = 0; i < 4; i++) {
			x[i] = intFromBytes(block, i * 4);
		}
		for (int i = 0; i < 32; i++) {
			int tmp = x[0] ^ round(x[1] ^ x[2] ^ x[3] ^ rk[i]);
			x[0] = x[1];
			x[1] = x[2];
			x[2] = x[3];
			x[3] = tmp;
		}
		for (int i = 0; i < 4; i++) {
			intToBytes(x[3 - i], out, i * 4);
		}
		return out;
	}

	private static byte[] cryptBlocks(int[] rk, byte[] data, boolean unpad) {
		byte[] out = new byte[data.length];
		for (int off = 0; off < data.length; off += BLOCK) {
			byte[] block = new byte[BLOCK];
			System.arraycopy(data, off, block, 0, BLOCK);
			byte[] enc = cryptBlock(rk, block);
			System.arraycopy(enc, 0, out, off, BLOCK);
		}
		return unpad ? unpad(out) : out;
	}

	private static int round(int x) {
		return l(t(x));
	}

	private static int t(int x) {
		return sbox(x & 0xff) | sbox(x >>> 8 & 0xff) << 8
				| sbox(x >>> 16 & 0xff) << 16 | sbox(x >>> 24 & 0xff) << 24;
	}

	private static int l(int x) {
		return x ^ Integer.rotateLeft(x, 2) ^ Integer.rotateLeft(x, 10)
				^ Integer.rotateLeft(x, 18) ^ Integer.rotateLeft(x, 24);
	}

	private static int sbox(int b) {
		return SBOX[b];
	}

	private static int[] expandKey(byte[] key, boolean encrypt) {
		if (key == null || key.length != BLOCK) {
			throw new CryptoException("SM4 密钥必须为 16 字节（128 位）");
		}
		int[] rk = new int[32];
		int[] k = new int[36];
		for (int i = 0; i < 4; i++) {
			k[i] = intFromBytes(key, i * 4) ^ FK[i];
		}
		for (int i = 0; i < 32; i++) {
			k[i + 4] = k[i] ^ tPrime(k[i + 1] ^ k[i + 2] ^ k[i + 3] ^ CK[i]);
			rk[i] = k[i + 4];
		}
		if (!encrypt) {
			for (int i = 0; i < 16; i++) {
				int tmp = rk[i];
				rk[i] = rk[31 - i];
				rk[31 - i] = tmp;
			}
		}
		return rk;
	}

	private static int tPrime(int x) {
		// T' = L'(τ(x))，密钥扩展与轮函数相同先过 S 盒
		x = sbox(x & 0xff) | sbox(x >>> 8 & 0xff) << 8
				| sbox(x >>> 16 & 0xff) << 16 | sbox(x >>> 24 & 0xff) << 24;
		return x ^ Integer.rotateLeft(x, 13) ^ Integer.rotateLeft(x, 23);
	}

	private static byte[] pad(byte[] data) {
		int padLen = BLOCK - (data.length % BLOCK);
		byte[] out = new byte[data.length + padLen];
		System.arraycopy(data, 0, out, 0, data.length);
		java.util.Arrays.fill(out, data.length, out.length, (byte) padLen);
		return out;
	}

	private static byte[] unpad(byte[] data) {
		if (data.length == 0 || data.length % BLOCK != 0) {
			throw new CryptoException("SM4 密文长度非法");
		}
		int padLen = data[data.length - 1] & 0xff;
		if (padLen <= 0 || padLen > BLOCK) {
			throw new CryptoException("SM4 填充非法");
		}
		for (int i = data.length - padLen; i < data.length; i++) {
			if ((data[i] & 0xff) != padLen) {
				throw new CryptoException("SM4 填充校验失败");
			}
		}
		byte[] out = new byte[data.length - padLen];
		System.arraycopy(data, 0, out, 0, out.length);
		return out;
	}

	private static void checkIv(byte[] iv) {
		if (iv == null || iv.length != BLOCK) {
			throw new CryptoException("SM4 IV 必须为 16 字节");
		}
	}

	private static int intFromBytes(byte[] b, int off) {
		return (b[off] & 0xff) << 24 | (b[off + 1] & 0xff) << 16
				| (b[off + 2] & 0xff) << 8 | b[off + 3] & 0xff;
	}

	private static void intToBytes(int x, byte[] out, int off) {
		out[off] = (byte) (x >>> 24);
		out[off + 1] = (byte) (x >>> 16);
		out[off + 2] = (byte) (x >>> 8);
		out[off + 3] = (byte) x;
	}

	private static String toHex(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (byte value : b) {
			sb.append(Character.forDigit((value >>> 4) & 0xf, 16));
			sb.append(Character.forDigit(value & 0xf, 16));
		}
		return sb.toString();
	}

	private static byte[] fromHex(String hex) {
		if (hex == null || hex.length() % 2 != 0) {
			throw new CryptoException("非法十六进制字符串");
		}
		byte[] out = new byte[hex.length() / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return out;
	}
}
