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

import java.math.BigDecimal;
import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据库实体，Map 风格（有序字段 + 表名）。
 *
 * <p>继承 {@link LinkedHashMap}，与 Map 生态完全互通，支持链式设置与类型化取值，
 * 对齐 Hutool {@code Entity} 的用法。</p>
 *
 * @author suretool
 * @since 1.2.0
 */
public class Entity extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/** 表名 */
	private String tableName;

	/**
	 * 创建空实体（无表名）。
	 *
	 * @return 实体
	 */
	public static Entity create() {
		return new Entity();
	}

	/**
	 * 创建指定表名的实体。
	 *
	 * @param tableName 表名
	 * @return 实体
	 */
	public static Entity create(String tableName) {
		return new Entity(tableName);
	}

	/**
	 * 由已有 Map 创建实体。
	 *
	 * @param map 初始字段
	 * @return 实体
	 */
	public static Entity create(Map<String, Object> map) {
		Entity entity = new Entity();
		if (map != null) {
			entity.putAll(map);
		}
		return entity;
	}

	/**
	 * 构造空实体。
	 */
	public Entity() {
	}

	/**
	 * 构造指定表名的实体。
	 *
	 * @param tableName 表名
	 */
	public Entity(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 获取表名。
	 *
	 * @return 表名，可能为 null
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 设置表名（链式）。
	 *
	 * @param tableName 表名
	 * @return this
	 */
	public Entity setTableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	/**
	 * 设置字段值（链式）。
	 *
	 * @param key   字段名
	 * @param value 字段值
	 * @return this
	 */
	public Entity set(String key, Object value) {
		put(key, value);
		return this;
	}

	/**
	 * 设置字段值，值为 null 时忽略（链式）。
	 *
	 * @param key   字段名
	 * @param value 字段值
	 * @return this
	 */
	public Entity setIgnoreNull(String key, Object value) {
		if (value != null) {
			put(key, value);
		}
		return this;
	}

	/**
	 * 以字符串读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值字符串，null 安全
	 */
	public String getStr(String key) {
		Object value = get(key);
		return value == null ? null : String.valueOf(value);
	}

	/**
	 * 以整型读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值整型，null 安全
	 */
	public Integer getInt(String key) {
		Object value = get(key);
		return value == null ? null : ((Number) value).intValue();
	}

	/**
	 * 以长整型读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值长整型，null 安全
	 */
	public Long getLong(String key) {
		Object value = get(key);
		return value == null ? null : ((Number) value).longValue();
	}

	/**
	 * 以双精度读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值双精度，null 安全
	 */
	public Double getDouble(String key) {
		Object value = get(key);
		return value == null ? null : ((Number) value).doubleValue();
	}

	/**
	 * 以 BigDecimal 读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值 BigDecimal，null 安全
	 */
	public BigDecimal getBigDecimal(String key) {
		Object value = get(key);
		return value == null ? null : new BigDecimal(String.valueOf(value));
	}

	/**
	 * 以 java.sql.Date 读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值 Date，null 安全
	 */
	public Date getDate(String key) {
		Object value = get(key);
		return value instanceof Date ? (Date) value : null;
	}

	/**
	 * 以布尔读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值布尔，null 安全
	 */
	public Boolean getBool(String key) {
		Object value = get(key);
		return value == null ? null : Boolean.valueOf(String.valueOf(value));
	}

	/**
	 * 以字节数组读取字段值。
	 *
	 * @param key 字段名
	 * @return 字段值字节数组，null 安全
	 */
	public byte[] getBytes(String key) {
		Object value = get(key);
		return value == null ? null : (byte[]) value;
	}

	@Override
	public Entity clone() {
		// LinkedHashMap 已实现 Cloneable 且 clone() 不抛受检异常
		Entity copy = (Entity) super.clone();
		copy.tableName = this.tableName;
		return copy;
	}

	/**
	 * 判断两实体是否等价（忽略表名，仅比较字段）。
	 *
	 * @param o 目标对象
	 * @return 字段完全一致返回 true
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Entity)) {
			return false;
		}
		Entity that = (Entity) o;
		return Objects.equals(this.tableName, that.tableName) && super.equals(that);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableName, super.hashCode());
	}
}
