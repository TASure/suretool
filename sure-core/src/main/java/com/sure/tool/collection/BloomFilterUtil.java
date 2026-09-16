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
package com.sure.tool.collection;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

/**
 * 布隆过滤器：空间高效的概率集合判重，零依赖。
 * <p>
 * 使用 3 个独立哈希（FNV-1a 64 / DJB2 / 混合哈希）映射到 {@link BitSet}，支持预期元素数与误判率配置。
 * 注意：存在误判（可能把未加入的元素判为存在），但不会漏判。
 *
 * @author suretool
 * @since 0.2.0
 */
public class BloomFilterUtil {

	private final BitSet bits;
	private final int bitSize;
	private final int hashFunctions;

	/**
	 * 构造布隆过滤器。
	 *
	 * @param expectedInsertions 预期插入元素数（&gt;0）
	 * @param falsePositiveRate  目标误判率（0~1，默认取 0.01 级别）
	 */
	public BloomFilterUtil(int expectedInsertions, double falsePositiveRate) {
		if (expectedInsertions <= 0) {
			throw new IllegalArgumentException("expectedInsertions 必须大于 0");
		}
		double fpp = falsePositiveRate <= 0 ? 0.01 : Math.min(falsePositiveRate, 1.0);
		// 位数组大小 m = -n*ln(p) / (ln2)^2
		this.bitSize = (int) Math.max(64, Math.ceil(-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2))));
		// 哈希函数个数 k = m/n * ln2
		this.hashFunctions = Math.max(1, (int) Math.round(bitSize * 1.0 / expectedInsertions * Math.log(2)));
		this.bits = new BitSet(bitSize);
	}

	/**
	 * 加入一个字符串。
	 *
	 * @param value 元素
	 */
	public void put(String value) {
		if (value == null) {
			return;
		}
		byte[] data = value.getBytes(StandardCharsets.UTF_8);
		long[] hashes = hash(data);
		for (int i = 0; i < hashFunctions; i++) {
			int index = (int) (((hashes[0] + i * hashes[1]) & Long.MAX_VALUE) % bitSize);
			bits.set(index);
		}
	}

	/**
	 * 判断是否可能存在。
	 *
	 * @param value 元素
	 * @return true 表示可能存在（含误判）；false 表示一定不存在
	 */
	public boolean mightContain(String value) {
		if (value == null) {
			return false;
		}
		byte[] data = value.getBytes(StandardCharsets.UTF_8);
		long[] hashes = hash(data);
		for (int i = 0; i < hashFunctions; i++) {
			int index = (int) (((hashes[0] + i * hashes[1]) & Long.MAX_VALUE) % bitSize);
			if (!bits.get(index)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 位数组大小（m）。
	 *
	 * @return 位数组大小
	 */
	public int bitSize() {
		return bitSize;
	}

	/**
	 * 哈希函数个数（k）。
	 *
	 * @return 哈希函数个数
	 */
	public int hashFunctions() {
		return hashFunctions;
	}

	/** 计算双哈希种子：两个独立初值的 FNV-1a 变体（旋转混合），保证双重哈希近似独立。 */
	private long[] hash(byte[] data) {
		long h1 = 0xcbf29ce484222325L;
		long h2 = 0x9e3779b97f4a7c15L; // 黄金比例初值，独立于 h1
		for (byte b : data) {
			h1 ^= (b & 0xff);
			h1 *= 0x100000001b3L;
			h2 ^= (b & 0xff);
			h2 = (h2 << 31) | (h2 >>> 33);
			h2 *= 0x100000001b3L;
		}
		// 混合哈希作为第三种子
		long h3 = h1 ^ (h2 >>> 32);
		return new long[] {h1, h2, h3};
	}
}
