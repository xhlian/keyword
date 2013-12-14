package net.codaview.utils.keyword;

import java.util.ArrayList;
import java.util.LinkedList;

public interface CharNodeMap {

	TrieNode get(char ch);

	void put(char ch, TrieNode node);

	char[] keys();

}

class ArrayCharMap implements CharNodeMap {

	private TrieNode[] nodes;

	public ArrayCharMap() {
		this.nodes = new TrieNode[256];
	}

	@Override
	public TrieNode get(char ch) {
		return this.nodes[(int) ch & 0xFF];
	}

	@Override
	public void put(char ch, TrieNode node) {
		this.nodes[(int) ch & 0xFF] = node;
	}

	@Override
	public char[] keys() {
		int resultLength = 0;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				resultLength++;
			}
		}

		char[] result = new char[resultLength];
		int nodeIndex = 0, resultIndex = 0;
		for (TrieNode node : nodes) {
			if (null != node) {
				result[resultIndex] = (char) nodeIndex;
				resultIndex++;
			}
			nodeIndex++;
		}
		return result;
	}

	@Override
	public String toString() {
		if (nodes == null)
			return "ArrayCharMap [nodes=null]";

		int iMax = nodes.length - 1;
		if (iMax == -1)
			return "ArrayCharMap[nodes=null[]]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(String.valueOf(nodes[i]));
			if (i == iMax) {
				b.append(']').toString();
				break;
			}
			b.append(", ");
		}
		return "ArrayCharMap [nodes=" + b + "]";
	}

}

class LinkedCharMap implements CharNodeMap {

	private LinkedList<Entry> nodes;

	public LinkedCharMap() {
		this.nodes = new LinkedList<LinkedCharMap.Entry>();
	}

	@Override
	public TrieNode get(char key) {
		for (Entry e : nodes) {
			if (key == e.key) {
				return e.value;
			}
		}
		return null;
	}

	@Override
	public void put(char key, TrieNode value) {
		this.nodes.add(new Entry(key, value));
	}

	@Override
	public char[] keys() {
		char[] result = new char[nodes.size()];
		int i = 0;
		for (Entry e : nodes) {
			result[i] = e.key;
			i++;
		}
		return result;
	}

	private static class Entry {
		char key;
		TrieNode value;

		public Entry(char ch, TrieNode node) {
			this.key = ch;
			this.value = node;
		}

	}
}

class Queue<T> {
	ArrayList<T> l1;
	ArrayList<T> l2;

	public Queue() {
		l1 = new ArrayList<T>();
		l2 = new ArrayList<T>();
	}

	public void add(T s) {
		l2.add(s);
	}

	public boolean isEmpty() {
		return l1.isEmpty() && l2.isEmpty();
	}

	public T pop() {
		if (isEmpty())
			throw new IllegalStateException();
		if (l1.isEmpty()) {
			for (int i = l2.size() - 1; i >= 0; i--)
				l1.add(l2.remove(i));
			assert l2.isEmpty();
			assert !l1.isEmpty();
		}
		return l1.remove(l1.size() - 1);
	}
}