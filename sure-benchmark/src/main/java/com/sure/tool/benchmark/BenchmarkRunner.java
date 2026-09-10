package com.sure.tool.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
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
		Options options = new OptionsBuilder()
				.include("com\\.sure\\.tool\\.benchmark\\..*")
				.forks(1)
				.warmupIterations(3)
				.warmupTime(TimeValue.seconds(1))
				.measurementIterations(5)
				.measurementTime(TimeValue.seconds(1))
				.jvmArgs("-Xms512m", "-Xmx512m")
				.build();
		new Runner(options).run();
	}
}
