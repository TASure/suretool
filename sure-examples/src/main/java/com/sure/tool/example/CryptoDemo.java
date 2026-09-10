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

import java.security.KeyPair;

import com.sure.tool.crypto.AesUtil;
import com.sure.tool.crypto.RsaUtil;
import com.sure.tool.crypto.SecureUtil;

/**
 * 加解密工具示例（AesUtil / RsaUtil / SecureUtil）。
 */
public class CryptoDemo {

	/**
	 * 运行示例。
	 */
	public static void run() {
		System.out.println("=== CryptoDemo ===");
		String key = AesUtil.generateKey();
		String hex = AesUtil.encryptHex("hello", key);
		System.out.println("aes encryptHex = " + hex);
		System.out.println("aes decryptHex = " + AesUtil.decryptHex(hex, key));
		System.out.println("md5(\"hello\") = " + SecureUtil.md5("hello"));
		System.out.println("sha256(\"hello\") = " + SecureUtil.sha256("hello"));
		KeyPair pair = RsaUtil.generateKeyPair();
		String rsaHex = RsaUtil.encryptHex("rsa-data", pair.getPublic());
		System.out.println("rsa decryptHex = " + RsaUtil.decryptHex(rsaHex, pair.getPrivate()));
	}
}
