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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.SmartLifecycle;

import com.sure.tool.cache.Cache;
import com.sure.tool.cache.CacheUtil;
import com.sure.tool.cron.CronUtil;
import com.sure.tool.event.EventBus;

/**
 * suretool Spring Boot 自动装配入口。
 *
 * <p>通过 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * 被 Spring Boot 自动发现。验证码域随 starter 的必选依赖（sure-core + sure-captcha）默认装配；
 * 事件总线 / 定时调度 / 缓存三域为可选依赖，仅当使用方显式引入对应模块时按
 * {@link ConditionalOnClass} 生效，避免 starter 被动拉入全量模块。
 * 每域独立开关（{@code suretool.*.enabled}）、默认安全（cron/cache 默认关闭）。
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

	/**
	 * 事件总线装配（可选域）：仅当使用方引入 sure-event 时生效。
	 *
	 * <p>独立配置类 + 类级 {@link ConditionalOnClass}，缺少模块时整类跳过，
	 * 不会触发 NoClassDefFoundError。
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(EventBus.class)
	static class EventConfiguration {

		/**
		 * 装配全局事件总线（默认开启，可通过 {@code suretool.event.enabled=false} 关闭）。
		 *
		 * <p>返回容器管理的事件总线实例（非 {@link EventBus#getDefault()} 静态单例），
		 * 上下文销毁时自动清理订阅者，多个 Spring 上下文互不干扰。
		 *
		 * @return 事件总线
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "suretool.event", name = "enabled", havingValue = "true", matchIfMissing = true)
		public EventBus sureEventBus() {
			return new EventBus();
		}
	}

	/**
	 * 定时调度装配（可选域）：仅当使用方引入 sure-cron 时生效。
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(CronUtil.class)
	static class CronConfiguration {

		/**
		 * 装配 CronUtil 调度生命周期（默认关闭，可通过 {@code suretool.cron.enabled=true} 开启）。
		 *
		 * @return 调度生命周期控制器
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "suretool.cron", name = "enabled", havingValue = "true")
		public CronLifecycle sureCronLifecycle() {
			return new CronLifecycle();
		}
	}

	/**
	 * 缓存装配（可选域）：仅当使用方引入 sure-cache 时生效。
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({Cache.class, CacheUtil.class})
	static class CacheConfiguration {

		/**
		 * 装配默认缓存 Bean（默认关闭，可通过 {@code suretool.cache.enabled=true} 开启）。
		 *
		 * <p>策略由 {@code suretool.cache.type} 指定：lru（默认）/ fifo / timed / weak。
		 *
		 * @param properties 配置
		 * @return 缓存实例
		 */
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "suretool.cache", name = "enabled", havingValue = "true")
		public Cache<?, ?> sureCache(SureToolProperties properties) {
			String type = properties.getCache().getType();
			int capacity = properties.getCache().getCapacity();
			long timeout = properties.getCache().getTimeout();
			switch (type) {
				case "fifo":
					return CacheUtil.<Object, Object>newFifoCache(capacity);
				case "timed":
					return CacheUtil.<Object, Object>newTimedCache(timeout);
				case "weak":
					return CacheUtil.<Object, Object>newWeakCache(capacity, timeout);
				case "lru":
				default:
					return CacheUtil.<Object, Object>newLruCache(capacity);
			}
		}
	}

	/**
	 * CronUtil 调度生命周期控制器：随 Spring 容器启动/停止调度器。
	 */
	public static class CronLifecycle implements SmartLifecycle {

		private volatile boolean running;

		@Override
		public void start() {
			CronUtil.start();
			this.running = true;
		}

		@Override
		public void stop() {
			CronUtil.stop();
			this.running = false;
		}

		@Override
		public boolean isRunning() {
			return this.running;
		}

		/** 最后启动、最先停止，保证任务注册完成后再扫描 */
		@Override
		public int getPhase() {
			return Integer.MAX_VALUE;
		}
	}
}
