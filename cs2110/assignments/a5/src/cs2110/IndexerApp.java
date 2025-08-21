package cs2110;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Indexes words found in text files specified via program arguments, printing the index to
 * `System.out`.  Implemented using the `Indexer` interface.
 */
public class IndexerApp {

    /**
     * Print an index of the words found in the files whose paths are provided in `args`.  The index
     * keys are the distinct words, after converting to upper case, in lexicographic order.  The
     * index entries are the paths to the files in which that word is found, deduplicated (by the
     * exact path provided in `args`) and in lexicographic order, each followed by the list of
     * distinct line numbers on which that word occurs (separated by spaces).  Each entry line is
     * indented with a tab character.
     */
    public static void main(String[] args) throws IOException {
        PrintWriter out = new PrintWriter(System.out);

        // Replace this with the `Indexer` implementation you want to end-to-end test.
        Indexer indexer = new JavaDictIndexer();
        indexer.writeSourcesIndex(Arrays.asList(args), out);
        out.flush();
    }
}
