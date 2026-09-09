package com.sure.tool.util;

/**
 * 系统属性工具类，参考 Hutool 的 {@code SystemUtil} 设计。
 *
 * @author suretool
 */
public class SystemUtil {

	private SystemUtil() {
	}

	/**
	 * 获取系统属性。
	 *
	 * @param key 属性名，如 {@code "os.name"}
	 * @return 属性值，不存在返回 {@code null}
	 */
	public static String get(String key) {
		return System.getProperty(key);
	}

	/**
	 * 获取系统属性，不存在返回默认值。
	 *
	 * @param key          属性名
	 * @param defaultValue 默认值
	 * @return 属性值
	 */
	public static String get(String key, String defaultValue) {
		return System.getProperty(key, defaultValue);
	}

	/**
	 * 操作系统名称，如 {@code "Windows 11"}。
	 *
	 * @return 操作系统名称
	 */
	public static String getOsName() {
		return get("os.name");
	}

	/**
	 * 操作系统架构，如 {@code "amd64"}。
	 *
	 * @return 操作系统架构
	 */
	public static String getOsArch() {
		return get("os.arch");
	}

	/**
	 * 操作系统版本。
	 *
	 * @return 操作系统版本
	 */
	public static String getOsVersion() {
		return get("os.version");
	}

	/**
	 * Java 版本，如 {@code "17.0.3"}。
	 *
	 * @return Java 版本
	 */
	public static String getJavaVersion() {
		return get("java.version");
	}

	/**
	 * Java 安装目录。
	 *
	 * @return Java 安装目录
	 */
	public static String getJavaHome() {
		return get("java.home");
	}

	/**
	 * 当前用户名。
	 *
	 * @return 当前用户名
	 */
	public static String getUserName() {
		return get("user.name");
	}

	/**
	 * 当前工作目录。
	 *
	 * @return 当前工作目录
	 */
	public static String getUserDir() {
		return get("user.dir");
	}

	/**
	 * 用户主目录。
	 *
	 * @return 用户主目录
	 */
	public static String getUserHome() {
		return get("user.home");
	}

	/**
	 * 行分隔符。
	 *
	 * @return 行分隔符
	 */
	public static String getLineSeparator() {
		return get("line.separator");
	}

	/**
	 * 文件分隔符（Windows 为 {@code \}，Linux 为 {@code /}）。
	 *
	 * @return 文件分隔符
	 */
	public static String getFileSeparator() {
		return get("file.separator");
	}

	/**
	 * 路径分隔符。
	 *
	 * @return 路径分隔符
	 */
	public static String getPathSeparator() {
		return get("path.separator");
	}

	/**
	 * JVM 总内存（字节）。
	 *
	 * @return 总内存
	 */
	public static long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * JVM 空闲内存（字节）。
	 *
	 * @return 空闲内存
	 */
	public static long getFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * JVM 最大可用内存（字节）。
	 *
	 * @return 最大可用内存
	 */
	public static long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}
}
