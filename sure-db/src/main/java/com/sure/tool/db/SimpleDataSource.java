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
package com.sure.tool.db;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 零第三方依赖的最小连接池数据源。
 *
 * <p>空闲连接队列 + 活跃上限（默认 10）+ 借用超时（默认 10 秒）：
 * 借用时校验连接有效性（{@code isValid(3)}），失效连接自动剔除重建；
 * 池满且未超时则等待归还。归还发生在调用方关闭包装连接时。</p>
 *
 * <p>为"最小可用"实现：不做空闲回收与公平调度，高并发重负载场景建议
 * 替换为 HikariCP 等专业连接池（实现 {@link DataSource} 即可无缝替换）。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class SimpleDataSource implements DataSource, AutoCloseable {

	/** 锁对象 */
	private final Object lock = new Object();

	/** 驱动类名（null 表示自动探测） */
	private final String driver;

	/** JDBC URL */
	private final String url;

	/** 用户名 */
	private final String user;

	/** 密码 */
	private final String password;

	/** 池内最大连接数 */
	private final int maxSize;

	/** 借用超时（毫秒） */
	private final long borrowTimeoutMillis;

	/** 空闲连接队列 */
	private final ArrayDeque<Connection> idle = new ArrayDeque<>();

	/** 池内存活连接数（空闲 + 借出） */
	private int active;

	/** 是否已关闭 */
	private boolean closed;

	/**
	 * 构造数据源（自动探测驱动）。
	 *
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 */
	public SimpleDataSource(String url, String user, String password) {
		this(null, url, user, password, 10);
	}

	/**
	 * 构造数据源（自动探测驱动，自定义上限）。
	 *
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 * @param maxSize  池内最大连接数
	 */
	public SimpleDataSource(String url, String user, String password, int maxSize) {
		this(null, url, user, password, maxSize);
	}

	/**
	 * 构造数据源。
	 *
	 * @param driver   驱动类名，null 表示自动探测
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 */
	public SimpleDataSource(String driver, String url, String user, String password) {
		this(driver, url, user, password, 10);
	}

	/**
	 * 构造数据源。
	 *
	 * @param driver   驱动类名，null 表示自动探测
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 * @param maxSize  池内最大连接数
	 */
	public SimpleDataSource(String driver, String url, String user, String password, int maxSize) {
		this.driver = driver;
		this.url = Objects.requireNonNull(url, "url 不能为 null");
		this.user = user;
		this.password = password;
		if (maxSize < 1) {
			throw new IllegalArgumentException("maxSize 必须大于 0");
		}
		this.maxSize = maxSize;
		this.borrowTimeoutMillis = 10_000L;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return borrow();
	}

	/**
	 * 返回池内连接（忽略传入的用户名/密码，与 {@link #getConnection()} 等价）。
	 *
	 * @param username 用户名（忽略）
	 * @param password 密码（忽略）
	 * @return 池内连接
	 * @throws SQLException 获取失败
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return borrow();
	}

	/**
	 * 关闭数据源：关闭全部空闲连接，后续借出/归还均失败。
	 */
	@Override
	public void close() {
		synchronized (lock) {
			if (closed) {
				return;
			}
			closed = true;
			while (!idle.isEmpty()) {
				closeQuietly(idle.pollFirst());
			}
			lock.notifyAll();
		}
	}

	// ----------------------------------------------------------------------
	// 池化实现
	// ----------------------------------------------------------------------

	/**
	 * 借用连接：优先复用有效空闲连接，不足新建，池满等待归还直至超时。
	 *
	 * @return 包装连接（close 时归还池）
	 * @throws SQLException 获取失败或超时
	 */
	private Connection borrow() throws SQLException {
		long deadline = System.currentTimeMillis() + borrowTimeoutMillis;
		synchronized (lock) {
			if (closed) {
				throw new SQLException("数据源已关闭");
			}
			while (true) {
				// 1. 优先复用空闲连接（校验有效性）
				Connection candidate = idle.pollLast();
				while (candidate != null) {
					if (candidate.isValid(3)) {
						// 直接复用
						return wrap(candidate);
					}
					closeQuietly(candidate);
					active--;
					candidate = idle.pollLast();
				}
				// 2. 未达上限则新建
				if (active < maxSize) {
					active++;
					Connection raw = create();
					if (raw.isValid(3)) {
						return wrap(raw);
					}
					// 新建即失效（罕见）：关闭并重试
					closeQuietly(raw);
					active--;
					continue;
				}
				// 3. 池满，等待归还
				long remain = deadline - System.currentTimeMillis();
				if (remain <= 0) {
					throw new SQLException("获取连接超时(" + borrowTimeoutMillis + "ms)，池已满 maxSize=" + maxSize);
				}
				try {
					lock.wait(remain);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new SQLException("获取连接被中断", e);
				}
			}
		}
	}

	/**
	 * 创建真实连接。
	 *
	 * @return 连接
	 * @throws SQLException 创建失败
	 */
	private Connection create() throws SQLException {
		if (driver != null) {
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				throw new SQLException("驱动类不存在: " + driver, e);
			}
		}
		return DriverManager.getConnection(url, user, password);
	}

	/**
	 * 归还连接：池未关闭且连接有效则入空闲队列，否则真实关闭。
	 *
	 * @param raw 真实连接
	 */
	private void returnToPool(Connection raw) {
		synchronized (lock) {
			if (closed) {
				closeQuietly(raw);
				active--;
				lock.notifyAll();
				return;
			}
			try {
				if (raw.isValid(3)) {
					idle.addLast(raw);
					lock.notifyAll();
					return;
				}
			} catch (SQLException ignored) {
				// 校验失败按失效处理
			}
			closeQuietly(raw);
			active--;
			lock.notifyAll();
		}
	}

	/**
	 * 包装连接：所有方法委托真实连接，{@code close()} 改为归还池。
	 *
	 * @param raw 真实连接
	 * @return 包装连接
	 */
	private Connection wrap(Connection raw) {
		return (Connection) Proxy.newProxyInstance(
				SimpleDataSource.class.getClassLoader(),
				new Class<?>[] {Connection.class},
				(proxy, method, args) -> {
					if ("close".equals(method.getName()) && method.getParameterCount() == 0) {
						returnToPool(raw);
						return null;
					}
					try {
						return method.invoke(raw, args);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				});
	}

	/**
	 * 安静关闭连接。
	 *
	 * @param connection 连接
	 */
	private static void closeQuietly(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignored) {
				// 忽略
			}
		}
	}

	// ----------------------------------------------------------------------
	// DataSource 其余方法
	// ----------------------------------------------------------------------

	@Override
	public PrintWriter getLogWriter() {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) {
		// 不支持，忽略
	}

	@Override
	public void setLoginTimeout(int seconds) {
		// 不支持，忽略
	}

	@Override
	public int getLoginTimeout() {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return iface.cast(this);
		}
		throw new SQLException("不可展开为 " + iface.getName());
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return iface.isInstance(this);
	}
}
