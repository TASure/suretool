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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JDK 序列化工具类：对象与字节互转、文件读写，零依赖。
 * <p>
 * 注意：对象必须实现 {@link Serializable}；跨 JVM 或类版本变更时反序列化可能失败。
 *
 * @author suretool
 * @since 0.2.0
 */
public class SerializeUtil {

	private SerializeUtil() {
	}

	/**
	 * 对象序列化为字节数组。
	 *
	 * @param obj 对象（必须可序列化）
	 * @return 字节数组
	 * @throws IOException IO 异常
	 */
	public static byte[] serialize(Serializable obj) throws IOException {
		if (obj == null) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(256);
		try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(obj);
		}
		return out.toByteArray();
	}

	/**
	 * 字节数组反序列化为对象。
	 *
	 * @param data 字节数组
	 * @param <T>  对象类型
	 * @return 对象
	 * @throws IOException             IO 异常
	 * @throws ClassNotFoundException 类不存在
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] data) throws IOException, ClassNotFoundException {
		if (data == null || data.length == 0) {
			return null;
		}
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
			return (T) ois.readObject();
		}
	}

	/**
	 * 序列化到文件。
	 *
	 * @param obj  对象
	 * @param path 目标文件
	 * @throws IOException IO 异常
	 */
	public static void write(Serializable obj, Path path) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
			oos.writeObject(obj);
		}
	}

	/**
	 * 从文件反序列化。
	 *
	 * @param path 文件
	 * @param <T>  对象类型
	 * @return 对象
	 * @throws IOException             IO 异常
	 * @throws ClassNotFoundException 类不存在
	 */
	@SuppressWarnings("unchecked")
	public static <T> T read(Path path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			return (T) ois.readObject();
		}
	}

	/**
	 * 基于序列化的深拷贝（对象须可序列化）。
	 *
	 * @param obj 源对象
	 * @param <T> 类型
	 * @return 深拷贝
	 * @throws IOException IO 异常
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T clone(T obj) throws IOException {
		if (obj == null) {
			return null;
		}
		byte[] data = serialize(obj);
		try {
			return (T) deserialize(data);
		} catch (ClassNotFoundException e) {
			throw new IOException("序列化深拷贝失败: 类不存在 " + obj.getClass().getName(), e);
		}
	}
}
