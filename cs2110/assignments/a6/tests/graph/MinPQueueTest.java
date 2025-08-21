package graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MinPQueueTest {

    @DisplayName("WHEN a new MinPQueue is created, THEN its size will be 0 AND it will be empty")
    @Test
    void testNew() {
        MinPQueue<Integer> q = new MinPQueue<>();

        assertEquals(0, q.size());
        assertTrue(q.isEmpty());
    }


    @DisplayName("GIVEN an empty MinPQueue, WHEN an element is added, THEN its size will become 1 "
            + "AND it will no longer be empty")
    @Test
    void testAddToEmpty() {
        MinPQueue<Integer> q = new MinPQueue<>();

        q.addOrUpdate(0, 0);
        assertEquals(1, q.size());
        assertFalse(q.isEmpty());
    }

    @DisplayName("GIVEN a non-empty MinPQueue, WHEN a distinct elements are added, "
            + "THEN its size will increase by 1")
    @Test
    void testAddDistinct() {
        MinPQueue<Integer> q = new MinPQueue<>();

        // Add distinct elements to the queue
        q.addOrUpdate(10, 10);  // Add element 10 with priority 10
        assertEquals(1, q.size());  // Size should be 1

        q.addOrUpdate(20, 20);  // Add element 20 with priority 20
        assertEquals(2, q.size());  // Size should be 2

        q.addOrUpdate(5, 5);  // Add element 5 with priority 5
        assertEquals(3, q.size());  // Size should be 3

        q.addOrUpdate(15, 15);  // Add element 15 with priority 15
        assertEquals(4, q.size());  // Size should be 4

        q.addOrUpdate(25, 25);  // Add element 25 with priority 25
        assertEquals(5, q.size());  // Size should be 5
    }

    @DisplayName("GIVEN a MinPQueue containing an element x whose priority is not the minimum, "
            + "WHEN x's priority is updated to become the unique minimum, "
            + "THEN the queue's size will not change "
            + "AND getting the minimum-priority element will return x "
            + "AND getting the minimum priority will return x's updated priority")
    @Test
    void testUpdateReduce() {
        MinPQueue<Integer> q = new MinPQueue<>();

        // Add elements to the queue with different priorities
        q.addOrUpdate(1, 10);  // Element 1 with priority 10
        q.addOrUpdate(2, 5);   // Element 2 with priority 5 (the minimum)
        q.addOrUpdate(3, 30);  // Element 3 with priority 30

        // Check the initial state - element 2 has minimum priority
        assertEquals(Integer.valueOf(2), q.peek());
        assertEquals(5, q.minPriority());
        assertEquals(3, q.size());

        // Update element 1's priority to become the unique minimum
        q.addOrUpdate(1, 3);

        // THEN the queue's size will not change
        assertEquals(3, q.size());

        // AND getting the minimum-priority element will return x
        assertEquals(Integer.valueOf(1), q.peek());

        // AND getting the minimum priority will return x's updated priority
        assertEquals(3, q.minPriority());
    }

    @DisplayName("GIVEN a non-empty MinPQueue, WHEN an element is removed,"
            + " THEN it size will decrease by 1.  IF its size was 1, THEN it will become empty.")
    @Test
    void testRemoveSize() {
        MinPQueue<Integer> q = new MinPQueue<>();

        // Add elements to the queue
        q.addOrUpdate(1, 10);  // Element 1 with priority 10
        q.addOrUpdate(2, 20);  // Element 2 with priority 20
        q.addOrUpdate(3, 30);  // Element 3 with priority 30

        // Check the size before removal
        assertEquals(3, q.size());  // Size should be 3

        // Remove an element and check the size
        q.remove();
        assertEquals(2, q.size());  // Size should be 2 after removal

        // Remove another element and check the size
        q.remove();
        assertEquals(1, q.size());  // Size should be 1 after removing another element

        // Remove the last element and check if the queue is empty
        q.remove();
        assertTrue(q.isEmpty());  // Queue should be empty after removing the last element
    }

    @DisplayName("GIVEN a MinPQueue containing elements whose priorities follow their natural "
            + "ordering, WHEN elements are successively removed, THEN they will be returned in "
            + "ascending order")
    @Test
    void testRemoveElementOrder() {
        MinPQueue<Integer> q = new MinPQueue<>();
        int nElem = 20;

        // Add distinct elements in random order (priority equals element)
        {
            List<Integer> elems = new ArrayList<>();
            for (int i = 0; i < nElem; i += 1) {
                elems.add(i);
            }
            int seed = 1;
            Random rng = new Random(seed);
            Collections.shuffle(elems, rng);
            for (Integer x : elems) {
                q.addOrUpdate(x, x);
            }
        }

        // Remove elements and check order
        int prevElem = q.remove();
        for (int i = 1; i < nElem; ++i) {
            assertEquals(nElem - i, q.size());
            int nextElem = q.peek();
            int removedElem = q.remove();
            assertEquals(nextElem, removedElem);
            assertTrue(nextElem > prevElem);
            prevElem = nextElem;
        }
        assertTrue(q.isEmpty());
    }

    @DisplayName("GIVEN a MinPQueue (whose elements' priorities may have been updated), "
            + "WHEN elements are successively removed, "
            + "THEN the minimum priority will not decrease after each removal")
    @Test
    void testRemovePriorityOrder() {
        MinPQueue<Integer> q = new MinPQueue<>();
        int nUpdates = 100;

        // Add random elements with random priorities to queue and randomly update some elements'
        //  priorities.
        int seed = 1;
        Random rng = new Random(seed);
        int bound = nUpdates / 2;
        for (int i = 0; i < nUpdates; i += 1) {
            int key = rng.nextInt(bound);
            int priority = rng.nextInt(bound);
            q.addOrUpdate(key, priority);
        }

        // Remove until 1 left, but no more than nUpdates times (to prevent infinite loop in test)
        for (int i = 0; q.size() > 1 && i < nUpdates; i += 1) {
            double removedPriority = q.minPriority();
            q.remove();
            assertTrue(q.minPriority() >= removedPriority);
        }
        q.remove();
        assertTrue(q.isEmpty());
    }

    @DisplayName("GIVEN an empty MinPQueue, WHEN attempting to query the next element "
            + "OR query the minimum priority OR remove the next element "
            + "THEN a NoSuchElementException will be thrown")
    @Test
    void testExceptions() {
        MinPQueue<Integer> q = new MinPQueue<>();
        // Test peek (should throw exception)
        assertThrows(NoSuchElementException.class, () -> q.peek());

        // Test minPriority (should throw exception)
        assertThrows(NoSuchElementException.class, () -> q.minPriority());

        // Test remove (should throw exception)
        assertThrows(NoSuchElementException.class, () -> q.remove());
    }
}
