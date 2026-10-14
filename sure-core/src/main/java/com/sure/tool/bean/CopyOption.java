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

package com.sure.tool.bean;

/**
 * 属性拷贝行为选项（{@link BeanCopier}）。
 *
 * @since 1.18.0
 */
public enum CopyOption {

	/**
	 * 源属性值为 {@code null} 时跳过，不覆盖目标属性（默认覆盖）。
	 */
	IGNORE_NULL,

	/**
	 * 目标属性只读时跳过（默认抛 {@link CopyException} 提示漏拷）。
	 */
	IGNORE_READONLY,

	/**
	 * 深拷贝：嵌套 Bean / 集合 / 数组递归拷贝为新对象（默认浅拷贝）。
	 */
	DEEP
}
