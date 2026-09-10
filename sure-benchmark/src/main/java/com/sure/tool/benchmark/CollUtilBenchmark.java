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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import com.sure.tool.collection.CollUtil;

/**
 * 集合工具基准：suretool CollUtil 与 Hutool CollUtil 的常用路径对比。
 *
 * @author suretool
 * @since 0.1.0
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class CollUtilBenchmark {

	/** 10 元素列表 */
	private final List<String> list = list(10);
	/** 空列表 */
	private final List<String> emptyList = Collections.emptyList();

	private static List<String> list(int size) {
		List<String> list = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			list.add("item-" + i);
		}
		return list;
	}

	@Benchmark
	public boolean sureIsEmpty() {
		return CollUtil.isEmpty(emptyList);
	}

	@Benchmark
	public boolean hutoolIsEmpty() {
		return cn.hutool.core.collection.CollUtil.isEmpty(emptyList);
	}

	@Benchmark
	public String sureJoin() {
		return CollUtil.join(list, ",");
	}

	@Benchmark
	public String hutoolJoin() {
		return cn.hutool.core.collection.CollUtil.join(list, ",");
	}

	@Benchmark
	public List<String> sureNewArrayList() {
		return CollUtil.newArrayList("a", "b", "c", "d");
	}

	@Benchmark
	public List<String> hutoolNewArrayList() {
		return cn.hutool.core.collection.CollUtil.newArrayList("a", "b", "c", "d");
	}

	@Benchmark
	public boolean sureContains() {
		return CollUtil.contains(list, "item-5");
	}

	@Benchmark
	public boolean hutoolContains() {
		return cn.hutool.core.collection.CollUtil.contains(list, "item-5");
	}
}