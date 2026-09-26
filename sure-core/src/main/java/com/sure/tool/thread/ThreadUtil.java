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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工具类：异步执行、休眠、线程工厂，参考 Hutool 的 {@code ThreadUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ThreadUtil {

	/**
	 * 共享线程池（守护线程，不阻止 JVM 退出）。
	 */
	private static final ExecutorService POOL = Executors.newCachedThreadPool(new NamedThreadFactory("suretool-", true));

	private ThreadUtil() {
	}

	/**
	 * 异步执行任务。
	 *
	 * @param runnable 任务
	 * @return Future
	 */
	public static Future<?> execAsync(Runnable runnable) {
		return POOL.submit(runnable);
	}

	/**
	 * 异步执行有返回值任务。
	 *
	 * @param callable 任务
	 * @param <T>      返回类型
	 * @return Future
	 */
	public static <T> Future<T> execAsync(Callable<T> callable) {
		return POOL.submit(callable);
	}

	/**
	 * 后台线程执行任务（不阻塞当前线程，不捕获异常）。
	 *
	 * @param runnable 任务
	 */
	public static void execute(Runnable runnable) {
		POOL.execute(runnable);
	}

	/**
	 * 休眠（毫秒），中断时恢复中断标志并抛运行时异常。
	 *
	 * @param millis 毫秒数
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("休眠被中断", e);
		}
	}

	/**
	 * 创建命名线程。
	 *
	 * @param runnable 任务
	 * @param name     线程名
	 * @return 线程
	 */
	public static Thread newThread(Runnable runnable, String name) {
		return newThread(runnable, name, false);
	}

	/**
	 * 创建命名线程。
	 *
	 * @param runnable 任务
	 * @param name     线程名
	 * @param daemon   是否守护线程
	 * @return 线程
	 */
	public static Thread newThread(Runnable runnable, String name, boolean daemon) {
		Thread thread = new Thread(runnable, name);
		thread.setDaemon(daemon);
		return thread;
	}

	/**
	 * 获取共享线程池。
	 *
	 * @return 共享线程池
	 */
	public static ExecutorService getExecutor() {
		return POOL;
	}

	/**
	 * 处理器核数。
	 *
	 * @return CPU 核数
	 */
	public static int getProcessorCount() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * 安静关闭线程池（最多等待 30 秒）。
	 *
	 * @param executor 线程池
	 */
	public static void shutdownQuietly(ExecutorService executor) {
		if (executor == null) {
			return;
		}
		executor.shutdown();
		try {
			if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 启动一个虚拟线程执行任务（JDK 21+）。
	 *
	 * @param runnable 任务
	 * @return 已启动的虚拟线程
	 */
	public static Thread startVirtualThread(Runnable runnable) {
		return Thread.startVirtualThread(runnable);
	}

	/**
	 * 创建虚拟线程（未启动）。
	 *
	 * @param runnable 任务
	 * @param name     线程名
	 * @return 虚拟线程
	 */
	public static Thread newVirtualThread(Runnable runnable, String name) {
		return Thread.ofVirtual().name(name).unstarted(runnable);
	}

	/**
	 * 创建每任务一虚拟线程的执行器（JDK 21+，适合高并发 I/O 密集任务）。
	 *
	 * @return ExecutorService
	 */
	public static ExecutorService virtualExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	/**
	 * 创建带命名前缀的每任务一虚拟线程执行器。
	 *
	 * @param prefix 线程名前缀
	 * @return ExecutorService
	 */
	public static ExecutorService virtualExecutor(String prefix) {
		return Executors.newThreadPerTaskExecutor(
				Thread.ofVirtual().name(prefix + "-", 0).factory());
	}

	/**
	 * 虚拟线程工厂（Thread.ofVirtual 便捷入口）。
	 *
	 * @param prefix 线程名前缀
	 * @return ThreadFactory
	 */
	public static ThreadFactory virtualThreadFactory(String prefix) {
		return Thread.ofVirtual().name(prefix + "-", 0).factory();
	}

	/**
	 * 判断线程是否为虚拟线程。
	 *
	 * @param thread 线程
	 * @return 是否虚拟线程
	 */
	public static boolean isVirtual(Thread thread) {
		return thread != null && thread.isVirtual();
	}

	/**
	 * 判断当前线程是否为虚拟线程。
	 *
	 * @return 是否虚拟线程
	 */
	public static boolean isVirtual() {
		return Thread.currentThread().isVirtual();
	}

	/**
	 * 用虚拟线程并发执行全部任务并等待完成；任一任务抛异常则聚合抛出。
	 *
	 * @param tasks 任务列表
	 */
	public static void parallel(Runnable... tasks) {
		if (tasks == null || tasks.length == 0) {
			return;
		}
		ExecutorService executor = virtualExecutor();
		try {
			List<Future<?>> futures = new java.util.ArrayList<>(tasks.length);
			for (Runnable task : tasks) {
				futures.add(executor.submit(task));
			}
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("并行任务被中断", e);
		} catch (java.util.concurrent.ExecutionException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw cause instanceof RuntimeException ? (RuntimeException) cause
					: new IllegalStateException("并行任务执行失败", cause);
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * 用虚拟线程并发执行有返回值任务并返回结果（按入参顺序）。
	 *
	 * @param tasks 任务列表
	 * @param <T>   返回类型
	 * @return 结果列表
	 */
	public static <T> List<T> invokeAll(java.util.List<java.util.concurrent.Callable<T>> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		ExecutorService executor = virtualExecutor();
		try {
			List<java.util.concurrent.Future<T>> futures = executor.invokeAll(tasks);
			List<T> results = new java.util.ArrayList<>(futures.size());
			for (java.util.concurrent.Future<T> future : futures) {
				results.add(future.get());
			}
			return results;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("并行任务被中断", e);
		} catch (java.util.concurrent.ExecutionException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw cause instanceof RuntimeException ? (RuntimeException) cause
					: new IllegalStateException("并行任务执行失败", cause);
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * 并行执行全部任务并在指定时限内返回结果（限时版本）。
	 *
	 * <p>超时后未完成的任务会被取消，并抛出 {@link IllegalStateException}（携带超时原因）。</p>
	 *
	 * @param tasks   任务列表，可为空
	 * @param timeout 整体执行时限，不允许为 {@code null}
	 * @param <T>     返回类型
	 * @return 结果列表；输入为空时返回空列表
	 * @since 1.1.0
	 */
	public static <T> List<T> invokeAll(java.util.List<java.util.concurrent.Callable<T>> tasks, java.time.Duration timeout) {
		if (tasks == null || tasks.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		ExecutorService executor = virtualExecutor();
		try {
			List<java.util.concurrent.Future<T>> futures = executor.invokeAll(tasks,
					timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
			List<T> results = new java.util.ArrayList<>(futures.size());
			for (java.util.concurrent.Future<T> future : futures) {
				try {
					results.add(future.get());
				} catch (java.util.concurrent.CancellationException ce) {
					throw new IllegalStateException("并行任务执行超时", ce);
				}
			}
			return results;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("并行任务被中断", e);
		} catch (java.util.concurrent.ExecutionException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw cause instanceof RuntimeException ? (RuntimeException) cause
					: new IllegalStateException("并行任务执行失败", cause);
		} finally {
			executor.shutdown();
		}
	}

	/**
	 * 命名线程工厂。
	 */
	public static class NamedThreadFactory implements ThreadFactory {

		private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
		private final AtomicInteger threadSeq = new AtomicInteger(1);
		private final String prefix;
		private final boolean daemon;

		/**
		 * 创建线程工厂。
		 *
		 * @param prefix 线程名前缀
		 * @param daemon 是否守护线程
		 */
		public NamedThreadFactory(String prefix, boolean daemon) {
			this.prefix = prefix + POOL_SEQ.getAndIncrement() + "-";
			this.daemon = daemon;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, prefix + threadSeq.getAndIncrement());
			thread.setDaemon(daemon);
			return thread;
		}
	}
}