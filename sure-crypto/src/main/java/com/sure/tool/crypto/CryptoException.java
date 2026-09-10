package com.sure.tool.crypto;

/**
 * 加密解密异常。
 *
 * @author suretool
 */
public class CryptoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 */
	public CryptoException(String message) {
		super(message);
	}

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public CryptoException(String message, Throwable cause) {
		super(message, cause);
	}
}
