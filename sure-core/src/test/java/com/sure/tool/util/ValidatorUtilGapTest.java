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
 * ValidatorUtil 覆盖率补测：IPv6/MAC/大小写/时间/生日/通用字符分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class ValidatorUtilGapTest {

	@Test
	public void testIpv6AndMac() {
		Assert.assertFalse(ValidatorUtil.isIpv6(null));
		Assert.assertFalse(ValidatorUtil.isIpv6(""));
		Assert.assertTrue(ValidatorUtil.isIpv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
		Assert.assertFalse(ValidatorUtil.isIpv6("1:2:3"));
		Assert.assertFalse(ValidatorUtil.isIpv6("12345:12345:12345:12345:12345:12345:12345:1234"));
		Assert.assertTrue(ValidatorUtil.isMac("aa:bb:cc:dd:ee:ff"));
		Assert.assertFalse(ValidatorUtil.isMac(null));
	}

	@Test
	public void testCaseAndTime() {
		Assert.assertFalse(ValidatorUtil.isLowerCase(null));
		Assert.assertFalse(ValidatorUtil.isLowerCase("ABc"));
		Assert.assertTrue(ValidatorUtil.isLowerCase("abc1"));
		Assert.assertFalse(ValidatorUtil.isUpperCase(null));
		Assert.assertFalse(ValidatorUtil.isUpperCase("aBc"));
		Assert.assertTrue(ValidatorUtil.isUpperCase("ABC1"));
		Assert.assertFalse(ValidatorUtil.isTime(null));
		Assert.assertFalse(ValidatorUtil.isTime("bad"));
		Assert.assertTrue(ValidatorUtil.isTime("12:30:00"));
	}

	@Test
	public void testDecimalBirthdayGeneral() {
		Assert.assertFalse(ValidatorUtil.isDecimal(null));
		Assert.assertTrue(ValidatorUtil.isDecimal("3.14"));
		Assert.assertFalse(ValidatorUtil.isBirthday("bad"));
		Assert.assertTrue(ValidatorUtil.isBirthday("19900101"));
		Assert.assertFalse(ValidatorUtil.isGeneralWithChinese(null));
		Assert.assertTrue(ValidatorUtil.isGeneralWithChinese("abc_中文1"));
		Assert.assertFalse(ValidatorUtil.isGeneralWithChinese("abc!"));
	}
}
