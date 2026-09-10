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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池构造器，链式配置并构建 {@link ThreadPoolExecutor}，参考 Hutool 的 {@code ExecutorBuilder} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ExecutorBuilder {

	private int corePoolSize = 1;
	private int maxPoolSize = 200;
	private long keepAliveTime = 60;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private int queueCapacity = 1024;
	private String threadNamePrefix = "suretool-pool-";
	private boolean daemon = true;
	private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

	/**
	 * 创建构造器。
	 *
	 * @return 构造器
	 */
	public static ExecutorBuilder create() {
		return new ExecutorBuilder();
	}

	/**
	 * 设置核心线程数。
	 *
	 * @param corePoolSize 核心线程数
	 * @return 当前构造器
	 */
	public ExecutorBuilder setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
		return this;
	}

	/**
	 * 设置最大线程数。
	 *
	 * @param maxPoolSize 最大线程数
	 * @return 当前构造器
	 */
	public ExecutorBuilder setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		return this;
	}

	/**
	 * 设置空闲线程存活时间。
	 *
	 * @param keepAliveTime 存活时间
	 * @param timeUnit      时间单位
	 * @return 当前构造器
	 */
	public ExecutorBuilder setKeepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
		this.keepAliveTime = keepAliveTime;
		this.timeUnit = timeUnit;
		return this;
	}

	/**
	 * 设置任务队列容量（&gt;0 时使用有界队列，否则无界）。
	 *
	 * @param queueCapacity 队列容量
	 * @return 当前构造器
	 */
	public ExecutorBuilder setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
		return this;
	}

	/**
	 * 设置线程名前缀。
	 *
	 * @param threadNamePrefix 前缀
	 * @return 当前构造器
	 */
	public ExecutorBuilder setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
		return this;
	}

	/**
	 * 设置线程是否守护。
	 *
	 * @param daemon 是否守护
	 * @return 当前构造器
	 */
	public ExecutorBuilder setDaemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	/**
	 * 设置拒绝策略。
	 *
	 * @param rejectedHandler 拒绝策略
	 * @return 当前构造器
	 */
	public ExecutorBuilder setRejectedHandler(RejectedExecutionHandler rejectedHandler) {
		this.rejectedHandler = rejectedHandler;
		return this;
	}

	/**
	 * 构建线程池。
	 *
	 * @return 线程池
	 */
	public ThreadPoolExecutor build() {
		BlockingQueue<Runnable> queue = queueCapacity > 0
				? new ArrayBlockingQueue<>(queueCapacity)
				: new LinkedBlockingQueue<>();
		AtomicInteger seq = new AtomicInteger(1);
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, queue, r -> {
			Thread thread = new Thread(r, threadNamePrefix + seq.getAndIncrement());
			thread.setDaemon(daemon);
			return thread;
		}, rejectedHandler);
	}
}