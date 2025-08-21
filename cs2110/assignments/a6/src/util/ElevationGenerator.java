package util;

import java.util.Random;

/**
 * Uses Perlin noise to generate the heights of each tile in the maze grid.
 */
public class ElevationGenerator {

    /**
     * Return a 2D double array with given `width` and `height` representing the elevations of each
     * tile, calculated using Perlin noise.
     */
    public static double[][] generateElevations(int width, int height, Random rand) {
        int spread = (width + height) / 6;  // how far apart the topographic features are

        double[][] elevations = new double[width][height];

        // build a courser grid of random gradients
        int gridWidth = width / spread + (width % spread == 0 ? 1 : 2);
        int gridHeight = height / spread + (width % spread == 0 ? 1 : 2);

        double[][][] vectors = new double[gridWidth][gridHeight][2];
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                vectors[i][j] = randomUnitVector(rand);
            }
        }

        // compute elevations using Perlin's procedure
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double x = (double) (2 * (i % spread) + 1) / (2 * spread);
                double y = (double) (2 * (j % spread) + 1) / (2 * spread);

                double nw = vectors[i / spread][j / spread][0] * x
                        + vectors[i / spread][j / spread][1] * y;
                double ne = vectors[i / spread + 1][j / spread][0] * (x - 1)
                        + vectors[i / spread + 1][j / spread][1] * y;
                double sw = vectors[i / spread][j / spread + 1][0] * x
                        + vectors[i / spread][j / spread + 1][1] * (y - 1);
                double se = vectors[i / spread + 1][j / spread + 1][0] * (x - 1)
                        + vectors[i / spread + 1][j / spread + 1][1] * (y - 1);

                elevations[i][j] = (nw * (1 - x) + ne * x) * (1 - y) + (sw * (1 - x) + se * x) * y;
            }
        }

        // linearly transform all elevations into [0,1]
        double minElev = elevations[0][0];
        double maxElev = elevations[0][0];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                maxElev = Math.max(elevations[i][j], maxElev);
                minElev = Math.min(elevations[i][j], minElev);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (elevations[i][j] - minElev) / (maxElev - minElev);
            }
        }

        return elevations;
    }

    /**
     * Return a random 2D unit vector
     */
    static double[] randomUnitVector(Random rand) {
        double u = rand.nextGaussian();
        double v = rand.nextGaussian();
        double norm = Math.sqrt(u * u + v * v);
        return new double[]{u / norm, v / norm};
    }
}
