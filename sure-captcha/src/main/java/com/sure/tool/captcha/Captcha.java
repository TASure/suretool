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
package com.sure.tool.captcha;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 验证码接口，参考 Hutool 的 {@code Captcha} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public interface Captcha {

	/**
	 * 获取验证码文本。
	 *
	 * @return 验证码文本
	 */
	String getCode();

	/**
	 * 校验用户输入（忽略大小写）。
	 *
	 * @param userInputCode 用户输入
	 * @return 是否匹配
	 */
	boolean verify(String userInputCode);

	/**
	 * 输出验证码图片（PNG 格式）。
	 *
	 * @param out 输出流
	 * @throws IOException 输出失败
	 */
	void write(OutputStream out) throws IOException;

	/**
	 * 获取验证码图片字节（PNG 格式）。
	 *
	 * @return 图片字节
	 */
	byte[] getImageBytes();

	/**
	 * 获取 data URI（data:image/png;base64,...），便于直接嵌入页面。
	 *
	 * @return data URI
	 */
	String getImageBase64();
}
