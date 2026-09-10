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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sure.tool.json.JSONObject;
import com.sure.tool.json.JSONUtil;
import com.sure.tool.lang.Snowflake;
import com.sure.tool.util.StrUtil;
import com.sure.tool.util.ValidatorUtil;

/**
 * 综合示例：一个"用户注册 + 数据组装"的典型业务场景，
 * 串联 StrUtil / ValidatorUtil / Snowflake / JSONUtil。
 */
public class UserServiceDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== UserServiceDemo ===");
		Map<String, Object> form = new HashMap<>();
		form.put("username", " alice ");
		form.put("email", "alice@example.com");
		form.put("mobile", "13800138000");

		String username = StrUtil.cleanBlank((String) form.get("username"));
		boolean validEmail = ValidatorUtil.isEmail((String) form.get("email"));
		boolean validMobile = ValidatorUtil.isMobile((String) form.get("mobile"));

		Snowflake snowflake = new Snowflake(1, 1);
		long uid = snowflake.nextId();

		JSONObject user = new JSONObject();
		user.set("uid", uid);
		user.set("username", username);
		user.set("email", form.get("email"));
		user.set("mobile", form.get("mobile"));

		List<String> tags = Arrays.asList("新用户", "示例");
		Map<String, Object> payload = new HashMap<>();
		payload.put("user", user);
		payload.put("tags", tags);
		payload.put("valid", validEmail && validMobile);

		System.out.println("json = " + JSONUtil.toJsonStr(payload));
		System.out.println("isJson round-trip = " + JSONUtil.isJson(JSONUtil.toJsonStr(payload)));
	}
}
