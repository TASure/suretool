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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link Result} 的 Fluent 组合子门面：批量聚合（全部成功 / 首个成功）。
 * 零依赖，对标 Vavr {@code Validation} 与函数式组合子。
 *
 * @since 1.12.0
 */
public final class Results {

	private Results() {
	}

	/**
	 * 全部成功聚合：任一失败返回携带首个错误信息的失败 Result。
	 *
	 * @param results 结果列表
	 * @param <T>     值泛型
	 * @return Result：成功=按输入顺序的值列表；失败=首个错误
	 */
	@SafeVarargs
	public static <T> Result<List<T>> allOf(Result<T>... results) {
		Objects.requireNonNull(results, "results must not be null");
		List<T> values = new ArrayList<>(results.length);
		for (Result<T> result : results) {
			if (result.isFailure()) {
				return Result.fail(result.getErrorMessage(), result.getCause());
			}
			values.add(result.get());
		}
		return Result.ok(List.copyOf(values));
	}

	/**
	 * 首个成功聚合：任一成功即返回；全部失败携带最后一个错误。
	 *
	 * @param results 结果列表
	 * @param <T>     值泛型
	 * @return Result：成功=首个成功值；失败=最后一个错误
	 */
	@SafeVarargs
	public static <T> Result<T> anyOf(Result<T>... results) {
		Objects.requireNonNull(results, "results must not be null");
		String lastError = null;
		Throwable lastCause = null;
		for (Result<T> result : results) {
			if (result.isSuccess()) {
				return Result.ok(result.get());
			}
			lastError = result.getErrorMessage();
			lastCause = result.getCause();
		}
		return Result.fail(lastError == null ? "all results failed" : lastError, lastCause);
	}

	/**
	 * 列表聚合（allOf 别名）：全部成功聚合，任一失败携带首个错误。
	 *
	 * @param results 结果列表
	 * @param <T>     值泛型
	 * @return Result：成功=按输入顺序的值列表；失败=首个错误
	 */
	public static <T> Result<List<T>> sequence(List<Result<T>> results) {
		Objects.requireNonNull(results, "results must not be null");
		List<T> values = new ArrayList<>(results.size());
		for (Result<T> result : results) {
			if (result.isFailure()) {
				return Result.fail(result.getErrorMessage(), result.getCause());
			}
			values.add(result.get());
		}
		return Result.ok(List.copyOf(values));
	}
}
