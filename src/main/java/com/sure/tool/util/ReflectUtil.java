package com.sure.tool.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具类，参考 Hutool 的 {@code ReflectUtil} 设计。
 *
 * @author suretool
 */
public class ReflectUtil {

	private ReflectUtil() {
	}

	/**
	 * 获取类或父类中的字段（自动设置可访问）。
	 *
	 * @param clazz     类
	 * @param fieldName 字段名
	 * @return 字段，不存在返回 {@code null}
	 */
	public static Field getField(Class<?> clazz, String fieldName) {
		if (clazz == null || fieldName == null) {
			return null;
		}
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				Field field = c.getDeclaredField(fieldName);
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				return field;
			} catch (NoSuchFieldException ignore) {
				// 继续查找父类
			}
		}
		return null;
	}

	/**
	 * 获取对象字段值（支持静态字段，对象传 {@code null}）。
	 *
	 * @param obj       对象，静态字段传 {@code null}
	 * @param fieldName 字段名
	 * @return 字段值
	 */
	public static Object getFieldValue(Object obj, String fieldName) {
		Class<?> clazz = (obj == null) ? null : obj.getClass();
		Field field = getField(clazz, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("字段不存在: " + fieldName);
		}
		try {
			return field.get(obj);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("无法访问字段: " + fieldName, e);
		}
	}

	/**
	 * 设置对象字段值（支持静态字段，对象传 {@code null}）。
	 *
	 * @param obj       对象，静态字段传 {@code null}
	 * @param fieldName 字段名
	 * @param value     字段值
	 */
	public static void setFieldValue(Object obj, String fieldName, Object value) {
		Class<?> clazz = (obj == null) ? null : obj.getClass();
		Field field = getField(clazz, fieldName);
		if (field == null) {
			throw new IllegalArgumentException("字段不存在: " + fieldName);
		}
		try {
			field.set(obj, value);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("无法设置字段: " + fieldName, e);
		}
	}

	/**
	 * 获取类及其父类的全部字段。
	 *
	 * @param clazz 类
	 * @return 字段数组
	 */
	public static Field[] getFields(Class<?> clazz) {
		if (clazz == null) {
			return new Field[0];
		}
		List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			Field[] declaredFields = c.getDeclaredFields();
			for (Field field : declaredFields) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				fields.add(field);
			}
		}
		return fields.toArray(new Field[0]);
	}

	/**
	 * 获取类的全部公共方法（含继承）。
	 *
	 * @param clazz 类
	 * @return 方法数组
	 */
	public static Method[] getMethods(Class<?> clazz) {
		if (clazz == null) {
			return new Method[0];
		}
		return clazz.getMethods();
	}

	/**
	 * 获取指定方法。
	 *
	 * @param clazz      类
	 * @param methodName 方法名
	 * @param paramTypes 参数类型
	 * @return 方法，不存在返回 {@code null}
	 */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		if (clazz == null || methodName == null) {
			return null;
		}
		try {
			Method method = clazz.getMethod(methodName, paramTypes);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			return method;
		} catch (NoSuchMethodException ignore) {
			// 继续查找父类
		}
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				Method method = c.getDeclaredMethod(methodName, paramTypes);
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				return method;
			} catch (NoSuchMethodException ignore) {
				// 继续
			}
		}
		return null;
	}

	/**
	 * 调用对象方法。
	 *
	 * @param obj        对象
	 * @param methodName 方法名
	 * @param args       参数
	 * @return 方法返回值，无返回值返回 {@code null}
	 */
	public static Object invoke(Object obj, String methodName, Object... args) {
		if (obj == null) {
			throw new IllegalArgumentException("对象不能为 null");
		}
		Object[] params = (args == null) ? new Object[0] : args;
		Method method = findMethod(obj.getClass(), methodName, params);
		if (method == null) {
			throw new IllegalArgumentException("方法不存在: " + methodName + " 于类 " + obj.getClass().getName());
		}
		try {
			return method.invoke(obj, params);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("调用方法失败: " + methodName, e);
		}
	}

	/**
	 * 调用静态方法。
	 *
	 * @param clazz      类
	 * @param methodName 方法名
	 * @param args       参数
	 * @return 方法返回值
	 */
	public static Object invokeStatic(Class<?> clazz, String methodName, Object... args) {
		if (clazz == null) {
			throw new IllegalArgumentException("类不能为 null");
		}
		Object[] params = (args == null) ? new Object[0] : args;
		Method method = findMethod(clazz, methodName, params);
		if (method == null || !Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("静态方法不存在: " + methodName + " 于类 " + clazz.getName());
		}
		try {
			return method.invoke(null, params);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("调用静态方法失败: " + methodName, e);
		}
	}

	/**
	 * 实例化对象。
	 *
	 * @param clazz  类
	 * @param params 构造参数
	 * @param <T>    类型
	 * @return 实例
	 */
	public static <T> T invokeConstructor(Class<T> clazz, Object... params) {
		return ClassUtil.newInstance(clazz, params);
	}

	private static Method findMethod(Class<?> clazz, String methodName, Object[] args) {
		Method best = null;
		int bestScore = -1;
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Method method : c.getDeclaredMethods()) {
				if (!method.getName().equals(methodName)) {
					continue;
				}
				Class<?>[] types = method.getParameterTypes();
				if (types.length != args.length) {
					continue;
				}
				int score = 0;
				boolean match = true;
				for (int i = 0; i < types.length; i++) {
					if (args[i] == null) {
						if (types[i].isPrimitive()) {
							match = false;
							break;
						}
					} else if (ClassUtil.isAssignable(types[i], args[i].getClass())) {
						score++;
						if (types[i].equals(args[i].getClass())) {
							score++;
						}
					} else {
						match = false;
						break;
					}
				}
				if (match && score > bestScore) {
					bestScore = score;
					best = method;
				}
			}
		}
		if (best != null && !best.isAccessible()) {
			best.setAccessible(true);
		}
		return best;
	}
}
