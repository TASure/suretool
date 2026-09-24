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
package com.sure.tool.json;

/**
 * JSON 流式解析事件回调。通过 {@link StreamJsonParser} 解析大 JSON 时按事件顺序触发，
 * 由调用方自行维护当前上下文，从而避免将整个文档载入内存。
 *
 * @author suretool
 * @since 1.1.0
 */
public interface JsonHandler {

	/**
	 * 对象开始。
	 */
	default void onStartObject() {
	}

	/**
	 * 对象结束。
	 */
	default void onEndObject() {
	}

	/**
	 * 数组开始。
	 */
	default void onStartArray() {
	}

	/**
	 * 数组结束。
	 */
	default void onEndArray() {
	}

	/**
	 * 对象键（仅当键为字符串时触发）。
	 *
	 * @param key 键名
	 */
	default void onKey(String key) {
	}

	/**
	 * 字符串值。
	 *
	 * @param value 值（已解码转义）
	 */
	default void onString(String value) {
	}

	/**
	 * 数字值（保留原始字面量，由调用方决定精度）。
	 *
	 * @param raw 数字原始文本
	 */
	default void onNumber(String raw) {
	}

	/**
	 * 布尔值。
	 *
	 * @param value 布尔值
	 */
	default void onBoolean(boolean value) {
	}

	/**
	 * null 值。
	 */
	default void onNull() {
	}
}
