package com.sure.tool.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 并发同步器：以固定线程数并发执行一组任务并等待全部完成，参考 Hutool 的 {@code SyncFinisher} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class SyncFinisher {

	/**
	 * 工作单元。
	 */
	public interface Worker {

		/**
		 * 执行任务。
		 *
		 * @throws Exception 任务异常
		 */
		void work() throws Exception;
	}

	private final int workers;
	private final List<Worker> tasks = new ArrayList<>();
	private final List<Throwable> errors = new CopyOnWriteArrayList<>();
	private ExecutorService pool;
	private CountDownLatch latch;

	/**
	 * 创建同步器。
	 *
	 * @param workers 并发线程数
	 */
	public SyncFinisher(int workers) {
		this.workers = Math.max(1, workers);
	}

	/**
	 * 添加任务。
	 *
	 * @param worker 任务
	 * @return 当前同步器
	 */
	public SyncFinisher addWorker(Worker worker) {
		tasks.add(worker);
		return this;
	}

	/**
	 * 重复添加同一任务多次。
	 *
	 * @param count  次数
	 * @param worker 任务
	 * @return 当前同步器
	 */
	public SyncFinisher repeat(int count, Worker worker) {
		for (int i = 0; i < count; i++) {
			tasks.add(worker);
		}
		return this;
	}

	/**
	 * 启动并发执行。
	 *
	 * @return 当前同步器
	 */
	public synchronized SyncFinisher start() {
		errors.clear();
		pool = Executors.newFixedThreadPool(workers);
		latch = new CountDownLatch(tasks.size());
		for (Worker task : tasks) {
			pool.submit(() -> {
				try {
					task.work();
				} catch (Throwable t) {
					errors.add(t);
				} finally {
					latch.countDown();
				}
			});
		}
		return this;
	}

	/**
	 * 等待全部任务完成，若存在任务异常则抛出。
	 *
	 * @return 当前同步器
	 * @throws InterruptedException 等待被中断
	 */
	public SyncFinisher await() throws InterruptedException {
		latch.await();
		if (!errors.isEmpty()) {
			throw new RuntimeException("SyncFinisher 有 " + errors.size() + " 个任务执行失败", errors.get(0));
		}
		return this;
	}

	/**
	 * 启动并等待全部完成。
	 *
	 * @return 当前同步器
	 * @throws InterruptedException 等待被中断
	 */
	public SyncFinisher sync() throws InterruptedException {
		start();
		await();
		return this;
	}

	/**
	 * 停止并释放线程池（复用前需重新调用 {@link #start()}）。
	 */
	public void stop() {
		if (pool != null) {
			pool.shutdown();
			pool = null;
		}
	}
}
