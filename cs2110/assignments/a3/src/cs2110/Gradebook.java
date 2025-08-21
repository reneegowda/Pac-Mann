package cs2110;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Scanner;

/**
 * A collection of static methods for reading/writing gradebook tables in CSV format, transforming
 * their data, and calculating summary statistics.
 */
public class Gradebook {

    // --------------------------------------------------------------------------------------------
    // Section 1: Input / output

    /**
     * Read contents of a csv file and use this to construct a nested row-major sequence of strings.
     * Throws an IOException if `filename` cannot be accessed or read from.
     */
    public static Seq<Seq<String>> constructTable(String filename) throws IOException {
        Scanner scanner = new Scanner(new File(filename));
        Seq<Seq<String>> table = new DLinkedSeq<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] columns = line.split(",");

            Seq<String> row = new DLinkedSeq<>();
            for (String column : columns) {
                row.append(column);
            }
            table.append(row);
        }
        scanner.close();
        return table;
    }


    /**
     * Convert the contents of `table` into Simplified CSV format and write this to the file located
     * at `filename`. Throws an IOException if `filename` cannot be accessed or written to.
     */
    public static <T> void outputCSV(String filename, Seq<Seq<T>> table) throws IOException {
        // Recommendation: Use a `PrintWriter` and a try-with-resources statement.
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Iterate over each row in the table
            for (Seq<T> row : table) {
                String line = "";
                // Iterate over each element in the row
                for (T cell : row) {
                    line = line + cell + ",";
                }
                // Remove the trailing comma and write the line
                if (!line.isEmpty()) {
                    line = line.substring(0, line.length() - 1);
                }
                writer.println(line);
            }
        } catch (IOException e) {
            // Propagate the IOException to be handled by the caller
            throw new IOException("Error writing to file: " + filename, e);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Section 2: Table manipulation


    /**
     * Return whether each element of `table` has the same size.
     */
    private static <T> boolean isRectangular(Seq<Seq<T>> table) {
        if (table.size() == 0) {
            return true;
        }
        int width = table.get(0).size();
        for (Seq<T> row : table) {
            if (row.size() != width) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return a new table that is the transpose of the original table. That is, the rows of the
     * original table become the columns of the new table (and vice versa), and the orders of the
     * entries in these rows / columns remains the same. Requires `original` is rectangular.
     */
    public static <T> Seq<Seq<T>> transpose(Seq<Seq<T>> original) {
        // Implementation Constraint: The runtime of this method must be linearly proportional to
        //  the number of entries in the table.
        assert isRectangular(original);
        // Handle the empty matrix case
        if (original.size() == 0) {
            return new DLinkedSeq<>();
        }
        // Initialize the transposed sequence
        Seq<Seq<T>> transposed = new DLinkedSeq<>();
        // Use the first row to determine the number of columns
        int numCols = original.get(0).size();
        // Initialize empty rows for the transposed matrix
        for (int col = 0; col < numCols; col++) {
            transposed.append(new DLinkedSeq<>());
        }
        // Use iterators to avoid non-linear access times
        Iterator<Seq<T>> rowIterator = original.iterator();
        while (rowIterator.hasNext()) {
            Seq<T> currentRow = rowIterator.next(); // Get the current row
            Iterator<T> colIterator = currentRow.iterator(); // Iterator for the current row's
            // elements
            Iterator<Seq<T>> transposedColIterator = transposed.iterator(); // Iterator for
            // transposed columns
            while (colIterator.hasNext()) {
                // Append the current element to the corresponding column in the transposed matrix
                transposedColIterator.next().append(colIterator.next());
            }
        }
        return transposed;
    }

    // --------------------------------------------------------------------------------------------
    // Section 3: Summary statistics

    /**
     * Return the sum of the n'th powers of all numeric entries of `seq`. A numeric entry is any
     * entry that can be successfully parsed to a double by Double.parseDouble().
     */
    public static double powerSum(Seq<String> seq, int n) {
        double sum = 0.0;
        // Iterate over each element in the sequence
        for (String element : seq) {
            try {
                // Try parsing the element to a double
                double value = Double.parseDouble(element);
                // Add the n-th power of the value to the sum
                sum += Math.pow(value, n);
            } catch (NumberFormatException e) {
                // Ignore invalid entries that cannot be parsed as a number
            }
        }
        // Return the final sum of n-th powers
        return sum;
    }

    /**
     * Return the mean of the numerical entries of `seq`.  Returns NaN (aka 0.0/0.0) if `seq` does
     * not contain any numerical entries.
     */
    public static double mean(Seq<String> seq) {
        // Implementation Constraint: Your implementation must use the powerSum() method.
        double sum = powerSum(seq, 1);
        int count = 0;
        for (String element : seq) {
            try {
                Double.parseDouble(element);  // Try parsing the number
                count++;  // Increment count if parsing succeeds
            } catch (NumberFormatException e) {
                // Ignore invalid entries (do nothing)
            }
        }
        // If there are no valid numbers, return NaN (Not a Number)
        if (count == 0) {
            return Double.NaN;
        }
        // Return the mean (sum of numbers divided by count)
        return sum / count;
    }

    /**
     * Return the standard deviation of the numerical entries of `seq`.  Returns NaN (aka 0.0/0.0)
     * if `seq` does not contain any numerical entries.
     */
    public static double stdDev(Seq<String> seq) {
        // Implementation Constraint: Your implementation must use the powerSum() method.
        // Get the mean using the previously implemented method
        double mean = mean(seq);
        // If the mean is NaN, there are no valid numerical entries, so return NaN
        if (Double.isNaN(mean)) {
            return Double.NaN;
        }
        // Calculate the sum of the squares of the values using powerSum() for n=2
        double sumOfSquares = powerSum(seq, 2);
        // Count the number of valid entries
        int count = 0;
        for (String element : seq) {
            try {
                Double.parseDouble(element);  // Try parsing the number
                count++;  // Increment count if parsing succeeds
            } catch (NumberFormatException e) {
                // Ignore invalid entries (do nothing)
            }
        }
        // If there are no valid numbers, return NaN (since stdDev is undefined for empty data)
        if (count == 0) {
            return Double.NaN;
        }
        // Calculate the variance
        double variance = (sumOfSquares / count) - (mean * mean);
        // Return the square root of the variance to get the standard deviation
        return Math.sqrt(variance);
    }

    /**
     * Append "mean" and "standard deviation" columns to the end of the table `table`. The first row
     * of the table (the column "headers") has "mean" and "standard deviation" appended (in that
     * order). In subsequent rows, the string representations of the mean and standard deviation (in
     * that order) of the numerical entries are appended. All numbers are represented with full
     * precision. Requires that `table` is rectangular with at least one row and that no rows alias
     * one another.
     */
    public static void addSummaryColumns(Seq<Seq<String>> table) {
        assert table != null;
        assert isRectangular(table);
        assert table.size() > 0;

        // Add "mean" and "standard deviation" to the header row
        Seq<String> headerRow = table.get(0);  // Access the first row directly
        headerRow.append("mean");
        headerRow.append("standard deviation");

        // Add mean and standard deviation to each subsequent row
        for (int i = 1; i < table.size(); i++) {
            Seq<String> currentRow = table.get(i);
            double mean = mean(currentRow);
            double stdDev = stdDev(currentRow);
            currentRow.append(String.valueOf(mean));
            currentRow.append(String.valueOf(stdDev));
        }
    }

    /**
     * Append "mean" and "standard deviation" rows to the bottom of the table `table`. The first
     * entries of these rows are "mean" and "standard deviation" (respectively). The subsequent
     * entries are the mean and standard deviation (respectively) of the numerical entries in that
     * column. All numbers are represented with full precision. Requires that `table` is rectangular
     * with at least one column (implying at least one row).
     */
    public static void addSummaryRows(Seq<Seq<String>> table) {
        assert table != null;
        assert isRectangular(table);
        assert table.size() > 0;
        assert table.get(0).size() > 0;

        // Create the "mean" and "standard deviation" rows
        Seq<String> meanRow = new DLinkedSeq<>();
        meanRow.append("mean");
        Seq<String> stdDevRow = new DLinkedSeq<>();
        stdDevRow.append("standard deviation");

        // Iterate over each column (skip the first column, as it contains headers)
        int numCols = table.get(0).size();  // Get the number of columns from the first row
        for (int col = 1; col < numCols; col++) {
            // Extract the column values
            Seq<String> columnValues = new DLinkedSeq<>();
            for (Seq<String> currentRow : table) {
                columnValues.append(currentRow.get(col));  // Access the column directly
            }

            // Calculate the mean and standard deviation for the column
            double mean = mean(columnValues);
            double stdDev = stdDev(columnValues);

            // Append the mean and standard deviation to their respective rows
            meanRow.append(String.valueOf(mean));
            stdDevRow.append(String.valueOf(stdDev));
        }

        // Append the summary rows to the table
        table.append(meanRow);
        table.append(stdDevRow);
    }

    /**
     * Rounds all numerical data within `table` to the specified number of decimal places.
     */
    public static Seq<Seq<String>> roundEntries(Seq<Seq<String>> table, int decimalPlaces) {
        assert decimalPlaces >= 0;
        DecimalFormat df = new DecimalFormat("#." + "#".repeat(decimalPlaces));

        Seq<Seq<String>> rounded = new DLinkedSeq<>();
        for (Seq<String> row : table) {
            Seq<String> roundedRow = new DLinkedSeq<>();
            for (String cell : row) {
                try { // see if cell holds numerical data
                    double d = Double.parseDouble(cell);
                    roundedRow.append(df.format(d));
                } catch (NumberFormatException e) {
                    roundedRow.append(cell);
                }
            }
            rounded.append(roundedRow);
        }

        return rounded;
    }

    // --------------------------------------------------------------------------------------------
    // Section 4: A grading statistics application

    /**
     * A basic application to add summary statistics to a gradebook spreadsheet table. Takes in two
     * program arguments: the filepath of the input csv and the filepath for the output csv.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: Gradebook <input filename> <output filename>");
            System.exit(1);
        }
        String inputFilename = args[0];
        String outputFilename = args[1];

        Seq<Seq<String>> gradebook = constructTable(inputFilename);

        // TODO (challenge extension): Uncomment this line after implementing `rectangularize()`,
        //  then test with a ragged input CSV.
//        rectangularize(gradebook);

        // Verify preconditions
        if (gradebook.size() == 0) {
            System.err.println("Gradebook must have at least one row");
            System.exit(1);
        }

        addSummaryColumns(gradebook);
        addSummaryRows(gradebook);
        gradebook = roundEntries(gradebook, 5);
        outputCSV(outputFilename, gradebook);
    }
}
