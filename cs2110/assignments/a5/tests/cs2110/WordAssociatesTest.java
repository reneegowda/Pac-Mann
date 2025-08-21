package cs2110;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class WordAssociatesTest {

    private Set<String> toSet(Iterable<String> iterable) {
        Set<String> result = new HashSet<>();
        for (String s : iterable) result.add(s);
        return result;
    }

    @Test
    public void testWordNotInIndex() {
        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file1", 1);
        List<WordOccurrences> index = List.of(banana);

        Iterable<String> associated = WordAssociates.associatedWords(index, "APPLE", 1);
        assertTrue(toSet(associated).isEmpty());
    }

    @Test
    public void testNoLineOverlap() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);

        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file1", 2);

        List<WordOccurrences> index = List.of(apple, banana);

        Iterable<String> associated = WordAssociates.associatedWords(index, "apple", 1);
        assertTrue(toSet(associated).isEmpty());
    }

    @Test
    public void testMultipleMatchesSameLine() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);

        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file1", 1);

        WordOccurrences cherry = new WordOccurrences("CHERRY");
        cherry.addOccurrence("file1", 1);

        List<WordOccurrences> index = List.of(apple, banana, cherry);

        Set<String> expected = Set.of("BANANA", "CHERRY");
        Set<String> actual = toSet(WordAssociates.associatedWords(index, "apple", 1));

        assertEquals(expected, actual);
    }

    @Test
    public void testThresholdTooHigh() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);
        apple.addOccurrence("file1", 2);

        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file1", 1);

        List<WordOccurrences> index = List.of(apple, banana);

        Iterable<String> associated = WordAssociates.associatedWords(index, "apple", 2);
        assertTrue(toSet(associated).isEmpty());
    }

    @Test
    public void testSelfMatchingIsSkipped() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);
        apple.addOccurrence("file1", 2);

        List<WordOccurrences> index = List.of(apple);

        Iterable<String> associated = WordAssociates.associatedWords(index, "apple", 1);
        assertTrue(toSet(associated).isEmpty());
    }

    @Test
    public void testDifferentFilesSameLineNumber() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);

        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file2", 1);  // Same line number, different file

        List<WordOccurrences> index = List.of(apple, banana);

        Iterable<String> associated = WordAssociates.associatedWords(index, "apple", 1);
        assertTrue(toSet(associated).isEmpty());
    }

    @Test
    public void testMultipleOverlapsSameSource() {
        WordOccurrences apple = new WordOccurrences("APPLE");
        apple.addOccurrence("file1", 1);
        apple.addOccurrence("file1", 2);

        WordOccurrences banana = new WordOccurrences("BANANA");
        banana.addOccurrence("file1", 1);
        banana.addOccurrence("file1", 2);

        List<WordOccurrences> index = List.of(apple, banana);

        Iterable<String> associated = WordAssociates.associatedWords(index, "apple", 2);
        Set<String> expected = Set.of("BANANA");
        assertEquals(expected, toSet(associated));
    }

    @Test
    public void testChefsExampleFromMenus() {
        WordOccurrences chefs = new WordOccurrences("CHEF'S");
        chefs.addOccurrence("file1", 1);
        chefs.addOccurrence("file2", 2);
        chefs.addOccurrence("file3", 3);
        chefs.addOccurrence("file4", 4);

        WordOccurrences choice = new WordOccurrences("CHOICE");
        choice.addOccurrence("file1", 1);
        choice.addOccurrence("file2", 2);
        choice.addOccurrence("file3", 3);

        WordOccurrences sides = new WordOccurrences("SIDES");
        sides.addOccurrence("file1", 1);
        sides.addOccurrence("file2", 2);
        sides.addOccurrence("file3", 3);

        WordOccurrences dash = new WordOccurrences("-");
        dash.addOccurrence("file1", 1);
        dash.addOccurrence("file2", 2);
        dash.addOccurrence("file3", 3);

        WordOccurrences table = new WordOccurrences("TABLE");
        table.addOccurrence("file2", 2);
        table.addOccurrence("file3", 3);
        table.addOccurrence("file4", 4);

        WordOccurrences notEnough = new WordOccurrences("SOUP");
        notEnough.addOccurrence("file1", 1);
        notEnough.addOccurrence("file2", 2);  // Only 2 overlaps

        List<WordOccurrences> index = List.of(chefs, choice, sides, dash, table, notEnough);

        Set<String> expected = Set.of("CHOICE", "SIDES", "-", "TABLE");
        Set<String> actual = toSet(WordAssociates.associatedWords(index, "CHEF'S", 3));

        assertEquals(expected, actual);
    }
}