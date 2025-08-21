package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) double
 * priorities, implemented using a binary heap paired with a hash table.
 */
public class MinPQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, double priority) {
        // Note: This is equivalent to declaring a static nested class with fields `key` and
        //  `priority` and a corresponding constructor and observers, overriding `equals()` and
        //  `hashCode()` to depend on the fields, and overriding `toString()` to print their values.
        // https://docs.oracle.com/en/java/javase/17/language/records.html
    }

    /**
     * ArrayList representing a binary min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size())`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Associates each element in the queue with its index in `heap`. Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final Map<KeyType, Integer> index;

    /**
     * Asserts that all class invariants are satisfied.
     *
     * @throws AssertionError if any invariant is violated
     */
    private void assertInv() {
        // Check that index size matches heap size
        assert index.size() == heap.size();

        // Check heap property: parent priority <= child priority
        for (int i = 1; i < heap.size(); i++) {
            int parent = (i - 1) / 2;
            assert heap.get(parent).priority <= heap.get(i).priority;
        }

        // Check that index correctly maps keys to their positions
        for (int i = 0; i < heap.size(); i++) {
            KeyType key = heap.get(i).key;
            assert index.containsKey(key);
            assert index.get(key) == i;
        }
    }

    /**
     * Create an empty queue.
     */
    public MinPQueue() {
        index = new HashMap<>();
        heap = new ArrayList<>();
        assertInv();
    }

    /**
     * Return whether this queue contains no elements.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType peek() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws
     * NoSuchElementException if this queue is empty.
     */
    public double minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * Swap the Entries at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires
     * {@code 0 <= i,j < heap.size()}.
     */
    private void swap(int i, int j) {
        Entry<KeyType> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);

        index.put(heap.get(i).key, i);
        index.put(heap.get(j).key, j);
    }


    /**
     * Restores the heap property by moving the entry at index `i` upward as necessary. After
     * execution, the priority of the entry at `i` will be greater than or equal to its parent's
     * priority, and less than or equal to its children's priorities.
     *
     * @param i the index of the entry to bubble up
     * @requires 0 <= i < heap.size()
     * @modifies this.heap, this.index
     * @effects The entry at `i` may be moved upward in the heap until the heap property is restored
     * (parent priority <= child priority)
     */
    private void bubbleUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;  // Parent index calculation
            if (heap.get(parent).priority <= heap.get(i).priority) {
                break;  // Stop if the current entry is larger than or equal to the parent's priority
            }
            swap(i, parent);  // Swap current element with its parent
            i = parent;  // Move the index up to the parent's position
        }
    }

    /**
     * Restores the heap property by moving the entry at index `i` downward as necessary. After
     * execution, the priority of the entry at `i` will be greater than or equal to its parent's
     * priority (if any), and less than or equal to its children's priorities.
     *
     * @param i the index of the entry to bubble down
     * @requires 0 <= i < heap.size()
     * @modifies this.heap, this.index
     * @effects The entry at `i` may be moved downward in the heap until the heap property is
     * restored (parent priority <= child priority)
     */
    private void bubbleDown(int i) {
        while (true) {
            int left = 2 * i + 1;  // Left child index
            int right = 2 * i + 2;  // Right child index
            int smallest = i;  // Assume the current element is the smallest

            // Check if left child exists and has a smaller priority than the current element
            if (left < heap.size() && heap.get(left).priority < heap.get(smallest).priority) {
                smallest = left;  // If left child has smaller priority, set smallest to left
            }

            // Check if right child exists and has a smaller priority than the current element or left child
            if (right < heap.size() && heap.get(right).priority < heap.get(smallest).priority) {
                smallest = right;  // If right child has smaller priority, set smallest to right
            }

            // If the current element is not the smallest, swap it with the smallest child
            if (smallest != i) {
                swap(i, smallest);  // Swap current element with the smallest child
                i = smallest;  // Move the index down to the child's position
            } else {
                break;  // If the heap property is restored, exit the loop
            }
        }
    }

    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, double priority) {
        assert !index.containsKey(key);
        assertInv();
        Entry<KeyType> entry = new Entry<>(key, priority);
        heap.add(entry);
        index.put(key, heap.size() - 1);
        bubbleUp(heap.size() - 1);
        assertInv();
    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, double priority) {
        assert index.containsKey(key);  // Ensure the key exists in the queue
        assertInv();  // Check the invariants before making changes

        // Get the index of the element in the heap
        int i = index.get(key);
        double oldPriority = heap.get(i).priority;  // Get the current priority of the element

        // Update the priority of the element at index `i`
        heap.set(i, new Entry<>(key, priority));

        // Update the index map to reflect the new priority position
        index.put(key, i); // Ensuring the index map reflects the correct position

        // If the new priority is smaller, bubble up
        if (priority < oldPriority) {
            bubbleUp(i);  // Move the element up if the priority decreased
        }
        // If the new priority is larger, bubble down
        else if (priority > oldPriority) {
            bubbleDown(i);  // Move the element down if the priority increased
        }

        assertInv();  // Check the invariants after the changes
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    public void addOrUpdate(KeyType key, double priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType remove() {
        assertInv();
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        KeyType minKey = heap.get(0).key;
        if (heap.size() == 1) {
            heap.remove(0);
            index.remove(minKey);
        } else {
            Entry<KeyType> last = heap.remove(heap.size() - 1);
            heap.set(0, last);
            index.put(last.key, 0);
            index.remove(minKey);
            bubbleDown(0);
        }
        assertInv();
        return minKey;
    }
}
