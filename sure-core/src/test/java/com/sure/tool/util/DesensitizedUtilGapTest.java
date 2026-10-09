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
package com.sure.tool.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * DesensitizedUtil 覆盖率补测：短串原样返回与 mask 边界钳制分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class DesensitizedUtilGapTest {

	@Test
	public void testShortInputsReturnAsIs() {
		Assert.assertEquals("1234567", DesensitizedUtil.bankCard("1234567"));
		Assert.assertNull(DesensitizedUtil.email(null));
		Assert.assertEquals("ab", DesensitizedUtil.address("ab"));
		Assert.assertEquals("北京市", DesensitizedUtil.address("北京市"));
		Assert.assertNull(DesensitizedUtil.ipv4(null));
		Assert.assertNull(DesensitizedUtil.mask(null, 1, 2));
	}

	@Test
	public void testMaskClamp() {
		Assert.assertEquals("***", DesensitizedUtil.mask("abc", -1, 10));
		Assert.assertEquals("a**", DesensitizedUtil.mask("abc", 1, 5));
		Assert.assertEquals("abc", DesensitizedUtil.mask("abc", 2, 1));
		Assert.assertEquals("138****5678", DesensitizedUtil.mobilePhone("13812345678"));
		Assert.assertEquals("a***@b.com", DesensitizedUtil.email("ab@b.com"));
		Assert.assertEquals("192.168.*.*", DesensitizedUtil.ipv4("192.168.1.1"));
	}
}
