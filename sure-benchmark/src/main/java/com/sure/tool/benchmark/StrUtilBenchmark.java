package com.sure.tool.benchmark;

import java.util.ArrayList;
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

import com.sure.tool.util.StrUtil;

/**
 * 字符串工具基准：suretool StrUtil 与 Hutool StrUtil 的常用路径对比。
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
public class StrUtilBenchmark {

	/** 带首尾空白的常规文本 */
	private final String text = "  hello suretool world  ";
	/** 纯空白文本 */
	private final String blank = " \t\r\n ";
	/** 空串 */
	private final String empty = "";
	/** 拼接片段 */
	private final List<String> parts = parts();

	private static List<String> parts() {
		List<String> list = new ArrayList<String>();
		list.add("java");
		list.add("suretool");
		list.add("hutool");
		list.add("benchmark");
		list.add("jmh");
		return list;
	}

	@Benchmark
	public boolean sureIsBlank() {
		return StrUtil.isBlank(blank);
	}

	@Benchmark
	public boolean hutoolIsBlank() {
		return cn.hutool.core.util.StrUtil.isBlank(blank);
	}

	@Benchmark
	public boolean sureIsEmpty() {
		return StrUtil.isEmpty(empty);
	}

	@Benchmark
	public boolean hutoolIsEmpty() {
		return cn.hutool.core.util.StrUtil.isEmpty(empty);
	}

	@Benchmark
	public String sureTrim() {
		return StrUtil.trim(text);
	}

	@Benchmark
	public String hutoolTrim() {
		return cn.hutool.core.util.StrUtil.trim(text);
	}

	@Benchmark
	public String sureJoin() {
		return StrUtil.join(",", parts);
	}

	@Benchmark
	public String hutoolJoin() {
		return cn.hutool.core.util.StrUtil.join(",", parts);
	}

	@Benchmark
	public String sureSub() {
		return StrUtil.sub(text, 2, 7);
	}

	@Benchmark
	public String hutoolSub() {
		return cn.hutool.core.util.StrUtil.sub(text, 2, 7);
	}
}
