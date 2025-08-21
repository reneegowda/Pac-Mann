package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for `StringDict` implementations.  Subclasses must provide a way to create a dict
 * instance to test.
 */
abstract class StringDictTest {
    // Note: For compatibility with the `TrieStringDict` challenge extension, be sure the tests in
    //  this class only use keys with capital letters and digits.

    /**
     * Create a new, empty dictionary of the class to be tested (mapping String keys to values of
     * type `V`).
     */
    abstract <V> StringDict<V> makeDict();

    /**
     * Create a new dictionary of the class to be tested containing the same mappings as `map`.
     * Requires `map`'s keys and values are non-null.
     */
    <V> StringDict<V> fromMap(Map<String, V> map) {
        StringDict<V> dict = makeDict();
        for (Map.Entry<String, V> kv : map.entrySet()) {
            // Deliberately avoid string aliasing
            dict.put(new String(kv.getKey()), kv.getValue());
        }
        return dict;
    }

    @DisplayName("A newly-created dict is initially empty")
    @Test
    void testConstruction() {
        StringDict<Integer> dict = makeDict();
        assertEquals(0, dict.size());

        assertFalse(dict.iterator().hasNext());
    }

    @DisplayName("Associating a value with a new key in a dict will increase its size by 1, the "
            + "key will be contained in the dict, and the mapped value will be returned by "
            + "`get()`.")
    @Test
    void testAddition() {
        StringDict<Integer> dict = makeDict();
        dict.put(new String("KEY"), 42);
        assertEquals(1, dict.size());
        assertTrue(dict.containsKey(new String("KEY")));
        assertEquals(42, dict.get(new String("KEY")));
    }

    @DisplayName("Getting a value that does not exist throws NoSuchElementException")
    @Test
    void testGetNonexistentKeyThrows() {
        StringDict<Integer> dict = makeDict();
        assertThrows(NoSuchElementException.class, () -> dict.get("MISSING"));
    }

    @DisplayName("containsKey returns false when key is missing")
    @Test
    void testContainsKeyFalse() {
        StringDict<Integer> dict = makeDict();
        dict.put("EXISTING", 99);
        assertFalse(dict.containsKey("MISSING"));
    }

    @DisplayName("getMap returns the backing map with correct values")
    @Test
    void testGetMap() {
        Map<String, Integer> backingMap = new HashMap<>();
        backingMap.put("A", 1);
        backingMap.put("B", 2);

        JavaStringDict<Integer> dict = new JavaStringDict<>(backingMap);
        Map<String, Integer> mapFromGetter = dict.getMap();
        assertEquals(2, mapFromGetter.size());
        assertEquals(1, mapFromGetter.get("A"));
        assertEquals(2, mapFromGetter.get("B"));
    }

    @DisplayName("Iterator traverses all values in the dict")
    @Test
    void testIterator() {
        StringDict<Integer> dict = makeDict();
        dict.put("ONE", 1);
        dict.put("TWO", 2);
        dict.put("THREE", 3);

        Set<Integer> expected = Set.of(1, 2, 3);
        Set<Integer> actual = new HashSet<>();
        for (int val : dict) {
            actual.add(val);
        }
        assertEquals(expected, actual);
    }

    @DisplayName("fromMap properly transfers values and avoids aliasing")
    @Test
    void testFromMap() {
        Map<String, String> source = new HashMap<>();
        source.put("HELLO", "WORLD");
        source.put("FOO", "BAR");

        StringDict<String> dict = fromMap(source);
        assertEquals(2, dict.size());
        assertTrue(dict.containsKey("HELLO"));
        assertEquals("WORLD", dict.get("HELLO"));
        assertEquals("BAR", dict.get("FOO"));
    }

    // TODO 11: Write additional testcases to thoroughly cover `StrictDict` functionality.  Your
    //  test suite will be evaluated on whether or not it catches common bugs.
    //  If you plan to complete the `TrieStringDict` challenge extension, be sure these tests only
    //  use keys with capital letters and digits.
}

/**
 * Test suite for `JavaStringDict`.
 */
class JavaStringDictTest extends StringDictTest {

    @Override
    <V> StringDict<V> makeDict() {
        return new JavaStringDict<>(new HashMap<>());
    }
}

/**
 * Test suite for `ProbingStringDict`.  Includes additional testcases specific to that
 * implementation.
 */
class ProbingStringDictTest extends StringDictTest {

    @Override
    <V> ProbingStringDict<V> makeDict() {
        return new ProbingStringDict<>();
    }

    // This is an example of testing functionality that is specific to `ProbingStringDict` (that is,
    //  these tests would not make sense for other `StringDict` implementations).  You may add your
    //  own tests here to aid in debugging (note: such tests do not count towards TODO 11).

    @DisplayName("The load factor of an empty dict must be 0")
    @Test
    void testLoadFactorEmpty() {
        ProbingStringDict<Integer> dict = makeDict();
        assertEquals(0, dict.loadFactor());
    }

    @Test
    @DisplayName("Basic put/get functionality with multiple entries")
    void testPutGetBasic() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("cat", "meow");
        dict.put("dog", "woof");
        dict.put("bird", "tweet");

        assertEquals("meow", dict.get("cat"));
        assertEquals("woof", dict.get("dog"));
        assertEquals("tweet", dict.get("bird"));
        assertEquals(3, dict.size());
    }

    @Test
    @DisplayName("Put should overwrite existing key")
    void testPutOverwrite() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("key", "old");
        dict.put("key", "new");

        assertEquals("new", dict.get("key")); // Value is updated
        assertEquals(1, dict.size());         // Size should remain 1
    }

    @Test
    @DisplayName("Put triggers resize and retains all values")
    void testPutResize() {
        ProbingStringDict<String> dict = makeDict();
        for (int i = 0; i < 50; i++) {
            dict.put("k" + i, "v" + i);
        }
        assertEquals(50, dict.size());
        for (int i = 0; i < 50; i++) {
            assertEquals("v" + i, dict.get("k" + i));
        }
    }

    @Test
    @DisplayName("Get throws for missing key")
    void testGetMissing() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("exists", "yes");
        assertThrows(NoSuchElementException.class, () -> dict.get("missing"));
    }

    @Test
    @DisplayName("containsKey returns true if found, false if not")
    void testContainsKey() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("a", "1");
        assertTrue(dict.containsKey("a"));
        assertFalse(dict.containsKey("z"));
    }

    @Test
    @DisplayName("Edge case: insert at last index wraps around")
    void testWrapAroundInsert() {
        ProbingStringDict<String> dict = new ProbingStringDict<>() {
            @Override
            protected int hash(String key) {
                return entries.length - 1; // force wrap-around
            }
        };
        dict.put("wrap1", "one");
        dict.put("wrap2", "two");
        assertEquals("one", dict.get("wrap1"));
        assertEquals("two", dict.get("wrap2"));
    }

    @Test
    @DisplayName("Edge case: findEntry handles deleted slot (no infinite loop)")
    void testDeletedSlotNotInfiniteLoop() {
        ProbingStringDict<String> dict = new ProbingStringDict<>() {
            {
                @SuppressWarnings("unchecked")
                Entry<String, String>[] arr = new Entry[3];
                this.entries = arr;
                this.maxLoadFactor = 1.0;
                arr[0] = new Entry<>("a", "A");
                arr[1] = new Entry<>("b", "B");
                arr[2] = null;
                this.size = 2;
            }

            @Override
            protected int hash(String key) {
                return 0;
            }
        };
        dict.put("c", "C"); // triggers probing with a deleted slot
        assertEquals("C", dict.get("c"));
    }

    @Test
    @DisplayName("findEntry throws if table is full and no match is found")
    void testFindEntryThrows() {
        ProbingStringDict<Integer> dict = new ProbingStringDict<>() {
            {
                @SuppressWarnings("unchecked")
                Entry<String, Integer>[] arr = new Entry[3];
                this.entries = arr;
                this.maxLoadFactor = 1.0;
                arr[0] = new Entry<>("a", 1);
                arr[1] = new Entry<>("b", 2);
                arr[2] = new Entry<>("c", 3);
                this.size = 3;
            }

            @Override
            protected int hash(String key) {
                return 0; // All collide
            }
        };
        assertThrows(NoSuchElementException.class, () -> dict.findEntry("x"));
    }

    @Test
    @DisplayName("containsKey false when table full but key not present")
    void testFullHashTable() {
        ProbingStringDict<Integer> dict = new ProbingStringDict<>() {
            {
                @SuppressWarnings("unchecked")
                Entry<String, Integer>[] arr = new Entry[3];
                this.entries = arr;
                this.maxLoadFactor = 1.0;
                arr[0] = new Entry<>("a", 1);
                arr[1] = new Entry<>("b", 2);
                arr[2] = new Entry<>("c", 3);
                this.size = 3;
            }

            @Override
            protected int hash(String key) {
                return 0; // All collide
            }
        };
        assertFalse(dict.containsKey("l")); // should not infinite loop or throw
    }

    @Test
    @DisplayName("Load factor is correct after insertions")
    void testLoadFactorAfterInsertions() {
        ProbingStringDict<Integer> dict = makeDict();
        dict.put("one", 1);
        dict.put("two", 2);
        dict.put("three", 3);
        double lf = dict.loadFactor();
        assertTrue(lf > 0 && lf <= 1); // Check valid range
    }

    @Test
    @DisplayName("Linear probing correctly resolves hash collisions")
    void testCollisionResolution() {
        ProbingStringDict<String> dict = new ProbingStringDict<>() {
            @Override
            protected int hash(String key) {
                return key.length(); // force collisions
            }
        };

        dict.put("aa", "first");
        dict.put("bb", "second");
        dict.put("cc", "third");

        assertEquals("first", dict.get("aa"));
        assertEquals("second", dict.get("bb"));
        assertEquals("third", dict.get("cc"));
    }

    @Test
    @DisplayName("Iterator returns all values in dict")
    void testIteratorValues() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("a", "A");
        dict.put("b", "B");
        dict.put("c", "C");

        Iterator<String> it = dict.iterator();
        int count = 0;
        while (it.hasNext()) {
            String val = it.next();
            assertTrue(val.equals("A") || val.equals("B") || val.equals("C"));
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Iterator skips null entries and stops correctly")
    void testIteratorEmptySpots() {
        ProbingStringDict<String> dict = new ProbingStringDict<>();
        dict.put("x", "X");

        Iterator<String> it = dict.iterator();
        assertTrue(it.hasNext());
        assertEquals("X", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    @DisplayName("Iterator throws exception when no more elements")
    void testIteratorThrowsException() {
        ProbingStringDict<String> dict = makeDict();
        dict.put("x", "X");
        Iterator<String> it = dict.iterator();
        it.next(); // consume the one element
        assertThrows(NoSuchElementException.class, it::next);
    }
}

// TODO (challenge extension): Uncomment this after implementing `TrieStringDict`.

/**
 * Test suite for `TrieStringDict`.  Only valid if superclass restricts keys to strings containing
 * only capital letters and digits.
 */
class TrieStringDictTest extends StringDictTest {

    @Override
    <V> StringDict<V> makeDict() {
        return new TrieStringDict<>();
    }
}
