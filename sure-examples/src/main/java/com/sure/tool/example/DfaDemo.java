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

import com.sure.tool.dfa.DfaUtil;

/**
 * 敏感词过滤示例（DfaUtil）。
 */
public class DfaDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== DfaDemo ===");
		DfaUtil.addWords("赌博", "色情", "诈骗");
		String text = "打击赌博和诈骗行为，人人有责。";
		System.out.println("isMatch = " + DfaUtil.isMatch(text));
		System.out.println("matchAll = " + DfaUtil.matchAll(text));
		System.out.println("contains = " + DfaUtil.contains(text));
	}
}
