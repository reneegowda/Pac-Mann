package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

abstract class IndexerTest {

    abstract Indexer makeIndexer();

    private List<WordOccurrences> handoutIndex() {
        List<WordOccurrences> index = new LinkedList<>();
        WordOccurrences wo = new WordOccurrences("AVOCADO");
        wo.addOccurrence("aldi.txt", 1);
        wo.addOccurrence("tops.txt", 2);
        wo.addOccurrence("wegmans.txt", 3);
        index.add(wo);
        wo = new WordOccurrences("BANANA");
        wo.addOccurrence("aldi.txt", 1);
        wo.addOccurrence("aldi.txt", 2);
        wo.addOccurrence("aldi.txt", 3);
        wo.addOccurrence("wegmans.txt", 1);
        index.add(wo);
        return index;
    }

    @DisplayName("WHEN the indexer is passed the names of the three input files from the assignment"
            + " handout THEN it produces the correct index")
    @Test
    void testIndexCreationHandoutExample() throws IOException {
        Indexer indexer = makeIndexer();
        // Note: these are sorted and distinct to satisfy the preconditions of `index()`.
        Iterable<WordOccurrences> index = indexer.index(
                List.of("aldi.txt", "tops.txt", "wegmans.txt"));

        List<WordOccurrences> expected = handoutIndex();
        assertIterableEquals(expected, index);
    }

    @DisplayName("WHEN the indexer is given an index THEN it prints output in the correct format "
            + "to the provided PrintWriter.")
    @Test
    void testWriteIndexHandoutExample() {
        List<WordOccurrences> index = handoutIndex();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Indexer indexer = makeIndexer();
        indexer.writeIndex(index, pw);
        pw.close();
        // Replace any Windows line separators with Unix ones (note that text blocks always produce
        //  Unix line separators)
        String actual = sw.toString().replace("\r\n", "\n");

        String expected = """
                AVOCADO
                \taldi.txt 1
                \ttops.txt 2
                \twegmans.txt 3
                BANANA
                \taldi.txt 1 2 3
                \twegmans.txt 1
                """;
        assertEquals(expected, actual);
    }

}

class JavaDictIndexerTest extends IndexerTest {

    @Override
    Indexer makeIndexer() {
        return new JavaDictIndexer();
    }
}

class ProbingDictIndexerTest extends IndexerTest {

    @Override
    Indexer makeIndexer() {
        return new ProbingDictIndexer();
    }
}

class TwoPassIndexerTest extends IndexerTest {

    @Override
    Indexer makeIndexer() {
        return new TwoPassIndexer();
    }
}
