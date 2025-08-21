package cs2110;


import java.io.*;
import java.util.*;


/**
 * Indexes words found in text files specified via program arguments, printing the index to
 * `System.out`.  Implemented using Java's Collections library.
 */
public class CollectionsIndexerApp {
    /**
     * Print an index of the words found in the files whose paths are provided in `args`.  The index
     * keys are the distinct words, after converting to upper case, in lexicographic order.  The
     * index entries are the paths to the files in which that word is found, deduplicated (by the
     * exact path provided in `args`) and in lexicographic order, each followed by the list of
     * distinct line numbers on which that word occurs (separated by spaces).  Each entry line is
     * indented with a tab character.
     */
    public static void main(String[] args) throws IOException {
        // Check that at least one file is passed as an argument
        if (args.length < 1) {
            System.err.println("Please input at least one file");
            System.exit(1);
        }


        // TreeMap to store word → (file → set of line numbers)
        Map<String, Map<String, Set<Integer>>> index = new TreeMap<>();


        // LinkedHashSet to keep only unique files (removes duplicates in args)
        Set<String> uniqueFiles = new LinkedHashSet<>(Arrays.asList(args));


        // Loop through each file only once
        for (String filename : uniqueFiles) {
            try (Scanner scanner = new Scanner(new FileReader(filename))) {
                int lineNumber = 0;


                // Read file line-by-line
                while (scanner.hasNextLine()) {
                    lineNumber++;
                    String line = scanner.nextLine();


                    // Split the line into words
                    String[] words = line.split("\\s+");


                    // Keep track of which words we've already seen on this line
                    Set<String> seenThisLine = new HashSet<>();


                    for (String word : words) {
                        if (!word.isEmpty()) {
                            String upperWord = word.toUpperCase();


                            // Only count the word once per line
                            if (!seenThisLine.contains(upperWord)) {
                                seenThisLine.add(upperWord);


                                // If this word isn't in the index yet, add it
                                if (!index.containsKey(upperWord)) {
                                    index.put(upperWord, new TreeMap<>());
                                }


                                Map<String, Set<Integer>> fileMap = index.get(upperWord);


                                // If this file isn't in the fileMap yet, add it
                                if (!fileMap.containsKey(filename)) {
                                    fileMap.put(filename, new TreeSet<>());
                                }


                                Set<Integer> lines = fileMap.get(filename);
                                lines.add(lineNumber);  // Add the current line number
                            }
                        }
                    }
                }
            }
        }


        // Print out the index
        for (String word : index.keySet()) {
            System.out.println(word);
            Map<String, Set<Integer>> fileMap = index.get(word);


            for (String file : fileMap.keySet()) {
                System.out.print("\t" + file);
                for (int lineNum : fileMap.get(file)) {
                    System.out.print(" " + lineNum);
                }
                System.out.println(); // move to the next file line
            }
        }
    }
}














