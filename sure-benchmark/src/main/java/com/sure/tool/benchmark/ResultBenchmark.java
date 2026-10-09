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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.sure.tool.lang.Result;

/**
 * Result 门面性能基准：构建/取值/链式 vs JDK Optional 与异常流。
 *
 * <p>验证「显式错误门面」零成本易用：Result 构建与 Optional 同量级，远低于异常流。</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ResultBenchmark {

	private final int value = 42;

	@Benchmark
	public int resultOkGet() {
		return Result.ok(value).get();
	}

	@Benchmark
	public int resultOkMap() {
		return Result.ok(value).map(x -> x * 2).get();
	}

	@Benchmark
	public int optionalOfGet() {
		return Optional.of(value).get();
	}

	@Benchmark
	public int optionalMap() {
		return Optional.of(value).map(x -> x * 2).get();
	}

	@Benchmark
	public int exceptionFlow() {
		try {
			return Integer.parseInt("42");
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
