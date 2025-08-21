package cs2110;

/**
 * Keeps track of which line numbers of which sources (i.e., files) contain a certain word.  All
 * occurrences for a given source must be recorded before recording any occurrences in another
 * source.
 * <p>
 * The natural ordering of `WordOccurrences` is the same as the natural ordering of their words.
 * This is inconsistent with `equals()`.
 */
public class WordOccurrences implements Comparable<WordOccurrences> {

    /**
     * The word we are tracking occurrences of.
     */
    private final String word;

    /**
     * The sources this word is known to occur in, and the line numbers in each it is known to occur
     * on.  Order corresponds to the order in which source occurrences were recorded, with the last
     * element (if any) corresponding to the most recently added occurrence.
     */
    private final IndexedSeq<SourceLines> sources;


    /**
     * Create an object to track occurrences of the word `word`.  Initially has no occurrences.
     */
    public WordOccurrences(String word) {
        this.word = word;
        sources = new DynamicArrayIndexedSeq<>();
    }

    /**
     * Return the word we are tracking occurrences of.
     */
    public String word() {
        return word;
    }

    /**
     * Return the sources (associated with their matching line numbers) that contain our word, in
     * the order the sources were recorded in.  Clients must not attempt to mutate the returned
     * object.
     */
    public Iterable<SourceLines> sources() {
        // Danger: rep exposure
        return sources;
    }

    /**
     * Record that line `lineNumber` of source `sourceName` contains our word.  Returns whether this
     * line of the source was already known to contain our word.  Requires `sourceName` is either
     * the same as the last added source name or has never been added, and lineNumber is not less
     * than last line number added with this source name (if any).
     */
    public boolean addOccurrence(String sourceName, int lineNumber) {
        if (sources.isEmpty() || !sources.getLast().sourceName().equals(sourceName)) {
            sources.add(new SourceLines(sourceName));
        }
        return sources.getLast().addLine(lineNumber);
    }

    @Override
    public int compareTo(WordOccurrences other) {
        // Note: This is "inconsistent with equals" as it will return 0 for two WordOccurrences with
        //  the same word, even when `equals()` evaluates to false.
        return word().compareTo(other.word());
    }

    @Override
    public String toString() {
        return "WordOccurrences[word=" + word() + ", sources=" + sources() + "]";
    }

    @Override
    public boolean equals(Object other) {
        // We are overriding `equals()` for testing convenience, even though this class is mutable.
        if (!(other instanceof WordOccurrences)) {
            return false;
        }
        WordOccurrences wo = (WordOccurrences) other;
        if (!word.equals(wo.word)) {
            return false;
        }
        return IndexedSeq.equals(sources, wo.sources);
    }

    @Override
    public int hashCode() {
        // Reminder: Must override hashCode() if overriding equals()
        return java.util.Objects.hash(word, IndexedSeq.hashCode(sources));
    }
}
