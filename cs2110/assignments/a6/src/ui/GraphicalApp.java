package ui;

import javax.swing.SwingUtilities;

/**
 * Runs the PacMann game in its GUI mode.
 */
public class GraphicalApp {

    /**
     * Process the program arguments and construct/show the GameFrame on the event dispatch thread.
     */
    public static void main(String[] args) {
        // Default configuration parameters
        int width = 10;
        int height = 10;
        boolean withAI = false;
        boolean showPaths = false;
        // Default to a different seed every time
        long seed = System.currentTimeMillis();

        for (String arg : args) {
            if (arg.startsWith("w=")) {
                width = Integer.parseInt(arg.substring(2));
                if (width < 4) {
                    throw new IllegalArgumentException("Board width must be at least 4.");
                }
            } else if (arg.startsWith("h=")) {
                height = Integer.parseInt(arg.substring(2));
                if (height < 3) {
                    throw new IllegalArgumentException("Board height must be at least 3.");
                }
            } else if (arg.startsWith("seed=")) {
                seed = Long.parseLong(arg.substring(5));
            } else if (arg.equals("ai_on")) {
                withAI = true;
            } else if (arg.equals("paths_on")) {
                showPaths = true;
            } else {
                throw new IllegalArgumentException("Unable to interpret argument: " + arg +
                        "\n Usage: java PacMannApp [h=<##>] [w=<##>] [seed=<##>] [ai_on] [paths_on]");
            }
        }

        // Save final configuration in new variables, suitable for lambda capture
        int finalWidth = width;
        int finalHeight = height;
        boolean finalWithAI = withAI;
        boolean finalShowPaths = showPaths;
        long finalSeed = seed;

        // Print randomness seed, so an "interesting" game can be reproduced
        System.out.println("Randomness seed: " + finalSeed);

        // Create and start GUI
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame(finalWidth, finalHeight, finalWithAI, finalShowPaths,
                    finalSeed);
            frame.setVisible(true);
        });
    }
}
