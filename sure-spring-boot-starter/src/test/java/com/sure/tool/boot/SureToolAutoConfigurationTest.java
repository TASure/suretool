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

import com.sure.tool.cache.Cache;
import com.sure.tool.event.EventBus;

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

	@Test
	public void eventBusRegisteredByDefault() {
		runner.run(context -> {
			assertThat(context).hasNotFailed();
			assertThat(context).hasSingleBean(EventBus.class);
			// 容器管理实例：可发布与订阅
			EventBus bus = context.getBean(EventBus.class);
			bus.post("ping");
		});
	}

	@Test
	public void eventBusDisabledWhenPropertyFalse() {
		runner.withPropertyValues("suretool.event.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean(EventBus.class);
		});
	}

	@Test
	public void userDefinedEventBusTakesPrecedence() {
		runner.withBean(EventBus.class, () -> new EventBus()).run(context -> {
			assertThat(context).hasSingleBean(EventBus.class);
			// @ConditionalOnMissingBean：用户 Bean 优先，自动装配不重复注册
		});
	}

	@Test
	public void cronLifecycleNotRegisteredByDefault() {
		runner.run(context -> {
			assertThat(context).doesNotHaveBean(SureToolAutoConfiguration.CronLifecycle.class);
		});
	}

	@Test
	public void cronLifecycleRegisteredAndLifecycleIdempotent() {
		runner.withPropertyValues("suretool.cron.enabled=true").run(context -> {
			assertThat(context).hasSingleBean(SureToolAutoConfiguration.CronLifecycle.class);
			SureToolAutoConfiguration.CronLifecycle lifecycle =
					context.getBean(SureToolAutoConfiguration.CronLifecycle.class);
			lifecycle.start();
			assertThat(lifecycle.isRunning()).isTrue();
			lifecycle.start();   // 幂等：重复 start 不抛异常
			lifecycle.stop();
			lifecycle.stop();    // 幂等：重复 stop 不抛异常
			assertThat(lifecycle.isRunning()).isFalse();
		});
	}

	@Test
	public void cacheNotRegisteredByDefault() {
		runner.run(context -> {
			assertThat(context).doesNotHaveBean(Cache.class);
		});
	}

	@Test
	public void lruCacheRegisteredWithCapacity() {
		runner.withPropertyValues("suretool.cache.enabled=true", "suretool.cache.type=lru",
				"suretool.cache.capacity=2048").run(context -> {
			assertThat(context).hasSingleBean(Cache.class);
		});
	}

	@Test
	public void timedCacheRegisteredWithTimeout() {
		runner.withPropertyValues("suretool.cache.enabled=true", "suretool.cache.type=timed",
				"suretool.cache.timeout=30000").run(context -> {
			assertThat(context).hasSingleBean(Cache.class);
		});
	}

	@Test
	public void weakCacheRegistered() {
		runner.withPropertyValues("suretool.cache.enabled=true", "suretool.cache.type=weak",
				"suretool.cache.capacity=256", "suretool.cache.timeout=5000").run(context -> {
			assertThat(context).hasSingleBean(Cache.class);
		});
	}

	@Test
	public void userDefinedCacheTakesPrecedence() {
		runner.withPropertyValues("suretool.cache.enabled=true")
				.withBean(Cache.class, () -> com.sure.tool.cache.CacheUtil.<Object, Object>newLruCache(64))
				.run(context -> {
					assertThat(context).hasSingleBean(Cache.class);
				});
	}
}
