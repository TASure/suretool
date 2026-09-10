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

import com.sure.tool.thread.ThreadUtil;

/**
 * 并发工具示例（ThreadUtil / ExecutorBuilder）。
 */
public class ThreadDemo {

	/**
	 * 运行示例。
	 */
	public static void run() throws Exception {
		System.out.println("=== ThreadDemo ===");
		java.util.concurrent.Future<String> future = ThreadUtil.execAsync(() -> "done");
		System.out.println("execAsync result = " + future.get());
		ThreadUtil.sleep(10);
		System.out.println("sleep ok");
		System.out.println("processors = " + ThreadUtil.getProcessorCount());
	}
}
