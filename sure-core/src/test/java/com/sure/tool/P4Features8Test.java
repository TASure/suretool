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

import com.sure.tool.collection.CollUtil;
import com.sure.tool.collection.MapUtil;
import com.sure.tool.date.DateUtil;
import com.sure.tool.util.BankCardUtil;
import com.sure.tool.util.CreditCodeUtil;
import com.sure.tool.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * P4（v0.2.0）第八批新增/增强单元测试：CreditCodeUtil / BankCardUtil / DateUtil / StrUtil / CollUtil / MapUtil。
 */
public class P4Features8Test {

	// ---------------- CreditCodeUtil ----------------

	@Test
	public void testCreditCode() {
		// 示例合法代码（GB 32100-2015 校验通过）
		Assert.assertTrue(CreditCodeUtil.isValidCreditCode("91110000MA001B5M6D"));
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode("91110000MA001B5M6X"));
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode(null));
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode("12345"));
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode("9O110000MA001B5M6X")); // 含 O
		Assert.assertFalse(CreditCodeUtil.isValidCreditCode("9I110000MA001B5M6X")); // 含 I

		Assert.assertEquals("91************5M6D", CreditCodeUtil.hide("91110000MA001B5M6D"));
		Assert.assertNull(CreditCodeUtil.hide(null));
	}

	// ---------------- BankCardUtil ----------------

	@Test
	public void testBankCard() {
		// 用动态生成的合法 Luhn 卡号验证（避免硬编码假号）
		String validCard = buildLuhnCard("622202123456789");
		Assert.assertTrue(BankCardUtil.isValid(validCard));
		Assert.assertFalse(BankCardUtil.isValid(validCard.substring(0, validCard.length() - 1) + "0"));
		Assert.assertFalse(BankCardUtil.isValid("abc"));
		Assert.assertFalse(BankCardUtil.isValid(""));
		Assert.assertFalse(BankCardUtil.isValid(null));

		Assert.assertEquals("6222********" + validCard.substring(12), BankCardUtil.hide(validCard));
		Assert.assertEquals("1234567", BankCardUtil.hide("1234567"));
		Assert.assertNull(BankCardUtil.hide(null));
	}

	/** 生成合法 Luhn 卡号：前 15 位 + 计算校验位。 */
	private static String buildLuhnCard(String prefix) {
		int sum = 0;
		boolean dbl = true;
		for (int i = prefix.length() - 1; i >= 0; i--) {
			int d = prefix.charAt(i) - '0';
			if (dbl) {
				d *= 2;
				if (d > 9) {
					d -= 9;
				}
			}
			sum += d;
			dbl = !dbl;
		}
		int check = (10 - sum % 10) % 10;
		return prefix + check;
	}

	// ---------------- DateUtil 增强 ----------------

	@Test
	public void testDateEnhance4() throws Exception {
		Date start = DateUtil.parse("2026-09-16 10:00:00", DateUtil.NORM_DATETIME_PATTERN);
		Date end = DateUtil.parse("2026-09-18 13:05:10", DateUtil.NORM_DATETIME_PATTERN);
		Assert.assertEquals("2天3小时5分10秒", DateUtil.formatBetween(start, end));
		Assert.assertEquals("1小时0分0秒", DateUtil.formatBetween(start, DateUtil.offsetHour(start, 1)));

		// 2026-09-16 周三非周末；2026-09-19 周六是周末
		Date wed = DateUtil.parse("2026-09-16", DateUtil.NORM_DATE_PATTERN);
		Date sat = DateUtil.parse("2026-09-19", DateUtil.NORM_DATE_PATTERN);
		Assert.assertFalse(DateUtil.isWeekend(wed));
		Assert.assertTrue(DateUtil.isWeekend(sat));

		Assert.assertEquals("2026-09-23", DateUtil.formatDate(DateUtil.offsetWeek(wed, 1)));
		Assert.assertEquals("2026-09-09", DateUtil.formatDate(DateUtil.offsetWeek(wed, -1)));
	}

	// ---------------- StrUtil 增强 ----------------

	@Test
	public void testStrEnhance2() {
		Assert.assertEquals(0, StrUtil.indexOf("abcabc", "abc"));
		Assert.assertEquals(3, StrUtil.lastIndexOf("abcabc", "abc"));
		Assert.assertEquals(-1, StrUtil.indexOf("abc", "x"));
		Assert.assertEquals(-1, StrUtil.indexOf(null, "a"));
		Assert.assertEquals(-1, StrUtil.indexOf("abc", null));
		Assert.assertEquals(-1, StrUtil.lastIndexOf("abc", ""));
	}

	// ---------------- CollUtil / MapUtil 增强 ----------------

	@Test
	public void testCollAndMapEnhance2() {
		List<DemoBean> list = new ArrayList<>();
		list.add(new DemoBean("b", 2));
		list.add(new DemoBean("a", 3));
		list.add(new DemoBean("c", 1));

		List<DemoBean> asc = CollUtil.sortByProperty(list, "name", true);
		Assert.assertEquals(List.of("a", "b", "c"), asc.stream().map(d -> d.name).toList());
		List<DemoBean> desc = CollUtil.sortByProperty(list, "age", false);
		Assert.assertEquals(List.of(3, 2, 1), desc.stream().map(d -> d.age).toList());
		Assert.assertEquals(0, CollUtil.sortByProperty(new ArrayList<DemoBean>(), "name", true).size());
		Assert.assertEquals(3, list.size());

		Map<String, Object> map = new HashMap<>();
		map.put("a", 1);
		map.put("b", "x");
		java.util.Properties props = MapUtil.toProperties(map);
		Assert.assertEquals("1", props.getProperty("a"));
		Assert.assertEquals("x", props.getProperty("b"));
		Assert.assertEquals(0, MapUtil.toProperties(new HashMap<>()).size());
	}

	/** 排序测试 Bean。 */
	public static class DemoBean {
		public final String name;
		public final int age;

		public DemoBean(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}
	}
}
