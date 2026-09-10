package com.sure.tool.util;

import java.nio.charset.Charset;

/**
 * 字符集工具类，参考 Hutool 的 {@code CharsetUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CharsetUtil {

	/** UTF-8 */
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	/** GBK */
	public static final Charset GBK = Charset.forName("GBK");
	/** ISO-8859-1 */
	public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	/** US-ASCII */
	public static final Charset US_ASCII = Charset.forName("US-ASCII");
	/** UTF-16 */
	public static final Charset UTF_16 = Charset.forName("UTF-16");
	/** UTF-16BE */
	public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
	/** UTF-16LE */
	public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

	private CharsetUtil() {
	}

	/**
	 * 获取字符集，字符集名为 {@code null} 时返回默认字符集。
	 *
	 * @param charsetName 字符集名
	 * @return 字符集
	 */
	public static Charset charset(String charsetName) {
		if (charsetName == null) {
			return Charset.defaultCharset();
		}
		return Charset.forName(charsetName);
	}

	/**
	 * 字符集间转换字符串编码。
	 *
	 * @param source       源字符串
	 * @param srcCharset   源字符集名
	 * @param destCharset  目标字符集名
	 * @return 转换后的字符串
	 */
	public static String convert(String source, String srcCharset, String destCharset) {
		return convert(source, charset(srcCharset), charset(destCharset));
	}

	/**
	 * 字符集间转换字符串编码。
	 *
	 * @param source      源字符串
	 * @param srcCharset  源字符集
	 * @param destCharset 目标字符集
	 * @return 转换后的字符串
	 */
	public static String convert(String source, Charset srcCharset, Charset destCharset) {
		if (source == null) {
			return null;
		}
		return new String(source.getBytes(srcCharset), destCharset);
	}

	/**
	 * 字符集是否被支持。
	 *
	 * @param charsetName 字符集名
	 * @return 是否支持
	 */
	public static boolean isSupported(String charsetName) {
		try {
			Charset.forName(charsetName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 系统默认字符集。
	 *
	 * @return 默认字符集
	 */
	public static Charset defaultCharset() {
		return Charset.defaultCharset();
	}
}
