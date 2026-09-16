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

import org.junit.Assert;
import org.junit.Test;

/**
 * P4（v0.2.0）第十二批：DfaUtil 敏感词替换测试。
 */
public class P4DfaTest {

	@Test
	public void testReplace() {
		DfaUtil.addWord("赌博");
		DfaUtil.addWord("色情");
		String text = "禁止赌博和色情内容";
		Assert.assertEquals("禁止**和**内容", DfaUtil.replace(text, '*'));
		Assert.assertEquals("没有违规内容", DfaUtil.replace("没有违规内容", '*'));
		Assert.assertNull(DfaUtil.replace(null, '*'));
		Assert.assertEquals("", DfaUtil.replace("", '*'));
	}

	@Test
	public void testReplaceString() {
		com.sure.tool.dfa.DfaUtil.addWords("敏感", "词库");
		Assert.assertEquals("****测试**测试", com.sure.tool.dfa.DfaUtil.replace("敏感词库测试敏感测试", "**"));
	}


}
