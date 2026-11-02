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
package com.sure.tool.codec;

/**
 * ROT 轮转密码工具。
 *
 * <p>ROT-N 是凯撒密码的特例（字母轮转 N 位，常见值：13、47）。
 * ROT-13 专为英文字母设计（自身即逆操作）；ROT-47 作用于 33–126 可打印 ASCII 字符，
 * 覆盖数字、标点、大小写字母，适合邮件主题、Usenet 隐藏文本等场景。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * String rot13 = RotUtil.rot13("Hello, World!");   // "Uryyb, Jbeyq!"
 * String rot47 = RotUtil.rot47("Hello, World!");   // "w6==@ &amp;=5yC!"
 * String self  = RotUtil.rot13(RotUtil.rot13("x")); // "x"（自身即逆）
 * }</pre>
 *
 * <p>null 语义：入参为 {@code null} 时返回 {@code null}（空安全）。</p>
 *
 * @author suretool
 * @since 1.21.0
 */
public final class RotUtil {

	/** ROT-13 字母表长度 */
	private static final int ROT13_SHIFT = 13;
	/** ROT-47 可打印 ASCII 起始码点 */
	private static final int ROT47_START = 33;
	/** ROT-47 可打印 ASCII 覆盖长度（33–126 共 94 字符） */
	private static final int ROT47_LEN = 94;
	/** ROT-47 位移量（自身即逆） */
	private static final int ROT47_SHIFT = 47;

	private RotUtil() {
	}

	/**
	 * ROT-13 编码/解码（自身互逆）。
	 *
	 * @param text 原文，可为 {@code null}
	 * @return 编码后的文本
	 */
	public static String rot13(String text) {
		return CaesarUtil.encode(text, ROT13_SHIFT);
	}

	/**
	 * ROT-47 编码/解码（自身互逆）。
	 *
	 * <p>作用于 ASCII 33 ({@code !}) 到 126 ({@code ~}) 共 94 个可打印字符，
	 * 空白、tab、换行等原样保留。</p>
	 *
	 * @param text 原文，可为 {@code null}
	 * @return 编码后的文本
	 */
	public static String rot47(String text) {
		if (text == null) {
			return null;
		}
		char[] out = new char[text.length()];
		for (int i = 0; i < out.length; i++) {
			int c = text.charAt(i);
			if (c >= ROT47_START && c <= 126) {
				out[i] = (char) (((c - ROT47_START + ROT47_SHIFT) % ROT47_LEN) + ROT47_START);
			} else {
				out[i] = (char) c;
			}
		}
		return new String(out);
	}
}
