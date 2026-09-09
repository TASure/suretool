package com.sure.tool.json;

/**
 * JSON 解析或序列化异常。
 *
 * @author suretool
 */
public class JSONException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 */
	public JSONException(String message) {
		super(message);
	}

	/**
	 * 创建异常。
	 *
	 * @param message 错误信息
	 * @param cause   原因
	 */
	public JSONException(String message, Throwable cause) {
		super(message, cause);
	}
}
