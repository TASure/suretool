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
package com.sure.tool.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * {@link ClassScanner} 测试：目录扫描、jar 扫描、注解 / 父类过滤。
 */
public class ClassScannerBatch37Test {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** 测试用注解。 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Marker {
	}

	/** 带注解的标记类。 */
	@Marker
	public static class Annotated {
	}

	/** 普通类。 */
	public static class Plain {
	}

	/** 测试父接口。 */
	public interface Base {
	}

	/** 接口实现类。 */
	public static class Impl implements Base {
	}

	/** 内部类（应被跳过）。 */
	public static class Inner {
	}

	@Test
	public void 扫描包包含本类与测试类() {
		Set<Class<?>> classes = ClassScanner.scanPackage("com.sure.tool.util");
		assertTrue(classes.contains(ClassScannerBatch37Test.class));
		assertTrue(classes.contains(TypeUtil.class));
		assertTrue(classes.contains(ClassScanner.class));
	}

	@Test
	public void 过滤只取本包测试类() {
		Set<Class<?>> classes = ClassScanner.scanPackage("com.sure.tool.util",
				c -> c.getName().startsWith("com.sure.tool.util.TypeUtil"));
		assertTrue(classes.contains(TypeUtil.class));
		assertFalse(classes.contains(ClassScanner.class));
	}

	@Test
	public void 按注解扫描() {
		Set<Class<?>> classes = ClassScanner.scanPackageByAnnotation("com.sure.tool.util", Marker.class);
		assertTrue(classes.contains(Annotated.class));
		assertFalse(classes.contains(Plain.class));
	}

	@Test
	public void 按父类扫描排除接口自身() {
		Set<Class<?>> classes = ClassScanner.scanPackageBySuper("com.sure.tool.util", Base.class);
		assertTrue(classes.contains(Impl.class));
		assertFalse(classes.contains(Base.class));
	}

	@Test
	public void null参数安全() {
		assertTrue(ClassScanner.scanPackage(null).isEmpty());
		assertTrue(ClassScanner.scanPackage("no.such.package.xyz").isEmpty());
		assertTrue(ClassScanner.scanPackageBySuper("com.sure.tool.util", null).isEmpty());
		assertTrue(ClassScanner.scanPackage("com.sure.tool.util", null).contains(ClassScanner.class));
		assertThrows(IllegalArgumentException.class,
				() -> ClassScanner.scanPackageByAnnotation("com.sure.tool.util", null));
	}

	@Test
	public void 扫描core包全部类非空() {
		Set<Class<?>> classes = ClassScanner.scanPackage("com.sure.tool");
		assertFalse(classes.isEmpty());
		assertTrue(classes.size() > 100);
	}

	/**
	 * jar 扫描分支：file: 协议 jar、jar: 协议、$ 嵌套类跳过、损坏字节触发 LinkageError 跳过、
	 * 不存在的 jar 触发 IOException 跳过。通过反射驱动私有 {@code collect} 覆盖。
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void jar扫描与损坏类跳过() throws Exception {
		File jarFile = tmp.newFile("scan-test.jar");
		byte[] bytes = readResource("com/sure/tool/util/ClassScannerBatch37Test.class");
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
			jos.putNextEntry(new JarEntry("com/sure/tool/util/ClassScannerBatch37Test.class"));
			jos.write(bytes);
			jos.closeEntry();
			// $ 嵌套类：collectFromJar 的 $ 过滤分支（不加载，字节可不匹配）
			jos.putNextEntry(new JarEntry("com/sure/tool/util/ClassScannerBatch37Test$Inner.class"));
			jos.write(bytes);
			jos.closeEntry();
			// 损坏字节：forName 抛 ClassFormatError（LinkageError）→ loadAndAdd catch
			jos.putNextEntry(new JarEntry("com/sure/tool/util/Bad.class"));
			jos.write(new byte[] {1, 2, 3, 4, 5});
			jos.closeEntry();
		}
		Method collect = ClassScanner.class.getDeclaredMethod("collect", URL.class, String.class,
				String.class, Predicate.class, Set.class, ClassLoader.class);
		collect.setAccessible(true);
		Set<Class<?>> result = new HashSet<>();
		Predicate<Class<?>> all = c -> true;
		// 用包含 jar 的 URLClassLoader，使 jar 内类可被 forName 加载
		try (java.net.URLClassLoader jarLoader = new java.net.URLClassLoader(
				new URL[] {jarFile.toURI().toURL()}, ClassScanner.class.getClassLoader())) {
			// file: 协议 + .jar 后缀（else if 分支）
			collect.invoke(null, jarFile.toURI().toURL(), "com/sure/tool/util", "com.sure.tool.util",
					all, result, jarLoader);
			// jar: 协议分支
			collect.invoke(null, new URL("jar:" + jarFile.toURI().toURL() + "!/"), "com/sure/tool/util",
					"com.sure.tool.util", all, result, jarLoader);
			// 不存在的 jar → collectFromJar 的 IOException catch
			collect.invoke(null, new URL("jar:file:/tmp/definitely-not-exist-" + System.nanoTime() + ".jar!/"),
					"com/sure/tool/util", "com.sure.tool.util", all, result, jarLoader);
		}

		// 顶层测试类被加载（jarLoader 父委托命中，同一 Class 对象）
		assertTrue("jar 类未加载, size=" + result.size(),
				result.stream().anyMatch(c -> c.getName().equals(ClassScannerBatch37Test.class.getName())));
		// $ 嵌套类与损坏类均被跳过
		assertFalse(result.stream().anyMatch(c -> c.getName().contains("$")));
		assertFalse(result.stream().anyMatch(c -> c.getName().endsWith("Bad")));
	}

	private static byte[] readResource(String path) throws IOException {
		try (InputStream in = ClassScannerBatch37Test.class.getClassLoader().getResourceAsStream(path)) {
			if (in == null) {
				throw new IOException("resource not found: " + path);
			}
			return in.readAllBytes();
		}
	}

	/**
	 * 兜底与异常分支：ContextClassLoader 为 null 时回退自身加载器；
	 * 资源枚举抛 IOException 时返回已收集部分（空集）。
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void contextClassLoader为空与资源枚举失败() throws Exception {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(null);
			assertTrue(ClassScanner.scanPackage("com.sure.tool.util").contains(ClassScanner.class));
			ClassLoader throwing = new ClassLoader(original) {
				@Override
				public java.util.Enumeration<URL> getResources(String name) throws IOException {
					throw new IOException("boom");
				}
			};
			Thread.currentThread().setContextClassLoader(throwing);
			assertTrue(ClassScanner.scanPackage("com.sure.tool.util").isEmpty());
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	/** jar 中仅含 $ 嵌套类时全部被过滤跳过。 */
	@Test
	@SuppressWarnings("unchecked")
	public void jar仅含嵌套类被跳过() throws Exception {
		File jarFile = tmp.newFile("nested-only.jar");
		byte[] bytes = readResource("com/sure/tool/util/ClassScannerBatch37Test.class");
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
			jos.putNextEntry(new JarEntry("com/sure/tool/util/Outer$Inner.class"));
			jos.write(bytes);
			jos.closeEntry();
		}
		Method collect = ClassScanner.class.getDeclaredMethod("collect", URL.class, String.class,
				String.class, Predicate.class, Set.class, ClassLoader.class);
		collect.setAccessible(true);
		Set<Class<?>> result = new HashSet<>();
		try (java.net.URLClassLoader l = new java.net.URLClassLoader(
				new URL[] {jarFile.toURI().toURL()}, ClassScanner.class.getClassLoader())) {
			collect.invoke(null, jarFile.toURI().toURL(), "com/sure/tool/util", "com.sure.tool/util",
					(Predicate<Class<?>>) c -> true, result, l);
		}
		assertTrue(result.isEmpty());
	}

	/** 不可读目录：listFiles 返回 null 时静默返回空集。 */
	@Test
	@SuppressWarnings("unchecked")
	public void 不可读目录返回空集() throws Exception {
		File dir = tmp.newFolder("noperm");
		dir.setReadable(false, false);
		dir.setExecutable(false, false);
		if (dir.canRead()) {
			return; // 环境（如 root）无视权限位时跳过
		}
		Method collect = ClassScanner.class.getDeclaredMethod("collect", URL.class, String.class,
				String.class, Predicate.class, Set.class, ClassLoader.class);
		collect.setAccessible(true);
		Set<Class<?>> result = new HashSet<>();
		collect.invoke(null, dir.toURI().toURL(), "com/sure/tool/util/noperm", "com.sure.tool.util.noperm",
				(Predicate<Class<?>>) c -> true, result, ClassScanner.class.getClassLoader());
		assertTrue(result.isEmpty());
	}
}
