package com.sure.tool.benchmark;

import java.util.Date;
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

import com.sure.tool.date.DateUtil;

/**
 * 日期工具基准：suretool DateUtil（SimpleDateFormat 线程缓存实现）与 Hutool DateUtil 的格式化/解析对比。
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
public class DateUtilBenchmark {

	/** 固定时刻 */
	private final Date date = new Date(1789000000000L);
	/** 标准日期时间文本 */
	private final String dateStr = "2026-09-10 10:30:00";

	@Benchmark
	public String sureFormat() {
		return DateUtil.format(date);
	}

	@Benchmark
	public String hutoolFormat() {
		return cn.hutool.core.date.DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
	}

	@Benchmark
	public Date sureParse() {
		return DateUtil.parse(dateStr);
	}

	@Benchmark
	public Date hutoolParse() {
		return cn.hutool.core.date.DateUtil.parse(dateStr);
	}
}
