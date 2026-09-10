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

import com.sure.tool.util.StrUtil;

/**
 * 字符串工具示例（StrUtil）。
 */
public class StrUtilDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== StrUtilDemo ===");
		System.out.println("isEmpty(\"\") = " + StrUtil.isEmpty(""));
		System.out.println("isBlank(\"  \") = " + StrUtil.isBlank("  "));
		System.out.println("sub(\"hello world\", 0, 5) = " + StrUtil.sub("hello world", 0, 5));
		System.out.println("removeSuffixIgnoreCase(\"Hello.LLO\") = " + StrUtil.removeSuffixIgnoreCase("Hello", "LLO"));
		System.out.println("replace(\"hello\", \"el\", \"x\") = " + StrUtil.replace("hello", "el", "x"));
		System.out.println("cleanBlank(\"a b c\") = " + StrUtil.cleanBlank("a b c"));
		System.out.println("padPre(\"7\", 3, '0') = " + StrUtil.padPre("7", 3, '0'));
	}
}
