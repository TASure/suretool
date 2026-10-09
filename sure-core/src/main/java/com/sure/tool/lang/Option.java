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
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * JDK {@link Optional} 超集门面：补高频便捷方法（{@link #getOrElse}、
 * {@link #onEmpty}、{@link #orElse(Option)}），保持链式 Fluent 体验。
 * 零依赖，对标 Guava {@code Optional} 与 Vavr {@code Option}。
 *
 * @param <T> 值类型
 * @since 1.12.0
 */
public final class Option<T> {

	private final T value;

	private Option(T value) {
		this.value = value;
	}

	/**
	 * 非空值包装（null 抛 NPE，语义与 Optional.of 一致）。
	 *
	 * @param value 值
	 * @param <T>   泛型
	 * @return Option
	 * @throws NullPointerException value 为 null
	 */
	public static <T> Option<T> of(T value) {
		return new Option<>(Objects.requireNonNull(value, "value must not be null"));
	}

	/**
	 * 可空包装。
	 *
	 * @param value 值（可为 null）
	 * @param <T>   泛型
	 * @return Option
	 */
	public static <T> Option<T> ofNullable(T value) {
		return value == null ? empty() : new Option<>(value);
	}

	/**
	 * 空 Option。
	 *
	 * @param <T> 泛型
	 * @return Option
	 */
	@SuppressWarnings("unchecked")
	public static <T> Option<T> empty() {
		return (Option<T>) EmptyHolder.EMPTY;
	}

	/**
	 * 是否有值。
	 *
	 * @return true=有值
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * 是否为空。
	 *
	 * @return true=空
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * 获取值（空抛 {@link NoSuchElementException}）。
	 *
	 * @return 值
	 * @throws NoSuchElementException 空时
	 */
	public T get() {
		if (value == null) {
			throw new NoSuchElementException("Option is empty");
		}
		return value;
	}

	/**
	 * 取值；空返回默认值。
	 *
	 * @param defaultValue 默认值
	 * @return 值或默认值
	 */
	public T getOrElse(T defaultValue) {
		return value != null ? value : defaultValue;
	}

	/**
	 * 取值；空由供应器提供。
	 *
	 * @param supplier 供应器
	 * @return 值或供应值
	 */
	public T getOrElseGet(Supplier<T> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		return value != null ? value : supplier.get();
	}

	/**
	 * 空时取另一个 Option。
	 *
	 * @param other 备选 Option
	 * @return 有值时返回自身，否则返回 other
	 */
	public Option<T> orElse(Option<T> other) {
		Objects.requireNonNull(other, "other must not be null");
		return value != null ? this : other;
	}

	/**
	 * 映射（空短路）。
	 *
	 * @param mapper 映射函数
	 * @param <U>    新泛型
	 * @return 新 Option（mapper 返回 null 视为空）
	 */
	public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		return value == null ? empty() : Option.ofNullable(mapper.apply(value));
	}

	/**
	 * 平铺映射。
	 *
	 * @param mapper 返回 Option 的映射函数
	 * @param <U>    新泛型
	 * @return 新 Option
	 */
	public <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		if (value == null) {
			return empty();
		}
		@SuppressWarnings("unchecked")
		Option<U> next = (Option<U>) mapper.apply(value);
		return next == null ? empty() : next;
	}

	/**
	 * 谓词过滤（不满足转空）。
	 *
	 * @param predicate 谓词
	 * @return 过滤后的 Option
	 */
	public Option<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate, "predicate must not be null");
		if (value == null || predicate.test(value)) {
			return this;
		}
		return empty();
	}

	/**
	 * 有值时消费（链式可用）。
	 *
	 * @param consumer 消费者
	 * @return this
	 */
	public Option<T> peek(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (value != null) {
			consumer.accept(value);
		}
		return this;
	}

	/**
	 * 空时执行（链式可用）。
	 *
	 * @param runnable 空时动作
	 * @return this
	 */
	public Option<T> onEmpty(Runnable runnable) {
		Objects.requireNonNull(runnable, "runnable must not be null");
		if (value == null) {
			runnable.run();
		}
		return this;
	}

	/**
	 * 有值时消费（终止式）。
	 *
	 * @param consumer 消费者
	 */
	public void ifPresent(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (value != null) {
			consumer.accept(value);
		}
	}

	/**
	 * 转 JDK Optional。
	 *
	 * @return Optional
	 */
	public Optional<T> toOptional() {
		return Optional.ofNullable(value);
	}

	private static final class EmptyHolder {
		private static final Option<Object> EMPTY = new Option<>(null);
	}
}
