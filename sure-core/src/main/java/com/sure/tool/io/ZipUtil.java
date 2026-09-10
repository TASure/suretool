package com.sure.tool.io;

import com.sure.tool.util.CharsetUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 压缩解压工具类，参考 Hutool 的 {@code ZipUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class ZipUtil {

	private ZipUtil() {
	}

	/**
	 * 压缩文件或目录到 zip 文件（UTF-8 文件名）。
	 *
	 * @param src     源文件或目录
	 * @param zipFile 目标 zip 文件
	 * @throws IOException IO 异常
	 */
	public static void zip(File src, File zipFile) throws IOException {
		zip(src, zipFile, CharsetUtil.UTF_8);
	}

	/**
	 * 压缩文件或目录到 zip 文件。
	 *
	 * @param src     源文件或目录
	 * @param zipFile 目标 zip 文件
	 * @param charset 文件名编码
	 * @throws IOException IO 异常
	 */
	public static void zip(File src, File zipFile, Charset charset) throws IOException {
		if (src == null || zipFile == null || !src.exists()) {
			throw new IllegalArgumentException("源文件或目标文件不合法");
		}
		File parent = zipFile.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)), charset)) {
			if (src.isDirectory()) {
				zipDir(zos, src, src.getName(), charset);
			} else {
				zipFile(zos, src, src.getName(), charset);
			}
		}
	}

	/**
	 * 压缩文件或目录到 zip 文件（路径字符串版本）。
	 *
	 * @param srcPath 源路径
	 * @param zipPath 目标 zip 路径
	 * @throws IOException IO 异常
	 */
	public static void zip(String srcPath, String zipPath) throws IOException {
		zip(new File(srcPath), new File(zipPath));
	}

	/**
	 * 解压 zip 文件到目标目录（UTF-8 文件名）。
	 *
	 * @param zipFile zip 文件
	 * @param outDir  目标目录
	 * @throws IOException IO 异常
	 */
	public static void unzip(File zipFile, File outDir) throws IOException {
		unzip(zipFile, outDir, CharsetUtil.UTF_8);
	}

	/**
	 * 解压 zip 文件到目标目录。
	 *
	 * @param zipFile zip 文件
	 * @param outDir  目标目录
	 * @param charset 文件名编码
	 * @throws IOException IO 异常
	 */
	public static void unzip(File zipFile, File outDir, Charset charset) throws IOException {
		if (zipFile == null || outDir == null || !zipFile.isFile()) {
			throw new IllegalArgumentException("zip 文件或目标目录不合法");
		}
		outDir.mkdirs();
		String outPath = outDir.getCanonicalPath();
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)), charset)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				File target = new File(outDir, entry.getName());
				// 防 zip 路径穿越（zip-slip）
				if (!target.getCanonicalPath().startsWith(outPath + File.separator)) {
					throw new IOException("非法 zip 条目: " + entry.getName());
				}
				if (entry.isDirectory()) {
					target.mkdirs();
					continue;
				}
				File parent = target.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}
				try (FileOutputStream fos = new FileOutputStream(target)) {
					IoUtil.copy(zis, fos);
				}
			}
		}
	}

	/**
	 * 解压 zip 文件到目标目录（路径字符串版本）。
	 *
	 * @param zipPath   zip 文件路径
	 * @param outDirPath 目标目录路径
	 * @throws IOException IO 异常
	 */
	public static void unzip(String zipPath, String outDirPath) throws IOException {
		unzip(new File(zipPath), new File(outDirPath));
	}

	private static void zipDir(ZipOutputStream zos, File dir, String baseName, Charset charset) throws IOException {
		String base = normalizePath(baseName);
		ZipEntry dirEntry = new ZipEntry(base + "/");
		zos.putNextEntry(dirEntry);
		zos.closeEntry();
		File[] children = dir.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.isDirectory()) {
					zipDir(zos, child, base + "/" + child.getName(), charset);
				} else {
					zipFile(zos, child, base + "/" + child.getName(), charset);
				}
			}
		}
	}

	private static void zipFile(ZipOutputStream zos, File file, String name, Charset charset) throws IOException {
		ZipEntry entry = new ZipEntry(normalizePath(name));
		entry.setSize(file.length());
		zos.putNextEntry(entry);
		try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
			IoUtil.copy(in, zos);
		}
		zos.closeEntry();
	}

	private static String normalizePath(String path) {
		return path.replace('\\', '/');
	}
}
