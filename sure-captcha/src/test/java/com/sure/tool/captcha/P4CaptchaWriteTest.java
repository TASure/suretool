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
package com.sure.tool.captcha;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * P4（v0.2.0）第十五批：验证码写入文件测试。
 */
public class P4CaptchaWriteTest {

	@Test
	public void testWriteFile() throws Exception {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 10);
		File tmp = File.createTempFile("captcha-", ".png");
		try {
			captcha.write(tmp);
			Assert.assertTrue(tmp.exists());
			Assert.assertTrue(tmp.length() > 0);
			byte[] bytes = Files.readAllBytes(tmp.toPath());
			// PNG 魔数
			Assert.assertEquals((byte) 0x89, bytes[0]);
			Assert.assertEquals((byte) 0x50, bytes[1]);
		} finally {
			tmp.delete();
		}
	}
}
