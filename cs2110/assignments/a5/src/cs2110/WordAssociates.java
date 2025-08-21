package cs2110;


public class WordAssociates {


    /**
     * Helper function which generates the lines target word appears at.
     */


    private static DynamicArrayIndexedSeq<SourceLines> lineNumbers (Iterable<WordOccurrences> index, String word) {


        DynamicArrayIndexedSeq<SourceLines> ans = new DynamicArrayIndexedSeq<>();
        WordOccurrences wordNeeded = null;
        boolean found = true;


        while (wordNeeded == null && found) {
            for (WordOccurrences someWord : index) {
                if (someWord.word().equals(word.toUpperCase())) {
                    wordNeeded = someWord;
                }
            }
            found = false;
        }


        if (wordNeeded == null){
            return ans;
        }
        for(SourceLines another : wordNeeded.sources()){
            ans.add(another);
        }
        return ans;
    }


    /**
     * Helper function which takes the word and checks to see number of Source Lines it matches.
     */


    private static int matches
    (DynamicArrayIndexedSeq<SourceLines> sources, WordOccurrences potentialMatch){
        int ans = 0;
        for (SourceLines source : potentialMatch.sources()){
            for (SourceLines ogAppearences : sources){
                if (source.sourceName().equals(ogAppearences.sourceName())){
                    for (int potentialNumber : source.lines()){
                        for (int actualNumber : ogAppearences.lines()){
                            if (potentialNumber == actualNumber){
                                ans++;
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }


    /**
     * Return which words are found on the same line (in the same source) as the
     * word `word` at least `threshold` times across the sources indexed in `index`.
     */
    static Iterable<String> associatedWords(Iterable<WordOccurrences> index, String word, int threshold){
        assert word != null;
        DynamicArrayIndexedSeq<String> something = new DynamicArrayIndexedSeq<>();
        DynamicArrayIndexedSeq<SourceLines> matchAgainstLines = lineNumbers(index, word);


        for (WordOccurrences occur : index) {
            if (!occur.word().toUpperCase().equals(word.toUpperCase())) {
                int possibleMatch = matches(matchAgainstLines, occur);
                if (possibleMatch >= threshold) {
                    something.add(occur.word());
                }
            }
        }
        return something;
    }
}