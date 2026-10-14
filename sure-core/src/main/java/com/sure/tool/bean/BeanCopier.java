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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.sure.tool.util.ConvertUtil;
import com.sure.tool.util.ReflectUtil;

/**
 * 属性拷贝门面：在 {@link BeanUtil} 之上提供选项化与链式 API。
 *
 * <p>与 {@link BeanUtil#copyProperties} 的差异：
 * <ul>
 *   <li>目标属性只读默认抛 {@link CopyException}（选项 {@link CopyOption#IGNORE_READONLY} 跳过）；</li>
 *   <li>支持 {@link CopyOption#DEEP} 深拷贝（嵌套 Bean / 集合 / 数组递归）；</li>
 *   <li>链式 {@link #of(Object)} 支持字段白名单/黑名单；</li>
 *   <li>类型转换失败抛带属性名的 {@link CopyException}（复用 {@link ConvertUtil}）。</li>
 * </ul>
 *
 * @since 1.18.0
 */
public final class BeanCopier {

	private BeanCopier() {
	}

	/**
	 * 拷贝为新目标对象（默认浅拷贝）。
	 *
	 * @param source      源对象
	 * @param targetClass 目标类型（需有无参构造器）
	 * @param <T>         目标类型
	 * @return 新目标对象
	 * @throws CopyException 目标实例化失败、属性只读或类型转换失败
	 */
	public static <T> T copy(Object source, Class<T> targetClass) {
		return copy(source, targetClass, new CopyOption[0]);
	}

	/**
	 * 拷贝为新目标对象。
	 *
	 * @param source      源对象
	 * @param targetClass 目标类型（需有无参构造器）
	 * @param options     拷贝选项
	 * @param <T>         目标类型
	 * @return 新目标对象
	 * @throws CopyException 目标实例化失败、属性只读或类型转换失败
	 */
	public static <T> T copy(Object source, Class<T> targetClass, CopyOption... options) {
		requireNonNullArg(source, "source must not be null");
		requireNonNullArg(targetClass, "targetClass must not be null");
		T target = newInstance(targetClass);
		copyTo(source, target, new CopyConfig(options, null, null));
		return target;
	}

	/**
	 * 拷贝到已有目标对象。
	 *
	 * @param source 源对象
	 * @param target 目标对象
	 * @throws CopyException 属性只读或类型转换失败
	 */
	public static void copy(Object source, Object target) {
		copy(source, target, new CopyOption[0]);
	}

	/**
	 * 拷贝到已有目标对象。
	 *
	 * @param source 源对象
	 * @param target 目标对象
	 * @param options 拷贝选项
	 * @throws CopyException 属性只读或类型转换失败
	 */
	public static void copy(Object source, Object target, CopyOption... options) {
		requireNonNullArg(source, "source must not be null");
		requireNonNullArg(target, "target must not be null");
		copyTo(source, target, new CopyConfig(options, null, null));
	}

	/**
	 * 深拷贝为新目标对象（等价于 {@code copy(source, targetClass, CopyOption.DEEP)}）。
	 *
	 * @param source      源对象
	 * @param targetClass 目标类型
	 * @param <T>         目标类型
	 * @return 深拷贝后的新目标对象
	 */
	public static <T> T deepCopy(Object source, Class<T> targetClass) {
		return copy(source, targetClass, CopyOption.DEEP);
	}

	/**
	 * Map 转 Bean（带类型转换，支持枚举 String 转换）。
	 *
	 * @param map       Map（key 为属性名）
	 * @param beanClass Bean 类型
	 * @param <T>       Bean 类型
	 * @return Bean 实例
	 * @throws CopyException 目标实例化失败或属性转换失败
	 */
	public static <T> T toBean(Map<String, ?> map, Class<T> beanClass) {
		requireNonNullArg(map, "map must not be null");
		requireNonNullArg(beanClass, "beanClass must not be null");
		T bean = newInstance(beanClass);
		BeanDesc desc = BeanUtil.getBeanDesc(beanClass);
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			String name = entry.getKey();
			PropDesc prop = desc.getProp(name);
			if (prop == null || !prop.isWritable()) {
				continue;
			}
			Object value = entry.getValue();
			if (value == null && prop.getType().isPrimitive()) {
				continue;
			}
			Object converted = convert(name, prop.getType(), value, false);
			invokeSetter(prop, bean, converted);
		}
		return bean;
	}

	/**
	 * Bean 转 Map（含 null 值）。
	 *
	 * @param bean Bean 对象
	 * @return Map（key 为属性名）
	 */
	public static Map<String, Object> toMap(Object bean) {
		return BeanUtil.beanToMap(bean);
	}

	/**
	 * 链式拷贝入口。
	 *
	 * @param source 源对象
	 * @return 链式拷贝器
	 */
	public static Copier of(Object source) {
		requireNonNullArg(source, "source must not be null");
		return new Copier(source);
	}

	/**
	 * 链式拷贝器：可组合忽略策略与字段白名单/黑名单。
	 *
	 * @since 1.18.0
	 */
	public static final class Copier {

		private final Object source;
		private boolean ignoreNull;
		private boolean ignoreReadonly;
		private boolean deep;
		private Set<String> include;
		private Set<String> exclude;

		private Copier(Object source) {
			this.source = source;
		}

		/**
		 * 源属性值为 {@code null} 时跳过。
		 *
		 * @return this
		 */
		public Copier ignoreNull() {
			this.ignoreNull = true;
			return this;
		}

		/**
		 * 目标属性只读时跳过（默认抛 {@link CopyException}）。
		 *
		 * @return this
		 */
		public Copier ignoreReadonly() {
			this.ignoreReadonly = true;
			return this;
		}

		/**
		 * 深拷贝（嵌套 Bean / 集合 / 数组递归）。
		 *
		 * @return this
		 */
		public Copier deep() {
			this.deep = true;
			return this;
		}

		/**
		 * 仅拷贝指定字段（白名单）。
		 *
		 * @param fields 字段名
		 * @return this
		 */
		public Copier include(String... fields) {
			requireNonNullArg(fields, "fields must not be null");
			if (exclude != null) {
				throw new IllegalArgumentException("include 与 exclude 不可同时使用");
			}
			include = asSet(fields);
			return this;
		}

		/**
		 * 跳过指定字段（黑名单）。
		 *
		 * @param fields 字段名
		 * @return this
		 */
		public Copier exclude(String... fields) {
			requireNonNullArg(fields, "fields must not be null");
			if (include != null) {
				throw new IllegalArgumentException("include 与 exclude 不可同时使用");
			}
			exclude = asSet(fields);
			return this;
		}

		/**
		 * 拷贝为新目标对象。
		 *
		 * @param targetClass 目标类型
		 * @param <T>         目标类型
		 * @return 新目标对象
		 */
		public <T> T to(Class<T> targetClass) {
			requireNonNullArg(targetClass, "targetClass must not be null");
			T target = newInstance(targetClass);
			copyTo(source, target, config());
			return target;
		}

		/**
		 * 拷贝到已有目标对象。
		 *
		 * @param target 目标对象
		 */
		public void to(Object target) {
			requireNonNullArg(target, "target must not be null");
			copyTo(source, target, config());
		}

		private CopyConfig config() {
			return new CopyConfig(ignoreNull, ignoreReadonly, deep, include, exclude);
		}
	}

	/** 拷贝配置（内部聚合）。 */
	private static final class CopyConfig {

		private final boolean ignoreNull;
		private final boolean ignoreReadonly;
		private final boolean deep;
		private final Set<String> include;
		private final Set<String> exclude;

		private CopyConfig(CopyOption[] options, Set<String> include, Set<String> exclude) {
			boolean ignoreNull = false;
			boolean ignoreReadonly = false;
			boolean deep = false;
			for (CopyOption option : options) {
				if (option == CopyOption.IGNORE_NULL) {
					ignoreNull = true;
				} else if (option == CopyOption.IGNORE_READONLY) {
					ignoreReadonly = true;
				} else if (option == CopyOption.DEEP) {
					deep = true;
				}
			}
			this.ignoreNull = ignoreNull;
			this.ignoreReadonly = ignoreReadonly;
			this.deep = deep;
			this.include = include;
			this.exclude = exclude;
		}

		private CopyConfig(boolean ignoreNull, boolean ignoreReadonly, boolean deep,
				Set<String> include, Set<String> exclude) {
			this.ignoreNull = ignoreNull;
			this.ignoreReadonly = ignoreReadonly;
			this.deep = deep;
			this.include = include;
			this.exclude = exclude;
		}
	}

	private static void copyTo(Object source, Object target, CopyConfig cfg) {
		BeanDesc sourceDesc = BeanUtil.getBeanDesc(source.getClass());
		BeanDesc targetDesc = BeanUtil.getBeanDesc(target.getClass());
		for (PropDesc sourceProp : sourceDesc.getProps()) {
			String name = sourceProp.getName();
			if (sourceProp.getGetter() == null) {
				continue;
			}
			if (cfg.include != null && !cfg.include.contains(name)) {
				continue;
			}
			if (cfg.exclude != null && cfg.exclude.contains(name)) {
				continue;
			}
			Object value = invokeGetter(sourceProp, source);
			if (cfg.ignoreNull && value == null) {
				continue;
			}
			PropDesc targetProp = targetDesc.getProp(name);
			if (targetProp == null) {
				continue;
			}
			if (!targetProp.isWritable()) {
				if (cfg.ignoreReadonly) {
					continue;
				}
				throw new CopyException("目标属性只读，无法拷贝: " + name);
			}
			Object converted = convert(name, targetProp.getType(), value, cfg.deep);
			if (converted == null && targetProp.getType().isPrimitive()) {
				// 基本类型目标属性无法赋 null，保持目标原值（与 BeanUtil 一致）
				continue;
			}
			invokeSetter(targetProp, target, converted);
		}
	}

	private static Object convert(String name, Class<?> targetType, Object value, boolean deep) {
		Object result = value;
		if (deep) {
			result = deepValue(value);
		}
		if (result != null && targetType.isInstance(result)) {
			return result;
		}
		if (targetType.isEnum()) {
			if (result == null) {
				return null;
			}
			if (result instanceof String) {
				return enumValue(targetType, name, (String) result);
			}
			if (targetType.isInstance(result)) {
				return result;
			}
		}
		try {
			return ConvertUtil.convert(targetType, result);
		} catch (Exception e) {
			throw new CopyException("属性类型转换失败: " + name + " → " + targetType.getSimpleName(), e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object enumValue(Class<?> enumType, String name, String value) {
		try {
			return Enum.valueOf((Class<? extends Enum>) enumType, value);
		} catch (IllegalArgumentException e) {
			throw new CopyException("属性类型转换失败: " + name + " → " + enumType.getSimpleName()
					+ "（无法解析枚举值: " + value + "）", e);
		}
	}

	private static <T> T newInstance(Class<T> targetClass) {
		try {
			T target = ReflectUtil.invokeConstructor(targetClass);
			if (target == null) {
				throw new CopyException("无法实例化目标类型（缺少可访问的无参构造器）: " + targetClass.getName());
			}
			return target;
		} catch (CopyException e) {
			throw e;
		} catch (Exception e) {
			throw new CopyException("无法实例化目标类型: " + targetClass.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Object deepValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Map) {
			Map<Object, Object> source = (Map<Object, Object>) value;
			Map<Object, Object> result = new LinkedHashMap<>(source.size());
			for (Map.Entry<Object, Object> entry : source.entrySet()) {
				result.put(entry.getKey(), deepValue(entry.getValue()));
			}
			return result;
		}
		if (value instanceof Collection) {
			Collection<Object> source = (Collection<Object>) value;
			Collection<Object> result;
			if (source instanceof Set) {
				result = new LinkedHashSet<>(source.size());
			} else {
				result = new ArrayList<>(source.size());
			}
			for (Object item : source) {
				result.add(deepValue(item));
			}
			return result;
		}
		if (value.getClass().isArray()) {
			Object[] array = (Object[]) value;
			Object result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length);
			for (int i = 0; i < array.length; i++) {
				java.lang.reflect.Array.set(result, i, deepValue(array[i]));
			}
			return result;
		}
		if (BeanUtil.isBean(value.getClass())) {
			return copy(value, value.getClass(), CopyOption.DEEP);
		}
		return value;
	}

	private static Object invokeGetter(PropDesc prop, Object bean) {
		try {
			return prop.getGetter().invoke(bean);
		} catch (Exception e) {
			throw new CopyException("读取属性失败: " + prop.getName(), e);
		}
	}

	private static void invokeSetter(PropDesc prop, Object bean, Object value) {
		Method setter = prop.getSetter();
		if (setter == null) {
			if (prop.getField() == null) {
				throw new CopyException("目标属性不可写: " + prop.getName());
			}
			try {
				prop.getField().setAccessible(true);
				prop.getField().set(bean, value);
				return;
			} catch (Exception e) {
				throw new CopyException("写入属性失败: " + prop.getName(), e);
			}
		}
		try {
			setter.invoke(bean, new Object[] { value });
		} catch (Exception e) {
			throw new CopyException("写入属性失败: " + prop.getName(), e);
		}
	}

	private static Set<String> asSet(String... fields) {
		Set<String> result = new LinkedHashSet<>(fields.length);
		for (String field : fields) {
			if (field == null || field.isEmpty()) {
				throw new IllegalArgumentException("字段名不能为 null 或空");
			}
			result.add(field);
		}
		return result;
	}

	private static <T> T requireNonNullArg(T value, String message) {
		if (value == null) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}
}
