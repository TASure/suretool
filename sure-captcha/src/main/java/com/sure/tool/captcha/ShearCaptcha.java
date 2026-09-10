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

import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 扭曲（错切）验证码，参考 Hutool 的 {@code ShearCaptcha} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ShearCaptcha extends AbstractCaptcha {

	/**
	 * 创建扭曲验证码。
	 *
	 * @param width          图片宽度
	 * @param height         图片高度
	 * @param codeCount      字符个数
	 * @param interfereCount 干扰线数量
	 */
	public ShearCaptcha(int width, int height, int codeCount, int interfereCount) {
		super(width, height, codeCount, interfereCount);
	}

	@Override
	protected void applyShear(Graphics2D g) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		double shearX = random.nextDouble(-0.3, 0.3);
		double shearY = random.nextDouble(-0.15, 0.15);
		g.shear(shearX, shearY);
	}

	@Override
	protected void drawInterfere(Graphics2D g) {
		drawRandomLines(g);
	}
}
