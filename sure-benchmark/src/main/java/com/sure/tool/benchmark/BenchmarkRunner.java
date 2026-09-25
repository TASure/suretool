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

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * JMH 基准统一入口：一次运行全部基准类。
 *
 * <p>命令行运行（项目根目录）：</p>
 * <pre>
 * mvn -pl sure-benchmark exec:java
 * </pre>
 *
 * <p>基准策略：每个基准 {@code @Fork(1)}、warmup 1 秒 × 3 轮、measurement 1 秒 × 5 轮，
 * 平均耗时（ns/op）输出，报告见 {@code sure-benchmark/README.md} 与 ROADMAP。</p>
 *
 * @author suretool
 * @since 0.1.0
 */
public class BenchmarkRunner {

	/**
	 * 运行全部基准。
	 *
	 * @param args 命令行参数（忽略）
	 * @throws Exception 基准执行异常
	 */
	public static void main(String[] args) throws Exception {
		ChainedOptionsBuilder builder = new OptionsBuilder()
				// 3 个 fork：共享 runner 上单次 fork 噪声大（曾出现 Format 项 ratio 虚高 1.84 而误报回归）
				.forks(3)
				.warmupIterations(3)
				.warmupTime(TimeValue.seconds(1))
				.measurementIterations(5)
				.measurementTime(TimeValue.seconds(1))
				// 降低 fork JVM 内存占用，避免低配环境内存压力导致 fork 进程被杀
				.jvmArgs("-Xms256m", "-Xmx256m");
		if (args.length > 0) {
			// 传入过滤参数时仅跑指定基准（本地排查用）；否则跑全部
			builder.include(args[0]);
		} else {
			builder.include("com\\.sure\\.tool\\.benchmark\\..*");
		}
		Options options = builder.build();
		new Runner(options).run();
	}
}