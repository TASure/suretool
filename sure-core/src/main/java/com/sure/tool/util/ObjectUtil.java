package com.sure.tool.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 对象工具类，参考 Hutool 的 {@code ObjectUtil} 设计。
 *
 * @author suretool
 */
public class ObjectUtil {

	private ObjectUtil() {
	}

	/**
	 * 对象是否为 {@code null}。
	 *
	 * @param obj 对象
	 * @return 是否为 {@code null}
	 */
	public static boolean isNull(Object obj) {
		return obj == null;
	}

	/**
	 * 对象是否非 {@code null}。
	 *
	 * @param obj 对象
	 * @return 是否非 {@code null}
	 */
	public static boolean isNotNull(Object obj) {
		return obj != null;
	}

	/**
	 * 对象是否为空：{@code null}、空字符串、空集合、空 Map、空数组、空 Optional。
	 *
	 * @param obj 对象
	 * @return 是否为空
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof CharSequence) {
			return ((CharSequence) obj).length() == 0;
		}
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).isEmpty();
		}
		if (obj instanceof Map) {
			return ((Map<?, ?>) obj).isEmpty();
		}
		if (obj instanceof Optional) {
			return !((Optional<?>) obj).isPresent();
		}
		if (obj.getClass().isArray()) {
			return ArrayUtil.isEmpty(obj);
		}
		return false;
	}

	/**
	 * 对象是否非空。
	 *
	 * @param obj 对象
	 * @return 是否非空
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	/**
	 * 深度比较两个对象是否相等（数组按元素比较）。
	 *
	 * @param obj1 对象 1
	 * @param obj2 对象 2
	 * @return 是否相等
	 */
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
		}
		if (obj1.getClass().isArray() && obj2.getClass().isArray()) {
			Object[] arr1 = ArrayUtil.toList(obj1).toArray();
			Object[] arr2 = ArrayUtil.toList(obj2).toArray();
			if (arr1.length != arr2.length) {
				return false;
			}
			for (int i = 0; i < arr1.length; i++) {
				if (!equals(arr1[i], arr2[i])) {
					return false;
				}
			}
			return true;
		}
		return obj1.equals(obj2);
	}

	/**
	 * 对象是否不相等。
	 *
	 * @param obj1 对象 1
	 * @param obj2 对象 2
	 * @return 是否不相等
	 */
	public static boolean notEqual(Object obj1, Object obj2) {
		return !equals(obj1, obj2);
	}

	/**
	 * 获取对象的 hashCode。
	 *
	 * @param obj 对象
	 * @return hashCode
	 */
	public static int hashCode(Object obj) {
		return (obj == null) ? 0 : obj.hashCode();
	}

	/**
	 * 获取对象的身份标识字符串：类名@十六进制哈希。
	 *
	 * @param obj 对象
	 * @return 身份标识字符串
	 */
	public static String identityToString(Object obj) {
		if (obj == null) {
			return "null";
		}
		return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
	}

	/**
	 * 获取对象的类。
	 *
	 * @param obj 对象
	 * @return 对象的类，{@code null} 对象返回 {@code null}
	 */
	public static Class<?> getClass(Object obj) {
		return (obj == null) ? null : obj.getClass();
	}

	/**
	 * 对象为 {@code null} 时使用默认值。
	 *
	 * @param obj          对象
	 * @param defaultValue 默认值
	 * @param <T>          类型
	 * @return 非 {@code null} 返回原对象，否则返回默认值
	 */
	public static <T> T defaultIfNull(T obj, T defaultValue) {
		return (obj == null) ? defaultValue : obj;
	}

	/**
	 * 对象为空时使用默认值（空字符串、空集合、空数组等视为空）。
	 *
	 * @param obj          对象
	 * @param defaultValue 默认值
	 * @param <T>          类型
	 * @return 非空返回原对象，否则返回默认值
	 */
	public static <T> T defaultIfEmpty(T obj, T defaultValue) {
		return isEmpty(obj) ? defaultValue : obj;
	}

	/**
	 * 克隆对象（对象需实现 {@link Cloneable}）。
	 *
	 * @param obj 对象
	 * @param <T> 类型
	 * @return 克隆后的对象，克隆失败返回 {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T obj) {
		if (obj instanceof Cloneable) {
			try {
				return (T) obj.getClass().getMethod("clone").invoke(obj);
			} catch (ReflectiveOperationException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 通过序列化克隆对象（对象及其成员需实现 {@link Serializable}）。
	 *
	 * @param obj 对象
	 * @param <T> 类型
	 * @return 克隆后的对象，失败时返回 {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cloneByStream(T obj) {
		if (obj == null || !(obj instanceof Serializable)) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(obj);
			}
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
				return (T) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	/**
	 * 对象序列化为字节数组。
	 *
	 * @param obj 对象（需实现 {@link Serializable}）
	 * @return 字节数组，失败返回 {@code null}
	 */
	public static byte[] serialize(Object obj) {
		if (obj == null || !(obj instanceof Serializable)) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(obj);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * 字节数组反序列化为对象。
	 *
	 * @param bytes 字节数组
	 * @return 对象，失败返回 {@code null}
	 */
	public static Object deserialize(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			return ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
