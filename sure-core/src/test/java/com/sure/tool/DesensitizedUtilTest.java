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

import com.sure.tool.util.DesensitizedUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * DesensitizedUtil 单元测试。
 */
public class DesensitizedUtilTest {

	@Test
	public void testMobilePhone() {
		Assert.assertEquals("138****5678", DesensitizedUtil.mobilePhone("13812345678"));
		Assert.assertEquals("12345", DesensitizedUtil.mobilePhone("12345"));
	}

	@Test
	public void testIdCardNum() {
		Assert.assertEquals("110***********1234", DesensitizedUtil.idCardNum("110101199001011234"));
		Assert.assertEquals("1101", DesensitizedUtil.idCardNum("1101"));
	}

	@Test
	public void testBankCard() {
		Assert.assertEquals("6222************1234", DesensitizedUtil.bankCard("62220212345678901234"));
	}

	@Test
	public void testEmail() {
		Assert.assertEquals("a***@example.com", DesensitizedUtil.email("abc@example.com"));
		Assert.assertEquals("a@b", DesensitizedUtil.email("a@b"));
	}

	@Test
	public void testPassword() {
		Assert.assertEquals("******", DesensitizedUtil.password("secret123"));
	}

	@Test
	public void testChineseName() {
		Assert.assertEquals("张*丰", DesensitizedUtil.chineseName("张三丰"));
		Assert.assertEquals("张*", DesensitizedUtil.chineseName("张三"));
		Assert.assertEquals("李", DesensitizedUtil.chineseName("李"));
	}

	@Test
	public void testAddress() {
		String masked = DesensitizedUtil.address("北京市朝阳区望京街道阜通东大街");
		Assert.assertTrue(masked.contains("****"));
		Assert.assertFalse(masked.contains("朝阳区望京"));
	}

	@Test
	public void testIpv4() {
		Assert.assertEquals("192.168.*.*", DesensitizedUtil.ipv4("192.168.1.100"));
		Assert.assertEquals("192.168.1.100.1", DesensitizedUtil.ipv4("192.168.1.100.1"));
	}

	@Test
	public void testMask() {
		Assert.assertEquals("1***5", DesensitizedUtil.mask("12345", 1, 4));
		Assert.assertEquals("12345", DesensitizedUtil.mask("12345", 3, 2));
	}
}