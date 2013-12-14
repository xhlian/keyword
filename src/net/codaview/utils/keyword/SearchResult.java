package net.codaview.utils.keyword;

class SearchResult {

	private final int lastIndex;

	private final TrieNode lastMatchedNode;

	public SearchResult(int lastIndex, TrieNode lastMatchedNode) {
		this.lastIndex = lastIndex;
		this.lastMatchedNode = lastMatchedNode;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public TrieNode getLastMatchedNode() {
		return lastMatchedNode;
	}

}
