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
package com.sure.tool.extra.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * FTP 客户端封装（基于 Apache Commons Net）。
 * <p>
 * 提供连接/登出、上传下载、文件列表、目录切换与删除等常用操作。
 * 支持主动/被动模式与二进制/ASCII 传输类型切换。
 *
 * @author suretool
 * @since 1.1.0
 */
public class FtpUtil implements Closeable {

	private final FTPClient client;

	/**
	 * 创建封装（内部新建 {@link FTPClient}）。
	 */
	public FtpUtil() {
		this(new FTPClient());
	}

	/**
	 * 创建封装。
	 *
	 * @param client 已配置的 FTPClient（便于注入测试替身）
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
	public FtpUtil(FTPClient client) {
		this.client = client;
	}

	/**
	 * 连接并登录（UTF-8 控制连接编码，默认超时）。
	 *
	 * @param host     主机
	 * @param port     端口（21 常用）
	 * @param username 用户名
	 * @param password 密码
	 * @throws IOException 连接或登录失败
	 */
	public void connect(String host, int port, String username, String password) throws IOException {
		connect(host, port, username, password, StandardCharsets.UTF_8.name());
	}

	/**
	 * 连接并登录，指定控制连接编码。
	 *
	 * @param host     主机
	 * @param port     端口
	 * @param username 用户名
	 * @param password 密码
	 * @param encoding 控制连接编码（中文文件名场景使用 UTF-8 或 GBK）
	 * @throws IOException 连接或登录失败
	 * @since 1.7.0
	 */
	public void connect(String host, int port, String username, String password,
			String encoding) throws IOException {
		connect(host, port, username, password, 0, 0, encoding);
	}

	/**
	 * 连接并登录，指定超时与控制连接编码。
	 *
	 * @param host           主机
	 * @param port           端口
	 * @param username       用户名
	 * @param password       密码
	 * @param connectTimeout 连接超时毫秒数（&lt;=0 使用系统默认）
	 * @param soTimeout      读写超时毫秒数（&lt;=0 使用系统默认）
	 * @param encoding       控制连接编码（可为 {@code null}，默认 UTF-8）
	 * @throws IOException 连接或登录失败
	 * @since 1.7.0
	 */
	public void connect(String host, int port, String username, String password,
			int connectTimeout, int soTimeout, String encoding) throws IOException {
		String controlEncoding = encoding == null ? StandardCharsets.UTF_8.name() : encoding;
		client.setControlEncoding(controlEncoding);
		if (connectTimeout > 0) {
			client.setConnectTimeout(connectTimeout);
		}
		client.connect(host, port);
		if (soTimeout > 0) {
			client.setSoTimeout(soTimeout);
		}
		if (!client.login(username, password)) {
			throw new IOException("FTP 登录失败: " + client.getReplyString());
		}
	}

	/**
	 * 启用被动模式（穿越防火墙常用）。
	 *
	 * @return this
	 */
	public FtpUtil passive() {
		client.enterLocalPassiveMode();
		return this;
	}

	/**
	 * 启用二进制传输（默认）。
	 *
	 * @return this
	 */
	public FtpUtil binary() throws IOException {
		client.setFileType(FTP.BINARY_FILE_TYPE);
		return this;
	}

	/**
	 * 上传文件。
	 *
	 * @param remotePath 远端路径
	 * @param in         内容流
	 * @return 是否成功
	 * @throws IOException IO 异常
	 */
	public boolean upload(String remotePath, InputStream in) throws IOException {
		return client.storeFile(remotePath, in);
	}

	/**
	 * 下载文件。
	 *
	 * @param remotePath 远端路径
	 * @param out        输出流
	 * @return 是否成功
	 * @throws IOException IO 异常
	 */
	public boolean download(String remotePath, OutputStream out) throws IOException {
		return client.retrieveFile(remotePath, out);
	}

	/**
	 * 列出当前目录文件。
	 *
	 * @return 文件列表（空列表表示无文件）
	 * @throws IOException IO 异常
	 */
	public List<FTPFile> listFiles() throws IOException {
		FTPFile[] files = client.listFiles();
		return files == null ? Collections.emptyList() : Arrays.asList(files);
	}

	/**
	 * 列出指定目录文件。
	 *
	 * @param remotePath 远端目录
	 * @return 文件列表
	 * @throws IOException IO 异常
	 */
	public List<FTPFile> listFiles(String remotePath) throws IOException {
		FTPFile[] files = client.listFiles(remotePath);
		return files == null ? Collections.emptyList() : Arrays.asList(files);
	}

	/**
	 * 切换目录。
	 *
	 * @param remotePath 远端目录
	 * @return 是否成功
	 * @throws IOException IO 异常
	 */
	public boolean cd(String remotePath) throws IOException {
		return client.changeWorkingDirectory(remotePath);
	}

	/**
	 * 创建目录。
	 *
	 * @param remotePath 远端目录
	 * @return 是否成功
	 * @throws IOException IO 异常
	 */
	public boolean mkdir(String remotePath) throws IOException {
		return client.makeDirectory(remotePath);
	}

	/**
	 * 删除文件。
	 *
	 * @param remotePath 远端文件
	 * @return 是否成功
	 * @throws IOException IO 异常
	 */
	public boolean delete(String remotePath) throws IOException {
		return client.deleteFile(remotePath);
	}

	/**
	 * 重命名 / 移动远端文件。
	 *
	 * @param fromPath 原路径
	 * @param toPath   新路径
	 * @return 是否成功
	 * @throws IOException IO 异常
	 * @since 1.7.0
	 */
	public boolean rename(String fromPath, String toPath) throws IOException {
		return client.rename(fromPath, toPath);
	}

	/**
	 * 判断远端文件或目录是否存在。
	 *
	 * @param remotePath 远端路径
	 * @return 是否存在
	 * @throws IOException IO 异常
	 * @since 1.7.0
	 */
	public boolean exist(String remotePath) throws IOException {
		FTPFile[] files = client.listFiles(remotePath);
		return files != null && files.length > 0;
	}

	/**
	 * 当前目录。
	 *
	 * @return 路径
	 * @throws IOException IO 异常
	 */
	public String pwd() throws IOException {
		return client.printWorkingDirectory();
	}

	/**
	 * 底层客户端（高级操作使用）。
	 *
	 * @return FTPClient
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
	public FTPClient client() {
		return client;
	}

	/**
	 * 是否已连接。
	 *
	 * @return 是否已连接
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	@Override
	public void close() throws IOException {
		if (client.isConnected()) {
			client.logout();
			client.disconnect();
		}
	}
}
