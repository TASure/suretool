package com.sure.tool;

import com.sure.tool.lang.PatternPool;
import com.sure.tool.util.IdcardUtil;
import com.sure.tool.util.ValidatorUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * ValidatorUtil / IdcardUtil 单元测试。
 */
public class ValidatorUtilTest {

	@Test
	public void testEmail() {
		Assert.assertTrue(ValidatorUtil.isEmail("test@example.com"));
		Assert.assertTrue(ValidatorUtil.isEmail("a.b+c@sub.domain.cn"));
		Assert.assertFalse(ValidatorUtil.isEmail("test@"));
		Assert.assertFalse(ValidatorUtil.isEmail("test@.com"));
		Assert.assertFalse(ValidatorUtil.isEmail(null));
	}

	@Test
	public void testMobile() {
		Assert.assertTrue(ValidatorUtil.isMobile("13800138000"));
		Assert.assertTrue(ValidatorUtil.isMobile("19912345678"));
		Assert.assertFalse(ValidatorUtil.isMobile("12800138000"));
		Assert.assertFalse(ValidatorUtil.isMobile("1380013800"));
		Assert.assertFalse(ValidatorUtil.isMobile("23800138000"));
	}

	@Test
	public void testIpv4Url() {
		Assert.assertTrue(ValidatorUtil.isIpv4("192.168.1.1"));
		Assert.assertTrue(ValidatorUtil.isIpv4("255.255.255.255"));
		Assert.assertFalse(ValidatorUtil.isIpv4("256.1.1.1"));
		Assert.assertFalse(ValidatorUtil.isIpv4("192.168.1"));
		Assert.assertTrue(ValidatorUtil.isUrl("https://www.baidu.com"));
		Assert.assertTrue(ValidatorUtil.isUrl("http://localhost:8080/api"));
		Assert.assertFalse(ValidatorUtil.isUrl("www.baidu.com"));
	}

	@Test
	public void testChineseAndPlate() {
		Assert.assertTrue(ValidatorUtil.isChinese("中文测试"));
		Assert.assertFalse(ValidatorUtil.isChinese("中文123"));
		Assert.assertTrue(ValidatorUtil.isPlateNumber("京A12345"));
		Assert.assertTrue(ValidatorUtil.isPlateNumber("粤BD12345"));
		Assert.assertFalse(ValidatorUtil.isPlateNumber("京A1234"));
	}

	@Test
	public void testOthers() {
		Assert.assertTrue(ValidatorUtil.isPostalCode("100000"));
		Assert.assertFalse(ValidatorUtil.isPostalCode("12345"));
		Assert.assertTrue(ValidatorUtil.isMoney("12.34"));
		Assert.assertTrue(ValidatorUtil.isMoney("0"));
		Assert.assertFalse(ValidatorUtil.isMoney("12.345"));
		Assert.assertTrue(ValidatorUtil.isGeneral("abc_123"));
		Assert.assertFalse(ValidatorUtil.isGeneral("abc-123"));
		Assert.assertTrue(ValidatorUtil.isUuid("123e4567-e89b-42d3-a456-426614174000"));
		Assert.assertTrue(PatternPool.MOBILE.matcher("13800138000").matches());
		Assert.assertTrue(ValidatorUtil.isValidPort(80));
		Assert.assertFalse(ValidatorUtil.isValidPort(0));
	}

	@Test
	public void testIdCardValid() {
		String id17 = "11010119900307123";
		char code = IdcardUtil.calculateCheckCode(id17);
		String id18 = id17 + code;
		Assert.assertTrue(IdcardUtil.isValidCard(id18));
		Assert.assertTrue(IdcardUtil.isValidCard18(id18));
		Assert.assertTrue(ValidatorUtil.isIdCard(id18));
		Assert.assertFalse(IdcardUtil.isValidCard(id17 + '0'));
		Assert.assertFalse(IdcardUtil.isValidCard("123"));
		Assert.assertFalse(IdcardUtil.isValidCard(null));
	}

	@Test
	public void testIdCard15AndConvert() {
		String id15 = "110101900307123";
		String id18 = IdcardUtil.convert15To18(id15);
		Assert.assertEquals(18, id18.length());
		Assert.assertTrue(IdcardUtil.isValidCard18(id18));
		Assert.assertTrue(IdcardUtil.isValidCard(id15));
	}

	@Test
	public void testIdCardInfo() {
		String male = "11010119900307123" + IdcardUtil.calculateCheckCode("11010119900307123");
		Assert.assertEquals("1990-03-07", IdcardUtil.getBirthDate(male));
		Assert.assertEquals("男", IdcardUtil.getGender(male));
		Assert.assertEquals("11", IdcardUtil.getProvinceCode(male));

		String female = "11010119900307242" + IdcardUtil.calculateCheckCode("11010119900307242");
		Assert.assertEquals("女", IdcardUtil.getGender(female));

		Assert.assertNull(IdcardUtil.getBirthDate("123"));
		Assert.assertNull(IdcardUtil.getGender(null));
	}
}
