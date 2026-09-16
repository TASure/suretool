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
package com.sure.tool.thread;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 令牌桶限流器：按固定速率补充令牌，突发容量由桶大小决定，线程安全，零依赖。
 * <p>
 * 基于 {@link System#nanoTime()} 的平滑令牌桶实现（参考 Guava {@code RateLimiter} 的简化版）：
 * 每次放行前计算自上次补充以来应累积的令牌数，桶内最多保留 {@code capacity} 个令牌。
 *
 * @author suretool
 * @since 0.2.0
 */
public class RateLimiter {

	private final double tokensPerNano;
	private final double capacity;
	private final AtomicLong nextFreeTicketNanos = new AtomicLong();
	private volatile double storedTokens;

	/**
	 * 构造限流器。
	 *
	 * @param permitsPerSecond 每秒放行数（&gt;0）
	 */
	public RateLimiter(double permitsPerSecond) {
		this(permitsPerSecond, permitsPerSecond);
	}

	/**
	 * 构造限流器。
	 *
	 * @param permitsPerSecond 每秒放行数（&gt;0）
	 * @param capacity         突发容量（桶大小，&gt;0）
	 */
	public RateLimiter(double permitsPerSecond, double capacity) {
		if (permitsPerSecond <= 0) {
			throw new IllegalArgumentException("permitsPerSecond 必须大于 0");
		}
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity 必须大于 0");
		}
		this.tokensPerNano = permitsPerSecond / 1_000_000_000.0;
		this.capacity = capacity;
		this.storedTokens = capacity;
		this.nextFreeTicketNanos.set(System.nanoTime());
	}

	/**
	 * 尝试获取 1 个许可，不等待。
	 *
	 * @return 是否获得许可
	 */
	public boolean tryAcquire() {
		return tryAcquire(1);
	}

	/**
	 * 尝试获取指定数量许可，不等待。
	 *
	 * @param permits 许可数（&gt;0）
	 * @return 是否获得许可
	 */
	public boolean tryAcquire(int permits) {
		if (permits <= 0) {
			throw new IllegalArgumentException("permits 必须大于 0");
		}
		double now = System.nanoTime();
		synchronized (this) {
			// 先补充自上次以来的令牌
			replenish(now);
			if (storedTokens >= permits) {
				storedTokens -= permits;
				return true;
			}
			return false;
		}
	}

	/**
	 * 阻塞等待直到获得许可（可被中断）。
	 *
	 * @throws InterruptedException 线程中断
	 */
	public void acquire() throws InterruptedException {
		acquire(1);
	}

	/**
	 * 阻塞等待直到获得指定数量许可（可被中断）。
	 *
	 * @param permits 许可数（&gt;0）
	 * @throws InterruptedException 线程中断
	 */
	public void acquire(int permits) throws InterruptedException {
		if (permits <= 0) {
			throw new IllegalArgumentException("permits 必须大于 0");
		}
		double now = System.nanoTime();
		synchronized (this) {
			replenish(now);
			if (storedTokens >= permits) {
				storedTokens -= permits;
				return;
			}
			double missing = permits - storedTokens;
			storedTokens = 0;
			long waitNanos = (long) (missing / tokensPerNano);
			nextFreeTicketNanos.set((long) (now + waitNanos));
		}
		long sleep = nextFreeTicketNanos.get() - System.nanoTime();
		if (sleep > 0) {
			Thread.sleep(sleep / 1_000_000, (int) (sleep % 1_000_000));
		}
	}

	private void replenish(double now) {
		double elapsed = now - nextFreeTicketNanos.get();
		if (elapsed > 0) {
			double added = elapsed * tokensPerNano;
			storedTokens = Math.min(capacity, storedTokens + added);
		}
	}
}
