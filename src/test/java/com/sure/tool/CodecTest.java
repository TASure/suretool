package com.sure.tool;

import com.sure.tool.codec.Base64Util;
import com.sure.tool.codec.HashUtil;
import com.sure.tool.codec.HexUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Base64Util / HexUtil / HashUtil 单元测试。
 */
public class CodecTest {

	// ---------------- Base64 ----------------

	@Test
	public void testBase64RoundTrip() {
		String text = "Hello, 你好！";
		String encoded = Base64Util.encode(text);
		Assert.assertEquals(text, Base64Util.decodeStr(encoded));
		Assert.assertTrue(Base64Util.isBase64(encoded));
		Assert.assertFalse(Base64Util.isBase64("not base64!!!"));
		Assert.assertFalse(Base64Util.isBase64(""));
	}

	@Test
	public void testBase64UrlSafe() {
		byte[] data = new byte[]{0, 1, 2, -1, -2};
		String encoded = Base64Util.encodeUrlSafe(data);
		Assert.assertFalse(encoded.contains("+"));
		Assert.assertFalse(encoded.contains("/"));
		Assert.assertFalse(encoded.contains("="));
		Assert.assertArrayEquals(data, Base64Util.decode(encoded));
	}

	// ---------------- Hex ----------------

	@Test
	public void testHexRoundTrip() {
		byte[] data = "suretool".getBytes(StandardCharsets.UTF_8);
		String hex = HexUtil.encodeHexStr(data);
		Assert.assertEquals(16, hex.length());
		Assert.assertArrayEquals(data, HexUtil.decodeHex(hex));
		Assert.assertEquals("73757265746f6f6c", hex);
		Assert.assertEquals("73757265746F6F6C", HexUtil.encodeHexStr(data, false));
	}

	@Test
	public void testHexNumber() {
		Assert.assertTrue(HexUtil.isHexNumber("0x1F"));
		Assert.assertTrue(HexUtil.isHexNumber("1a2b"));
		Assert.assertFalse(HexUtil.isHexNumber("12g3"));
		Assert.assertFalse(HexUtil.isHexNumber(""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHexOddLength() {
		HexUtil.decodeHex("abc");
	}

	// ---------------- Hash ----------------

	@Test
	public void testMd5() {
		// 已知 MD5 值
		Assert.assertEquals("d41d8cd98f00b204e9800998ecf8427e", HashUtil.md5Hex(""));
		Assert.assertEquals("900150983cd24fb0d6963f7d28e17f72", HashUtil.md5Hex("abc"));
	}

	@Test
	public void testSha() {
		// 已知 SHA-1 / SHA-256 值
		Assert.assertEquals("a9993e364706816aba3e25717850c26c9cd0d89d", HashUtil.sha1Hex("abc"));
		Assert.assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
				HashUtil.sha256Hex("abc"));
		Assert.assertEquals(128, HashUtil.sha512Hex("abc").length());
	}

	@Test
	public void testCrc32() {
		Assert.assertEquals(891568578L, HashUtil.crc32("abc"));
		Assert.assertEquals("352441c2", HashUtil.crc32Hex("abc"));
	}
}
