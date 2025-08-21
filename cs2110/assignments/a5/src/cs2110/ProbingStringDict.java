package cs2110;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A dictionary mapping String keys to values of type `V`, implemented using a hash table with
 * linear probing. Employs a custom hash function for keys.
 */
public class ProbingStringDict<V> implements StringDict<V> {
    // Note: Many members of this class that would normally be `private` have instead been declared
    //  as `protected` to facilitate testing and to emphasize that they should not be changed.

    /**
     * Represents an association of a key `key` (of type `K`) with a value `value` (of type `V`).
     */
    protected record Entry<K, V>(K key, V value) {
        // A "record" class will automatically generate a constructor `Entry(key, value)` and
        //  accessors `key()` and `value()`, as well as overrides for `toString()`, `equals()`, and
        //  `hashCode()`.  It is otherwise just a nested, static (not inner) class.
    }

    /**
     * The hash table.  Elements without an entry are null.  For keys contained in this dictionary,
     * if the key's hash code maps to index `i`, then the (unique) entry containing that key will be
     * reachable via linear search starting at element `i` (wrapping around the array if necessary)
     * without encountering null.  Because this dictionary does not support removing keys, there is
     * no need to represent "tombstones".
     */
    protected Entry<String, V>[] entries;

    /**
     * The number of keys currently mapped to values in this dictionary.  Confined to
     * `[0..entries.length]`.
     */
    protected int size;

    /**
     * Maximum allowed value for the ratio of `size` to `entries.length`.  If the load factor were
     * to exceed this value after adding a new key, then the hash table must be enlarged so that the
     * load factor does not exceed this value.  Must be positive.
     */
    protected double maxLoadFactor;


    /**
     * The initial capacity of the hash table for new instances of `ProbingStringDict`.
     */
    protected static final int INITIAL_CAPACITY = 16;

    /**
     * The initial maximum load factor for new instances of `ProbingStringDict`.
     */
    private static final double DEFAULT_MAX_LOAD_FACTOR = 0.5;


    /**
     * Create a new empty `ProbingStringDict`.
     */
    public ProbingStringDict() {
        @SuppressWarnings("unchecked")
        Entry<String, V>[] entries = new Entry[INITIAL_CAPACITY];
        this.entries = entries;
        size = 0;
        maxLoadFactor = DEFAULT_MAX_LOAD_FACTOR;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Derive a hash code from the key `key`.  May return any `int` value, but the same key will
     * always produce the same hash code.  Should be resistant to collisions for closely-related
     * keys.
     */
    protected int hash(String key) {
        int result = 0;
        for (int i = 0; i < key.length(); i++) {
            result = result * 31 + key.charAt(i);
        }
        return result;

        // TODO 12: Implement this method as specified.  We expect the only methods you need will be
        //  `String.charAt()` and `String.length()`, though `Math` functions would be fine too.  Do
        //  not use `hashCode()` or `Objects.hash()` or `Arrays.hashCode()`.  Be creative!
    }


    /**
     * Return the current ratio of the number of keys in this dictionary to its backing hash table's
     * size.
     */
    protected double loadFactor() {
        return (double) size() / entries.length;
        // TODO 13: Implement this method as specified.
    }


    /**
     * If `key` is a key in this dictionary, return the index of the entry in `entries` for this
     * key. Otherwise, return the first index of a null entry in the table at or after the index
     * corresponding to the key's hash code (wrapping around).  Throws `NoSuchElementException` if
     * the key is not in this dictionary and the table is full.
     */
    protected int findEntry(String key){
        assert key != null;
        int keyIndex = Math.floorMod(hash(key), entries.length);
        int startingIndex = keyIndex;

        // Check the initial position before entering the loop
        while (entries[keyIndex] != null && !entries[keyIndex].key().equals(key)) {
            keyIndex = (keyIndex + 1) % entries.length;

            // If we wrap around and return to the starting index, the table is full and the key is not found
            if (keyIndex == startingIndex) {
                throw new NoSuchElementException();
            }
        }

        // If we find the key or find an empty spot, return the current index
        return keyIndex;
        // TODO 14: Implement this method as specified.
    }


    @Override
    public boolean containsKey(String key) {
        assert key != null;
        int index = 0;
        try{
            index = findEntry(key);
        } catch (NoSuchElementException e){
            return false;
        }
        return entries[index] != null && entries[index].key().equals(key);
        // TODO 15: Implement this method as specified.  `findEntry()` will probably be useful.
    }

    @Override
    public V get(String key) {
        assert key != null;
        V ans = null;
        try {
            int index = findEntry(key);
            if (entries[index] != null) {
                ans = entries[index].value();
            }
        } catch (NoSuchElementException ignored){}
        if(ans == null){
            throw new NoSuchElementException();
        }
        return ans;
        // TODO 16: Implement this method as specified.  `findEntry()` will probably be useful.
    }

    @Override
    public void put(String key, V value) {
        assert key != null;

        if (loadFactor() >= maxLoadFactor) {
            @SuppressWarnings("unchecked")
            Entry<String, V>[] newEntries = new Entry[entries.length * 2];
            Entry<String, V>[] oldEntries = entries;
            entries = newEntries;
            size = 0;

            for (Entry<String, V> entry : oldEntries) {
                if (entry != null) {
                    put(entry.key(), entry.value());  // cleaner and safer
                }
            }
        }
        int index = findEntry(key);
        if (entries[index] == null) {
            entries[index] = new Entry<>(key, value);
            size++;
        } else {
            entries[index] = new Entry<>(key, value);  // overwrite
        }
    // TODO 17: Implement this method as specified.  `findEntry()` will probably be useful.
        //  You may define additional (private) helper methods as well if you like.
    }


    @Override
    public Iterator<V> iterator() {
        return new Itr();
    }


    /**
     * An iterator over the values in this hash table.  This dictionary must not be structurally
     * modified while any such iterators are alive.
     */
    private class Itr implements Iterator<V> {

        /**
         * The index of the entry in `entries` containing the next value to yield, or
         * `entries.length` if all values have been yielded.
         */
        private int iNext;

        /**
         * Create a new iterator over this dictionary's values.
         */
        Itr() {
            iNext = 0;
            findNext();
        }

        /**
         * Set `iNext` to the first index `i` not less than the current value of `iNext` such that
         * `entries[i] != null`, or set it to `entries.length` if there are no remaining non-null
         * entries.  Note that if `iNext` is already the index of a non-null entry, then it will not
         * be changed.
         */
        private void findNext() {
            while (iNext < entries.length && entries[iNext] == null) {
                iNext += 1;
            }
        }

        @Override
        public boolean hasNext() {
            return iNext < entries.length;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            V ans = entries[iNext].value;
            iNext += 1;
            findNext();
            return ans;
        }
    }
}
