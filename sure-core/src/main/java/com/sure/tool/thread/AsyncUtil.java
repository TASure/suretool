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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * 虚拟线程一等公民的异步门面。
 *
 * <p>默认全部使用虚拟线程执行，无需手动构造线程池；同时提供
 * {@link CompletableFuture} 风格的链式异步与结构化并发语义
 * （并行任务任一失败即取消其余并传播首个异常；说明：JDK 25 中
 * {@code StructuredTaskScope} 仍为 preview API，本项目不使用，等价语义
 * 由虚拟线程执行器 + Future 实现，与 {@link StructuredTaskUtil} 一致）。</p>
 *
 * <p>与 {@link StructuredTaskUtil} 的区别：本类返回 {@link CompletableFuture}，
 * 支持非阻塞链式编排；需要阻塞等待时可配合 {@link #await(Duration, Future)} /
 * {@link #joinAll(Duration, Callable...)}。</p>
 *
 * @since 1.12.0
 */
public final class AsyncUtil {

	private AsyncUtil() {
	}

	/**
	 * 虚拟线程异步执行（fire-and-forget）。
	 *
	 * @param runnable 任务
	 * @return Future（异常经 {@link Future#get()} 传播）
	 */
	public static Future<?> runAsync(Runnable runnable) {
		Objects.requireNonNull(runnable, "runnable must not be null");
		return supplyAsync(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * 指定执行器异步执行（fire-and-forget）。
	 *
	 * @param runnable  任务
	 * @param executor  执行器
	 * @return Future（异常经 {@link Future#get()} 传播）
	 */
	public static Future<?> runAsync(Runnable runnable, Executor executor) {
		Objects.requireNonNull(runnable, "runnable must not be null");
		Objects.requireNonNull(executor, "executor must not be null");
		CompletableFuture<Void> cf = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				runnable.run();
				cf.complete(null);
			} catch (Throwable t) {
				cf.completeExceptionally(t);
			}
		});
		return cf;
	}

	/**
	 * 虚拟线程异步供应值。
	 *
	 * @param supplier 供应器
	 * @param <T>      结果泛型
	 * @return CompletableFuture（异常经 {@link CompletableFuture#get()} 传播）
	 */
	public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		CompletableFuture<T> cf = new CompletableFuture<>();
		Thread.ofVirtual().name("sure-async").start(() -> {
			try {
				cf.complete(supplier.get());
			} catch (Throwable t) {
				cf.completeExceptionally(t);
			}
		});
		return cf;
	}

	/**
	 * 并行全部完成（失败取消其余并传播首个异常，等价结构化并发语义）。
	 *
	 * @param tasks 任务列表
	 * @param <T>   结果泛型
	 * @return 按输入顺序排列的结果 CompletableFuture
	 */
	@SafeVarargs
	public static <T> CompletableFuture<List<T>> allOf(Callable<T>... tasks) {
		Objects.requireNonNull(tasks, "tasks must not be null");
		CompletableFuture<List<T>> result = new CompletableFuture<>();
		Thread.ofVirtual().name("sure-async-allof").start(() -> {
			ExecutorService executor = ThreadUtil.virtualExecutor("sure-async-allof-worker");
			try {
				List<Future<T>> futures = new ArrayList<>(tasks.length);
				for (Callable<T> task : tasks) {
					futures.add(executor.submit(task));
				}
				List<T> values = new ArrayList<>(tasks.length);
				for (Future<T> future : futures) {
					values.add(future.get());
				}
				result.complete(values);
			} catch (Throwable t) {
				cancelAll(executor);
				result.completeExceptionally(unwrap(t));
			} finally {
				executor.shutdownNow();
			}
		});
		return result;
	}

	/**
	 * 首个成功即返回（其余任务被取消）。
	 *
	 * @param tasks 任务列表
	 * @param <T>   结果泛型
	 * @return 首个成功结果 CompletableFuture；全部失败则异常
	 */
	@SafeVarargs
	public static <T> CompletableFuture<T> anyOf(Callable<T>... tasks) {
		Objects.requireNonNull(tasks, "tasks must not be null");
		CompletableFuture<T> result = new CompletableFuture<>();
		Thread.ofVirtual().name("sure-async-anyof").start(() -> {
			ExecutorService executor = ThreadUtil.virtualExecutor("sure-async-anyof-worker");
			try {
				List<Future<T>> futures = new ArrayList<>(tasks.length);
				for (Callable<T> task : tasks) {
					futures.add(executor.submit(task));
				}
				T winner = null;
				Throwable lastFailure = null;
				for (Future<T> future : futures) {
					try {
						T value = future.get();
						winner = value;
						break;
					} catch (ExecutionException e) {
						lastFailure = e.getCause() == null ? e : e.getCause();
					}
				}
				if (winner != null) {
					result.complete(winner);
				} else {
					result.completeExceptionally(lastFailure == null
							? new IllegalStateException("all tasks failed")
							: lastFailure);
				}
			} catch (Throwable t) {
				result.completeExceptionally(unwrap(t));
			} finally {
				cancelAll(executor);
				executor.shutdownNow();
			}
		});
		return result;
	}

	/**
	 * 限时供应：超时抛 {@link TimeoutException}。
	 *
	 * @param timeout  超时
	 * @param supplier 供应器
	 * @param <T>      结果泛型
	 * @return 供应值
	 * @throws TimeoutException     超时
	 * @throws ExecutionException   任务异常
	 * @throws InterruptedException 线程中断
	 */
	public static <T> T withTimeout(Duration timeout, Supplier<T> supplier)
			throws TimeoutException, ExecutionException, InterruptedException {
		Objects.requireNonNull(timeout, "timeout must not be null");
		return await(timeout, supplyAsync(supplier));
	}

	/**
	 * 限时等待 Future 并解包 {@link ExecutionException}（保留原始 cause）。
	 *
	 * @param timeout 超时
	 * @param future  Future
	 * @param <T>     结果泛型
	 * @return 结果
	 * @throws TimeoutException     超时
	 * @throws ExecutionException   任务异常（cause 为真实异常）
	 * @throws InterruptedException 线程中断
	 */
	public static <T> T await(Duration timeout, Future<T> future)
			throws TimeoutException, ExecutionException, InterruptedException {
		Objects.requireNonNull(timeout, "timeout must not be null");
		Objects.requireNonNull(future, "future must not be null");
		try {
			return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new ExecutionException("task failed: " + cause, cause);
		}
	}

	/**
	 * 限时并行聚合（全部完成后返回，任一失败取消其余）。
	 *
	 * @param timeout 总超时
	 * @param tasks   任务列表
	 * @param <T>     结果泛型
	 * @return 按输入顺序排列的结果列表
	 * @throws TimeoutException     超时
	 * @throws ExecutionException   首个任务异常
	 * @throws InterruptedException 线程中断
	 */
	@SafeVarargs
	public static <T> List<T> joinAll(Duration timeout, Callable<T>... tasks)
			throws TimeoutException, ExecutionException, InterruptedException {
		Objects.requireNonNull(timeout, "timeout must not be null");
		Objects.requireNonNull(tasks, "tasks must not be null");
		ExecutorService executor = ThreadUtil.virtualExecutor("sure-async-joinall");
		try {
			List<Future<T>> futures = new ArrayList<>(tasks.length);
			for (Callable<T> task : tasks) {
				futures.add(executor.submit(task));
			}
			long deadline = System.nanoTime() + timeout.toNanos();
			List<T> values = new ArrayList<>(tasks.length);
			for (Future<T> future : futures) {
				values.add(future.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS));
			}
			return List.copyOf(values);
		} catch (ExecutionException e) {
			cancelAll(executor);
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new ExecutionException("task failed: " + cause, cause);
		} catch (TimeoutException e) {
			cancelAll(executor);
			throw e;
		} finally {
			executor.shutdownNow();
		}
	}

	private static void cancelAll(ExecutorService executor) {
		executor.shutdownNow();
	}

	private static Throwable unwrap(Throwable t) {
		Throwable current = t;
		while (current instanceof ExecutionException && current.getCause() != null) {
			current = current.getCause();
		}
		return current;
	}
}
