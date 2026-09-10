package com.sure.tool.io;

import com.sure.tool.collection.CollUtil;
import com.sure.tool.util.StrUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 文件工具类，参考 Hutool 的 {@code FileUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class FileUtil {

	private FileUtil() {
	}

	/**
	 * 拼接路径为 File。
	 *
	 * @param pathParts 路径片段
	 * @return File
	 */
	public static File file(String... pathParts) {
		if (pathParts == null || pathParts.length == 0) {
			return null;
		}
		if (pathParts.length == 1) {
			return new File(pathParts[0]);
		}
		Path path = Paths.get(pathParts[0], java.util.Arrays.copyOfRange(pathParts, 1, pathParts.length));
		return path.toFile();
	}

	/**
	 * 文件或目录是否存在。
	 *
	 * @param file 文件
	 * @return 是否存在
	 */
	public static boolean exists(File file) {
		return file != null && file.exists();
	}

	/**
	 * 文件或目录是否存在。
	 *
	 * @param path 路径
	 * @return 是否存在
	 */
	public static boolean exists(String path) {
		return exists(new File(path));
	}

	/**
	 * 是否为文件。
	 *
	 * @param file 文件
	 * @return 是否为文件
	 */
	public static boolean isFile(File file) {
		return file != null && file.isFile();
	}

	/**
	 * 是否为目录。
	 *
	 * @param file 文件
	 * @return 是否为目录
	 */
	public static boolean isDirectory(File file) {
		return file != null && file.isDirectory();
	}

	/**
	 * 创建目录（含父目录）。
	 *
	 * @param dir 目录
	 */
	public static void mkdir(File dir) {
		if (dir != null && !dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * 创建文件（自动创建父目录，已存在则跳过）。
	 *
	 * @param file 文件
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File touch(File file) throws IOException {
		if (file == null) {
			return null;
		}
		mkdir(file.getParentFile());
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	/**
	 * 获取文件名。
	 *
	 * @param file 文件
	 * @return 文件名
	 */
	public static String getName(File file) {
		return (file == null) ? null : file.getName();
	}

	/**
	 * 获取文件扩展名（不含点，小写）。
	 *
	 * @param file 文件
	 * @return 扩展名，无扩展名返回空串
	 */
	public static String getExt(File file) {
		return (file == null) ? null : getExt(file.getName());
	}

	/**
	 * 获取文件扩展名（不含点）。
	 *
	 * @param fileName 文件名
	 * @return 扩展名，无扩展名返回空串
	 */
	public static String getExt(String fileName) {
		if (fileName == null) {
			return null;
		}
		int pos = fileName.lastIndexOf('.');
		return (pos == -1) ? StrUtil.EMPTY : fileName.substring(pos + 1);
	}

	/**
	 * 获取主文件名（不含扩展名）。
	 *
	 * @param file 文件
	 * @return 主文件名
	 */
	public static String mainName(File file) {
		return (file == null) ? null : mainName(file.getName());
	}

	/**
	 * 获取主文件名（不含扩展名）。
	 *
	 * @param fileName 文件名
	 * @return 主文件名
	 */
	public static String mainName(String fileName) {
		if (fileName == null) {
			return null;
		}
		int pos = fileName.lastIndexOf('.');
		return (pos == -1) ? fileName : fileName.substring(0, pos);
	}

	/**
	 * 获取绝对路径。
	 *
	 * @param file 文件
	 * @return 绝对路径
	 */
	public static String getAbsolutePath(File file) {
		return (file == null) ? null : file.getAbsolutePath();
	}

	/**
	 * 列出目录下的直接子项。
	 *
	 * @param dir 目录
	 * @return 子项数组，目录无效返回 {@code null}
	 */
	public static File[] ls(File dir) {
		return (dir == null) ? null : dir.listFiles();
	}

	/**
	 * 递归列出目录下全部文件。
	 *
	 * @param dir 目录
	 * @return 文件列表
	 */
	public static List<File> loopFiles(File dir) {
		List<File> result = new ArrayList<>();
		if (dir == null || !dir.isDirectory()) {
			return result;
		}
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					result.addAll(loopFiles(f));
				} else {
					result.add(f);
				}
			}
		}
		return result;
	}

	/**
	 * 递归列出目录下全部文件。
	 *
	 * @param path 目录路径
	 * @return 文件列表
	 */
	public static List<File> loopFiles(String path) {
		return loopFiles(new File(path));
	}

	/**
	 * 列出目录下的文件名（仅文件）。
	 *
	 * @param path 目录路径
	 * @return 文件名列表
	 */
	public static List<String> listFileNames(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();
		if (files == null) {
			return Collections.emptyList();
		}
		List<String> names = new ArrayList<>();
		for (File f : files) {
			if (f.isFile()) {
				names.add(f.getName());
			}
		}
		return names;
	}

	// ---------------- 读取 ----------------

	/**
	 * 按 UTF-8 读取文件为字符串。
	 *
	 * @param file 文件
	 * @return 字符串
	 * @throws IOException IO 异常
	 */
	public static String readUtf8String(File file) throws IOException {
		return readString(file, com.sure.tool.util.CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集读取文件为字符串。
	 *
	 * @param file    文件
	 * @param charset 字符集
	 * @return 字符串
	 * @throws IOException IO 异常
	 */
	public static String readString(File file, Charset charset) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return IoUtil.read(in, charset);
		}
	}

	/**
	 * 按 UTF-8 读取文件为行列表。
	 *
	 * @param file 文件
	 * @return 行列表
	 * @throws IOException IO 异常
	 */
	public static List<String> readUtf8Lines(File file) throws IOException {
		return readLines(file, com.sure.tool.util.CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集读取文件为行列表。
	 *
	 * @param file    文件
	 * @param charset 字符集
	 * @return 行列表
	 * @throws IOException IO 异常
	 */
	public static List<String> readLines(File file, Charset charset) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return IoUtil.readLines(in, charset);
		}
	}

	/**
	 * 读取文件全部字节。
	 *
	 * @param file 文件
	 * @return 字节数组
	 * @throws IOException IO 异常
	 */
	public static byte[] readBytes(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return IoUtil.readBytes(in);
		}
	}

	// ---------------- 写入 ----------------

	/**
	 * 按 UTF-8 写入字符串（覆盖）。
	 *
	 * @param content 内容
	 * @param file    文件
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File writeUtf8String(String content, File file) throws IOException {
		return writeString(content, file, com.sure.tool.util.CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集写入字符串（覆盖）。
	 *
	 * @param content 内容
	 * @param file    文件
	 * @param charset 字符集
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File writeString(String content, File file, Charset charset) throws IOException {
		touch(file);
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), charset)) {
			writer.write(content);
			writer.flush();
		}
		return file;
	}

	/**
	 * 按 UTF-8 追加写入字符串。
	 *
	 * @param content 内容
	 * @param file    文件
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File appendUtf8String(String content, File file) throws IOException {
		return appendString(content, file, com.sure.tool.util.CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集追加写入字符串。
	 *
	 * @param content 内容
	 * @param file    文件
	 * @param charset 字符集
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File appendString(String content, File file, Charset charset) throws IOException {
		touch(file);
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), charset)) {
			writer.write(content);
			writer.flush();
		}
		return file;
	}

	/**
	 * 按 UTF-8 写入行列表（每行后跟换行符）。
	 *
	 * @param lines 行列表
	 * @param file  文件
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File writeUtf8Lines(java.util.Collection<?> lines, File file) throws IOException {
		return writeLines(lines, file, com.sure.tool.util.CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集写入行列表（每行后跟换行符）。
	 *
	 * @param lines   行列表
	 * @param file    文件
	 * @param charset 字符集
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File writeLines(java.util.Collection<?> lines, File file, Charset charset) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (CollUtil.isNotEmpty(lines)) {
			for (Object line : lines) {
				sb.append(line).append('\n');
			}
		}
		return writeString(sb.toString(), file, charset);
	}

	/**
	 * 写入字节数组。
	 *
	 * @param data 字节数组
	 * @param file 文件
	 * @return 文件
	 * @throws IOException IO 异常
	 */
	public static File writeBytes(byte[] data, File file) throws IOException {
		touch(file);
		IoUtil.write(data, new FileOutputStream(file));
		return file;
	}

	// ---------------- 复制、移动、删除 ----------------

	/**
	 * 复制文件或目录（目录递归复制）。
	 *
	 * @param src  源文件
	 * @param dest 目标文件
	 * @return 目标文件
	 * @throws IOException IO 异常
	 */
	public static File copy(File src, File dest) throws IOException {
		if (src == null || dest == null) {
			return null;
		}
		if (!src.exists()) {
			return null;
		}
		if (src.isDirectory()) {
			mkdir(dest);
			File[] files = src.listFiles();
			if (files != null) {
				for (File f : files) {
					copy(f, new File(dest, f.getName()));
				}
			}
			return dest;
		}
		mkdir(dest.getParentFile());
		Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return dest;
	}

	/**
	 * 移动文件或目录。
	 *
	 * @param src  源文件
	 * @param dest 目标文件
	 * @return 目标文件
	 * @throws IOException IO 异常
	 */
	public static File move(File src, File dest) throws IOException {
		if (src == null || dest == null || !src.exists()) {
			return null;
		}
		mkdir(dest.getParentFile());
		Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return dest;
	}

	/**
	 * 删除文件或目录（目录递归删除）。
	 *
	 * @param file 文件或目录
	 * @return 是否删除成功
	 */
	public static boolean delete(File file) {
		if (file == null || !file.exists()) {
			return false;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					delete(f);
				}
			}
		}
		return file.delete();
	}

	// ---------------- 其他 ----------------

	/**
	 * 计算文件或目录大小（字节）。
	 *
	 * @param file 文件或目录
	 * @return 字节数
	 */
	public static long size(File file) {
		if (file == null || !file.exists()) {
			return 0L;
		}
		if (file.isFile()) {
			return file.length();
		}
		long size = 0L;
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				size += size(f);
			}
		}
		return size;
	}

	/**
	 * 文件大小可读化：{@code 1024} → {@code 1 KB}，{@code 1536} → {@code 1.5 KB}。
	 *
	 * @param size 字节数
	 * @return 可读大小
	 */
	public static String readableFileSize(long size) {
		if (size < 0) {
			return "0 B";
		}
		if (size < 1024) {
			return size + " B";
		}
		String[] units = { "B", "KB", "MB", "GB", "TB" };
		int idx = (int) (Math.log10(size) / Math.log10(1024));
		if (idx >= units.length) {
			idx = units.length - 1;
		}
		double value = size / Math.pow(1024, idx);
		return new DecimalFormat("#.##").format(value) + " " + units[idx];
	}

	/**
	 * 规范化路径：统一斜杠、去除 {@code ./} 与多余的 {@code ../}。
	 *
	 * @param path 路径
	 * @return 规范化后的路径
	 */
	public static String normalize(String path) {
		if (path == null) {
			return null;
		}
		String p = path.replace('\\', '/').trim();
		if (p.isEmpty()) {
			return p;
		}
		boolean absolute = p.startsWith("/");
		String drivePrefix = null;
		if (p.matches("^[A-Za-z]:/.*")) {
			drivePrefix = p.substring(0, 2);
			p = p.substring(2);
			absolute = true;
		}
		String[] parts = p.split("/");
		List<String> stack = new ArrayList<>();
		for (String part : parts) {
			if (part.isEmpty() || ".".equals(part)) {
				continue;
			}
			if ("..".equals(part)) {
				if (!stack.isEmpty() && !"..".equals(stack.get(stack.size() - 1))) {
					stack.remove(stack.size() - 1);
				} else if (!absolute) {
					stack.add(part);
				}
			} else {
				stack.add(part);
			}
		}
		String joined = StrUtil.join("/", stack);
		if (absolute) {
			joined = "/" + joined;
		}
		if (drivePrefix != null) {
			joined = drivePrefix + joined;
		}
		if (joined.isEmpty()) {
			return absolute ? "/" : ".";
		}
		return joined;
	}

	/**
	 * 系统临时目录路径。
	 *
	 * @return 临时目录路径
	 */
	public static String getTmpDirPath() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * 系统临时目录。
	 *
	 * @return 临时目录
	 */
	public static File getTmpDir() {
		return new File(getTmpDirPath());
	}

	/**
	 * 用户主目录路径。
	 *
	 * @return 主目录路径
	 */
	public static String getUserHomePath() {
		return System.getProperty("user.home");
	}

	/**
	 * 最后修改时间。
	 *
	 * @param file 文件
	 * @return 最后修改时间，空文件返回 {@code null}
	 */
	public static Date lastModifiedTime(File file) {
		return (file == null) ? null : new Date(file.lastModified());
	}
}
