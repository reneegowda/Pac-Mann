package cs2110;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class E2ETest {

    @DisplayName("The provided end-to-end test on small_gradebook.csv produces the expected output")
    @Test
    void testGradebook() throws IOException {
        Path outfile = Files.createTempFile("testGradebook-", ".csv");
        outfile.toFile().deleteOnExit();

        Gradebook.main(new String[]{"csv/small_gradebook.csv", outfile.toString()});
        String expectedLines = Files.readString(Path.of("csv/small_gradebook_expected_output.csv"));
        String actualLines = Files.readString(outfile);

        // Compare line-by-line, which both makes the output easier to understand and avoids issues
        //  with platform-dependent line endings.
        Scanner scanExpected = new Scanner(expectedLines);
        Scanner scanActual = new Scanner(actualLines);
        while (scanExpected.hasNextLine()) {
            String expected = scanExpected.nextLine();
            String actual = scanActual.nextLine();
            assertEquals(expected, actual);
        }
        assertFalse(scanActual.hasNextLine());
    }
}
