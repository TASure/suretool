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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

/**
 * StrUtil 生成式属性测试（jqwik）：不依赖具体样例，用随机输入验证核心不变量。
 */
public class StrUtilPropertyTest {

	@Property(tries = 300)
	boolean trimIsIdempotent(@ForAll @StringLength(max = 40) String s) {
		return StrUtil.trim(StrUtil.trim(s)).equals(StrUtil.trim(s));
	}

	@Property(tries = 300)
	boolean trimRemovesLeadingAndTrailingWhitespace(@ForAll @StringLength(max = 40) String s) {
		String t = StrUtil.trim(s);
		if (t.isEmpty()) {
			return true;
		}
		// Java trim() 只移除 <= U+0020 的空白（全角空格 U+3000 不在其列），故用 > ' ' 判定
		return t.charAt(0) > ' ' && t.charAt(t.length() - 1) > ' ';
	}

	@Property(tries = 300)
	boolean reverseIsInvolution(@ForAll @StringLength(max = 40) String s) {
		return StrUtil.reverse(StrUtil.reverse(s)).equals(s);
	}

	@Property(tries = 300)
	boolean repeatLengthMatches(@ForAll @StringLength(max = 20) String s, @ForAll @IntRange(min = 0, max = 50) int n) {
		return StrUtil.repeat(s, n).length() == s.length() * n;
	}

	@Property(tries = 200)
	boolean repeatZeroIsEmpty(@ForAll String s) {
		return StrUtil.repeat(s, 0).equals("");
	}

	@Property(tries = 300)
	boolean padPreHonorsMinLength(@ForAll @StringLength(max = 30) String s, @ForAll @IntRange(min = 0, max = 40) int min, @ForAll char c) {
		String p = StrUtil.padPre(s, min, c);
		return p.length() >= min && p.length() >= s.length();
	}

	@Property(tries = 300)
	boolean padPreEndsWithOriginal(@ForAll @StringLength(max = 30) String s, @ForAll @IntRange(min = 0, max = 40) int min, @ForAll char c) {
		return StrUtil.padPre(s, min, c).endsWith(s);
	}

	@Property(tries = 300)
	boolean removePrefixBehavior(@ForAll @StringLength(max = 30) String s, @ForAll @StringLength(max = 30) String prefix) {
		String r = StrUtil.removePrefix(s, prefix);
		if (prefix.isEmpty() || !s.startsWith(prefix)) {
			return r.equals(s);
		}
		return !r.startsWith(prefix) && r.equals(s.substring(prefix.length()));
	}

	@Property(tries = 300)
	boolean containsEmptyStringAlwaysTrue(@ForAll String s) {
		return StrUtil.contains(s, "");
	}

	@Property(tries = 300)
	boolean equalsIsReflexive(@ForAll String s) {
		return StrUtil.equals(s, s);
	}

	@Property(tries = 300)
	boolean subFullRangeIsIdentity(@ForAll @StringLength(max = 30) String s) {
		return StrUtil.sub(s, 0, s.length()).equals(s);
	}

	@Property(tries = 300)
	boolean camelUnderlineRoundTrip(@ForAll @StringLength(min = 1, max = 20) String word) {
		if (!word.matches("[a-zA-Z][a-zA-Z0-9]*")) {
			return true;
		}
		return StrUtil.toCamelCase(StrUtil.toUnderlineCase(word)).equalsIgnoreCase(word);
	}
}
