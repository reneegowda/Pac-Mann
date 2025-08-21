package cs2110;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * An indexer that takes advantage of a `StringDict`.  Subclasses determine the implementation of
 * the dictionary and list types used.
 */
public abstract class DictIndexer extends Indexer {

    /**
     * Create an empty dictionary mapping strings to values of type `V`.  Such a dictionary will be
     * used to associate words with `WordOccurrences` objects.
     */
    protected abstract <V> StringDict<V> makeStringDict();

    @Override
    public Iterable<WordOccurrences> index(Iterable<String> sortedSrcNames) throws IOException {
        //to do 3
        StringDict<WordOccurrences> dict = makeStringDict();

        for (String filename : sortedSrcNames) {
            try (Scanner scanner = new Scanner(new FileReader(filename))) {
                int lineNumber = 0;

                while (scanner.hasNextLine()) {
                    lineNumber++;
                    String line = scanner.nextLine();
                    String[] words = line.split("\\s+");

                    for (String word : words) {
                        if (!word.isEmpty()) {
                            String upperWord = word.toUpperCase();

                            if (!dict.containsKey(upperWord)) {
                                dict.put(upperWord, new WordOccurrences(upperWord));
                            }

                            WordOccurrences occurrences = dict.get(upperWord);
                            occurrences.addOccurrence(filename, lineNumber);
                        }
                    }
                }
            }
        }

        IndexedSeq<WordOccurrences> result = makeIndexedSeq();

        for (WordOccurrences occ : dict) {
            result.add(occ);
        }

        result.sortDistinct();

        return result;
    }
}
