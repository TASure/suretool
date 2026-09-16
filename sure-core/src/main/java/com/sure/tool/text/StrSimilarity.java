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
package com.sure.tool.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 字符串相似度工具类：编辑距离、相似度、Jaccard、余弦相似度，零依赖。
 *
 * @author suretool
 * @since 0.2.0
 */
public class StrSimilarity {

	private StrSimilarity() {
	}

	/**
	 * Levenshtein 编辑距离（插入/删除/替换各计 1）。
	 *
	 * @param a 字符串
	 * @param b 字符串
	 * @return 编辑距离；任一为 null 按空串处理
	 */
	public static int levenshtein(String a, String b) {
		String s1 = a == null ? "" : a;
		String s2 = b == null ? "" : b;
		int m = s1.length();
		int n = s2.length();
		int[] prev = new int[n + 1];
		int[] curr = new int[n + 1];
		for (int j = 0; j <= n; j++) {
			prev[j] = j;
		}
		for (int i = 1; i <= m; i++) {
			curr[0] = i;
			for (int j = 1; j <= n; j++) {
				int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
				curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
			}
			int[] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		return prev[n];
	}

	/**
	 * 相似度（1 - 编辑距离 / 最大长度），0~1，1 表示完全相同。
	 *
	 * @param a 字符串
	 * @param b 字符串
	 * @return 相似度
	 */
	public static double similarity(String a, String b) {
		String s1 = a == null ? "" : a;
		String s2 = b == null ? "" : b;
		int maxLen = Math.max(s1.length(), s2.length());
		if (maxLen == 0) {
			return 1.0;
		}
		return 1.0 - (double) levenshtein(s1, s2) / maxLen;
	}

	/**
	 * Jaccard 相似度（字符集合交集/并集），0~1。
	 *
	 * @param a 字符串
	 * @param b 字符串
	 * @return Jaccard 相似度；两串均空返回 1
	 */
	public static double jaccard(String a, String b) {
		Set<Character> setA = new HashSet<>();
		Set<Character> setB = new HashSet<>();
		for (char c : (a == null ? "" : a).toCharArray()) {
			setA.add(c);
		}
		for (char c : (b == null ? "" : b).toCharArray()) {
			setB.add(c);
		}
		if (setA.isEmpty() && setB.isEmpty()) {
			return 1.0;
		}
		Set<Character> union = new HashSet<>(setA);
		union.addAll(setB);
		Set<Character> inter = new HashSet<>(setA);
		inter.retainAll(setB);
		return (double) inter.size() / union.size();
	}

	/**
	 * 余弦相似度（按字符频率向量），0~1。
	 *
	 * @param a 字符串
	 * @param b 字符串
	 * @return 余弦相似度；两串均空返回 1
	 */
	public static double cosine(String a, String b) {
		String s1 = a == null ? "" : a;
		String s2 = b == null ? "" : b;
		Map<Character, Integer> freqA = freq(s1);
		Map<Character, Integer> freqB = freq(s2);
		if (freqA.isEmpty() && freqB.isEmpty()) {
			return 1.0;
		}
		double dot = 0;
		double normA = 0;
		double normB = 0;
		for (int v : freqA.values()) {
			normA += (double) v * v;
		}
		for (int v : freqB.values()) {
			normB += (double) v * v;
		}
		Set<Character> keys = new HashSet<>(freqA.keySet());
		keys.retainAll(freqB.keySet());
		for (char c : keys) {
			dot += (double) freqA.get(c) * freqB.get(c);
		}
		double denom = Math.sqrt(normA) * Math.sqrt(normB);
		return denom == 0 ? 0 : dot / denom;
	}

	private static Map<Character, Integer> freq(String s) {
		Map<Character, Integer> map = new HashMap<>();
		for (char c : s.toCharArray()) {
			map.merge(c, 1, Integer::sum);
		}
		return map;
	}

	/**
	 * 完全相同返回 1.0，否则返回 {@link #similarity} 相似度分数。
	 *
	 * @param a 字符串
	 * @param b 字符串
	 * @return 1.0 或相似度
	 */
	public static double identical(String a, String b) {
		String s1 = a == null ? "" : a;
		String s2 = b == null ? "" : b;
		return s1.equals(s2) ? 1.0 : similarity(s1, s2);
	}
}
