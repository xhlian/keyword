package net.codaview.utils.keyword;

/**
 * 关键字替换策略。
 */
public interface ReplaceStrategy {

	/**
	 * 将关键字替换为期望的结果字符串
	 * 
	 * @param keyword
	 *            keyword
	 * @return The resulting <tt>String</tt>
	 */
	String replaceWith(String keyword);

}
