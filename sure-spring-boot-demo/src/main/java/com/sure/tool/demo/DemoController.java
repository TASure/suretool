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

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sure.tool.boot.CaptchaGenerator;
import com.sure.tool.cache.Cache;
import com.sure.tool.event.EventBus;

/**
 * 示例接口：验证码图片、事件发布、装配状态。
 */
@RestController
public class DemoController {

	private final CaptchaGenerator captcha;
	private final EventBus eventBus;
	private final Cache<?, ?> cache;
	private final AtomicInteger received = new AtomicInteger();

	/**
	 * 构造器：注入 starter 与可选域 Bean。
	 *
	 * @param captcha  验证码生成器（starter 必选域）
	 * @param eventBus 事件总线（引入 sure-event 后装配）
	 * @param cache    缓存（引入 sure-cache 且开启后装配）
	 */
	public DemoController(CaptchaGenerator captcha, EventBus eventBus, Cache<?, ?> cache) {
		this.captcha = captcha;
		this.eventBus = eventBus;
		this.cache = cache;
		this.eventBus.subscribe(DemoEvent.class, event -> received.incrementAndGet());
	}

	/**
	 * 装配状态。
	 *
	 * @return 文本说明
	 */
	@GetMapping("/")
	public String home() {
		return "captcha=LineCaptcha " + captcha.getWidth() + "x" + captcha.getHeight()
				+ ", eventBus=" + eventBus.listenerCount() + " listeners"
				+ ", cache=" + cache.getClass().getSimpleName()
				+ ", receivedEvents=" + received.get();
	}

	/**
	 * 生成验证码图片（验证码答案打印在应用控制台）。
	 *
	 * @return PNG 字节
	 */
	@GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] captcha() {
		var image = captcha.generate();
		System.out.println("[captcha] code = " + image.getCode());
		return image.getImageBytes();
	}

	/**
	 * 发布一个事件，返回订阅者累计收到的事件数。
	 *
	 * @return 已接收事件数
	 */
	@GetMapping("/event")
	public String event() {
		eventBus.post(new DemoEvent("ping from /event"));
		return "receivedEvents=" + received.get();
	}

	/**
	 * 示例事件。
	 *
	 * @param message 事件内容
	 */
	public record DemoEvent(String message) {
	}
}
