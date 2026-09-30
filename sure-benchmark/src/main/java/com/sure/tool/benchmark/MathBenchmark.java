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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import com.sure.tool.math.BigDecimalUtil;
import com.sure.tool.math.MathUtil;
import com.sure.tool.math.RandomUtil;

/**
 * 数学/数值/随机工具基准：suretool 与 Hutool/原生实现对拍。
 *
 * <p>门禁配对项：sureMathIsPrime ↔ hutoolMathIsPrime、sureRandomString ↔ hutoolRandomString；
 * 其余项仅作展示。</p>
 */
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MathBenchmark {

	private final int primeCandidate = 104729;
	private final String randomText = "suretool-benchmark-input-string-2026";

	@Benchmark
	public boolean sureMathIsPrime() {
		return MathUtil.isPrime(primeCandidate);
	}

	@Benchmark
	public boolean hutoolMathIsPrime() {
		return cn.hutool.core.util.NumberUtil.isPrimes(primeCandidate);
	}

	@Benchmark
	public long sureMathGcd() {
		return MathUtil.gcd(1071, 462);
	}

	@Benchmark
	public BigDecimal sureBigDecimalRound() {
		return BigDecimalUtil.roundHalfUp(new BigDecimal("1234.56789"), 2);
	}

	@Benchmark
	public BigDecimal plainBigDecimalRound() {
		return new BigDecimal("1234.56789").setScale(2, java.math.RoundingMode.HALF_UP);
	}

	@Benchmark
	public String sureRandomString() {
		return RandomUtil.randomString(randomText.length());
	}

	@Benchmark
	public String hutoolRandomString() {
		return cn.hutool.core.util.RandomUtil.randomString(randomText.length());
	}

	@Benchmark
	public List<Integer> sureMathSieve() {
		return MathUtil.primes(1000);
	}
}
