package com.sure.tool.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * BeanUtil / BeanDesc / FieldUtil 测试。
 */
public class BeanUtilTest {

	public static class Address {
		private String city;

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}
	}

	public static class User {
		private String name;
		private int age;
		private boolean active;
		private Address address;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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
	}

	public static class UserDto {
		private String name;
		private Integer age;

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
	}

	@Test
	public void testCopyPropertiesWithConvert() {
		User source = new User();
		source.setName("sure");
		source.setAge(18);
		source.setActive(true);

		UserDto target = new UserDto();
		BeanUtil.copyProperties(source, target);

		assertEquals("sure", target.getName());
		assertEquals(Integer.valueOf(18), target.getAge());
	}

	@Test
	public void testCopyPropertiesIgnoreNull() {
		User source = new User();
		source.setName("sure");
		source.setAge(18);
		source.setAddress(null);

		User target = new User();
		target.setAge(30);
		target.setAddress(new Address());
		BeanUtil.copyProperties(source, target, true);

		assertEquals("sure", target.getName());
		assertEquals(18, target.getAge());
		assertNotNull(target.getAddress());
	}

	@Test
	public void testCopyPropertiesIgnoreList() {
		User source = new User();
		source.setName("sure");
		source.setAge(18);

		User target = new User();
		BeanUtil.copyProperties(source, target, "age");

		assertEquals("sure", target.getName());
		assertEquals(0, target.getAge());
	}

	@Test
	public void testBeanToMap() {
		User user = new User();
		user.setName("sure");
		user.setAge(18);
		user.setActive(true);

		Map<String, Object> map = BeanUtil.beanToMap(user);
		assertEquals("sure", map.get("name"));
		assertEquals(18, map.get("age"));
		assertEquals(Boolean.TRUE, map.get("active"));
		assertTrue(map.containsKey("address"));

		Map<String, Object> map2 = BeanUtil.beanToMap(user, true);
		assertFalse(map2.containsKey("address"));
	}

	@Test
	public void testMapToBean() {
		Map<String, Object> map = new java.util.LinkedHashMap<>();
		map.put("name", "sure");
		map.put("age", "18");
		map.put("active", true);

		User user = BeanUtil.mapToBean(map, User.class);
		assertNotNull(user);
		assertEquals("sure", user.getName());
		assertEquals(18, user.getAge());
		assertTrue(user.isActive());
	}

	@Test
	public void testGetSetProperty() {
		User user = new User();
		BeanUtil.setProperty(user, "name", "sure");
		BeanUtil.setProperty(user, "age", 20);

		assertEquals("sure", BeanUtil.getProperty(user, "name"));
		assertEquals(20, BeanUtil.getProperty(user, "age"));
	}

	@Test
	public void testGetSetPropertyInvalid() {
		User user = new User();
		BeanUtil.setProperty(user, "notExist", 1);
		assertNull(BeanUtil.getProperty(user, "notExist"));
	}

	@Test
	public void testIsBean() {
		assertTrue(BeanUtil.isBean(User.class));
		assertFalse(BeanUtil.isBean(String.class));
		assertFalse(BeanUtil.isBean(List.class));
		assertFalse(BeanUtil.isBean(int.class));
		assertFalse(BeanUtil.isBean(null));
	}

	@Test
	public void testBeanDesc() {
		BeanDesc desc = BeanUtil.getBeanDesc(User.class);
		assertTrue(desc.containsProp("name"));
		assertTrue(desc.containsProp("age"));
		assertTrue(desc.containsProp("active"));
		assertNotNull(desc.getProp("name").getGetter());
		assertNotNull(desc.getProp("name").getSetter());
		assertTrue(desc.getPropNames().contains("address"));
	}

	@Test
	public void testFieldUtil() {
		java.lang.reflect.Field[] fields = FieldUtil.getFields(User.class);
		boolean hasName = false;
		for (java.lang.reflect.Field field : fields) {
			if ("name".equals(field.getName())) {
				hasName = true;
			}
		}
		assertTrue(hasName);
		assertTrue(FieldUtil.getFieldNames(User.class).contains("age"));
		assertNotNull(FieldUtil.getField(User.class, "address"));
		assertNull(FieldUtil.getField(User.class, "notExist"));
		assertNull(FieldUtil.getConstantValue(User.class, "name"));
	}
}
