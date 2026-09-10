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
package com.sure.tool.boot;

import com.sure.tool.captcha.CaptchaUtil;
import com.sure.tool.captcha.LineCaptcha;

/**
 * 验证码生成器（由 Spring 容器装配的示例服务 Bean）。
 *
 * <p>应用侧注入后即可调用，例如：{@code captchaGenerator.generate()}。
 */
public class CaptchaGenerator {

	private final int width;
	private final int height;

	/**
	 * 构造器。
	 *
	 * @param width  宽度
	 * @param height 高度
	 */
	public CaptchaGenerator(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * 生成验证码。
	 *
	 * @return 验证码对象（含图形字节与验证方法）
	 */
	public LineCaptcha generate() {
		return CaptchaUtil.createLineCaptcha(width, height);
	}
}
