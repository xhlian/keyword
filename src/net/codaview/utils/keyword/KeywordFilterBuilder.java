package net.codaview.utils.keyword;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class KeywordFilterBuilder {

	private Collection<Character> skipChars;

	private List<String> keywords = Collections.emptyList();

	public void setSkipChars(Collection<Character> skipChars) {
		this.skipChars = skipChars;
	}

	public void setKeywords(List<String> keywords) {
		if (null == keywords || keywords.isEmpty()) {
			throw new IllegalArgumentException("请指定非null的关键字集合.");
		}
		this.keywords = keywords;
	}

	public KeywordFilter build() {
		TrieTree tree = new TrieTree();
		for (String keyword : keywords) {
			tree.add(keyword);
		}
		tree.addSkipChar(skipChars);
		tree.compile();
		return tree;
	}
}
