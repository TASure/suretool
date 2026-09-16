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
 * 有序三元组，不可变，JDK record 实现。
 *
 * @param <L> 第一个值类型
 * @param <M> 第二个值类型
 * @param <R> 第三个值类型
 * @author suretool
 * @since 0.2.0
 */
public record Triple<L, M, R>(L left, M middle, R right) {

	/**
	 * 静态工厂。
	 *
	 * @param left   第一个值
	 * @param middle 第二个值
	 * @param right  第三个值
	 * @param <L>    第一个值类型
	 * @param <M>    第二个值类型
	 * @param <R>    第三个值类型
	 * @return Triple
	 */
	public static <L, M, R> Triple<L, M, R> of(L left, M middle, R right) {
		return new Triple<>(left, middle, right);
	}

	/**
	 * 取第一个值。
	 *
	 * @return 第一个值
	 */
	public L getLeft() {
		return left;
	}

	/**
	 * 取第二个值。
	 *
	 * @return 第二个值
	 */
	public M getMiddle() {
		return middle;
	}

	/**
	 * 取第三个值。
	 *
	 * @return 第三个值
	 */
	public R getRight() {
		return right;
	}
}
