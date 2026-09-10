package com.sure.tool.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean 描述，扫描并缓存类的属性（getter/setter/字段），参考 Hutool 的 {@code BeanDesc} 设计。
 *
 * @author suretool
 */
public class BeanDesc {

	private final Class<?> beanClass;
	private final Map<String, PropDesc> propMap = new LinkedHashMap<>();

	/**
	 * 创建 Bean 描述并扫描属性。
	 *
	 * @param beanClass Bean 类
	 */
	public BeanDesc(Class<?> beanClass) {
		this.beanClass = beanClass;
		init();
	}

	/**
	 * Bean 类。
	 *
	 * @return Bean 类
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * 属性名集合。
	 *
	 * @return 属性名集合
	 */
	public Collection<String> getPropNames() {
		return propMap.keySet();
	}

	/**
	 * 全部属性描述。
	 *
	 * @return 属性描述集合
	 */
	public Collection<PropDesc> getProps() {
		return propMap.values();
	}

	/**
	 * 获取属性描述，不存在返回 {@code null}。
	 *
	 * @param name 属性名
	 * @return 属性描述或 {@code null}
	 */
	public PropDesc getProp(String name) {
		return propMap.get(name);
	}

	/**
	 * 是否包含指定属性。
	 *
	 * @param name 属性名
	 * @return 是否包含
	 */
	public boolean containsProp(String name) {
		return propMap.containsKey(name);
	}

	/**
	 * 扫描 getter/setter 与字段，构造属性描述。
	 */
	private void init() {
		for (Method method : beanClass.getMethods()) {
			scanMethod(method);
		}
		for (Field field : getAllFields(beanClass)) {
			addField(field);
		}
	}

	/**
	 * 解析方法为 getter 或 setter。
	 *
	 * @param method 方法
	 */
	private void scanMethod(Method method) {
		if (method.isBridge() || method.isSynthetic()) {
			return;
		}
		String methodName = method.getName();
		int paramCount = method.getParameterCount();
		Class<?> returnType = method.getReturnType();

		if (returnType == void.class) {
			// setter：setXxx(value)
			if (paramCount == 1 && methodName.startsWith("set") && methodName.length() > 3) {
				PropDesc desc = getOrCreateProp(decapitalize(methodName.substring(3)));
				if (desc.getSetter() == null) {
					desc = new PropDesc(desc.getName(), desc.getType() != null ? desc.getType() : method.getParameterTypes()[0],
							desc.getGetter(), method, desc.getField());
					propMap.put(desc.getName(), desc);
				}
			}
		} else if (paramCount == 0) {
			// getter：getXxx() 或 isXxx()
			String propName = null;
			if (methodName.startsWith("get") && methodName.length() > 3 && !"getClass".equals(methodName)) {
				propName = decapitalize(methodName.substring(3));
			} else if (methodName.startsWith("is") && methodName.length() > 2
					&& (returnType == boolean.class || returnType == Boolean.class)) {
				propName = decapitalize(methodName.substring(2));
			}
			if (propName != null) {
				PropDesc desc = getOrCreateProp(propName);
				if (desc.getGetter() == null) {
					desc = new PropDesc(desc.getName(), desc.getType() != null ? desc.getType() : returnType,
							method, desc.getSetter(), desc.getField());
					propMap.put(desc.getName(), desc);
				}
			}
		}
	}

	/**
	 * 补充字段信息。
	 *
	 * @param field 字段
	 */
	private void addField(Field field) {
		PropDesc desc = getOrCreateProp(field.getName());
		if (desc.getField() == null) {
			desc = new PropDesc(desc.getName(), desc.getType() != null ? desc.getType() : field.getType(),
					desc.getGetter(), desc.getSetter(), field);
			propMap.put(desc.getName(), desc);
		}
	}

	/**
	 * 获取或创建属性描述。
	 *
	 * @param name 属性名
	 * @return 属性描述
	 */
	private PropDesc getOrCreateProp(String name) {
		PropDesc desc = propMap.get(name);
		if (desc == null) {
			desc = new PropDesc(name, null, null, null, null);
			propMap.put(name, desc);
		}
		return desc;
	}

	/**
	 * 收集类层级上全部声明字段（按声明顺序，子类优先，重名字段去重）。
	 *
	 * @param clazz 类
	 * @return 字段列表
	 */
	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				if (!containsName(fields, field.getName())) {
					fields.add(field);
				}
			}
		}
		return fields;
	}

	private static boolean containsName(List<Field> fields, String name) {
		for (Field field : fields) {
			if (field.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将 getter 属性名首字母小写（遵循 JavaBeans 规范：连续大写字母保持原样）。
	 *
	 * @param name 属性名
	 * @return 小写化后的属性名
	 */
	private static String decapitalize(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
			return name;
		}
		char[] chars = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
}
