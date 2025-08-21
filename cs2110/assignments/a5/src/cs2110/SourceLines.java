package cs2110;

/**
 * Keeps track of which line numbers in a source meet some criteria (determined by the client). Line
 * numbers must be recorded in ascending order.
 * <p>
 * The natural ordering of `SourceLines` is the same as the natural ordering of their source names.
 * This is inconsistent with `equals()`.
 */
public class SourceLines implements Comparable<SourceLines> {

    /**
     * The name of our source.
     */
    private final String sourceName;

    /**
     * The line numbers in our source known to meet the client's criteria, in ascending order.
     */
    private final IndexedSeq<Integer> lines;

    /**
     * Create an object to track which line numbers in a source with name `name` meet some criteria.
     * Initially no lines are known to meet the criteria.
     */
    public SourceLines(String sourceName) {
        this.sourceName = sourceName;
        lines = new DynamicArrayIndexedSeq<>();
    }

    /**
     * Return the name of our source.
     */
    public String sourceName() {
        return sourceName;
    }

    /**
     * Return the unique line numbers in our source that meet the criteria, in ascending order.
     * Clients must not attempt to mutate the returned object.
     */
    public Iterable<Integer> lines() {
        // Danger: rep exposure
        return lines;
    }

    /**
     * Record the fact that `lineNumber` in our source meets the criteria.  Returns whether the line
     * was already known to meet the criteria.  Requires `lineNumber` is not less than previously
     * added line number (if any).
     */
    public boolean addLine(int lineNumber) {
        if (lines.isEmpty() || lineNumber != lines.getLast()) {
            lines.add(lineNumber);
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(SourceLines other) {
        // Note: This is "inconsistent with equals" as it will return 0 for two SourceLines with
        //  the same source name, even when `equals()` evaluates to false.
        return sourceName().compareTo(other.sourceName());
    }

    @Override
    public String toString() {
        return "SourceLines[sourceName=" + sourceName() + ", lines=" + lines() + "]";
    }

    @Override
    public boolean equals(Object other) {
        // We are overriding `equals()` for testing convenience, even though this class is mutable.
        if (!(other instanceof SourceLines)) {
            return false;
        }
        SourceLines sl = (SourceLines) other;
        if (!sourceName.equals(sl.sourceName)) {
            return false;
        }
        return IndexedSeq.equals(lines, sl.lines);
    }

    @Override
    public int hashCode() {
        // Reminder: Must override hashCode() if overriding equals()
        return java.util.Objects.hash(sourceName, IndexedSeq.hashCode(lines));
    }
}
