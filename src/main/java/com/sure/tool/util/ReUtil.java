package com.sure.tool.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类，参考 Hutool 的 {@code ReUtil} 设计。
 *
 * @author suretool
 */
public class ReUtil {

	private ReUtil() {
	}

	/**
	 * 是否匹配正则（部分匹配）。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 是否匹配
	 */
	public static boolean isMatch(String regex, CharSequence content) {
		if (content == null) {
			return false;
		}
		return Pattern.compile(regex).matcher(content).find();
	}

	/**
	 * 提取第一个匹配的指定分组。
	 *
	 * @param regex      正则表达式
	 * @param content    内容
	 * @param groupIndex 分组索引，0 表示整个匹配
	 * @return 分组内容，未匹配返回 {@code null}
	 */
	public static String get(String regex, CharSequence content, int groupIndex) {
		if (content == null) {
			return null;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (matcher.find()) {
			return matcher.group(groupIndex);
		}
		return null;
	}

	/**
	 * 提取第一个匹配的指定命名分组。
	 *
	 * @param regex     正则表达式
	 * @param content   内容
	 * @param groupName 分组名
	 * @return 分组内容，未匹配返回 {@code null}
	 */
	public static String get(String regex, CharSequence content, String groupName) {
		if (content == null) {
			return null;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (matcher.find()) {
			return matcher.group(groupName);
		}
		return null;
	}

	/**
	 * 获取第一个匹配的所有分组。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 分组列表（索引 0 为整个匹配），未匹配返回空列表
	 */
	public static List<String> getAllGroups(String regex, CharSequence content) {
		List<String> result = new ArrayList<>();
		if (content == null) {
			return result;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (matcher.find()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				result.add(matcher.group(i));
			}
		}
		return result;
	}

	/**
	 * 获取第一个匹配的所有分组，返回带分组名的 Map（键：分组名或数字索引）。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 分组 Map，未匹配返回空 Map
	 */
	public static Map<String, String> getAllGroupMap(String regex, CharSequence content) {
		Map<String, String> result = new LinkedHashMap<>();
		if (content == null) {
			return result;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (matcher.find()) {
			for (int i = 0; i <= matcher.groupCount(); i++) {
				result.put(String.valueOf(i), matcher.group(i));
			}
			// 命名分组
			java.util.regex.Pattern p = matcher.pattern();
			java.util.Set<String> groupNames = getNamedGroups(regex);
			for (String name : groupNames) {
				try {
					result.put(name, matcher.group(name));
				} catch (IllegalArgumentException ignore) {
					// 忽略无法访问的命名分组
				}
			}
		}
		return result;
	}

	private static java.util.Set<String> getNamedGroups(String regex) {
		java.util.Set<String> names = new java.util.HashSet<>();
		Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
		while (m.find()) {
			names.add(m.group(1));
		}
		return names;
	}

	/**
	 * 查找所有匹配的字符串。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 所有匹配结果
	 */
	public static List<String> findAll(String regex, CharSequence content) {
		List<String> result = new ArrayList<>();
		if (content == null) {
			return result;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}

	/**
	 * 统计匹配次数。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 匹配次数
	 */
	public static int count(String regex, CharSequence content) {
		if (content == null) {
			return 0;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * 替换所有匹配，模板中可使用 {@code $1}、{@code $2} 引用分组。
	 *
	 * @param content             内容
	 * @param regex               正则表达式
	 * @param replacementTemplate 替换模板
	 * @return 替换后的字符串
	 */
	public static String replaceAll(CharSequence content, String regex, String replacementTemplate) {
		if (content == null) {
			return StrUtil.EMPTY;
		}
		return Pattern.compile(regex).matcher(content).replaceAll(replacementTemplate);
	}

	/**
	 * 删除第一个匹配。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 删除后的字符串
	 */
	public static String delFirst(String regex, CharSequence content) {
		if (content == null) {
			return StrUtil.EMPTY;
		}
		return Pattern.compile(regex).matcher(content).replaceFirst(StrUtil.EMPTY);
	}

	/**
	 * 删除所有匹配。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @return 删除后的字符串
	 */
	public static String delAll(String regex, CharSequence content) {
		if (content == null) {
			return StrUtil.EMPTY;
		}
		return content.toString().replaceAll(regex, StrUtil.EMPTY);
	}

	/**
	 * 提取多组内容并套入模板，模板中使用 {@code {1}}、{@code {2}} 引用分组。
	 *
	 * @param regex   正则表达式
	 * @param content 内容
	 * @param template 模板
	 * @return 套用后的字符串，未匹配返回 {@code null}
	 */
	public static String extractMulti(String regex, CharSequence content, String template) {
		if (content == null) {
			return null;
		}
		Matcher matcher = Pattern.compile(regex).matcher(content);
		if (!matcher.find()) {
			return null;
		}
		String result = template;
		for (int i = 1; i <= matcher.groupCount(); i++) {
			result = result.replace("{" + i + "}", matcher.group(i) == null ? StrUtil.EMPTY : matcher.group(i));
		}
		return result;
	}

	/**
	 * 将正则表达式中的特殊字符转义（返回 {@code \Q...\E} 形式）。
	 *
	 * @param regex 正则表达式
	 * @return 转义后的字符串
	 */
	public static String quote(String regex) {
		return Pattern.quote(regex);
	}
}
