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
package com.sure.tool.fluent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 链式 Map 构建与变换（Fluent API 全面化第三项）。
 *
 * <p>与 {@code MapUtil} 静态方法形态并存：支持「构建 → 变换 → 终端」一条链完成。
 * 中间操作返回新的 {@code FluentMap}（内部 Map 不可变，插入顺序保留，原 Map 不受影响）；
 * 终端 {@link #toMap()} 等返回不可变结果（对标 Guava ImmutableMap 理念，零依赖实现）。
 * 键与值均不允许为 null：任何途径引入 null 都会抛 {@link IllegalArgumentException}。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * Map<String, Integer> m = Fluent.<String, Integer>map()
 *         .put("a", 1).put("b", 2).filterValues(v -> v > 1).toMap();
 * // {"b": 2}，不可变、保插入顺序
 * }</pre>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @since 1.19.0
 */
public final class FluentMap<K, V> {

	/** 内部不可变、保插入顺序的 Map（键值均非 null）。 */
	private final Map<K, V> map;

	FluentMap(Map<K, V> map) {
		this.map = map;
	}

	/**
	 * 创建空 Map 管道。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentMap<String, Integer> m = Fluent.map();
	 * }</pre>
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 空管道实例
	 */
	public static <K, V> FluentMap<K, V> of() {
		return new FluentMap<>(Collections.emptyMap());
	}

	/**
	 * 创建含单个键值对的 Map 管道。
	 *
	 * @param key   键，不能为 null
	 * @param value 值，不能为 null
	 * @param <K>   键类型
	 * @param <V>   值类型
	 * @return 管道实例
	 * @throws IllegalArgumentException key 或 value 为 null
	 */
	public static <K, V> FluentMap<K, V> of(K key, V value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key and value must not be null");
		}
		Map<K, V> one = new LinkedHashMap<>(1);
		one.put(key, value);
		return new FluentMap<>(Collections.unmodifiableMap(one));
	}

	/**
	 * 从既有 Map 创建管道（防御拷贝，插入顺序保留，原 Map 修改不影响管道）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * FluentMap<String, Integer> m = FluentMap.from(existingMap);
	 * }</pre>
	 *
	 * @param source 源 Map，不能为 null，键值均不能为 null
	 * @param <K>    键类型
	 * @param <V>    值类型
	 * @return 管道实例（内部不可变副本）
	 * @throws NullPointerException      source 为 null
	 * @throws IllegalArgumentException source 含 null 键或 null 值
	 */
	public static <K, V> FluentMap<K, V> from(Map<? extends K, ? extends V> source) {
		Objects.requireNonNull(source, "source must not be null");
		Map<K, V> copy = new LinkedHashMap<>(source.size());
		for (Map.Entry<? extends K, ? extends V> e : source.entrySet()) {
			if (e.getKey() == null || e.getValue() == null) {
				throw new IllegalArgumentException("map key and value must not be null");
			}
			copy.put(e.getKey(), e.getValue());
		}
		return new FluentMap<>(Collections.unmodifiableMap(copy));
	}

	/**
	 * 插入或覆盖键值对。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Integer> m = Fluent.map()
	 *         .put("a", 1).put("b", 2).toMap();
	 * }</pre>
	 *
	 * @param key   键，不能为 null
	 * @param value 值，不能为 null
	 * @return 新的管道实例
	 * @throws IllegalArgumentException key 或 value 为 null
	 */
	public FluentMap<K, V> put(K key, V value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key and value must not be null");
		}
		Map<K, V> next = new LinkedHashMap<>(map);
		next.put(key, value);
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 键不存在时插入（已存在则保持不变）。
	 *
	 * @param key   键，不能为 null
	 * @param value 值，不能为 null
	 * @return 新的管道实例
	 * @throws IllegalArgumentException key 或 value 为 null
	 */
	public FluentMap<K, V> putIfAbsent(K key, V value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key and value must not be null");
		}
		Map<K, V> next = new LinkedHashMap<>(map);
		next.putIfAbsent(key, value);
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 移除指定键（不存在则原样返回）。
	 *
	 * @param key 键，可为 null（恒无操作）
	 * @return 新的管道实例
	 */
	public FluentMap<K, V> remove(K key) {
		if (key == null) {
			return this;
		}
		if (!map.containsKey(key)) {
			return this;
		}
		Map<K, V> next = new LinkedHashMap<>(map);
		next.remove(key);
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 值映射（键保持不变）。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, String> m = Fluent.<String, Integer>map()
	 *         .put("a", 1).mapValues(v -> "v" + v).toMap(); // {"a": "v1"}
	 * }</pre>
	 *
	 * @param mapper 映射函数，不能为 null；结果不能为 null
	 * @param <V2>   目标值类型
	 * @return 新的管道实例
	 * @throws NullPointerException      mapper 为 null
	 * @throws IllegalArgumentException 映射结果含 null
	 */
	public <V2> FluentMap<K, V2> mapValues(Function<? super V, ? extends V2> mapper) {
		Objects.requireNonNull(mapper, "mapper must not be null");
		Map<K, V2> next = new LinkedHashMap<>(map.size());
		for (Map.Entry<K, V> e : map.entrySet()) {
			V2 r = mapper.apply(e.getValue());
			if (r == null) {
				throw new IllegalArgumentException("mapValues result must not be null");
			}
			next.put(e.getKey(), r);
		}
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 按键过滤。
	 *
	 * @param predicate 过滤条件，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException predicate 为 null
	 */
	public FluentMap<K, V> filterKeys(Predicate<? super K> predicate) {
		Objects.requireNonNull(predicate, "predicate must not be null");
		Map<K, V> next = new LinkedHashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (predicate.test(e.getKey())) {
				next.put(e.getKey(), e.getValue());
			}
		}
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 按值过滤。
	 *
	 * <p>示例：</p>
	 * <pre>{@code
	 * Map<String, Integer> m = Fluent.<String, Integer>map()
	 *         .put("a", 1).put("b", 2).filterValues(v -> v > 1).toMap();
	 * // {"b": 2}
	 * }</pre>
	 *
	 * @param predicate 过滤条件，不能为 null
	 * @return 新的管道实例
	 * @throws NullPointerException predicate 为 null
	 */
	public FluentMap<K, V> filterValues(Predicate<? super V> predicate) {
		Objects.requireNonNull(predicate, "predicate must not be null");
		Map<K, V> next = new LinkedHashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (predicate.test(e.getValue())) {
				next.put(e.getKey(), e.getValue());
			}
		}
		return new FluentMap<>(Collections.unmodifiableMap(next));
	}

	/**
	 * 终端：返回不可变 Map（保插入顺序）。
	 *
	 * <p>返回结果不可修改，且为独立副本，与管道内部互不影响。</p>
	 *
	 * @return 不可变 Map
	 */
	public Map<K, V> toMap() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(map));
	}

	/**
	 * 终端：按键取值（键不存在或为 null 返回 null）。
	 *
	 * @param key 键，可为 null
	 * @return 对应值或 null
	 */
	public V get(K key) {
		return map.get(key);
	}

	/**
	 * 终端：键值对数量。
	 *
	 * @return 数量
	 */
	public int size() {
		return map.size();
	}

	/**
	 * 终端：是否为空。
	 *
	 * @return 空返回 true
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * 终端：是否包含指定键（null 返回 false）。
	 *
	 * @param key 键，可为 null
	 * @return 包含返回 true
	 */
	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	/**
	 * 终端：返回不可变键集合（保插入顺序）。
	 *
	 * @return 不可变 Set
	 */
	public Set<K> keys() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(map.keySet()));
	}

	/**
	 * 终端：返回不可变值列表（保插入顺序）。
	 *
	 * @return 不可变 List
	 */
	public List<V> values() {
		return List.copyOf(map.values());
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
