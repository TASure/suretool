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
package com.sure.tool.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 41：CollUtil 多键分组 / 笛卡尔积测试（对标 Guava Multimaps / Sets）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class CollUtilBatch41Test {

	private static class User {
		String name;
		String city;
		String dept;

		User(String name, String city, String dept) {
			this.name = name;
			this.city = city;
			this.dept = dept;
		}
	}

	@Test
	public void testGroupByTwoKeys() {
		List<User> users = Arrays.asList(
				new User("a", "西安", "后端"),
				new User("b", "西安", "后端"),
				new User("c", "西安", "前端"),
				new User("d", "北京", "后端"));
		Map<String, Map<String, List<User>>> grouped =
				CollUtil.groupBy(users, u -> u.city, u -> u.dept);
		Assert.assertEquals(2, grouped.size());
		Assert.assertEquals(2, grouped.get("西安").get("后端").size());
		Assert.assertEquals(1, grouped.get("西安").get("前端").size());
		Assert.assertEquals(1, grouped.get("北京").get("后端").size());
		// null 与空输入
		Assert.assertTrue(CollUtil.<User, String, String>groupBy(null, u -> u.city, u -> u.dept).isEmpty());
		Assert.assertTrue(CollUtil.groupBy(new ArrayList<User>(), u -> u.city, u -> u.dept).isEmpty());
	}

	@Test
	public void testGroupByMultiKeys() {
		List<User> users = Arrays.asList(
				new User("a", "西安", "后端"),
				new User("b", "西安", "后端"),
				new User("c", "西安", "前端"));
		// 三键链式分组（只匹配变参版，返回嵌套 Map）
		Map<Object, Object> grouped = CollUtil.groupBy(users, u -> u.city, u -> u.dept, u -> u.name);
		Assert.assertEquals(1, grouped.size());
		Map<?, ?> xian = (Map<?, ?>) grouped.get("西安");
		Assert.assertNotNull(xian);
		Map<?, ?> houduan = (Map<?, ?>) xian.get("后端");
		Assert.assertNotNull(houduan);
		Assert.assertEquals(1, ((List<?>) houduan.get("a")).size());
		// 无键 / null
		Assert.assertTrue(CollUtil.groupBy(users).isEmpty());
		Assert.assertTrue(CollUtil.<User>groupBy(null, (User u) -> u.city).isEmpty());
		Assert.assertTrue(CollUtil.groupBy(users, (java.util.function.Function<User, ?>) null).isEmpty());
	}

	@Test
	public void testCartesianProductCollection() {
		Collection<Collection<Integer>> input = Arrays.asList(
				Arrays.asList(1, 2), Arrays.asList(3, 4));
		List<List<Integer>> result = CollUtil.cartesianProduct(input);
		Assert.assertEquals(4, result.size());
		Assert.assertEquals(Arrays.asList(1, 3), result.get(0));
		Assert.assertEquals(Arrays.asList(2, 4), result.get(3));

		// 空维度 / null
		Collection<Collection<Integer>> withEmpty = Arrays.asList(
				Arrays.asList(1), Collections.emptyList());
		Assert.assertTrue(CollUtil.cartesianProduct(withEmpty).isEmpty());
		Assert.assertTrue(CollUtil.cartesianProduct(null).isEmpty());
	}
}
