package cs2110;

/**
 * A dictionary mapping String keys to values of type `V`.  This is a simpler, specialized
 * alternative to Java's `Map` interface.  Supports iterating over _values_ in an unspecified
 * order.
 */
public interface StringDict<V> extends Iterable<V> {

    /**
     * Return the number of keys currently mapped to values in this dictionary.
     */
    int size();

    /**
     * Return the value associated with key `key`.  Throws `NoSuchElementException` if no value is
     * associated with that key.  Requires `key` is not null.
     */
    V get(String key);

    /**
     * Associate value `value` with key `key`.  Requires `key` and `value` are not null.
     */
    void put(String key, V value);

    /**
     * Return whether a value is associated with key `key`.
     */
    boolean containsKey(String key);
}
