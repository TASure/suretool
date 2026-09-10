package com.sure.tool.xml;

/**
 * XML 处理异常。
 *
 * @author suretool
 */
public class XmlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 */
	public XmlException(String message) {
		super(message);
	}

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}
}
