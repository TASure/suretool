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

import java.util.Date;

import com.sure.tool.date.DateUnit;
import com.sure.tool.date.DateUtil;

/**
 * 日期工具示例（DateUtil）。
 */
public class DateUtilDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== DateUtilDemo ===");
		Date now = DateUtil.now();
		System.out.println("now = " + now);
		System.out.println("format(now) = " + DateUtil.format(now));
		System.out.println("formatDate(now) = " + DateUtil.formatDate(now));
		System.out.println("formatTime(now) = " + DateUtil.formatTime(now));
		System.out.println("parse = " + DateUtil.parse("2026-01-01 00:00:00"));
		System.out.println("between(now, now+1h, MINUTE) = "
				+ DateUtil.between(now, new Date(now.getTime() + 3600_000L), DateUnit.MINUTE));
	}
}
