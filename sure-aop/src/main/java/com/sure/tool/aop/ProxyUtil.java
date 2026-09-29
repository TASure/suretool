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

package com.sure.tool.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.sure.tool.lang.Assert;

/**
 * 代理工厂。
 *
 * <p>基于 JDK 动态代理（{@code java.lang.reflect.Proxy}）创建切面代理：
 * 仅支持接口代理。对未实现接口的类目标，抛出带指引的异常
 * （类代理需引入 CGLIB 等字节码库自行实现）。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * Aspect aspect = new Aspect() {
 *     &#64;Override
 *     public void before(Object target, Method method, Object[] args) {
 *         Console.log("before: {}", method.getName());
 *     }
 * };
 * UserService proxy = ProxyUtil.proxy(new UserServiceImpl(), aspect);
 * proxy.createUser("sure"); // 触发 before 通知
 * }</pre>
 *
 * @since 1.3.0
 */
public final class ProxyUtil {

	private ProxyUtil() {
	}

	/**
	 * 为对象创建切面代理（JDK 动态代理）。
	 *
	 * <p>目标对象必须至少实现一个接口；调用拦截流程：
	 * match 不通过直接调用原方法；通过则 before → 调用 → after，
	 * 异常时 afterException 后继续传播。</p>
	 *
	 * @param target 目标对象
	 * @param aspect 切面
	 * @param <T>    目标类型
	 * @return 代理对象
	 * @throws IllegalArgumentException 目标为 null / 未实现接口 / 切面为 null 时抛出
	 */
	public static <T> T proxy(T target, Aspect aspect) {
		Assert.notNull(target, "目标对象不能为 null");
		Assert.notNull(aspect, "切面不能为 null");
		final Class<?> clazz = target.getClass();
		final Class<?>[] interfaces = clazz.getInterfaces();
		Assert.isFalse(interfaces.length == 0,
				"目标对象 [{}] 未实现任何接口，JDK 动态代理仅支持接口代理；类代理请引入 CGLIB 等字节码库",
				clazz.getName());
		final InvocationHandler handler = (proxy, method, args) -> dispatch(target, aspect, proxy, method, args);
		@SuppressWarnings("unchecked")
		final T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, handler);
		return result;
	}

	/**
	 * 为接口创建代理实例（无目标对象）。
	 *
	 * <p>适用于 mock / 拦截器场景：调用触发通知，原方法返回 null。</p>
	 *
	 * @param interfaceClass 接口类型
	 * @param aspect         切面
	 * @param <T>            接口类型
	 * @return 代理实例
	 * @throws IllegalArgumentException 参数为 null / 非接口时抛出
	 */
	public static <T> T proxy(Class<T> interfaceClass, Aspect aspect) {
		Assert.notNull(interfaceClass, "接口不能为 null");
		Assert.notNull(aspect, "切面不能为 null");
		Assert.isTrue(interfaceClass.isInterface(), "代理目标必须是接口，实际为 [{}]", interfaceClass.getName());
		final InvocationHandler handler = (proxy, method, args) -> dispatch(null, aspect, proxy, method, args);
		return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] { interfaceClass }, handler));
	}

	/**
	 * 分发调用：Object 方法走默认语义；业务方法走「匹配 → 通知 → 调用」流程。
	 */
	private static Object dispatch(Object target, Aspect aspect, Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getDeclaringClass() == Object.class) {
			return handleObjectMethod(proxy, method, args);
		}
		if (!aspect.match(method)) {
			return target == null ? null : method.invoke(target, args);
		}
		aspect.before(target, method, args);
		try {
			final Object result = target == null ? null : method.invoke(target, args);
			aspect.after(target, method, args, result);
			return result;
		} catch (InvocationTargetException e) {
			final Throwable cause = e.getCause() != null ? e.getCause() : e;
			aspect.afterException(target, method, args, cause);
			throw cause;
		}
	}

	/**
	 * Object 通用方法走代理默认语义（避免递归调用与无目标 NPE）。
	 */
	private static Object handleObjectMethod(Object proxy, Method method, Object[] args) {
		return switch (method.getName()) {
			case "toString" -> proxy.getClass().getName() + "@"
					+ Integer.toHexString(System.identityHashCode(proxy));
			case "hashCode" -> System.identityHashCode(proxy);
			case "equals" -> proxy == args[0];
			default -> null;
		};
	}
}
