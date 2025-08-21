package ui;

import model.GameModel;
import model.GameModel.GameState;
import util.Randomness;

/**
 * Run a sequence of non-interactive PacMan games and report final scores and other metrics.
 */
public class BatchApp {

    private GameModel model;

    public BatchApp(GameModel model) {
        this.model = model;
        setModel(model);
    }

    public void setModel(GameModel newModel) {
        model = newModel;
    }

    public GameModel model() {
        return model;
    }

    public GameModel.GameState play() {
        while (model.state() != GameState.VICTORY && model.state() != GameState.DEFEAT) {
            model.updateActors(Double.POSITIVE_INFINITY);
        }
        return model.state();
    }

    public static void main(String[] args) {

        // Default configuration parameters
        int width = 10;
        int height = 10;
        int numGames = 20;
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
            } else if (arg.startsWith("n=")) {
                numGames = Integer.parseInt(arg.substring(2));
            } else {
                throw new IllegalArgumentException("Unable to interpret argument: " + arg +
                        "\n Usage: java PacMannApp [h=<##>] [w=<##>] [seed=<##>] [ai_on] [paths_on]");
            }
        }

        // Print randomness seed, so an "interesting" game can be reproduced
        System.out.println("Randomness seed: " + seed);

        Randomness randomness = new Randomness(seed);
        var controller = new BatchApp(null);

        // Track statistics
        int numWins = 0;
        int totalScore = 0;
        int maxScore = 0;
        long bestSeed = randomness.seed();

        System.out.printf("%4s  %7s  %5s  %8s  %5s\n",
                "Game", "Result", "Score", "Time [s]", "Lives");
        for (int i = 0; i < numGames; i += 1) {
            controller.setModel(GameModel.newGame(10, 10, true, randomness));
            controller.play();
            var model = controller.model();

            // Update statistics
            if (model.state() == GameState.VICTORY) {
                numWins += 1;
            }
            totalScore += model.score();
            if (model.score() > maxScore) {
                maxScore = model.score();
                bestSeed = randomness.seed();
            }
            System.out.printf("%4d  %7s  %5d  %8.3f  %5d\n",
                    i, model.state(), model.score(), model.time() / 1000.0, model.numLives());

            randomness = randomness.next();
        }

        // Report statistics
        System.out.println();
        System.out.printf("Number of wins: %d / %d (%.1f %%)\n",
                numWins, numGames, 100.0 * numWins / numGames);
        System.out.printf("Average score: %.1f\n", (double) totalScore / numGames);
        System.out.printf("Best score: %d (seed: %d)\n", maxScore, bestSeed);
    }
}
