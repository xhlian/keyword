package net.codaview.utils.keyword;

/**
 * 关键字的查找、替换、统计功能。
 * 
 * <pre>
 * 典型用法如下：
 * <code>
 *   KeywordFilterBuilder builder = new KeywordFilterBuilder();
 *   // 设置关键字
 *   builder.setKeywords(Arrays.asList("心情", "哈哈"));
 *   // 设置跳过字符
 *   builder.setSkipChars(Arrays.asList('*', ' '));
 *   final KeywordFilter filter = builder.build();
 *   
 *   // 统计关键字出现次数, 下例中返回2. 
 *   filter.count("老龙恼怒闹老农，老农恼怒闹老龙。", "老龙");
 *   filter.count("老*龙恼怒闹老农，老农恼怒闹老 龙。", "老龙");
 *   
 *   // 替换关键字, 下例中返回"买彩票中奖了，呵呵".
 *   filter.replace("买彩票中奖了，哈哈", new ReplaceStrategy() {
 *   	public String replaceWith(String keywords) {
 *  		return "呵呵";
 *  	}
 *   });
 *   
 *   // 判断是否包含关键字, 下例中返回true, 因为包含"心情".
 *   filter.hasKeywords("今天天气不错，心情也跟着好起来了")
 *   filter.hasKeywords("今天天气不错，心*情也跟着好起来了")
 *   
 *  </code>
 * </pre>
 * 
 * 更多例子参见{@link TestKeywordFilter}
 * 
 * @author lianxh
 * @since 2013/12/14
 */
public interface KeywordFilter {

	/**
	 * 是否包含指定关键字
	 * 
	 * @param text
	 *            待匹配文本
	 * @return 如果包含返回true，否则false
	 */
	public boolean hasKeywords(String text);

	/**
	 * 统计指定关键字出现次数.
	 * 
	 * @param text
	 *            待统计的文本
	 * @param keyword
	 *            关键字
	 * @return 关键字出现次数
	 */
	public int count(String text, String keyword);

	/**
	 * 根据指定策略替换关键字，使用不同的策略可实现高亮功能。
	 * 
	 * @param text
	 *            待匹配文本
	 * @param strategy
	 *            替换策略
	 * @return 替换后的结果字符串
	 */
	public String replace(String text, ReplaceStrategy strategy);

}
