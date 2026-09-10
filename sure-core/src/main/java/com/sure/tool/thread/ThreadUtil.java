package com.sure.tool.thread;

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
