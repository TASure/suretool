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

import com.sure.tool.util.ConvertUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map 工具类，参考 Hutool 的 {@code MapUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class MapUtil {

	private MapUtil() {
	}

	/**
	 * Map 是否为空。
	 *
	 * @param map Map
	 * @return 是否为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Map 是否非空。
	 *
	 * @param map Map
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * 创建 HashMap。
	 *
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return HashMap
	 */
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<>();
	}

	/**
	 * 创建带初始容量的 HashMap。
	 *
	 * @param size 初始容量
	 * @param <K>  key 类型
	 * @param <V>  value 类型
	 * @return HashMap
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<>(size);
	}

	/**
	 * 创建 LinkedHashMap。
	 *
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return LinkedHashMap
	 */
	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
		return new LinkedHashMap<>();
	}

	/**
	 * 创建 TreeMap。
	 *
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return TreeMap
	 */
	public static <K, V> TreeMap<K, V> newTreeMap() {
		return new TreeMap<>();
	}

	/**
	 * 创建 ConcurrentHashMap。
	 *
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return ConcurrentHashMap
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<>();
	}

	/**
	 * 从成对参数构建 Map，如 {@code of("a", 1, "b", 2)}。
	 *
	 * @param pairs key/value 成对参数
	 * @param <K>   key 类型
	 * @param <V>   value 类型
	 * @return HashMap
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> of(Object... pairs) {
		if (pairs == null || pairs.length % 2 != 0) {
			throw new IllegalArgumentException("参数必须为 key/value 成对出现");
		}
		Map<K, V> map = new HashMap<>(pairs.length / 2);
		for (int i = 0; i < pairs.length; i += 2) {
			map.put((K) pairs[i], (V) pairs[i + 1]);
		}
		return map;
	}

	/**
	 * 获取值，key 不存在时返回默认值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @param <K>          key 类型
	 * @param <V>          value 类型
	 * @return 值或默认值
	 */
	public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
		if (map == null) {
			return defaultValue;
		}
		return map.getOrDefault(key, defaultValue);
	}

	/**
	 * 获取 String 值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return String 值或默认值
	 */
	public static String getStr(Map<?, ?> map, Object key, String defaultValue) {
		Object value = (map == null) ? null : map.get(key);
		return ConvertUtil.toStr(value, defaultValue);
	}

	/**
	 * 获取 int 值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return int 值或默认值
	 */
	public static int getInt(Map<?, ?> map, Object key, int defaultValue) {
		Object value = (map == null) ? null : map.get(key);
		return ConvertUtil.toInt(value, defaultValue);
	}

	/**
	 * 获取 long 值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return long 值或默认值
	 */
	public static long getLong(Map<?, ?> map, Object key, long defaultValue) {
		Object value = (map == null) ? null : map.get(key);
		return ConvertUtil.toLong(value, defaultValue);
	}

	/**
	 * 获取 double 值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return double 值或默认值
	 */
	public static double getDouble(Map<?, ?> map, Object key, double defaultValue) {
		Object value = (map == null) ? null : map.get(key);
		return ConvertUtil.toDouble(value, defaultValue);
	}

	/**
	 * 获取 boolean 值。
	 *
	 * @param map          Map
	 * @param key          key
	 * @param defaultValue 默认值
	 * @return boolean 值或默认值
	 */
	public static boolean getBool(Map<?, ?> map, Object key, boolean defaultValue) {
		Object value = (map == null) ? null : map.get(key);
		return ConvertUtil.toBoolean(value, defaultValue);
	}

	/**
	 * Map 是否包含 key。
	 *
	 * @param map Map
	 * @param key key
	 * @return 是否包含
	 */
	public static boolean containsKey(Map<?, ?> map, Object key) {
		return map != null && map.containsKey(key);
	}

	/**
	 * 空 Map 返回空 Map。
	 *
	 * @param map Map
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return 非空返回原 Map，否则返回空 Map
	 */
	public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
		return (map == null) ? Collections.<K, V>emptyMap() : map;
	}

	/**
	 * key/value 反转（值相同会互相覆盖）。
	 *
	 * @param map 原 Map
	 * @param <K> key 类型
	 * @param <V> value 类型
	 * @return 反转后的 LinkedHashMap
	 */
	public static <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> result = new LinkedHashMap<>();
		if (map == null) {
			return result;
		}
		for (Map.Entry<K, V> entry : map.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		return result;
	}

	/**
	 * 拼接 Map 为字符串，如 {@code a=1,b=2}。
	 *
	 * @param map                Map
	 * @param delimiter          条目分隔符
	 * @param keyValueSeparator  key/value 分隔符
	 * @return 拼接结果
	 */
	public static String join(Map<?, ?> map, CharSequence delimiter, CharSequence keyValueSeparator) {
		if (map == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(entry.getKey()).append(keyValueSeparator).append(entry.getValue());
			first = false;
		}
		return sb.toString();
	}

	/**
	 * 按值排序（值需可比），返回有序 LinkedHashMap。
	 *
	 * @param <K>         键类型
	 * @param <V>         值类型（Comparable）
	 * @param map         原 Map
	 * @param isAscending 是否升序
	 * @return 排序后的新 Map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean isAscending) {
		if (isEmpty(map)) {
			return map;
		}
		List<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
		Comparator<Map.Entry<K, V>> cmp = Comparator.comparing(Map.Entry::getValue);
		if (!isAscending) {
			cmp = cmp.reversed();
		}
		entries.sort(cmp);
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> e : entries) {
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}



	/**
	 * 取 BigDecimal。
	 *
	 * @param map  Map
	 * @param key  键
	 * @return BigDecimal 值；缺失返回 null
	 */
	public static BigDecimal getBigDecimal(Map<?, ?> map, Object key) {
		return getBigDecimal(map, key, null);
	}

	/**
	 * 取 BigDecimal（带默认值）。
	 *
	 * @param map          Map
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return BigDecimal 值
	 */
	public static BigDecimal getBigDecimal(Map<?, ?> map, Object key, BigDecimal defaultValue) {
		Object value = map.get(key);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		return new BigDecimal(String.valueOf(value).trim());
	}



	/**
	 * Map 转 Properties（值经 String.valueOf 转换）。
	 *
	 * @param map Map
	 * @return Properties
	 */
	public static java.util.Properties toProperties(Map<?, ?> map) {
		java.util.Properties properties = new java.util.Properties();
		if (isEmpty(map)) {
			return properties;
		}
		for (Map.Entry<?, ?> e : map.entrySet()) {
			properties.setProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}
		return properties;
	}



	/**
	 * 按 key 排序（key 需可比），返回 LinkedHashMap。
	 *
	 * @param <K>         键类型（Comparable）
	 * @param <V>         值类型
	 * @param map         原 Map
	 * @param isAscending 是否升序
	 * @return 排序后新 Map
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sort(Map<K, V> map, boolean isAscending) {
		if (isEmpty(map)) {
			return map;
		}
		List<K> keys = new ArrayList<>(map.keySet());
		if (isAscending) {
			keys.sort(Comparator.naturalOrder());
		} else {
			keys.sort(Comparator.reverseOrder());
		}
		Map<K, V> result = new LinkedHashMap<>();
		for (K key : keys) {
			result.put(key, map.get(key));
		}
		return result;
	}

	/**
	 * 按条件过滤 Map（不修改原 Map）。
	 *
	 * @param <K>       键类型
	 * @param <V>       值类型
	 * @param map       原 Map
	 * @param predicate 过滤条件
	 * @return 过滤后新 Map
	 */
	public static <K, V> Map<K, V> filter(Map<K, V> map, java.util.function.Predicate<Map.Entry<K, V>> predicate) {
		Map<K, V> result = new LinkedHashMap<>();
		if (isEmpty(map)) {
			return result;
		}
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (predicate.test(e)) {
				result.put(e.getKey(), e.getValue());
			}
		}
		return result;
	}

	/**
	 * 取 Date（支持时间戳与常见日期格式）。
	 *
	 * @param map Map
	 * @param key 键
	 * @return Date 值；缺失或不可解析返回 null
	 */
	public static java.util.Date getDate(Map<?, ?> map, Object key) {
		return ConvertUtil.toDate(map.get(key));
	}



	/**
	 * 取 float（含默认值）。
	 *
	 * @param map          Map
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return float
	 */
	public static float getFloat(Map<?, ?> map, Object key, float defaultValue) {
		return ConvertUtil.toFloat(map.get(key), defaultValue);
	}

	/**
	 * 取 char（含默认值）。
	 *
	 * @param map          Map
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return char
	 */
	public static char getChar(Map<?, ?> map, Object key, char defaultValue) {
		Object value = map.get(key);
		if (value == null) {
			return defaultValue;
		}
		String s = String.valueOf(value);
		return s.isEmpty() ? defaultValue : s.charAt(0);
	}

	/**
	 * 取 byte（含默认值）。
	 *
	 * @param map          Map
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return byte
	 */
	public static byte getByte(Map<?, ?> map, Object key, byte defaultValue) {
		return ConvertUtil.toByte(map.get(key), defaultValue);
	}

	/**
	 * 取 short（含默认值）。
	 *
	 * @param map          Map
	 * @param key          键
	 * @param defaultValue 默认值
	 * @return short
	 */
	public static short getShort(Map<?, ?> map, Object key, short defaultValue) {
		return ConvertUtil.toShort(map.get(key), defaultValue);
	}



	/**
	 * 创建 Map 链式构建器（默认 LinkedHashMap 保持插入顺序）。
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return MapBuilder
	 */
	public static <K, V> MapBuilder<K, V> builder() {
		return new MapBuilder<>();
	}

	/**
	 * Map 链式构建器。
	 *
	 * @param <K> 键类型
	 * @param <V> 值类型
	 */
	public static class MapBuilder<K, V> {
		private final Map<K, V> map;

		public MapBuilder() {
			this.map = new LinkedHashMap<>();
		}

		/**
		 * 添加键值。
		 *
		 * @param key   键
		 * @param value 值
		 * @return this
		 */
		public MapBuilder<K, V> put(K key, V value) {
			this.map.put(key, value);
			return this;
		}

		/**
		 * 构建 Map。
		 *
		 * @return 新建的 Map（拷贝，防外部修改内部状态）
		 */
		public Map<K, V> build() {
			return new LinkedHashMap<>(this.map);
		}
	}



	/**
	 * 键值互换（适用于 BiMap 场景；重复值后者覆盖前者）。
	 *
	 * @param map 原 Map
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 互换后的新 Map
	 */
	public static <K, V> Map<V, K> inverse(Map<K, V> map) {
		if (map == null) {
			return null;
		}
		Map<V, K> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		return result;
	}



	/**
	 * 合并两个 Map（后者覆盖前者同名键）。
	 *
	 * @param map1 Map 一
	 * @param map2 Map 二
	 * @param <K>  键类型
	 * @param <V>  值类型
	 * @return 合并后的 Map（LinkedHashMap）
	 */
	public static <K, V> Map<K, V> merge(Map<K, V> map1, Map<K, V> map2) {
		Map<K, V> result = new LinkedHashMap<>();
		if (map1 != null) {
			result.putAll(map1);
		}
		if (map2 != null) {
			result.putAll(map2);
		}
		return result;
	}



	/**
	 * 按值降序排序（返回 LinkedHashMap 保持顺序）。
	 *
	 * @param map 原 Map
	 * @param <K> 键类型
	 * @param <V> 值类型
	 * @return 排序后的 Map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
		return sortByValue(map, false);
	}


}