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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.sure.tool.captcha.Captcha;
import com.sure.tool.captcha.CaptchaUtil;
import com.sure.tool.captcha.CircleCaptcha;
import com.sure.tool.captcha.LineCaptcha;
import com.sure.tool.captcha.ShearCaptcha;

/**
 * Captcha 测试：字符生成、校验、图片字节与三种干扰类型。
 */
public class CaptchaTest {

	@Test
	public void testLineCaptchaCodeAndVerify() {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 10);
		assertEquals(4, captcha.getCode().length());
		assertTrue(captcha.verify(captcha.getCode()));
		assertTrue(captcha.verify(captcha.getCode().toLowerCase()));
		assertTrue(captcha.verify(" " + captcha.getCode() + " "));
		assertFalse(captcha.verify("wrong"));
		assertFalse(captcha.verify(null));
		assertFalse(captcha.verify(""));
	}

	@Test
	public void testImageBytesAndBase64() {
		Captcha captcha = CaptchaUtil.createLineCaptcha(200, 80);
		byte[] bytes = captcha.getImageBytes();
		assertTrue(bytes.length > 0);
		// PNG 魔数
		assertEquals((byte) 0x89, bytes[0]);
		assertEquals((byte) 'P', bytes[1]);
		assertEquals((byte) 'N', bytes[2]);
		assertEquals((byte) 'G', bytes[3]);
		assertTrue(captcha.getImageBase64().startsWith("data:image/png;base64,"));
	}

	@Test
	public void testWriteOutputStream() throws Exception {
		Captcha captcha = CaptchaUtil.createCircleCaptcha(200, 80, 4, 20);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		captcha.write(out);
		assertEquals(captcha.getImageBytes().length, out.size());
	}

	@Test
	public void testAllTypesGenerate() {
		LineCaptcha line = new LineCaptcha(160, 60, 5, 15);
		CircleCaptcha circle = new CircleCaptcha(160, 60, 5, 15);
		ShearCaptcha shear = new ShearCaptcha(160, 60, 5, 15);
		assertEquals(5, line.getCode().length());
		assertEquals(5, circle.getCode().length());
		assertEquals(5, shear.getCode().length());
		assertTrue(line.getImageBytes().length > 0);
		assertTrue(circle.getImageBytes().length > 0);
		assertTrue(shear.getImageBytes().length > 0);
		assertTrue(shear.verify(shear.getCode()));
	}

	@Test
	public void testFactoryDefaults() {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(300, 100);
		assertEquals(4, captcha.getCode().length());
	}
}
