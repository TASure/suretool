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

	/** 事件总线配置 */
	private Event event = new Event();

	/** 定时调度配置 */
	private Cron cron = new Cron();

	/** 缓存配置 */
	private Cache cache = new Cache();

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

	/**
	 * 事件总线子配置（{@code suretool.event.*}）。
	 */
	public static class Event {

		/** 是否装配全局 EventBus 单例（默认 true） */
		private boolean enabled = true;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	/**
	 * 定时调度子配置（{@code suretool.cron.*}）。默认关闭——不隐式启动后台线程。
	 */
	public static class Cron {

		/** 是否装配 CronUtil 调度生命周期（默认 false） */
		private boolean enabled = false;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public Cron getCron() {
		return cron;
	}

	public void setCron(Cron cron) {
		this.cron = cron;
	}

	/**
	 * 缓存子配置（{@code suretool.cache.*}）。默认关闭——不隐式占用内存。
	 */
	public static class Cache {

		/** 是否装配默认 Cache Bean（默认 false） */
		private boolean enabled = false;

		/** 缓存策略：lru / fifo / timed / weak（默认 lru） */
		private String type = "lru";

		/** 容量（lru/fifo/weak 生效，默认 1024） */
		private int capacity = 1024;

		/** 过期毫秒（timed/weak 生效，默认 60000） */
		private long timeout = 60_000L;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getCapacity() {
			return capacity;
		}

		public void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		public long getTimeout() {
			return timeout;
		}

		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
