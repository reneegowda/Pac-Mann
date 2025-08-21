package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SortingTest {

    /**
     * Create an `IndexedSeq` with `items` as its elements.
     */
    @SafeVarargs
    static <T extends Comparable<? super T>> IndexedSeq<T> makeSeq(T... items) {
        return new JavaIndexedSeq<>(new ArrayList<>(Arrays.asList(items)));
    }

    @DisplayName("deduplicateSorted()")
    @Nested
    class DeduplicateSortedTest {

        // Test case: No duplicates in the sequence
        @DisplayName("will have no effect on a seq containing no duplicate values")
        @Test
        void testNoDups() {
            // Create a sequence with no duplicates
            IndexedSeq<Integer> items = makeSeq(0, 1, 2, 3, 4);
            int originalSize = items.size();  // Store the original size of the sequence
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the size of the sequence remains the same
            assertEquals(originalSize, items.size());
            // Assert that the sequence remains unchanged
            assertIterableEquals(List.of(0, 1, 2, 3, 4), items);
        }

        // Test case: Sequence with one duplicate value
        @DisplayName("will have an effect on a seq containing one duplicate value")
        @Test
        void testOneDups() {
            // Create a sequence with one duplicate value (3 appears twice)
            IndexedSeq<Integer> items = makeSeq(0, 1, 2, 3, 3);
            int originalSize = items.size();  // Store the original size of the sequence
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the size of the sequence has decreased by 1
            assertEquals(originalSize - 1, items.size());
            // Assert that the duplicate (3) has been removed
            assertIterableEquals(List.of(0, 1, 2, 3), items);
        }

        // Test case: Sequence containing only one value
        @DisplayName("will return a seq containing one value")
        @Test
        void testOneValue() {
            // Create a sequence with a single value (0)
            IndexedSeq<Integer> items = makeSeq(0);
            int originalSize = items.size();  // Store the original size of the sequence
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the size of the sequence remains the same
            assertEquals(originalSize, items.size());
            // Assert that the sequence contains the single value (0)
            assertIterableEquals(List.of(0), items);
        }

        // Test case: Sequence with multiple duplicate values
        @DisplayName("will have an effect on a seq containing multiple duplicate values")
        @Test
        void testMultipleDuplicates() {
            // Create a sequence with multiple duplicate values (0, 1, 2 appear twice each)
            IndexedSeq<Integer> items = makeSeq(0, 0, 1, 1, 2, 2);
            int originalSize = items.size();  // Store the original size of the sequence
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the size of the sequence has decreased by 3 (because of 3 duplicates)
            assertEquals(originalSize - 3, items.size());
            // Assert that the duplicates have been removed
            assertIterableEquals(List.of(0, 1, 2), items);
        }

        // Edge Case: All elements in the sequence are the same
        @DisplayName("will reduce a seq of all the same value to one element")
        @Test
        void testAllSame() {
            // Create a sequence where all elements are the same (9 repeated)
            IndexedSeq<Integer> items = makeSeq(9, 9, 9, 9, 9);
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the sequence has only one element (9)
            assertEquals(1, items.size());
            // Assert that the sequence contains only one value (9)
            assertIterableEquals(List.of(9), items);
        }

        // Edge Case: Empty sequence
        @DisplayName("handles an empty sequence gracefully")
        @Test
        void testEmpty() {
            // Create an empty sequence
            IndexedSeq<Integer> items = makeSeq();
            Sorting.deduplicateSorted(items);  // Run deduplication

            // Assert that the size of the sequence is 0
            assertEquals(0, items.size());
            // Assert that the sequence is still empty
            assertIterableEquals(List.of(), items);
        }
    }

    // Tests of `med3()` from A1.  You are welcome to add additional testcases if you like.
    @Nested
    @DisplayName("med3()")
    class Med3Test {

        @Test
        @DisplayName("computes the median of distinct, sorted values")
        void testSorted() {
            assertEquals(2, Sorting.med3(1, 2, 3));
        }

        @Test
        @DisplayName("computes the median of distinct, unsorted values")
        void testUnsorted() {
            // Observe here how a single test case can make multiple assertions.
            assertEquals(2, Sorting.med3(2, 1, 3));
            assertEquals(2, Sorting.med3(1, 3, 2));
            assertEquals(2, Sorting.med3(3, 2, 1));
        }

        @Test
        @DisplayName("computes the median in the presence of negative numbers")
        void testNegative() {
            assertEquals(0, Sorting.med3(-3, 0, 4));
        }

        @Test
        @DisplayName("computes the median when two values are duplicates")
        void testDup2() {
            assertEquals(1, Sorting.med3(1, 2, 1));
            assertEquals(2, Sorting.med3(2, 2, 1));
        }

        @Test
        @DisplayName("computes the median when all three values are the same")
        void testDup3() {
            assertEquals(1, Sorting.med3(1, 1, 1));
        }

        @Test
        @DisplayName("computes the median in the presence of extreme values")
        void testExtreme() {
            assertEquals(2, Sorting.med3(Integer.MIN_VALUE, 2, Integer.MAX_VALUE));
            assertEquals(Integer.MAX_VALUE,
                    Sorting.med3(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
    }

    @DisplayName("partitionShiftedDiscardPivot()")
    @Nested
    class PartitionShiftedDiscardPivotTest {

        @DisplayName("Successfully shifts to the beginning of the array when pivot is present")
        @Test
        void testDstZero() {
            IndexedSeq<Character> items = makeSeq('X', 'X', 'Z', 'A', 'M');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 2, 5, 0, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(1, ij.i());
            assertEquals(4, ij.j());

            // Check elements in partitions (in this case, since only one element is in each
            // partition, we know exactly where each will be)
            assertEquals('A', items.get(0));
            assertEquals('Z', items.get(4));
            // Cannot make any claims about `items[i..j)`.
        }

        @DisplayName("Pivot is not present in the sequence")
        @Test
        void testPivotNotPresent() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'Z', 'A', 'M');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 5, 0, 'B');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(1, ij.i());
            assertEquals(1, ij.j());

            // Check elements in partitions
            assertEquals('A', items.get(0));
        }

        @DisplayName("Pivot appears multiple times in the sequence")
        @Test
        void testPivotAppearsMultipleTimes() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'M', 'Z', 'M');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 5, 0, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(0, ij.i());
            assertEquals(2, ij.j());

            // Check elements in partitions
            assertEquals('M', items.get(0));
            assertEquals('M', items.get(1));
        }

        @DisplayName("dst is equal to begin")
        @Test
        void testDstEqualBegin() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'M', 'Z', 'A');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 5, 0, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(1, ij.i());
            assertEquals(2, ij.j());

            // Check elements in partitions
            assertEquals('A', items.get(0));
            assertEquals('M', items.get(1));
        }

        @DisplayName("dst is not zero")
        @Test
        void testDstNotZero() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'M', 'Z', 'A');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 2, 5, 1, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(2, ij.i());
            assertEquals(4, ij.j());

            // Check elements in partitions
            assertEquals('X', items.get(0));
            assertEquals('A', items.get(1));
            assertEquals('Z', items.get(4));
        }

        @DisplayName("end is not items.size()")
        @Test
        void testEndNotSize() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'Z', 'M', 'A');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 4, 0, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(0, ij.i());
            assertEquals(1, ij.j());

            // Check elements in partitions
            assertEquals('A', items.get(4));
            assertEquals('M', items.get(0));
        }

        @DisplayName("No values smaller or larger than the pivot. All values are the same.")
        @Test
        void testNoSmallerOrLargerValues() {
            IndexedSeq<Character> items = makeSeq('M', 'M', 'M');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 3, 0, 'M');

            // Seq's size should not have been affected
            assertEquals(originalSize, items.size());

            // Check return value(s)
            assertEquals(0, ij.i());
            assertEquals(3, ij.j());

            // Check elements in partitions (all elements are equal to the pivot)
            assertEquals('M', items.get(0));
            assertEquals('M', items.get(1));
            assertEquals('M', items.get(2));
        }

        @DisplayName("All values smaller than the pivot, no values larger than the pivot")
        @Test
        void testSmallerThanPivotNoLarger() {
            IndexedSeq<Character> items = makeSeq('A', 'B', 'C', 'M');
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 4, 0, 'M');

            // The size of the sequence should remain the same after partitioning
            assertEquals(originalSize, items.size());

            // Check return value(s) of the partition
            assertEquals(3, ij.i());
            assertEquals(4, ij.j());

            // Check elements in partitions
            assertEquals('M', items.get(3));
        }

        @DisplayName("All values larger than the pivot, no values smaller than the pivot")
        @Test
        void testLargerThanPivotNoSmaller() {
            IndexedSeq<Character> items = makeSeq('X', 'Y', 'Z', 'M'); // 'M' is the pivot
            int originalSize = items.size();
            IPair ij = Sorting.partitionShiftedDiscardPivot(items, 0, 4, 0, 'M');

            // The size of the sequence should remain the same after partitioning
            assertEquals(originalSize, items.size());

            // Check return value(s) of the partition
            assertEquals(0, ij.i());
            assertEquals(1, ij.j());

            // Check elements in partitions
            assertEquals('M', items.get(0));
        }

        // TODO 8: Write additional testcases to thoroughly cover `partitionShiftedDiscardPivot()`.
        //  Be sure to cover the following situations:
        //  * Pivot is not present; pivot is present more than once
        //  * `dst` is equal to `begin`; `dst` is not 0
        //  * `end` is not `items.size()`
        //  * There are no values smaller/larger than the pivot
        //  A single testcase may be able to cover multiple of the above situations; use
        //  DisplayNames and/or comments to clearly indicate which situations are being covered in
        //  each case.  Be careful not to assert more than is guaranteed by the spec (any valid
        //  implementation must pass your tests).
    }

    @DisplayName("quicksortDistinct()")
    @Nested
    class SortDistinctTest {

        @DisplayName("has no effect on a seq that is already sorted and distinct")
        @Test
        void testAlreadySortedDistinct() {
            IndexedSeq<Integer> items = makeSeq(0, 1, 2, 3, 4);
            int originalSize = items.size();
            Sorting.sortDistinct(items);

            assertEquals(originalSize, items.size());
            // Use `assertIterableEquals()` rather than `assertEquals()` so that we can use Java
            //  lists for our expected value.
            assertIterableEquals(List.of(0, 1, 2, 3, 4), items);
        }

        // Reverse sorted sequence: tests correct sorting of descending inputs
        @DisplayName("correctly sorts a seq of distinct values in reverse order")
        @Test
        void testReverseSortedDistinct() {
            IndexedSeq<Integer> items = makeSeq(9, 7, 5, 3, 1);
            int originalSize = items.size();
            Sorting.sortDistinct(items);

            assertEquals(originalSize, items.size());
            assertIterableEquals(List.of(1, 3, 5, 7, 9), items);
        }

        // Randomly ordered sequence: tests sorting correctness on unsorted distinct input
        @DisplayName("correctly sorts a scrambled seq of distinct values")
        @Test
        void testUnsortedDistinct() {
            IndexedSeq<Integer> items = makeSeq(42, 13, 7, 99, 1, 73);
            int originalSize = items.size();
            Sorting.sortDistinct(items);

            assertEquals(originalSize, items.size());
            assertIterableEquals(List.of(1, 7, 13, 42, 73, 99), items);
        }

        // Empty sequence: edge case, should return immediately with no errors
        @DisplayName("correctly handles an empty sequence")
        @Test
        void testEmptySeq() {
            IndexedSeq<Integer> items = makeSeq();
            Sorting.sortDistinct(items);

            assertEquals(0, items.size());
            assertIterableEquals(List.of(), items);
        }

        // Single element: edge case, should return the same element unchanged
        @DisplayName("correctly handles a sequence with a single element")
        @Test
        void testSingleElementSeq() {
            IndexedSeq<Integer> items = makeSeq(42);
            Sorting.sortDistinct(items);

            assertEquals(1, items.size());
            assertIterableEquals(List.of(42), items);
        }

        // Ensures pivot selection works correctly when the median is in the middle
        @DisplayName("handles pivot being the middle value")
        @Test
        void testPivotMiddle() {
            IndexedSeq<Integer> items = makeSeq(7, 5, 9);
            Sorting.sortDistinct(items);

            assertEquals(3, items.size());
            assertIterableEquals(List.of(5, 7, 9), items);
        }

        // TODO 10: Write at least two additional testcases to thoroughly cover
        //  `quicksortDistinct()`.
    }
}
