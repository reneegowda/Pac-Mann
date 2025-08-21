package cs2110;

import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * A dictionary mapping String keys to values of type `V`, implemented as a trie (prefix tree)
 * over the restricted alphabet A-Z and 0-9.  Implements StringDict<V>.
 */
public class TrieStringDict<V> implements StringDict<V> {

    private static final int ALPHABET_SIZE = 36; // 26 letters + 10 digits

    /**
     * Trie node structure: up to 36 children, a flag for end-of-key, and an associated value.
     */
    private static class TrieNode<V> {
        @SuppressWarnings("unchecked")
        TrieNode<V>[] children = new TrieNode[ALPHABET_SIZE];
        boolean isKey;
        V value;
    }

    private final TrieNode<V> root;
    private int size;

    /**
     * Construct an empty TrieStringDict.
     */
    public TrieStringDict() {
        root = new TrieNode<>();
        size = 0;
    }

    /**
     * Return the number of keys currently mapped in the trie.
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Return whether the trie contains the given key.
     */
    @Override
    public boolean containsKey(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        TrieNode<V> node = root;
        for (char c : key.toUpperCase().toCharArray()) {
            int idx = charToIndex(c);
            node = node.children[idx];
            if (node == null) return false;
        }
        return node.isKey;
    }

    /**
     * Return the value associated with key.  Throws if not present.
     */
    @Override
    public V get(String key) {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        TrieNode<V> node = root;
        for (char c : key.toUpperCase().toCharArray()) {
            int idx = charToIndex(c);
            node = node.children[idx];
            if (node == null) throw new NoSuchElementException("Key not found: " + key);
        }
        if (!node.isKey) throw new NoSuchElementException("Key not found: " + key);
        return node.value;
    }

    /**
     * Associate the given value with key, inserting into the trie if needed.
     */
    @Override
    public void put(String key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException("Key and value must not be null");
        TrieNode<V> node = root;
        for (char c : key.toUpperCase().toCharArray()) {
            int idx = charToIndex(c);
            if (node.children[idx] == null) {
                node.children[idx] = new TrieNode<>();
            }
            node = node.children[idx];
        }
        if (!node.isKey) {
            node.isKey = true;
            size++;
        }
        node.value = value;
    }

    /**
     * Convert a character into its child index (0-35).
     */
    private int charToIndex(char c) {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A';
        } else if (c >= '0' && c <= '9') {
            return 26 + (c - '0');
        }
        throw new IllegalArgumentException("Invalid character: " + c);
    }

    /**
     * Iterate over all values stored in the dictionary in unspecified order.
     */
    @Override
    public Iterator<V> iterator() {
        DynamicArrayIndexedSeq<V> allValues = new DynamicArrayIndexedSeq<>();
        collectValues(root, allValues);
        return allValues.iterator();
    }

    /**
     * Depth‐first traversal of the trie to collect stored values.
     */
    private void collectValues(TrieNode<V> node, DynamicArrayIndexedSeq<V> seq) {
        if (node.isKey) {
            seq.add(node.value);
        }
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            TrieNode<V> child = node.children[i];
            if (child != null) {
                collectValues(child, seq);
            }
        }
    }
}
