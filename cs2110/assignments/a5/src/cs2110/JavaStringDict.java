package cs2110;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A dictionary mapping String keys to values of type `V`, backed by a Java Map.  Changes made to
 * this map are reflected in the original map, and vice versa.  Null keys and values must never be
 * added to the backing map.
 */
public class JavaStringDict<V> implements StringDict<V> {

    /**
     * The Java Map backing this dictionary.  Must not contain null keys or values.
     */
    private final Map<String, V> map;

    /**
     * Return the Java Map backing this dictionary.
     */
    public Map<String, V> getMap() {
        return map;
    }


    /**
     * Create a `StringDict` backed by `map`.  Requires `map` is mutable and does not contain any
     * null keys or values.
     */
    public JavaStringDict(Map<String, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public V get(String key) {
        assert key != null;
        V ans = map.get(key);
        if (ans == null) {
            throw new NoSuchElementException();
        }
        return ans;
    }

    @Override
    public void put(String key, V value) {
        assert key != null;
        assert value != null;
        map.put(key, value);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Iterator<V> iterator() {
        return map.values().iterator();
    }
}
