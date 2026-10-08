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
package com.sure.tool.extra.mail;

/**
 * 邮箱账户配置（SMTP 连接参数）。
 *
 * @author suretool
 * @since 1.1.0
 */
public class MailAccount {

	private String host;
	private int port = 25;
	private String from;
	private String username;
	private String password;
	private boolean ssl;
	private boolean starttls;
	private String charset = "UTF-8";
	private int timeout = 10000;

	/**
	 * 创建空配置。
	 */
	public MailAccount() {
	}

	/**
	 * 创建配置。
	 *
	 * @param host SMTP 主机
	 * @param from 发件人地址
	 */
	public MailAccount(String host, String from) {
		this.host = host;
		this.from = from;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isStarttls() {
		return starttls;
	}

	public void setStarttls(boolean starttls) {
		this.starttls = starttls;
	}

	/**
	 * 获取正文与主题编码。
	 *
	 * @return 字符编码，默认 UTF-8
	 * @since 1.7.0
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * 设置正文与主题编码。
	 *
	 * @param charset 字符编码（如 UTF-8 / GBK）
	 * @since 1.7.0
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * 获取 SMTP 连接/读写超时（毫秒）。
	 *
	 * @return 超时毫秒数，默认 10000
	 * @since 1.7.0
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 设置 SMTP 连接/读写超时（毫秒）。
	 *
	 * @param timeout 超时毫秒数（&gt;=0）
	 * @since 1.7.0
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
