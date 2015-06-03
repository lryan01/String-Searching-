import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HybridTST<E> implements TrieInterface<E> {

	private Node<E>[] trie;
	private static int R = 256;
	private int size;
	private int depth;
	private int maxHeight;
	private double total;

	private static class Node<E> {
		private char c;
		private Node<E> left, mid, right;
		private E value;
	}
	//creates a comparator to sort a list of Strings by length
	Comparator<String> lengthSort = new Comparator<String>(){

		@Override
		public int compare(String arg0, String arg1) {
			if(arg0.length() > arg1.length()){
				return 1;
			}
			else if(arg0.length() < arg1.length()){
				return -1;
			}
			return 0;
		}
	};
		

	public HybridTST() {
		trie = new Node[R];
		size = 0;
		maxHeight = 0;
		total = 0;
	}

	/**
	 * Returns the value associated with the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the value associated with the given key if the key is in the
	 *         symbol table and <tt>null</tt> if the key is not in the symbol
	 *         table
	 * @throws InvalidKeyException
	 *             if <tt>key</tt> is <tt>null</tt> or if the key uses an
	 *             invalid character set
	 */
	public E get(String key) throws InvalidKeyException {
		if ((key == null) || (notASCII(key))) {
			throw new InvalidKeyException();
		}
		Node<E> x = get(trie[arrayIndex(key)], key, 0);
		if (x == null)
			return null;
		return x.value;
	}

	private Node<E> get(Node<E> x, String key, int d) {
		if (x == null)
			return null;
		char c = key.charAt(d);
		if (c < x.c)
			return get(x.left, key, d);
		else if (c > x.c)
			return get(x.right, key, d);
		else if (d < key.length() - 1)
			return get(x.mid, key, d + 1);
		else
			return x;
	}

	/**
	 * Does this symbol table contain the given key?
	 * 
	 * @param key
	 *            the key
	 * @return <tt>true</tt> if this symbol table contains <tt>key</tt> and
	 *         <tt>false</tt> otherwise
	 * @throws InvalidKeyException
	 *             if <tt>key</tt> is <tt>null</tt> or if the key uses an
	 *             invalid character set
	 */
	public boolean contains(String key) throws InvalidKeyException {
		if ((key == null) || (notASCII(key))) {
			throw new InvalidKeyException();
		}
		return get(key) != null;
	}

	/**
	 * Inserts the key-value pair into the symbol table, overwriting the old
	 * value with the new value if the key is already in the symbol table. If
	 * the value is <tt>null</tt>, this effectively deletes the key from the
	 * symbol table.
	 * 
	 * @param key
	 *            the key
	 * @param val
	 *            the value
	 * @throws InvalidKeyException
	 *             if <tt>key</tt> is <tt>null</tt> or if the key uses an
	 *             invalid character set
	 */
	public void put(String key, E val) throws InvalidKeyException {
		if ((key == null) || (notASCII(key))) {
			throw new InvalidKeyException();
		}

		depth = 1;
		trie[arrayIndex(key)] = put(trie[arrayIndex(key)], key, val, 0);

	}

	private Node<E> put(Node<E> x, String key, E val, int d) {

		char c = key.charAt(d);
		if (x == null) {
			x = new Node<E>();
			x.c = c;
		}

		if (c < x.c) {
			depth++;
			x.left = put(x.left, key, val, d);
		} else if (c > x.c) {
			depth++;
			x.right = put(x.right, key, val, d);
		} else if (d < key.length() - 1) {
			depth++;
			x.mid = put(x.mid, key, val, d + 1);
		} else {
			x.value = val;
			if (depth >= maxHeight) {
				maxHeight = depth;
			}
			if (!contains(key)) {
				size++;
				total += (depth - 1);
			}

		}
		return x;
	}

	/**
	 * Returns the number of key-value pairs in this symbol table.
	 * 
	 * @return the number of key-value pairs in this symbol table
	 */
	public int size() {
		return size;
	}

	/**
	 * Is this symbol table empty?
	 * 
	 * @return <tt>true</tt> if this symbol table is empty and <tt>false</tt>
	 *         otherwise
	 */
	public boolean isEmpty() {
		return (size() == 0);
	}

	/**
	 * Returns all keys in the symbol table as an <tt>Iterable</tt>. To iterate
	 * over all of the keys in the symbol table named <tt>st</tt>, use the
	 * foreach notation: <tt>for (Key key : st.keys())</tt>.
	 * 
	 * @return all keys in the symbol table as an <tt>Iterable</tt>
	 */
	public Iterable<String> keys() {
		ArrayList<String> keyList = new ArrayList<String>();
		for (int i = 0; i < R; i++) {
			collect(trie[i], new StringBuilder(), keyList);
		}

		return keyList;
	}

	private void collect(Node<E> x, StringBuilder prefix, ArrayList<String> queue) {
		if (x == null)
			return;
		collect(x.left, prefix, queue);
		if (x.value != null)
			queue.add(prefix.toString() + x.c);
		collect(x.mid, prefix.append(x.c), queue);
		prefix.deleteCharAt(prefix.length() - 1);
		collect(x.right, prefix, queue);
	}

	/**
	 * Returns all of the keys in the set that start with <tt>prefix</tt>.
	 * 
	 * @param prefix
	 *            the prefix
	 * @return all of the keys in the set that start with <tt>prefix</tt>, as an
	 *         iterable
	 * @throws NullPointerException
	 *             if <tt>prefix</tt> is <tt>null</tt>
	 * @throws InvalidKeyException
	 *             if the prefix uses an invalid character set
	 */
	public Iterable<String> keysWithPrefix(String prefix) {
		if (notASCII(prefix)) {
			throw new InvalidKeyException();
		}
		ArrayList<String> queue = new ArrayList<String>();
		Node<E> x = get(trie[arrayIndex(prefix)], prefix, 0);
		if (x == null)
			return queue;
		if (x.value != null)
			queue.add(prefix);
		collect(x.mid, new StringBuilder(prefix), queue);
		Collections.sort(queue);
		Collections.sort(queue, lengthSort);
		return queue;

	}

	/**
	 * Returns all of the keys in the symbol table that match <tt>pattern</tt>,
	 * where . symbol is treated as a wildcard character.
	 * 
	 * @param pattern
	 *            the pattern
	 * @return all of the keys in the symbol table that match <tt>pattern</tt>,
	 *         as an iterable, where . is treated as a wildcard character.
	 * @throws NullPointerException
	 *             if <tt>pattern</tt> is <tt>null</tt>
	 * @throws InvalidKeyException
	 *             if the pattern uses an invalid character set
	 */
	public Iterable<String> keysThatMatch(String pattern) {
		if (notASCII(pattern)) {
			throw new InvalidKeyException();
		}
		ArrayList<String> queue = new ArrayList<String>();
		if (pattern.charAt(0) == '.') {
			for (int i = 0; i < R; i++) {
				collect(trie[i], new StringBuilder(), 0, pattern, queue);
			}
		} else {
			collect(trie[arrayIndex(pattern)], new StringBuilder(), 0, pattern,
					queue);
		}
		Collections.sort(queue);
		Collections.sort(queue, lengthSort);
		return queue;

	}

	private void collect(Node<E> x, StringBuilder prefix, int i,
			String pattern, ArrayList<String> queue) {
		if (x == null)
			return;
		char c = pattern.charAt(i);
		if (c == '.' || c < x.c)
			collect(x.left, prefix, i, pattern, queue);
		if (c == '.' || c == x.c) {
			if (i == pattern.length() - 1 && x.value != null)
				queue.add(prefix.toString() + x.c);
			if (i < pattern.length() - 1) {
				collect(x.mid, prefix.append(x.c), i + 1, pattern, queue);
				prefix.deleteCharAt(prefix.length() - 1);
			}
		}
		if (c == '.' || c > x.c)
			collect(x.right, prefix, i, pattern, queue);
	}

	/**
	 * Returns the string in the symbol table that is the longest prefix of
	 * <tt>query</tt>, or <tt>null</tt>, if no such string.
	 * 
	 * @param query
	 *            the query string
	 * @return the string in the symbol table that is the longest prefix of
	 *         <tt>query</tt>, or <tt>null</tt> if no such string
	 * @throws NullPointerException
	 *             if <tt>query</tt> is <tt>null</tt>
	 * @throws InvalidKeyException
	 *             if the query uses an invalid character set
	 */
	public String longestPrefixOf(String query) {
		if (notASCII(query)) {
			throw new InvalidKeyException();
		}
		if (query.length() == 0)
			return null;
		int length = 0;
		Node<E> x = trie[arrayIndex(query)];
		int i = 0;
		while (x != null && i < query.length()) {
			char c = query.charAt(i);
			if (c < x.c)
				x = x.left;
			else if (c > x.c)
				x = x.right;
			else {
				i++;
				if (x.value != null)
					length = i;
				x = x.mid;
			}
		}
		if(query.substring(0, length).length() == 0){
			return null;
		}
		return query.substring(0, length);
	}

	/**
	 * Get the current height of the tree used to represent this Trie. This is a
	 * diagnostic method used for this lab so you can see how tall your tree is
	 * and therefore what the maximum number of steps are that must be followed
	 * in a search.
	 * 
	 * @return the current height of the tree
	 */
	public int getTreeHeight() {
		return maxHeight;
	}

	/**
	 * Compute the average depth of all nodes in the Trie. The depth for a
	 * particular node is the number of edges that are traversed when starting
	 * from any index of the multi-way root node. Each root node index is at
	 * depth 0.
	 * 
	 * This value, together with the overall height of the tree will help to
	 * quantify real world "average" efficiencies for accessing items in this
	 * trie.
	 * 
	 * @return the average depth for all nodes in the tree
	 */
	public double getAverageNodeDepth() {
		return total / size;
	}

/**
 * Checks to see if a String is within Extended ASCII
 * @param key
 * @return
 */
	private boolean notASCII(String key) {
		for (int i = 0; i < key.length(); i++) {
			int val = key.charAt(i);
			if (val >= R) {
				return true;
			}
		}
		return false;
	}
	
/**
 * Finds the proper array index for a String to be entered into the trie
 * @param word
 * @return
 */
	private int arrayIndex(String word) {
		return word.charAt(0);
	}

}
