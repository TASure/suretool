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

package com.sure.tool.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@link BeanCopier} 批38补测：选项化/链式拷贝、类型转换、深浅拷贝、Bean↔Map。
 */
public class BeanCopierBatch38Test {

	/** 测试枚举。 */
	enum Level {
		LOW, HIGH
	}

	/** 嵌套 Bean。 */
	public static class Address {
		private String city;
		private String street;

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}
	}

	/** 源 Bean：字段类型与目标不同，验证类型转换。 */
	public static class Source {
		private String id;
		private String age;
		private String active;
		private Address address;
		private List<String> tags;
		private Level level;
		private String status;
		private String readonly;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getAge() {
			return age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public String getActive() {
			return active;
		}

		public void setActive(String active) {
			this.active = active;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getReadonly() {
			return readonly;
		}

		public void setReadonly(String readonly) {
			this.readonly = readonly;
		}
	}

	/** 目标 Bean：id 为 long、age 为 int、active 为 boolean、status 为枚举，另有只读属性。 */
	public static class Target {
		private long id;
		private int age;
		private boolean active;
		private Address address;
		private List<String> tags;
		private Level level;
		private Level status;
		private final String readonly = "fixed";

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}

		public Level getStatus() {
			return status;
		}

		public void setStatus(Level status) {
			this.status = status;
		}

		public String getReadonly() {
			return readonly;
		}
	}

	private static Source newSource() {
		Source source = new Source();
		source.setId("42");
		source.setAge("18");
		source.setActive("true");
		Address address = new Address();
		address.setCity("西安");
		address.setStreet("科技路");
		source.setAddress(address);
		List<String> tags = new ArrayList<>();
		tags.add("a");
		tags.add("b");
		source.setTags(tags);
		source.setLevel(Level.HIGH);
		source.setStatus("LOW");
		source.setReadonly("src");
		return source;
	}

	@Test
	public void 基础拷贝自动类型转换() {
		Target target = BeanCopier.copy(newSource(), Target.class, CopyOption.IGNORE_READONLY);
		assertEquals(42L, target.getId());
		assertEquals(18, target.getAge());
		assertTrue(target.isActive());
		assertEquals(Level.HIGH, target.getLevel());
		assertEquals(Level.LOW, target.getStatus());
		assertEquals("西安", target.getAddress().getCity());
		assertEquals(2, target.getTags().size());
	}

	@Test
	public void 拷贝到已有目标覆盖旧值() {
		Target target = new Target();
		target.setId(1L);
		BeanCopier.copy(newSource(), target, CopyOption.IGNORE_READONLY);
		assertEquals(42L, target.getId());
		assertEquals(18, target.getAge());
	}

	@Test
	public void null源值默认覆盖对象属性() {
		Source source = newSource();
		source.setAge(null);
		source.setAddress(null);
		Target target = new Target();
		target.setAge(99);
		Address old = new Address();
		old.setCity("旧城");
		target.setAddress(old);
		BeanCopier.copy(source, target, CopyOption.IGNORE_READONLY);
		// 基本类型目标无法赋 null，保持原值；对象属性 null 覆盖
		assertEquals(99, target.getAge());
		assertNull(target.getAddress());
	}

	@Test
	public void ignoreNull跳过null值() {
		Source source = newSource();
		source.setAge(null);
		source.setActive(null);
		Target target = new Target();
		target.setAge(99);
		BeanCopier.copy(source, target, CopyOption.IGNORE_NULL, CopyOption.IGNORE_READONLY);
		assertEquals(99, target.getAge());
		// active 为 null 被跳过，保持目标初始 false
		org.junit.Assert.assertFalse(target.isActive());
	}

	@Test
	public void 目标只读默认抛CopyException() {
		Source source = newSource();
		Target target = new Target();
		// readonly 只有 getter 无 setter，默认严格模式应抛异常
		CopyException e = assertThrows(CopyException.class, () -> BeanCopier.copy(source, target));
		assertTrue(e.getMessage().contains("readonly"));
	}

	@Test
	public void ignoreReadonly跳过只读属性() {
		Target target = BeanCopier.copy(newSource(), Target.class, CopyOption.IGNORE_READONLY);
		assertEquals(42L, target.getId());
		assertEquals("fixed", target.getReadonly());
	}

	@Test
	public void 链式ignoreReadonly() {
		Target target = BeanCopier.of(newSource()).ignoreReadonly().to(Target.class);
		assertEquals(42L, target.getId());
		assertEquals("fixed", target.getReadonly());
	}

	@Test
	public void 链式include白名单() {
		Target target = BeanCopier.of(newSource()).ignoreReadonly().include("id", "age").to(Target.class);
		assertEquals(42L, target.getId());
		assertEquals(18, target.getAge());
		assertNull(target.getAddress());
		assertNull(target.getLevel());
	}

	@Test
	public void 链式exclude黑名单() {
		Target target = BeanCopier.of(newSource()).ignoreReadonly().exclude("address", "tags").to(Target.class);
		assertEquals(42L, target.getId());
		assertNull(target.getAddress());
		assertNull(target.getTags());
		assertEquals(Level.HIGH, target.getLevel());
	}

	@Test
	public void include与exclude互斥() {
		assertThrows(IllegalArgumentException.class,
				() -> BeanCopier.of(newSource()).include("id").exclude("age"));
	}

	@Test
	public void 浅拷贝共享嵌套引用() {
		Source source = newSource();
		Target target = BeanCopier.copy(source, Target.class, CopyOption.IGNORE_READONLY);
		assertSame(source.getAddress(), target.getAddress());
		assertSame(source.getTags(), target.getTags());
	}

	@Test
	public void 深拷贝嵌套独立() {
		Source source = newSource();
		Target target = BeanCopier.copy(source, Target.class, CopyOption.IGNORE_READONLY, CopyOption.DEEP);
		assertNotSame(source.getAddress(), target.getAddress());
		assertNotSame(source.getTags(), target.getTags());
		assertEquals("西安", target.getAddress().getCity());
		assertEquals("a", target.getTags().get(0));
	}

	@Test
	public void 链式deep() {
		Source source = newSource();
		Target target = BeanCopier.of(source).ignoreReadonly().deep().to(Target.class);
		assertNotSame(source.getAddress(), target.getAddress());
		assertNotSame(source.getTags(), target.getTags());
		assertEquals(2, target.getTags().size());
	}

	@Test
	public void bean转Map() {
		Map<String, Object> map = BeanCopier.toMap(newSource());
		assertEquals("42", map.get("id"));
		assertEquals(Level.HIGH, map.get("level"));
	}

	@Test
	public void map转Bean带转换() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", "7");
		map.put("age", "30");
		map.put("active", "false");
		map.put("level", "LOW");
		Target target = BeanCopier.toBean(map, Target.class);
		assertEquals(7L, target.getId());
		assertEquals(30, target.getAge());
		assertEquals(Level.LOW, target.getLevel());
	}

	@Test
	public void 类型转换失败抛CopyException带属性名() {
		Source source = newSource();
		source.setStatus("UNKNOWN");
		// include 限定只处理 status，避免依赖属性遍历顺序
		CopyException e = assertThrows(CopyException.class,
				() -> BeanCopier.of(source).include("status").to(Target.class));
		assertTrue(e.getMessage().contains("status"));
	}

	@Test
	public void null参数抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(null, Target.class));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(newSource(), (Class<Target>) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(newSource(), (Object) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(null));
	}

	@Test
	public void 无法实例化目标抛CopyException() {
		// 无无参构造器的类
		class NoNoArg {
			@SuppressWarnings("unused")
			NoNoArg(String x) {
			}
		}
		assertThrows(CopyException.class, () -> BeanCopier.copy(newSource(), NoNoArg.class));
	}

	@Test
	public void 深拷贝Bean的map字段() {
		Source source = newSource();
		Map<String, Object> map = BeanCopier.toMap(source);
		// toMap 的浅拷贝 map 中 address 与原 bean 共享引用
		assertSame(source.getAddress(), map.get("address"));
	}
}
