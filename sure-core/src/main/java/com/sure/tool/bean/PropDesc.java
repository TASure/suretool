package com.sure.tool.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Bean 属性描述，包含属性名、类型、getter/setter 与字段，参考 Hutool 的 {@code PropDesc} 设计。
 *
 * @author suretool
 */
public class PropDesc {

	private final String name;
	private final Class<?> type;
	private final Method getter;
	private final Method setter;
	private final Field field;

	/**
	 * 创建属性描述。
	 *
	 * @param name   属性名
	 * @param type   属性类型
	 * @param getter getter 方法（可能为 {@code null}）
	 * @param setter setter 方法（可能为 {@code null}）
	 * @param field  字段（可能为 {@code null}）
	 */
	public PropDesc(String name, Class<?> type, Method getter, Method setter, Field field) {
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
		this.field = field;
	}

	/**
	 * 属性名。
	 *
	 * @return 属性名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 属性类型。
	 *
	 * @return 属性类型
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * getter 方法。
	 *
	 * @return getter 方法，可能为 {@code null}
	 */
	public Method getGetter() {
		return getter;
	}

	/**
	 * setter 方法。
	 *
	 * @return setter 方法，可能为 {@code null}
	 */
	public Method getSetter() {
		return setter;
	}

	/**
	 * 字段。
	 *
	 * @return 字段，可能为 {@code null}
	 */
	public Field getField() {
		return field;
	}

	/**
	 * 是否可读（存在 getter 或可访问字段）。
	 *
	 * @return 是否可读
	 */
	public boolean isReadable() {
		return getter != null || (field != null && isFieldAccessible());
	}

	/**
	 * 是否可写（存在 setter 或可访问字段）。
	 *
	 * @return 是否可写
	 */
	public boolean isWritable() {
		return setter != null || (field != null && isFieldAccessible());
	}

	private boolean isFieldAccessible() {
		return !java.lang.reflect.Modifier.isFinal(field.getModifiers());
	}

	@Override
	public String toString() {
		return "PropDesc{name='" + name + "', type=" + type + '}';
	}
}
