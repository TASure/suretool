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

import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

/**
 * 数据库门面：面向日常 CRUD / 事务 / 分页的最简入口。
 *
 * <p>无状态、线程安全，可安全复用；内部委托 {@link SqlRunner} 执行。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class Db {

	/** 底层执行器 */
	private final SqlRunner runner;

	/**
	 * 构造门面。
	 *
	 * @param runner 执行器，非空
	 */
	private Db(SqlRunner runner) {
		this.runner = Objects.requireNonNull(runner, "runner 不能为 null");
	}

	/**
	 * 使用 {@link DbUtil} 的全局默认数据源创建门面。
	 *
	 * @return 门面
	 */
	public static Db use() {
		return new Db(DbUtil.newSqlRunner());
	}

	/**
	 * 使用指定数据源创建门面。
	 *
	 * @param dataSource 数据源
	 * @return 门面
	 */
	public static Db use(DataSource dataSource) {
		return new Db(new SqlRunner(dataSource));
	}

	/**
	 * 使用连接参数创建门面（内置连接池）。
	 *
	 * @param driver   驱动类名，可为 null（自动探测）
	 * @param url      JDBC URL
	 * @param user     用户名
	 * @param password 密码
	 * @return 门面
	 */
	public static Db use(String driver, String url, String user, String password) {
		return new Db(new SqlRunner(new SimpleDataSource(driver, url, user, password)));
	}

	/**
	 * 执行查询，返回实体列表。
	 *
	 * @param sql    查询 SQL
	 * @param params 绑定参数
	 * @return 实体列表
	 */
	public List<Entity> query(String sql, Object... params) {
		return runner.query(sql, params);
	}

	/**
	 * 执行查询，返回首行实体。
	 *
	 * @param sql    查询 SQL
	 * @param params 绑定参数
	 * @return 首行实体，无数据返回 null
	 */
	public Entity queryOne(String sql, Object... params) {
		return runner.queryOne(sql, params);
	}

	/**
	 * 执行查询，返回单值数字。
	 *
	 * @param sql    查询 SQL
	 * @param params 绑定参数
	 * @return 单值数字
	 */
	public Number queryNumber(String sql, Object... params) {
		return runner.queryNumber(sql, params);
	}

	/**
	 * 执行计数查询。
	 *
	 * @param sql    计数 SQL
	 * @param params 绑定参数
	 * @return 行数
	 */
	public long queryCount(String sql, Object... params) {
		return runner.queryCount(sql, params);
	}

	/**
	 * 执行更新 / 删除 / DDL。
	 *
	 * @param sql    执行 SQL
	 * @param params 绑定参数
	 * @return 影响行数
	 */
	public int execute(String sql, Object... params) {
		return runner.execute(sql, params);
	}

	/**
	 * 插入实体。
	 *
	 * @param entity 实体
	 * @return 影响行数
	 */
	public int insert(Entity entity) {
		return runner.insert(entity);
	}

	/**
	 * 插入实体并返回生成主键。
	 *
	 * @param entity 实体
	 * @return 生成主键
	 */
	public Object insertReturnKey(Entity entity) {
		return runner.insertReturnKey(entity);
	}

	/**
	 * 按条件更新实体。
	 *
	 * @param entity 待更新实体
	 * @param where  条件实体
	 * @return 影响行数
	 */
	public int update(Entity entity, Entity where) {
		return runner.update(entity, where);
	}

	/**
	 * 按 SQL 更新。
	 *
	 * @param sql    更新 SQL
	 * @param params 绑定参数
	 * @return 影响行数
	 */
	public int update(String sql, Object... params) {
		return runner.update(sql, params);
	}

	/**
	 * 按条件删除。
	 *
	 * @param entity 实体（取表名）
	 * @param where  条件实体
	 * @return 影响行数
	 */
	public int del(Entity entity, Entity where) {
		return runner.del(entity, where);
	}

	/**
	 * 按 SQL 删除。
	 *
	 * @param sql    删除 SQL
	 * @param params 绑定参数
	 * @return 影响行数
	 */
	public int del(String sql, Object... params) {
		return runner.del(sql, params);
	}

	/**
	 * 分页查询（LIMIT/OFFSET 方言）。
	 *
	 * @param sql      查询 SQL（不含 LIMIT）
	 * @param params   绑定参数
	 * @param pageNo   页码
	 * @param pageSize 每页条数
	 * @return 分页结果
	 */
	public PageResult page(String sql, Object[] params, int pageNo, int pageSize) {
		return runner.page(sql, params, pageNo, pageSize);
	}

	/**
	 * 事务执行。
	 *
	 * @param consumer 事务回调
	 */
	public void transaction(SqlRunner.SqlConsumer<SqlRunner> consumer) {
		runner.transaction(consumer);
	}

	/**
	 * 事务执行（别名）。
	 *
	 * @param consumer 事务回调
	 */
	public void tx(SqlRunner.SqlConsumer<SqlRunner> consumer) {
		runner.transaction(consumer);
	}
}
