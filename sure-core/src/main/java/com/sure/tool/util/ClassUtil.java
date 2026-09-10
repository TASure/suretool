package com.sure.tool.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 类工具类，参考 Hutool 的 {@code ClassUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ClassUtil {

	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

	static {
		PRIMITIVE_WRAPPER_MAP.put(Boolean.class, boolean.class);
		PRIMITIVE_WRAPPER_MAP.put(Byte.class, byte.class);
		PRIMITIVE_WRAPPER_MAP.put(Character.class, char.class);
		PRIMITIVE_WRAPPER_MAP.put(Short.class, short.class);
		PRIMITIVE_WRAPPER_MAP.put(Integer.class, int.class);
		PRIMITIVE_WRAPPER_MAP.put(Long.class, long.class);
		PRIMITIVE_WRAPPER_MAP.put(Float.class, float.class);
		PRIMITIVE_WRAPPER_MAP.put(Double.class, double.class);
	}

	private ClassUtil() {
	}

	/**
	 * 获取对象的类。
	 *
	 * @param obj 对象
	 * @return 对象的类，{@code null} 返回 {@code null}
	 */
	public static Class<?> getClass(Object obj) {
		return (obj == null) ? null : obj.getClass();
	}

	/**
	 * 获取对象的类名。
	 *
	 * @param obj      对象
	 * @param isSimple 是否返回简单类名
	 * @return 类名，{@code null} 对象返回 {@code null}
	 */
	public static String getClassName(Object obj, boolean isSimple) {
		Class<?> clazz = getClass(obj);
		return (clazz == null) ? null : (isSimple ? clazz.getSimpleName() : clazz.getName());
	}

	/**
	 * 是否为基本类型（基本类型、包装类型、String、Number、Date 等）。
	 *
	 * @param clazz 类
	 * @return 是否为基本类型
	 */
	public static boolean isBasicType(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		return clazz.isPrimitive()
				|| isPrimitiveWrapper(clazz)
				|| clazz == String.class
				|| clazz == Number.class
				|| Number.class.isAssignableFrom(clazz)
				|| clazz == Boolean.class
				|| clazz == Character.class
				|| clazz == Date.class;
	}

	/**
	 * 是否为基本类型包装类。
	 *
	 * @param clazz 类
	 * @return 是否为包装类
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		return clazz != null && PRIMITIVE_WRAPPER_MAP.containsKey(clazz);
	}

	/**
	 * 包装类转基本类型。
	 *
	 * @param wrapperClass 包装类
	 * @return 基本类型，非包装类返回原类
	 */
	public static Class<?> getPrimitive(Class<?> wrapperClass) {
		Class<?> primitive = PRIMITIVE_WRAPPER_MAP.get(wrapperClass);
		return (primitive == null) ? wrapperClass : primitive;
	}

	/**
	 * 获取包名。
	 *
	 * @param clazz 类
	 * @return 包名，匿名类返回空串
	 */
	public static String getPackageName(Class<?> clazz) {
		if (clazz == null) {
			return StrUtil.EMPTY;
		}
		Package pkg = clazz.getPackage();
		return (pkg == null) ? StrUtil.EMPTY : pkg.getName();
	}

	/**
	 * 获取包路径（点号转斜杠）。
	 *
	 * @param clazz 类
	 * @return 包路径，如 {@code com/sure/tool}
	 */
	public static String getPackagePath(Class<?> clazz) {
		return getPackageName(clazz).replace('.', '/');
	}

	/**
	 * 加载类，依次使用线程上下文类加载器和当前类加载器。
	 *
	 * @param className    类名
	 * @param isInitialized 是否初始化
	 * @return 类
	 * @throws ClassNotFoundException 类不存在
	 */
	public static Class<?> loadClass(String className, boolean isInitialized) throws ClassNotFoundException {
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
		if (contextLoader != null) {
			try {
				return Class.forName(className, isInitialized, contextLoader);
			} catch (ClassNotFoundException ignore) {
				// 继续尝试当前类加载器
			}
		}
		return Class.forName(className, isInitialized, ClassUtil.class.getClassLoader());
	}

	/**
	 * 加载类（默认初始化）。
	 *
	 * @param className 类名
	 * @return 类
	 * @throws ClassNotFoundException 类不存在
	 */
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return loadClass(className, true);
	}

	/**
	 * 按类名实例化对象。
	 *
	 * @param className 类名
	 * @param params    构造参数
	 * @param <T>       类型
	 * @return 实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className, Object... params) {
		try {
			return (T) newInstance(loadClass(className), params);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("类不存在: " + className, e);
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
	public static <T> T newInstance(Class<T> clazz, Object... params) {
		if (clazz == null) {
			return null;
		}
		try {
			if (params == null || params.length == 0) {
				Constructor<T> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			}
			Class<?>[] paramTypes = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++) {
				paramTypes[i] = (params[i] == null) ? null : params[i].getClass();
			}
			Constructor<T> constructor = findConstructor(clazz, paramTypes);
			if (constructor == null) {
				throw new IllegalArgumentException("未找到匹配的构造方法: " + clazz.getName());
			}
			constructor.setAccessible(true);
			return constructor.newInstance(params);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("实例化失败: " + clazz.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>[] paramTypes) {
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			Class<?>[] types = constructor.getParameterTypes();
			if (types.length != paramTypes.length) {
				continue;
			}
			boolean match = true;
			for (int i = 0; i < types.length; i++) {
				if (paramTypes[i] == null) {
					if (types[i].isPrimitive()) {
						match = false;
						break;
					}
				} else if (!isAssignable(types[i], paramTypes[i])) {
					match = false;
					break;
				}
			}
			if (match) {
				return (Constructor<T>) constructor;
			}
		}
		return null;
	}

	/**
	 * 目标类是否能接收来源类实例（含基本类型/包装类型互转）。
	 *
	 * @param target 目标类型
	 * @param from   来源类型
	 * @return 是否可赋值
	 */
	public static boolean isAssignable(Class<?> target, Class<?> from) {
		if (target == null || from == null) {
			return false;
		}
		if (target.isAssignableFrom(from)) {
			return true;
		}
		if (target.isPrimitive()) {
			return getPrimitive(from) == target;
		}
		if (from.isPrimitive()) {
			return PRIMITIVE_WRAPPER_MAP.get(target) == from;
		}
		return false;
	}

	/**
	 * 是否为普通类（非接口、非注解、非枚举、非基本类型、非数组、非抽象类）。
	 *
	 * @param clazz 类
	 * @return 是否为普通类
	 */
	public static boolean isNormalClass(Class<?> clazz) {
		return clazz != null
				&& !clazz.isInterface()
				&& !clazz.isAnnotation()
				&& !clazz.isEnum()
				&& !clazz.isPrimitive()
				&& !clazz.isArray()
				&& !Modifier.isAbstract(clazz.getModifiers());
	}

	/**
	 * 是否为枚举。
	 *
	 * @param clazz 类
	 * @return 是否为枚举
	 */
	public static boolean isEnum(Class<?> clazz) {
		return clazz != null && clazz.isEnum();
	}

	/**
	 * 是否为接口。
	 *
	 * @param clazz 类
	 * @return 是否为接口
	 */
	public static boolean isInterface(Class<?> clazz) {
		return clazz != null && clazz.isInterface();
	}

	/**
	 * 是否为注解。
	 *
	 * @param clazz 类
	 * @return 是否为注解
	 */
	public static boolean isAnnotation(Class<?> clazz) {
		return clazz != null && clazz.isAnnotation();
	}

	/**
	 * 获取基本类型的默认值。
	 *
	 * @param clazz 基本类型
	 * @return 默认值，非基本类型返回 {@code null}
	 */
	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz == null || !clazz.isPrimitive()) {
			return null;
		}
		if (clazz == boolean.class) {
			return Boolean.FALSE;
		}
		if (clazz == char.class) {
			return '\0';
		}
		return 0;
	}

	/**
	 * 获取简单类名。
	 *
	 * @param clazz 类
	 * @return 简单类名
	 */
	public static String getShortClassName(Class<?> clazz) {
		return (clazz == null) ? null : clazz.getSimpleName();
	}
}
