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
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 轻量 Actor 模型：消息驱动的并发单元，Hutool / Guava / Apache Commons 三家均无的独占能力。
 *
 * <p>每个 Actor 拥有独立邮箱与一个虚拟线程消息循环：</p>
 * <ul>
 *     <li><b>顺序消费</b>：消息按投递顺序被 {@code handler} 逐个处理；</li>
 *     <li><b>错误隔离</b>：单条消息处理异常不中断消息循环，仅回调 {@code errorHandler}（可缺省）；</li>
 *     <li><b>优雅关闭</b>：{@link #close()} 后拒绝新消息，已入队消息全部处理完毕才退出；</li>
 *     <li><b>邮箱策略</b>：无界（默认）或有界（可配容量，满时投递抛异常或限时等待）；</li>
 *     <li>全部公开方法线程安全。</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>{@code
 * Actor<String> logger = Actor.start(msg -> System.out.println("收到: " + msg));
 * logger.send("hello");
 * logger.send("world");
 * logger.close();
 * }</pre>
 *
 * @param <T> 消息类型
 * @author suretool
 * @since 1.19.0
 */
public final class Actor<T> {

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	private final BlockingQueue<T> mailbox;
	private final Consumer<T> handler;
	private final Consumer<Throwable> errorHandler;
	/** 消息循环虚拟线程（包内可见，供同包测试与诊断） */
	Thread loopThread;
	private volatile boolean closed;

	private Actor(Consumer<T> handler, int capacity, Consumer<Throwable> errorHandler) {
		this.handler = handler;
		this.errorHandler = errorHandler;
		this.mailbox = capacity > 0 ? new ArrayBlockingQueue<>(capacity) : new LinkedBlockingQueue<>();
	}

	/**
	 * 启动一个无界邮箱 Actor，消息在独立虚拟线程上被顺序消费。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<Integer> actor = Actor.start(n -> System.out.println("n = " + n));
	 * actor.send(1);
	 * }</pre>
	 *
	 * @param handler 消息处理器，不允许为 {@code null}
	 * @param <T>     消息类型
	 * @return 已启动的 Actor
	 * @throws IllegalArgumentException handler 为 {@code null} 时抛出
	 */
	public static <T> Actor<T> start(Consumer<T> handler) {
		return start(handler, 0, null);
	}

	/**
	 * 启动一个邮箱容量受限的 Actor（容量 ≤ 0 视为无界）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<String> actor = Actor.start(s -> { }, 16);
	 * }</pre>
	 *
	 * @param handler  消息处理器，不允许为 {@code null}
	 * @param capacity 邮箱容量，≤ 0 表示无界
	 * @param <T>      消息类型
	 * @return 已启动的 Actor
	 * @throws IllegalArgumentException handler 为 {@code null} 时抛出
	 */
	public static <T> Actor<T> start(Consumer<T> handler, int capacity) {
		return start(handler, capacity, null);
	}

	/**
	 * 启动一个带错误回调的 Actor（容量 ≤ 0 视为无界）。
	 *
	 * <p>单条消息处理异常不中断消息循环；{@code errorHandler} 为 {@code null} 时异常被忽略。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<String> actor = Actor.start(msg -> { throw new RuntimeException("boom"); },
	 *         16, t -> System.err.println("处理异常: " + t));
	 * actor.send("x");
	 * }</pre>
	 *
	 * @param handler      消息处理器，不允许为 {@code null}
	 * @param capacity     邮箱容量，≤ 0 表示无界
	 * @param errorHandler 消息处理异常回调，可为 {@code null}
	 * @param <T>          消息类型
	 * @return 已启动的 Actor
	 * @throws IllegalArgumentException handler 为 {@code null} 时抛出
	 */
	public static <T> Actor<T> start(Consumer<T> handler, int capacity, Consumer<Throwable> errorHandler) {
		if (handler == null) {
			throw new IllegalArgumentException("handler 不能为 null");
		}
		Actor<T> actor = new Actor<>(handler, capacity, errorHandler);
		actor.loopThread = Thread.ofVirtual().name("sure-actor-" + SEQUENCE.incrementAndGet()).start(actor::runLoop);
		return actor;
	}

	/**
	 * 异步投递一条消息（非阻塞）。
	 *
	 * <p>无界邮箱恒成功；有界邮箱已满时抛出 {@link IllegalStateException}。
	 * 需要限时等待入队请使用 {@link #trySend(Object, Duration)}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<String> actor = Actor.start(s -> { });
	 * actor.send("ping");
	 * }</pre>
	 *
	 * @param message 消息，不允许为 {@code null}
	 * @throws NullPointerException  message 为 {@code null} 时抛出
	 * @throws IllegalStateException Actor 已关闭或邮箱已满时抛出
	 */
	public void send(T message) {
		Objects.requireNonNull(message, "message 不能为 null");
		if (closed) {
			throw new IllegalStateException("actor 已关闭");
		}
		if (!mailbox.offer(message)) {
			throw new IllegalStateException("actor 邮箱已满");
		}
	}

	/**
	 * 限时投递一条消息：在指定时间内成功入队返回 {@code true}，超时返回 {@code false}。
	 *
	 * <p>适用于有界邮箱的背压控制；{@code timeout} 为 {@code null} 视为立即尝试。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<String> actor = Actor.start(s -> { }, 1);
	 * boolean ok = actor.trySend("ping", Duration.ofMillis(100));
	 * }</pre>
	 *
	 * @param message 消息，不允许为 {@code null}
	 * @param timeout 等待时限，{@code null} 视为立即尝试
	 * @return 成功入队返回 {@code true}，超时返回 {@code false}
	 * @throws NullPointerException  message 为 {@code null} 时抛出
	 * @throws IllegalStateException Actor 已关闭时抛出
	 */
	public boolean trySend(T message, Duration timeout) {
		Objects.requireNonNull(message, "message 不能为 null");
		if (closed) {
			throw new IllegalStateException("actor 已关闭");
		}
		long nanos = timeout == null ? 0L : timeout.toNanos();
		try {
			return mailbox.offer(message, nanos, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	/**
	 * 优雅关闭：拒绝新消息，已入队消息全部处理完毕后消息循环退出；幂等。
	 *
	 * <p>关闭后 {@link #send} 与 {@link #trySend} 抛出 {@link IllegalStateException}。</p>
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Actor<String> actor = Actor.start(s -> { });
	 * actor.send("flush");
	 * actor.close();
	 * }</pre>
	 */
	public void close() {
		closed = true;
	}

	/**
	 * 是否已关闭。
	 *
	 * @return 已关闭返回 {@code true}
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 当前待处理消息数（诊断/监控用途）。
	 *
	 * @return 邮箱中尚未被消费的消息数
	 */
	public int pendingCount() {
		return mailbox.size();
	}

	private void runLoop() {
		while (true) {
			T message;
			if (closed) {
				message = mailbox.poll();
				if (message == null) {
					return;
				}
			} else {
				try {
					message = mailbox.take();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
			}
			try {
				handler.accept(message);
			} catch (Throwable t) {
				if (errorHandler != null) {
					try {
						errorHandler.accept(t);
					} catch (Throwable ignored) {
						// 错误回调自身异常不影响消息循环
					}
				}
			}
		}
	}
}
