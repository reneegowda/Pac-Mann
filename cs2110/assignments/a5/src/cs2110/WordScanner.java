package cs2110;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Iterate over the whitespace-separated words in an input source, returning the (1-based) line
 * number along with each word.  Whitespace and newline definitions follow those of `Scanner`.
 */
public class WordScanner implements Closeable, Iterator<WordScanner.WordLine> {

    /**
     * Bundles a word with the line number it was found on.
     */
    public record WordLine(String word, int lineNumber) {
        // A "record" class will automatically generate a constructor `WordLine(word, lineNumber)`
        //  and accessors `word()` and `lineNumber()`, as well as overrides for `toString()`,
        //  `equals()`, and `hashCode()`.  It is otherwise just a nested, static (not inner) class.
    }

    /**
     * A scanner over our input source, used to read one line at a time.
     */
    private final Scanner sourceScanner;

    /**
     * The line number of the last line obtained from `sourceScanner`, or 0 if no lines have been
     * read.
     */
    private int lineCount;

    /**
     * A scanner over the last line obtained from `sourceScanner`, used to split the line into
     * words.
     */
    private Scanner lineScanner;

    /**
     * The next pair of (word, lineNumber) to return, or null if there are no words left in the
     * source.
     */
    private WordLine nextWord;


    /**
     * Create a WordScanner to iterate over the words in input source `in`.
     */
    public WordScanner(Reader in) {
        sourceScanner = new Scanner(in);
        lineCount = 0;
        if (sourceScanner.hasNextLine()) {
            lineScanner = new Scanner(sourceScanner.nextLine());
            lineCount += 1;
            findNext();
        } else {
            nextWord = null;
        }
    }

    /**
     * Find the next word in the source and assign it (and its line number) to `nextWord`, or set
     * `nextWord` to null if no words remain in the source.
     */
    private void findNext() {
        while (!lineScanner.hasNext() && sourceScanner.hasNextLine()) {
            lineScanner = new Scanner(sourceScanner.nextLine());
            lineCount += 1;
        }
        if (lineScanner.hasNext()) {
            nextWord = new WordLine(lineScanner.next(), lineCount);
        } else {
            nextWord = null;
        }
    }

    @Override
    public boolean hasNext() {
        return nextWord != null;
    }

    @Override
    public WordLine next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        WordLine ans = nextWord;
        findNext();
        return ans;
    }

    @Override
    public void close() {
        sourceScanner.close();
    }

    /**
     * Return the last IOException thrown by our source Reader, or null if no IOExceptions have been
     * thrown.  Analogous to `Scanner`.
     */
    public IOException ioException() {
        return sourceScanner.ioException();
    }
}
