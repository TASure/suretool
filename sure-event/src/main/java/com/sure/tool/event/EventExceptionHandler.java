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

/**
 * 事件监听器异常处理器。
 *
 * @since 1.4.0
 */
@FunctionalInterface
public interface EventExceptionHandler {

	/**
	 * 处理监听器抛出的异常（默认打印警告，不影响其他监听器）。
	 *
	 * @param throwable 异常
	 * @param event     正在投递的事件
	 */
	void handle(Throwable throwable, Object event);
}
