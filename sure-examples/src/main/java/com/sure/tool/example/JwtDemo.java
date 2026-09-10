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

import java.util.HashMap;
import java.util.Map;

import com.sure.tool.json.JSONObject;
import com.sure.tool.jwt.JwtUtil;

/**
 * JWT 示例（JwtUtil）。
 */
public class JwtDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== JwtDemo ===");
		Map<String, Object> claims = new HashMap<>();
		claims.put("uid", 10001);
		claims.put("role", "admin");
		String token = JwtUtil.createToken(claims, "secret-key", 3600);
		System.out.println("token = " + token);
		System.out.println("verify = " + JwtUtil.verify(token, "secret-key"));
		JSONObject parsed = JwtUtil.parseToken(token, "secret-key");
		System.out.println("parsed.uid = " + parsed.get("uid"));
	}
}
