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
package com.sure.tool.dfa;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 敏感词树：基于前缀树（Trie）的 DFA 敏感词过滤，参考 Hutool 的 {@code WordTree} 设计。
 *
 * <p>支持停用词：敏感词中间出现停用词时自动跳过（如敏感词"大傻子"、停用词"说"，文本"大说傻子"仍命中）。
 *
 * <p><b>线程安全约定</b>：{@link #addWord} / {@link #addStopWord} 等写操作应在构建期完成（非线程安全），
 * 构建完成后可并发执行 {@link #match} / {@link #contains} 等只读操作。
 *
 * <pre>{@code
 * WordTree tree = new WordTree();
 * tree.addWords("赌博", "赌球");
 * boolean hit = tree.contains("我爱赌博"); // true
 * }</pre>
 *
 * @author suretool
 * @since 0.1.0
 */
public class WordTree {

	private final WordNode root = new WordNode();
	private WordNode stopRoot;

	/**
	 * 添加敏感词（自动去除首尾空白，null/空词忽略）。
	 *
	 * @param word 敏感词
	 * @return 本树（链式）
	 */
	public WordTree addWord(String word) {
		if (word == null || word.trim().isEmpty()) {
			return this;
		}
		word = word.trim();
		WordNode node = root;
		for (int i = 0; i < word.length(); i++) {
			node = node.getOrCreate(word.charAt(i));
		}
		node.end = true;
		node.word = word;
		return this;
	}

	/**
	 * 批量添加敏感词。
	 *
	 * @param words 敏感词数组
	 * @return 本树（链式）
	 */
	public WordTree addWords(String... words) {
		if (words != null) {
			for (String word : words) {
				addWord(word);
			}
		}
		return this;
	}

	/**
	 * 批量添加敏感词。
	 *
	 * @param words 敏感词集合
	 * @return 本树（链式）
	 */
	public WordTree addWords(Collection<String> words) {
		if (words != null) {
			for (String word : words) {
				addWord(word);
			}
		}
		return this;
	}

	/**
	 * 从文件加载敏感词（每行一个词，跳过空行）。
	 *
	 * @param file    词表文件
	 * @param charset 文件字符集
	 * @return 本树（链式）
	 * @throws IOException 文件读取失败
	 */
	public WordTree addWords(File file, Charset charset) throws IOException {
		if (file == null || !file.isFile()) {
			throw new IllegalArgumentException("词表文件不存在: " + file);
		}
		for (String line : Files.readAllLines(file.toPath(), charset)) {
			if (line != null && !line.trim().isEmpty()) {
				addWord(line);
			}
		}
		return this;
	}

	/**
	 * 添加停用词：匹配敏感词过程中自动跳过（自动去除首尾空白，null/空词忽略）。
	 *
	 * @param word 停用词
	 * @return 本树（链式）
	 */
	public WordTree addStopWord(String word) {
		if (word == null || word.trim().isEmpty()) {
			return this;
		}
		word = word.trim();
		if (stopRoot == null) {
			stopRoot = new WordNode();
		}
		WordNode node = stopRoot;
		for (int i = 0; i < word.length(); i++) {
			node = node.getOrCreate(word.charAt(i));
		}
		node.end = true;
		return this;
	}

	/**
	 * 批量添加停用词。
	 *
	 * @param words 停用词集合
	 * @return 本树（链式）
	 */
	public WordTree addStopWords(Collection<String> words) {
		if (words != null) {
			for (String word : words) {
				addStopWord(word);
			}
		}
		return this;
	}

	/**
	 * 匹配文本中的全部敏感词（含起止索引）。每个命中位置可能对应多个词（前缀重叠），
	 * 同一 (词, 起, 止) 只记录一次。
	 *
	 * @param text 待匹配文本
	 * @return 命中列表（空输入返回空列表）
	 */
	public List<FoundWord> match(String text) {
		List<FoundWord> result = new ArrayList<>();
		if (text == null) {
			return result;
		}
		int length = text.length();
		for (int start = 0; start < length; start++) {
			WordNode node = root;
			int i = start;
			// 敏感词实际起点：敏感词开始前跳过停用词时前移
			int wordStart = start;
			while (i < length) {
				WordNode next = node.getChild(text.charAt(i));
				if (next != null) {
					node = next;
					if (node.end) {
						FoundWord found = new FoundWord(node.word, wordStart, i);
						if (!result.contains(found)) {
							result.add(found);
						}
					}
					i++;
				} else {
					int skip = matchStop(text, i);
					if (skip > 0) {
						if (node == root) {
							wordStart = i + skip;
						}
						i += skip;
					} else {
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 匹配文本中的全部敏感词（去重保序，不含位置）。
	 *
	 * @param text 待匹配文本
	 * @return 去重后的敏感词列表
	 */
	public List<String> matchAll(String text) {
		List<FoundWord> found = match(text);
		LinkedHashSet<String> words = new LinkedHashSet<>();
		for (FoundWord word : found) {
			words.add(word.getWord());
		}
		return new ArrayList<>(words);
	}

	/**
	 * 文本是否完全等于某个敏感词（整词匹配，不涉及停用词）。
	 *
	 * @param text 待匹配文本
	 * @return 是否完全匹配
	 */
	public boolean isMatch(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}
		WordNode node = root;
		for (int i = 0; i < text.length(); i++) {
			WordNode next = node.getChild(text.charAt(i));
			if (next == null) {
				return false;
			}
			node = next;
		}
		return node.end;
	}

	/**
	 * 文本是否包含任意敏感词。
	 *
	 * @param text 待匹配文本
	 * @return 是否包含
	 */
	public boolean contains(String text) {
		return !match(text).isEmpty();
	}

	/**
	 * 敏感词总数。
	 *
	 * @return 敏感词数量
	 */
	public int getWordCount() {
		return count(root);
	}

	/**
	 * 从位置 i 开始匹配停用词，返回最长停用词长度，无匹配返回 0。
	 */
	private int matchStop(String text, int i) {
		if (stopRoot == null) {
			return 0;
		}
		WordNode node = stopRoot;
		int longest = 0;
		int j = i;
		int length = text.length();
		while (j < length) {
			WordNode next = node.getChild(text.charAt(j));
			if (next == null) {
				break;
			}
			node = next;
			j++;
			if (node.end) {
				longest = j - i;
			}
		}
		return longest;
	}

	/**
	 * 统计子树中词尾节点数。
	 */
	private static int count(WordNode node) {
		int total = node.end ? 1 : 0;
		for (WordNode child : node.children.values()) {
			total += count(child);
		}
		return total;
	}

	/**
	 * 前缀树节点。
	 */
	private static class WordNode {

		private final Map<Character, WordNode> children = new HashMap<>();
		private boolean end;
		private String word;

		private WordNode getOrCreate(char c) {
			WordNode child = children.get(c);
			if (child == null) {
				child = new WordNode();
				children.put(c, child);
			}
			return child;
		}

		private WordNode getChild(char c) {
			return children.get(c);
		}
	}
}
