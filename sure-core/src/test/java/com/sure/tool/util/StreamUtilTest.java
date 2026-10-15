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
package com.sure.tool.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 39：StreamUtil 新建工具类测试（of 系列 / concat / nullToEmpty / empty）。
 *
 * @author suretool
 * @since 1.19.0
 */
public class StreamUtilTest {

	@Test
	public void testOfVarargs() {
		Assert.assertEquals(Arrays.asList("a", "b", "c"),
				StreamUtil.of("a", "b", "c").collect(Collectors.toList()));
		Assert.assertEquals(0L, StreamUtil.of((Object[]) null).count());
		Assert.assertEquals(0L, StreamUtil.of().count());
	}

	@Test
	public void testOfCollection() {
		List<String> list = Arrays.asList("x", "y");
		Assert.assertEquals(list, StreamUtil.of(list).collect(Collectors.toList()));
		Assert.assertEquals(0L, StreamUtil.of((List<String>) null).count());
	}

	@Test
	public void testOfIterable() {
		Iterable<String> iterable = () -> Arrays.asList("1", "2").iterator();
		Assert.assertEquals(Arrays.asList("1", "2"), StreamUtil.of(iterable).collect(Collectors.toList()));
		Assert.assertEquals(0L, StreamUtil.of((Iterable<String>) null).count());
	}

	@Test
	public void testOfOptional() {
		Assert.assertEquals(Arrays.asList("v"), StreamUtil.of(Optional.of("v")).collect(Collectors.toList()));
		Assert.assertEquals(0L, StreamUtil.of(Optional.empty()).count());
		Assert.assertEquals(0L, StreamUtil.of((Optional<String>) null).count());
	}

	@Test
	public void testConcat() {
		List<Integer> result = StreamUtil.concat(Stream.of(1), null, Stream.of(2)).collect(Collectors.toList());
		Assert.assertEquals(Arrays.asList(1, 2), result);
		Assert.assertEquals(0L, StreamUtil.concat((Stream<Integer>[]) null).count());
		Assert.assertEquals(0L, StreamUtil.concat(null, null).count());
		Assert.assertEquals(Arrays.asList(1), StreamUtil.concat(Stream.of(1)).collect(Collectors.toList()));
	}

	@Test
	public void testNullToEmpty() {
		Assert.assertEquals(Arrays.asList("a"), StreamUtil.nullToEmpty(Stream.of("a")).collect(Collectors.toList()));
		Assert.assertEquals(0L, StreamUtil.nullToEmpty(null).count());
	}

	@Test
	public void testEmpty() {
		Assert.assertEquals(0L, StreamUtil.empty().count());
		Assert.assertTrue(StreamUtil.empty().findAny().isEmpty());
	}
}
