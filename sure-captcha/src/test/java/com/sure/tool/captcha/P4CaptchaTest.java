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

/**
 * P4（v0.2.0）第十三批：验证码 Data URI 测试。
 */
public class P4CaptchaTest {

	@Test
	public void testCodeBase64() {
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 10);
		String base64 = captcha.getCodeBase64();
		Assert.assertNotNull(base64);
		Assert.assertTrue(base64.startsWith("data:image/png;base64,"));
		Assert.assertTrue(base64.length() > "data:image/png;base64,".length());
	}
}
