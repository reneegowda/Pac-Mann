package cs2110;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A sequence of elements of type `T` implemented as a doubly-linked list.  Null elements are not
 * allowed.
 */
public class DLinkedSeq<T> implements Seq<T> {

    /**
     * Number of elements in the sequence.  Equal to the number of linked nodes reachable from
     * `head` using `next` arrows.
     */
    private int size;

    /**
     * First node of the doubly-linked sequence (null for empty sequence). `head.prev` must be null
     */
    private DNode head;

    /**
     * Last node of the doubly-linked sequence (null for empty sequence). `tail.next` must be null.
     */
    private DNode tail;

    /**
     * Assert that this object satisfies its class invariants.
     */
    private void assertInv() {
        assert size >= 0;
        if (size == 0) {
            assert head == null;
            assert tail == null;
        } else {
            assert head != null;
            assert tail != null;

            DNode currentNode = head;
            DNode prev = null;
            int count = 0;

            while (currentNode != null) {
                // Check that the current node does not store a null element
                assert currentNode.data != null;

                // Check the consistency of the linking (n.next.prev == n) for non-tail nodes
                if (currentNode.next != null) {
                    assert currentNode.next.prev == currentNode;
                }

                // Move to the next node
                prev = currentNode; // Keep track of the current node as prev for the next
                // iteration
                currentNode = currentNode.next;
                count++;
            }

            // Check the number of nodes is equal to the size of the sequence
            assert count == size;

            // Check that the last node is the same as tail
            assert prev == tail;
        }
    }

    /**
     * Create an empty sequence.
     */
    public DLinkedSeq() {
        size = 0;
        head = null;
        tail = null;
        assertInv();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void prepend(T elem) {
        assertInv();
        assert elem != null;

        DNode newHead = new DNode(elem, null, head);
        if (size == 0) {
            tail = newHead; // if sequence was empty, assign tail as well
        } else {
            head.prev = newHead; // sequence had a different head, must point back to new head
        }
        head = newHead;
        size += 1;
        assertInv();
    }

    @Override
    public void append(T elem) {
        // Implementation constraint: efficiency must not depend on the size of the sequence.
        assertInv();
        assert elem != null;
        DNode newTail = new DNode(elem, tail, null);
        if (tail != null) {
            tail.next = newTail;  // Link the old tail to the new node
        } else {
            head = newTail; // If sequence was empty, new tail is also head
        }
        tail = newTail;
        size++;
        assertInv();
    }

    @Override
    public T get(int index) {
        assert 0 <= index && index < size();
        // Start from either head or tail, whichever is closer
        DNode currentNode;
        if (index < size / 2) {
            // Start from head if the index is in the first half
            currentNode = head;
            for (int i = 0; i < index; i++) {
                currentNode = currentNode.next;
            }
        } else {
            // Start from tail if the index is in the second half
            currentNode = tail;
            for (int i = size - 1; i > index; i--) {
                currentNode = currentNode.prev;
            }
        }
        // Return the data at the index
        return currentNode.data;
    }

    /**
     * Return the first node `n` such that `n.data.equals(elem)`, or null if this sequence does not
     * contain `elem`.
     */
    private DNode firstNodeWith(T elem) {
        assert elem != null;
        DNode currentNode = head;
        while (currentNode != null) {
            if (currentNode.data.equals(elem)) {
                return currentNode; // Found the node containing elem
            }
            currentNode = currentNode.next; // Move to the next node
        }
        return null; // Return null if elem is not found
    }

    @Override
    public boolean contains(T elem) {
        // Implementation constraint: use `firstNodeWith()` to avoid code duplication.
        assert elem != null;
        return firstNodeWith(elem) != null;
    }

    @Override
    public void insertBefore(T elem, T successor) {
        // Implementation constraint: use `firstNodeWith()` to avoid code duplication.
        assertInv();
        assert elem != null && successor != null;

        DNode succNode = firstNodeWith(successor);
        DNode newNode = new DNode(elem, succNode.prev, succNode);

        if (succNode.prev != null) {
            succNode.prev.next = newNode;
        } else {
            head = newNode;
        }
        succNode.prev = newNode;
        size++;
        assertInv();
    }

    @Override
    public boolean remove(T elem) {
        // Implementation constraint: use `firstNodeWith()` to avoid code duplication.
        assertInv();
        assert elem != null;

        // Handle empty sequence case
        if (size == 0) {
            return false; // No elements to remove
        }

        DNode nodeToRemove = firstNodeWith(elem);
        if (nodeToRemove == null) {
            return false; // Element not found
        }
        // Re-link the previous and next nodes
        if (nodeToRemove.prev != null) {
            nodeToRemove.prev.next = nodeToRemove.next;
        } else {
            head = nodeToRemove.next; // Updating head if it's the first element
        }
        if (nodeToRemove.next != null) {
            nodeToRemove.next.prev = nodeToRemove.prev;
        } else {
            tail = nodeToRemove.prev; // Updating tail if it's the last element
        }
        size--;
        assertInv(); // Ensure class invariants hold after modification
        return true; // Successfully removed
    }

    /**
     * Return whether this and `other` are `DLinkedSeq`s containing the same elements in the same
     * order.  Two elements `e1` and `e2` are "the same" if `e1.equals(e2)`.  Note that `DLinkedSeq`
     * is mutable, so equivalence between two objects may change over time.  See `Object.equals()`
     * for additional guarantees.
     */
    @Override
    public boolean equals(Object other) {
        /* Note: In the `instanceof` check, we write `DLinkedSeq` instead of `DLinkedSeq<T>` because
         * of a limitation inherent in Java generics: it is not possible to check at run-time what
         * what the specific type `T` is.  So instead we check a weaker property: that `other` is
         * some (unknown) instantiation of `DLinkedSeq`.  As a result, the static type of
         * `currNodeOther.data` is `Object`.
         */
        if ((other == null) || (getClass() != other.getClass())) {
            return false;
        }

        @SuppressWarnings(("unchecked"))
        DLinkedSeq<Object> otherSeq = (DLinkedSeq) other;
        DNode currNodeThis = head;

        for (Object o : otherSeq) {
            if (currNodeThis == null || !currNodeThis.data.equals(o)) {
                return false;
            }
            currNodeThis = currNodeThis.next;
        }
        return currNodeThis == null;
    }

    /**
     * Returns a hash code value for the object.  See `Object.hashCode()` for additional
     * guarantees.
     */
    @Override
    public int hashCode() {
        // Whenever overriding `equals()`, must also override `hashCode()` to be consistent.
        // This hash recipe is recommended in _Effective Java_ (Joshua Bloch, 2008).
        int hash = 1;
        for (T e : this) {
            hash = 31 * hash + e.hashCode();
        }
        return hash;
    }

    /**
     * Return a text representation of this sequence with the following format: the string starts
     * with '[' and ends with ']'.  In between are the string representations of each element, in
     * sequence order, separated by ", ".
     * <p>
     * Example: a sequence containing 4 7 8 in that order would be represented by "[4, 7, 8]".
     * <p>
     * Example: a sequence containing two empty strings would be represented by "[, ]".
     * <p>
     * The string representations of elements may contain the characters '[', ',', and ']'; these
     * are not treated specially.
     */
    @Override
    public String toString() {
        // Note: We don't take advantage of being `Iterable` here (as in ArraySeq) so that you can
        //  use strings for testing before you've implemented DLinkedSeqIterator.

        StringBuilder str = new StringBuilder("[");
        DNode node = head;
        while (node != null) {
            str.append(node.data);
            if (node.next != null) {
                str.append(", ");
            }
            node = node.next;
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Return an iterator over the elements of this sequence (in order).  By implementing
     * `Iterable`, clients can use Java's "enhanced for-loops" to iterate over the elements of the
     * sequence.  Requires that the sequence not be mutated while the iterator is in use (except via
     * a single active Iterator's `remove()` method).
     */
    @Override
    public Iterator<T> iterator() {
        return new DLinkedSeqIterator();
    }

    /**
     * A forward iterator over a doubly-linked sequence.  Generally requires that the sequence is
     * not mutated except via this iterator's own `remove()` method.  Optional behavior: methods may
     * throw ConcurrentModificatoinException if sequence is mutated by any means other than this
     * iterator's own `remove()` method.
     */
    protected class DLinkedSeqIterator implements Iterator<T> {

        /**
         * The current node in the linked list during iteration. This field keeps track of the node
         * that the iterator is currently pointing to. It is updated each time the next() method is
         * called to move the iterator forward.
         */
        private DNode currentNode;

        /**
         * The last node returned by the next() method. This field is used to keep track of the node
         * that was most recently returned by the iterator, so that it can be removed by the
         * remove() method if called. It is set to null after a successful removal or when the
         * iterator is reset.
         */
        private DNode lastReturnedNode;

        /**
         * A flag indicating whether the remove() method can be called. This flag is set to true
         * after a call to the next() method, allowing the remove() method to be called exactly once
         * to remove the last returned element. It is reset to false after a call to remove() or
         * when the iterator is in a state where no element has been returned (e.g., at the start of
         * iteration).
         */
        private boolean canRemove = false;

        /**
         * Constructs an iterator starting at the head of the sequence.
         */
        public DLinkedSeqIterator() {
            currentNode = head; // Start at the first node
        }

        @Override
        public boolean hasNext() {
            // Implementation constraint: efficiency must not depend on the size of the sequence.
            return currentNode != null;
        }

        /**
         * Return the next element in the iteration, advancing this iterator. Throws
         * NoSuchElementException if the iteration has no more elements.
         */
        @Override
        public T next() {
            // Implementation constraint: efficiency must not depend on the size of the sequence.
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturnedNode = currentNode;  // Track the last returned node
            T data = currentNode.data;       // Get the data from the current node
            currentNode = currentNode.next; // Move to the next node
            canRemove = true;                // Allow remove() to be called
            return data;
        }

        /**
         * Removes from the underlying collection the last element returned by this iterator. Does
         * not affect the next element that will be returned from the iteration.  Throws
         * IllegalStateException if `next()` has not yet been called or if `remove()` has already
         * been called since the last call to `next()`.
         */
        @Override
        public void remove() {
            // Implementation constraint: efficiency must not depend on the size of the sequence.
            // Check if remove() can be called
            if (!canRemove) {
                throw new IllegalStateException(
                        "remove() can only be called once per call to next()");
            }

            // Remove the lastReturnedNode from the list
            if (lastReturnedNode.prev != null) {
                lastReturnedNode.prev.next = lastReturnedNode.next; // Update the previous node's
                // next pointer
            } else {
                // If the node to be removed is the head, update the head
                head = lastReturnedNode.next;
            }

            if (lastReturnedNode.next != null) {
                lastReturnedNode.next.prev = lastReturnedNode.prev; // Update the next node's prev
                // pointer
            } else {
                // If the node to be removed is the tail, update the tail
                tail = lastReturnedNode.prev;
            }
            //Decrement the size of the sequence
            size--;
            // Clear the lastReturnedNode reference
            lastReturnedNode = null;
            canRemove = false; // Disallow remove() until next() is called again
        }
    }

    /**
     * A node of a doubly-linked sequence whose elements have type `T`.
     */
    private class DNode {

        /**
         * The element in this node.
         */
        final T data;

        /**
         * Next node in the sequence (null if this is the last node).
         */
        DNode next;

        /**
         * Previous node in the sequence (null if this is the first node).
         */
        DNode prev;

        /**
         * Create a Node containing element `elem`, pointing backward to node 'prev' (may be null),
         * and pointing forward to node `next` (may be null).
         */
        DNode(T elem, DNode prev, DNode next) {
            data = elem;
            this.prev = prev;
            this.next = next;
        }
    }
}
