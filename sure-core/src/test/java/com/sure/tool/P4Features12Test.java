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

import com.sure.tool.util.StrUtil;

/**
 * P4（v0.2.0）第十六/十七批补充测试。
 */
public class P4Features12Test {

	@Test
	public void testBatch16() {
		// DesensitizedUtil.carLicense
		Assert.assertEquals("陕A***45", com.sure.tool.util.DesensitizedUtil.carLicense("陕A12345"));
		Assert.assertEquals("粤B***88", com.sure.tool.util.DesensitizedUtil.carLicense("粤B66688"));
		Assert.assertNull(com.sure.tool.util.DesensitizedUtil.carLicense(null));

		// ImageUtil.toBase64 / toDataUri
		try {
			java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB);
			String b64 = com.sure.tool.image.ImageUtil.toBase64(img, "png");
			Assert.assertNotNull(b64);
			Assert.assertTrue(b64.length() > 0);
			String uri = com.sure.tool.image.ImageUtil.toDataUri(img, "png");
			Assert.assertTrue(uri.startsWith("data:image/png;base64,"));
		} catch (Exception e) {
			Assert.fail("ImageUtil base64 异常: " + e);
		}

		// RandomUtil.randomEleWeighted
		java.util.List<com.sure.tool.lang.WeightRandom.WeightObj<String>> weights = new java.util.ArrayList<>();
		weights.add(new com.sure.tool.lang.WeightRandom.WeightObj<>("A", 10));
		weights.add(new com.sure.tool.lang.WeightRandom.WeightObj<>("B", 90));
		String picked = com.sure.tool.util.RandomUtil.randomEleWeighted(weights);
		Assert.assertTrue("A".equals(picked) || "B".equals(picked));

		// DateUtil.beginOfQuarter / endOfQuarter
		java.util.Date q = com.sure.tool.date.DateUtil.parse("2026-05-20");
		java.util.Date qBegin = com.sure.tool.date.DateUtil.beginOfQuarter(q);
		java.util.Date qEnd = com.sure.tool.date.DateUtil.endOfQuarter(q);
		Assert.assertEquals("2026-04-01 00:00:00", com.sure.tool.date.DateUtil.format(qBegin, "yyyy-MM-dd HH:mm:ss"));
		Assert.assertEquals("2026-06-30 23:59:59", com.sure.tool.date.DateUtil.format(qEnd, "yyyy-MM-dd HH:mm:ss"));

		// ConvertUtil.toEnum
		Assert.assertEquals(java.util.concurrent.TimeUnit.SECONDS, com.sure.tool.util.ConvertUtil.toEnum("SECONDS", java.util.concurrent.TimeUnit.class));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toEnum("BAD", java.util.concurrent.TimeUnit.class));
		Assert.assertNull(com.sure.tool.util.ConvertUtil.toEnum(null, java.util.concurrent.TimeUnit.class));
	}
	@Test
	public void testBatch17() {
		// StrUtil.splitTrim / blankToDefault
		Assert.assertArrayEquals(new String[] {"a", "b", ""}, StrUtil.splitTrim(" a , b , ", ","));
		Assert.assertArrayEquals(new String[] {"x"}, StrUtil.splitTrim(" x ", ""));
		Assert.assertEquals("def", StrUtil.blankToDefault("", "def"));
		Assert.assertEquals(" abc ", StrUtil.blankToDefault(" abc ", "def"));

		// EscapeUtil.escapeXml / unescapeXml
		String xml = "<a x=\"1\">&</a>";
		String esc = com.sure.tool.util.EscapeUtil.escapeXml(xml);
		Assert.assertTrue(esc.contains("&lt;"));
		Assert.assertTrue(esc.contains("&amp;"));
		Assert.assertEquals(xml, com.sure.tool.util.EscapeUtil.unescapeXml(esc));

		// DateUtil.age
		java.util.Date birth = com.sure.tool.date.DateUtil.parse("2000-06-01", "yyyy-MM-dd");
		java.util.Date ref = com.sure.tool.date.DateUtil.parse("2026-09-16", "yyyy-MM-dd");
		Assert.assertEquals(26, com.sure.tool.date.DateUtil.age(birth, ref));
		java.util.Date refBefore = com.sure.tool.date.DateUtil.parse("2026-05-01", "yyyy-MM-dd");
		Assert.assertEquals(25, com.sure.tool.date.DateUtil.age(birth, refBefore));

		// NumberUtil.isNumber
		Assert.assertTrue(com.sure.tool.util.NumberUtil.isNumber("3.14"));
		Assert.assertTrue(com.sure.tool.util.NumberUtil.isNumber("-1.5e3"));
		Assert.assertTrue(com.sure.tool.util.NumberUtil.isNumber("100"));
		Assert.assertFalse(com.sure.tool.util.NumberUtil.isNumber("12a"));
		Assert.assertFalse(com.sure.tool.util.NumberUtil.isNumber(null));
	}
}
