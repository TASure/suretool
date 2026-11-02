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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 52：版本号比较测试（数字段、前导零、预发布、构建元数据、空值）。
 *
 * @author suretool
 * @since 1.20.0
 */
public class VersionComparatorTest {

	private static final VersionComparator COMPARATOR = VersionComparator.INSTANCE;

	@Test
	public void testNumericSegments() {
		Assert.assertTrue(COMPARATOR.compare("1.10.0", "1.9.0") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.9.0", "1.10.0") < 0);
		Assert.assertTrue(COMPARATOR.compare("2.0", "10.0") < 0);
		Assert.assertEquals(0, COMPARATOR.compare("1.0", "1.0.0"));
		Assert.assertEquals(0, COMPARATOR.compare("01.002", "1.2"));
		Assert.assertEquals(0, COMPARATOR.compare("1.2", "1.002"));
		Assert.assertEquals(0, COMPARATOR.compare("1.0.0", "1.0.0.0"));
		Assert.assertTrue(COMPARATOR.compare("1.0", "1.0.5") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.5", "1.0") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.a", "1.1") < 0);
	}

	@Test
	public void testPrerelease() {
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha", "1.0.0") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0", "1.0.0-rc1") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha", "1.0.0-alpha.1") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha.1", "1.0.0-alpha") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha.1", "1.0.0-alpha.2") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha", "1.0.0-beta") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-1", "1.0.0-alpha") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0-alpha", "1.0.0-beta.1") < 0);
	}

	@Test
	public void testBuildMetadataAndWhitespace() {
		Assert.assertEquals(0, COMPARATOR.compare("1.0.0+build1", "1.0.0+build2"));
		Assert.assertEquals(0, COMPARATOR.compare(" 1.2.3 ", "1.2.3"));
		Assert.assertEquals(0, COMPARATOR.compare("1.0.0-rc.1+build", "1.0.0-rc.1+build2"));
		Assert.assertEquals(0, COMPARATOR.compare("1.0.0-", "1.0.0"));
		Assert.assertEquals(0, COMPARATOR.compare("1.0.0-ALPHA", "1.0.0-alpha"));
	}

	@Test
	public void testMixedIdentifiers() {
		Assert.assertTrue(COMPARATOR.compare("1.0.0rc1", "1.0.0") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.0rc1", "1.0.0rc2") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0a", "1.0.1") > 0);
		Assert.assertTrue(COMPARATOR.compare("1.0.1", "1.0a") < 0);
		Assert.assertEquals(0, COMPARATOR.compare("1.0a", "1.0A"));
	}

	@Test
	public void testNewInstance() {
		VersionComparator comparator = new VersionComparator();
		Assert.assertEquals(0, comparator.compare("1.0.0", "1.0.0"));
		Assert.assertTrue(comparator.compare("1.10.0", "1.9.0") > 0);
	}

	@Test
	public void testNullOrdering() {
		Assert.assertEquals(0, COMPARATOR.compare(null, null));
		Assert.assertTrue(COMPARATOR.compare(null, "1.0") < 0);
		Assert.assertTrue(COMPARATOR.compare("1.0", null) > 0);
	}

	@Test
	public void testSort() {
		List<String> versions = new ArrayList<>(Arrays.asList(
				"1.0.0", "1.0.0-alpha", "1.10.0", "1.9.0", "2.0.0-rc1", "2.0.0"));
		versions.sort(COMPARATOR);
		Assert.assertEquals(Arrays.asList(
				"1.0.0-alpha", "1.0.0", "1.9.0", "1.10.0", "2.0.0-rc1", "2.0.0"), versions);
		Assert.assertEquals("1.0.0-alpha", Collections.min(versions, COMPARATOR));
	}
}
