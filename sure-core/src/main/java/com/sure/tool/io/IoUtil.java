package com.sure.tool.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.sure.tool.util.CharsetUtil;

/**
 * IO 流工具类，参考 Hutool 的 {@code IoUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class IoUtil {

	/** 默认缓冲区大小 */
	public static final int DEFAULT_BUFFER_SIZE = 8192;

	private IoUtil() {
	}

	/**
	 * 复制输入流到输出流。
	 *
	 * @param in  输入流
	 * @param out 输出流
	 * @return 复制的字节数
	 * @throws IOException IO 异常
	 */
	public static long copy(InputStream in, OutputStream out) throws IOException {
		return copy(in, out, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * 复制输入流到输出流。
	 *
	 * @param in         输入流
	 * @param out        输出流
	 * @param bufferSize 缓冲区大小
	 * @return 复制的字节数
	 * @throws IOException IO 异常
	 */
	public static long copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		long count = 0;
		int n;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
			count += n;
		}
		out.flush();
		return count;
	}

	/**
	 * 复制字符流。
	 *
	 * @param reader 字符输入流
	 * @param writer 字符输出流
	 * @return 复制的字符数
	 * @throws IOException IO 异常
	 */
	public static long copy(Reader reader, Writer writer) throws IOException {
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n;
		while ((n = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, n);
			count += n;
		}
		writer.flush();
		return count;
	}

	/**
	 * 读取输入流全部字节。
	 *
	 * @param in 输入流
	 * @return 字节数组
	 * @throws IOException IO 异常
	 */
	public static byte[] readBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		return out.toByteArray();
	}

	/**
	 * 按 UTF-8 读取输入流为字符串。
	 *
	 * @param in 输入流
	 * @return 字符串
	 * @throws IOException IO 异常
	 */
	public static String readUtf8(InputStream in) throws IOException {
		return read(in, CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集读取输入流为字符串。
	 *
	 * @param in      输入流
	 * @param charset 字符集
	 * @return 字符串
	 * @throws IOException IO 异常
	 */
	public static String read(InputStream in, Charset charset) throws IOException {
		return new String(readBytes(in), charset);
	}

	/**
	 * 按 UTF-8 读取输入流为行列表。
	 *
	 * @param in 输入流
	 * @return 行列表
	 * @throws IOException IO 异常
	 */
	public static List<String> readUtf8Lines(InputStream in) throws IOException {
		return readLines(in, CharsetUtil.UTF_8);
	}

	/**
	 * 按指定字符集读取输入流为行列表。
	 *
	 * @param in      输入流
	 * @param charset 字符集
	 * @return 行列表
	 * @throws IOException IO 异常
	 */
	public static List<String> readLines(InputStream in, Charset charset) throws IOException {
		List<String> lines = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
		String line;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines;
	}

	/**
	 * 写入字节数组。
	 *
	 * @param data 字节数组
	 * @param out  输出流
	 * @throws IOException IO 异常
	 */
	public static void write(byte[] data, OutputStream out) throws IOException {
		out.write(data);
		out.flush();
	}

	/**
	 * 按 UTF-8 写入字符串。
	 *
	 * @param content 字符串
	 * @param out     输出流
	 * @throws IOException IO 异常
	 */
	public static void writeUtf8(String content, OutputStream out) throws IOException {
		write(content.getBytes(CharsetUtil.UTF_8), out);
	}

	/**
	 * 按指定字符集写入字符串。
	 *
	 * @param content 字符串
	 * @param out     输出流
	 * @param charset 字符集
	 * @throws IOException IO 异常
	 */
	public static void write(String content, OutputStream out, Charset charset) throws IOException {
		write(content.getBytes(charset), out);
	}

	/**
	 * 按指定字符集写入字符串（通过 Writer）。
	 *
	 * @param content 字符串
	 * @param writer  字符输出流
	 * @throws IOException IO 异常
	 */
	public static void write(String content, Writer writer) throws IOException {
		writer.write(content);
		writer.flush();
	}

	/**
	 * 安静关闭可关闭资源，忽略关闭异常。
	 *
	 * @param closeables 可关闭资源
	 */
	public static void closeQuietly(Closeable... closeables) {
		if (closeables == null) {
			return;
		}
		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (IOException ignore) {
					// 忽略关闭异常
				}
			}
		}
	}

	/**
	 * 比较两个输入流内容是否一致。
	 *
	 * @param in1 输入流 1
	 * @param in2 输入流 2
	 * @return 内容是否一致
	 * @throws IOException IO 异常
	 */
	public static boolean contentEquals(InputStream in1, InputStream in2) throws IOException {
		byte[] buf1 = new byte[DEFAULT_BUFFER_SIZE];
		byte[] buf2 = new byte[DEFAULT_BUFFER_SIZE];
		int n1;
		int n2;
		while (true) {
			n1 = in1.read(buf1);
			n2 = in2.read(buf2);
			if (n1 != n2) {
				return false;
			}
			if (n1 == -1) {
				return true;
			}
			for (int i = 0; i < n1; i++) {
				if (buf1[i] != buf2[i]) {
					return false;
				}
			}
		}
	}

	/**
	 * 输入流转输出流（输出流关闭）。
	 *
	 * @param in  输入流
	 * @param out 输出流
	 * @return 复制的字节数
	 * @throws IOException IO 异常
	 */
	public static long toOutput(InputStream in, OutputStream out) throws IOException {
		return copy(in, out);
	}
}
