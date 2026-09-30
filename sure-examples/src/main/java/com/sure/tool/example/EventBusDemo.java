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
package com.sure.tool.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.sure.tool.event.EventBus;

/**
 * 事件总线示例（sure-event）。
 */
public class EventBusDemo {

	/** 订单事件。 */
	public static final class OrderCreated {
		private final long orderId;

		OrderCreated(long orderId) {
			this.orderId = orderId;
		}

		public long orderId() {
			return orderId;
		}
	}

	/**
	 * 运行示例。
	 */
	public static void run() throws Exception {
		System.out.println("=== EventBusDemo ===");
		EventBus bus = new EventBus();
		CountDownLatch latch = new CountDownLatch(1);
		bus.subscribe(OrderCreated.class, e -> {
			System.out.println("订单事件: " + e.orderId());
			latch.countDown();
		});
		bus.post(new OrderCreated(1001L));
		System.out.println("listener 收到 = " + latch.await(2, TimeUnit.SECONDS));
	}
}
