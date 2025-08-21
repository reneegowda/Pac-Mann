package cs2110;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents the capability of creating an index of distinct words found across multiple input
 * sources and the line numbers they occur on.
 */
public abstract class Indexer {

    /**
     * Create an index of the words in the text sources with filenames `srcNames`, then write the
     * index to `out`.  The index keys are the distinct words, after converting to upper case, in
     * lexicographic order.  The index entries are the names of the sources in which that word is
     * found, deduplicated and in lexicographic order, each followed by the list of distinct line
     * numbers on which that word occurs (separated by spaces).  Each entry line is indented with a
     * tab character.
     */
    void writeSourcesIndex(Iterable<String> srcNames, PrintWriter out)
            throws IOException {
        // Step 1: Deduplicate and alphabetize source names
        IndexedSeq<String> sortedSrcNames = makeIndexedSeq();
        for (String name : srcNames) {
            sortedSrcNames.add(name);
        }
        sortedSrcNames.sortDistinct();

        // Step 2: Read sources and create index
        Iterable<WordOccurrences> index = index(sortedSrcNames);

        // Step 3: Write index
        writeIndex(index, out);
    }

    /**
     * Create an index of the words in the text sources with filenames `sortedSrcNames`.  Requires
     * `sortedSrcNames` is sorted and distinct.
     */
    public abstract Iterable<WordOccurrences> index(Iterable<String> sortedSrcNames)
            throws IOException;

    /**
     * Write the index `index` to `out`.  Index keys are written on their own line, in the order
     * produced by iterating over `index`.  Index entries are printed under their key, one line per
     * source, each indented with a tab character.  Source names and line numbers are separated by
     * spaces.
     */
    void writeIndex(Iterable<WordOccurrences> index, PrintWriter out) {
        //TO DO 2
        for (WordOccurrences word: index) {
            out.println(word.word());
            for (SourceLines source: word.sources()) {
                StringBuilder numbers = new StringBuilder();
                for (Integer line: source.lines()) {
                    numbers.append(" ").append(line);
                }
                out.println("\t" + source.sourceName() + numbers);
            }
        }
    }

    /**
     * Create an empty sequence of values of type `T`.  Such a sequence may be used to store and
     * sort source names and/or words (for example).
     */
    protected abstract <T> IndexedSeq<T> makeIndexedSeq();
}
