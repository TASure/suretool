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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第二十二批测试。
 */
public class P4Features17Test {

	@Test
	public void testBatch22() {
		// StrUtil.strip
		Assert.assertEquals("hello", StrUtil.strip("--hello--", '-'));
		Assert.assertEquals("hello", StrUtil.strip("  hello  "));
		Assert.assertEquals("", StrUtil.strip("---", '-'));
		Assert.assertNull(StrUtil.strip(null, '-'));

		// CollUtil.minBy / maxBy
		List<String> words = List.of("apple", "banana", "kiwi");
		Assert.assertEquals("kiwi", com.sure.tool.collection.CollUtil.minBy(words, String::length));
		Assert.assertEquals("banana", com.sure.tool.collection.CollUtil.maxBy(words, String::length));
		Assert.assertNull(com.sure.tool.collection.CollUtil.minBy(List.of(), String::length));

		// ValidatorUtil.isLowerCase / isUpperCase
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isLowerCase("hello123"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isLowerCase("Hello"));
		Assert.assertTrue(com.sure.tool.util.ValidatorUtil.isUpperCase("HELLO123"));
		Assert.assertFalse(com.sure.tool.util.ValidatorUtil.isUpperCase("hello"));

		// BooleanUtil.toStringCn
		Assert.assertEquals("是", com.sure.tool.util.BooleanUtil.toStringCn(true));
		Assert.assertEquals("否", com.sure.tool.util.BooleanUtil.toStringCn(false));
	}
}
