package com.sure.tool.http;

/**
 * HTTP 请求异常，携带响应状态码。
 *
 * @author suretool
 */
public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int statusCode;

	/**
	 * 创建异常（无状态码）。
	 *
	 * @param message 错误信息
	 */
	public HttpException(String message) {
		super(message);
		this.statusCode = -1;
	}

	/**
	 * 创建异常。
	 *
	 * @param message    错误信息
	 * @param statusCode 响应状态码
	 */
	public HttpException(String message, int statusCode) {
		super(message + "（HTTP " + statusCode + "）");
		this.statusCode = statusCode;
	}

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = -1;
	}

	/**
	 * 响应状态码，无响应时为 -1。
	 *
	 * @return 状态码
	 */
	public int getStatusCode() {
		return statusCode;
	}
}
