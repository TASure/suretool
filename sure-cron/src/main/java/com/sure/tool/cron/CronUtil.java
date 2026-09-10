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
package com.sure.tool.cron;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cron 定时调度器：注册表达式任务，启动后每秒扫描并在匹配时刻执行，参考 Hutool 的 {@code CronUtil} 设计。
 * <p>
 * 调度线程每秒检查一次；任务执行在独立线程池，互不阻塞。
 * 同一秒内同一任务只执行一次（秒级精度）。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CronUtil {

	private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
	private static final List<CronJob> JOBS = new CopyOnWriteArrayList<>();

	private static volatile ScheduledExecutorService scheduler;
	private static volatile ExecutorService worker;
	private static volatile boolean started;

	private CronUtil() {
	}

	/**
	 * 校验 Cron 表达式是否合法。
	 *
	 * @param cron 表达式
	 * @return 是否合法
	 */
	public static boolean isValid(String cron) {
		return CronPattern.isValid(cron);
	}

	/**
	 * 解析 Cron 表达式。
	 *
	 * @param cron 表达式
	 * @return 表达式对象
	 */
	public static CronPattern parse(String cron) {
		return CronPattern.of(cron);
	}

	/**
	 * 注册定时任务（调度器未启动时注册，{@link #start()} 后生效）。
	 *
	 * @param cron 6 段表达式
	 * @param task 任务
	 */
	public static void schedule(String cron, Runnable task) {
		if (cron == null || task == null) {
			throw new IllegalArgumentException("cron 与 task 不能为 null");
		}
		JOBS.add(new CronJob(CronPattern.of(cron), task));
	}

	/**
	 * 已注册任务数。
	 *
	 * @return 任务数
	 */
	public static int getJobCount() {
		return JOBS.size();
	}

	/**
	 * 启动调度（幂等）。
	 */
	public static synchronized void start() {
		if (started) {
			return;
		}
		started = true;
		if (scheduler == null || scheduler.isShutdown()) {
			scheduler = Executors.newSingleThreadScheduledExecutor(CronUtil::newDaemonThread);
		}
		if (worker == null || worker.isShutdown()) {
			worker = Executors.newCachedThreadPool(CronUtil::newDaemonThread);
		}
		scheduler.scheduleAtFixedRate(CronUtil::tick, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * 停止调度并清空任务（幂等）。
	 */
	public static synchronized void stop() {
		started = false;
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
		if (worker != null) {
			worker.shutdownNow();
		}
		JOBS.clear();
	}

	/**
	 * 每秒扫描一次。
	 */
	private static void tick() {
		if (!started) {
			return;
		}
		Date now = new Date();
		long second = now.getTime() / 1000;
		for (CronJob job : JOBS) {
			if (job.lastSecond != second && job.pattern.match(now)) {
				job.lastSecond = second;
				worker.submit(job.task);
			}
		}
	}

	private static Thread newDaemonThread(Runnable r) {
		Thread thread = new Thread(r, "suretool-cron-" + POOL_SEQ.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	}

	/**
	 * 调度任务单元。
	 */
	private static class CronJob {

		private final CronPattern pattern;
		private final Runnable task;
		private volatile long lastSecond = -1;

		CronJob(CronPattern pattern, Runnable task) {
			this.pattern = pattern;
			this.task = task;
		}
	}
}