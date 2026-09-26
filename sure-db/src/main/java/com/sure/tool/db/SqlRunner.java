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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

/**
 * SQL 执行器：负责连接获取/归还、参数绑定、ResultSet 到 {@link Entity} 的映射，
 * 以及 CRUD / 事务 / 分页的执行。
 *
 * <p>所有 SQL 均通过 {@link PreparedStatement} 参数绑定执行（防注入）；
 * 每个非事务操作独立借还连接，实例本身无状态、线程安全。
 * 事务模式下固定使用单一连接贯穿回调，保证 ACID。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class SqlRunner {

	/** 数据源（事务模式为 null） */
	private final DataSource dataSource;

	/** 事务专用连接（事务模式非 null） */
	private final Connection txConnection;

	/**
	 * 构造执行器。
	 *
	 * @param dataSource 数据源，非空
	 */
	public SqlRunner(DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource, "dataSource 不能为 null");
		this.txConnection = null;
	}

	/**
	 * 事务专用构造：固定使用给定连接。
	 *
	 * @param txConnection 事务连接
	 */
	private SqlRunner(Connection txConnection) {
		this.dataSource = null;
		this.txConnection = txConnection;
	}

	/**
	 * 执行查询，返回实体列表。
	 *
	 * @param sql    查询 SQL（含 ? 占位符）
	 * @param params 绑定参数，可空
	 * @return 实体列表，永不 null
	 */
	public List<Entity> query(String sql, Object... params) {
		Connection connection = connOrThrow();
		try {
			return query(connection, sql, params);
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 执行查询，返回首行实体。
	 *
	 * @param sql    查询 SQL
	 * @param params 绑定参数，可空
	 * @return 首行实体，无数据返回 null
	 */
	public Entity queryOne(String sql, Object... params) {
		List<Entity> list = query(sql, params);
		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * 执行查询，返回单值数字（如 {@code SELECT COUNT(*)}）。
	 *
	 * @param sql    查询 SQL
	 * @param params 绑定参数，可空
	 * @return 单值数字，无数据返回 null
	 */
	public Number queryNumber(String sql, Object... params) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			bindParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return (Number) rs.getObject(1);
				}
				return null;
			}
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 执行计数查询，返回行数。
	 *
	 * @param sql    计数 SQL（通常为 {@code SELECT COUNT(*)}）
	 * @param params 绑定参数，可空
	 * @return 行数
	 */
	public long queryCount(String sql, Object... params) {
		Number number = queryNumber(sql, params);
		return number == null ? 0L : number.longValue();
	}

	/**
	 * 执行更新 / 删除 / DDL，返回影响行数。
	 *
	 * @param sql    执行 SQL
	 * @param params 绑定参数，可空
	 * @return 影响行数
	 */
	public int execute(String sql, Object... params) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			bindParams(ps, params);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 插入实体（自动生成 INSERT），返回影响行数。
	 *
	 * @param entity 实体，需含表名与非空字段
	 * @return 影响行数
	 */
	public int insert(Entity entity) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(buildInsertSql(entity), Statement.RETURN_GENERATED_KEYS)) {
			bindParams(ps, entity.values().toArray());
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 插入实体并返回数据库生成的主键。
	 *
	 * @param entity 实体，需含表名与非空字段
	 * @return 生成主键，数据库不支持时返回 null
	 */
	public Object insertReturnKey(Entity entity) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(buildInsertSql(entity), Statement.RETURN_GENERATED_KEYS)) {
			bindParams(ps, entity.values().toArray());
			ps.executeUpdate();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) {
					return rs.getObject(1);
				}
				return null;
			}
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 按条件更新实体（SET 为实体字段，WHERE 为条件实体字段，AND 连接）。
	 *
	 * @param entity 待更新实体，需含表名
	 * @param where  条件实体
	 * @return 影响行数
	 */
	public int update(Entity entity, Entity where) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(buildUpdateSql(entity, where))) {
			List<Object> args = new ArrayList<>(entity.values());
			args.addAll(where.values());
			bindParams(ps, args.toArray());
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 按 SQL 更新。
	 *
	 * @param sql    更新 SQL
	 * @param params 绑定参数，可空
	 * @return 影响行数
	 */
	public int update(String sql, Object... params) {
		return execute(sql, params);
	}

	/**
	 * 按条件删除（WHERE 为条件实体字段，AND 连接）。
	 *
	 * @param entity 实体，需含表名
	 * @param where  条件实体
	 * @return 影响行数
	 */
	public int del(Entity entity, Entity where) {
		Connection connection = connOrThrow();
		try (PreparedStatement ps = connection.prepareStatement(buildDeleteSql(entity, where))) {
			bindParams(ps, where.values().toArray());
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			release(connection);
		}
	}

	/**
	 * 按 SQL 删除。
	 *
	 * @param sql    删除 SQL
	 * @param params 绑定参数，可空
	 * @return 影响行数
	 */
	public int del(String sql, Object... params) {
		return execute(sql, params);
	}

	/**
	 * 分页查询（LIMIT/OFFSET 方言）。
	 *
	 * <p>先以子查询统计总数，再追加 {@code LIMIT ? OFFSET ?} 取当前页，
	 * 适用于 MySQL / PostgreSQL / H2 / HSQLDB / SQLite 等 LIMIT 方言数据库；
	 * SQLServer / Oracle 请自行编写分页 SQL。</p>
	 *
	 * @param sql      查询 SQL（不含 LIMIT）
	 * @param params   绑定参数，可空
	 * @param pageNo   页码，从 1 开始
	 * @param pageSize 每页条数，需大于 0
	 * @return 分页结果
	 */
	public PageResult page(String sql, Object[] params, int pageNo, int pageSize) {
		if (pageNo < 1) {
			pageNo = 1;
		}
		if (pageSize < 1) {
			pageSize = 10;
		}
		Object[] base = params == null ? new Object[0] : params;
		long total = queryCount("SELECT COUNT(*) FROM (" + sql + ") __total", base);
		Object[] pageParams = new Object[base.length + 2];
		System.arraycopy(base, 0, pageParams, 0, base.length);
		pageParams[base.length] = pageSize;
		pageParams[base.length + 1] = (long) (pageNo - 1) * pageSize;
		List<Entity> list = query(sql + " LIMIT ? OFFSET ?", pageParams);
		return PageResult.of(pageNo, pageSize, total, list);
	}

	/**
	 * 事务执行：回调内所有操作使用同一连接，成功提交、异常回滚。
	 *
	 * <p>不支持嵌套事务传播；回调抛出的 {@link DbRuntimeException} 原样向上抛，
	 * 其他异常包装为 DbRuntimeException。</p>
	 *
	 * @param consumer 事务回调
	 */
	public void transaction(SqlConsumer<SqlRunner> consumer) {
		Objects.requireNonNull(consumer, "consumer 不能为 null");
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			boolean oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			try {
				consumer.accept(new SqlRunner(connection));
				connection.commit();
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (SQLException rollbackError) {
					rollbackError.addSuppressed(e);
				}
				if (e instanceof DbRuntimeException dbe) {
					throw dbe;
				}
				throw new DbRuntimeException("事务执行失败: " + e.getMessage(), toSqlException(e));
			} finally {
				try {
					connection.setAutoCommit(oldAutoCommit);
				} catch (SQLException ignored) {
					// 恢复失败不影响事务结果
				}
			}
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		} finally {
			if (connection != null) {
				release(connection);
			}
		}
	}

	// ----------------------------------------------------------------------
	// 内部实现
	// ----------------------------------------------------------------------

	/**
	 * 获取执行连接：事务模式返回固定连接，普通模式从数据源借用。
	 *
	 * @return 连接
	 * @throws SQLException 获取失败
	 */
	private Connection conn() throws SQLException {
		return txConnection != null ? txConnection : dataSource.getConnection();
	}

	/**
	 * 获取执行连接，异常包装为 {@link DbRuntimeException}。
	 *
	 * @return 连接
	 */
	private Connection connOrThrow() {
		try {
			return conn();
		} catch (SQLException e) {
			throw DbRuntimeException.of(e);
		}
	}

	/**
	 * 释放连接：事务模式不关闭（由事务外层统一归还），普通模式关闭归还。
	 *
	 * @param connection 连接
	 */
	private void release(Connection connection) {
		if (txConnection == null) {
			closeQuietly(connection);
		}
	}

	/**
	 * 在给定连接上执行查询。
	 *
	 * @param connection 连接
	 * @param sql        查询 SQL
	 * @param params     绑定参数
	 * @return 实体列表
	 * @throws SQLException 执行失败
	 */
	private List<Entity> query(Connection connection, String sql, Object... params) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			bindParams(ps, params);
			try (ResultSet rs = ps.executeQuery()) {
				return toEntityList(rs);
			}
		}
	}

	/**
	 * 绑定参数到预编译语句（null 使用 setNull，其余 setObject）。
	 *
	 * @param ps     预编译语句
	 * @param params 参数列表
	 * @throws SQLException 绑定失败
	 */
	private static void bindParams(PreparedStatement ps, Object... params) throws SQLException {
		if (params == null) {
			return;
		}
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];
			if (param == null) {
				ps.setNull(i + 1, Types.NULL);
			} else {
				ps.setObject(i + 1, param);
			}
		}
	}

	/**
	 * ResultSet 转为实体列表（列名统一小写，兼容 H2/HSQLDB/Oracle 等大写列名驱动）。
	 *
	 * @param rs 结果集
	 * @return 实体列表
	 * @throws SQLException 读取失败
	 */
	private static List<Entity> toEntityList(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();
		List<Entity> list = new ArrayList<>();
		while (rs.next()) {
			Entity entity = new Entity();
			for (int i = 1; i <= columnCount; i++) {
				entity.put(meta.getColumnLabel(i).toLowerCase(java.util.Locale.ROOT), rs.getObject(i));
			}
			list.add(entity);
		}
		return list;
	}

	/**
	 * 构建 INSERT SQL。
	 *
	 * @param entity 实体
	 * @return INSERT 语句
	 */
	private static String buildInsertSql(Entity entity) {
		String table = requireTable(entity);
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (");
		StringBuilder marks = new StringBuilder();
		for (String column : entity.keySet()) {
			sql.append(column).append(',');
			marks.append("?,");
		}
		sql.setLength(sql.length() - 1);
		marks.setLength(marks.length() - 1);
		return sql.append(") VALUES (").append(marks).append(')').toString();
	}

	/**
	 * 构建 UPDATE SQL。
	 *
	 * @param entity 待更新实体
	 * @param where  条件实体
	 * @return UPDATE 语句
	 */
	private static String buildUpdateSql(Entity entity, Entity where) {
		String table = requireTable(entity);
		StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ");
		for (String column : entity.keySet()) {
			sql.append(column).append("=?,");
		}
		sql.setLength(sql.length() - 1);
		sql.append(" WHERE ");
		appendWhere(sql, where);
		return sql.toString();
	}

	/**
	 * 构建 DELETE SQL。
	 *
	 * @param entity 实体（仅取表名）
	 * @param where  条件实体
	 * @return DELETE 语句
	 */
	private static String buildDeleteSql(Entity entity, Entity where) {
		String table = requireTable(entity);
		StringBuilder sql = new StringBuilder("DELETE FROM ").append(table).append(" WHERE ");
		appendWhere(sql, where);
		return sql.toString();
	}

	/**
	 * 拼接 WHERE 条件（AND 连接）。
	 *
	 * @param sql   待拼接的 SQL
	 * @param where 条件实体
	 */
	private static void appendWhere(StringBuilder sql, Entity where) {
		Objects.requireNonNull(where, "where 不能为 null");
		if (where.isEmpty()) {
			throw new IllegalArgumentException("where 条件实体不能为空");
		}
		boolean first = true;
		for (String column : where.keySet()) {
			if (!first) {
				sql.append(" AND ");
			}
			sql.append(column).append("=?");
			first = false;
		}
	}

	/**
	 * 校验并获取实体表名。
	 *
	 * @param entity 实体
	 * @return 表名
	 */
	private static String requireTable(Entity entity) {
		Objects.requireNonNull(entity, "entity 不能为 null");
		String table = entity.getTableName();
		if (table == null || table.isEmpty()) {
			throw new IllegalArgumentException("entity 缺少表名，请使用 Entity.create(tableName)");
		}
		return table;
	}

	/**
	 * 从异常中提取 SQLException 根因。
	 *
	 * @param e 异常
	 * @return SQLException，无则 null
	 */
	private static SQLException toSqlException(Exception e) {
		Throwable cause = e.getCause();
		return cause instanceof SQLException ? (SQLException) cause : null;
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
				// 关闭失败不影响调用方
			}
		}
	}

	/**
	 * 事务回调接口：入参为事务专用执行器。
	 */
	@FunctionalInterface
	public interface SqlConsumer<T> {

		/**
		 * 执行事务内操作。
		 *
		 * @param runner 事务专用执行器
		 * @throws Exception 操作失败
		 */
		void accept(T runner) throws Exception;
	}
}
