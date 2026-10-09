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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

/**
 * WordTree / DfaUtil / FoundWord 边界补测：停用词批量加载、文件加载异常、替换空/无命中、equals 分支。
 */
public class DfaExtraTest {

	@Test
	public void 批量停用词加载() {
		final WordTree tree = new WordTree();
		tree.addStopWords(List.of("的", "了"));
		tree.addWords("赌博");
		// 含停用词仍命中，顺带验证停用词树被使用
		assertTrue(tree.contains("赌的博"));
	}

	@Test
	public void 词表文件不存在抛异常() {
		assertThrows(IllegalArgumentException.class,
				() -> new WordTree().addWords(new File("/suretool/no-such-file-词表.txt"), StandardCharsets.UTF_8));
	}

	@Test
	public void DfaUtil批量添加集合与替换分支() {
		DfaUtil.addWords(List.of("ZZ唯一敏感词"));
		// 空文本原样返回
		assertEquals("", DfaUtil.replace("", "*"));
		// 无命中文本原样返回
		final String plain = "甲乙丙丁戊己庚辛壬癸";
		assertEquals(plain, DfaUtil.replace(plain, "*"));
	}

	@Test
	public void FoundWordEquals自反与异类型() {
		final FoundWord word = new FoundWord("赌博", 0, 1);
		assertTrue(word.equals(word));
		assertFalse(word.equals("字符串而非FoundWord"));
		assertNotEquals(word, new FoundWord("赌博", 1, 2));
	}
}
