package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Used to generate a "Pac-Man style" maze on which the game is played.
 */
public class MazeGenerator {

    /**
     * Each tile (i.e. square of the game grid) is either occupied by a wall, a path (accessible to
     * PacMann) or the ghost box (inaccessible to PacMann).
     */
    public enum TileType {
        WALL, PATH, GHOSTBOX
    }

    /**
     * Connected components of Cells model wall segments in our generator algorithm.
     */
    private record Cell(int x, int y) {

    }

    /**
     * Edges represent the boundaries of cells.
     */
    private record CellBoundary(Orientation orientation, int x, int y) {

    }

    /**
     * The direction of this edge.
     */
    private enum Orientation {HORIZONTAL, VERTICAL}

    /**
     * The width of the mazes (i.e., number of path columns) produced by this generator.
     */
    final int width;

    /**
     * The height of the mazes (i.e., number of path rows) produced by this generator.
     */
    final int height;

    final Random rng;

    /**
     * Construct a maze generator that produces mazes of size `3*width+2 x 3*height+2`.
     */
    public MazeGenerator(int width, int height, Random rng) {
        this.width = width;
        this.height = height;

        this.rng = rng;
    }

    /**
     * Returns a randomly generated maze, which a 2D array of dimension `3*width+2` by `3*height+2`
     * of either wall or path tiles. Guarantees that all path tiles (except for the ghost's starting
     * box) will be connected. Requires `width >= 4` and `height >= 3`.
     */
    public TileType[][] generateMaze() {
        /* This algorithm is inspired by https://shaunlebron.github.io/pacman-mazegen/ */

        TileType[][] tiles = new TileType[3 * width + 2][3 * height + 2];
        fillKnownTileTypes(tiles);

        HashMap<Cell, HashSet<Cell>> components = initializeCellComponents(width / 2,
                height - 1);

        boolean[][] horizontalEdges = new boolean[width / 2][height];
        for (boolean[] row : horizontalEdges) {
            Arrays.fill(row, true);
        }
        boolean[][] verticalEdges = new boolean[(width + 1) / 2][height - 1];
        for (boolean[] row : verticalEdges) {
            Arrays.fill(row, true);
        }
        List<CellBoundary> assignableCellBoundaries = initializeEdges(horizontalEdges,
                verticalEdges);

        randomlyAssignEdges(horizontalEdges, verticalEdges, assignableCellBoundaries, components);

        fillAssignedTileTypes(tiles, horizontalEdges, verticalEdges);

        fixGhostBox(tiles);

        return tiles;
    }

    /**
     * Fill in the types of tiles that are not randomly generated.
     */
    private void fillKnownTileTypes(TileType[][] tiles) {
        int w = tiles.length;
        int h = tiles[0].length;

        // most borders should be walls
        for (int i = 0; i < w; i++) {
            tiles[i][0] = TileType.WALL;
            tiles[i][1] = TileType.WALL;
            tiles[i][h - 2] = TileType.WALL;
            tiles[i][h - 1] = TileType.WALL;
        }
        for (int j = 2; j < h - 2; j++) {
            tiles[0][j] = TileType.WALL;
            tiles[1][j] = TileType.WALL;
            tiles[w - 2][j] = TileType.WALL;
            tiles[w - 1][j] = TileType.WALL;
        }

        // add tunnels at borders
        int firstHorizontalTunnel = (((h / 3 + 2) % 5) / 2 + 1) * 3 + 2;
        for (int j = firstHorizontalTunnel; j < h; j += 15) {
            tiles[0][j] = TileType.PATH;
            tiles[1][j] = TileType.PATH;
            tiles[w - 2][j] = TileType.PATH;
            tiles[w - 1][j] = TileType.PATH;
        }

        int firstVerticalTunnel = (((w / 3 + 2) % 5) / 2 + 1) * 3 + 2;
        for (int i = firstVerticalTunnel; i < w; i += 15) {
            tiles[i][0] = TileType.PATH;
            tiles[i][1] = TileType.PATH;
            tiles[i][h - 2] = TileType.PATH;
            tiles[i][h - 1] = TileType.PATH;
        }

        // guaranteed path tiles
        for (int i = 0; i < w / 3; i++) {
            for (int j = 0; j < h / 3; j++) {
                tiles[3 * i + 2][3 * j + 2] = TileType.PATH;
            }
        }

        // guaranteed interior walls
        for (int i = 1; i < w / 3; i++) {
            for (int j = 1; j < h / 3; j++) {
                tiles[3 * i][3 * j] = TileType.WALL;
                tiles[3 * i + 1][3 * j] = TileType.WALL;
                tiles[3 * i][3 * j + 1] = TileType.WALL;
                tiles[3 * i + 1][3 * j + 1] = TileType.WALL;
            }
        }
    }

    /**
     * Return a hashmap associating each cell with its connected component. Initially, a cells
     * connected component is the singleton set containing that cell.
     */
    private HashMap<Cell, HashSet<Cell>> initializeCellComponents(int w, int h) {
        HashMap<Cell, HashSet<Cell>> components = new HashMap<>();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Cell c = new Cell(i, j);
                components.put(c, new HashSet<>(Set.of(c)));
            }
        }
        return components;
    }

    /**
     * Initialize the edges that must be present/absent in the graph. Return a list of the edges
     * that can be randomly determined.
     */
    private List<CellBoundary> initializeEdges(boolean[][] horizontalEdges,
            boolean[][] verticalEdges) {
        List<CellBoundary> assignableCellBoundaries = new ArrayList<>();

        int hw = horizontalEdges.length; // width of horizontal array
        int hh = horizontalEdges[0].length; // height of horizontal array
        int vw = verticalEdges.length; // width of vertical array
        int vh = verticalEdges[0].length; // height of vertical array

        for (int i = 0; i < hw; i++) {
            for (int j = 1; j < hh - 1; j++) {
                assignableCellBoundaries.add(new CellBoundary(Orientation.HORIZONTAL, i, j));
            }
        }
        // ghost box
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 1, (hh - 3) / 2));
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 2, (hh - 3) / 2));
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 1, (hh - 1) / 2));
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 2, (hh - 1) / 2));
        horizontalEdges[hw - 2][(hh - 1) / 2] = false;
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 1, (hh + 1) / 2));
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.HORIZONTAL, hw - 2, (hh + 1) / 2));

        // pac man spawn spot
        if (width % 2 == 0) {
            assignableCellBoundaries.remove(
                    new CellBoundary(Orientation.HORIZONTAL, hw - 1, (3 * hh - 1) / 4));
        }

        for (int i = 1; i < vw; i++) {
            for (int j = 0; j < vh; j++) {
                assignableCellBoundaries.add(new CellBoundary(Orientation.VERTICAL, i, j));
            }
        }
        // ghost box
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.VERTICAL, vw - 1, (vh - 2) / 2));
        assignableCellBoundaries.remove(new CellBoundary(Orientation.VERTICAL, vw - 1, vh / 2));
        verticalEdges[vw - 1][(vh - 2) / 2] = false;
        verticalEdges[vw - 1][vh / 2] = false;
        assignableCellBoundaries.remove(
                new CellBoundary(Orientation.VERTICAL, vw - 2, (vh - 2) / 2));
        assignableCellBoundaries.remove(new CellBoundary(Orientation.VERTICAL, vw - 2, vh / 2));
        if (width % 2 == 1) {
            verticalEdges[vw - 2][(vh - 2) / 2] = false;
            verticalEdges[vw - 2][vh / 2] = false;
            assignableCellBoundaries.remove(
                    new CellBoundary(Orientation.VERTICAL, vw - 3, (vh - 2) / 2));
            assignableCellBoundaries.remove(new CellBoundary(Orientation.VERTICAL, vw - 3, vh / 2));
        }
        return assignableCellBoundaries;
    }

    /**
     * Iterate over the assignable edges in a random order to determine whether to include them. An
     * edge is deleted as long as it does not introduce a connected component that is too large or
     * create a dead-end in the maze.
     */
    private void randomlyAssignEdges(boolean[][] horizontalEdges, boolean[][] verticalEdges,
            List<CellBoundary> assignableCellBoundaries, HashMap<Cell, HashSet<Cell>> components) {
        shuffle(assignableCellBoundaries, rng);

        for (CellBoundary e : assignableCellBoundaries) {
            if (e.orientation == Orientation.HORIZONTAL) {
                HashSet<Cell> mergedComponent = new HashSet<>(
                        components.get(new Cell(e.x, e.y - 1)));
                mergedComponent.addAll(components.get(new Cell(e.x, e.y)));

                int numTouchingLeftEndpoint = 1;
                numTouchingLeftEndpoint += verticalEdges[e.x][e.y] ? 1 : 0;
                numTouchingLeftEndpoint += verticalEdges[e.x][e.y - 1] ? 1 : 0;
                numTouchingLeftEndpoint += (e.x > 0 && horizontalEdges[e.x - 1][e.y]) ? 1 : 0;

                int numTouchingRightEndpoint = 1;
                if (width % 2 == 0 && e.x == (width - 2) / 2) {
                    numTouchingRightEndpoint = numTouchingLeftEndpoint;
                } else {
                    numTouchingRightEndpoint += verticalEdges[e.x + 1][e.y] ? 1 : 0;
                    numTouchingRightEndpoint += verticalEdges[e.x + 1][e.y - 1] ? 1 : 0;
                    numTouchingRightEndpoint +=
                            (e.x < (width - 2) / 2 && horizontalEdges[e.x + 1][e.y]) ? 1 : 0;
                }

                if (mergedComponent.size() <= 4 && numTouchingLeftEndpoint > 2 &&
                        numTouchingRightEndpoint > 2) {
                    horizontalEdges[e.x][e.y] = false;
                    for (Cell c : mergedComponent) {
                        components.put(c, mergedComponent);
                    }
                }
            } else {
                HashSet<Cell> mergedComponent = new HashSet<>(
                        components.get(new Cell(e.x - 1, e.y)));
                if (width % 2 == 1 && e.x == (width - 1) / 2) {
                    // special case for center of odd width boards
                    // double their merge component size by adding dummy cells
                    for (Cell c : components.get(new Cell(e.x - 1, e.y))) {
                        mergedComponent.add(new Cell(-c.x(), c.y()));
                    }
                } else {
                    mergedComponent.addAll(components.get(new Cell(e.x, e.y)));
                }

                int numTouchingTopEndpoint = 1;
                if (width % 2 == 1 && e.x == (width - 1) / 2) {
                    numTouchingTopEndpoint += horizontalEdges[e.x - 1][e.y] ? 2 : 0;
                } else {
                    numTouchingTopEndpoint += horizontalEdges[e.x - 1][e.y] ? 1 : 0;
                    numTouchingTopEndpoint += horizontalEdges[e.x][e.y] ? 1 : 0;
                }
                numTouchingTopEndpoint +=
                        (e.y > 0 && verticalEdges[e.x][e.y - 1]) ? 1 : 0;

                int numTouchingBottomEndpoint = 1;
                if (width % 2 == 1 && e.x == (width - 1) / 2) {
                    numTouchingBottomEndpoint += horizontalEdges[e.x - 1][e.y + 1] ? 2 : 0;
                } else {
                    numTouchingBottomEndpoint += horizontalEdges[e.x - 1][e.y + 1] ? 1 : 0;
                    numTouchingBottomEndpoint += horizontalEdges[e.x][e.y + 1] ? 1 : 0;
                }
                numTouchingBottomEndpoint +=
                        (e.y < height - 2 && verticalEdges[e.x][e.y + 1]) ? 1 : 0;

                if (mergedComponent.size() <= 4 && numTouchingTopEndpoint > 2 &&
                        numTouchingBottomEndpoint > 2) {
                    verticalEdges[e.x][e.y] = false;
                    for (Cell c : mergedComponent) {
                        components.put(c, mergedComponent);
                    }
                }
            }
        }
    }

    /**
     * Use the horizontal and vertical edge assignments to determine the remaining tile types.
     */
    private void fillAssignedTileTypes(TileType[][] tiles, boolean[][] horizontalEdges,
            boolean[][] verticalEdges) {

        int hw = horizontalEdges.length; // width of horizontal array
        int hh = horizontalEdges[0].length; // height of horizontal array
        int vw = verticalEdges.length; // width of vertical array
        int vh = verticalEdges[0].length; // height of vertical array

        for (int i = 0; i < hw; i++) {
            for (int j = 0; j < hh; j++) {
                tiles[3 * i + 3][3 * j + 2] = horizontalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * i + 4][3 * j + 2] = horizontalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * (width - i) - 2][3 * j + 2] =
                        horizontalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * (width - i) - 3][3 * j + 2] =
                        horizontalEdges[i][j] ? TileType.PATH : TileType.WALL;
            }
        }
        for (int i = 0; i < vw; i++) {
            for (int j = 0; j < vh; j++) {
                tiles[3 * i + 2][3 * j + 3] = verticalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * i + 2][3 * j + 4] = verticalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * (width - i) - 1][3 * j + 3] =
                        verticalEdges[i][j] ? TileType.PATH : TileType.WALL;
                tiles[3 * (width - i) - 1][3 * j + 4] =
                        verticalEdges[i][j] ? TileType.PATH : TileType.WALL;
            }
        }
    }

    /**
     * Reassign the types of tiles within the ghost box.
     */
    private void fixGhostBox(TileType[][] tiles) {
        int i = 3 * (width / 2) - 1;
        int j = 3 * ((height - 1) / 2) + 2;
        for (int di = 0; di < 4 + 3 * (width % 2); di++) {
            tiles[i + di][j] = TileType.GHOSTBOX;
        }
    }

    /**
     * Shuffle the contents of `list` in place.  Each element has a uniform probability of being
     * placed at any index in the list.  The resulting order depends on the sequence of random
     * integers provided by `rng`.
     */
    private static <T> void shuffle(List<T> list, Random rng) {
        // Fisher-Yates shuffle
        for (int i = 0; i < list.size() - 1; i += 1) {
            int j = rng.nextInt(i, list.size());
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
