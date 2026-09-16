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
package com.sure.tool.util;

import java.nio.charset.StandardCharsets;

/**
 * 字节数组工具类：基础类型与字节互转、拼接、切片、反转、十六进制，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class BytesUtil {

	private BytesUtil() {
	}

	/**
	 * long 转 8 字节（大端）。
	 *
	 * @param value long 值
	 * @return 8 字节
	 */
	public static byte[] longToBytes(long value) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (value >>> (56 - i * 8));
		}
		return bytes;
	}

	/**
	 * 8 字节转 long（大端）。
	 *
	 * @param bytes 字节（至少 8 字节）
	 * @return long 值
	 */
	public static long bytesToLong(byte[] bytes) {
		if (bytes == null || bytes.length < 8) {
			throw new IllegalArgumentException("至少需要 8 字节");
		}
		long value = 0;
		for (int i = 0; i < 8; i++) {
			value = (value << 8) | (bytes[i] & 0xFF);
		}
		return value;
	}

	/**
	 * int 转 4 字节（大端）。
	 *
	 * @param value int 值
	 * @return 4 字节
	 */
	public static byte[] intToBytes(int value) {
		return new byte[] {(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
	}

	/**
	 * 4 字节转 int（大端）。
	 *
	 * @param bytes 字节（至少 4 字节）
	 * @return int 值
	 */
	public static int bytesToInt(byte[] bytes) {
		if (bytes == null || bytes.length < 4) {
			throw new IllegalArgumentException("至少需要 4 字节");
		}
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
	}

	/**
	 * short 转 2 字节（大端）。
	 *
	 * @param value short 值
	 * @return 2 字节
	 */
	public static byte[] shortToBytes(short value) {
		return new byte[] {(byte) (value >>> 8), (byte) value};
	}

	/**
	 * 2 字节转 short（大端）。
	 *
	 * @param bytes 字节（至少 2 字节）
	 * @return short 值
	 */
	public static short bytesToShort(byte[] bytes) {
		if (bytes == null || bytes.length < 2) {
			throw new IllegalArgumentException("至少需要 2 字节");
		}
		return (short) (((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF));
	}

	/**
	 * 拼接多个字节数组。
	 *
	 * @param arrays 数组组
	 * @return 拼接结果
	 */
	public static byte[] concat(byte[]... arrays) {
		int total = 0;
		for (byte[] arr : arrays) {
			total += arr == null ? 0 : arr.length;
		}
		byte[] result = new byte[total];
		int pos = 0;
		for (byte[] arr : arrays) {
			if (arr != null) {
				System.arraycopy(arr, 0, result, pos, arr.length);
				pos += arr.length;
			}
		}
		return result;
	}

	/**
	 * 切片（含 from 不含 to）。
	 *
	 * @param bytes 原数组
	 * @param from  起始（含）
	 * @param to    结束（不含）
	 * @return 切片
	 */
	public static byte[] slice(byte[] bytes, int from, int to) {
		if (bytes == null) {
			return null;
		}
		int start = Math.max(0, from);
		int end = Math.min(bytes.length, to);
		if (start >= end) {
			return new byte[0];
		}
		byte[] result = new byte[end - start];
		System.arraycopy(bytes, start, result, 0, end - start);
		return result;
	}

	/**
	 * 反转字节数组（返回新数组）。
	 *
	 * @param bytes 原数组
	 * @return 反转结果
	 */
	public static byte[] reverse(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		byte[] result = bytes.clone();
		for (int i = 0, j = result.length - 1; i < j; i++, j--) {
			byte tmp = result[i];
			result[i] = result[j];
			result[j] = tmp;
		}
		return result;
	}

	/**
	 * 转小写十六进制字符串。
	 *
	 * @param bytes 字节
	 * @return 十六进制
	 */
	public static String toHex(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(Character.forDigit((b >> 4) & 0xF, 16));
			sb.append(Character.forDigit(b & 0xF, 16));
		}
		return sb.toString();
	}

	/**
	 * 字节数组转字符串（UTF-8）。
	 *
	 * @param bytes 字节
	 * @return 字符串
	 */
	public static String toString(byte[] bytes) {
		return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * 字符串转字节数组（UTF-8）。
	 *
	 * @param str 字符串
	 * @return 字节
	 */
	public static byte[] toBytes(String str) {
		return str == null ? null : str.getBytes(StandardCharsets.UTF_8);
	}
}
