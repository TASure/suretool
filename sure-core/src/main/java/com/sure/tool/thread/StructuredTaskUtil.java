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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 结构化并发工具类：扇出-聚合、首成功短路与限时编排。
 *
 * <p>本类基于虚拟线程池 + 任务取消实现结构化语义：</p>
 * <ul>
 *     <li><b>扇出-聚合</b>：{@link #parallel} 并发执行全部任务，任一失败即取消其余并传播异常；</li>
 *     <li><b>首成功短路</b>：{@link #anyOf} 返回第一个成功的结果，其余任务被取消；</li>
 *     <li><b>限时编排</b>：两种模式均支持整体超时，超时自动取消未完成任务；</li>
 *     <li>所有任务在虚拟线程上执行，可承载高扇出并发。</li>
 * </ul>
 *
 * <p>注意：JDK 21 的 {@code StructuredTaskScope} 仍为 preview API，不适合发布制品；
 * 本类以稳定 API 提供等价语义，待 JDK 24+ 正式化后可在内部平滑迁移实现。</p>
 *
 * @since 1.1.0
 */
public final class StructuredTaskUtil {

	private StructuredTaskUtil() {
	}

	/**
	 * 并发执行全部任务，全部成功后按输入顺序返回结果；任一任务失败则取消其余任务，
	 * 并抛出 {@link RuntimeException}（携带任务异常）。
	 *
	 * @param tasks 任务列表，不允许包含 {@code null}
	 * @param <T>   任务返回类型
	 * @return 按输入顺序排列的结果列表
	 */
	@SafeVarargs
	public static <T> List<T> parallel(Callable<T>... tasks) {
		return parallel(List.of(tasks));
	}

	/**
	 * 并发执行全部任务，全部成功后按输入顺序返回结果；任一任务失败则取消其余任务，
	 * 并抛出 {@link RuntimeException}（携带任务异常）。
	 *
	 * @param tasks 任务集合，可为空
	 * @param <T>   任务返回类型
	 * @return 按输入顺序排列的结果列表；输入为空时返回空列表
	 */
	public static <T> List<T> parallel(Collection<Callable<T>> tasks) {
		return parallel(tasks, null);
	}

	/**
	 * 限时并发执行全部任务：在指定时间内全部成功则按输入顺序返回结果；
	 * 超时或任一任务失败均取消未完成任务并抛出 {@link RuntimeException}。
	 *
	 * @param tasks   任务集合，可为空
	 * @param timeout 整体执行时限，{@code null} 表示不限时
	 * @param <T>     任务返回类型
	 * @return 按输入顺序排列的结果列表；输入为空时返回空列表
	 */
	public static <T> List<T> parallel(Collection<Callable<T>> tasks, Duration timeout) {
		if (tasks == null || tasks.isEmpty()) {
			return List.of();
		}
		ExecutorService executor = ThreadUtil.virtualExecutor();
		try {
			List<Future<T>> futures = new ArrayList<>(tasks.size());
			for (Callable<T> task : tasks) {
				futures.add(executor.submit(task));
			}
			List<T> results = new ArrayList<>(futures.size());
			long deadline = timeout == null ? 0L : System.nanoTime() + timeout.toNanos();
			for (Future<T> future : futures) {
				try {
					results.add(timeout == null ? future.get()
							: future.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS));
				} catch (ExecutionException e) {
					cancelAll(futures);
					Throwable cause = e.getCause() == null ? e : e.getCause();
					throw cause instanceof RuntimeException ? (RuntimeException) cause
							: new RuntimeException("parallel 任务执行失败", cause);
				} catch (TimeoutException e) {
					cancelAll(futures);
					throw new RuntimeException("parallel 执行超时", e);
				} catch (CancellationException e) {
					throw new RuntimeException("parallel 执行被取消", e);
				}
			}
			return List.copyOf(results);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("parallel 执行被中断", e);
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * 并发执行全部任务，返回第一个成功的结果，其余任务被取消；
	 * 全部失败抛出 {@link RuntimeException}（携带最后一个任务异常）。
	 *
	 * @param tasks 任务列表，不允许为空
	 * @param <T>   任务返回类型
	 * @return 第一个成功任务的结果
	 */
	@SafeVarargs
	public static <T> T anyOf(Callable<T>... tasks) {
		return anyOf(List.of(tasks), null);
	}

	/**
	 * 限时并发执行全部任务，返回第一个成功的结果；超时或全部失败均抛出
	 * {@link RuntimeException}。
	 *
	 * @param tasks   任务集合，不允许为空
	 * @param timeout 整体执行时限，{@code null} 表示不限时
	 * @param <T>     任务返回类型
	 * @return 第一个成功任务的结果
	 */
	public static <T> T anyOf(Collection<Callable<T>> tasks, Duration timeout) {
		if (tasks == null || tasks.isEmpty()) {
			throw new IllegalArgumentException("anyOf 任务集合不允许为空");
		}
		ExecutorService executor = ThreadUtil.virtualExecutor();
		try {
			ExecutorCompletionService<T> completion = new ExecutorCompletionService<>(executor);
			int total = tasks.size();
			AtomicInteger failures = new AtomicInteger();
			Throwable lastFailure = null;
			for (Callable<T> task : tasks) {
				completion.submit(task);
			}
			long deadline = timeout == null ? 0L : System.nanoTime() + timeout.toNanos();
			while (true) {
				Future<T> future = timeout == null ? completion.take()
						: completion.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
				if (future == null) {
					throw new RuntimeException("anyOf 执行超时");
				}
				try {
					return future.get();
				} catch (ExecutionException e) {
					lastFailure = e.getCause() == null ? e : e.getCause();
					if (failures.incrementAndGet() == total) {
						throw new RuntimeException("anyOf 全部任务失败", lastFailure);
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("anyOf 执行被中断", e);
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * 取消集合内所有未完成任务。
	 *
	 * @param futures 任务句柄集合
	 */
	private static void cancelAll(List<? extends Future<?>> futures) {
		for (Future<?> future : futures) {
			future.cancel(true);
		}
	}
}
