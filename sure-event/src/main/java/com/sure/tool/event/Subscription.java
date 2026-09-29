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
package com.sure.tool.event;

import java.util.function.Consumer;

/**
 * 事件订阅句柄。
 *
 * <p>持有注册的监听器与事件类型，{@link #close()} 后取消注册（幂等）。</p>
 *
 * @since 1.4.0
 */
public final class Subscription implements AutoCloseable {

	private final EventBus bus;
	private final Class<?> eventType;
	private final Consumer<Object> listener;
	private final Consumer<?> rawListener;
	private final Object owner;
	private volatile boolean active = true;

	Subscription(EventBus bus, Class<?> eventType, Consumer<Object> listener, Consumer<?> rawListener, Object owner) {
		this.bus = bus;
		this.eventType = eventType;
		this.listener = listener;
		this.rawListener = rawListener;
		this.owner = owner;
	}

	/**
	 * 是否仍处于订阅状态。
	 *
	 * @return true 表示有效
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * 取消订阅（幂等）。
	 */
	@Override
	public void close() {
		if (active) {
			active = false;
			bus.remove(this);
		}
	}

	Class<?> eventType() {
		return eventType;
	}

	Consumer<Object> listener() {
		return listener;
	}

	Object owner() {
		return owner;
	}

	Consumer<?> rawListener() {
		return rawListener;
	}
}
