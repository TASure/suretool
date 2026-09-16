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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.ReflectUtil;

/**
 * Bean 属性操作工具类：属性拷贝、Bean/Map 互转、属性读写，参考 Hutool 的 {@code BeanUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class BeanUtil {

	private static final Map<Class<?>, BeanDesc> BEAN_DESC_CACHE = new ConcurrentHashMap<>();

	private BeanUtil() {
	}

	/**
	 * 获取 Bean 描述（带缓存）。
	 *
	 * @param clazz Bean 类
	 * @return Bean 描述
	 */
	public static BeanDesc getBeanDesc(Class<?> clazz) {
		BeanDesc desc = BEAN_DESC_CACHE.get(clazz);
		if (desc == null) {
			desc = new BeanDesc(clazz);
			BEAN_DESC_CACHE.put(clazz, desc);
		}
		return desc;
	}

	/**
	 * 拷贝属性（不忽略 null 值）。
	 *
	 * @param source          源对象
	 * @param target          目标对象
	 * @param ignoreProperties 忽略的属性名
	 * @param <T>             目标类型
	 * @return 目标对象
	 */
	public static <T> T copyProperties(Object source, T target, String... ignoreProperties) {
		return copyProperties(source, target, false, ignoreProperties);
	}

	/**
	 * 拷贝属性，支持类型转换（如 String 转 int）。
	 *
	 * @param source           源对象
	 * @param target           目标对象
	 * @param ignoreNullValue  是否忽略值为 {@code null} 的属性
	 * @param ignoreProperties 忽略的属性名
	 * @param <T>              目标类型
	 * @return 目标对象
	 */
	public static <T> T copyProperties(Object source, T target, boolean ignoreNullValue, String... ignoreProperties) {
		if (source == null || target == null) {
			return target;
		}
		BeanDesc sourceDesc = getBeanDesc(source.getClass());
		BeanDesc targetDesc = getBeanDesc(target.getClass());
		for (PropDesc prop : sourceDesc.getProps()) {
			String name = prop.getName();
			if (contains(ignoreProperties, name) || prop.getGetter() == null) {
				continue;
			}
			Object value = invoke(prop.getGetter(), source);
			if (ignoreNullValue && value == null) {
				continue;
			}
			PropDesc targetProp = targetDesc.getProp(name);
			if (targetProp != null && targetProp.isWritable()) {
				setPropValue(targetProp, target, value);
			}
		}
		return target;
	}

	/**
	 * Bean 转 Map（不忽略 null 值）。
	 *
	 * @param bean Bean 对象
	 * @return Map
	 */
	public static Map<String, Object> beanToMap(Object bean) {
		return beanToMap(bean, false);
	}

	/**
	 * Bean 转 Map。
	 *
	 * @param bean             Bean 对象
	 * @param ignoreNullValue  是否忽略值为 {@code null} 的属性
	 * @return Map
	 */
	public static Map<String, Object> beanToMap(Object bean, boolean ignoreNullValue) {
		Map<String, Object> map = new LinkedHashMap<>();
		if (bean == null) {
			return map;
		}
		BeanDesc desc = getBeanDesc(bean.getClass());
		for (PropDesc prop : desc.getProps()) {
			if (prop.getGetter() == null) {
				continue;
			}
			Object value = invoke(prop.getGetter(), bean);
			if (ignoreNullValue && value == null) {
				continue;
			}
			map.put(prop.getName(), value);
		}
		return map;
	}

	/**
	 * Map 转 Bean（自动实例化并写入属性，带类型转换）。
	 *
	 * @param map       Map
	 * @param beanClass Bean 类
	 * @param <T>       Bean 类型
	 * @return Bean 实例
	 */
	public static <T> T mapToBean(Map<String, ? extends Object> map, Class<T> beanClass) {
		if (map == null || beanClass == null) {
			return null;
		}
		T bean = ReflectUtil.invokeConstructor(beanClass);
		if (bean == null) {
			return null;
		}
		BeanDesc desc = getBeanDesc(beanClass);
		for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
			PropDesc prop = desc.getProp(entry.getKey());
			if (prop != null && prop.isWritable()) {
				setPropValue(prop, bean, entry.getValue());
			}
		}
		return bean;
	}

	/**
	 * 读取属性值。
	 *
	 * @param bean Bean 对象
	 * @param name 属性名
	 * @return 属性值
	 */
	public static Object getProperty(Object bean, String name) {
		if (bean == null || name == null) {
			return null;
		}
		PropDesc prop = getBeanDesc(bean.getClass()).getProp(name);
		if (prop != null && prop.getGetter() != null) {
			return invoke(prop.getGetter(), bean);
		}
		if (FieldUtil.getField(bean.getClass(), name) != null) {			return ReflectUtil.getFieldValue(bean, name);
		}
		return null;
	}

	/**
	 * 写入属性值（带类型转换）。
	 *
	 * @param bean  Bean 对象
	 * @param name  属性名
	 * @param value 属性值
	 */
	public static void setProperty(Object bean, String name, Object value) {
		if (bean == null || name == null) {
			return;
		}
		PropDesc prop = getBeanDesc(bean.getClass()).getProp(name);
		if (prop != null && prop.isWritable()) {
			setPropValue(prop, bean, value);
			return;
		}
		if (FieldUtil.getField(bean.getClass(), name) != null) {
			ReflectUtil.setFieldValue(bean, name, value);
		}
	}

	/**
	 * 对象转 Bean：Map 走 {@link #mapToBean}，普通 Bean 走 {@link #copyProperties}，其他类型返回原对象。
	 *
	 * @param source    源对象（Map 或 Bean）
	 * @param beanClass 目标 Bean 类
	 * @param <T>       Bean 类型
	 * @return 转换后的 Bean；源为 {@code null} 返回 {@code null}
	 */
	public static <T> T toBean(Object source, Class<T> beanClass) {
		if (source == null || beanClass == null) {
			return null;
		}
		if (source instanceof Map<?, ?> map) {
			return mapToBean((Map<String, Object>) map, beanClass);
		}
		if (isBean(source.getClass()) || beanClass.isInstance(source)) {
			T target = ReflectUtil.invokeConstructor(beanClass);
			if (target != null) {
				copyProperties(source, target);
				return target;
			}
		}
		return beanClass.isInstance(source) ? beanClass.cast(source) : null;
	}

	/**
	 * 判断是否为可操作 Bean（非基础类型、非容器、非日期等）。
	 *
	 * @param clazz 类
	 * @return 是否 Bean
	 */
	public static boolean isBean(Class<?> clazz) {
		if (clazz == null || clazz.isPrimitive() || clazz.isArray() || clazz.isEnum() || clazz.isInterface()) {
			return false;
		}
		if (clazz == String.class || clazz == Character.class || clazz == Class.class
				|| CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)
				|| Boolean.class == clazz || Date.class.isAssignableFrom(clazz)
				|| java.util.Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			return false;
		}
		return true;
	}

	/**
	 * 写入属性值：优先 setter，带类型转换。
	 *
	 * @param prop  属性描述
	 * @param bean  Bean 对象
	 * @param value 值
	 */
	private static void setPropValue(PropDesc prop, Object bean, Object value) {
		Object converted = ConvertUtil.convert(prop.getType(), value);
		Method setter = prop.getSetter();
		if (setter != null) {
			invoke(setter, bean, converted != null ? converted : value);
			return;
		}
		Field field = prop.getField();
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(bean, converted != null ? converted : value);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("写入字段 " + prop.getName() + " 失败", e);
			}
		}
	}

	private static Object invoke(Method method, Object target, Object... args) {
		try {
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("调用方法 " + method.getName() + " 失败", e);
		}
	}

	private static boolean contains(String[] array, String value) {
		if (array == null || value == null) {
			return false;
		}
		for (String item : array) {
			if (value.equals(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 深拷贝：递归复制 Bean / Map / List / Set / 数组 / 不可变值类型。
	 * <p>
	 * 规则：基本类型、包装类型、String、枚举、Date 直接返回原引用（不可变或无需拷贝）；
	 * 集合与 Map 逐元素深拷贝；数组逐元素深拷贝；其余按 Bean 处理（无参构造 + 属性拷贝）。
	 * <p>
	 * 注意：循环引用不处理（会栈溢出），请勿对存在环的对象图调用。
	 *
	 * @param source 源对象
	 * @param <T>    类型
	 * @return 深拷贝结果，入参为 null 时返回 null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deepCopy(T source) {
		if (source == null) {
			return null;
		}
		Class<?> type = source.getClass();
		if (type.isPrimitive() || type.isEnum() || source instanceof String
				|| source instanceof Number || source instanceof Boolean
				|| source instanceof Character || source instanceof java.util.Date) {
			return source;
		}
		if (source instanceof java.util.List<?> list) {
			java.util.List<Object> copy = new java.util.ArrayList<>(list.size());
			for (Object item : list) {
				copy.add(deepCopy(item));
			}
			return (T) copy;
		}
		if (source instanceof java.util.Set<?> set) {
			java.util.Set<Object> copy = new java.util.LinkedHashSet<>(set.size());
			for (Object item : set) {
				copy.add(deepCopy(item));
			}
			return (T) copy;
		}
		if (source instanceof java.util.Map<?, ?> map) {
			java.util.Map<Object, Object> copy = new java.util.LinkedHashMap<>(map.size());
			for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
				copy.put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
			}
			return (T) copy;
		}
		if (type.isArray()) {
			int len = java.lang.reflect.Array.getLength(source);
			Object copy = java.lang.reflect.Array.newInstance(type.getComponentType(), len);
			if (!type.getComponentType().isPrimitive()) {
				for (int i = 0; i < len; i++) {
					java.lang.reflect.Array.set(copy, i, deepCopy(java.lang.reflect.Array.get(source, i)));
				}
			} else {
				System.arraycopy(source, 0, copy, 0, len);
			}
			return (T) copy;
		}
		// Bean：无参构造 + 属性拷贝
		T target;
		try {
			target = (T) type.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("deepCopy 需要无参构造: " + type.getName(), e);
		}
		copyProperties(source, target);
		return target;
	}


	/**
	 * 用 Map 填充 Bean 属性（值为 {@code null} 的键跳过，不覆盖已有值）。
	 *
	 * @param bean Bean
	 * @param map  属性名-值
	 * @param <T>  Bean 类型
	 * @return Bean 本身
	 */
	public static <T> T fill(T bean, Map<String, Object> map) {
		if (bean == null || map == null) {
			return bean;
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null) {
				setProperty(bean, entry.getKey(), entry.getValue());
			}
		}
		return bean;
	}


}