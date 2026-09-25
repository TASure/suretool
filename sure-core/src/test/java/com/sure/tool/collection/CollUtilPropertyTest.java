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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

/**
 * CollUtil 生成式属性测试（jqwik）：集合工具核心不变量。
 */
public class CollUtilPropertyTest {

	@Property(tries = 300)
	boolean isEmptyMatchesSize(@ForAll @Size(max = 40) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		return CollUtil.isEmpty(list) == (list.isEmpty());
	}

	@Property(tries = 300)
	boolean containsOwnElement(@ForAll @Size(min = 1, max = 40) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 39) int i) {
		if (i >= list.size()) {
			return true;
		}
		return CollUtil.contains(list, list.get(i));
	}

	@Property(tries = 300)
	boolean getMatchesListGet(@ForAll @Size(min = 1, max = 40) List<@IntRange(min = -1000, max = 1000) Integer> list,
			@ForAll @IntRange(min = 0, max = 39) int i) {
		if (i >= list.size()) {
			return true;
		}
		return CollUtil.get(list, i).equals(list.get(i));
	}

	@Property(tries = 300)
	boolean firstAndLastMatch(@ForAll @Size(min = 1, max = 40) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		return CollUtil.getFirst(list).equals(list.get(0))
				&& CollUtil.getLast(list).equals(list.get(list.size() - 1));
	}

	@Property(tries = 300)
	boolean distinctKeepsFirstOccurrenceAndNoDup(@ForAll @Size(max = 40) List<@IntRange(min = -10, max = 10) Integer> list) {
		List<Integer> d = CollUtil.distinct(list);
		Set<Integer> seen = new HashSet<>();
		for (Integer v : d) {
			if (!seen.add(v)) {
				return false;
			}
		}
		return d.size() <= list.size();
	}

	@Property(tries = 200)
	boolean unionContainsAll(@ForAll @Size(max = 20) List<@IntRange(min = -100, max = 100) Integer> a,
			@ForAll @Size(max = 20) List<@IntRange(min = -100, max = 100) Integer> b) {
		List<Integer> u = CollUtil.union(a, b);
		Set<Integer> s = new HashSet<>(u);
		return s.containsAll(a) && s.containsAll(b);
	}

	@Property(tries = 200)
	boolean intersectionElementsInBoth(@ForAll @Size(max = 20) List<@IntRange(min = -100, max = 100) Integer> a,
			@ForAll @Size(max = 20) List<@IntRange(min = -100, max = 100) Integer> b) {
		List<Integer> in = CollUtil.intersection(a, b);
		return new HashSet<>(in).stream().allMatch(v -> a.contains(v) && b.contains(v));
	}

	@Property(tries = 300)
	boolean filterKeepsPredicateAndSubset(@ForAll @Size(max = 40) List<@IntRange(min = -100, max = 100) Integer> list) {
		Predicate<Integer> p = v -> v % 2 == 0;
		List<Integer> f = CollUtil.filter(list, p);
		return f.stream().allMatch(p) && new HashSet<>(list).containsAll(f);
	}

	@Property(tries = 300)
	boolean mapPreservesSizeAndMapping(@ForAll @Size(max = 40) List<@IntRange(min = -100, max = 100) Integer> list) {
		Function<Integer, String> f = v -> "v" + v;
		List<String> m = CollUtil.map(list, f);
		if (m.size() != list.size()) {
			return false;
		}
		for (int i = 0; i < list.size(); i++) {
			if (!m.get(i).equals(f.apply(list.get(i)))) {
				return false;
			}
		}
		return true;
	}

	@Property(tries = 300)
	boolean joinSplitMatchesSize(@ForAll @Size(max = 30) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		if (list.isEmpty()) {
			return CollUtil.join(list, ",").isEmpty();
		}
		return CollUtil.join(list, ",").split(",", -1).length == list.size();
	}

	@Property(tries = 300)
	boolean newArrayListPreserves(@ForAll @Size(max = 30) List<@IntRange(min = -1000, max = 1000) Integer> list) {
		return CollUtil.newArrayList(list.toArray(new Integer[0])).equals(list);
	}

	@Property(tries = 200)
	boolean groupByKeyGroupsCorrectly(@ForAll @Size(max = 40) List<@IntRange(min = 0, max = 5) Integer> list) {
		Map<Integer, List<Integer>> g = CollUtil.groupByKey(list, v -> v % 3);
		for (Map.Entry<Integer, List<Integer>> e : g.entrySet()) {
			for (Integer v : e.getValue()) {
				if (!Integer.valueOf(v % 3).equals(e.getKey())) {
					return false;
				}
			}
		}
		return true;
	}
}
