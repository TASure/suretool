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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * suretool 自动装配配置项（前缀 {@code suretool}）。
 *
 * <p>示例：<pre>{@code
 * suretool:
 *   captcha:
 *     enabled: true
 *     width: 200
 *     height: 80
 * }</pre>
 */
@ConfigurationProperties(prefix = "suretool")
public class SureToolProperties {

	/** 验证码配置 */
	private Captcha captcha = new Captcha();

	/**
	 * 验证码子配置。
	 */
	public static class Captcha {

		/** 是否启用验证码工具 Bean */
		private boolean enabled = true;

		/** 宽度 */
		private int width = 200;

		/** 高度 */
		private int height = 80;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}
	}

	public Captcha getCaptcha() {
		return captcha;
	}

	public void setCaptcha(Captcha captcha) {
		this.captcha = captcha;
	}
}
