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

/**
 * 有序二元组，不可变，JDK record 实现。
 *
 * @param <L> 左值类型
 * @param <R> 右值类型
 * @author suretool
 * @since 0.2.0
 */
public record Pair<L, R>(L left, R right) {

	/**
	 * 静态工厂。
	 *
	 * @param left  左值
	 * @param right 右值
	 * @param <L>   左值类型
	 * @param <R>   右值类型
	 * @return Pair
	 */
	public static <L, R> Pair<L, R> of(L left, R right) {
		return new Pair<>(left, right);
	}

	/**
	 * 取左值。
	 *
	 * @return 左值
	 */
	public L getLeft() {
		return left;
	}

	/**
	 * 取右值。
	 *
	 * @return 右值
	 */
	public R getRight() {
		return right;
	}
}
