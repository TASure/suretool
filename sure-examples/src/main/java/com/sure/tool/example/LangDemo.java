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

import com.sure.tool.lang.Result;
import com.sure.tool.lang.Results;
import com.sure.tool.lang.NullUtil;
import com.sure.tool.lang.Option;

/**
 * 批32 示例：Result / Option / Results 显式错误门面用法。
 */
public class LangDemo {

	public static void main(String[] args) {
		// 1. 显式成功 / 失败，无异常开销
		Result<Integer> ok = Result.ok(42);
		Result<Integer> bad = Result.fail("服务不可用");
		System.out.println("ok=" + ok.get() + ", bad.isFail=" + bad.isFailure());

		// 2. 链式短路：失败后 map/onSuccess 不再执行
		Result<String> mapped = bad.map(v -> "值=" + v).onSuccess(v -> System.out.println("不会执行"));
		System.out.println("短路后 stillFail=" + mapped.isFailure());

		// 3. recover 兜底 + 抛出检查
		int value = bad.recover(err -> -1).get();
		System.out.println("recovered=" + value);

		// 4. 批量执行：Results.allOf 任一失败则聚合失败
		Result<java.util.List<Integer>> all = Results.allOf(
				Result.ok(1),
				Result.fail("第二项失败"),
				Result.ok(3));
		System.out.println("allOf=" + (all.isFailure() ? "聚合失败：" + all.getErrorMessage() : all.get()));

		// 5. NullUtil 安全调用 → Option
		String name = null;
		Option<String> opt = NullUtil.applyIfNotNull(name, s -> s.trim());
		System.out.println("null-safe option empty=" + opt.isEmpty());
	}
}
