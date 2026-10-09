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

import org.junit.Assert;
import org.junit.Test;

/**
 * Multiset 覆盖率补测：entrySet/toString 分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class MultisetGapTest {

	@Test
	public void testEntrySetAndToString() {
		Multiset<String> m = new Multiset<>();
		m.add("a", 3);
		m.add("b");
		Assert.assertEquals(4, m.size());
		Assert.assertEquals(2, m.entrySet().size());
		Assert.assertEquals("a x3", m.entrySet().iterator().next().toString());
		Assert.assertTrue(m.toString().contains("a"));
		Assert.assertEquals(2, m.uniqueSize());
	}
}
