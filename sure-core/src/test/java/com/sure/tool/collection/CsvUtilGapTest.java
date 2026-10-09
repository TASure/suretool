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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * CsvUtil 覆盖率补测：null 行与空字段守卫。
 *
 * @author suretool
 * @since 1.13.1
 */
public class CsvUtilGapTest {

	@Test
	public void testNullGuards() throws Exception {
		Assert.assertEquals("", CsvUtil.toCsv(null));
		Assert.assertEquals("a", CsvUtil.toCsv(Collections.singletonList(Collections.singletonList("a"))));
		Assert.assertTrue(CsvUtil.read((java.io.InputStream) null, java.nio.charset.StandardCharsets.UTF_8).isEmpty());
	}
}
