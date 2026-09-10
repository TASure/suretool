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

import java.util.Objects;

/**
 * 敏感词命中结果：命中的词 + 在文本中的起止索引（含两端），参考 Hutool 的 {@code FoundWord} 设计。
 * 不可变对象，可作为 Map/Set 键使用。
 *
 * @author suretool
 * @since 0.1.0
 */
public class FoundWord {

	private final String word;
	private final int startIndex;
	private final int endIndex;

	/**
	 * 创建命中结果。
	 *
	 * @param word       命中的敏感词
	 * @param startIndex 起始索引（含）
	 * @param endIndex   结束索引（含）
	 */
	public FoundWord(String word, int startIndex, int endIndex) {
		this.word = Objects.requireNonNull(word, "词不能为 null");
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * 命中的敏感词。
	 *
	 * @return 敏感词
	 */
	public String getWord() {
		return word;
	}

	/**
	 * 起始索引（含）。
	 *
	 * @return 起始索引
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * 结束索引（含）。
	 *
	 * @return 结束索引
	 */
	public int getEndIndex() {
		return endIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FoundWord)) {
			return false;
		}
		FoundWord that = (FoundWord) o;
		return startIndex == that.startIndex && endIndex == that.endIndex && word.equals(that.word);
	}

	@Override
	public int hashCode() {
		int result = word.hashCode();
		result = 31 * result + startIndex;
		result = 31 * result + endIndex;
		return result;
	}

	@Override
	public String toString() {
		return word + "[" + startIndex + "," + endIndex + "]";
	}
}
