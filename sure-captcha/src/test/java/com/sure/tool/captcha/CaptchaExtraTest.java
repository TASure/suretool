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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 验证码工厂与受保护访问器补测：扭曲验证码工厂、宽高与干扰数量读取。
 */
public class CaptchaExtraTest {

	@Test
	public void 扭曲验证码工厂与尺寸访问器() {
		final ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 80, 4, 7);
		assertEquals(200, captcha.getWidth());
		assertEquals(80, captcha.getHeight());
		assertEquals(7, captcha.getInterfereCount());
		assertTrue(captcha.getImageBytes().length > 0);
	}
}
