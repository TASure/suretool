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
package com.sure.tool;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.sure.tool.util.StrSplitter;

/**
 * StrSplitter 测试。
 */
public class StrSplitterTest {

	@Test
	public void testSplitByChar() {
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.split("a,b,c", ','));
		assertEquals(Arrays.asList(""), StrSplitter.split("", ','));
		assertTrue(StrSplitter.split(null, ',').isEmpty());
	}

	@Test
	public void testSplitWithLimit() {
		assertEquals(Arrays.asList("a", "b,c"), StrSplitter.split("a,b,c", ',', 2, false, false));
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.split("a,b,c", ',', 0, false, false));
	}

	@Test
	public void testSplitTrimIgnoreEmpty() {
		assertEquals(Arrays.asList("a", "b"), StrSplitter.split(" a ,, b ", ',', 0, true, true));
		assertEquals(Arrays.asList("a", "", "b"), StrSplitter.split("a,,b", ',', 0, false, false));
		assertEquals(Arrays.asList("a", "b"), StrSplitter.split("a,,b", ',', 0, false, true));
	}

	@Test
	public void testSplitToArray() {
		assertArrayEquals(new String[]{"a", "b"}, StrSplitter.splitToArray("a,b", ','));
	}

	@Test
	public void testSplitByString() {
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.split("a::b::c", "::", false));
		assertEquals(Arrays.asList("a b"), StrSplitter.split("a b", "", false));
		assertTrue(StrSplitter.split(null, "::", false).isEmpty());
	}

	@Test
	public void testSplitByLength() {
		assertEquals(Arrays.asList("abc", "de"), StrSplitter.splitByLength("abcde", 3));
		assertEquals(Arrays.asList("abcde"), StrSplitter.splitByLength("abcde", 5));
		assertTrue(StrSplitter.splitByLength("abcde", 0).isEmpty());
		assertTrue(StrSplitter.splitByLength(null, 2).isEmpty());
	}

	@Test
	public void testSplitPath() {
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.splitPath("a/b/c"));
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.splitPath("a\\b\\c"));
		assertEquals(Arrays.asList("a", "b", "c"), StrSplitter.splitPath("a\\b/c"));
		assertEquals(Arrays.asList("a", "b/c"), StrSplitter.splitPath("a/b/c", 2));
		assertTrue(StrSplitter.splitPath(null).isEmpty());
	}
}
