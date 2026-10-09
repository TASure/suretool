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
package com.sure.tool.lang;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Option 批26 测试。
 */
public class OptionTest {

	@Test
	public void testConstruct() {
		Assert.assertEquals("v", Option.of("v").get());
		Assert.assertEquals("v", Option.ofNullable("v").get());
		Assert.assertTrue(Option.ofNullable(null).isEmpty());
		Assert.assertTrue(Option.empty().isEmpty());
	}

	@Test
	public void testOfNullThrows() {
		try {
			Option.of(null);
			Assert.fail("应抛 NPE");
		} catch (NullPointerException expected) {
			// 预期
		}
	}

	@Test
	public void testGetThrowsOnEmpty() {
		try {
			Option.empty().get();
			Assert.fail("应抛 NoSuchElementException");
		} catch (NoSuchElementException expected) {
			// 预期
		}
	}

	@Test
	public void testGetOrElse() {
		Assert.assertEquals("v", Option.ofNullable("v").getOrElse("d"));
		Assert.assertEquals("d", Option.empty().getOrElse("d"));
		Assert.assertEquals("v", Option.ofNullable("v").getOrElseGet(() -> "d"));
		Assert.assertEquals("d", Option.empty().getOrElseGet(() -> "d"));
	}

	@Test
	public void testOrElseOption() {
		Assert.assertEquals("v", Option.ofNullable("v").orElse(Option.ofNullable("d")).get());
		Assert.assertEquals("d", Option.empty().orElse(Option.ofNullable("d")).get());
	}

	@Test
	public void testMapFlatMapFilter() {
		Assert.assertEquals("A", Option.ofNullable("a").map(String::toUpperCase).get());
		Assert.assertTrue(Option.empty().map(x -> 1).isEmpty());
		Assert.assertEquals(Integer.valueOf(20), Option.ofNullable(2).flatMap(x -> Option.ofNullable(x * 10)).get());
		Assert.assertTrue(Option.empty().flatMap(x -> Option.ofNullable(1)).isEmpty());
		Assert.assertEquals("v", Option.ofNullable("v").filter(x -> x.length() > 0).get());
		Assert.assertTrue(Option.ofNullable("v").filter(x -> x.length() > 10).isEmpty());
	}

	@Test
	public void testPeekOnEmptyCallbacks() {
		AtomicInteger peeked = new AtomicInteger();
		AtomicInteger emptied = new AtomicInteger();
		Option.ofNullable("v").peek(x -> peeked.incrementAndGet()).onEmpty(() -> emptied.incrementAndGet());
		Assert.assertEquals(1, peeked.get());
		Assert.assertEquals(0, emptied.get());
		Option.empty().peek(x -> peeked.incrementAndGet()).onEmpty(() -> emptied.incrementAndGet());
		Assert.assertEquals(1, peeked.get());
		Assert.assertEquals(1, emptied.get());
	}

	@Test
	public void testIfPresent() {
		AtomicInteger count = new AtomicInteger();
		Option.ofNullable("v").ifPresent(x -> count.incrementAndGet());
		Option.empty().ifPresent(x -> count.incrementAndGet());
		Assert.assertEquals(1, count.get());
	}

	@Test
	public void testToOptional() {
		Assert.assertEquals(Optional.of("v"), Option.ofNullable("v").toOptional());
		Assert.assertEquals(Optional.empty(), Option.empty().toOptional());
	}
}
