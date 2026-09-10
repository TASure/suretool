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

/**
 * sure-examples 聚合入口：依次运行全部 Demo。
 *
 * <p>执行方式：{@code mvn -pl sure-examples exec:java}
 */
public class ExamplesRunner {

	/**
	 * 入口方法。
	 *
	 * @param args 命令行参数（忽略）
	 */
	public static void main(String[] args) throws Exception {
		StrUtilDemo.run();
		CollUtilDemo.run();
		DateUtilDemo.run();
		JsonDemo.run();
		CryptoDemo.run();
		HttpDemo.run();
		CacheDemo.run();
		CaptchaDemo.run();
		JwtDemo.run();
		DfaDemo.run();
		XmlDemo.run();
		PoiDemo.run();
		ThreadDemo.run();
		UserServiceDemo.run();
		System.out.println("=== 全部示例执行完成 ===");
	}
}
