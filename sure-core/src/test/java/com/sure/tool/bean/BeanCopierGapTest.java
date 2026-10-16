/*
 * Copyright 2026 TASure contributors
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@link BeanCopier} 防御分支确定性补测（无时序依赖）。
 */
public class BeanCopierGapTest {

	/** 无无参构造目标。 */
	public static class NoCtor {
		/** 值。 */
		public int age;

		private NoCtor() {
		}
	}

	/** 枚举目标。 */
	public enum Level {
		/** 高。 */
		HIGH,
		/** 低。 */
		LOW
	}

	/** 类型不可转换目标。 */
	public static class BadType {
		/** int 属性。 */
		private int age;

		/** getter。 */
		public int getAge() {
			return age;
		}

		/** setter。 */
		public void setAge(int age) {
			this.age = age;
		}
	}

	/** 只读属性目标（仅 getter 返回常量，无 setter 无字段）。 */
	public static class ReadOnly {
		/** getter（无字段）。 */
		public int getAge() {
			return 42;
		}
	}

	/** setter 抛异常目标（与源属性同名）。 */
	public static class ThrowingSetter {
		private Level level;

		@SuppressWarnings("unused")
		public Level getLevel() {
			return level;
		}

		@SuppressWarnings("unused")
		public void setLevel(Level level) {
			throw new IllegalStateException("boom");
		}
	}

	@Test
	public void 实例化失败抛CopyException() {
		Map<String, Object> map = new HashMap<>();
		map.put("age", 3);
		assertThrows(CopyException.class, () -> BeanCopier.copy(map, ListLike.class));
	}

	/** 抽象目标（实例化失败）。 */
	public abstract static class ListLike {
		/** 值。 */
		public int age;
	}

	@Test
	public void 枚举属性null返回null() {
		Map<String, Object> map = new HashMap<>();
		map.put("level", null);
		EnumTarget t = BeanCopier.copy(map, EnumTarget.class);
		assertNotNull(t);
		assertEquals(null, t.getLevel());
	}

	/** 枚举属性目标。 */
	public static class EnumTarget {
		private Level level;

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}

	/** 可读写源（age 属性）。 */
	public static class AgeHolder {
		private int age;

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}

	@Test
	public void 只读属性抛CopyException() {
		AgeHolder src = new AgeHolder();
		src.setAge(3);
		assertThrows(CopyException.class, () -> BeanCopier.copy(src, new ReadOnly()));
	}

	@Test
	public void setter抛异常包成CopyException() {
		LevelHolder src = new LevelHolder();
		src.setLevel(Level.HIGH);
		assertThrows(CopyException.class, () -> BeanCopier.copy(src, ThrowingSetter.class));
	}

	@Test
	public void 同类型值直接返回() {
		LevelHolder src = new LevelHolder();
		src.setLevel(Level.HIGH);
		LevelHolder dst = BeanCopier.copy(src, LevelHolder.class);
		assertEquals(Level.HIGH, dst.getLevel());
		assertTrue(src != dst);
	}

	/** 同类型属性目标。 */
	public static class LevelHolder {
		private Level level;

		public Level getLevel() {
			return level;
		}

		public void setLevel(Level level) {
			this.level = level;
		}
	}
}
