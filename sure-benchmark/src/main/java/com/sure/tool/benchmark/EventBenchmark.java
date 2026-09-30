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
package com.sure.tool.benchmark;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import com.sure.tool.event.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * 事件总线基准：suretool EventBus（零依赖）与 Guava EventBus 对拍。
 *
 * <p>sureEventPost 无 hutool 对应项，不参与门禁判定，仅作报告展示。</p>
 */
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EventBenchmark {

	/** 事件负载。 */
	public static final class Payload {
		final long value;

		Payload(long value) {
			this.value = value;
		}
	}

	private final EventBus sureBus = new EventBus();
	private final com.google.common.eventbus.EventBus guavaBus = new com.google.common.eventbus.EventBus();
	private final AtomicLong counter = new AtomicLong();

	/** Guava 监听器。 */
	public static final class GuavaListener {
		private final AtomicLong counter;

		GuavaListener(AtomicLong counter) {
			this.counter = counter;
		}

		@Subscribe
		public void on(Payload event) {
			counter.incrementAndGet();
		}
	}

	{
		sureBus.subscribe(Payload.class, e -> counter.incrementAndGet());
		guavaBus.register(new GuavaListener(counter));
	}

	@Benchmark
	public void sureEventPost() {
		sureBus.post(new Payload(1));
	}

	@Benchmark
	public void guavaEventPost() {
		guavaBus.post(new Payload(1));
	}

	@Benchmark
	public long sureEventListenerCount() {
		return sureBus.listenerCount();
	}
}
