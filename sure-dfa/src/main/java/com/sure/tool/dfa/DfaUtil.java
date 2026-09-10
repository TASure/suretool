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

import java.util.Collection;
import java.util.List;

/**
 * 敏感词过滤门面：基于默认 {@link WordTree} 单例的静态方法，适合快速过滤场景。
 * 如需独立词库实例（不同上下文不同敏感词），直接使用 {@link WordTree}。
 *
 * <pre>{@code
 * DfaUtil.addWord("赌博");
 * boolean hit = DfaUtil.contains("我爱赌博"); // true
 * }</pre>
 *
 * @author suretool
 * @since 0.1.0
 */
public class DfaUtil {

	private static final WordTree DEFAULT_TREE = new WordTree();

	private DfaUtil() {
	}

	/**
	 * 添加敏感词（默认词库）。
	 *
	 * @param word 敏感词
	 * @return 默认词库树（链式）
	 */
	public static WordTree addWord(String word) {
		return DEFAULT_TREE.addWord(word);
	}

	/**
	 * 批量添加敏感词（默认词库）。
	 *
	 * @param words 敏感词数组
	 * @return 默认词库树（链式）
	 */
	public static WordTree addWords(String... words) {
		return DEFAULT_TREE.addWords(words);
	}

	/**
	 * 批量添加敏感词（默认词库）。
	 *
	 * @param words 敏感词集合
	 * @return 默认词库树（链式）
	 */
	public static WordTree addWords(Collection<String> words) {
		return DEFAULT_TREE.addWords(words);
	}

	/**
	 * 添加停用词（默认词库）。
	 *
	 * @param word 停用词
	 * @return 默认词库树（链式）
	 */
	public static WordTree addStopWord(String word) {
		return DEFAULT_TREE.addStopWord(word);
	}

	/**
	 * 匹配文本中的全部敏感词（默认词库，含起止索引）。
	 *
	 * @param text 待匹配文本
	 * @return 命中列表
	 */
	public static List<FoundWord> match(String text) {
		return DEFAULT_TREE.match(text);
	}

	/**
	 * 匹配文本中的全部敏感词（默认词库，去重保序）。
	 *
	 * @param text 待匹配文本
	 * @return 去重后的敏感词列表
	 */
	public static List<String> matchAll(String text) {
		return DEFAULT_TREE.matchAll(text);
	}

	/**
	 * 文本是否完全等于某个敏感词（默认词库）。
	 *
	 * @param text 待匹配文本
	 * @return 是否完全匹配
	 */
	public static boolean isMatch(String text) {
		return DEFAULT_TREE.isMatch(text);
	}

	/**
	 * 文本是否包含任意敏感词（默认词库）。
	 *
	 * @param text 待匹配文本
	 * @return 是否包含
	 */
	public static boolean contains(String text) {
		return DEFAULT_TREE.contains(text);
	}
}
