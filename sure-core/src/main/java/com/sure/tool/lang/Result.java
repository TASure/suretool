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
package com.sure.tool.lang;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 显式错误传播的两态结果门面（成功值 / 失败信息）。
 *
 * <p>与异常流互补：适合业务校验、批量聚合等"失败信息需要显式携带"的场景，
 * 支持 {@link #map}/{@link #flatMap} 链式短路、{@link #recover} 恢复与
 * {@link #throwIfFailed} 终止式转换。零依赖，对标 Vavr {@code Result}。</p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * Result<Integer> r = Result.ok(1)
 *         .map(x -> x * 2)                       // 2
 *         .onSuccess(v -> log(v))
 *         .recover(e -> 0);                      // 失败恢复
 * if (r.isFailure()) { throw new BizException(r.getErrorMessage()); }
 * }</pre>
 *
 * <p><b>null 语义：</b>{@link #ok} 允许 null 值；{@link #fail} 不允许 null
 * 信息（立即 NPE）；失败时 {@link #get}/{@link #throwIfFailed} 抛异常，
 * {@link #getOrNull}/{@link #getOrElse} 返回默认值。</p>
 *
 * @param <T> 成功值类型
 * @since 1.12.0
 */
public final class Result<T> {

	private final T value;
	private final String errorMessage;
	private final Throwable cause;

	private Result(T value, String errorMessage, Throwable cause) {
		this.value = value;
		this.errorMessage = errorMessage;
		this.cause = cause;
	}

	/**
	 * 成功结果。
	 *
	 * @param value 值（可为 null）
	 * @param <T>   泛型
	 * @return Result
	 */
	public static <T> Result<T> ok(T value) {
		return new Result<>(value, null, null);
	}

	/**
	 * 失败结果（仅信息）。
	 *
	 * @param errorMessage 失败信息
	 * @param <T>          泛型
	 * @return Result
	 */
	public static <T> Result<T> fail(String errorMessage) {
		Objects.requireNonNull(errorMessage, "errorMessage must not be null");
		return new Result<>(null, errorMessage, null);
	}

	/**
	 * 失败结果（携带异常）。
	 *
	 * @param cause 异常
	 * @param <T>   泛型
	 * @return Result
	 */
	public static <T> Result<T> fail(Throwable cause) {
		Objects.requireNonNull(cause, "cause must not be null");
		return new Result<>(null, cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage(), cause);
	}

	/**
	 * 失败结果（信息 + 异常）。
	 *
	 * @param errorMessage 失败信息
	 * @param cause        异常（可为 null）
	 * @param <T>          泛型
	 * @return Result
	 */
	public static <T> Result<T> fail(String errorMessage, Throwable cause) {
		Objects.requireNonNull(errorMessage, "errorMessage must not be null");
		return new Result<>(null, errorMessage, cause);
	}

	/**
	 * 是否成功。
	 *
	 * @return true=成功
	 */
	public boolean isSuccess() {
		return errorMessage == null;
	}

	/**
	 * 是否失败。
	 *
	 * @return true=失败
	 */
	public boolean isFailure() {
		return !isSuccess();
	}

	/**
	 * 获取成功值（失败抛 {@link NoSuchElementException}）。
	 *
	 * @return 成功值
	 * @throws NoSuchElementException 失败时
	 */
	public T get() {
		if (isFailure()) {
			throw new NoSuchElementException("Result is failure: " + errorMessage);
		}
		return value;
	}

	/**
	 * 获取成功值；失败返回 null。
	 *
	 * @return 值或 null
	 */
	public T getOrNull() {
		return isSuccess() ? value : null;
	}

	/**
	 * 获取成功值；失败返回默认值。
	 *
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public T getOrElse(T defaultValue) {
		return isSuccess() ? value : defaultValue;
	}

	/**
	 * 获取成功值；失败时由供应器提供。
	 *
	 * @param supplier 默认值供应器
	 * @return 值或供应值
	 */
	public T getOrElseGet(Supplier<T> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		return isSuccess() ? value : supplier.get();
	}

	/**
	 * 映射成功值（失败短路保留原错误）。
	 *
	 * @param mapper 映射函数
	 * @param <U>    新泛型
	 * @return 新 Result
	 */
	public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		if (isFailure()) {
			return fail(errorMessage, cause);
		}
		try {
			return ok(mapper.apply(value));
		} catch (Throwable t) {
			return fail("map failed: " + t.getMessage(), t);
		}
	}

	/**
	 * 平铺映射（失败短路保留原错误；mapper 返回 null 视为成功 null 值）。
	 *
	 * @param mapper 返回 Result 的映射函数
	 * @param <U>    新泛型
	 * @return 新 Result
	 */
	public <U> Result<U> flatMap(Function<? super T, ? extends Result<? extends U>> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		if (isFailure()) {
			return fail(errorMessage, cause);
		}
		try {
			@SuppressWarnings("unchecked")
			Result<U> next = (Result<U>) mapper.apply(value);
			return next == null ? fail("flatMap returned null") : next;
		} catch (Throwable t) {
			return fail("flatMap failed: " + t.getMessage(), t);
		}
	}

	/**
	 * 成功时回调（链式可用，返回自身）。
	 *
	 * @param consumer 成功消费者
	 * @return this
	 */
	public Result<T> onSuccess(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (isSuccess()) {
			consumer.accept(value);
		}
		return this;
	}

	/**
	 * 失败时回调（链式可用，返回自身）。
	 *
	 * @param consumer 失败消费者（入参为错误信息）
	 * @return this
	 */
	public Result<T> onFailure(Consumer<String> consumer) {
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (isFailure()) {
			consumer.accept(errorMessage);
		}
		return this;
	}

	/**
	 * 失败恢复：将失败转换为成功值（成功原样返回）。
	 *
	 * @param recoverer 恢复函数（入参为错误信息）
	 * @return 恢复后的 Result
	 */
	public Result<T> recover(Function<String, ? extends T> recoverer) {
		Objects.requireNonNull(recoverer, "recoverer must not be null");
		if (isSuccess()) {
			return this;
		}
		try {
			return ok(recoverer.apply(errorMessage));
		} catch (Throwable t) {
			return fail("recover failed: " + t.getMessage(), t);
		}
	}

	/**
	 * 失败时抛出异常（携带 cause），成功返回自身值。
	 *
	 * @return 成功值
	 * @throws RuntimeException 失败时（cause 为原始异常，无 cause 时含错误信息）
	 */
	public T throwIfFailed() {
		if (isFailure()) {
			throw cause == null
					? new IllegalStateException(errorMessage)
					: new IllegalStateException(errorMessage, cause);
		}
		return value;
	}

	/**
	 * 获取失败信息（成功返回 null）。
	 *
	 * @return 失败信息或 null
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * 获取失败异常（成功返回 null）。
	 *
	 * @return 异常或 null
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * 转 JDK Optional（失败为空）。
	 *
	 * @return Optional
	 */
	public Optional<T> toOptional() {
		return isSuccess() ? Optional.ofNullable(value) : Optional.empty();
	}

	@Override
	public String toString() {
		return isSuccess() ? "Result.ok(" + value + ")" : "Result.fail(" + errorMessage + ")";
	}
}
