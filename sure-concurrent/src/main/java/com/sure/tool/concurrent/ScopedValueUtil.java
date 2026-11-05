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
package com.sure.tool.concurrent;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@link ScopedValue} 便捷封装：一行式绑定执行、缺省值读取与绑定状态判断。
 *
 * <p>Scoped Values（JDK 25 正式 API）是不可变、线程级作用域的隐式参数：绑定只在
 * {@code where} 执行期间可见，退出后自动恢复；不会像 ThreadLocal 那样泄漏到线程池后续任务。
 *
 * <p>示例：
 * <pre>{@code
 * ScopedValue<String> REQUEST_ID = ScopedValueUtil.newKey();
 * String result = ScopedValueUtil.where(REQUEST_ID, "req-1", () -> REQUEST_ID.get());
 * // result = "req-1"；在此作用域外 orElse(REQUEST_ID, "none") 返回 "none"
 * }</pre>
 *
 * <p>绑定是线程本地的：在 {@code where} 内部启动的新线程看不到该绑定（
 * 需要跨线程传递时请使用 JDK 结构化并发 {@code StructuredTaskScope}，本项目未封装预览 API）。
 *
 * @since 1.20.0
 */
public final class ScopedValueUtil {

	private ScopedValueUtil() {
	}

	/**
	 * 创建一个新的作用域值键。
	 *
	 * @param <T> 值类型
	 * @return 未绑定的 {@link ScopedValue}
	 */
	public static <T> ScopedValue<T> newKey() {
		return ScopedValue.newInstance();
	}

	/**
	 * 在绑定作用域内执行操作，结束后自动恢复原绑定。
	 *
	 * @param key   作用域值键，不可为 null
	 * @param value 绑定值，可为 null
	 * @param op    作用域内执行的操作，不可为 null
	 * @param <T>   值类型
	 * @throws NullPointerException key 或 op 为 null
	 */
	public static <T> void where(ScopedValue<T> key, T value, Runnable op) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(op, "op must not be null");
		ScopedValue.where(key, value).run(op);
	}

	/**
	 * 在绑定作用域内执行有返回值的操作，结束后自动恢复原绑定。
	 *
	 * @param key   作用域值键，不可为 null
	 * @param value 绑定值，可为 null
	 * @param op    作用域内执行的操作，不可为 null
	 * @param <T>   值类型
	 * @param <R>   返回类型
	 * @return 操作返回值
	 * @throws NullPointerException key 或 op 为 null
	 */
	public static <T, R> R where(ScopedValue<T> key, T value, Supplier<R> op) {
		Objects.requireNonNull(key, "key must not be null");
		Objects.requireNonNull(op, "op must not be null");
		return ScopedValue.where(key, value).call(op::get);
	}

	/**
	 * 读取当前绑定值；未绑定时返回缺省值。
	 *
	 * <p>绑定值本身为 null 时返回 null，不会回退到缺省值（null 是合法绑定值）。
	 *
	 * @param key          作用域值键，不可为 null
	 * @param defaultValue 未绑定时返回的缺省值，可为 null
	 * @param <T>          值类型
	 * @return 当前绑定值或缺省值
	 * @throws NullPointerException key 为 null
	 */
	public static <T> T orElse(ScopedValue<T> key, T defaultValue) {
		Objects.requireNonNull(key, "key must not be null");
		return key.isBound() ? key.get() : defaultValue;
	}

	/**
	 * 判断当前线程上下文是否已绑定该键。
	 *
	 * @param key 作用域值键，不可为 null
	 * @param <T> 值类型
	 * @return 已绑定返回 true
	 * @throws NullPointerException key 为 null
	 */
	public static <T> boolean isBound(ScopedValue<T> key) {
		Objects.requireNonNull(key, "key must not be null");
		return key.isBound();
	}
}
