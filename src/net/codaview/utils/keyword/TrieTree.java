package net.codaview.utils.keyword;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 * Trie树又称单词查找树，字典树，是实现AC自动机算法的关键数据结构。
 * AC自动机算法分为3步：
 * (1)构造一棵Trie树;
 * (2)构造失败指针;
 * (3)根据AC自动机，搜索待处理的文本。
 * 
 * Trie树有3个基本性质：
 * (1) 根节点不包含字符，除根节点外每一个节点都只包含一个字符； 
 * (2) 从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串； 
 * (3) 每个节点的所有子节点包含的字符都不相同。
 * 
 * 搜索的方法为:
 * (1) 从根节点开始一次搜索； 
 * (2) 取得要查找关键词的第一个字符，并根据该字符选择对应的子树并转到该子树继续进行检索； 
 * (3) 在相应的子树上，取得要查找关键词的第二个字符,并进一步选择对应的子树进行检索。 
 * (4) 迭代过程…… 
 * (5) 在某个节点处，关键词的所有字符已被取出，则读取附在该节点上的信息，即完成查找。
 * </pre>
 * 
 * @author lianxh
 * @since 2013/12/13
 */
public class TrieTree implements KeywordFilter {

	private TrieNode root;

	private boolean compiled = false;

	private Set<Character> skipChars = new HashSet<Character>();

	public TrieTree() {
		this.root = new TrieNode();
	}

	/**
	 * 构建Trie树，作为搜索的数据结构。
	 * 
	 * @param keyword
	 *            关键字字符串
	 */
	public void add(String keyword) {
		if (null == keyword || keyword.trim().isEmpty()) {
			throw new IllegalArgumentException("过滤关键词不能为空！");
		}
		if (compiled) {
			throw new IllegalStateException("TrieTree编译后不能再添加关键字");
		}
		TrieNode last = this.root.extend(keyword.toCharArray());
		last.addResult(keyword);
	}

	public void addSkipChar(char ch) {
		this.skipChars.add(ch);
	}

	public void addSkipChar(Collection<Character> chars) {
		if (null != chars) {
			this.skipChars.addAll(chars);
		}
	}

	/**
	 * 编译Trie树
	 */
	public void compile() {
		this.buildFailPath();
		this.compiled = true;
	}

	@Override
	public String replace(String text, ReplaceStrategy s) {
		checkNotNull(text, "Null value not allowed for parameter 'text'");
		checkNotNull(s, "Null value not allowed for parameter 'strategy'");

		StringBuilder ret = new StringBuilder();
		char[] chars = text.toCharArray();
		TrieNode last = this.root;
		int lastIndex = 0;
		/* 保留上一字符匹配到的字符串，比如心事、心事重、心事重重 */
		String preKeyword = null;
		/* 已匹配的关键词个数 */
		int cnt = 0;
		for (int i = lastIndex; i < chars.length; i++) {
			char ch = chars[i];
			if (skipChars.contains(ch)) {
				continue;
			}
			while (last.get(ch) == null) {
				if (last == this.root) {
					break;
				}
				last = last.getFail();
			}
			last = last.get(ch);
			if(null == last) {
				last = root;
			}
			if (!last.getResults().isEmpty()) {
				preKeyword = last.getResults().iterator().next();
				cnt++;
				// 已经匹配到主串的最后一个字符，直接替换
				if (i == chars.length - 1) {
					ret.setLength(ret.length() - (preKeyword.length() - cnt));
					ret.append(s.replaceWith(preKeyword));
				}
			} else if (preKeyword != null) {
				ret.setLength(ret.length() - (preKeyword.length() - cnt));
				ret.append(s.replaceWith(preKeyword));
				ret.append(ch);
				// 重置状态
				preKeyword = null;
				cnt = 0;
			} else {
				ret.append(ch);
			}
		}

		return ret.toString();
	}

	@Override
	public boolean hasKeywords(String text) {
		checkNotNull(text, "Null value not allowed for parameter 'text'.");

		SearchResult start = new SearchResult(0, this.root);
		return null != continueSearch(text.toCharArray(), start);
	}

	/**
	 * 匹配模式串中出现的单词。当我们的模式串在Trie上进行匹配时，如果与当前节点的关键字不能继续匹配的时候，
	 * 就应该去当前节点的失败指针所指向的节点继续进行匹配。
	 * 
	 * <pre>
	 * 匹配过程如下： 
	 * 从root节点开始，每次根据读入的字符沿着自动机向下移动。 当读入的字符，在分支中不存在时，递归走失败路径。如果走失败路径走到了root节点，
	 * 则跳过该字符，处理下一个字符。 因为AC自动机是沿着输入文本的最长后缀移动的，所以在读取完所有输入文本后，最后递归走失败路径，直到到达根节点，
	 * 这样可以检测出所有的模式。
	 * 
	 * 分两种情况：
	 * (1)当前字符匹配，表示从当前节点沿着树边有一条路径可以到达目标字符， 此时只需沿该路径走向下一个节点继续匹配即可 ，目标字符串指针移向下个字符继续匹配；
	 * (2)当前字符不匹配，则去当前节点失败指针所指向的字符继续匹配，匹配过程随着指针指向root结束。
	 * 重复这2个过程中的任意一个，直到模式串走到结尾为止。
	 * </pre>
	 * 
	 */
	private SearchResult continueSearch(char[] text, SearchResult lastResult) {
		TrieNode last = lastResult.getLastMatchedNode();
		for (int i = lastResult.getLastIndex(); i < text.length; i++) {
			char ch = text[i];
			// 包含忽略字符
			if (skipChars.contains(ch)) {
				continue;
			}

			// 如果当前匹配的字符在trie树中无子节点且不是根节点
			// 则要通过失败指针去找它的当前节点的子节点
			while (null != last && last.get(ch) == null) {
				if (last == this.root) {
					break;
				}
				last = last.getFail();
			}
			last = last.get(ch);
			if (null == last) {
				last = this.root;
			}
			if (!last.getResults().isEmpty()) {
				return new SearchResult(i + 1, last);
			}
		}
		return null;
	}

	@Override
	public int count(String text, String keyword) {
		checkNotNull(text, "Null value not allowed for parameter 'text'.");
		checkNotNull(keyword, "Null value not allowed for parameter 'keyword'.");

		final char[] source = text.toCharArray();
		final char[] target = keyword.toCharArray();
		int cnt = 0;
		for (int i = 0; i < source.length; i++) {
			int k = 0;
			while (i < source.length && k < target.length) {
				// 完全匹配，双方比对下一个字符
				if (source[i] == target[k]) {
					k++;
					i++;
				}
				// 不匹配, 但属于跳过字符, 跳过1个字符, 但关键字不跳过
				else if (skipChars.contains(source[i])) {
					i++;
				}
				// 不匹配又不是忽略字符
				else {
					break;
				}
			}

			if (k == target.length) {
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 * <pre>
	 * 构造失败指针的过程概括起来就一句话：设这个节点上的字母为x，沿着他父亲的失败指针走，直到走到一个节点，他的儿子中也有字母为x的节点。
	 * 然后把当前节点的失败指针指向那个字符也为x的儿子。如果一直走到了root都没找到，那就把失败指针指向root。
	 * 有两个规则：
	 * (1)root的子节点的失败指针都指向root。
	 * (2)节点(字符为x)的失败指针指向：从X节点的父节点的fail节点回溯直到找到某节点的子节点也是字符x，没有找到就指向root。
	 * </pre>
	 */
	private void buildFailPath() {
		Deque<TrieNode> nodes = new LinkedList<TrieNode>();
		// 第二层要特殊处理，将这层中的节点的失败路径直接指向父节点(也就是根节点)。
		for (char ch : this.root.keys()) {
			TrieNode child = this.root.get(ch);
			child.setFail(this.root);
			nodes.add(child);
		}

		while (!nodes.isEmpty()) {
			TrieNode node = nodes.pop();
			char[] keys = node.keys();
			for (int i = 0; i < keys.length; i++) {
				TrieNode r = node;

				char ch = keys[i];
				TrieNode child = r.get(ch);
				nodes.add(child);

				r = r.getFail();
				while (null != r && r.get(ch) == null) {
					r = r.getFail();
				}
				if(null == r) {
					child.setFail(this.root);
				} else {
					child.setFail(r.get(ch));
				}
				
			}
		}
	}

	private void checkNotNull(Object o, String msg) {
		if (o == null) {
			throw new NullPointerException(msg);
		}
	}

}

/**
 * Trie树中的节点
 */
class TrieNode {

	/**
	 * 子节点
	 */
	private Map<Character, TrieNode> children;

	/**
	 * 失败指针
	 */
	private TrieNode fail;

	/**
	 * 当匹配到些节点为止时，匹配到的关键字
	 */
	private Set<String> results = new HashSet<String>();

	public TrieNode() {
		this.children = new LinkedHashMap<Character, TrieNode>();
	}

	/**
	 * 扩展枝条
	 */
	public TrieNode extend(char[] chars) {
		TrieNode node = this;
		for (int i = 0; i < chars.length; i++) {
			node = node.touchChild(chars[i]);
		}
		return node;
	}

	/**
	 * 有则返回，没有则创建后再返回
	 */
	private TrieNode touchChild(char ch) {
		TrieNode child = this.children.get(ch);
		if (child != null) {
			return child;
		}

		TrieNode next = new TrieNode();
		this.children.put(ch, next);
		return next;
	}

	public TrieNode get(char key) {
		return this.children.get(key);
	}

	public void put(char key, TrieNode value) {
		this.children.put(key, value);
	}

	public char[] keys() {
		char[] result = new char[children.size()];
		int i = 0;
		for (Character c : children.keySet()) {
			result[i] = c;
			i++;
		}
		return result;
	}

	public TrieNode getFail() {
		return this.fail;
	}

	public void setFail(TrieNode f) {
		this.fail = f;
	}

	public void addResult(String result) {
		this.results.add(result);
	}

	public Collection<String> getResults() {
		return this.results;
	}

}
