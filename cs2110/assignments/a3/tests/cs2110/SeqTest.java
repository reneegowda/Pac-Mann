package cs2110;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public abstract class SeqTest {

    /**
     * Abstract method to construct an empty sequence. Subclasses are responsible for implementing
     * this method to construct the Seq subtype that they wish to test.
     */
    abstract <T> Seq<T> makeSeq();
    // **Important Note:** Within the tests in this class, you should always use `makeSeq()` to
    // construct any `Seq` object. This will allow these tests to be applied to both `ArraySeq`s
    // and `DLinkedSeq`s.

    // The following two methods provide convenient ways to create sequences for your test cases

    /**
     * Creates a sequence containing the same elements (in the same order) as array `elements` using
     * only `prepend` operations on an initially empty sequence.
     */
    <T> Seq<T> makeSeq(T[] elements) {
        Seq<T> seq = makeSeq();
        for (int i = elements.length; i > 0; i--) {
            seq.prepend(elements[i - 1]);
        }
        return seq;
    }

    /**
     * Creates a sequence containing the integers 0..n-1 (in sorted order) using only `prepend`
     * operations on an initially empty sequence.
     */
    Seq<Integer> makeSeqOfLength(int n) {
        Seq<Integer> seq = makeSeq();
        for (int i = n - 1; i >= 0; i--) {
            seq.prepend(i);
        }
        return seq;
    }

    @DisplayName("WHEN a Seq is first constructed, THEN it should be empty.")
    @Test
    void testConstructorSize() {
        Seq<String> list = makeSeq();
        assertEquals(0, list.size());
    }

    @DisplayName("GIVEN a Seq, WHEN an element is prepended, " +
            "THEN its size should increase by 1 each time.")
    @Test
    void testPrependSize() {
        Seq<Integer> seq = makeSeq();
        seq.prepend(1);
        assertEquals(1, seq.size());
        seq.prepend(2);
        assertEquals(2, seq.size());
        seq.prepend(3);
        assertEquals(3, seq.size());
    }

    @DisplayName("GIVEN a Seq containing a sequence of values, " +
            "THEN its string representation should include the string representations of its " +
            "values, in order, separated by a comma and space, all enclosed in square brackets.")
    @Test
    void testToString() {
        // WHEN sequence is empty
        Seq<String> seq0 = makeSeq();
        assertEquals("[]", seq0.toString());

        // WHEN head = tail
        Seq<String> seq1 = makeSeq(new String[]{"A"});
        assertEquals("[A]", seq1.toString());

        // WHEN only items are head and tail
        Seq<String> seq2 = makeSeq(new String[]{"A", "B"});
        assertEquals("[A, B]", seq2.toString());

        // WHEN there are at least 3 nodes
        Seq<String> seq3 = makeSeq(new String[]{"A", "B", "C"});
        assertEquals("[A, B, C]", seq3.toString());

        // WHEN values are not strings
        Seq<Integer> intSeq = makeSeqOfLength(5);
        assertEquals("[0, 1, 2, 3, 4]", intSeq.toString());
    }

    @DisplayName("GIVEN a Seq, WHEN an element is prepended, " +
            "THEN it is added to the beginning of the Seq.")
    @Test
    void testPrependToString() {
        Seq<Integer> seq = makeSeq();
        seq.prepend(1);
        assertEquals("[1]", seq.toString());
        seq.prepend(2);
        assertEquals("[2, 1]", seq.toString());
        seq.prepend(3);
        assertEquals("[3, 2, 1]", seq.toString());
    }

    // TODOs 1-5: Add new test cases here for the methods of `Seq`.  To save typing, you may
    // combine multiple tests for the _same_ method in the same @Test procedure, but be sure
    // that each test case is visibly distinct (comments are good for this, as demonstrated above).
    // You are welcome to compare against an expected `toString()` output in order to check
    // multiple aspects of the state at once (in general, later tests may make use of methods that
    // have previously been tested). Each test procedure must describe its scenario using
    // @DisplayName. As you write each testcase, run it via `ArraySeqTest` to see whether our
    // `ArraySeq` passes your test.  Note that there are TWO BUGS in the `ArraySeq` release code.
    // Ensure that your testcases catch these bugs, then fix them and respond to reflection
    // question 1 in "reflection.txt".

    @DisplayName("Testing the append() function")
    @Test
    void testAppend() {
        //Case One: Appending to an empty sequence
        Seq<Integer> seq1 = makeSeq();
        seq1.append(10); //Appending an element
        assertEquals("[10]", seq1.toString());
        assertEquals(1, seq1.size());  // Check updated size
        assertEquals(10, seq1.get(0)); // Check correct placement of element

        // Case Two: Appending to a sequence with one element
        Seq<Integer> seq2 = makeSeq(new Integer[]{5}); // Creating a sequence with one element
        seq2.append(15);  // Appending an element
        assertEquals("[5, 15]", seq2.toString()); // Check the correct string representation of
        // the sequence
        assertEquals(2, seq2.size());  // Size should be 2 now
        assertEquals(5, seq2.get(0));  // The first element should still be 5
        assertEquals(15, seq2.get(1));  // The second element should now be 15

        // Case Three: Appending to a longer sequence and testing all accessor methods
        Seq<Integer> seq3 = makeSeq(new Integer[]{1, 2, 3, 4});  // Creating a sequence with
        // 4 elements
        seq3.append(5);  // Appending an element
        assertEquals("[1, 2, 3, 4, 5]", seq3.toString());  // Check the correct
        // string representation
        assertEquals(5, seq3.size());  // Size should now be 5
        assertEquals(1, seq3.get(0));  // The first element should be 1
        assertEquals(2, seq3.get(1));  // The second element should be 2
        assertEquals(3, seq3.get(2));  // The third element should be 3
        assertEquals(4, seq3.get(3));  // The fourth element should be 4
        assertEquals(5, seq3.get(4));  // The fifth element should now be 5

        //Case Four: Appending to a sequence, triggering resizing DOES THIS MAKE SENSE?
        Seq<Integer> seq4 = makeSeq(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        int initialSize = seq4.size();  // Capture the initial size before appending

        // Ensure we know the size before append
        assertEquals(10, initialSize);

        seq4.append(11); // This should trigger resizing

        // Check the updated size
        assertEquals(initialSize + 1, seq4.size());  // Size should now be initialSize + 1
        assertEquals(11, seq4.get(10)); // Check the last element after resizing

        // Check the toString method to verify the sequence string representation
        assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]", seq4.toString());

        // Check the sequence behavior by getting the last element after resizing
        assertEquals(11, seq4.get(10)); // Ensure the last element is correct after resizing

        // After resizing, the size should have increased and the new element should be at the end.
        assertTrue(seq4.size() > initialSize);  // The sequence should have expanded in size.

        // Optionally, you can check if the last element is indeed the 11th element
        // since after resizing, it should not throw an error and maintain the expected behavior.
    }

    @DisplayName("Testing the get() function")
    @Test
    void testGet() {
        // Case One: Getting the first element
        Seq<Integer> seq1 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        assertEquals(10, seq1.get(0)); // Check the first element

        //Case Two: Getting the last element
        assertEquals(50, seq1.get(4));

        //Case Three: Getting an element in the middle
        assertEquals(30, seq1.get(2));
    }

    @DisplayName("Testing the contains() function")
    @Test
    void testContains() {
        // Case One: Element is in the sequence (exactly once)
        Seq<Integer> seq1 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        assertTrue(seq1.contains(30)); // Element 30 is present in the sequence

        // Case Two: Element is not in the sequence
        assertFalse(seq1.contains(60)); // Element 60 is not in the sequence

        // Case Three: Element appears multiple times
        Seq<Integer> seq2 = makeSeq(new Integer[]{10, 10, 30, 40, 50});
        assertTrue(seq2.contains(10)); // Element 10 is present multiple times
        // (should still return true)
    }

    @DisplayName("Testing the insertBefore() function")
    @Test
    void testInsertBefore() {
        // Case One: Inserting before an element in the middle
        Seq<Integer> seq1 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        seq1.insertBefore(25, 30);
        assertEquals("[10, 20, 25, 30, 40, 50]", seq1.toString());
        assertEquals(6, seq1.size()); // Ensure size increased
        assertEquals(25, seq1.get(2)); // Ensure correct placement

        // Case Two: Inserting before the first element
        Seq<Integer> seq2 = makeSeq(new Integer[]{10, 20});
        seq2.insertBefore(5, 10);
        assertEquals("[5, 10, 20]", seq2.toString());
        assertEquals(3, seq2.size()); // Ensure size increased
        assertEquals(5, seq2.get(0)); // Ensure 5 is at the start

        // Case Three: Inserting before the last element
        Seq<Integer> seq3 = makeSeq(new Integer[]{30});
        seq3.insertBefore(25, 30);
        assertEquals("[25, 30]", seq3.toString());
        assertEquals(2, seq3.size()); // Ensure size increased
        assertEquals(25, seq3.get(0)); // Ensure 25 is correctly inserted

        // Case Five: Using a loop to test scalability with different list sizes**
        for (int size = 1; size <= 20; size++) {  // Testing sizes from 1 to 20
            Integer[] values = new Integer[size];
            for (int i = 0; i < size; i++) {
                values[i] = i + 1;
            }
            Seq<Integer> seq = makeSeq(values);
            if (size > 1) {
                seq.insertBefore(99, values[size - 1]);
            } else {
                seq.insertBefore(99, values[0]);
            }
            assertEquals(size + 1, seq.size()); // Ensure size increased
            assertEquals(99, seq.get(size - 1)); // Ensure 99 is correctly inserted
        }

        // Case 6: insertBefore triggers resizing when the sequence is full
        Seq<Integer> seq8 = makeSeq(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        int initialSize = seq8.size();  // Capture the initial size before inserting
        // Ensure we know the size before inserting
        assertEquals(10, initialSize);
        // Insert before element 10 (which exists in the list)
        seq8.insertBefore(99, 10); // This should trigger resizing if the capacity is full
        // Check that size has increased
        assertEquals(initialSize + 1, seq8.size());
        // Verify the inserted element is in the correct position
        assertEquals(99, seq8.get(9)); // Since it should be before 10
        // Check the last element is still correct
        assertEquals(10, seq8.get(10));
        // Check the sequence string representation
        assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 10]", seq8.toString());
        // Confirm that resizing actually happened
        assertTrue(seq8.size() > initialSize);
    }

    @DisplayName("Testing the remove() method")
    @Test
    void testRemove() {
        // Case 1: Remove from the middle
        Seq<Integer> seq1 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        seq1.remove(30);
        assertEquals("[10, 20, 40, 50]", seq1.toString());
        assertEquals(4, seq1.size()); // Ensure size decreased
        assertEquals(40, seq1.get(2)); // Ensure 40 shifted left

        // Case 2: Remove the first element
        Seq<Integer> seq2 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        seq2.remove(10);
        assertEquals("[20, 30, 40, 50]", seq2.toString());
        assertEquals(4, seq2.size()); // Ensure size decreased
        assertEquals(20, seq2.get(0)); // Ensure new first element

        // Case 3: Remove the last element
        Seq<Integer> seq3 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        seq3.remove(50);
        assertEquals("[10, 20, 30, 40]", seq3.toString());
        assertEquals(4, seq3.size()); // Ensure size decreased
        assertEquals(40, seq3.get(3)); // Ensure new last element

        // Case 4: Remove the only element in a single-element list
        Seq<Integer> seq4 = makeSeq(new Integer[]{10});
        seq4.remove(10);
        assertEquals("[]", seq4.toString());
        assertEquals(0, seq4.size()); // Ensure list is empty

        // Case 5: Attempt to remove an element not in the list (should remain unchanged)
        Seq<Integer> seq5 = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        assertFalse(seq5.remove(99));
        assertEquals("[10, 20, 30, 40, 50]", seq5.toString());
        assertEquals(5, seq5.size()); // Ensure size remains the same

        // Case 6: Attempt to remove an element from an empty sequence (should return false)
        Seq<Integer> seq6 = makeSeq(new Integer[]{});
        assertFalse(seq6.remove(10)); // Should return false
        assertEquals("[]", seq6.toString()); // Ensure sequence remains empty
        assertEquals(0, seq6.size()); // Ensure size remains 0
    }

    @Nested
    @DisplayName("GIVEN two distinct Seqs containing the same elements in the same order,"
            + "then, they should be compared equal...")
    class EqualsTrueTest {

        @Test
        @DisplayName("when both are empty")
        void testEqualsTrue0() {
            // In these tests, you might be wondering why we are using the assertTrue(a.equals(b))
            // pattern instead of assertEquals(a,b). It's true that both of these make the same
            // assertion (and likely IntelliJ will offer assertEquals as a simplification. However,
            // philosophically, asserting equality goes against what we are testing. We're trying
            // to verify the functionality of the equals() method, meaning we want to know
            // whether it is returning true/false in the correct scenarios. When we use
            // assertEquals, it's because we want to make sure that its second argument
            // (the computed value) is equal to the expected value, and that's not true in these
            // tests.
            assertTrue(makeSeq().equals(makeSeq()));
        }

        @Test
        @DisplayName("when both sequences contain one element")
        void testEqualsTrue1() {
            Seq<Integer> seq1 = makeSeqOfLength(1);
            Seq<Integer> seq2 = makeSeq();
            seq2.append(0);
            assertTrue(seq1.equals(seq2));
            assertTrue(seq2.equals(seq1));
        }

        @Test
        @DisplayName("when both sequences contain two elements")
        void testEqualsTrue2() {
            Seq<Integer> seq1 = makeSeqOfLength(2);
            Seq<Integer> seq2 = makeSeq();
            seq2.append(0);
            seq2.append(1);
            assertTrue(seq1.equals(seq2));
            assertTrue(seq2.equals(seq1));
        }

        @Test
        @DisplayName("when both sequences contain three elements")
        void testEqualsTrue3() {
            Seq<Integer> seq1 = makeSeqOfLength(3);
            Seq<Integer> seq2 = makeSeq();
            seq2.append(0);
            seq2.append(1);
            seq2.append(2);
            assertTrue(seq1.equals(seq2));
            assertTrue(seq2.equals(seq1));
        }
    }

    @Nested
    @DisplayName("GIVEN two distinct Seqs, then, they should not be compared equal when...")
    class EqualsFalseTest {

        @Test
        @DisplayName("the second is null")
        void testEqualsFalseNull() {
            assertFalse(makeSeq().equals(null));
        }

        @Test
        @DisplayName("they have the same length but contain different elements")
        void testEqualsFalseDistinct() {
            assertFalse(makeSeq(new String[]{"A"}).equals(makeSeq(new String[]{"B"})));
            assertFalse(makeSeq(new String[]{"1"}).equals(makeSeqOfLength(1)));
            assertFalse(makeSeq(new String[]{"A", "B"}).equals(makeSeq(new String[]{"A", "C"})));
            assertFalse(makeSeq(new String[]{"A", "C"}).equals(makeSeq(new String[]{"B", "C"})));
        }

        @Test
        @DisplayName("the argument sequence contains the target sequence as a prefix")
        void testEqualsFalseLonger() {
            assertFalse(makeSeqOfLength(1).equals(makeSeqOfLength(2)));
            assertFalse(makeSeqOfLength(2).equals(makeSeqOfLength(3)));
        }

        @Test
        @DisplayName("the target sequence contains the argument sequence as a prefix")
        void testEqualsFalseShorter() {
            assertFalse(makeSeqOfLength(2).equals(makeSeqOfLength(1)));
            assertFalse(makeSeqOfLength(3).equals(makeSeqOfLength(2)));
        }
    }

    @DisplayName("GIVEN two distinct LinkedSeqs containing equivalent values in the same order, " +
            "THEN their hash codes should be the same.")
    @Test
    void testHashCode() {
        // WHEN empty
        assertEquals(makeSeq().hashCode(), makeSeq().hashCode());

        // WHEN head and tail are the same
        assertEquals(makeSeqOfLength(1).hashCode(), makeSeqOfLength(1).hashCode());

        // WHEN there are no nodes between head and tail
        assertEquals(makeSeqOfLength(2).hashCode(), makeSeqOfLength(2).hashCode());

        // WHEN there are at least 3 nodes
        assertEquals(makeSeqOfLength(3).hashCode(), makeSeqOfLength(3).hashCode());
    }

    @DisplayName("Testing the seq iterator")
    @Test
    void testSeqIterator() {
        // Case 1: Iterator on an empty sequence
        Seq<Integer> emptySeq = makeSeq(new Integer[]{});
        Iterator<Integer> emptyIterator = emptySeq.iterator();
        assertFalse(emptyIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> emptyIterator.next());

        // Case 2: Iterator on a single-element sequence
        Seq<Integer> singleSeq = makeSeq(new Integer[]{42});
        Iterator<Integer> singleIterator = singleSeq.iterator();
        assertTrue(singleIterator.hasNext());
        assertEquals(42, singleIterator.next());
        assertFalse(singleIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> singleIterator.next());

        // Case 3: Iterator on a multi-element sequence
        Seq<Integer> multiSeq = makeSeq(new Integer[]{10, 20, 30});
        Iterator<Integer> multiIterator = multiSeq.iterator();
        assertTrue(multiIterator.hasNext());
        assertEquals(10, multiIterator.next());
        assertTrue(multiIterator.hasNext());
        assertEquals(20, multiIterator.next());
        assertTrue(multiIterator.hasNext());
        assertEquals(30, multiIterator.next());
        assertFalse(multiIterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> multiIterator.next());
    }
}

class ArraySeqTest extends SeqTest {

    @Override
    <T> Seq<T> makeSeq() {
        return new ArraySeq<>();
    }
}

class DLinkedSeqTest extends SeqTest {

    @Override
    <T> Seq<T> makeSeq() {
        return new DLinkedSeq<>();
    }

    // Case 1: Remove from the middle
    @Test
    void testRemoveFromMiddle() {
        Seq<Integer> seq = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        Iterator<Integer> it = seq.iterator();
        it.next(); // 10
        it.next(); // 20
        it.next(); // 30
        it.remove(); // Remove 30
        assertEquals("[10, 20, 40, 50]", seq.toString()); // Verify sequence after removal
        assertEquals(4, seq.size()); // Ensure size decreased
        assertEquals(40, seq.get(2)); // Ensure 40 shifted left
    }

    // Case 2: Remove the first element
    @Test
    void testRemoveFirstElement() {
        Seq<Integer> seq = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        Iterator<Integer> it = seq.iterator();
        it.next(); // 10
        it.remove(); // Remove 10
        assertEquals("[20, 30, 40, 50]", seq.toString()); // Verify sequence after removal
        assertEquals(4, seq.size()); // Ensure size decreased
        assertEquals(20, seq.get(0)); // Ensure new first element
    }

    // Case 3: Remove the last element
    @Test
    void testRemoveLastElement() {
        Seq<Integer> seq = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        Iterator<Integer> it = seq.iterator();
        it.next(); // 10
        it.next(); // 20
        it.next(); // 30
        it.next(); // 40
        it.next(); // 50
        it.remove(); // Remove 50
        assertEquals("[10, 20, 30, 40]", seq.toString()); // Verify sequence after removal
        assertEquals(4, seq.size()); // Ensure size decreased
        assertEquals(40, seq.get(3)); // Ensure new last element
    }

    // Case 4: Remove the only element in a single-element list
    @Test
    void testRemoveOnlyElement() {
        Seq<Integer> seq = makeSeq(new Integer[]{10});
        Iterator<Integer> it = seq.iterator();
        it.next(); // 10
        it.remove(); // Remove 10
        assertEquals("[]", seq.toString()); // Verify sequence after removal
        assertEquals(0, seq.size()); // Ensure list is empty
    }

    // Case 5: Attempt to remove without calling next() (should throw IllegalStateException)
    @Test
    void testRemoveWithoutNext() {
        Seq<Integer> seq = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        Iterator<Integer> it = seq.iterator();
        assertThrows(IllegalStateException.class, it::remove); // Should throw
        // IllegalStateException
    }

    // Case 6: Attempt to remove twice in a row (should throw IllegalStateException)
    @Test
    void testConsecutiveRemove() {
        Seq<Integer> seq = makeSeq(new Integer[]{10, 20, 30, 40, 50});
        Iterator<Integer> it = seq.iterator();
        it.next(); // 10
        it.remove(); // Remove 10
        assertThrows(IllegalStateException.class, it::remove); // Attempt to remove again
    }

    // Case 7: Attempt to remove from an empty sequence (should throw IllegalStateException)
    @Test
    void testRemoveFromEmptySequence() {
        Seq<Integer> seq = makeSeq(new Integer[]{});
        Iterator<Integer> it = seq.iterator();
        assertThrows(IllegalStateException.class, it::remove); // Should throw
        // IllegalStateException
    }

    // TODO (challenge extension): Add tests for iterator's fail-fast behavior (by only adding them
    //  to DLinkedSeqTest, your ArraySeqTest suite will stay green).
}
