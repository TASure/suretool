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
package com.sure.tool.concurrent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ClassFileUtil} 解析与生成测试（含边界与非法输入）。
 */
public class ClassFileUtilTest {

	/**
	 * 测试用目标类：实现 {@link Runnable}，覆盖接口解析分支。
	 */
	public static class TestTarget implements Runnable {

		@Override
		public void run() {
			// 测试目标，无行为
		}
	}

	/**
	 * 解析当前类：类名、父类、方法名与方法签名。
	 *
	 * @throws IOException 读取 class 资源失败
	 */
	@Test
	public void parseCurrentClass() throws IOException {
		byte[] bytes = resource(ClassFileUtil.class, "ClassFileUtil.class");
		ClassFileUtil.ClassInfo info = ClassFileUtil.parse(bytes);
		Assert.assertEquals("com.sure.tool.concurrent.ClassFileUtil", info.className());
		Assert.assertEquals("java.lang.Object", info.superName());
		Assert.assertTrue(info.interfaces().isEmpty());
		Assert.assertTrue(info.majorVersion() >= 65);
		Assert.assertTrue(ClassFileUtil.methodNames(bytes).contains("generate"));
		Assert.assertTrue(ClassFileUtil.methodSignatures(bytes).contains("parse([B)Lcom/sure/tool/concurrent/ClassFileUtil$ClassInfo;"));
	}

	/**
	 * 解析实现接口的类：接口列表按内部名转点分隔。
	 *
	 * @throws IOException 读取 class 资源失败
	 */
	@Test
	public void parseClassWithInterface() throws IOException {
		byte[] bytes = resource(TestTarget.class, "ClassFileUtilTest$TestTarget.class");
		ClassFileUtil.ClassInfo info = ClassFileUtil.parse(bytes);
		Assert.assertEquals("java.lang.Runnable", info.interfaces().get(0));
		Assert.assertTrue(ClassFileUtil.methodNames(bytes).contains("run"));
	}

	/**
	 * 重载方法在 methodNames 中保留重复项（ScopedValueUtil 有两个 where 重载）。
	 *
	 * @throws IOException 读取 class 资源失败
	 */
	@Test
	public void methodNamesKeepOverloads() throws IOException {
		byte[] bytes = resource(ScopedValueUtil.class, "ScopedValueUtil.class");
		List<String> names = ClassFileUtil.methodNames(bytes);
		Assert.assertEquals(2, names.stream().filter("where"::equals).count());
	}

	/**
	 * 从文件解析，并验证不存在文件抛 IOException。
	 *
	 * @throws IOException 文件操作失败
	 */
	@Test
	public void parseFileSuccessAndMissing() throws IOException {
		Path file = Files.createTempFile("sure-classfile-", ".class");
		try {
			Files.write(file, ClassFileUtil.generate("com.sure.tool.concurrent.Generated"));
			ClassFileUtil.ClassInfo info = ClassFileUtil.parseFile(file);
			Assert.assertEquals("com.sure.tool.concurrent.Generated", info.className());
		} finally {
			Files.deleteIfExists(file);
		}
		Path missing = file.getParent().resolve("sure-classfile-missing.class");
		try {
			ClassFileUtil.parseFile(missing);
			Assert.fail("预期 IOException");
		} catch (IOException expected) {
			// 预期路径
		}
	}

	/**
	 * 空字节与格式非法字节抛 IllegalArgumentException；null 抛 NullPointerException。
	 */
	@Test
	public void invalidBytesRejected() {
		Assert.assertThrows(NullPointerException.class, () -> ClassFileUtil.parse(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> ClassFileUtil.parse(new byte[0]));
		Assert.assertThrows(IllegalArgumentException.class, () -> ClassFileUtil.parse(new byte[] {1, 2, 3}));
		Assert.assertThrows(NullPointerException.class, () -> ClassFileUtil.methodNames(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> ClassFileUtil.methodNames(new byte[0]));
		Assert.assertThrows(IllegalArgumentException.class, () -> ClassFileUtil.methodSignatures(new byte[] {1}));
		Assert.assertThrows(NullPointerException.class, () -> ClassFileUtil.parseFile(null));
	}

	/**
	 * 生成空类：可被重新解析，含 public 构造器，父类为 Object。
	 */
	@Test
	public void generateEmptyClassRoundTrip() {
		byte[] bytes = ClassFileUtil.generate("com.sure.tool.concurrent.Generated");
		ClassFileUtil.ClassInfo info = ClassFileUtil.parse(bytes);
		Assert.assertEquals("com.sure.tool.concurrent.Generated", info.className());
		Assert.assertEquals("java.lang.Object", info.superName());
		Assert.assertTrue(info.majorVersion() > 0);
		Assert.assertTrue(ClassFileUtil.methodNames(bytes).contains("<init>"));
		Assert.assertTrue(ClassFileUtil.methodSignatures(bytes).contains("<init>()V"));
	}

	/**
	 * 非法二进制类名被拒绝。
	 */
	@Test
	public void invalidBinaryNamesRejected() {
		Assert.assertThrows(NullPointerException.class, () -> ClassFileUtil.generate(null));
		for (String name : List.of("", " ", "a/b", ".a", "a.", "a..b", "1abc", "a-b", "a b")) {
			Assert.assertThrows(name, IllegalArgumentException.class, () -> ClassFileUtil.generate(name));
		}
	}

	/**
	 * ClassInfo 的接口列表为不可变副本。
	 */
	@Test
	public void classInfoCopiesInterfaces() {
		List<String> mutable = new ArrayList<>();
		mutable.add("java.lang.Runnable");
		ClassFileUtil.ClassInfo info = new ClassFileUtil.ClassInfo("A", "B", 69, mutable);
		mutable.add("java.lang.Comparable");
		Assert.assertEquals(1, info.interfaces().size());
		Assert.assertThrows(UnsupportedOperationException.class, () -> info.interfaces().add("X"));
	}

	private static byte[] resource(Class<?> type, String name) throws IOException {
		try (InputStream in = type.getResourceAsStream(name)) {
			Assert.assertNotNull("缺少 class 资源: " + name, in);
			return in.readAllBytes();
		}
	}
}
