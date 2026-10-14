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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * {@link BeanCopier} 批38覆盖率补测：静态门面、链式 API、深拷贝、异常路径。
 */
public class BeanCopierB38CoverageTest {

	/** 简单源对象。 */
	public static class Src {
		private String name;
		private Integer age;
		private boolean active;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	/** 简单目标对象。 */
	public static class Dst {
		private String name;
		private Integer age;
		private boolean active;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}
	}

	/** 无 setter、仅 public 字段的目标（走 field 注入分支）。 */
	public static class PublicFieldDst {
		public String name;
		public Integer age;
	}

	/** 只读目标（仅 getter，无 setter、无字段）。 */
	public static class ReadonlyDst {
		private final String fixed = "fixed";

		public String getFixed() {
			return fixed;
		}
	}

	/** 带只读属性的源（与目标 ReadonlyDst 同名属性）。 */
	public static class OnlyFixedSrc {
		private String fixed = "src-fixed";

		public String getFixed() {
			return fixed;
		}
	}

	/** getter 抛异常的对象。 */
	public static class BadGetterSrc {
		public String getName() {
			throw new IllegalStateException("getter boom");
		}
	}

	/** 无无参构造器的目标。 */
	public static class NoDefaultCtor {
		@SuppressWarnings("unused")
		private final String name;

		NoDefaultCtor(String name) {
			this.name = name;
		}
	}

	/** 嵌套对象。 */
	public static class Address {
		private String city;
		private List<String> tags;
		private Map<String, String> meta;

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public Map<String, String> getMeta() {
			return meta;
		}

		public void setMeta(Map<String, String> meta) {
			this.meta = meta;
		}
	}

	public static class Holder {
		private String id;
		private Address address;
		private List<Integer> numbers;
		private Set<String> labels;
		private String[] words;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(List<Integer> numbers) {
			this.numbers = numbers;
		}

		public Set<String> getLabels() {
			return labels;
		}

		public void setLabels(Set<String> labels) {
			this.labels = labels;
		}

		public String[] getWords() {
			return words;
		}

		public void setWords(String[] words) {
			this.words = words;
		}
	}

	private static Src src(String name, Integer age, boolean active) {
		Src s = new Src();
		s.setName(name);
		s.setAge(age);
		s.setActive(active);
		return s;
	}

	@Test
	public void copy双参浅拷贝() {
		Dst dst = BeanCopier.copy(src("a", 1, true), Dst.class);
		assertEquals("a", dst.getName());
		assertEquals(Integer.valueOf(1), dst.getAge());
		assertTrue(dst.isActive());
	}

	@Test
	public void copy拷贝到已有对象() {
		Dst dst = new Dst();
		BeanCopier.copy(src("b", 2, false), dst);
		assertEquals("b", dst.getName());
		assertEquals(Integer.valueOf(2), dst.getAge());
	}

	@Test
	public void copy到已有对象带选项() {
		Dst dst = new Dst();
		dst.setName("keep");
		BeanCopier.copy(src(null, 2, false), dst, CopyOption.IGNORE_NULL);
		assertEquals("keep", dst.getName());
		assertEquals(Integer.valueOf(2), dst.getAge());
	}

	@Test
	public void deepCopy门面() {
		Holder h = new Holder();
		h.setId("h1");
		Address addr = new Address();
		addr.setCity("西安");
		List<String> tags = new ArrayList<>(Arrays.asList("t1", "t2"));
		addr.setTags(tags);
		h.setAddress(addr);
		h.setNumbers(new ArrayList<>(Arrays.asList(1, 2)));
		h.setLabels(new LinkedHashSet<>(Arrays.asList("x", "y")));
		h.setWords(new String[] { "w1", "w2" });
		Holder copy = BeanCopier.deepCopy(h, Holder.class);
		assertEquals("h1", copy.getId());
		assertEquals("西安", copy.getAddress().getCity());
		assertNotSame(addr, copy.getAddress());
		assertNotSame(tags, copy.getAddress().getTags());
		assertEquals(2, copy.getNumbers().size());
		assertEquals(2, copy.getLabels().size());
		assertEquals(2, copy.getWords().length);
		assertNotSame(h.getWords(), copy.getWords());
	}

	@Test
	public void deepCopy嵌套Bean独立() {
		Holder h = new Holder();
		Address addr = new Address();
		addr.setCity("原城市");
		h.setAddress(addr);
		Holder copy = BeanCopier.deepCopy(h, Holder.class);
		copy.getAddress().setCity("改城市");
		assertEquals("原城市", h.getAddress().getCity());
	}

	@Test
	public void deepCopyMap和Set() {
		Holder h = new Holder();
		Address addr = new Address();
		Map<String, String> meta = new java.util.LinkedHashMap<>();
		meta.put("k", "v");
		addr.setMeta(meta);
		h.setAddress(addr);
		Holder copy = BeanCopier.deepCopy(h, Holder.class);
		assertNotSame(meta, copy.getAddress().getMeta());
		assertEquals("v", copy.getAddress().getMeta().get("k"));
	}

	@Test
	public void toBean未知key跳过() {
		Src bean = BeanCopier.toBean(Map.of("name", "n", "notExist", 1), Src.class);
		assertEquals("n", bean.getName());
	}

	@Test
	public void toBean基本类型null跳过() {
		java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
		map.put("age", 3);
		map.put("active", null);
		Src bean = BeanCopier.toBean(map, Src.class);
		assertEquals(Integer.valueOf(3), bean.getAge());
	}

	@Test
	public void 链式ignoreNull() {
		Src s = src(null, 5, true);
		Dst dst = BeanCopier.of(s).ignoreNull().to(Dst.class);
		assertNull(dst.getName());
		assertEquals(Integer.valueOf(5), dst.getAge());
	}

	@Test
	public void 链式ignoreReadonly() {
		Src s = new Src();
		s.setName("x");
		ReadonlyDst dst = BeanCopier.of(s).ignoreReadonly().to(ReadonlyDst.class);
		assertEquals("fixed", dst.getFixed());
	}

	@Test
	public void 链式include() {
		Src s = src("a", 1, true);
		Dst dst = BeanCopier.of(s).include("name").to(Dst.class);
		assertEquals("a", dst.getName());
		assertNull(dst.getAge());
	}

	@Test
	public void 链式exclude() {
		Src s = src("a", 1, true);
		Dst dst = BeanCopier.of(s).exclude("age", "active").to(Dst.class);
		assertEquals("a", dst.getName());
		assertNull(dst.getAge());
	}

	@Test
	public void 链式includeExclude互斥() {
		Src s = src("a", 1, true);
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(s).include("name").exclude("age"));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(s).exclude("age").include("name"));
	}

	@Test
	public void 链式to已有对象() {
		Src s = src("a", 1, true);
		Dst dst = new Dst();
		BeanCopier.of(s).include("name").to(dst);
		assertEquals("a", dst.getName());
		assertNull(dst.getAge());
	}

	@Test
	public void 链式deep() {
		Holder h = new Holder();
		Address addr = new Address();
		addr.setCity("c");
		h.setAddress(addr);
		Holder copy = BeanCopier.of(h).deep().to(Holder.class);
		assertNotSame(addr, copy.getAddress());
	}

	@Test
	public void include空字段名抛IAE() {
		Src s = src("a", 1, true);
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(s).include("name", ""));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(s).include((String) null));
	}

	@Test
	public void 无无参构造器抛CopyException() {
		Src s = src("a", 1, true);
		assertThrows(CopyException.class, () -> BeanCopier.copy(s, NoDefaultCtor.class));
	}

	@Test
	public void getter抛异常抛CopyException() {
		BadGetterSrc s = new BadGetterSrc();
		assertThrows(CopyException.class, () -> BeanCopier.copy(s, Dst.class));
	}

	@Test
	public void 目标只读默认抛CopyException() {
		OnlyFixedSrc s = new OnlyFixedSrc();
		assertThrows(CopyException.class, () -> BeanCopier.copy(s, ReadonlyDst.class));
	}

	@Test
	public void 链式ignoreReadonly跳过只读() {
		OnlyFixedSrc s = new OnlyFixedSrc();
		ReadonlyDst dst = BeanCopier.of(s).ignoreReadonly().to(ReadonlyDst.class);
		assertEquals("fixed", dst.getFixed());
	}

	@Test
	public void public字段目标走field注入() {
		Src s = src("f", 7, true);
		PublicFieldDst dst = BeanCopier.copy(s, PublicFieldDst.class);
		assertEquals("f", dst.name);
		assertEquals(Integer.valueOf(7), dst.age);
	}

	@Test
	public void 枚举转换失败抛CopyException() {
		java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
		map.put("level", "NOT_A_LEVEL");
		assertThrows(CopyException.class, () -> BeanCopier.toBean(map, EnumTarget.class));
	}

	public static class EnumTarget {
		private Level level;

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}

	public enum Level {
		LOW, HIGH
	}

	@Test
	public void null参数抛IAE() {
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(null, Dst.class));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(src("a", 1, true), (Class<?>) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(null, new Dst()));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.copy(src("a", 1, true), (Object) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.toBean(null, Src.class));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.toBean(Map.of(), (Class<?>) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(src("a", 1, true)).to((Class<?>) null));
		assertThrows(IllegalArgumentException.class, () -> BeanCopier.of(src("a", 1, true)).to((Object) null));
	}
}
