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
package com.sure.tool.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 轻量事件总线（线程安全）。
 *
 * <p>支持两种注册风格：函数式 {@link #subscribe(Class, Consumer)} 与注解式
 * {@link #register(Object)}；两种投递：同步 {@link #post(Object)} 与
 * 虚拟线程异步 {@link #postAsync(Object)}。监听器异常由
 * {@link EventExceptionHandler} 统一处理，不影响其他监听器。</p>
 *
 * <p>类型匹配规则：监听类型与事件实际类型兼容（监听父类型/接口会收到子类事件）。</p>
 *
 * @since 1.4.0
 */
public class EventBus {

	private static final EventBus DEFAULT = new EventBus();

	/**
	 * 构造独立事件总线（彼此隔离注册表）。
	 */
	public EventBus() {
	}

	private final Map<Class<?>, List<Subscription>> registry = new ConcurrentHashMap<>();
	private volatile EventExceptionHandler exceptionHandler =
			(t, e) -> System.err.println("[sure-event] 监听器异常: " + t);

	/**
	 * 获取全局默认总线。
	 *
	 * @return 默认 EventBus
	 */
	public static EventBus getDefault() {
		return DEFAULT;
	}

	/**
	 * 函数式注册监听器。
	 *
	 * @param eventType 事件类型
	 * @param listener  监听器
	 * @param <T>       事件泛型
	 * @return 订阅句柄（{@link Subscription#close()} 取消）
	 */
	public <T> Subscription subscribe(Class<T> eventType, Consumer<T> listener) {
		Objects.requireNonNull(eventType, "eventType must not be null");
		Objects.requireNonNull(listener, "listener must not be null");
		Subscription subscription = new Subscription(this, eventType, e -> listener.accept(eventType.cast(e)), listener, null);
		registry.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscription);
		return subscription;
	}

	/**
	 * 按监听器取消注册（移除该 eventType 下与传入 listener 相同的注册）。
	 *
	 * @param eventType 事件类型
	 * @param listener  监听器
	 * @param <T>       事件泛型
	 */
	public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
		Objects.requireNonNull(eventType, "eventType must not be null");
		Objects.requireNonNull(listener, "listener must not be null");
		List<Subscription> list = registry.get(eventType);
		if (list == null) {
			return;
		}
		list.removeIf(sub -> sub.rawListener() == listener);
	}

	/**
	 * 同步投递事件（调用线程执行全部匹配监听器）。
	 *
	 * <p>若事件无任何匹配监听器，自动改投 {@link DeadEvent}（仅一次，不再递归），
	 * 与 Guava EventBus 行为一致。</p>
	 *
	 * @param event 事件
	 */
	public void post(Object event) {
		Objects.requireNonNull(event, "event must not be null");
		post0(event, true);
	}

	private void post0(Object event, boolean allowDeadEvent) {
		Class<?> type = event.getClass();
		boolean dispatched = false;
		for (Map.Entry<Class<?>, List<Subscription>> entry : registry.entrySet()) {
			if (!entry.getKey().isAssignableFrom(type)) {
				continue;
			}
			for (Subscription subscription : entry.getValue()) {
				if (!subscription.isActive()) {
					continue;
				}
				dispatch(subscription.listener(), event);
				dispatched = true;
			}
		}
		if (!dispatched && allowDeadEvent) {
			post0(new DeadEvent(this, event), false);
		}
	}

	/**
	 * 异步投递事件（每个事件一个虚拟线程，不保证顺序）。
	 *
	 * @param event 事件
	 */
	public void postAsync(Object event) {
		Objects.requireNonNull(event, "event must not be null");
		Thread.ofVirtual().name("sure-event-async").start(() -> post(event));
	}

	/**
	 * 注解式注册：收集 bean 上所有 {@link EventSubscribe} 标注的 public 方法
	 * （方法必须有且仅 1 个参数，参数类型即监听事件类型）。
	 *
	 * @param bean 监听器实例
	 * @return 本次注册的订阅数
	 * @throws IllegalArgumentException bean 为 null 或注解方法签名非法
	 */
	public int register(Object bean) {
		Objects.requireNonNull(bean, "bean must not be null");
		int count = 0;
		for (Method method : bean.getClass().getMethods()) {
			if (!method.isAnnotationPresent(EventSubscribe.class)) {
				continue;
			}
			if (!Modifier.isPublic(method.getModifiers()) || method.getParameterCount() != 1) {
				throw new IllegalArgumentException("@EventSubscribe 方法须为 public 且有且仅 1 个参数: "
						+ method.toGenericString());
			}
			Class<?> eventType = method.getParameterTypes()[0];
			Consumer<Object> listener = event -> invokeSafely(method, bean, event);
			registry.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
					.add(new Subscription(this, eventType, listener, null, bean));
			count++;
		}
		return count;
	}

	/**
	 * 移除 bean 上全部注解注册的监听器。
	 *
	 * @param bean 监听器实例
	 */
	public void unregister(Object bean) {
		Objects.requireNonNull(bean, "bean must not be null");
		registry.values().forEach(list -> list.removeIf(sub -> sub.owner() == bean));
	}

	/**
	 * 设置异常处理器。
	 *
	 * @param handler 处理器（null 表示恢复默认）
	 */
	public void setExceptionHandler(EventExceptionHandler handler) {
		this.exceptionHandler = handler == null
				? (t, e) -> System.err.println("[sure-event] 监听器异常: " + t)
				: handler;
	}

	/**
	 * 当前监听器总数。
	 *
	 * @return 监听器数量
	 */
	public int listenerCount() {
		return registry.values().stream().mapToInt(List::size).sum();
	}

	/**
	 * 清空全部注册。
	 */
	public void clear() {
		registry.clear();
	}

	private void dispatch(Consumer<Object> listener, Object event) {
		try {
			listener.accept(event);
		} catch (Throwable t) {
			exceptionHandler.handle(t, event);
		}
	}

	private void invokeSafely(Method method, Object bean, Object event) {
		try {
			method.invoke(bean, event);
		} catch (java.lang.reflect.InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			exceptionHandler.handle(cause, event);
		} catch (Throwable t) {
			exceptionHandler.handle(t, event);
		}
	}

	void remove(Subscription subscription) {
		List<Subscription> list = registry.get(subscription.eventType());
		if (list != null) {
			list.remove(subscription);
		}
	}

	/**
	 * 死事件：投递的事件没有任何匹配监听器时包装为 {@link DeadEvent} 再次投递，
	 * 便于全局兜底监听（{@code subscribe(DeadEvent.class, ...)}）。
	 *
	 * @since 1.11.0
	 */
	public static final class DeadEvent {
		private final Object source;
		private final Object event;

		/**
		 * 构造死事件。
		 *
		 * @param source 投递源（通常为 EventBus 实例）
		 * @param event  原始未消费事件
		 */
		public DeadEvent(Object source, Object event) {
			this.source = source;
			this.event = event;
		}

		/**
		 * 投递源。
		 *
		 * @return 源对象
		 */
		public Object getSource() {
			return source;
		}

		/**
		 * 原始未消费事件。
		 *
		 * @return 事件对象
		 */
		public Object getEvent() {
			return event;
		}
	}
}
