package cs2110;

import java.util.Collections;
import java.util.List;

/**
 * An indexed sequence of elements of type `T` backed by a Java List.  Changes made to the sequence
 * are reflected in the original list, and vice versa.  Null values must never be added to the
 * backing list.  It is recommended that an `ArrayList` is used for the backing list.
 */
public class JavaIndexedSeq<T> implements IndexedSeq<T> {

    /**
     * The Java List backing this sequence.  Must not contain null elements.
     */
    private final List<T> list;

    /**
     * Return the Java List backing this sequence.
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Create an `IndexedSeq` backed by `list`.  Requires `list` is mutable and does not contain any
     * null values.
     */
    public JavaIndexedSeq(List<T> list) {
        for (T elem : list) {
            assert elem != null;
        }

        this.list = list;
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public void set(int index, T value) {
        assert value != null;
        list.set(index, value);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void add(T value) {
        assert value != null;
        list.add(value);
    }

    @Override
    public void truncate(int end) {
        if (end < 0 || end > list.size()) {
            throw new IndexOutOfBoundsException();
        }
        list.subList(end, list.size()).clear();
    }

    @Override
    public <U extends Comparable<? super U>> void sortDistinct() {
        // Overridden to use Java's `List.sort()` implementation

        // `T` must be compatible with `U` as a precondition, so we perform this unchecked cast in
        //  order to pass ourselves to methods requiring `Comparable` types.
        @SuppressWarnings("unchecked")
        List<U> comparableList = (List<U>) list;
        Collections.sort(comparableList);
        Sorting.deduplicateSorted(this);
    }

    @Override
    public String toString() {
        return IndexedSeq.toString(this);
    }
}
