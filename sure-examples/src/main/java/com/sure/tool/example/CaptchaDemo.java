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
package com.sure.tool.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sure.tool.captcha.CaptchaUtil;
import com.sure.tool.captcha.LineCaptcha;

/**
 * 验证码示例（CaptchaUtil）。
 */
public class CaptchaDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws IOException {
		System.out.println("=== CaptchaDemo ===");
		LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 20);
		System.out.println("code = " + captcha.getCode());
		System.out.println("verify correct = " + captcha.verify(captcha.getCode()));
		System.out.println("verify wrong = " + captcha.verify("xxxx"));
		System.out.println("image bytes = " + captcha.getImageBytes().length);
		File out = new File("target/captcha.png");
		try (OutputStream os = new FileOutputStream(out)) {
			captcha.write(os);
		}
		System.out.println("written to " + out.getAbsolutePath());
	}
}
