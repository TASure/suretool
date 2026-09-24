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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 国密 SM2 椭圆曲线公钥密码算法工具类（纯 JDK 实现，零第三方依赖）。
 *
 * <p>实现遵循 GB/T 32918-2016，覆盖密钥对生成、签名/验签（r||s，64 字节）与
 * 加密/解密（C1||C3||C2，C1 为未压缩点）。默认用户标识 {@code 1234567812345678}。</p>
 *
 * <p>用法示例：</p>
 * <pre>{@code
 * Sm2Util.Sm2KeyPair kp = Sm2Util.generateKeyPair();
 * byte[] sig = Sm2Util.sign(kp, "消息".getBytes(StandardCharsets.UTF_8));
 * boolean ok = Sm2Util.verify(kp.getPublicX(), kp.getPublicY(),
 *         "消息".getBytes(StandardCharsets.UTF_8), sig);
 * }</pre>
 *
 * @author suretool
 * @since 1.0.1
 */
public final class Sm2Util {

	/** SM2 曲线 sm2p256v1 参数 */
	private static final BigInteger P = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
	private static final BigInteger A = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
	private static final BigInteger B = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
	private static final BigInteger N = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
	private static final BigInteger GX = new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
	private static final BigInteger GY = new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

	/** 默认用户标识（16 字节） */
	private static final byte[] DEFAULT_USER_ID = "1234567812345678".getBytes(StandardCharsets.UTF_8);

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final BigInteger TWO = BigInteger.valueOf(2);
	private static final BigInteger THREE = BigInteger.valueOf(3);

	private Sm2Util() {
	}

	/**
	 * SM2 密钥对（私钥 d 与公钥点 P = dG）。
	 */
	public static final class Sm2KeyPair {
		private final BigInteger d;
		private final BigInteger pubX;
		private final BigInteger pubY;

		Sm2KeyPair(BigInteger d, BigInteger pubX, BigInteger pubY) {
			this.d = d;
			this.pubX = pubX;
			this.pubY = pubY;
		}

		/**
		 * @return 私钥 d（1..n-1）
		 */
		public BigInteger getPrivateKey() {
			return d;
		}

		/**
		 * @return 公钥 x 坐标
		 */
		public BigInteger getPublicX() {
			return pubX;
		}

		/**
		 * @return 公钥 y 坐标
		 */
		public BigInteger getPublicY() {
			return pubY;
		}

		/**
		 * @return 未压缩公钥编码（04 || x || y，共 65 字节）
		 */
		public byte[] getPublicKeyBytes() {
			byte[] out = new byte[65];
			out[0] = 0x04;
			byte[] x = toBytes(pubX, 32);
			byte[] y = toBytes(pubY, 32);
			System.arraycopy(x, 0, out, 1, 32);
			System.arraycopy(y, 0, out, 33, 32);
			return out;
		}
	}

	/**
	 * 生成 SM2 密钥对。
	 *
	 * @return 密钥对
	 */
	public static Sm2KeyPair generateKeyPair() {
		BigInteger d;
		do {
			d = new BigInteger(N.bitLength(), RANDOM);
		} while (d.signum() == 0 || d.compareTo(N) >= 0);
		Point pub = multiply(new Point(GX, GY), d);
		return new Sm2KeyPair(d, pub.x, pub.y);
	}

	/**
	 * SM2 签名（r||s，64 字节）。
	 *
	 * @param keyPair 密钥对
	 * @param message 消息
	 * @return 签名（r||s）
	 */
	public static byte[] sign(Sm2KeyPair keyPair, byte[] message) {
		return sign(keyPair, message, DEFAULT_USER_ID);
	}

	/**
	 * SM2 签名（r||s，64 字节）。
	 *
	 * @param keyPair 密钥对
	 * @param message 消息
	 * @param userId  用户标识（默认 1234567812345678）
	 * @return 签名（r||s）
	 */
	public static byte[] sign(Sm2KeyPair keyPair, byte[] message, byte[] userId) {
		if (keyPair == null) {
			throw new CryptoException("SM2 密钥对不能为空");
		}
		byte[] z = za(keyPair.pubX, keyPair.pubY, userId);
		byte[] e = Sm3Util.sm3(concat(z, message));
		BigInteger eInt = new BigInteger(1, e);
		BigInteger r;
		BigInteger s;
		BigInteger k;
		do {
			do {
				k = new BigInteger(N.bitLength(), RANDOM);
			} while (k.signum() == 0 || k.compareTo(N) >= 0);
			Point kp = multiply(new Point(GX, GY), k);
			r = eInt.add(kp.x).mod(N);
		} while (r.signum() == 0 || r.add(k).compareTo(N) == 0);
		BigInteger inv = keyPair.d.add(BigInteger.ONE).modInverse(N);
		s = inv.multiply(k.subtract(r.multiply(keyPair.d)).mod(N)).mod(N);
		if (s.signum() == 0) {
			return sign(keyPair, message, userId);
		}
		byte[] out = new byte[64];
		System.arraycopy(toBytes(r, 32), 0, out, 0, 32);
		System.arraycopy(toBytes(s, 32), 0, out, 32, 32);
		return out;
	}

	/**
	 * SM2 验签。
	 *
	 * @param publicX   公钥 x 坐标
	 * @param publicY   公钥 y 坐标
	 * @param message   消息
	 * @param signature 签名（r||s，64 字节）
	 * @return 验签是否通过
	 */
	public static boolean verify(BigInteger publicX, BigInteger publicY, byte[] message, byte[] signature) {
		return verify(publicX, publicY, message, signature, DEFAULT_USER_ID);
	}

	/**
	 * SM2 验签。
	 *
	 * @param publicX   公钥 x 坐标
	 * @param publicY   公钥 y 坐标
	 * @param message   消息
	 * @param signature 签名（r||s，64 字节）
	 * @param userId    用户标识
	 * @return 验签是否通过
	 */
	public static boolean verify(BigInteger publicX, BigInteger publicY, byte[] message, byte[] signature, byte[] userId) {
		if (signature == null || signature.length != 64) {
			return false;
		}
		BigInteger r = new BigInteger(1, java.util.Arrays.copyOfRange(signature, 0, 32));
		BigInteger s = new BigInteger(1, java.util.Arrays.copyOfRange(signature, 32, 64));
		if (r.signum() <= 0 || r.compareTo(N) >= 0 || s.signum() <= 0 || s.compareTo(N) >= 0) {
			return false;
		}
		if (!isOnCurve(publicX, publicY)) {
			return false;
		}
		byte[] z = za(publicX, publicY, userId);
		byte[] e = Sm3Util.sm3(concat(z, message));
		BigInteger eInt = new BigInteger(1, e);
		BigInteger t = r.add(s).mod(N);
		if (t.signum() == 0) {
			return false;
		}
		Point p = new Point(publicX, publicY);
		Point sum = add(multiply(new Point(GX, GY), s), multiply(p, t));
		BigInteger rr = eInt.add(sum.x).mod(N);
		return rr.compareTo(r) == 0;
	}

	/**
	 * SM2 加密（C1||C3||C2）。
	 *
	 * @param publicX 公钥 x 坐标
	 * @param publicY 公钥 y 坐标
	 * @param data    明文
	 * @return 密文
	 */
	public static byte[] encrypt(BigInteger publicX, BigInteger publicY, byte[] data) {
		if (data == null || data.length == 0) {
			throw new CryptoException("SM2 明文不能为空");
		}
		if (!isOnCurve(publicX, publicY)) {
			throw new CryptoException("SM2 公钥不在曲线上");
		}
		BigInteger k;
		Point c1Point;
		Point kpb;
		byte[] t;
		byte[] x2;
		byte[] y2;
		do {
			do {
				k = new BigInteger(N.bitLength(), RANDOM);
			} while (k.signum() == 0 || k.compareTo(N) >= 0);
			c1Point = multiply(new Point(GX, GY), k);
			kpb = multiply(new Point(publicX, publicY), k);
			x2 = toBytes(kpb.x, 32);
			y2 = toBytes(kpb.y, 32);
			t = kdf(concat(x2, y2), data.length);
		} while (allZero(t));
		byte[] c2 = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			c2[i] = (byte) (data[i] ^ t[i]);
		}
		byte[] c3 = Sm3Util.sm3(concat(concat(x2, data), y2));
		byte[] out = new byte[65 + 32 + c2.length];
		System.arraycopy(c1Point.encode(), 0, out, 0, 65);
		System.arraycopy(c3, 0, out, 65, 32);
		System.arraycopy(c2, 0, out, 97, c2.length);
		return out;
	}

	/**
	 * SM2 解密（C1||C3||C2）。
	 *
	 * @param keyPair 密钥对
	 * @param cipher  密文
	 * @return 明文
	 * @throws CryptoException 密文非法或校验失败时抛出
	 */
	public static byte[] decrypt(Sm2KeyPair keyPair, byte[] cipher) {
		if (keyPair == null) {
			throw new CryptoException("SM2 密钥对不能为空");
		}
		if (cipher == null || cipher.length < 97) {
			throw new CryptoException("SM2 密文长度非法");
		}
		BigInteger c1x = new BigInteger(1, java.util.Arrays.copyOfRange(cipher, 1, 33));
		BigInteger c1y = new BigInteger(1, java.util.Arrays.copyOfRange(cipher, 33, 65));
		if (!isOnCurve(c1x, c1y)) {
			throw new CryptoException("SM2 密文 C1 不在曲线上");
		}
		byte[] c3 = java.util.Arrays.copyOfRange(cipher, 65, 97);
		byte[] c2 = java.util.Arrays.copyOfRange(cipher, 97, cipher.length);
		Point kpb = multiply(new Point(c1x, c1y), keyPair.d);
		byte[] x2 = toBytes(kpb.x, 32);
		byte[] y2 = toBytes(kpb.y, 32);
		byte[] t = kdf(concat(x2, y2), c2.length);
		if (allZero(t)) {
			throw new CryptoException("SM2 KDF 输出全零");
		}
		byte[] data = new byte[c2.length];
		for (int i = 0; i < c2.length; i++) {
			data[i] = (byte) (c2[i] ^ t[i]);
		}
		byte[] u = Sm3Util.sm3(concat(concat(x2, data), y2));
		if (!java.util.Arrays.equals(u, c3)) {
			throw new CryptoException("SM2 密文校验失败");
		}
		return data;
	}

	// ---- 椭圆曲线运算 ----

	private record Point(BigInteger x, BigInteger y) {
		byte[] encode() {
			byte[] out = new byte[65];
			out[0] = 0x04;
			System.arraycopy(toBytes(x, 32), 0, out, 1, 32);
			System.arraycopy(toBytes(y, 32), 0, out, 33, 32);
			return out;
		}
	}

	private static boolean isOnCurve(BigInteger x, BigInteger y) {
		if (x == null || y == null || x.signum() < 0 || x.compareTo(P) >= 0
				|| y.signum() < 0 || y.compareTo(P) >= 0) {
			return false;
		}
		BigInteger left = y.multiply(y).mod(P);
		BigInteger right = x.multiply(x).multiply(x).add(A.multiply(x)).add(B).mod(P);
		return left.compareTo(right) == 0;
	}

	private static Point add(Point p, Point q) {
		if (p == null || q == null) {
			return null;
		}
		if (p.x.compareTo(q.x) == 0) {
			if (p.y.add(q.y).mod(P).signum() == 0) {
				return null; // 无穷远点
			}
			return doublePoint(p);
		}
		BigInteger lambda = q.y.subtract(p.y).multiply(q.x.subtract(p.x).modInverse(P)).mod(P);
		BigInteger x3 = lambda.multiply(lambda).subtract(p.x).subtract(q.x).mod(P);
		BigInteger y3 = lambda.multiply(p.x.subtract(x3)).subtract(p.y).mod(P);
		return new Point(x3, y3);
	}

	private static Point doublePoint(Point p) {
		BigInteger lambda = p.x.multiply(p.x).multiply(THREE).add(A)
				.multiply(p.y.multiply(TWO).modInverse(P)).mod(P);
		BigInteger x3 = lambda.multiply(lambda).subtract(p.x.multiply(TWO)).mod(P);
		BigInteger y3 = lambda.multiply(p.x.subtract(x3)).subtract(p.y).mod(P);
		return new Point(x3, y3);
	}

	private static Point multiply(Point p, BigInteger k) {
		Point result = null;
		Point addend = p;
		BigInteger bits = k;
		while (bits.signum() > 0) {
			if (bits.testBit(0)) {
				result = (result == null) ? addend : add(result, addend);
			}
			addend = doublePoint(addend);
			bits = bits.shiftRight(1);
		}
		return result;
	}

	// ---- SM2 辅助 ----

	private static byte[] za(BigInteger pubX, BigInteger pubY, byte[] userId) {
		if (userId == null || userId.length == 0) {
			userId = DEFAULT_USER_ID;
		}
		int entl = userId.length * 8;
		byte[] entlBytes = {(byte) (entl >>> 8), (byte) entl};
		byte[] a = toBytes(A, 32);
		byte[] b = toBytes(B, 32);
		byte[] gx = toBytes(GX, 32);
		byte[] gy = toBytes(GY, 32);
		byte[] px = toBytes(pubX, 32);
		byte[] py = toBytes(pubY, 32);
		return Sm3Util.sm3(concat(entlBytes, userId, a, b, gx, gy, px, py));
	}

	private static byte[] kdf(byte[] z, int klen) {
		byte[] out = new byte[klen];
		int ct = 1;
		int off = 0;
		byte[] ctBytes = new byte[4];
		while (off < klen) {
			ctBytes[0] = (byte) (ct >>> 24);
			ctBytes[1] = (byte) (ct >>> 16);
			ctBytes[2] = (byte) (ct >>> 8);
			ctBytes[3] = (byte) ct;
			byte[] h = Sm3Util.sm3(concat(z, ctBytes));
			int n = Math.min(32, klen - off);
			System.arraycopy(h, 0, out, off, n);
			off += n;
			ct++;
		}
		return out;
	}

	private static boolean allZero(byte[] b) {
		for (byte value : b) {
			if (value != 0) {
				return false;
			}
		}
		return true;
	}

	private static byte[] concat(byte[]... arrays) {
		int len = 0;
		for (byte[] arr : arrays) {
			len += arr.length;
		}
		byte[] out = new byte[len];
		int off = 0;
		for (byte[] arr : arrays) {
			System.arraycopy(arr, 0, out, off, arr.length);
			off += arr.length;
		}
		return out;
	}

	private static byte[] toBytes(BigInteger v, int len) {
		byte[] raw = v.toByteArray();
		byte[] out = new byte[len];
		if (raw.length >= len) {
			System.arraycopy(raw, raw.length - len, out, 0, len);
		} else {
			System.arraycopy(raw, 0, out, len - raw.length, raw.length);
		}
		return out;
	}
}
