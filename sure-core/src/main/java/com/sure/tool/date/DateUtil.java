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
}
