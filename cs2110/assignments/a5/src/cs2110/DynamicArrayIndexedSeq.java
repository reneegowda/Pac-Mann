package cs2110;

import java.util.Arrays;

/**
 * An indexed sequence of elements of type `T` backed by a dynamic array.
 */
public class DynamicArrayIndexedSeq<T> implements IndexedSeq<T> {

    /**
     * The backing array for this sequence.  `store[0..size)` contains the elements with indices
     * `[0..size)`.  `store[size..) == null`.
     */
    private T[] store;

    /**
     * The number of elements in this sequence.
     */
    private int size;

    /**
     * The initial capacity of the backing array for new instances of `DynamicArrayIndexedSeq`.
     */
    private static final int INITIAL_CAPACITY = 16;

    /**
     * Create an empty `DynamicArrayIndexedSeq`.
     */
    public DynamicArrayIndexedSeq() {
        @SuppressWarnings("unchecked")
        T[] store = (T[]) new Object[INITIAL_CAPACITY];
        this.store = store;
        size = 0;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return store[index];
    }

    @Override
    public void set(int index, T value) {
        assert value != null;
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        store[index] = value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void add(T value) {
        assert value != null;
        // Ensure sufficient capacity
        if (size == store.length) {
            store = Arrays.copyOf(store, Math.max(2 * store.length, 1));
        }

        store[size] = value;
        size += 1;
    }

    @Override
    public void truncate(int end) {
        if (end < store.length / 2) {
            // If our fill factor would be less than 50%, allocate a new backing array with no
            //  additional capacity.
            store = Arrays.copyOf(store, end);
        } else {
            Arrays.fill(store, end, store.length, null);
        }
        size = end;
    }

    @Override
    public String toString() {
        return IndexedSeq.toString(this);
    }
}
