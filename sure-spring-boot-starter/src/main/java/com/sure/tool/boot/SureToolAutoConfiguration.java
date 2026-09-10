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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * suretool Spring Boot 自动装配入口。
 *
 * <p>通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 被 Spring Boot 自动发现。当前提供验证码工具 Bean 的装配骨架，
 * 新能力可在此按相同模式扩展。
 */
@AutoConfiguration
@EnableConfigurationProperties(SureToolProperties.class)
public class SureToolAutoConfiguration {

	/**
	 * 装配验证码生成器（默认开启，可通过 {@code suretool.captcha.enabled=false} 关闭）。
	 *
	 * @param properties 配置
	 * @return 验证码生成器
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "suretool.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
	public CaptchaGenerator captchaGenerator(SureToolProperties properties) {
		return new CaptchaGenerator(properties.getCaptcha().getWidth(), properties.getCaptcha().getHeight());
	}
}
