package cs2110;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A sequence of elements of type `T`, which can be accessed via their 0-based index.  This is a
 * simpler alternative to Java's `List` interface.
 */
public interface IndexedSeq<T> extends Iterable<T> {

    /**
     * Return the element at index `index`.  Throws `IndexOutOfBoundsException` if `index` is
     * negative or is not less than this sequence's size.
     */
    T get(int index);

    /**
     * Assign `value` to the element at index `index`.  Throws `IndexOutOfBoundsException` if
     * `index` is negative or is not less than this sequence's size.  Requires `value` is not null.
     */
    void set(int index, T value);

    /**
     * Return the number of elements in this sequence.
     */
    int size();

    /**
     * Append `value` to the end of this sequence.  Requires `value` is not null.
     */
    void add(T value);

    /**
     * Truncate this sequence, retaining only the elements with indices `[0..end)`.
     */
    void truncate(int end);

    /**
     * Return whether this sequence currently contains no elements.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Return the last element of this sequence.  Throws `NoSuchElementException` if this sequence
     * is empty.
     */
    default T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return get(size() - 1);
    }

    /**
     * Remove and return the last element of this sequence.  Throws `NoSuchElementException` if this
     * sequence is empty.
     */
    default T removeLast() {
        T ans = getLast();
        truncate(size() - 1);
        return ans;
    }

    /**
     * Swap the elements at indices `i` and `j`.  Throws `IndexOutOfBoundsException` if `i` or `j`
     * is negative or is not less than this list's size.
     */
    default void swap(int i, int j) {
        // First two calls to `get()` will perform necessary bounds checks before any mutation.
        T tmp = get(i);
        set(i, get(j));
        set(j, tmp);
    }

    /**
     * Sort the distinct elements in this sequence in ascending order, discarding duplicate copies.
     * Requires `T` is `Comparable` to itself (that is, `T` satisfies the bounds on `U`).
     */
    default <U extends Comparable<? super U>> void sortDistinct() {
        // Note to students: This is a hack that we're using to spare you the complexity of using
        //  `Comparator` throughout this assignment.  `Comparator`s exist to avoid hacks like these
        //  - they are the preferred approach in production code, despite the increased verbosity.

        // `T` must be compatible with `U` as a precondition, so we perform this unchecked cast in
        //  order to pass ourselves to methods requiring `Comparable` types.
        @SuppressWarnings("unchecked")
        IndexedSeq<U> comparableThis = (IndexedSeq<U>) this;
        Sorting.sortDistinct(comparableThis);
    }

    @Override
    default Iterator<T> iterator() {
        return new Itr<>(this);
    }

    /**
     * Return whether `a` and `b` have the same elements.  That is, every element in `a` is equal to
     * the corresponding element in `b`, and `b` has no additional elements beyond those in `a`. The
     * result is agnostic to the dynamic types of `a` and `b`.
     * <p>
     * Implementations may choose to call this when overriding their own `equals()`.
     */
    static boolean equals(IndexedSeq<?> a, IndexedSeq<?> b) {
        if (a.size() != b.size()) {
            return false;
        }
        Iterator<?> itA = a.iterator();
        Iterator<?> itB = b.iterator();
        while (itA.hasNext()) {
            assert itB.hasNext();
            if (!itA.next().equals(itB.next())) {
                return false;
            }
        }
        assert !itB.hasNext();
        return true;
    }

    /**
     * Return a hash code derived from the hash codes of all of the elements in `seq`, dependent on
     * their ordering.  Implementations may choose to call this when overriding their own
     * `hashCode()`.
     */
    static int hashCode(IndexedSeq<?> seq) {
        // This implementation is very inefficient (in order to not bias students' exploration of
        //  hashing techniques), but since IndexedSeqs are mutable, we don't expect them to be
        //  hashed in practice.
        Object[] elements = new Object[seq.size()];
        for (int i = 0; i < seq.size(); i += 1) {
            elements[i] = seq.get(i);
        }
        return java.util.Arrays.hashCode(elements);
    }

    /**
     * Return a String representation of `seq`.
     */
    static <T> String toString(IndexedSeq<T> seq) {
        StringBuilder ans = new StringBuilder("[");
        boolean first = true;
        for (T value : seq) {
            if (!first) {
                ans.append(", ");
            }
            ans.append(value);
            first = false;
        }
        ans.append("]");
        return ans.toString();
    }

    /**
     * An iterator over an indexed sequence of elements of type `T`.  The sequence should not be
     * structurally modified while an iterator instance is alive.
     */
    class Itr<T> implements Iterator<T> {
        // Note: since this class is nested in an interface, it is effectively a static nested
        //  class, not an inner class.

        /**
         * The sequence being iterated over.
         */
        private final IndexedSeq<T> seq;

        /**
         * The index of the next element to yield, or `seq.size()` if the iterator has been
         * exhausted.
         */
        private int iNext;

        /**
         * Create an iterator over `seq`.
         */
        Itr(IndexedSeq<T> seq) {
            this.seq = seq;
            iNext = 0;
        }

        @Override
        public boolean hasNext() {
            return iNext < seq.size();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T ans = seq.get(iNext);
            iNext += 1;
            return ans;
        }
    }
}
