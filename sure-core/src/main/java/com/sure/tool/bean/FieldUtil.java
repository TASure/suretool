package com.sure.tool.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段操作工具类，参考 Hutool 的 {@code FieldUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class FieldUtil {

	private FieldUtil() {
	}

	/**
	 * 获取类层级上的全部声明字段（子类优先，按声明顺序，重名字段去重）。
	 *
	 * @param clazz 类
	 * @return 字段数组
	 */
	public static Field[] getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		Map<String, Boolean> seen = new LinkedHashMap<>();
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				if (seen.putIfAbsent(field.getName(), Boolean.TRUE) == null) {
					fields.add(field);
				}
			}
		}
		return fields.toArray(new Field[0]);
	}

	/**
	 * 获取字段名列表。
	 *
	 * @param clazz 类
	 * @return 字段名列表
	 */
	public static List<String> getFieldNames(Class<?> clazz) {
		List<String> names = new ArrayList<>();
		for (Field field : getFields(clazz)) {
			names.add(field.getName());
		}
		return names;
	}

	/**
	 * 按名称查找字段（含父类）。
	 *
	 * @param clazz     类
	 * @param fieldName 字段名
	 * @return 字段，未找到返回 {@code null}
	 */
	public static Field getField(Class<?> clazz, String fieldName) {
		if (clazz == null || fieldName == null) {
			return null;
		}
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				Field field = c.getDeclaredField(fieldName);
				return field;
			} catch (NoSuchFieldException e) {
				// 继续向上查找
			}
		}
		return null;
	}

	/**
	 * 字段是否 public。
	 *
	 * @param field 字段
	 * @return 是否 public
	 */
	public static boolean isPublic(Field field) {
		return field != null && Modifier.isPublic(field.getModifiers());
	}

	/**
	 * 读取静态常量值。
	 *
	 * @param clazz     类
	 * @param fieldName 字段名
	 * @return 常量值
	 */
	public static Object getConstantValue(Class<?> clazz, String fieldName) {
		Field field = getField(clazz, fieldName);
		if (field == null || !Modifier.isStatic(field.getModifiers())) {
			return null;
		}
		try {
			field.setAccessible(true);
			return field.get(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("读取常量 " + fieldName + " 失败", e);
		}
	}
}
