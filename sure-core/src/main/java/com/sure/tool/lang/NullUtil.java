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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.sure.tool.util.ArrayUtil;
import com.sure.tool.util.ObjectUtil;

/**
 * 统一 null 门面：判断 / 取值 / 安全调用 / 展示。
 *
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 判断
 * NullUtil.isNull(x);                 // x == null
 * NullUtil.isAnyNull(a, b, c);        // 任一为 null
 * // 取值
 * String v = NullUtil.firstNonNull(a, b, "default");   // 首个非 null
 * String w = NullUtil.defaultIfNull(x, () -> "fallback");
 * // 安全调用（null 输入/结果转空；映射内部异常正常传播）
 * Option<String> name = NullUtil.applyIfNotNull(user, User::getName);
 * // 展示
 * String s = NullUtil.nullSafeToString(obj);           // null -> ""
 * }</pre>
 *
 * <p><b>null 语义：</b>除 {@link #applyIfNotNull} 外所有方法均安全接受 null
 * 入参且不抛 NPE；返回 Option 的方法将 null 输入/中间结果视为空。</p>
 *
 * @since 1.12.0
 */
public final class NullUtil {

	private NullUtil() {
	}

	/**
	 * 是否为 null。
	 *
	 * @param obj 对象
	 * @return true=null
	 */
	public static boolean isNull(Object obj) {
		return obj == null;
	}

	/**
	 * 是否非 null。
	 *
	 * @param obj 对象
	 * @return true=非 null
	 */
	public static boolean isNonNull(Object obj) {
		return obj != null;
	}

	/**
	 * 是否任一为 null。
	 *
	 * @param values 值列表
	 * @return true=至少一个为 null
	 */
	public static boolean isAnyNull(Object... values) {
		if (values == null) {
			return true;
		}
		for (Object value : values) {
			if (value == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否全部为 null。
	 *
	 * @param values 值列表
	 * @return true=全部为 null（空数组/空入参视为 true）
	 */
	public static boolean isAllNull(Object... values) {
		if (values == null || values.length == 0) {
			return true;
		}
		for (Object value : values) {
			if (value != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 取首个非 null 值（委托 {@link ArrayUtil#firstNonNull}）。
	 *
	 * @param values 值列表
	 * @param <T>    泛型
	 * @return 首个非 null 值；全 null 返回 null
	 */
	@SafeVarargs
	public static <T> T firstNonNull(T... values) {
		return ArrayUtil.firstNonNull(values);
	}

	/**
	 * 取最后一个非 null 值。
	 *
	 * @param values 值列表
	 * @param <T>    泛型
	 * @return 最后一个非 null 值；全 null 返回 null
	 */
	@SafeVarargs
	public static <T> T lastNonNull(T... values) {
		if (values == null) {
			return null;
		}
		for (int i = values.length - 1; i >= 0; i--) {
			if (values[i] != null) {
				return values[i];
			}
		}
		return null;
	}

	/**
	 * SQL COALESCE 语义：取首个非 null 值（{@link #firstNonNull} 别名）。
	 *
	 * @param values 值列表
	 * @param <T>    泛型
	 * @return 首个非 null 值；全 null 返回 null
	 */
	@SafeVarargs
	public static <T> T coalesce(T... values) {
		return firstNonNull(values);
	}

	/**
	 * null 时返回默认值（委托 {@link ObjectUtil#defaultIfNull}）。
	 *
	 * @param obj          对象
	 * @param defaultValue 默认值
	 * @param <T>          泛型
	 * @return 非 null 返回对象本身，否则默认值
	 */
	public static <T> T defaultIfNull(T obj, T defaultValue) {
		return ObjectUtil.defaultIfNull(obj, defaultValue);
	}

	/**
	 * null 时由供应器提供默认值（延迟求值）。
	 *
	 * @param obj          对象
	 * @param defaultValue 默认值供应器
	 * @param <T>          泛型
	 * @return 非 null 返回对象本身，否则供应值
	 */
	public static <T> T defaultIfNull(T obj, Supplier<T> defaultValue) {
		Objects.requireNonNull(defaultValue, "defaultValue must not be null");
		return obj != null ? obj : defaultValue.get();
	}

	/**
	 * 值非 null 时安全应用映射，返回 {@link Option}（仅短路显式 null；
	 * 映射函数内部异常——含 NPE——正常传播，便于定位调用方 bug）。
	 *
	 * @param value  值（可为 null）
	 * @param mapper 映射函数（返回 null 视为空）
	 * @param <T>    输入泛型
	 * @param <U>    输出泛型
	 * @return Option：null 输入/映射结果为 null 时转空，否则为映射值
	 */
	public static <T, U> Option<U> applyIfNotNull(T value, Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		if (value == null) {
			return Option.empty();
		}
		return Option.ofNullable(mapper.apply(value));
	}

	/**
	 * 值非 null 时消费（空入参静默忽略）。
	 *
	 * @param value    值（可为 null）
	 * @param consumer 消费者
	 * @param <T>      泛型
	 */
	public static <T> void consumeIfNotNull(T value, Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer, "consumer must not be null");
		if (value != null) {
			consumer.accept(value);
		}
	}

	/**
	 * null 安全转字符串（null 返回空串，不抛 NPE）。
	 *
	 * @param obj 对象（可为 null）
	 * @return null 时为 ""，否则 {@code String.valueOf(obj)}
	 */
	public static String nullSafeToString(Object obj) {
		return obj == null ? "" : String.valueOf(obj);
	}
}
