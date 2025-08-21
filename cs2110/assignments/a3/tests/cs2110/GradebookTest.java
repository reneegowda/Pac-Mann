package cs2110;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class GradebookTest {

    // Helper method to create a Seq<Seq<String>> from a 2D array
    private Seq<Seq<String>> createTable(String[][] data) {
        Seq<Seq<String>> table = new DLinkedSeq<>();
        for (String[] row : data) {
            Seq<String> seqRow = new DLinkedSeq<>();
            for (String cell : row) {
                seqRow.append(cell);
            }
            table.append(seqRow);
        }
        return table;
    }

    // Helper method to create a temporary CSV file for testing
    private File createTempCSV(String content) throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    // --------------------------------------------------------------------------------------------
    // Section 1: Input / Output Tests

    @Test
    void testConstructTable() throws IOException {
        // Create a temporary CSV file
        String csvContent = "Name,Age,Height\nAlice,25,165\nBob,30,180";
        File tempFile = createTempCSV(csvContent);

        // Test constructTable
        Seq<Seq<String>> table = Gradebook.constructTable(tempFile.getAbsolutePath());
        assertEquals(3, table.size()); // 3 rows
        assertEquals(3, table.get(0).size()); // 3 columns
        assertEquals("Name", table.get(0).get(0));
        assertEquals("25", table.get(1).get(1));
        assertEquals("180", table.get(2).get(2));

        // Clean up
        tempFile.delete();
    }

    @Test
    void testOutputCSV() throws IOException {
        // Create a table
        Seq<Seq<String>> table = createTable(new String[][]{
                {"Name", "Age", "Height"},
                {"Alice", "25", "165"},
                {"Bob", "30", "180"}
        });

        // Write to a temporary file
        File tempFile = File.createTempFile("test", ".csv");
        Gradebook.outputCSV(tempFile.getAbsolutePath(), table);

        // Read the file back and verify its contents
        Seq<Seq<String>> readTable = Gradebook.constructTable(tempFile.getAbsolutePath());
        assertEquals(table.size(), readTable.size());
        for (int i = 0; i < table.size(); i++) {
            assertEquals(table.get(i).size(), readTable.get(i).size());
            for (int j = 0; j < table.get(i).size(); j++) {
                assertEquals(table.get(i).get(j), readTable.get(i).get(j));
            }
        }

        // Clean up
        tempFile.delete();
    }

    // --------------------------------------------------------------------------------------------
    // Section 2: Table Manipulation Tests

    @Test
    void testTranspose() {
        Seq<Seq<String>> original = createTable(new String[][]{
                {"A", "B", "C"},
                {"1", "2", "3"}
        });
        Seq<Seq<String>> transposed = Gradebook.transpose(original);

        assertEquals(3, transposed.size()); // 3 rows after transposing
        assertEquals(2, transposed.get(0).size()); // 2 columns after transposing
        assertEquals("A", transposed.get(0).get(0));
        assertEquals("1", transposed.get(0).get(1));
        assertEquals("C", transposed.get(2).get(0));
        assertEquals("3", transposed.get(2).get(1));
    }

    // --------------------------------------------------------------------------------------------
    // Section 3: Summary Statistics Tests

    @Test
    void testPowerSum() {
        Seq<String> seq = new DLinkedSeq<>();
        seq.append("1");
        seq.append("2");
        seq.append("3");

        assertEquals(14.0, Gradebook.powerSum(seq, 2)); // 1^2 + 2^2 + 3^2 = 14
    }

    @Test
    void testMean() {
        Seq<String> seq = new DLinkedSeq<>();
        seq.append("10");
        seq.append("20");
        seq.append("30");

        assertEquals(20.0, Gradebook.mean(seq)); // (10 + 20 + 30) / 3 = 20
    }

    @Test
    void testStdDev() {
        Seq<String> seq = new DLinkedSeq<>();
        seq.append("10");
        seq.append("20");
        seq.append("30");

        double mean = Gradebook.mean(seq);
        double variance = (Math.pow(10 - mean, 2) + Math.pow(20 - mean, 2) + Math.pow(30 - mean, 2)) / 3;
        double expectedStdDev = Math.sqrt(variance);

        assertEquals(expectedStdDev, Gradebook.stdDev(seq));
    }

    @Test
    void testAddSummaryColumns() {
        // Create a table with sample data
        Seq<Seq<String>> table = createTable(new String[][]{
                {"Name", "Age", "Height"},
                {"Alice", "25", "165"},
                {"Bob", "30", "180"}
        });

        // Add summary columns
        Gradebook.addSummaryColumns(table);

        // Verify the number of rows and columns
        assertEquals(3, table.size()); // 3 rows
        assertEquals(5, table.get(0).size()); // 5 columns (original + mean + stdDev)
        assertEquals("mean", table.get(0).get(3));
        assertEquals("standard deviation", table.get(0).get(4));

        // Verify the "Alice" row
        Seq<String> aliceRow = table.get(1);
        assertEquals("Alice", aliceRow.get(0));
        assertEquals("25", aliceRow.get(1));
        assertEquals("165", aliceRow.get(2));

        // Calculate the expected mean and standard deviation for the "Alice" row
        double meanAlice = (25 + 165) / 2.0; // (25 + 165) / 2 = 95.0
        double stdDevAlice = Math.sqrt((Math.pow(25 - meanAlice, 2) + Math.pow(165 - meanAlice, 2)) / 2); // sqrt((4900 + 4900) / 2) = sqrt(4900) = 70.0
        assertEquals(String.valueOf(meanAlice), aliceRow.get(3)); // Verify mean
        assertEquals(String.valueOf(stdDevAlice), aliceRow.get(4)); // Verify stdDev

        // Verify the "Bob" row
        Seq<String> bobRow = table.get(2);
        assertEquals("Bob", bobRow.get(0));
        assertEquals("30", bobRow.get(1));
        assertEquals("180", bobRow.get(2));

        // Calculate the expected mean and standard deviation for the "Bob" row
        double meanBob = (30 + 180) / 2.0; // (30 + 180) / 2 = 105.0
        double stdDevBob = Math.sqrt((Math.pow(30 - meanBob, 2) + Math.pow(180 - meanBob, 2)) / 2); // sqrt((5625 + 5625) / 2) = sqrt(5625) = 75.
    }

    @Test
    void testAddSummaryRows() {
        // Create a table with sample data
        Seq<Seq<String>> table = createTable(new String[][]{
                {"Name", "Age", "Height"},
                {"Alice", "25", "165"},
                {"Bob", "30", "180"}
        });

        // Add summary rows
        Gradebook.addSummaryRows(table);

        // Verify the number of rows and columns
        assertEquals(5, table.size()); // 5 rows (original + mean + stdDev)
        assertEquals(3, table.get(0).size()); // 3 columns

        // Verify the "mean" row
        Seq<String> meanRow = table.get(3);
        assertEquals("mean", meanRow.get(0)); // First column is "mean"

        // Calculate the expected mean for the "Age" column
        double meanAge = (25 + 30) / 2.0; // (25 + 30) / 2 = 27.5
        assertEquals(String.valueOf(meanAge), meanRow.get(1)); // Verify mean of "Age"

        // Calculate the expected mean for the "Height" column
        double meanHeight = (165 + 180) / 2.0; // (165 + 180) / 2 = 172.5
        assertEquals(String.valueOf(meanHeight), meanRow.get(2)); // Verify mean of "Height"

        // Verify the "standard deviation" row
        Seq<String> stdDevRow = table.get(4);
        assertEquals("standard deviation", stdDevRow.get(0)); // First column is "standard deviation"

        // Calculate the expected standard deviation for the "Age" column
        double varianceAge = (Math.pow(25 - meanAge, 2) + Math.pow(30 - meanAge, 2)) / 2;
        double stdDevAge = Math.sqrt(varianceAge); // sqrt(12.5) ≈ 3.53553
        assertEquals(String.valueOf(stdDevAge), stdDevRow.get(1)); // Verify stdDev of "Age"

        // Calculate the expected standard deviation for the "Height" column
        double varianceHeight = (Math.pow(165 - meanHeight, 2) + Math.pow(180 - meanHeight, 2)) / 2;
        double stdDevHeight = Math.sqrt(varianceHeight); // sqrt(112.5) ≈ 10.6066
        assertEquals(String.valueOf(stdDevHeight), stdDevRow.get(2)); // Verify stdDev of "Height"
    }

    @Test
    void testRoundEntries() {
        Seq<Seq<String>> table = createTable(new String[][]{
                {"Name", "Age", "Height"},
                {"Alice", "25.123456", "165.987654"},
                {"Bob", "30.555555", "180.111111"}
        });

        Seq<Seq<String>> roundedTable = Gradebook.roundEntries(table, 2);

        assertEquals("25.12", roundedTable.get(1).get(1));
        assertEquals("165.99", roundedTable.get(1).get(2));
        assertEquals("30.56", roundedTable.get(2).get(1));
        assertEquals("180.11", roundedTable.get(2).get(2));
    }
}