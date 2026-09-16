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
package com.sure.tool.util;

/**
 * 计时器工具：记录时间点、统计间隔时长，线程不安全（单线程场景），零依赖。
 *
 * <p>用于方法耗时统计、性能打点：</p>
 * <pre>{@code
 * TimeInterval timer = TimeInterval.start();
 * // ...业务...
 * long ms = timer.intervalMs();      // 距 start 毫秒数
 * timer.restart();                    // 重新计时
 * }</pre>
 *
 * @author suretool
 * @since 0.2.0
 */
public class TimeInterval {

	private long startNanos;

	/**
	 * 构造并开始计时。
	 */
	public TimeInterval() {
		restart();
	}

	/**
	 * 构造并开始计时。
	 *
	 * @return 计时器
	 */
	public static TimeInterval start() {
		return new TimeInterval();
	}

	/**
	 * 重新开始计时（重置起点为当前时刻）。
	 *
	 * @return this
	 */
	public TimeInterval restart() {
		this.startNanos = System.nanoTime();
		return this;
	}

	/**
	 * 距起点毫秒数（不重置）。
	 *
	 * @return 毫秒
	 */
	public long intervalMs() {
		return (System.nanoTime() - startNanos) / 1_000_000;
	}

	/**
	 * 距起点秒数（不重置，保留毫秒精度）。
	 *
	 * @return 秒
	 */
	public double intervalSecond() {
		return intervalMs() / 1000.0;
	}

	/**
	 * 距起点纳秒数（不重置）。
	 *
	 * @return 纳秒
	 */
	public long intervalNs() {
		return System.nanoTime() - startNanos;
	}

	/**
	 * 可读耗时文本（如 12.3 秒 / 345 毫秒）。
	 *
	 * @return 耗时文本
	 */
	public String pretty() {
		long ms = intervalMs();
		if (ms >= 1000) {
			return String.format("%.1f 秒", ms / 1000.0);
		}
		return ms + " 毫秒";
	}

	@Override
	public String toString() {
		return "TimeInterval{elapsedMs=" + intervalMs() + '}';
	}
}
