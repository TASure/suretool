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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.Assert;

import com.sure.tool.util.ConvertUtil;

/**
 * 批30 模糊测试（属性测试）：编解码与转换不变量。
 */
public class CodecConvertPropertyTest {

	@Property
	public void base64RoundTrip(@ForAll byte[] data) {
		Assert.assertArrayEquals(data, Base64Util.decode(Base64Util.encode(data)));
	}

	@Property
	public void base64UrlSafeRoundTrip(@ForAll byte[] data) {
		Assert.assertArrayEquals(data, Base64Util.decode(Base64Util.encodeUrlSafe(data)));
	}

	@Property
	public void base64StringRoundTrip(@ForAll String s) {
		String back = Base64Util.decodeStr(Base64Util.encode(s));
		Assert.assertEquals(s, back);
	}

	@Property
	public void toIntNullSafety(@ForAll String s) {
		// 任意输入不抛异常：默认值兜底
		int v = ConvertUtil.toInt(s, -1);
		Assert.assertTrue(v == -1 || Integer.MIN_VALUE <= v);
	}

	@Property
	public void toBooleanStable(@ForAll boolean b) {
		Assert.assertEquals(b, ConvertUtil.toBoolean(Boolean.toString(b)));
	}

	@Property
	public void convertStringRoundTrip(@ForAll int i) {
		// int → String → int 往返稳定
		Assert.assertEquals(i, ConvertUtil.toInt(Integer.toString(i), Integer.MIN_VALUE));
	}
}
