package model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeVertex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.GameMap;
import util.MazeGenerator.TileType;

public class MazeGraphTest {

    /* Note, to conform to the precondition of the `MazeGraph` constructor, make sure that any
     * TileType arrays that you construct contain a `PATH` tile at index [2][2] and represent a
     * single, orthogonally connected component of `PATH` tiles. */

    /**
     * Create a game map with tile types corresponding to the letters on each line of `template`.
     * 'w' = WALL, 'p' = PATH, and 'g' = GHOSTBOX.  The letters of `template` must form a rectangle.
     * Elevations will be a gradient from the top-left to the bottom-right corner with a horizontal
     * slope of 2 and a vertical slope of 1.
     */
    static GameMap createMap(String template) {
        Scanner lines = new Scanner(template);
        ArrayList<ArrayList<TileType>> lineLists = new ArrayList<>();

        while (lines.hasNextLine()) {
            ArrayList<TileType> lineList = new ArrayList<>();
            for (char c : lines.nextLine().toCharArray()) {
                switch (c) {
                    case 'w' -> lineList.add(TileType.WALL);
                    case 'p' -> lineList.add(TileType.PATH);
                    case 'g' -> lineList.add(TileType.GHOSTBOX);
                }
            }
            lineLists.add(lineList);
        }

        int height = lineLists.size();
        int width = lineLists.getFirst().size();

        TileType[][] types = new TileType[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                types[i][j] = lineLists.get(j).get(i);
            }
        }

        double[][] elevations = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (2.0 * i + j);
            }
        }
        return new GameMap(types, elevations);
    }

    @DisplayName("WHEN a GameMap with exactly one path tile in position [2][2] is passed into the "
            + "MazeGraph constructor, THEN a graph with one vertex is created.")
    @Test
    void testOnePathCell() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(1, vertices.size());
        assertTrue(vertices.containsKey(new IPair(2, 2)));
    }

    @DisplayName("WHEN a GameMap with exactly two horizontally adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsHorizontal() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair left = new IPair(2, 2);
        IPair right = new IPair(3, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has one edge to the vertex to its right
        assertNull(vl.edgeInDirection(Direction.LEFT));
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.RIGHT);
        assertNotNull(l2r);

        // edge from left to right has the correct fields
        double lElev = map.elevations()[2][2];
        double rElev = map.elevations()[3][2];
        assertEquals(vl, l2r.src());
        assertEquals(vr, l2r.dst());
        assertEquals(Direction.RIGHT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex to its left with the correct fields
        assertNull(vr.edgeInDirection(Direction.RIGHT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.LEFT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.src());
        assertEquals(vl, r2l.dst());
        assertEquals(Direction.LEFT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap with exactly two vertically adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsVertical() {
        // Create map with two vertically stacked path tiles
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww  // Top path at (2,2)
                wwpww  // Bottom path at (2,3)
                wwwww""");

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify exactly two vertices created at correct positions
        assertEquals(2, vertices.size());
        IPair top = new IPair(2, 2);
        IPair bottom = new IPair(2, 3);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        // Verify top vertex only has downward edge
        assertNull(vt.edgeInDirection(Direction.LEFT));
        assertNull(vt.edgeInDirection(Direction.RIGHT));
        assertNull(vt.edgeInDirection(Direction.UP));
        MazeEdge t2b = vt.edgeInDirection(Direction.DOWN);
        assertNotNull(t2b);

        // Verify downward edge properties
        double tElev = map.elevations()[2][2];
        double bElev = map.elevations()[2][3];
        assertEquals(vt, t2b.src());
        assertEquals(vb, t2b.dst());
        assertEquals(Direction.DOWN, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        // Verify bottom vertex only has upward edge
        assertNull(vb.edgeInDirection(Direction.LEFT));
        assertNull(vb.edgeInDirection(Direction.RIGHT));
        assertNull(vb.edgeInDirection(Direction.DOWN));
        MazeEdge b2t = vb.edgeInDirection(Direction.UP);
        assertNotNull(b2t);
        assertEquals(vb, b2t.src());
        assertEquals(vt, b2t.dst());
        assertEquals(Direction.UP, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());
    }

    /**
     * Tests horizontal tunnel creation between path tiles at row edges
     */
    @DisplayName("WHEN a GameMap includes two path tiles in the first and last column of the same row, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testHorizontalTunnelEdgeCreation() {
        // Create map with path tiles at both ends of row 1
        GameMap map = createMap("""
                wwwww
                pwwwp  // Path tiles at (0,1) and (4,1)
                wwwww
                wwwww
                wwwww""");

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify exactly two vertices created
        assertEquals(2, vertices.size());
        IPair left = new IPair(0, 1);
        IPair right = new IPair(4, 1);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex leftVertex = vertices.get(left);
        MazeVertex rightVertex = vertices.get(right);

        // Verify tunnel edge from left to right (LEFT direction)
        MazeEdge rightEdge = leftVertex.edgeInDirection(Direction.LEFT); //tunnel from left to right
        assertNotNull(rightEdge);
        assertEquals(leftVertex, rightEdge.src());
        assertEquals(rightVertex, rightEdge.dst());
        assertEquals(Direction.LEFT, rightEdge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[0][1], map.elevations()[4][1]),
                rightEdge.weight());

        // Verify tunnel edge from right to left (RIGHT direction)
        MazeEdge leftEdge = rightVertex.edgeInDirection(
                Direction.RIGHT); //tunnel from right to left
        assertNotNull(leftEdge);
        assertEquals(rightVertex, leftEdge.src());
        assertEquals(leftVertex, leftEdge.dst());
        assertEquals(Direction.RIGHT, leftEdge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[4][1], map.elevations()[0][1]),
                leftEdge.weight());
    }

    /**
     * Tests cyclic path connections around a center wall
     */
    @DisplayName(
            "WHEN a GameMap includes a cyclic connected component of path tiles with a non-path tile in the middle, "
                    + "THEN its graph includes edges between all adjacent pairs of vertices.")
    @Test
    void testCyclicPaths() {
        // Create donut-shaped path around center wall
        GameMap map = createMap("""
                wwwwwww
                wwwwwww
                wwpppww  // Top path segment
                wwpwpww  // Middle with center wall
                wwpppww  // Bottom path segment
                wwwwwww""");

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Verify all 8 path vertices exist
        assertEquals(8, vertices.size());

        // Verify connections in clockwise cycle

        // 2,2 -> 3,2 (Right)
        MazeVertex src = vertices.get(new IPair(2, 2));
        MazeVertex dst = vertices.get(new IPair(3, 2));
        MazeEdge edge = src.edgeInDirection(Direction.RIGHT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.RIGHT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][2], map.elevations()[3][2]),
                edge.weight());

        // 3,2 -> 4,2 (Right)
        src = vertices.get(new IPair(3, 2));
        dst = vertices.get(new IPair(4, 2));
        edge = src.edgeInDirection(Direction.RIGHT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.RIGHT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[3][2], map.elevations()[4][2]),
                edge.weight());

        // 4,2 -> 4,3 (Down)
        src = vertices.get(new IPair(4, 2));
        dst = vertices.get(new IPair(4, 3));
        edge = src.edgeInDirection(Direction.DOWN);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.DOWN, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[4][2], map.elevations()[4][3]),
                edge.weight());

        // 4,3 -> 4,4 (Down)
        src = vertices.get(new IPair(4, 3));
        dst = vertices.get(new IPair(4, 4));
        edge = src.edgeInDirection(Direction.DOWN);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.DOWN, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[4][3], map.elevations()[4][4]),
                edge.weight());

        // 4,4 -> 3,4 (Left)
        src = vertices.get(new IPair(4, 4));
        dst = vertices.get(new IPair(3, 4));
        edge = src.edgeInDirection(Direction.LEFT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.LEFT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[4][4], map.elevations()[3][4]),
                edge.weight());

        // 3,4 -> 2,4 (Left)
        src = vertices.get(new IPair(3, 4));
        dst = vertices.get(new IPair(2, 4));
        edge = src.edgeInDirection(Direction.LEFT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.LEFT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[3][4], map.elevations()[2][4]),
                edge.weight());

        // 2,4 -> 2,3 (Up)
        src = vertices.get(new IPair(2, 4));
        dst = vertices.get(new IPair(2, 3));
        edge = src.edgeInDirection(Direction.UP);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.UP, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][4], map.elevations()[2][3]),
                edge.weight());

        // 2,3 -> 2,2 (Up)
        src = vertices.get(new IPair(2, 3));
        dst = vertices.get(new IPair(2, 2));
        edge = src.edgeInDirection(Direction.UP);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.UP, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][3], map.elevations()[2][2]),
                edge.weight());
    }

    // TODO 2d: Add at least two additional test cases that test other distinct path structures.
    //  It is crucial that your graph is being linked together correctly, otherwise the later
    //  portions of the assignment will break with strange behaviors.

    // Additional test cases
    @DisplayName("WHEN a GameMap has a 2x2 square of path tiles, THEN each vertex connects to its adjacent neighbors")
    @Test
    void testSquarePathTiles() {
        GameMap map = createMap("""
                wwwww
                wppww
                wppww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(4, vertices.size());

        // Check connections for the square (1,1), (1,2), (2,1), (2,2)

        // 1,1 -> 2,1 (Right)
        MazeVertex src = vertices.get(new IPair(1, 1));
        MazeVertex dst = vertices.get(new IPair(2, 1));
        MazeEdge edge = src.edgeInDirection(Direction.RIGHT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.RIGHT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][1], map.elevations()[2][1]),
                edge.weight());

        // 2,1 -> 1,1 (Left)
        src = vertices.get(new IPair(2, 1));
        dst = vertices.get(new IPair(1, 1));
        edge = src.edgeInDirection(Direction.LEFT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.LEFT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][1], map.elevations()[1][1]),
                edge.weight());

        // 1,1 -> 1,2 (Down)
        src = vertices.get(new IPair(1, 1));
        dst = vertices.get(new IPair(1, 2));
        edge = src.edgeInDirection(Direction.DOWN);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.DOWN, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][1], map.elevations()[1][2]),
                edge.weight());

        // 1,2 -> 1,1 (Up)
        src = vertices.get(new IPair(1, 2));
        dst = vertices.get(new IPair(1, 1));
        edge = src.edgeInDirection(Direction.UP);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.UP, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][2], map.elevations()[1][1]),
                edge.weight());

        // 2,1 -> 2,2 (Down)
        src = vertices.get(new IPair(2, 1));
        dst = vertices.get(new IPair(2, 2));
        edge = src.edgeInDirection(Direction.DOWN);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.DOWN, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][1], map.elevations()[2][2]),
                edge.weight());

        // 2,2 -> 2,1 (Up)
        src = vertices.get(new IPair(2, 2));
        dst = vertices.get(new IPair(2, 1));
        edge = src.edgeInDirection(Direction.UP);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.UP, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][2], map.elevations()[2][1]),
                edge.weight());

        // 1,2 -> 2,2 (Right)
        src = vertices.get(new IPair(1, 2));
        dst = vertices.get(new IPair(2, 2));
        edge = src.edgeInDirection(Direction.RIGHT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.RIGHT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][2], map.elevations()[2][2]),
                edge.weight());

        // 2,2 -> 1,2 (Left)
        src = vertices.get(new IPair(2, 2));
        dst = vertices.get(new IPair(1, 2));
        edge = src.edgeInDirection(Direction.LEFT);
        assertNotNull(edge);
        assertEquals(src, edge.src());
        assertEquals(dst, edge.dst());
        assertEquals(Direction.LEFT, edge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[2][2], map.elevations()[1][2]),
                edge.weight());
    }


    @DisplayName("WHEN a GameMap has path tiles at top and bottom of the same column THEN creates vertical tunnel edges")
    @Test
    void testVerticalTunnelCreation() {
        // 3x5 map with paths only at top and bottom of the same column
        GameMap map = createMap("""
                wpwww //Path at (1,0)
                wwwww
                wwwww
                wwwww
                wpwww // Path at (1,4) - should tunnel to (1,0)
                """);

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // Should only create vertices for the two path tiles
        assertEquals(2, vertices.size());

        IPair topPos = new IPair(1, 0);  // The path at (1, 0)
        IPair bottomPos = new IPair(1, 4); // The path at (1, 4)
        MazeVertex top = vertices.get(topPos);
        MazeVertex bottom = vertices.get(bottomPos);

        // Verify tunnel edge from top to bottom (UP direction)
        MazeEdge downEdge = top.edgeInDirection(Direction.UP);  // Tunnel up from top to bottom
        assertNotNull(downEdge);
        assertEquals(top, downEdge.src());
        assertEquals(bottom, downEdge.dst());
        assertEquals(Direction.UP, downEdge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][1], map.elevations()[1][4]),
                downEdge.weight());

        // Verify return tunnel from bottom to top (DOWN direction)
        MazeEdge upEdge = bottom.edgeInDirection(Direction.DOWN);  // Tunnel down from bottom to top
        assertNotNull(upEdge);
        assertEquals(bottom, upEdge.src());
        assertEquals(top, upEdge.dst());
        assertEquals(Direction.DOWN, upEdge.direction());
        assertEquals(MazeGraph.edgeWeight(map.elevations()[1][4], map.elevations()[1][1]),
                upEdge.weight());
    }

    @DisplayName("WHEN a GameMap has diagonal path tiles THEN creates no orthogonal connections")
    @Test
    void testDiagonalPathsNoConnections() {
        GameMap map = createMap("""
                www
                wpw
                wwp""");

        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(2, vertices.size());

        IPair first = new IPair(1, 1);  // Path at (1, 1)
        IPair second = new IPair(2, 2); // Path at (2, 2)
        MazeVertex v1 = vertices.get(first);
        MazeVertex v2 = vertices.get(second);

        // Check for no orthogonal connections for v1 (1,1)
        assertNull(v1.edgeInDirection(Direction.LEFT));
        assertNull(v1.edgeInDirection(Direction.RIGHT));
        assertNull(v1.edgeInDirection(Direction.UP));
        assertNull(v1.edgeInDirection(Direction.DOWN));

        // Check for no orthogonal connections for v2 (2,2)
        assertNull(v2.edgeInDirection(Direction.LEFT));
        assertNull(v2.edgeInDirection(Direction.RIGHT));
        assertNull(v2.edgeInDirection(Direction.UP));
        assertNull(v2.edgeInDirection(Direction.DOWN));
    }
}
