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
package com.sure.tool.collection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 两个 Map 的差异分析结果。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @param onlyLeft 只在左 Map 出现的键值对
 * @param onlyRight 只在右 Map 出现的键值对
 * @param valueDiffers 左右共有但值不同的键值对（值为左 Map 的值）
 * @since 1.16.0
 */
public record Difference<K, V>(Map<K, V> onlyLeft, Map<K, V> onlyRight, Map<K, V> valueDiffers) {

	/**
	 * 紧凑构造：防御性拷贝并返回不可变视图，防止外部修改与内部表示暴露。
	 */
	public Difference {
		onlyLeft = copy(onlyLeft);
		onlyRight = copy(onlyRight);
		valueDiffers = copy(valueDiffers);
	}

	/**
	 * 只在左 Map 出现的键值对（不可变视图）。
	 *
	 * @return 不可变 Map
	 */
	@Override
	public Map<K, V> onlyLeft() {
		return Collections.unmodifiableMap(onlyLeft);
	}

	/**
	 * 只在右 Map 出现的键值对（不可变视图）。
	 *
	 * @return 不可变 Map
	 */
	@Override
	public Map<K, V> onlyRight() {
		return Collections.unmodifiableMap(onlyRight);
	}

	/**
	 * 左右共有但值不同的键值对（值为左 Map 的值，不可变视图）。
	 *
	 * @return 不可变 Map
	 */
	@Override
	public Map<K, V> valueDiffers() {
		return Collections.unmodifiableMap(valueDiffers);
	}

	private static <K, V> Map<K, V> copy(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return Map.of();
		}
		return Collections.unmodifiableMap(new LinkedHashMap<>(map));
	}
}
