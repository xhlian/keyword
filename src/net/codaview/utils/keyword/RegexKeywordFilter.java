package net.codaview.utils.keyword;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the <tt>KeywordFilter</tt> interface, backed by regular
 * expression. Note : You'd better <b>not</b> use this implement when dealing
 * with large text.
 * 
 * @author lianxh
 * @since 2013/12/16
 */
public class RegexKeywordFilter implements KeywordFilter {

	private Pattern pattern;

	private Set<String> keywords = new HashSet<String>();

	private Set<Character> skipChars = new HashSet<Character>();

	private boolean compiled = false;

	public void compile() {
		String patternString;
		if (skipChars.isEmpty()) {
			patternString = join(keywords, "|");
		} else {
			final String skipRegex = toRegexSkipChar(skipChars);
			List<String> regexs = new ArrayList<String>(keywords.size());
			for (String keyword : keywords) {
				String regex = insertSkipChar(keyword, skipRegex);
				regexs.add(regex);
			}
			patternString = join(regexs, "|");
		}

		this.pattern = Pattern.compile(patternString);
		this.compiled = true;
	}

	public void add(Collection<String> keywords) {
		if (null == keywords || keywords.isEmpty()) {
			throw new IllegalArgumentException("过滤关键词不能为空！");
		}
		if (compiled) {
			throw new IllegalStateException("编译后不能再添加关键字");
		}
		this.keywords.addAll(keywords);
	}

	public void add(String keyword) {
		if (null == keyword || keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("过滤关键词不能为空！");
		}
		if (compiled) {
			throw new IllegalStateException("编译后不能再添加关键字");
		}
		this.keywords.add(keyword);
	}

	public void addSkipChar(char ch) {
		if (compiled) {
			throw new IllegalStateException("编译后不能再添加忽略字符");
		}
		this.skipChars.add(ch);
	}

	public void addSkipChar(Collection<Character> chars) {
		if (compiled) {
			throw new IllegalStateException("编译后不能再添加忽略字符");
		}
		if (null != chars) {
			this.skipChars.addAll(chars);
		}
	}

	@Override
	public boolean hasKeywords(String text) {
		checkNotNull(text, "请传入需要查询关键字的文本");

		Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	@Override
	public int count(String text, String keyword) {
		checkNotNull(text, "请传入需要查询关键字的文本");

		Pattern pattern;
		if (skipChars.isEmpty()) {
			pattern = Pattern.compile(keyword);
		} else {
			String skipRegex = toRegexSkipChar(skipChars);
			String regex = insertSkipChar(keyword, skipRegex);
			pattern = Pattern.compile(regex);
		}

		Matcher matcher = pattern.matcher(text);
		int count = 0;
		while (matcher.find()) {
			count++;
		}
		return count;
	}

	/**
	 * 根据指定策略替换关键字，可实现高亮、替换等
	 * 
	 * @param text
	 *            需要查询关键字的文本
	 * @param strategy
	 *            替换策略
	 * @see ReplaceStrategy
	 * @return 替换过的文本
	 */
	public String replace(String text, ReplaceStrategy strategy) {
		checkNotNull(text, "请传入需要查询关键字的文本");
		checkNotNull(strategy, "请传入替换策略ReplaceStrategy");

		Matcher matcher = pattern.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String replaceWith = strategy.replaceWith(matcher.group(0));
			matcher.appendReplacement(sb, replaceWith);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String join(Collection<String> c, String str) {
		if (null == c || c.isEmpty()) {
			return "";
		}
		
		List<String> strs = new ArrayList<String>(c);
		Collections.sort(strs, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.length() > o2.length() ? -1 :
					   o1.length() == o2.length() ? 0 : 1;
			}
		});
		
		StringBuilder sb = new StringBuilder();
		for (String s : strs) {
			sb.append(s).append(str);
		}
		sb.setLength(sb.length() - str.length());
		return sb.toString();
	}

	private void checkNotNull(Object o, String msg) {
		if (null == o) {
			throw new NullPointerException(msg);
		}
	}

	private String insertSkipChar(String keyword, String skipRegex) {
		StringBuilder s = new StringBuilder();
		for (char k : keyword.toCharArray()) {
			s.append(k).append(skipRegex);
		}
		return s.toString();
	}

	private String toRegexSkipChar(Collection<Character> skipChars) {
		StringBuilder s = new StringBuilder();
		s.append('[');
		for (Character c : skipChars) {
			s.append(c);
		}
		s.append("]*");
		return s.toString();
	}

}
