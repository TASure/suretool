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
package com.sure.tool.log;

import java.io.PrintStream;

/**
 * 兜底日志实现：直接输出到控制台（warn/error 走标准错误流，其余走标准输出流）。
 *
 * @author suretool
 * @since 1.2.0
 */
public final class ConsoleLog extends AbstractLog {

	/**
	 * 构造器。
	 *
	 * @param name 日志名称
	 */
	public ConsoleLog(String name) {
		super(name);
	}

	@Override
	protected void handle(String level, String message, Throwable throwable) {
		PrintStream out = switch (level) {
			case "warn", "error" -> System.err;
			default -> System.out;
		};
		out.println("[" + level.toUpperCase() + "] " + getName() + " - " + message);
		if (throwable != null) {
			throwable.printStackTrace(out);
		}
	}
}
