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
 * IdcardUtil 覆盖率补测：非法输入、15/18 位转换、生日与性别、闰年分支。
 *
 * @author suretool
 * @since 1.13.1
 */
public class IdcardUtilGapTest {

	private String valid18() {
		return IdcardUtil.convert15To18("110105491231001");
	}

	@Test
	public void testInvalidInputs() {
		Assert.assertFalse(IdcardUtil.isValidCard15(null));
		Assert.assertFalse(IdcardUtil.isValidCard15("bad"));
		try {
			IdcardUtil.convert15To18("bad");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			IdcardUtil.calculateCheckCode("short");
			Assert.fail("应抛异常");
		} catch (IllegalArgumentException e) {
			// expected
		}
		Assert.assertNull(IdcardUtil.getBirthDate("bad"));
		Assert.assertNull(IdcardUtil.getGender("bad"));
		Assert.assertNull(IdcardUtil.getProvinceCode(null));
		Assert.assertNull(IdcardUtil.getProvinceCode("bad"));
		Assert.assertEquals(-1, IdcardUtil.getAge("bad"));
	}

	@Test
	public void testValid15And18() {
		Assert.assertTrue(IdcardUtil.isValidCard15("110105491231001"));
		String card18 = valid18();
		Assert.assertEquals("1949-12-31", IdcardUtil.getBirthDate(card18));
		Assert.assertEquals("1949-12-31", IdcardUtil.getBirthDate("110105491231001"));
		Assert.assertNotNull(IdcardUtil.getGender(card18));
		Assert.assertNotNull(IdcardUtil.getGender("110105491231001"));
		Assert.assertEquals("11", IdcardUtil.getProvinceCode(card18));
		Assert.assertTrue(IdcardUtil.getAge(card18) >= 0);
	}

	@Test
	public void testInvalidBirthDate() {
		// 月份 13
		Assert.assertNull(IdcardUtil.getBirthDate("110105199913011234"));
		// 4 月只有 30 天，31 日非法
		Assert.assertNull(IdcardUtil.getBirthDate("110105199904311234"));
		// 2 月非闰年 29 日
		Assert.assertNull(IdcardUtil.getBirthDate("110105199902291234"));
	}
}
