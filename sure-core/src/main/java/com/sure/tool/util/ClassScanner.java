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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描工具：按包名扫描 classpath 中的类，支持注解 / 父类 / 自定义谓词过滤。
 *
 * <p>参考 Hutool {@code ClassScanner} 设计，覆盖 classpath 目录与 jar 两类来源，
 * 类加载失败（LinkageError / ClassNotFoundException 等）自动跳过，不中断扫描。</p>
 *
 * @author suretool
 * @since 1.18.0
 */
public class ClassScanner {

	private ClassScanner() {
	}

	/**
	 * 扫描指定包及其子包下的全部类（不含内部类 / 匿名类）。
	 *
	 * @param packageName 包名（如 {@code com.sure.tool.util}），null 或不存在时返回空集
	 * @return 类集合（去重）
	 */
	public static Set<Class<?>> scanPackage(String packageName) {
		return scanPackage(packageName, c -> true);
	}

	/**
	 * 按谓词过滤扫描指定包及其子包下的类。
	 *
	 * @param packageName 包名，null 或不存在时返回空集
	 * @param filter      过滤谓词，null 视为全部通过
	 * @return 过滤后的类集合
	 */
	public static Set<Class<?>> scanPackage(String packageName, Predicate<Class<?>> filter) {
		if (packageName == null || packageName.isEmpty()) {
			return Collections.emptySet();
		}
		Predicate<Class<?>> actual = filter == null ? c -> true : filter;
		Set<Class<?>> result = new HashSet<>();
		String path = packageName.replace('.', '/');
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if (loader == null) {
				loader = ClassScanner.class.getClassLoader();
			}
			Enumeration<URL> resources = loader.getResources(path);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				collect(url, path, packageName, actual, result, loader);
			}
		} catch (IOException e) {
			// 资源枚举失败不中断，返回已收集部分
		}
		return result;
	}

	/**
	 * 扫描指定包下标注了指定注解的类（含子包）。
	 *
	 * @param packageName 包名
	 * @param annotation  注解类型，null 抛 {@link IllegalArgumentException}
	 * @return 标注类集合
	 */
	public static Set<Class<?>> scanPackageByAnnotation(String packageName,
			Class<? extends java.lang.annotation.Annotation> annotation) {
		if (annotation == null) {
			throw new IllegalArgumentException("annotation must not be null");
		}
		return scanPackage(packageName, c -> c.isAnnotationPresent(annotation));
	}

	/**
	 * 扫描指定包下指定父类 / 接口的实现类（含子包）。
	 *
	 * <p>排除父类 / 接口自身与 Object；{@code superClass} 为 null 时返回空集。</p>
	 *
	 * @param packageName 包名
	 * @param superClass  父类或接口
	 * @return 实现类集合
	 */
	public static Set<Class<?>> scanPackageBySuper(String packageName, Class<?> superClass) {
		if (superClass == null) {
			return Collections.emptySet();
		}
		return scanPackage(packageName,
				c -> c != superClass && superClass.isAssignableFrom(c) && !c.isInterface() && !Object.class.equals(c));
	}

	private static void collect(URL url, String path, String packageName, Predicate<Class<?>> filter,
			Set<Class<?>> result, ClassLoader loader) throws IOException {
		String protocol = url.getProtocol();
		if ("file".equals(protocol)) {
			File dir = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
			if (dir.isDirectory()) {
				collectFromDir(dir, packageName, filter, result, loader);
			} else if (dir.getName().toLowerCase().endsWith(".jar")) {
				collectFromJar(dir, packageName, filter, result, loader);
			}
		} else if ("jar".equals(protocol)) {
			String file = url.getFile();
			int bang = file.indexOf('!');
			if (bang > 0) {
				File jar = new File(URLDecoder.decode(file.substring(0, bang), StandardCharsets.UTF_8)
						.replace("file:", ""));
				collectFromJar(jar, packageName, filter, result, loader);
			}
		}
	}

	private static void collectFromDir(File dir, String packageName, Predicate<Class<?>> filter,
			Set<Class<?>> result, ClassLoader loader) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				collectFromDir(f, packageName + "." + f.getName(), filter, result, loader);
			} else if (f.getName().endsWith(".class")) {
				String className = packageName + "." + f.getName().substring(0, f.getName().length() - 6);
				loadAndAdd(className, filter, result, loader);
			}
		}
	}

	private static void collectFromJar(File jarFile, String packageName, Predicate<Class<?>> filter,
			Set<Class<?>> result, ClassLoader loader) {
		if (!jarFile.isFile()) {
			return;
		}
		String prefix = packageName.replace('.', '/') + "/";
		try (JarFile jar = new JarFile(jarFile)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (!name.startsWith(prefix) || !name.endsWith(".class")) {
					continue;
				}
				String className = name.substring(0, name.length() - 6).replace('/', '.');
				if (className.contains("$")) {
					continue;
				}
				loadAndAdd(className, filter, result, loader);
			}
		} catch (IOException e) {
			// 单个 jar 读取失败跳过
		}
	}

	private static void loadAndAdd(String className, Predicate<Class<?>> filter, Set<Class<?>> result,
			ClassLoader loader) {
		try {
			Class<?> clazz = Class.forName(className, false, loader);
			if (clazz.isLocalClass() || clazz.isAnonymousClass()) {
				return;
			}
			if (filter.test(clazz)) {
				result.add(clazz);
			}
		} catch (LinkageError | ClassNotFoundException e) {
			// 加载失败跳过，不中断扫描（NoClassDefFoundError 为 LinkageError 子类）
		}
	}
}
