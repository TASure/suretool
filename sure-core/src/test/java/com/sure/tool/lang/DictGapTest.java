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
package com.sure.tool.lang;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Dict 覆盖率补测：缺失键、字符串转换与 NumberFormatException 回退分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class DictGapTest {

	@Test
	public void testGetConversions() {
		Dict d = Dict.of("num", 42, "str", "100", "bi", BigInteger.valueOf(7), "bad", "abc");
		Assert.assertNull(d.getLong("missing"));
		Assert.assertEquals(Long.valueOf(42L), d.getLong("num"));
		Assert.assertEquals(Long.valueOf(100L), d.getLong("str"));
		Assert.assertEquals(BigInteger.valueOf(7L), d.getBigInteger("bi"));
		Assert.assertNull(d.getBigInteger("bad"));
		Assert.assertNull(d.getDouble("missing"));
		Assert.assertEquals(Double.valueOf(42d), d.getDouble("num"));
	}
}
