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

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * starter 冒烟测试：上下文加载、属性绑定、条件装配。
 *
 * @author suretool
 * @since 1.1.0
 */
public class SureToolAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SureToolAutoConfiguration.class));

	@Test
	public void contextLoadsAndCaptchaGeneratorRegisteredByDefault() {
		runner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(CaptchaGenerator.class);
		});
	}

	@Test
	public void captchaPropertiesAreBoundToGenerator() {
		runner.withPropertyValues("suretool.captcha.width=320", "suretool.captcha.height=120").run(context -> {
			assertThat(context).hasSingleBean(CaptchaGenerator.class);
			CaptchaGenerator generator = context.getBean(CaptchaGenerator.class);
			assertThat(generator.getWidth()).isEqualTo(320);
			assertThat(generator.getHeight()).isEqualTo(120);
		});
	}

	@Test
	public void captchaDisabledWhenPropertyFalse() {
		runner.withPropertyValues("suretool.captcha.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean(CaptchaGenerator.class);
		});
	}
}
