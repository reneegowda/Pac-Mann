package cs2110;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An Indexer that uses Java's `HashMap`, wrapped in a `JavaStringDict`, to associate words with
 * their occurrences.  It also uses Java's `ArrayList`, wrapped in a `JavaIndexedSeq`, when a
 * sequence is required.
 */
public class JavaDictIndexer extends DictIndexer {

    /**
     * Return a `JavaStringDict` wrapping a `HashMap`.
     */
    @Override
    protected <V> JavaStringDict<V> makeStringDict() {
        return new JavaStringDict<>(new HashMap<>());
    }

    /**
     * Return a `JavaIndexedSeq` wrapping an `ArrayList`.
     */
    @Override
    protected <T> JavaIndexedSeq<T> makeIndexedSeq() {
        return new JavaIndexedSeq<>(new ArrayList<>());
    }
}
