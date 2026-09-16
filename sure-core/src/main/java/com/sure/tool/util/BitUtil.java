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

/**
 * 位操作工具类：单个位读写与整数位范围提取，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class BitUtil {

	private BitUtil() {
	}

	/**
	 * 获取 int 指定位（0 为最低位）。
	 *
	 * @param value 值
	 * @param pos   位位置（0~31）
	 * @return 该位 0/1
	 */
	public static int get(int value, int pos) {
		checkPos(pos, 32);
		return (value >> pos) & 1;
	}

	/**
	 * 获取 long 指定位（0 为最低位）。
	 *
	 * @param value 值
	 * @param pos   位位置（0~63）
	 * @return 该位 0/1
	 */
	public static int get(long value, int pos) {
		checkPos(pos, 64);
		return (int) ((value >> pos) & 1L);
	}

	/**
	 * 获取字节数组指定位（位序按大端：index 0 为第 1 字节最高位）。
	 *
	 * @param data 字节数组
	 * @param pos  位位置（0 ~ data.length*8-1）
	 * @return 该位 0/1
	 */
	public static int get(byte[] data, int pos) {
		if (data == null || pos < 0 || pos >= data.length * 8) {
			throw new IllegalArgumentException("pos 越界");
		}
		int byteIndex = pos / 8;
		int bitIndex = 7 - (pos % 8);
		return (data[byteIndex] >> bitIndex) & 1;
	}

	/**
	 * 设置 int 指定位为 1。
	 *
	 * @param value 值
	 * @param pos   位位置（0~31）
	 * @return 新值
	 */
	public static int set(int value, int pos) {
		checkPos(pos, 32);
		return value | (1 << pos);
	}

	/**
	 * 清除 int 指定位（置 0）。
	 *
	 * @param value 值
	 * @param pos   位位置（0~31）
	 * @return 新值
	 */
	public static int clear(int value, int pos) {
		checkPos(pos, 32);
		return value & ~(1 << pos);
	}

	/**
	 * 翻转 int 指定位。
	 *
	 * @param value 值
	 * @param pos   位位置（0~31）
	 * @return 新值
	 */
	public static int toggle(int value, int pos) {
		checkPos(pos, 32);
		return value ^ (1 << pos);
	}

	/**
	 * 设置 long 指定位为 1。
	 *
	 * @param value 值
	 * @param pos   位位置（0~63）
	 * @return 新值
	 */
	public static long set(long value, int pos) {
		checkPos(pos, 64);
		return value | (1L << pos);
	}

	/**
	 * 提取 int 的位范围 [from, to)（from 为低位），右对齐返回。
	 *
	 * @param value 值
	 * @param from  起始位（含，0 为最低位）
	 * @param to    结束位（不含）
	 * @return 提取的位（右对齐）
	 */
	public static int getRange(int value, int from, int to) {
		checkRange(from, to, 32);
		int len = to - from;
		int mask = len == 32 ? -1 : ((1 << len) - 1);
		return (value >>> from) & mask;
	}

	/**
	 * 提取 long 的位范围 [from, to)。
	 *
	 * @param value 值
	 * @param from  起始位（含）
	 * @param to    结束位（不含）
	 * @return 提取的位（右对齐）
	 */
	public static long getRange(long value, int from, int to) {
		checkRange(from, to, 64);
		int len = to - from;
		long mask = len == 64 ? -1L : ((1L << len) - 1);
		return (value >>> from) & mask;
	}

	private static void checkPos(int pos, int max) {
		if (pos < 0 || pos >= max) {
			throw new IllegalArgumentException("位位置越界: " + pos + "（允许 0~" + (max - 1) + "）");
		}
	}

	private static void checkRange(int from, int to, int max) {
		if (from < 0 || to > max || from >= to) {
			throw new IllegalArgumentException("位范围不合法: [" + from + ", " + to + ")");
		}
	}
}
