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

import org.junit.Assert;
import org.junit.Test;

/**
 * 批 52：VersionUtil 门面测试。
 *
 * @author suretool
 * @since 1.20.0
 */
public class VersionUtilTest {

	@Test
	public void testCompare() {
		Assert.assertTrue(VersionUtil.compare("1.10", "1.9") > 0);
		Assert.assertTrue(VersionUtil.compare("1.9", "1.10") < 0);
		Assert.assertEquals(0, VersionUtil.compare("1.0", "1.0.0"));
	}

	@Test
	public void testPredicates() {
		Assert.assertTrue(VersionUtil.isGreaterThan("1.2", "1.1"));
		Assert.assertFalse(VersionUtil.isGreaterThan("1.1", "1.1"));
		Assert.assertTrue(VersionUtil.isGreaterThanOrEqual("1.1", "1.1"));
		Assert.assertFalse(VersionUtil.isGreaterThanOrEqual("1.1", "1.2"));
		Assert.assertTrue(VersionUtil.isLessThan("1.1", "1.2"));
		Assert.assertFalse(VersionUtil.isLessThan("1.2", "1.2"));
		Assert.assertTrue(VersionUtil.isLessThanOrEqual("1.2", "1.2"));
		Assert.assertFalse(VersionUtil.isLessThanOrEqual("1.2", "1.1"));
	}

	@Test
	public void testIsEqual() {
		Assert.assertTrue(VersionUtil.isEqual("1.0", "1.0.0"));
		Assert.assertTrue(VersionUtil.isEqual("1.0.0+build1", "1.0.0+build2"));
		Assert.assertFalse(VersionUtil.isEqual("1.0.0-alpha", "1.0.0"));
		Assert.assertTrue(VersionUtil.isEqual(null, null));
		Assert.assertTrue(VersionUtil.VERSION_COMPARATOR.compare("1.0.0-alpha", "1.0.0") < 0);
	}
}
