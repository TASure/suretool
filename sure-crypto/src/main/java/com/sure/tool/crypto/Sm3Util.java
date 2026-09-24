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
 * 国密 SM3 杂凑算法工具类（纯 JDK 实现，零第三方依赖）。
 *
 * <p>SM3 输出 256 位（32 字节）摘要，是国家密码管理局发布的商用密码杂凑算法，
 * 用于完整性校验、数字签名等场景。实现遵循 GB/T 32905-2016。</p>
 *
 * <p>用法示例：</p>
 * <pre>{@code
 * String hex = Sm3Util.sm3Hex("abc");        // 66c7f0f4...
 * String b64 = Sm3Util.sm3Base64("abc");
 * byte[] digest = Sm3Util.sm3("abc".getBytes(StandardCharsets.UTF_8));
 * }</pre>
 *
 * @author suretool
 * @since 1.0.1
 */
public final class Sm3Util {

	private static final int[] IV = {
			0x7380166f, 0x4914b2b9, 0x172442d7, 0xda8a0600,
			0xa96f30bc, 0x163138aa, 0xe38dee4d, 0xb0fb0e4e
	};

	private static final int[] T_0_15 = new int[16];
	private static final int[] T_16_63 = new int[48];

	static {
		for (int i = 0; i < 16; i++) {
			T_0_15[i] = 0x79cc4519;
		}
		for (int i = 0; i < 48; i++) {
			T_16_63[i] = 0x7a879d8a;
		}
	}

	private Sm3Util() {
	}

	/**
	 * 计算 SM3 摘要。
	 *
	 * @param data 输入字节
	 * @return 32 字节摘要
	 */
	public static byte[] sm3(byte[] data) {
		if (data == null) {
			data = new byte[0];
		}
		// 1. 填充：1 + 0* + 64 位长度（bit 数，大端），使消息模 512 = 448
		long bitLen = (long) data.length * 8;
		int padLen = (56 - (data.length % 64)) & 63;
		byte[] padded = new byte[data.length + padLen + 8];
		System.arraycopy(data, 0, padded, 0, data.length);
		padded[data.length] = (byte) 0x80;
		for (int i = 0; i < 8; i++) {
			padded[padded.length - 1 - i] = (byte) (bitLen >>> (8 * i));
		}
		// 2. 迭代压缩
		int[] v = IV.clone();
		for (int off = 0; off < padded.length; off += 64) {
			compress(v, padded, off);
		}
		// 3. 输出
		byte[] out = new byte[32];
		for (int i = 0; i < 8; i++) {
			out[i * 4] = (byte) (v[i] >>> 24);
			out[i * 4 + 1] = (byte) (v[i] >>> 16);
			out[i * 4 + 2] = (byte) (v[i] >>> 8);
			out[i * 4 + 3] = (byte) v[i];
		}
		return out;
	}

	private static void compress(int[] v, byte[] block, int off) {
		int[] w = new int[68];
		int[] w1 = new int[64];
		for (int i = 0; i < 16; i++) {
			w[i] = ((block[off + i * 4] & 0xff) << 24)
					| ((block[off + i * 4 + 1] & 0xff) << 16)
					| ((block[off + i * 4 + 2] & 0xff) << 8)
					| (block[off + i * 4 + 3] & 0xff);
		}
		for (int j = 16; j < 68; j++) {
			int x = w[j - 16] ^ w[j - 9] ^ rotl(w[j - 3], 15);
			w[j] = p1(x) ^ rotl(w[j - 13], 7) ^ w[j - 6];
		}
		for (int j = 0; j < 64; j++) {
			w1[j] = w[j] ^ w[j + 4];
		}
		int a = v[0], b = v[1], c = v[2], d = v[3];
		int e = v[4], f = v[5], g = v[6], h = v[7];
		for (int j = 0; j < 64; j++) {
			int ss1 = rotl(rotl(a, 12) + e + rotl(t(j), j % 32), 7);
			int ss2 = ss1 ^ rotl(a, 12);
			int tt1 = ff(a, b, c, j) + d + ss2 + w1[j];
			int tt2 = gg(e, f, g, j) + h + ss1 + w[j];
			d = c;
			c = rotl(b, 9);
			b = a;
			a = tt1;
			h = g;
			g = rotl(f, 19);
			f = e;
			e = p0(tt2);
		}
		v[0] ^= a;
		v[1] ^= b;
		v[2] ^= c;
		v[3] ^= d;
		v[4] ^= e;
		v[5] ^= f;
		v[6] ^= g;
		v[7] ^= h;
	}

	private static int t(int j) {
		return j < 16 ? 0x79cc4519 : 0x7a879d8a;
	}

	private static int ff(int x, int y, int z, int j) {
		if (j < 16) {
			return x ^ y ^ z;
		}
		return (x & y) | (x & z) | (y & z);
	}

	private static int gg(int x, int y, int z, int j) {
		if (j < 16) {
			return x ^ y ^ z;
		}
		return (x & y) | (~x & z);
	}

	private static int p0(int x) {
		return x ^ rotl(x, 9) ^ rotl(x, 17);
	}

	private static int p1(int x) {
		return x ^ rotl(x, 15) ^ rotl(x, 23);
	}

	private static int rotl(int x, int n) {
		return Integer.rotateLeft(x, n);
	}

	/**
	 * 计算 SM3 摘要（十六进制小写）。
	 *
	 * @param data 输入字节
	 * @return 64 位十六进制摘要
	 */
	public static String sm3Hex(byte[] data) {
		byte[] digest = sm3(data);
		StringBuilder sb = new StringBuilder(64);
		for (byte b : digest) {
			sb.append(Character.forDigit((b >>> 4) & 0xf, 16));
			sb.append(Character.forDigit(b & 0xf, 16));
		}
		return sb.toString();
	}

	/**
	 * 计算字符串 UTF-8 编码的 SM3 摘要（十六进制小写）。
	 *
	 * @param text 字符串
	 * @return 64 位十六进制摘要
	 */
	public static String sm3Hex(String text) {
		return sm3Hex(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 计算字符串 UTF-8 编码的 SM3 摘要（Base64）。
	 *
	 * @param text 字符串
	 * @return Base64 摘要
	 */
	public static String sm3Base64(String text) {
		return java.util.Base64.getEncoder().encodeToString(sm3(text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 计算字节数组的 SM3 摘要（Base64）。
	 *
	 * @param data 输入字节
	 * @return Base64 摘要
	 */
	public static String sm3Base64(byte[] data) {
		return java.util.Base64.getEncoder().encodeToString(sm3(data));
	}
}
