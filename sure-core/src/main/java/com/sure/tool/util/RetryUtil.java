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

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * 重试工具类：按固定间隔重试指定次数，支持结果条件与异常判定，零依赖。
 * <p>
 * 典型场景：网络请求、远程调用等瞬态失败后的指数或固定间隔重试。
 *
 * @author suretool
 * @since 0.2.0
 */
public class RetryUtil {

	private RetryUtil() {
	}

	/**
	 * 固定间隔重试，直到成功或达到最大次数。
	 *
	 * @param callable  业务操作（返回 Boolean 或 true 表示成功）
	 * @param maxAttempts 最大尝试次数（含首次，&gt;0）
	 * @param interval  每次重试间隔
	 * @return 是否在重试次数内成功
	 * @throws InterruptedException 线程中断
	 */
	public static boolean retry(Callable<Boolean> callable, int maxAttempts, Duration interval) throws InterruptedException {
		return retry(callable, maxAttempts, interval, true);
	}

	/**
	 * 固定间隔重试，直到成功或达到最大次数。
	 *
	 * @param callable     业务操作
	 * @param maxAttempts  最大尝试次数（含首次，&gt;0）
	 * @param interval     每次重试间隔
	 * @param retryOnError 是否在异常时继续重试（false 则异常立即抛出）
	 * @return 是否在重试次数内成功
	 * @throws InterruptedException 线程中断
	 */
	public static boolean retry(Callable<Boolean> callable, int maxAttempts, Duration interval, boolean retryOnError)
			throws InterruptedException {
		if (callable == null || maxAttempts < 1 || interval == null || interval.isNegative()) {
			throw new IllegalArgumentException("参数不合法");
		}
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				if (Boolean.TRUE.equals(callable.call())) {
					return true;
				}
			} catch (Exception e) {
				if (!retryOnError) {
					throw new IllegalStateException("重试过程中业务异常（第 " + attempt + " 次尝试）", e);
				}
			}
			if (attempt < maxAttempts) {
				Thread.sleep(interval.toMillis());
			}
		}
		return false;
	}

	/**
	 * 重试直到结果满足条件。
	 *
	 * @param callable    业务操作
	 * @param condition   成功条件（对结果断言）
	 * @param maxAttempts 最大尝试次数（含首次）
	 * @param interval    重试间隔
	 * @param <T>         结果类型
	 * @return 满足条件的结果；全部失败返回最后一次结果
	 * @throws InterruptedException 线程中断
	 */
	public static <T> T retryUntil(Callable<T> callable, Predicate<T> condition, int maxAttempts, Duration interval)
			throws InterruptedException {
		if (callable == null || condition == null || maxAttempts < 1 || interval == null || interval.isNegative()) {
			throw new IllegalArgumentException("参数不合法");
		}
		T last = null;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				last = callable.call();
				if (condition.test(last)) {
					return last;
				}
			} catch (Exception e) {
				if (attempt == maxAttempts) {
					throw new IllegalStateException("重试达到最大次数仍异常", e);
				}
			}
			if (attempt < maxAttempts) {
				Thread.sleep(interval.toMillis());
			}
		}
		return last;
	}
}
