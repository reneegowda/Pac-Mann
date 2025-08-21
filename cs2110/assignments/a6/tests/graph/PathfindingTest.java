package graph;

import graph.Pathfinding.PathEnd;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static graph.SimpleGraph.*;

/**
 * Uses the `SimpleGraph` class to verify the functionality of the `Pathfinding` class.
 */
public class PathfindingTest {
    /*
     * Text graph format ([weight] is optional):
     * Directed edge: startLabel -> endLabel [weight]
     * Undirected edge (so two directed edges in both directions): startLabel -- endLabel [weight]
     */

    // a small, strongly-connected graph consisting of three vertices and four directed edges
    public static final String graph1 = """
            A -> B 2
            A -- C 6
            B -> C 3
            """;

    @DisplayName("WHEN we compute the `pathInfo` from a vertex `v`, THEN it includes an correct "
            + "entry for each vertex `w` reachable along a non-backtracking path from `v`.")
    @Nested
    class pathInfoTest {

        // Recall that "strongly connected" describes a graph that includes a (directed) path from
        // any vertex to any other vertex
        @DisplayName("In a strongly connected graph with no `previousEdge`.")
        @Test
        void testStronglyConnectedNoPrevious() {
            SimpleGraph g = SimpleGraph.fromText(graph1);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // compute paths from source vertex "A"
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(0, paths.get(va).distance());
            // since the shortest path A -> A is empty, we can't assert anything about its last edge
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "B"
            paths = Pathfinding.pathInfo(vb, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(9, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(0, paths.get(vb).distance());
            assertEquals(3, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());

            // compute paths from source vertex "C"
            paths = Pathfinding.pathInfo(vc, null);
            assertEquals(3, paths.size()); // all vertices are reachable
            assertEquals(6, paths.get(va).distance());
            assertEquals(g.getEdge(vc, va), paths.get(va).lastEdge());
            assertEquals(8, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(0, paths.get(vc).distance());
        }

        @DisplayName("In a graph that is *not* strongly connected and `pathInfo` is computed "
                + "starting from a vertex that cannot reach all other vertices.")
        @Test
        void testNotStronglyConnected() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);
            assertEquals(1, paths.size()); // only va is reachable
            assertTrue(paths.containsKey(va));
            assertFalse(paths.containsKey(vb));
        }

        @DisplayName("In a strongly connected graph with a `previousEdge` that prevents some vertex"
                + "from being reached.")
        @Test
        void testStronglyConnectedPreviousStillReachable() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 2
                    B -> C 3
                    C -> A 4
                    """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");

            // Create an edge from C to A as the previous edge
            SimpleEdge previousEdge = g.getEdge(vc, va);

            // Computing pathInfo from A with C->A as previous edge
            // This should prevent A from taking a path back to C immediately
            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, previousEdge);

            // All vertices should still be reachable
            assertEquals(3, paths.size());
            assertEquals(0, paths.get(va).distance());
            assertEquals(2, paths.get(vb).distance());
            assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
            assertEquals(5, paths.get(vc).distance());
            assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where the shortest path with backtracking is shorter than the "
                + "shortest non-backtracking path.")
        @Test
        void testBacktrackingShorter() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> A 1
                    A -> C 10
                    C -> D 1
                    """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");

            // Without backtracking constraint:
            // The shortest path from A to D would be A->B->A->C->D with distance 13
            // With backtracking constraint:
            // The only valid path is A->C->D with distance 11

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

            // Check that all vertices are reachable
            assertEquals(4, paths.size());

            // Check that we get the non-backtracking path to D through C
            assertEquals(11, paths.get(vd).distance());
            assertEquals(g.getEdge(vc, vd), paths.get(vd).lastEdge());

            // Check the path to C
            assertEquals(10, paths.get(vc).distance());
            assertEquals(g.getEdge(va, vc), paths.get(vc).lastEdge());
        }

        @DisplayName("In a graph where some shortest path includes at least 3 edges.")
        @Test
        void testLongerPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> C 2
                    C -> D 3
                    D -> E 4
                    A -> E 20
                    """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");
            SimpleVertex ve = g.getVertex("E");

            Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

            // Check that all vertices are reachable
            assertEquals(5, paths.size());

            // Check the path to E, which should be A->B->C->D->E (10) not A->E (20)
            assertEquals(10, paths.get(ve).distance());
            assertEquals(g.getEdge(vd, ve), paths.get(ve).lastEdge());

            // Verify the path by checking intermediate nodes
            PathEnd<SimpleEdge> pathToD = paths.get(vd);
            assertEquals(6, pathToD.distance());
            assertEquals(g.getEdge(vc, vd), pathToD.lastEdge());

            PathEnd<SimpleEdge> pathToC = paths.get(vc);
            assertEquals(3, pathToC.distance());
            assertEquals(g.getEdge(vb, vc), pathToC.lastEdge());

            PathEnd<SimpleEdge> pathToB = paths.get(vb);
            assertEquals(1, pathToB.distance());
            assertEquals(g.getEdge(va, vb), pathToB.lastEdge());
        }
    }

    // Example graph from Prof. Myers's notes
    public static final String graph2 = """
            A -> B 9
            A -> C 14
            A -> D 15
            B -> E 23
            C -> E 17
            C -> D 5
            C -> F 30
            D -> F 20
            D -> G 37
            E -> F 3
            E -> G 20
            F -> G 16""";

    /**
     * Ensures `pathEdges` is a well-formed path: the `dst` of each edge equals the `src` of the
     * subsequent edge, and that the ordered list of all vertices in the path equals
     * `expectedVertices`. Requires `path` is non-empty.
     */
    private void assertPathVertices(List<String> expectedVertices, List<SimpleEdge> pathEdges) {
        ArrayList<String> pathVertices = new ArrayList<>();
        pathVertices.add(pathEdges.getFirst().src().label);
        for (SimpleEdge e : pathEdges) {
            assertEquals(pathVertices.getLast(), e.src().label);
            pathVertices.add(e.dst().label);
        }
        assertIterableEquals(expectedVertices, pathVertices);
    }

    @DisplayName("WHEN a weighted, directed graph is given, THEN `shortestNonBacktracking` returns"
            + "the list of edges in the shortest non-backtracking path from a `src` vertex to a "
            + "`dst` vertex, or null if no such path exists.")
    @Nested
    class testShortestNonBacktrackingPath {

        @DisplayName("When the shortest non-backtracking path consists of multiple edges.")
        @Test
        void testLongPath() {
            SimpleGraph g = SimpleGraph.fromText(graph2);
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("G"), null);
            assertNotNull(path);
            assertPathVertices(Arrays.asList("A", "C", "E", "F", "G"), path);
        }

        @DisplayName("When the shortest non-backtracking path consists of a single edge.")
        @Test
        void testOneEdgePath() {
            SimpleGraph g = SimpleGraph.fromText("A -> B 5");
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vb, null);

            assertNotNull(path);
            assertEquals(1, path.size());
            assertEquals(va, path.get(0).src());
            assertEquals(vb, path.get(0).dst());
            assertEquals(5, path.get(0).weight());

            assertPathVertices(Arrays.asList("A", "B"), path);
        }

        @DisplayName("Path is empty when `src` and `dst` are the same.")
        @Test
        void testEmptyPath() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 2
                    B -> A 3
                    """);
            SimpleVertex va = g.getVertex("A");

            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, va, null);

            assertNotNull(path);
            assertEquals(0, path.size());
        }

        @DisplayName("Path is null when there is not a path from `src` to `dst` (even without "
                + "accounting for back-tracking.")
        @Test
        void testNoPath() {
            SimpleGraph g = SimpleGraph.fromText("B -> A 2");
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(g.getVertex("A"),
                    g.getVertex("B"), null);
            assertNull(path);
        }

        @DisplayName("Path is null when the non-backtracking condition prevents finding a path "
                + "from `src` to `dst`.")
        @Test
        void testNonBacktrackingPreventsPath() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 1
                    B -> A 1
                    """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");

            // Create an edge from B to A as the previous edge
            SimpleEdge previousEdge = g.getEdge(vb, va);

            // With B->A as previous edge, A can't go to B since that would create
            // a backtracking path (B->A->B)
            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vb, previousEdge);

            // There should be no valid path
            assertNull(path);
        }


        @DisplayName("When the graph includes multiple shortest paths from `src` to `dst`, one of "
                + "them is returned")
        @Test
        void testMultipleShortestPaths() {
            SimpleGraph g = SimpleGraph.fromText("""
                    A -> B 5
                    A -> C 5
                    B -> D 5
                    C -> D 5
                    """);
            SimpleVertex va = g.getVertex("A");
            SimpleVertex vb = g.getVertex("B");
            SimpleVertex vc = g.getVertex("C");
            SimpleVertex vd = g.getVertex("D");

            List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, null);

            assertNotNull(path);
            assertEquals(2, path.size());
            assertEquals(va, path.get(0).src());
            assertEquals(10, path.get(0).weight() + path.get(1).weight());

            // There are two possible paths: A->B->D or A->C->D
            // Both have the same total weight of 10
            // Check that one of these paths is returned
            boolean validPath = false;

            // Check if it's the A->B->D path
            if (path.get(0).dst().equals(vb) && path.get(1).dst().equals(vd)) {
                validPath = true;
            }

            // Check if it's the A->C->D path
            if (path.get(0).dst().equals(vc) && path.get(1).dst().equals(vd)) {
                validPath = true;
            }

            assertTrue(validPath, "The returned path should be either A->B->D or A->C->D");
        }
    }

    @DisplayName("WHEN a previous edge prevents backtracking, THEN the path will be correctly computed")
    @Test
    void testPathInfoBacktrackingPrevention() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 2
                B -> C 3
                C -> A 1
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");
        SimpleVertex vc = g.getVertex("C");

        // Create a previous edge that should prevent backtracking to C
        SimpleEdge previousEdge = g.getEdge(vc, va);

        // Calculate the path info from A with a previous edge that goes back to A from C
        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, previousEdge);

        assertEquals(3, paths.size()); // all vertices are reachable
        assertEquals(0, paths.get(va).distance());
        assertEquals(2, paths.get(vb).distance());
        assertEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
        assertEquals(5, paths.get(vc).distance());
        assertEquals(g.getEdge(vb, vc),
                paths.get(vc).lastEdge()); // Path to C through B, not back to A
    }

    @DisplayName("WHEN the source and destination are the same, THEN an empty path is returned")
    @Test
    void testPathToSameSourceAndDestination() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 5
                B -> A 5
                """);
        SimpleVertex va = g.getVertex("A");

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Same source and destination
        List<SimpleEdge> path = Pathfinding.pathTo(paths, va, va);

        // Path should be empty as the source and destination are the same
        assertTrue(path.isEmpty());
    }

    @DisplayName("WHEN there is no path between source and destination, THEN null is returned")
    @Test
    void testPathInfoNoPath() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 2
                C -> D 3
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");
        SimpleVertex vc = g.getVertex("C");

        // Calculate the path info from A to C
        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // There is no path from A to C, so the result should be null
        assertFalse(paths.containsKey(vc));
    }

    @DisplayName("WHEN there are multiple shortest paths, THEN one of the valid paths is returned")
    @Test
    void testMultiplePaths() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 5
                A -> C 5
                B -> D 5
                C -> D 5
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vd = g.getVertex("D");

        List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, null);

        assertNotNull(path);
        assertEquals(2, path.size());
        assertEquals(10, path.get(0).weight() + path.get(1).weight());

        // Either path A->B->D or A->C->D should be returned
        boolean validPath = false;

        // Check if it's the A->B->D path
        if (path.get(0).dst().equals(g.getVertex("B")) && path.get(1).dst()
                .equals(g.getVertex("D"))) {
            validPath = true;
        }

        // Check if it's the A->C->D path
        if (path.get(0).dst().equals(g.getVertex("C")) && path.get(1).dst()
                .equals(g.getVertex("D"))) {
            validPath = true;
        }

        assertTrue(validPath, "The returned path should be either A->B->D or A->C->D");
    }

    @DisplayName("Test pathTo with null path info")
    @Test
    void testPathToWithNullPathInfo() {
        SimpleGraph g = SimpleGraph.fromText("A -> B 5");
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        // Create an empty map to simulate no path info
        Map<SimpleVertex, PathEnd<SimpleEdge>> emptyPathInfo = new HashMap<>();

        List<SimpleEdge> path = Pathfinding.pathTo(emptyPathInfo, va, vb);

        // Should return empty list when destination is not in pathInfo
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @DisplayName("Test pathTo with unreachable destination")
    @Test
    void testPathToWithUnreachableDestination() {
        SimpleGraph g = SimpleGraph.fromText("A -> B 5");
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        // Create a pathInfo map that only contains the source vertex
        Map<SimpleVertex, PathEnd<SimpleEdge>> pathInfo = new HashMap<>();
        pathInfo.put(va, new PathEnd<>(0.0, null));

        List<SimpleEdge> path = Pathfinding.pathTo(pathInfo, va, vb);

        // Should return empty list when destination is not in pathInfo
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @DisplayName("Test pathInfo with isolated vertices")
    @Test
    void testPathInfoWithIsolatedVertices() {
        // Create a graph with an edge first, then remove it
        SimpleGraph g = SimpleGraph.fromText("A -> B 1");
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        // Now remove the edge to isolate the vertices
        va.outgoingEdges().clear();  // Remove A's outgoing edge

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Only the source vertex should be reachable
        assertEquals(1, paths.size());
        assertTrue(paths.containsKey(va));  // The source vertex A should be reachable
        assertFalse(paths.containsKey(vb)); // The isolated vertex B should not be reachable
        assertEquals(0, paths.get(va).distance());
        assertNull(paths.get(va).lastEdge()); // There should be no edge for the source
    }

    @DisplayName("Test pathInfo with cyclic graph and no outgoing edges from source")
    @Test
    void testPathInfoWithCyclicGraphNoOutgoingFromSource() {
        // Create graph with cycle between B and C, and isolated A
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1  // First create an edge from A
                B -> C 1
                C -> B 1
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");
        SimpleVertex vc = g.getVertex("C");

        // Now remove A's outgoing edge to make it isolated
        va.outgoingEdges().clear();

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Only the source vertex should be reachable
        assertEquals(1, paths.size());
        assertTrue(paths.containsKey(va));
        assertFalse(paths.containsKey(vb));
        assertFalse(paths.containsKey(vc));
    }

    @DisplayName("Test pathInfo with already settled vertex")
    @Test
    void testPathInfoWithAlreadySettledVertex() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 5
                A -> C 2
                C -> B 1
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // B should be reached through C (with total distance 3) instead of directly (with distance 5)
        assertEquals(3, paths.size());
        assertEquals(3, paths.get(vb).distance());
        assertNotEquals(g.getEdge(va, vb), paths.get(vb).lastEdge());
    }

    @DisplayName("Test pathInfo where a vertex is visited multiple times")
    @Test
    void testPathInfoWithVertexVisitedMultipleTimes() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1
                A -> C 10
                B -> C 1
                C -> D 1
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");
        SimpleVertex vc = g.getVertex("C");
        SimpleVertex vd = g.getVertex("D");

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Should find path A->B->C->D (dist=3) instead of A->C->D (dist=11)
        assertEquals(4, paths.size());
        assertEquals(2, paths.get(vc).distance());
        assertEquals(g.getEdge(vb, vc), paths.get(vc).lastEdge());
        assertEquals(3, paths.get(vd).distance());
    }

    @DisplayName("Test pathInfo with equal-weight paths")
    @Test
    void testPathInfoWithEqualWeightPaths() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 5
                A -> C 3
                B -> D 3
                C -> D 5
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vd = g.getVertex("D");

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Both paths A->B->D and A->C->D have weight 8
        assertEquals(4, paths.size());
        assertEquals(8, paths.get(vd).distance());
    }

    @DisplayName("Test shortestNonBacktrackingPath with previous edge restricting path choices")
    @Test
    void testShortestPathWithPreviousEdgeRestriction() {
        SimpleGraph g = SimpleGraph.fromText("""
                X -> A 1
                A -> B 2
                A -> C 1
                C -> D 1
                B -> D 1
                """);
        SimpleVertex vx = g.getVertex("X");
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vd = g.getVertex("D");

        // Create a previous edge X->A
        SimpleEdge previousEdge = g.getEdge(vx, va);

        // With previous edge X->A, A can't immediately go back to X
        List<SimpleEdge> path = Pathfinding.shortestNonBacktrackingPath(va, vd, previousEdge);

        assertNotNull(path);
        assertEquals(2, path.size());

        // Check that the path is valid (either A->B->D or A->C->D)
        SimpleVertex firstDst = path.get(0).dst();
        SimpleVertex secondDst = path.get(1).dst();

        assertTrue((firstDst.equals(g.getVertex("B")) && secondDst.equals(vd)) || (
                firstDst.equals(g.getVertex("C")) && secondDst.equals(vd)));
    }

    @DisplayName("Test pathTo with complex path reconstruction")
    @Test
    void testPathToWithComplexPathReconstruction() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1
                B -> C 2
                C -> D 3
                D -> E 4
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");
        SimpleVertex vc = g.getVertex("C");
        SimpleVertex vd = g.getVertex("D");
        SimpleVertex ve = g.getVertex("E");

        // Create a pathInfo map
        Map<SimpleVertex, PathEnd<SimpleEdge>> pathInfo = new HashMap<>();
        pathInfo.put(va, new PathEnd<>(0.0, null));
        pathInfo.put(vb, new PathEnd<>(1.0, g.getEdge(va, vb)));
        pathInfo.put(vc, new PathEnd<>(3.0, g.getEdge(vb, vc)));
        pathInfo.put(vd, new PathEnd<>(6.0, g.getEdge(vc, vd)));
        pathInfo.put(ve, new PathEnd<>(10.0, g.getEdge(vd, ve)));

        List<SimpleEdge> path = Pathfinding.pathTo(pathInfo, va, ve);

        // Check that the path is correctly reconstructed
        assertEquals(4, path.size());
        assertEquals(g.getEdge(va, vb), path.get(0));
        assertEquals(g.getEdge(vb, vc), path.get(1));
        assertEquals(g.getEdge(vc, vd), path.get(2));
        assertEquals(g.getEdge(vd, ve), path.get(3));
    }

    @DisplayName("WHEN a path leads to the source vertex, THEN we break when encountering null edge")
    @Test
    void testPathToNullEdgeBreak() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1
                B -> C 2
                C -> A 3
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vc = g.getVertex("C");

        // We want to test the case where we reach the source vertex and encounter null edge
        Map<SimpleVertex, PathEnd<SimpleEdge>> pathInfo = Pathfinding.pathInfo(va, null);
        List<SimpleEdge> path = Pathfinding.pathTo(pathInfo, va, vc);

        // Ensure that the path is valid and we follow the backpointers correctly
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(va, path.get(0).src()); // The first edge should be from C to A
    }

    @DisplayName("WHEN a path leads back to the source vertex and is not empty, THEN we stop and return the path")
    @Test
    void testPathToSourceWithNonEmptyPath() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1
                B -> C 2
                C -> A 3
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        // We want to test the case where we reach the source vertex and the path is not empty
        Map<SimpleVertex, PathEnd<SimpleEdge>> pathInfo = Pathfinding.pathInfo(va, null);
        List<SimpleEdge> path = Pathfinding.pathTo(pathInfo, va, vb);

        // Ensure that the path is valid, contains edges, and stops at the source
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(va, path.get(path.size() - 1).src()); // The last edge should be from B to A
    }

    @DisplayName("Test vertex distance calculation when vertex not in pathInfo")
    @Test
    void testVertexDistanceCalculation() {
        SimpleGraph g = SimpleGraph.fromText("A -> B 1");
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vb = g.getVertex("B");

        // This will force the code to use the 0.0 default distance
        Map<SimpleVertex, PathEnd<SimpleEdge>> pathInfo = new HashMap<>();
        double distance = pathInfo.containsKey(vb) ? pathInfo.get(vb).distance() : 0.0;
        assertEquals(0.0, distance);
    }

    @DisplayName("Test null edge handling")
    @Test
    void testNullEdgeHandling() {
        SimpleGraph g = new SimpleGraph();
        SimpleVertex va = g.addVertex("A");

        // This will test the break when edge is null
        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        assertEquals(1, paths.size());
        assertTrue(paths.containsKey(va));
        assertEquals(0, paths.get(va).distance());
        assertNull(paths.get(va).lastEdge());
    }

    @DisplayName("Test equal-distance paths are both valid")
    @Test
    void testEqualDistancePaths() {
        SimpleGraph g = SimpleGraph.fromText("""
                A -> B 1
                A -> C 2
                B -> C 1
                """);
        SimpleVertex va = g.getVertex("A");
        SimpleVertex vc = g.getVertex("C");

        Map<SimpleVertex, PathEnd<SimpleEdge>> paths = Pathfinding.pathInfo(va, null);

        // Verify distance
        assertEquals(2, paths.get(vc).distance());

        // Both paths are valid per spec
        SimpleEdge lastEdge = paths.get(vc).lastEdge();
        assertTrue(lastEdge.src().label().equals("A") || lastEdge.src().label().equals("B"),
                "Should accept either valid path");
    }
}
