package com.sure.tool.benchmark;

import java.util.LinkedHashMap;
import java.util.Map;
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

import com.sure.tool.json.JSONObject;
import com.sure.tool.json.JSONUtil;

/**
 * JSON 基准：suretool JSONUtil（零依赖递归下降）与 Hutool JSONUtil 的解析/序列化对比。
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
public class JsonBenchmark {

	/** 常规对象 JSON 文本 */
	private final String json = "{\"name\":\"suretool\",\"version\":\"0.1.0\",\"modules\":[\"core\",\"json\",\"http\"],\"active\":true,\"size\":128}";

	/** 待序列化 Map */
	private final Map<String, Object> map = buildMap();

	private static Map<String, Object> buildMap() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", "suretool");
		map.put("version", "0.1.0");
		map.put("active", Boolean.TRUE);
		map.put("size", Integer.valueOf(128));
		return map;
	}

	@Benchmark
	public JSONObject sureParseObj() {
		return JSONUtil.parseObj(json);
	}

	@Benchmark
	public cn.hutool.json.JSONObject hutoolParseObj() {
		return cn.hutool.json.JSONUtil.parseObj(json);
	}

	@Benchmark
	public String sureToJsonStr() {
		return JSONUtil.toJsonStr(map);
	}

	@Benchmark
	public String hutoolToJsonStr() {
		return cn.hutool.json.JSONUtil.toJsonStr(map);
	}

	@Benchmark
	public Object sureParse() {
		return JSONUtil.parse(json);
	}

	@Benchmark
	public Object hutoolParse() {
		return cn.hutool.json.JSONUtil.parse(json);
	}
}
