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

import java.lang.reflect.Method;

/**
 * 切面基类。
 *
 * <p>定义方法匹配钩子与三个通知点（前置 / 后置 / 异常）。
 * 子类按需覆写通知方法，未覆写的方法不产生任何副作用。</p>
 *
 * <p>示例（调用日志切面）：</p>
 * <pre>{@code
 * Aspect aspect = new Aspect() {
 *     &#64;Override
 *     public void before(Object target, Method method, Object[] args) {
 *         System.out.println("调用: " + method.getName());
 *     }
 * };
 * }</pre>
 *
 * @since 1.3.0
 */
public abstract class Aspect {

	/**
	 * 方法匹配钩子：决定通知是否应用于该方法。
	 *
	 * @param method 被调用方法
	 * @return true 表示拦截（触发通知），false 表示直接调用原方法
	 */
	public boolean match(Method method) {
		return true;
	}

	/**
	 * 前置通知：目标方法调用前触发。
	 *
	 * @param target 目标对象（接口代理模式为 null）
	 * @param method 被调用方法
	 * @param args   方法参数
	 */
	public void before(Object target, Method method, Object[] args) {
	}

	/**
	 * 后置通知：目标方法正常返回后触发。
	 *
	 * @param target 目标对象（接口代理模式为 null）
	 * @param method 被调用方法
	 * @param args   方法参数
	 * @param result 返回值
	 */
	public void after(Object target, Method method, Object[] args, Object result) {
	}

	/**
	 * 异常通知：目标方法抛出异常后触发，随后异常继续向外传播。
	 *
	 * @param target 目标对象（接口代理模式为 null）
	 * @param method 被调用方法
	 * @param args   方法参数
	 * @param e      被调用方法抛出的异常
	 */
	public void afterException(Object target, Method method, Object[] args, Throwable e) {
	}
}
