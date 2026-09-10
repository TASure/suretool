package com.sure.tool.cron;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

/**
 * Cron 表达式（6 段 Quartz 风格：秒 分 时 日 月 周），支持通配符、范围、列表、步进，
 * 提供时刻匹配与下次执行时间计算，参考 Hutool 的 {@code CronPattern} 设计。
 * <p>
 * 段语法：
 * <ul>
 *   <li>{@code *} 任意值；{@code ?} 日/周段的不指定标记</li>
 *   <li>{@code a} 指定值；{@code a-b} 范围；{@code a,b,c} 列表</li>
 *   <li>{@code *&sol;n} 从最小值开始步进；{@code a-b/n} 范围内步进</li>
 * </ul>
 * 日与周同时指定时按 Quartz 语义取「或」关系。
 *
 * @author suretool
 * @since 0.1.0
 */
public class CronPattern {

	private static final int MAX_SEARCH_STEPS = 100_000;

	private final String expression;
	private final CronField second;
	private final CronField minute;
	private final CronField hour;
	private final CronField dayOfMonth;
	private final CronField month;
	private final CronField dayOfWeek;

	/**
	 * 解析 Cron 表达式。
	 *
	 * @param expression 6 段表达式
	 */
	public CronPattern(String expression) {
		if (expression == null || expression.trim().isEmpty()) {
			throw new IllegalArgumentException("Cron 表达式不能为空");
		}
		this.expression = expression.trim();
		String[] fields = this.expression.split("\\s+");
		if (fields.length != 6) {
			throw new IllegalArgumentException("Cron 表达式必须为 6 段（秒 分 时 日 月 周）: " + this.expression);
		}
		second = new CronField(fields[0], 0, 59, false);
		minute = new CronField(fields[1], 0, 59, false);
		hour = new CronField(fields[2], 0, 23, false);
		dayOfMonth = new CronField(fields[3], 1, 31, true);
		month = new CronField(fields[4], 1, 12, false);
		dayOfWeek = new CronField(fields[5], 1, 7, true);
	}

	/**
	 * 解析 Cron 表达式。
	 *
	 * @param expression 6 段表达式
	 * @return 表达式对象
	 */
	public static CronPattern of(String expression) {
		return new CronPattern(expression);
	}

	/**
	 * 原始表达式。
	 *
	 * @return 表达式
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * 时刻是否匹配。
	 *
	 * @param date 时刻
	 * @return 是否匹配
	 */
	public boolean match(Date date) {
		if (date == null) {
			return false;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return match(cal);
	}

	/**
	 * 计算指定时刻之后的下一次匹配时间（不含指定时刻本身）。
	 *
	 * @param date 起始时刻
	 * @return 下一次匹配时间；表达式无可行时间（如 2 月 30 日）时返回 {@code null}
	 */
	public Date getNextTimeAfter(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.SECOND, 1);
		cal.set(Calendar.MILLISECOND, 0);

		for (int step = 0; step < MAX_SEARCH_STEPS; step++) {
			// 月
			if (!month.match(cal.get(Calendar.MONTH) + 1)) {
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				setToTimeStart(cal);
				continue;
			}
			// 日（含日/周或关系）
			if (!isDayMatched(cal)) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
				setToTimeStart(cal);
				continue;
			}
			// 时
			if (!hour.match(cal.get(Calendar.HOUR_OF_DAY))) {
				Integer next = hour.nextMatchAfter(cal.get(Calendar.HOUR_OF_DAY));
				if (next == null) {
					cal.add(Calendar.DAY_OF_MONTH, 1);
					setToTimeStart(cal);
				} else {
					cal.set(Calendar.HOUR_OF_DAY, next);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
				}
				continue;
			}
			// 分
			if (!minute.match(cal.get(Calendar.MINUTE))) {
				Integer next = minute.nextMatchAfter(cal.get(Calendar.MINUTE));
				if (next == null) {
					cal.add(Calendar.HOUR_OF_DAY, 1);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
				} else {
					cal.set(Calendar.MINUTE, next);
					cal.set(Calendar.SECOND, 0);
				}
				continue;
			}
			// 秒
			if (!second.match(cal.get(Calendar.SECOND))) {
				Integer next = second.nextMatchAfter(cal.get(Calendar.SECOND));
				if (next == null) {
					cal.add(Calendar.MINUTE, 1);
					cal.set(Calendar.SECOND, 0);
				} else {
					cal.set(Calendar.SECOND, next);
				}
				continue;
			}
			return cal.getTime();
		}
		return null;
	}

	/**
	 * 校验表达式是否合法。
	 *
	 * @param expression 表达式
	 * @return 是否合法
	 */
	public static boolean isValid(String expression) {
		try {
			new CronPattern(expression);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private boolean match(Calendar cal) {
		if (!month.match(cal.get(Calendar.MONTH) + 1)) {
			return false;
		}
		if (!isDayMatched(cal)) {
			return false;
		}
		return hour.match(cal.get(Calendar.HOUR_OF_DAY))
				&& minute.match(cal.get(Calendar.MINUTE))
				&& second.match(cal.get(Calendar.SECOND));
	}

	/**
	 * 日匹配（日/周段或关系）。
	 */
	private boolean isDayMatched(Calendar cal) {
		boolean dayMatches = dayOfMonth.match(cal.get(Calendar.DAY_OF_MONTH));
		boolean weekMatches = dayOfWeek.match(cal.get(Calendar.DAY_OF_WEEK));
		if (dayOfMonth.isAny() && dayOfWeek.isAny()) {
			return true;
		}
		if (dayOfWeek.isAny()) {
			return dayMatches;
		}
		if (dayOfMonth.isAny()) {
			return weekMatches;
		}
		return dayMatches || weekMatches;
	}

	private static void setToTimeStart(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	@Override
	public String toString() {
		return "CronPattern{" + expression + '}';
	}

	/**
	 * 单个字段：展开为匹配表与有序值集合。
	 */
	private static class CronField {

		private final int min;
		private final boolean[] table;
		private final TreeSet<Integer> values = new TreeSet<>();
		private final boolean any;

		CronField(String segment, int min, int max, boolean allowQuestion) {
			this.min = min;
			if (segment == null || segment.isEmpty()) {
				throw new IllegalArgumentException("字段不能为空");
			}
			// 周段支持英文缩写（SUN-SAT）
			if (max == 7) {
				segment = segment.replaceAll("(?i)\\bSUN\\b", "1")
						.replaceAll("(?i)\\bMON\\b", "2")
						.replaceAll("(?i)\\bTUE\\b", "3")
						.replaceAll("(?i)\\bWED\\b", "4")
						.replaceAll("(?i)\\bTHU\\b", "5")
						.replaceAll("(?i)\\bFRI\\b", "6")
						.replaceAll("(?i)\\bSAT\\b", "7");
			}
			if ("?".equals(segment)) {
				if (!allowQuestion) {
					throw new IllegalArgumentException("? 仅允许用于日/周字段");
				}
				table = null;
				any = true;
				return;
			}
			if ("*".equals(segment)) {
				table = null;
				any = true;
				for (int v = min; v <= max; v++) {
					values.add(v);
				}
				return;
			}
			table = new boolean[max - min + 1];
			any = false;
			for (String part : segment.split(",")) {
				int slash = part.indexOf('/');
				if (slash >= 0) {
					int step = parseStep(part.substring(slash + 1));
					int[] range = parseRange(part.substring(0, slash), min, max);
					for (int v = range[0]; v <= range[1]; v += step) {
						add(v);
					}
				} else {
					int[] range = parseRange(part, min, max);
					for (int v = range[0]; v <= range[1]; v++) {
						add(v);
					}
				}
			}
			if (values.isEmpty()) {
				throw new IllegalArgumentException("字段无有效值: " + segment);
			}
		}

		private void add(int value) {
			table[value - min] = true;
			values.add(value);
		}

		/**
		 * 值是否匹配。
		 */
		boolean match(int value) {
			if (any) {
				return true;
			}
			int index = value - min;
			return index >= 0 && index < table.length && table[index];
		}

		/**
		 * 下一个匹配值（不含当前值），无则返回 {@code null}。
		 */
		Integer nextMatchAfter(int value) {
			return values.higher(value);
		}

		/**
		 * 是否任意值（* 或 ?）。
		 */
		boolean isAny() {
			return any;
		}

		private int parseStep(String step) {
			try {
				int value = Integer.parseInt(step.trim());
				if (value <= 0) {
					throw new IllegalArgumentException("步进必须为正整数: " + step);
				}
				return value;
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("非法步进值: " + step, e);
			}
		}

		private int[] parseRange(String range, int min, int max) {
			if ("*".equals(range)) {
				return new int[]{min, max};
			}
			int dash = range.indexOf('-');
			try {
				if (dash < 0) {
					int value = Integer.parseInt(range.trim());
					checkRange(value, min, max, range);
					return new int[]{value, value};
				}
				int from = Integer.parseInt(range.substring(0, dash).trim());
				int to = Integer.parseInt(range.substring(dash + 1).trim());
				checkRange(from, min, max, range);
				checkRange(to, min, max, range);
				if (from > to) {
					throw new IllegalArgumentException("范围起始大于结束: " + range);
				}
				return new int[]{from, to};
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("非法数值: " + range, e);
			}
		}

		private void checkRange(int value, int min, int max, String source) {
			if (value < min || value > max) {
				throw new IllegalArgumentException("数值 " + value + " 超出范围 [" + min + "," + max + "]: " + source);
			}
		}
	}
}
