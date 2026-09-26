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

import javax.sql.DataSource;

/**
 * 数据库静态门面：全局默认数据源与便捷入口。
 *
 * @author suretool
 * @since 1.2.0
 */
public class DbUtil {

	/** 全局默认数据源（volatile 保证可见性） */
	private static volatile DataSource defaultDataSource;

	/**
	 * 私有构造器，禁止实例化。
	 */
	private DbUtil() {
	}

	/**
	 * 设置全局默认数据源；传入 null 表示清除已设置的默认值。
	 *
	 * @param dataSource 数据源，可为 null（清除）
	 */
	public static void setDefaultDataSource(DataSource dataSource) {
		defaultDataSource = dataSource;
	}

	/**
	 * 使用全局默认数据源创建门面。
	 *
	 * @return 门面
	 */
	public static Db use() {
		return Db.use();
	}

	/**
	 * 使用指定数据源创建门面。
	 *
	 * @param dataSource 数据源
	 * @return 门面
	 */
	public static Db use(DataSource dataSource) {
		return Db.use(dataSource);
	}

	/**
	 * 使用连接参数创建门面（内置连接池）。
	 *
	 * @param driver   驱动类名，可为 null
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 * @return 门面
	 */
	public static Db use(String driver, String url, String user, String password) {
		return Db.use(driver, url, user, password);
	}

	/**
	 * 基于全局默认数据源创建执行器。
	 *
	 * @return 执行器
	 */
	public static SqlRunner newSqlRunner() {
		return new SqlRunner(requireDefault());
	}

	/**
	 * 基于指定数据源创建执行器。
	 *
	 * @param dataSource 数据源
	 * @return 执行器
	 */
	public static SqlRunner newSqlRunner(DataSource dataSource) {
		return new SqlRunner(dataSource);
	}

	/**
	 * 获取全局默认数据源，未设置时抛异常。
	 *
	 * @return 数据源
	 */
	private static DataSource requireDefault() {
		DataSource ds = defaultDataSource;
		if (ds == null) {
			throw new DbRuntimeException("未设置默认数据源，请先调用 DbUtil.setDefaultDataSource(DataSource)");
		}
		return ds;
	}
}
