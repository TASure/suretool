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

package com.sure.tool.io;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * 目录监听选项（builder 风格）。
 *
 * <p>默认值：递归监听、不过滤、事件类型 CREATE + MODIFY + DELETE。
 */
public class WatchOptions {

	private boolean recursive = true;
	private PathFilter filter;
	private EventType[] eventTypes = { EventType.CREATE, EventType.MODIFY, EventType.DELETE };

	/**
	 * 是否递归监听子目录（默认 {@code true}；新目录创建后自动注册）。
	 *
	 * @param recursive 是否递归
	 * @return this
	 */
	public WatchOptions recursive(boolean recursive) {
		this.recursive = recursive;
		return this;
	}

	/**
	 * 事件路径过滤器（默认不过滤）。
	 *
	 * @param filter 过滤器
	 * @return this
	 */
	public WatchOptions filter(PathFilter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * 监听的事件类型（默认 CREATE + MODIFY + DELETE）。
	 *
	 * @param eventTypes 事件类型
	 * @return this
	 */
	public WatchOptions eventTypes(EventType... eventTypes) {
		this.eventTypes = eventTypes;
		return this;
	}

	/**
	 * 是否递归监听。
	 *
	 * @return 是否递归
	 */
	public boolean recursive() {
		return recursive;
	}

	/**
	 * 事件路径过滤器。
	 *
	 * @return 过滤器；可为 {@code null}
	 */
	public PathFilter filter() {
		return filter;
	}

	/**
	 * 监听的事件类型（返回副本，外部修改不影响本选项）。
	 *
	 * @return 事件类型
	 */
	public EventType[] eventTypes() {
		return eventTypes == null ? null : eventTypes.clone();
	}

	/**
	 * 是否接受该事件类型。
	 *
	 * @param type 事件类型
	 * @return 是否监听
	 */
	boolean acceptEvent(EventType type) {
		if (eventTypes == null) {
			return true;
		}
		for (EventType t : eventTypes) {
			if (t == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 需要注册的 WatchEvent.Kind（OVERFLOW 无需注册，由服务自动产生）。
	 *
	 * @return kinds
	 */
	WatchEvent.Kind<?>[] kinds() {
		java.util.List<WatchEvent.Kind<?>> list = new java.util.ArrayList<>();
		if (acceptEvent(EventType.CREATE)) {
			list.add(StandardWatchEventKinds.ENTRY_CREATE);
		}
		if (acceptEvent(EventType.MODIFY)) {
			list.add(StandardWatchEventKinds.ENTRY_MODIFY);
		}
		if (acceptEvent(EventType.DELETE)) {
			list.add(StandardWatchEventKinds.ENTRY_DELETE);
		}
		if (list.isEmpty()) {
			// 无有效类型时仍注册 CREATE，保证 take() 有事件可等
			list.add(StandardWatchEventKinds.ENTRY_CREATE);
		}
		return list.toArray(new WatchEvent.Kind<?>[0]);
	}
}
