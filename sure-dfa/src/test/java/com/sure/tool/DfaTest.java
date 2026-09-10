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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * WordTree / FoundWord / DfaUtil 测试。
 */
public class DfaTest {

	@Test
	public void testMatchBasic() {
		WordTree tree = new WordTree();
		tree.addWords("赌博", "赌球");
		List<FoundWord> found = tree.match("我爱赌博，赌球很危险");
		assertEquals(2, found.size());
		assertEquals(new FoundWord("赌博", 2, 3), found.get(0));
		assertEquals(new FoundWord("赌球", 5, 6), found.get(1));
	}

	@Test
	public void testMatchOverlapPrefix() {
		WordTree tree = new WordTree();
		tree.addWords("赌", "赌博");
		List<FoundWord> found = tree.match("赌博");
		assertEquals(2, found.size());
		assertEquals(new FoundWord("赌", 0, 0), found.get(0));
		assertEquals(new FoundWord("赌博", 0, 1), found.get(1));
	}

	@Test
	public void testMatchAllDedup() {
		WordTree tree = new WordTree();
		tree.addWord("赌");
		assertEquals(Collections.singletonList("赌"), tree.matchAll("赌赌赌"));
	}

	@Test
	public void testIsMatch() {
		WordTree tree = new WordTree();
		tree.addWord("赌博");
		assertTrue(tree.isMatch("赌博"));
		assertFalse(tree.isMatch("我爱赌博"));
		assertFalse(tree.isMatch(""));
		assertFalse(tree.isMatch(null));
	}

	@Test
	public void testContains() {
		WordTree tree = new WordTree();
		tree.addWord("赌博");
		assertTrue(tree.contains("我爱赌博"));
		assertFalse(tree.contains("我爱学习"));
		assertFalse(tree.contains(null));
		assertFalse(tree.contains(""));
	}

	@Test
	public void testStopWordInMiddle() {
		WordTree tree = new WordTree();
		tree.addWord("大傻子");
		tree.addStopWord("说");
		assertEquals(Collections.singletonList(new FoundWord("大傻子", 0, 3)),
				tree.match("大说傻子"));
	}

	@Test
	public void testStopWordBeforeMatch() {
		WordTree tree = new WordTree();
		tree.addWord("大傻子");
		tree.addStopWord("说");
		assertEquals(Collections.singletonList(new FoundWord("大傻子", 1, 3)),
				tree.match("说大傻子"));
	}

	@Test
	public void testStopWordMultiChar() {
		WordTree tree = new WordTree();
		tree.addWord("赌博");
		tree.addStopWord("和与");
		assertEquals(Collections.singletonList(new FoundWord("赌博", 0, 3)),
				tree.match("赌和与博"));
	}

	@Test
	public void testStopWordNotAffectOtherMatches() {
		WordTree tree = new WordTree();
		tree.addWords("赌博", "比赛");
		tree.addStopWord("和");
		List<FoundWord> found = tree.match("比赛和赌博");
		assertEquals(2, found.size());
		assertEquals(new FoundWord("比赛", 0, 1), found.get(0));
		assertEquals(new FoundWord("赌博", 3, 4), found.get(1));
	}

	@Test
	public void testEnglishWord() {
		WordTree tree = new WordTree();
		tree.addWord("fuck");
		assertTrue(tree.contains("what the fuck"));
		assertEquals(Collections.singletonList(new FoundWord("fuck", 9, 12)),
				tree.match("what the fuck"));
	}

	@Test
	public void testAddWordsFile() throws Exception {
		File file = File.createTempFile("sure-dfa", ".txt");
		file.deleteOnExit();
		Files.write(file.toPath(), Arrays.asList("赌博", "  ", "赌球"), StandardCharsets.UTF_8);
		WordTree tree = new WordTree();
		tree.addWords(file, StandardCharsets.UTF_8);
		assertTrue(tree.contains("赌球"));
		assertFalse(tree.contains("比赛"));
		assertEquals(2, tree.getWordCount());
	}

	@Test
	public void testWordCount() {
		WordTree tree = new WordTree();
		tree.addWords("赌", "赌博", "赌球");
		assertEquals(3, tree.getWordCount());
		assertEquals(0, new WordTree().getWordCount());
	}

	@Test
	public void testAddWordTrimAndIgnore() {
		WordTree tree = new WordTree();
		tree.addWord("  赌博  ");
		tree.addWord(null);
		tree.addWord("  ");
		assertTrue(tree.contains("赌博"));
		assertEquals(1, tree.getWordCount());
	}

	@Test
	public void testAddStopWordIgnore() {
		WordTree tree = new WordTree();
		tree.addWord("大傻子");
		tree.addStopWord(null);
		tree.addStopWord(" ");
		assertEquals(Collections.singletonList(new FoundWord("大傻子", 0, 2)),
				tree.match("大傻子"));
	}

	@Test
	public void testFoundWordValue() {
		FoundWord a = new FoundWord("赌", 0, 1);
		FoundWord b = new FoundWord("赌", 0, 1);
		FoundWord c = new FoundWord("赌", 0, 2);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
		assertFalse(a.equals(c));
		assertEquals("赌[0,1]", a.toString());
	}

	@Test
	public void testDfaUtil() {
		DfaUtil.addWord("赌博");
		DfaUtil.addStopWord("和");
		assertTrue(DfaUtil.contains("赌和博"));
		assertEquals(Collections.singletonList(new FoundWord("赌博", 0, 2)),
				DfaUtil.match("赌和博"));
		assertEquals(Collections.singletonList("赌博"), DfaUtil.matchAll("赌和博"));
		assertTrue(DfaUtil.isMatch("赌博"));
	}

	@Test
	public void testAddWordsCollection() {
		WordTree tree = new WordTree();
		tree.addWords(Arrays.asList("赌博", "赌球"));
		assertTrue(tree.contains("赌球"));
		assertEquals(2, tree.getWordCount());
	}

	@Test
	public void testMatchNullAndEmpty() {
		WordTree tree = new WordTree();
		tree.addWord("赌");
		assertTrue(tree.match(null).isEmpty());
		assertTrue(tree.match("").isEmpty());
		assertTrue(tree.matchAll(null).isEmpty());
	}
}
