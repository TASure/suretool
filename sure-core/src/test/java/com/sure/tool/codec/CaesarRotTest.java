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

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 53a：凯撒密码与 ROT 轮转密码测试。
 *
 * @author suretool
 * @since 1.21.0
 */
public class CaesarRotTest {

	// ---------------- CaesarUtil ----------------

	@Test
	public void testCaesarEncode() {
		Assert.assertEquals("Khoor, Zruog!", CaesarUtil.encode("Hello, World!", 3));
		Assert.assertEquals("KHOOR, ZRUOG!", CaesarUtil.encode("HELLO, WORLD!", 3));
		Assert.assertEquals("fhergbby", CaesarUtil.encode("suretool", 13));
	}

	@Test
	public void testCaesarDecode() {
		Assert.assertEquals("Hello, World!", CaesarUtil.decode("Khoor, Zruog!", 3));
		Assert.assertEquals("suretool", CaesarUtil.decode("fhergbby", 13));
	}

	@Test
	public void testCaesarRoundtrip() {
		String original = "SureTool 2026! 测试, 中文 Hello World.";
		String encoded = CaesarUtil.encode(original, 7);
		Assert.assertEquals(original, CaesarUtil.decode(encoded, 7));
	}

	@Test
	public void testCaesarZeroShift() {
		Assert.assertEquals("Hello", CaesarUtil.encode("Hello", 0));
	}

	@Test
	public void testCaesarNegativeShift() {
		Assert.assertEquals("Hello, World!", CaesarUtil.encode("Khoor, Zruog!", -3));
	}

	@Test
	public void testCaesarNull() {
		Assert.assertNull(CaesarUtil.encode(null, 3));
		Assert.assertNull(CaesarUtil.decode(null, 3));
	}

	@Test
	public void testCaesarNonLetters() {
		Assert.assertEquals("!@#$%^&*()", CaesarUtil.encode("!@#$%^&*()", 5));
		Assert.assertEquals("1234567890", CaesarUtil.encode("1234567890", 10));
	}

	// ---------------- RotUtil ----------------

	@Test
	public void testRot13() {
		Assert.assertEquals("Uryyb, Jbeyq!", RotUtil.rot13("Hello, World!"));
		Assert.assertEquals("SOS", RotUtil.rot13("FBF"));
	}

	@Test
	public void testRot13SelfInverse() {
		Assert.assertEquals("Hello World", RotUtil.rot13(RotUtil.rot13("Hello World")));
		Assert.assertEquals("suretool", RotUtil.rot13(RotUtil.rot13("suretool")));
	}

	@Test
	public void testRot47() {
		String encoded = RotUtil.rot47("Hello, World!");
		Assert.assertNotEquals("Hello, World!", encoded);
		Assert.assertEquals("Hello, World!", RotUtil.rot47(encoded));
	}

	@Test
	public void testRot47SelfInverse() {
		Assert.assertEquals("suretool v1.21.0", RotUtil.rot47(RotUtil.rot47("suretool v1.21.0")));
		Assert.assertEquals("!@#$%", RotUtil.rot47(RotUtil.rot47("!@#$%")));
	}

	@Test
	public void testRot47PreservesNewlines() {
		String multi = "Hello\nWorld\n123";
		Assert.assertEquals(multi, RotUtil.rot47(RotUtil.rot47(multi)));
	}

	@Test
	public void testRotNull() {
		Assert.assertNull(RotUtil.rot13(null));
		Assert.assertNull(RotUtil.rot47(null));
	}
}
