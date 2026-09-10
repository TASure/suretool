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
package com.sure.tool.lang;

import java.util.regex.Pattern;

/**
 * 常用正则表达式模式池，参考 Hutool 的 {@code PatternPool} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class PatternPool {

	/** 邮箱 */
	public static final Pattern EMAIL = Pattern.compile("^[\\w.%+-]+@[\\w-]+(\\.[\\w-]+)+$");

	/** 手机号（中国大陆） */
	public static final Pattern MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

	/** 18 位身份证 */
	public static final Pattern ID_CARD_18 = Pattern.compile("^\\d{17}[\\dXx]$");

	/** 15 位身份证 */
	public static final Pattern ID_CARD_15 = Pattern.compile("^\\d{15}$");

	/** IPv4 地址 */
	public static final Pattern IPV4 = Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

	/** URL */
	public static final Pattern URL = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

	/** 纯中文 */
	public static final Pattern CHINESE = Pattern.compile("^[\\u4e00-\\u9fa5]+$");

	/** 车牌号（含新能源，7-8 位） */
	public static final Pattern PLATE_NUMBER = Pattern.compile("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$");

	/** 邮政编码（中国大陆） */
	public static final Pattern POSTAL_CODE = Pattern.compile("^\\d{6}$");

	/** 金额（正数，最多两位小数） */
	public static final Pattern MONEY = Pattern.compile("^(0|[1-9]\\d*)(\\.\\d{1,2})?$");

	/** 通用单词（字母数字下划线） */
	public static final Pattern GENERAL = Pattern.compile("^[a-zA-Z0-9_]+$");

	/** UUID（含连字符） */
	public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	/** MAC 地址 */
	public static final Pattern MAC_ADDRESS = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$");

	private PatternPool() {
	}
}