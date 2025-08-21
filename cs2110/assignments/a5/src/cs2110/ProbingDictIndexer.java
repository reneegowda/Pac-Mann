package cs2110;

/**
 * An Indexer that uses a `ProbingStringDict` to associate words with their occurrences.  It also
 * uses `DynamicArrayIndexedSeq` when a sequence is required.
 */
public class ProbingDictIndexer extends DictIndexer {

    /**
     * Create a `ProbingStringDict`.
     */
    @Override
    protected <V> ProbingStringDict<V> makeStringDict() {
        return new ProbingStringDict<>();
    }

    /**
     * Create a `DynamicArrayIndexedSeq`.
     */
    @Override
    protected <T> DynamicArrayIndexedSeq<T> makeIndexedSeq() {
        return new DynamicArrayIndexedSeq<>();
    }
}
