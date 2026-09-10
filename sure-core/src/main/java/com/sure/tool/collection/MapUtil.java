package com.sure.tool.collection;

import com.sure.tool.util.ConvertUtil;

import java.util.Collections;
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
	 * @param map               Map
	 * @param delimiter         条目分隔符
	 * @param keyValueSeparator key/value 分隔符
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
}
