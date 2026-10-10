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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 集合二分结果：按谓词将集合分为「匹配」与「不匹配」两部分，均保持输入顺序。
 * 构造时对列表做防御性拷贝，访问器返回不可变视图，杜绝内部表示暴露。
 *
 * @param <T> 元素类型
 * @param matched 匹配谓词的元素列表（可为空列表，不为 {@code null}）
 * @param unmatched 不匹配谓词的元素列表（可为空列表，不为 {@code null}）
 * @since 1.15.0
 */
public record Partition<T>(List<T> matched, List<T> unmatched) {

	/**
	 * 紧凑构造：参数为 {@code null} 时按空列表处理，非空时做防御性拷贝。
	 */
	public Partition {
		matched = matched == null ? new ArrayList<>() : new ArrayList<>(matched);
		unmatched = unmatched == null ? new ArrayList<>() : new ArrayList<>(unmatched);
	}

	/**
	 * 匹配元素列表的不可变视图。
	 *
	 * @return 不可变列表
	 */
	@Override
	public List<T> matched() {
		return Collections.unmodifiableList(matched);
	}

	/**
	 * 不匹配元素列表的不可变视图。
	 *
	 * @return 不可变列表
	 */
	@Override
	public List<T> unmatched() {
		return Collections.unmodifiableList(unmatched);
	}
}
