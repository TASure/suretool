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
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class-File API（JDK 24 正式 API）封装：解析 class 文件的类名/父类/版本/接口与方法，并生成空类。
 *
 * <p>相比 ASM 等第三方字节码库，零依赖且不受 JDK 内部实现变更影响；适合启动期扫描、
 * 构建工具与运行期元数据读取。
 *
 * <p>示例：
 * <pre>{@code
 * byte[] bytes = ClassFileUtil.generate("com.sure.tool.demo.Generated");
 * ClassFileUtil.ClassInfo info = ClassFileUtil.parse(bytes);
 * // info.className() = "com.sure.tool.demo.Generated"，superName = "java.lang.Object"
 * }</pre>
 *
 * @since 1.20.0
 */
public final class ClassFileUtil {

	private static final ClassFile CLASS_FILE = ClassFile.of();

	private ClassFileUtil() {
	}

	/**
	 * class 文件元信息。
	 *
	 * @param className  二进制类名（点分隔，如 {@code java.lang.String}）
	 * @param superName  父类二进制类名；{@code java.lang.Object} 或模块描述符类为 null
	 * @param majorVersion class 文件主版本号（JDK 25 为 69）
	 * @param interfaces 直接实现的接口二进制类名（不可变）
	 */
	public record ClassInfo(String className, String superName, int majorVersion, List<String> interfaces) {

		/**
		 * 规范化接口列表为不可变副本。
		 */
		public ClassInfo {
			interfaces = List.copyOf(interfaces);
		}
	}

	/**
	 * 解析 class 字节。
	 *
	 * @param classBytes class 文件字节，不可为 null 且不可为空
	 * @return 元信息
	 * @throws NullPointerException     classBytes 为 null
	 * @throws IllegalArgumentException classBytes 为空或格式非法
	 */
	public static ClassInfo parse(byte[] classBytes) {
		return toInfo(parseModel(classBytes));
	}

	/**
	 * 读取并解析 class 文件。
	 *
	 * @param path class 文件路径，不可为 null
	 * @return 元信息
	 * @throws IOException              读取失败
	 * @throws NullPointerException     path 为 null
	 * @throws IllegalArgumentException 文件为空或格式非法
	 */
	public static ClassInfo parseFile(Path path) throws IOException {
		Objects.requireNonNull(path, "path must not be null");
		return parse(Files.readAllBytes(path));
	}

	/**
	 * 列出 class 中声明的方法名（保留重载导致的重复项）。
	 *
	 * @param classBytes class 文件字节，不可为 null 且不可为空
	 * @return 方法名列表（不可变，按 class 文件声明顺序）
	 * @throws NullPointerException     classBytes 为 null
	 * @throws IllegalArgumentException classBytes 为空或格式非法
	 */
	public static List<String> methodNames(byte[] classBytes) {
		List<String> names = new ArrayList<>();
		for (var method : parseModel(classBytes).methods()) {
			names.add(method.methodName().stringValue());
		}
		return List.copyOf(names);
	}

	/**
	 * 列出 class 中声明的方法签名（{@code 方法名+描述符}，如 {@code run()V}）。
	 *
	 * @param classBytes class 文件字节，不可为 null 且不可为空
	 * @return 方法签名列表（不可变，按 class 文件声明顺序）
	 * @throws NullPointerException     classBytes 为 null
	 * @throws IllegalArgumentException classBytes 为空或格式非法
	 */
	public static List<String> methodSignatures(byte[] classBytes) {
		List<String> signatures = new ArrayList<>();
		for (var method : parseModel(classBytes).methods()) {
			signatures.add(method.methodName().stringValue() + method.methodType().stringValue());
		}
		return List.copyOf(signatures);
	}

	/**
	 * 生成一个仅含 public 无参构造器的空类。
	 *
	 * @param binaryName 二进制类名（点分隔，如 {@code com.sure.tool.demo.Generated}）
	 * @return class 文件字节
	 * @throws NullPointerException     binaryName 为 null
	 * @throws IllegalArgumentException binaryName 为空、含非法字符或非法路径分隔符
	 */
	public static byte[] generate(String binaryName) {
		ClassDesc classDesc = parseBinaryName(binaryName);
		return CLASS_FILE.build(classDesc, classBuilder -> classBuilder
				.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER)
				.withMethod(ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC,
						methodBuilder -> methodBuilder.withCode(codeBuilder -> codeBuilder
								.aload(0)
								.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME,
										MethodTypeDesc.of(ConstantDescs.CD_void))
								.return_())));
	}

	private static ClassModel parseModel(byte[] classBytes) {
		Objects.requireNonNull(classBytes, "classBytes must not be null");
		if (classBytes.length == 0) {
			throw new IllegalArgumentException("classBytes must not be empty");
		}
		return CLASS_FILE.parse(classBytes);
	}

	private static ClassInfo toInfo(ClassModel model) {
		String className = model.thisClass().asInternalName().replace('/', '.');
		String superName = model.superclass()
				.map(superClass -> superClass.asInternalName().replace('/', '.'))
				.orElse(null);
		List<String> interfaces = new ArrayList<>();
		model.interfaces().forEach(iface -> interfaces.add(iface.asInternalName().replace('/', '.')));
		return new ClassInfo(className, superName, model.majorVersion(), interfaces);
	}

	private static ClassDesc parseBinaryName(String binaryName) {
		Objects.requireNonNull(binaryName, "binaryName must not be null");
		if (binaryName.isBlank() || binaryName.contains("/") || binaryName.startsWith(".")
				|| binaryName.endsWith(".") || binaryName.contains("..")) {
			throw new IllegalArgumentException("非法二进制类名: " + binaryName);
		}
		String[] segments = binaryName.split("\\.", -1);
		for (String segment : segments) {
			if (segment.isEmpty() || !Character.isJavaIdentifierStart(segment.charAt(0))) {
				throw new IllegalArgumentException("非法二进制类名: " + binaryName);
			}
			for (int i = 1; i < segment.length(); i++) {
				if (!Character.isJavaIdentifierPart(segment.charAt(i))) {
					throw new IllegalArgumentException("非法二进制类名: " + binaryName);
				}
			}
		}
		return ClassDesc.of(binaryName);
	}
}
