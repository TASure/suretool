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

/**
 * 验证码工厂，参考 Hutool 的 {@code CaptchaUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CaptchaUtil {

	private CaptchaUtil() {
	}

	/**
	 * 创建线段干扰验证码。
	 *
	 * @param width          图片宽度
	 * @param height         图片高度
	 * @param codeCount      字符个数
	 * @param interfereCount 干扰线数量
	 * @return 验证码
	 */
	public static LineCaptcha createLineCaptcha(int width, int height, int codeCount, int interfereCount) {
		return new LineCaptcha(width, height, codeCount, interfereCount);
	}

	/**
	 * 创建圆圈干扰验证码。
	 *
	 * @param width          图片宽度
	 * @param height         图片高度
	 * @param codeCount      字符个数
	 * @param interfereCount 干扰圆数量
	 * @return 验证码
	 */
	public static CircleCaptcha createCircleCaptcha(int width, int height, int codeCount, int interfereCount) {
		return new CircleCaptcha(width, height, codeCount, interfereCount);
	}

	/**
	 * 创建扭曲验证码。
	 *
	 * @param width          图片宽度
	 * @param height         图片高度
	 * @param codeCount      字符个数
	 * @param interfereCount 干扰线数量
	 * @return 验证码
	 */
	public static ShearCaptcha createShearCaptcha(int width, int height, int codeCount, int interfereCount) {
		return new ShearCaptcha(width, height, codeCount, interfereCount);
	}

	/**
	 * 创建线段干扰验证码（默认 4 字符、150 条干扰线）。
	 *
	 * @param width  图片宽度
	 * @param height 图片高度
	 * @return 验证码
	 */
	public static LineCaptcha createLineCaptcha(int width, int height) {
		return createLineCaptcha(width, height, 4, 150);
	}
}
