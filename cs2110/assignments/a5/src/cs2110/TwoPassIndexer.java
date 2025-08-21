package cs2110;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * An Indexer that uses a sorted array plus binary search to serve as a string map.  Each source is
 * read twice: the first pass is used to assemble the unique keys, while the second pass is used to
 * accumulate the word occurrences.
 */
public class TwoPassIndexer extends Indexer {

    @Override
    public Iterable<WordOccurrences> index(Iterable<String> sortedSrcNames) throws IOException {
        // TODO 18: Implement this method as specified.  Must create an `IndexedSeq` of words (using
        //  `makeIndexedSeq()`), populate it with all words from all sources, and sort+deduplicate
        //  it.  Then must create a corresponding `IndexedSeq` of word occurrences, using binary
        //  search to update the appropriate one as each word is re-read from the sources.  You may
        //  only use methods declared in `IndexedSeq`, `WordOccurrences`, `Scanner`/`WordScanner`,
        //  `String`, `FileReader`, and `Iterable`/`Iterator`, as well as `Sorting.binarySearch()`.
        IndexedSeq<String> wordStrings = makeIndexedSeq();

        for (String name : sortedSrcNames){
            try{ Scanner scanner = new Scanner(new FileReader(name));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        if (!word.isEmpty()){
                            wordStrings.add((word.toUpperCase()));
                        }
                    }
                }
            } catch (IOException e){
                throw new IOException();
            }
        }

        wordStrings.sortDistinct();

        IndexedSeq<WordOccurrences> ans = makeIndexedSeq();

        for (String word : wordStrings){
            ans.add(new WordOccurrences(word));
        }

        for (String name : sortedSrcNames){
            int lineNumber = 0;
            try{ Scanner scanner = new Scanner(new FileReader(name));
                while (scanner.hasNextLine()) {
                    lineNumber++;
                    String line = scanner.nextLine();
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        if (!word.isEmpty()){
                            int position = Sorting.binarySearch(wordStrings, word.toUpperCase());
                            ans.get(position).addOccurrence(name,lineNumber);
                        }
                    }
                }
            } catch (IOException e){
                throw new IOException();
            }
        }
        return ans;
    }

    /**
     * Create a `DynamicArrayIndexedSeq`.
     */
    @Override
    protected <T> DynamicArrayIndexedSeq<T> makeIndexedSeq() {
        return new DynamicArrayIndexedSeq<>();
    }
}
