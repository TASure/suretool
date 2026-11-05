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
package com.sure.tool.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.sure.tool.boot.CaptchaGenerator;
import com.sure.tool.boot.SureToolAutoConfiguration;
import com.sure.tool.cache.Cache;
import com.sure.tool.event.EventBus;

/**
 * suretool starter 示例入口。
 *
 * <p>启动时打印各域 Bean 的装配结果：验证码（starter 必选域）、事件总线 / 调度 / 缓存（可选域）。
 */
@SpringBootApplication
public class DemoApplication {

	/**
	 * 应用入口。
	 *
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	/**
	 * 启动报告：证明 core + captcha 默认装配，event / cron / cache 因显式引入而生效。
	 *
	 * @param context Spring 容器
	 * @return 启动运行器
	 */
	@Bean
	ApplicationRunner startupReport(ApplicationContext context) {
		return args -> {
			CaptchaGenerator captcha = context.getBean(CaptchaGenerator.class);
			System.out.println("[suretool] captcha = " + captcha.getWidth() + "x" + captcha.getHeight());
			context.getBeanProvider(EventBus.class).ifAvailable(
					bus -> System.out.println("[suretool] event   = EventBus bean (" + bus.listenerCount() + " listeners)"));
			context.getBeanProvider(Cache.class).ifAvailable(
					cache -> System.out.println("[suretool] cache   = " + cache.getClass().getSimpleName()));
			context.getBeanProvider(SureToolAutoConfiguration.CronLifecycle.class).ifAvailable(
					lifecycle -> System.out.println("[suretool] cron    = CronLifecycle running=" + lifecycle.isRunning()));
			System.out.println("[suretool] version = 1.19.1");
		};
	}
}
