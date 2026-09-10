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
package com.sure.tool.lang;

/**
 * 秒表计时器，参考 Hutool 的 {@code StopWatch} 设计。
 * 基于 {@link System#nanoTime()} 累计耗时，支持分段累计与多次启停。
 *
 * @author suretool
 * @since 0.1.0
 */
public class StopWatch {

	private final String name;
	private long startNanos;
	private long totalNanos;
	private boolean running;

	public StopWatch() {
		this("StopWatch");
	}

	/**
	 * 创建秒表。
	 *
	 * @param name 名称（用于打印展示）
	 */
	public StopWatch(String name) {
		this.name = (name == null) ? "StopWatch" : name;
	}

	/**
	 * 开始计时（已在计时中则忽略）。
	 */
	public void start() {
		if (running) {
			return;
		}
		running = true;
		startNanos = System.nanoTime();
	}

	/**
	 * 停止计时并累计耗时（未在计时中则忽略）。
	 */
	public void stop() {
		if (!running) {
			return;
		}
		totalNanos += System.nanoTime() - startNanos;
		running = false;
	}

	/**
	 * 重置计时器。
	 */
	public void reset() {
		totalNanos = 0;
		running = false;
	}

	/**
	 * 是否正在计时。
	 *
	 * @return 是否正在计时
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * 累计耗时（纳秒）。
	 *
	 * @return 累计耗时
	 */
	public long getTotalTimeNanos() {
		return totalNanos;
	}

	/**
	 * 累计耗时（毫秒）。
	 *
	 * @return 累计耗时
	 */
	public long getTotalTimeMillis() {
		return totalNanos / 1_000_000L;
	}

	/**
	 * 累计耗时（秒）。
	 *
	 * @return 累计耗时
	 */
	public long getTotalTimeSeconds() {
		return totalNanos / 1_000_000_000L;
	}

	/**
	 * 当前累计耗时（毫秒），与 {@link #getTotalTimeMillis()} 一致。
	 *
	 * @return 累计耗时
	 */
	public long getTime() {
		return getTotalTimeMillis();
	}

	/**
	 * 格式化累计耗时（秒，保留三位小数）。
	 *
	 * @return 如 {@code "test: 1.234s"}
	 */
	public String prettyPrint() {
		return name + ": " + String.format("%.3fs", totalNanos / 1_000_000_000.0);
	}

	@Override
	public String toString() {
		return prettyPrint();
	}
}