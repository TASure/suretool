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
package com.sure.tool.date;

import com.sure.tool.util.StrUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期时间工具类，参考 Hutool 的 {@code DateUtil} 设计。
 *
 * @author suretool
 * @since 0.1.0
 */
public class DateUtil {

	/** 标准日期格式：yyyy-MM-dd */
	public static final String NORM_DATE_PATTERN = "yyyy-MM-dd";
	/** 标准时间格式：HH:mm:ss */
	public static final String NORM_TIME_PATTERN = "HH:mm:ss";
	/** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
	public static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	/** ISO 日期时间格式：yyyy-MM-dd'T'HH:mm:ss */
	public static final String ISO_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * 格式解析器缓存。
	 *
	 * <p>{@link SimpleDateFormat} 非线程安全，按线程隔离缓存可避免频繁创建对象的开销
	 * （与 Hutool 的 ThreadLocal 方案一致），且不影响 API 与线程安全语义。</p>
	 */
	private static final ThreadLocal<Map<String, SimpleDateFormat>> FORMATTER_CACHE = new ThreadLocal<Map<String, SimpleDateFormat>>() {
		@Override
		protected Map<String, SimpleDateFormat> initialValue() {
			return new HashMap<String, SimpleDateFormat>(8);
		}
	};

	private static SimpleDateFormat getFormatter(String pattern) {
		Map<String, SimpleDateFormat> cache = FORMATTER_CACHE.get();
		SimpleDateFormat format = cache.get(pattern);
		if (format == null) {
			format = new SimpleDateFormat(pattern);
			cache.put(pattern, format);
		}
		return format;
	}

	private DateUtil() {
	}

	/**
	 * 当前时间。
	 *
	 * @return 当前时间
	 */
	public static Date now() {
		return new Date();
	}

	/**
	 * 时间戳转 Date。
	 *
	 * @param timeMillis 毫秒时间戳
	 * @return Date
	 */
	public static Date date(long timeMillis) {
		return new Date(timeMillis);
	}

	/**
	 * Calendar 转 Date。
	 *
	 * @param calendar Calendar
	 * @return Date，空值返回 {@code null}
	 */
	public static Date date(Calendar calendar) {
		return (calendar == null) ? null : calendar.getTime();
	}

	/**
	 * 格式化日期时间（yyyy-MM-dd HH:mm:ss）。
	 *
	 * @param date 日期
	 * @return 格式化字符串，空值返回 {@code null}
	 */
	public static String format(Date date) {
		return format(date, NORM_DATETIME_PATTERN);
	}

	/**
	 * 按格式格式化日期。
	 *
	 * @param date   日期
	 * @param format 格式
	 * @return 格式化字符串，空值返回 {@code null}
	 */
	public static String format(Date date, String format) {
		if (date == null) {
			return null;
		}
		return getFormatter(format).format(date);
	}

	/**
	 * 格式化日期（yyyy-MM-dd）。
	 *
	 * @param date 日期
	 * @return 格式化字符串
	 */
	public static String formatDate(Date date) {
		return format(date, NORM_DATE_PATTERN);
	}

	/**
	 * 格式化时间（HH:mm:ss）。
	 *
	 * @param date 日期
	 * @return 格式化字符串
	 */
	public static String formatTime(Date date) {
		return format(date, NORM_TIME_PATTERN);
	}

	/**
	 * 按格式解析日期。
	 *
	 * @param dateStr 日期字符串
	 * @param format  格式
	 * @return Date
	 */
	public static Date parse(String dateStr, String format) {
		try {
			return getFormatter(format).parse(dateStr);
		} catch (ParseException e) {
			throw new IllegalArgumentException("日期解析失败: " + dateStr + "，格式: " + format, e);
		}
	}

	/**
	 * 自动识别解析日期，支持：
	 * <ul>
	 *   <li>yyyy-MM-dd</li>
	 *   <li>HH:mm:ss</li>
	 *   <li>yyyy-MM-dd HH:mm:ss</li>
	 *   <li>yyyy-MM-dd'T'HH:mm:ss</li>
	 * </ul>
	 *
	 * @param dateStr 日期字符串
	 * @return Date，空白返回 {@code null}
	 */
	public static Date parse(String dateStr) {
		if (StrUtil.isBlank(dateStr)) {
			return null;
		}
		String s = dateStr.trim();
		if (s.length() == 10) {
			return parse(s, NORM_DATE_PATTERN);
		}
		if (s.length() == 8) {
			return parse(s, NORM_TIME_PATTERN);
		}
		if (s.length() == 19 && s.charAt(10) == ' ') {
			return parse(s, NORM_DATETIME_PATTERN);
		}
		if (s.indexOf('T') >= 0) {
			return parse(s, ISO_DATETIME_PATTERN);
		}
		throw new IllegalArgumentException("无法识别日期格式: " + dateStr);
	}

	/**
	 * 日期偏移。
	 *
	 * @param date     日期
	 * @param datePart 偏移字段（{@link Calendar} 常量，如 {@link Calendar#DAY_OF_MONTH}）
	 * @param offset   偏移量，负数向前
	 * @return 偏移后的日期
	 */
	public static Date offset(Date date, int datePart, int offset) {
		Calendar calendar = toCalendar(date);
		calendar.add(datePart, offset);
		return calendar.getTime();
	}

	/**
	 * 天偏移。
	 *
	 * @param date   日期
	 * @param offset 偏移天数，负数向前
	 * @return 偏移后的日期
	 */
	public static Date offsetDay(Date date, int offset) {
		return offset(date, Calendar.DAY_OF_MONTH, offset);
	}

	/**
	 * 小时偏移。
	 *
	 * @param date   日期
	 * @param offset 偏移小时数，负数向前
	 * @return 偏移后的日期
	 */
	public static Date offsetHour(Date date, int offset) {
		return offset(date, Calendar.HOUR_OF_DAY, offset);
	}

	/**
	 * 分钟偏移。
	 *
	 * @param date   日期
	 * @param offset 偏移分钟数，负数向前
	 * @return 偏移后的日期
	 */
	public static Date offsetMinute(Date date, int offset) {
		return offset(date, Calendar.MINUTE, offset);
	}

	/**
	 * 昨天。
	 *
	 * @return 昨天
	 */
	public static Date yesterday() {
		return offsetDay(now(), -1);
	}

	/**
	 * 明天。
	 *
	 * @return 明天
	 */
	public static Date tomorrow() {
		return offsetDay(now(), 1);
	}

	/**
	 * 当天开始时间（00:00:00.000）。
	 *
	 * @param date 日期
	 * @return 当天开始时间
	 */
	public static Date beginOfDay(Date date) {
		Calendar calendar = toCalendar(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 当天结束时间（23:59:59.999）。
	 *
	 * @param date 日期
	 * @return 当天结束时间
	 */
	public static Date endOfDay(Date date) {
		Calendar calendar = toCalendar(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * 当月开始时间（1 日 00:00:00.000）。
	 *
	 * @param date 日期
	 * @return 当月开始时间
	 */
	public static Date beginOfMonth(Date date) {
		Calendar calendar = toCalendar(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 当月结束时间（最后一天 23:59:59.999）。
	 *
	 * @param date 日期
	 * @return 当月结束时间
	 */
	public static Date endOfMonth(Date date) {
		Calendar calendar = toCalendar(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	/**
	 * 两个日期之间的时间差。
	 *
	 * @param start 开始日期
	 * @param end   结束日期
	 * @param unit  时间单位
	 * @return 时间差（按单位取整）
	 */
	public static long between(Date start, Date end, DateUnit unit) {
		long diff = end.getTime() - start.getTime();
		return diff / unit.getMillis();
	}

	/**
	 * 计算周岁年龄。
	 *
	 * @param birthDate 出生日期
	 * @return 年龄（周岁）
	 */
	public static int age(Date birthDate) {
		if (birthDate == null) {
			return 0;
		}
		Calendar birth = toCalendar(birthDate);
		Calendar now = Calendar.getInstance();
		int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
		if (now.get(Calendar.MONTH) < birth.get(Calendar.MONTH)
				|| (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
						&& now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
			age--;
		}
		return Math.max(age, 0);
	}

	/**
	 * 获取年份。
	 *
	 * @param date 日期
	 * @return 年份
	 */
	public static int year(Date date) {
		return toCalendar(date).get(Calendar.YEAR);
	}

	/**
	 * 获取月份（1-12）。
	 *
	 * @param date 日期
	 * @return 月份
	 */
	public static int month(Date date) {
		return toCalendar(date).get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取日（1-31）。
	 *
	 * @param date 日期
	 * @return 日
	 */
	public static int day(Date date) {
		return toCalendar(date).get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取星期几（1=周日，7=周六）。
	 *
	 * @param date 日期
	 * @return 星期几
	 */
	public static int dayOfWeek(Date date) {
		return toCalendar(date).get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 是否为同一天。
	 *
	 * @param date1 日期 1
	 * @param date2 日期 2
	 * @return 是否为同一天
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		Calendar c1 = toCalendar(date1);
		Calendar c2 = toCalendar(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
				&& c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 是否为闰年。
	 *
	 * @param year 年份
	 * @return 是否为闰年
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}

	/**
	 * Date 转 Calendar。
	 *
	 * @param date 日期
	 * @return Calendar
	 */
	public static Calendar toCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	/** 星座日期边界（每月起始日）。 */
	private static final int[][] ZODIAC_BOUNDS = {{1, 20}, {2, 19}, {3, 21}, {4, 20}, {5, 21}, {6, 22},
			{7, 23}, {8, 23}, {9, 23}, {10, 24}, {11, 23}, {12, 22}};
	private static final String[] ZODIACS = {"水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座",
			"狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
	private static final String[] CHINESE_ZODIACS = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};

	/**
	 * 获取星座。
	 *
	 * @param date 日期
	 * @return 星座名，如 白羊座；null 返回 null
	 */
	public static String getZodiac(Date date) {
		if (date == null) {
			return null;
		}
		Calendar c = toCalendar(date);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int idx = 0;
		for (int i = 0; i < 12; i++) {
			if (month > ZODIAC_BOUNDS[i][0] || (month == ZODIAC_BOUNDS[i][0] && day >= ZODIAC_BOUNDS[i][1])) {
				idx = i;
			}
		}
		// 1/20 前为摩羯座
		if (month == 1 && day < 20) {
			return "摩羯座";
		}
		return ZODIACS[idx];
	}

	/**
	 * 获取农历生肖（按公历年，1900 年为鼠年）。
	 *
	 * @param year 公历年份
	 * @return 生肖
	 */
	public static String getChineseZodiac(int year) {
		return CHINESE_ZODIACS[Math.floorMod(year - 1900, 12)];
	}



	/**
	 * 一天开始（周一为一周第一天，00:00:00.000）。
	 *
	 * @param date 日期
	 * @return 当周周一 00:00:00.000
	 */
	public static Date beginOfWeek(Date date) {
		Calendar c = toCalendar(date);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 一周结束（周日 23:59:59.999）。
	 *
	 * @param date 日期
	 * @return 当周周日 23:59:59.999
	 */
	public static Date endOfWeek(Date date) {
		Calendar c = toCalendar(date);
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTime();
	}

	/**
	 * 中文格式日期时间（2026年9月16日 15:30:45）。
	 *
	 * @param date 日期
	 * @return 中文日期时间
	 */
	public static String formatChineseDateTime(Date date) {
		return new SimpleDateFormat("yyyy年M月d日 HH:mm:ss").format(date);
	}

	/**
	 * 当月第几天（1~31）。
	 *
	 * @param date 日期
	 * @return 日
	 */
	public static int dayOfMonth(Date date) {
		return toCalendar(date).get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 小时（0~23）。
	 *
	 * @param date 日期
	 * @return 小时
	 */
	public static int hour(Date date) {
		return toCalendar(date).get(Calendar.HOUR_OF_DAY);
	}



	/**
	 * 季度（1~4）。
	 *
	 * @param date 日期
	 * @return 季度
	 */
	public static int season(Date date) {
		return toCalendar(date).get(Calendar.MONTH) / 3 + 1;
	}

	/**
	 * 中文格式日期（2026年9月16日）。
	 *
	 * @param date 日期
	 * @return 中文日期
	 */
	public static String formatChineseDate(Date date) {
		return new SimpleDateFormat("yyyy年M月d日").format(date);
	}

	/**
	 * 当年第几天（1~366）。
	 *
	 * @param date 日期
	 * @return 天序号
	 */
	public static int dayOfYear(Date date) {
		return toCalendar(date).get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 分钟（0~59）。
	 *
	 * @param date 日期
	 * @return 分钟
	 */
	public static int minute(Date date) {
		return toCalendar(date).get(Calendar.MINUTE);
	}

	/**
	 * 秒（0~59）。
	 *
	 * @param date 日期
	 * @return 秒
	 */
	public static int second(Date date) {
		return toCalendar(date).get(Calendar.SECOND);
	}



	/**
	 * 两时间差的可读文本（x天x小时x分x秒）。
	 *
	 * @param start 开始
	 * @param end   结束
	 * @return 可读时长
	 */
	public static String formatBetween(Date start, Date end) {
		long ms = Math.abs(end.getTime() - start.getTime());
		long days = ms / 86_400_000;
		long hours = ms % 86_400_000 / 3_600_000;
		long minutes = ms % 3_600_000 / 60_000;
		long seconds = ms % 60_000 / 1000;
		StringBuilder sb = new StringBuilder();
		if (days > 0) {
			sb.append(days).append("天");
		}
		if (hours > 0 || sb.length() > 0) {
			sb.append(hours).append("小时");
		}
		if (minutes > 0 || sb.length() > 0) {
			sb.append(minutes).append("分");
		}
		sb.append(seconds).append("秒");
		return sb.toString();
	}

	/**
	 * 是否为周末（周六/周日）。
	 *
	 * @param date 日期
	 * @return 是否周末
	 */
	public static boolean isWeekend(Date date) {
		int week = dayOfWeek(date);
		return week == Calendar.SATURDAY || week == Calendar.SUNDAY;
	}

	/**
	 * 按周偏移。
	 *
	 * @param date   日期
	 * @param offset 周偏移量（可负）
	 * @return 偏移后日期
	 */
	public static Date offsetWeek(Date date, int offset) {
		return offset(date, Calendar.WEEK_OF_YEAR, offset);
	}



	/**
	 * 年初（1 月 1 日 00:00:00.000）。
	 *
	 * @param date 日期
	 * @return 当年 1 月 1 日 00:00:00.000
	 */
	public static Date beginOfYear(Date date) {
		Calendar c = toCalendar(date);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 年末（12 月 31 日 23:59:59.999）。
	 *
	 * @param date 日期
	 * @return 当年 12 月 31 日 23:59:59.999
	 */
	public static Date endOfYear(Date date) {
		Calendar c = toCalendar(date);
		c.set(Calendar.MONTH, Calendar.DECEMBER);
		c.set(Calendar.DAY_OF_MONTH, 31);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTime();
	}

	/**
	 * 按月偏移。
	 *
	 * @param date   日期
	 * @param offset 月偏移量（可负）
	 * @return 偏移后日期
	 */
	public static Date offsetMonth(Date date, int offset) {
		return offset(date, Calendar.MONTH, offset);
	}



	/**
	 * 年份天干地支（如 2026 → 丙午）。
	 *
	 * @param date 日期
	 * @return 天干地支
	 */
	public static String getGanzhi(Date date) {
		int year = year(date);
		String[] gan = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
		String[] zhi = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
		return gan[(year - 4) % 10] + zhi[(year - 4) % 12];
	}

	/**
	 * 按秒偏移。
	 *
	 * @param date   日期
	 * @param offset 秒偏移量（可负）
	 * @return 偏移后日期
	 */
	public static Date offsetSecond(Date date, int offset) {
		return offset(date, Calendar.SECOND, offset);
	}

	/**
	 * 按年偏移。
	 *
	 * @param date   日期
	 * @param offset 年偏移量（可负）
	 * @return 偏移后日期
	 */
	public static Date offsetYear(Date date, int offset) {
		return offset(date, Calendar.YEAR, offset);
	}

	/**
	 * 是否在时间区间内（含边界）。
	 *
	 * @param date  待判断日期
	 * @param start 区间开始（可为 null 表示无下界）
	 * @param end   区间结束（可为 null 表示无上界）
	 * @return 是否在区间内
	 */
	public static boolean isIn(Date date, Date start, Date end) {
		if (date == null) {
			return false;
		}
		long t = date.getTime();
		return (start == null || t >= start.getTime()) && (end == null || t <= end.getTime());
	}



	/**
	 * 是否今天。
	 *
	 * @param date 日期
	 * @return 是否今天
	 */
	public static boolean isToday(Date date) {
		return isSameDay(date, now());
	}

	/**
	 * 是否昨天。
	 *
	 * @param date 日期
	 * @return 是否昨天
	 */
	public static boolean isYesterday(Date date) {
		return isSameDay(date, offsetDay(now(), -1));
	}



	/**
	 * 获取季度开始时间（当季第 1 天 00:00:00）。
	 *
	 * @param date 日期
	 * @return 季度开始
	 */
	public static java.util.Date beginOfQuarter(java.util.Date date) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(date);
		int quarterMonth = (cal.get(java.util.Calendar.MONTH) / 3) * 3;
		cal.set(java.util.Calendar.MONTH, quarterMonth);
		cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
		cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		cal.set(java.util.Calendar.MINUTE, 0);
		cal.set(java.util.Calendar.SECOND, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获取季度结束时间（当季最后 1 天 23:59:59）。
	 *
	 * @param date 日期
	 * @return 季度结束
	 */
	public static java.util.Date endOfQuarter(java.util.Date date) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.setTime(beginOfQuarter(date));
		cal.add(java.util.Calendar.MONTH, 3);
		cal.add(java.util.Calendar.MILLISECOND, -1);
		return cal.getTime();
	}



	/**
	 * 计算周岁年龄（指定参考日期）。
	 *
	 * @param birthDate 出生日期
	 * @param date      参考日期
	 * @return 周岁
	 */
	public static int age(java.util.Date birthDate, java.util.Date date) {
		if (birthDate == null || date == null) {
			return 0;
		}
		java.time.LocalDate birth = birthDate.toInstant()
				.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		java.time.LocalDate ref = date.toInstant()
				.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
		return (int) java.time.temporal.ChronoUnit.YEARS.between(birth, ref);
	}



	/**
	 * 判断两个日期是否同一个月。
	 *
	 * @param date1 日期 1
	 * @param date2 日期 2
	 * @return 是否同年同月；任一为 null 返回 false
	 */
	public static boolean isSameMonth(java.util.Date date1, java.util.Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		java.util.Calendar cal1 = java.util.Calendar.getInstance();
		cal1.setTime(date1);
		java.util.Calendar cal2 = java.util.Calendar.getInstance();
		cal2.setTime(date2);
		return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
				&& cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH);
	}


}