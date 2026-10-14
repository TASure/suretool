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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 集合流式处理工具，参考 Hutool {@code CollStreamUtil} 设计。
 *
 * <p>把高频的 Stream 样板（分组 / 聚合 / 去重 / 拼接）收敛为一行调用；
 * 默认返回新结构且保持输入顺序（LinkedHashMap / List），不修改入参集合。</p>
 *
 * @author suretool
 * @since 1.18.0
 */
public class CollStreamUtil {

	private CollStreamUtil() {
	}

	private static <T> T requireNonNullArg(T value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	/**
	 * 按键分组：{@code Map<键, List<元素>>}，保持首次出现顺序。
	 *
	 * @param data      数据集合，null 返回空 Map
	 * @param keyMapper 键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>       元素类型
	 * @param <K>       键类型
	 * @return 分组结果（LinkedHashMap）
	 */
	public static <T, K> Map<K, List<T>> groupBy(Collection<T> data, Function<T, K> keyMapper) {
		return groupBy(data, keyMapper, LinkedHashMap::new);
	}

	/**
	 * 按键分组到自定义容器。
	 *
	 * @param data      数据集合，null 返回空容器（{@code mapSupplier.get()}）
	 * @param keyMapper 键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param mapSupplier 结果容器工厂，null 抛 {@link IllegalArgumentException}
	 * @param <T>       元素类型
	 * @param <K>       键类型
	 * @param <M>       结果 Map 类型
	 * @return 分组结果
	 */
	public static <T, K, M extends Map<K, List<T>>> M groupBy(Collection<T> data,
			Function<T, K> keyMapper, Supplier<M> mapSupplier) {
		requireNonNullArg(keyMapper, "keyMapper must not be null");
		requireNonNullArg(mapSupplier, "mapSupplier must not be null");
		M result = mapSupplier.get();
		if (data == null || data.isEmpty()) {
			return result;
		}
		for (T item : data) {
			K key = keyMapper.apply(item);
			result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
		}
		return result;
	}

	/**
	 * 按键聚合值：{@code Map<键, 值>}，同键后者覆盖前者。
	 *
	 * @param data        数据集合，null 返回空 Map
	 * @param keyMapper   键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param valueMapper 值提取函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>         元素类型
	 * @param <K>         键类型
	 * @param <V>         值类型
	 * @return 键值映射（LinkedHashMap 保序）
	 */
	public static <T, K, V> Map<K, V> groupByKeyValue(Collection<T> data, Function<T, K> keyMapper,
			Function<T, V> valueMapper) {
		requireNonNullArg(keyMapper, "keyMapper must not be null");
		requireNonNullArg(valueMapper, "valueMapper must not be null");
		Map<K, V> result = new LinkedHashMap<>();
		if (data == null) {
			return result;
		}
		for (T item : data) {
			result.put(keyMapper.apply(item), valueMapper.apply(item));
		}
		return result;
	}

	/**
	 * 转为唯一键 Map（Guava {@code uniqueIndex} 语义）：重复键抛
	 * {@link IllegalArgumentException}。
	 *
	 * @param data        数据集合，null 返回空 Map
	 * @param keyMapper   键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param valueMapper 值提取函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>         元素类型
	 * @param <K>         键类型
	 * @param <V>         值类型
	 * @return 键值映射
	 * @throws IllegalArgumentException 存在重复键
	 */
	public static <T, K, V> Map<K, V> toMap(Collection<T> data, Function<T, K> keyMapper,
			Function<T, V> valueMapper) {
		return toMap(data, keyMapper, valueMapper,
				(k, v) -> {
					throw new IllegalArgumentException("重复键: " + k);
				});
	}

	/**
	 * 转为 Map，冲突时用合并函数处理。
	 *
	 * @param data          数据集合，null 返回空 Map
	 * @param keyMapper     键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param valueMapper   值提取函数，null 抛 {@link IllegalArgumentException}
	 * @param mergeFunction 冲突合并函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>           元素类型
	 * @param <K>           键类型
	 * @param <V>           值类型
	 * @return 键值映射
	 */
	public static <T, K, V> Map<K, V> toMap(Collection<T> data, Function<T, K> keyMapper,
			Function<T, V> valueMapper, BinaryOperator<V> mergeFunction) {
		requireNonNullArg(keyMapper, "keyMapper must not be null");
		requireNonNullArg(valueMapper, "valueMapper must not be null");
		requireNonNullArg(mergeFunction, "mergeFunction must not be null");
		Map<K, V> result = new LinkedHashMap<>();
		if (data == null) {
			return result;
		}
		for (T item : data) {
			K key = keyMapper.apply(item);
			V value = valueMapper.apply(item);
			if (result.containsKey(key)) {
				value = mergeFunction.apply(result.get(key), value);
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * 数值统计：一次性汇总 count / sum / min / max / average。
	 *
	 * @param data      数据集合，null 或空返回全零统计
	 * @param valueExtractor 取值函数（Long），null 抛 {@link IllegalArgumentException}
	 * @param <T>       元素类型
	 * @return {@link LongSummaryStatistics}
	 */
	public static <T> LongSummaryStatistics summaryStatistics(Collection<T> data,
			Function<T, Long> valueExtractor) {
		requireNonNullArg(valueExtractor, "valueExtractor must not be null");
		LongSummaryStatistics stats = new LongSummaryStatistics();
		if (data == null) {
			return stats;
		}
		for (T item : data) {
			Long value = valueExtractor.apply(item);
			if (value != null) {
				stats.accept(value);
			}
		}
		return stats;
	}

	/**
	 * 过滤出新 List（保持顺序）。
	 *
	 * @param data      数据集合，null 返回空 List
	 * @param predicate 过滤条件，null 抛 {@link IllegalArgumentException}
	 * @param <T>       元素类型
	 * @return 过滤结果
	 */
	public static <T> List<T> filter(Collection<T> data, Predicate<T> predicate) {
		requireNonNullArg(predicate, "predicate must not be null");
		List<T> result = new ArrayList<>();
		if (data == null) {
			return result;
		}
		for (T item : data) {
			if (predicate.test(item)) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 按提取键去重（首现保留），返回新 List 保持顺序。
	 *
	 * @param data         数据集合，null 返回空 List
	 * @param keyExtractor 去重键提取函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>          元素类型
	 * @param <K>          键类型
	 * @return 去重结果
	 */
	public static <T, K> List<T> distinct(Collection<T> data, Function<T, K> keyExtractor) {
		requireNonNullArg(keyExtractor, "keyExtractor must not be null");
		List<T> result = new ArrayList<>();
		if (data == null) {
			return result;
		}
		Set<K> seen = new LinkedHashSet<>();
		for (T item : data) {
			if (seen.add(keyExtractor.apply(item))) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 拼接元素：按提取函数取值后以分隔符连接，null 值跳过。
	 *
	 * @param data      数据集合，null 返回空串
	 * @param extractor 取值函数（null 值跳过），null 抛 {@link IllegalArgumentException}
	 * @param separator 分隔符，null 视为空串
	 * @param <T>       元素类型
	 * @return 拼接结果
	 */
	public static <T> String join(Collection<T> data, Function<T, ?> extractor, String separator) {
		requireNonNullArg(extractor, "extractor must not be null");
		if (data == null) {
			return "";
		}
		String sep = separator == null ? "" : separator;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (T item : data) {
			Object value = extractor.apply(item);
			if (value == null) {
				continue;
			}
			if (!first) {
				sb.append(sep);
			}
			sb.append(value);
			first = false;
		}
		return sb.toString();
	}

	/**
	 * 映射出新 List（保持顺序，null 映射结果跳过）。
	 *
	 * @param data    数据集合，null 返回空 List
	 * @param mapper  映射函数，null 抛 {@link IllegalArgumentException}
	 * @param <T>     元素类型
	 * @param <R>     结果类型
	 * @return 映射结果
	 */
	public static <T, R> List<R> map(Collection<T> data, Function<T, R> mapper) {
		requireNonNullArg(mapper, "mapper must not be null");
		List<R> result = new ArrayList<>();
		if (data == null) {
			return result;
		}
		for (T item : data) {
			R value = mapper.apply(item);
			if (value != null) {
				result.add(value);
			}
		}
		return result;
	}
}
