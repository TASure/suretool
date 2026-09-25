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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

/**
 * ArrayUtil 生成式属性测试（jqwik）：数组工具核心不变量。
 */
public class ArrayUtilPropertyTest {

	@Property(tries = 300)
	boolean isEmptyMatchesLength(@ForAll @Size(max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		Object[] a = list.toArray();
		return ArrayUtil.isEmpty(a) == (a.length == 0);
	}

	@Property(tries = 300)
	boolean lengthMatchesArrayLength(@ForAll @Size(max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		return ArrayUtil.length(list.toArray()) == list.size();
	}

	@Property(tries = 300)
	boolean containsOwnElement(@ForAll @Size(min = 1, max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 49) int i) {
		Object[] a = list.toArray();
		if (i >= a.length) {
			return true;
		}
		return ArrayUtil.contains(a, a[i]);
	}

	@Property(tries = 300)
	boolean indexOfReturnsValidPosition(@ForAll @Size(min = 1, max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 49) int i) {
		Object[] a = list.toArray();
		if (i >= a.length) {
			return true;
		}
		int idx = ArrayUtil.indexOf(a, a[i]);
		return idx >= 0 && idx < a.length && a[idx].equals(a[i]);
	}

	@Property(tries = 300)
	boolean getReturnsElement(@ForAll @Size(min = 1, max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 49) int i) {
		Object[] a = list.toArray();
		if (i >= a.length) {
			return true;
		}
		return ArrayUtil.get(a, i).equals(a[i]);
	}

	@Property(tries = 300)
	boolean reverseTwiceRestores(@ForAll @Size(max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		Object[] a = list.toArray();
		Object[] copy = Arrays.copyOf(a, a.length);
		ArrayUtil.reverse(a);
		ArrayUtil.reverse(a);
		return Arrays.equals(a, copy);
	}

	@Property(tries = 300)
	boolean distinctRemovesDuplicates(@ForAll @Size(max = 50) List<@IntRange(min = -5, max = 5) Integer> list) {
		Object[] d = ArrayUtil.distinct(list.toArray());
		for (int i = 0; i < d.length; i++) {
			for (int j = i + 1; j < d.length; j++) {
				if (d[i].equals(d[j])) {
					return false;
				}
			}
		}
		return d.length <= list.size();
	}

	@Property(tries = 300)
	boolean joinSplitMatchesCount(@ForAll @Size(max = 30) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		Object[] a = list.toArray();
		if (a.length == 0) {
			return ArrayUtil.join(a, ",").isEmpty();
		}
		return ArrayUtil.join(a, ",").split(",", -1).length == a.length;
	}

	@Property(tries = 300)
	boolean toListPreservesElements(@ForAll @Size(max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		List<Object> l = ArrayUtil.toList(list.toArray());
		return l.equals(Arrays.asList(list.toArray()));
	}

	@Property(tries = 300)
	boolean swapExchangesElements(@ForAll @Size(min = 2, max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 49) int i, @ForAll @IntRange(min = 0, max = 49) int j) {
		Object[] a = list.toArray();
		if (i >= a.length || j >= a.length || i == j) {
			return true;
		}
		Object vi = a[i];
		Object vj = a[j];
		ArrayUtil.swap(a, i, j);
		return a[i].equals(vj) && a[j].equals(vi);
	}

	@Property(tries = 300)
	boolean subLengthMatches(@ForAll @Size(max = 50) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 50) int from, @ForAll @IntRange(min = 0, max = 50) int to) {
		Object[] a = list.toArray();
		if (from > a.length || to < from) {
			return true;
		}
		int t = Math.min(to, a.length);
		Object[] sub = ArrayUtil.sub(a, from, t);
		return sub.length == t - from;
	}

	@Property(tries = 200)
	boolean minMaxMatchManual(@ForAll @Size(min = 1, max = 30) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		Integer[] a = list.toArray(new Integer[0]);
		int mn = Integer.MAX_VALUE;
		int mx = Integer.MIN_VALUE;
		for (Integer v : a) {
			mn = Math.min(mn, v);
			mx = Math.max(mx, v);
		}
		return ArrayUtil.min(a) == mn && ArrayUtil.max(a) == mx;
	}
}
