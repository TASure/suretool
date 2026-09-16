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
package com.sure.tool.codec;

import com.sure.tool.util.CharsetUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * 哈希工具类，参考 Hutool 的 {@code DigestUtil} / {@code HashUtil} 设计。
 *
 * <p>基于 JDK 原生 {@link MessageDigest} 实现 MD5、SHA 系列摘要与 CRC32 校验。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class HashUtil {

	private HashUtil() {
	}

	/**
	 * 计算字符串（UTF-8）的 MD5 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return MD5 十六进制字符串
	 */
	public static String md5Hex(String data) {
		return md5Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 MD5 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return MD5 十六进制字符串
	 */
	public static String md5Hex(byte[] data) {
		return digest("MD5", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-1 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-1 十六进制字符串
	 */
	public static String sha1Hex(String data) {
		return sha1Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-1 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-1 十六进制字符串
	 */
	public static String sha1Hex(byte[] data) {
		return digest("SHA-1", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-256 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-256 十六进制字符串
	 */
	public static String sha256Hex(String data) {
		return sha256Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-256 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-256 十六进制字符串
	 */
	public static String sha256Hex(byte[] data) {
		return digest("SHA-256", data);
	}

	/**
	 * 计算字符串（UTF-8）的 SHA-512 摘要（小写十六进制）。
	 *
	 * @param data 字符串
	 * @return SHA-512 十六进制字符串
	 */
	public static String sha512Hex(String data) {
		return sha512Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 SHA-512 摘要（小写十六进制）。
	 *
	 * @param data 字节数组
	 * @return SHA-512 十六进制字符串
	 */
	public static String sha512Hex(byte[] data) {
		return digest("SHA-512", data);
	}

	/**
	 * 通用摘要计算。
	 *
	 * @param algorithm 算法名
	 * @param data      数据
	 * @return 十六进制摘要
	 */
	public static String digest(String algorithm, byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			return HexUtil.encodeHexStr(md.digest(data));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的摘要算法: " + algorithm, e);
		}
	}

	/**
	 * 计算字符串（UTF-8）的 CRC32 校验值。
	 *
	 * @param data 字符串
	 * @return CRC32 值
	 */
	public static long crc32(String data) {
		return crc32(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 CRC32 校验值。
	 *
	 * @param data 字节数组
	 * @return CRC32 值
	 */
	public static long crc32(byte[] data) {
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		return crc32.getValue();
	}

	/**
	 * 计算字符串（UTF-8）的 CRC32 校验值（8 位十六进制字符串）。
	 *
	 * @param data 字符串
	 * @return CRC32 十六进制字符串
	 */
	public static String crc32Hex(String data) {
		return crc32Hex(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * 计算字节数组的 CRC32 校验值（8 位十六进制字符串）。
	 *
	 * @param data 字节数组
	 * @return CRC32 十六进制字符串
	 */
	public static String crc32Hex(byte[] data) {
		String hex = Long.toHexString(crc32(data));
		return "00000000".substring(hex.length()) + hex;
	}

	/**
	 * MurmurHash3 x86 32 位（字符串，UTF-8）。
	 *
	 * @param data 字符串
	 * @return 32 位哈希
	 */
	public static int murmur3_32(String data) {
		return data == null ? 0 : murmur3_32(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * MurmurHash3 x86 32 位。
	 *
	 * @param data 字节
	 * @return 32 位哈希
	 */
	public static int murmur3_32(byte[] data) {
		if (data == null) {
			return 0;
		}
		int h = 0;
		int len = data.length;
		int i = 0;
		while (i + 4 <= len) {
			int k = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)
					| ((data[i + 2] & 0xFF) << 16) | ((data[i + 3] & 0xFF) << 24);
			k *= 0xcc9e2d51;
			k = Integer.rotateLeft(k, 15);
			k *= 0x1b873593;
			h ^= k;
			h = Integer.rotateLeft(h, 13);
			h = h * 5 + 0xe6546b64;
			i += 4;
		}
		int k = 0;
		int remaining = len & 3;
		if (remaining == 3) {
			k ^= (data[i + 2] & 0xFF) << 16;
		}
		if (remaining >= 2) {
			k ^= (data[i + 1] & 0xFF) << 8;
		}
		if (remaining >= 1) {
			k ^= (data[i] & 0xFF);
			k *= 0xcc9e2d51;
			k = Integer.rotateLeft(k, 15);
			k *= 0x1b873593;
			h ^= k;
		}
		h ^= len;
		h ^= h >>> 16;
		h *= 0x85ebca6b;
		h ^= h >>> 13;
		h *= 0xc2b2ae35;
		h ^= h >>> 16;
		return h;
	}

	/**
	 * FNV-1a 64 位（字符串，UTF-8）。
	 *
	 * @param data 字符串
	 * @return 64 位哈希
	 */
	public static long fnv1a64(String data) {
		return data == null ? 0 : fnv1a64(data.getBytes(CharsetUtil.UTF_8));
	}

	/**
	 * FNV-1a 64 位。
	 *
	 * @param data 字节
	 * @return 64 位哈希
	 */
	public static long fnv1a64(byte[] data) {
		if (data == null) {
			return 0;
		}
		long h = 0xcbf29ce484222325L;
		for (byte b : data) {
			h ^= (b & 0xff);
			h *= 0x100000001b3L;
		}
		return h;
	}

	/**
	 * DJB2 哈希（字符串）。
	 *
	 * @param data 字符串
	 * @return 64 位哈希
	 */
	public static long djb2(String data) {
		if (data == null) {
			return 0;
		}
		long h = 5381L;
		for (int i = 0; i < data.length(); i++) {
			h = ((h << 5) + h) + data.charAt(i);
		}
		return h;
	}


}