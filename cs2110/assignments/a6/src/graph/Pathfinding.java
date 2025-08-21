package graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Pathfinding {

    /**
     * Represents a path ending at `lastEdge.end()` along with its total weight (distance).
     */
    record PathEnd<E extends Edge<?>>(double distance, E lastEdge) {

    }

    /**
     * Returns a list of `E` edges comprising the shortest non-backtracking path from vertex `src`
     * to vertex `dst`. A non-backtracking path never contains two consecutive edges between the
     * same two vertices (e.g., v -> w -> v). As a part of this requirement, the first edge in the
     * returned path cannot back-track `previousEdge` (when `previousEdge` is not null). If there is
     * not a non-backtracking path from `src` to `dst`, then null is returned. Requires that if `E
     * != null` then `previousEdge.dst().equals(src)`.
     */
    public static <V extends Vertex<E>, E extends Edge<V>> List<E> shortestNonBacktrackingPath(
            V src, V dst, E previousEdge) {

        Map<V, PathEnd<E>> paths = pathInfo(src, previousEdge);
        return paths.containsKey(dst) ? pathTo(paths, src, dst) : null;
    }

    /**
     * Returns a map that associates each vertex reachable from `src` along a non-backtracking path
     * with a `PathEnd` object. The `PathEnd` object summarizes relevant information about the
     * shortest non-backtracking path from `src` to that vertex. A non-backtracking path never
     * contains two consecutive edges between the same two vertices (e.g., v -> w -> v). As a part
     * of this requirement, the first edge in the returned path cannot backtrack `previousEdge`
     * (when `previousEdge` is not null). Requires that if `E != null` then
     * `previousEdge.dst().equals(src)`.
     */
    static <V extends Vertex<E>, E extends Edge<V>> Map<V, PathEnd<E>> pathInfo(V src,
            E previousEdge) {

        assert previousEdge == null || previousEdge.dst().equals(src);

        // Associate vertex labels with info about the shortest-known path from `start` to that
        // vertex.  Populated as vertices are discovered (not as they are settled).
        Map<V, PathEnd<E>> pathInfo = new HashMap<>();

        MinPQueue<V> frontier = new MinPQueue<>(); // Min priority queue (frontier for Dijkstra's algorithm)
        Map<V, Double> settled = new HashMap<>(); // Map of settled vertices with their distances

        frontier.addOrUpdate(src, 0.0); // Add the source vertex with a distance of 0
        pathInfo.put(src, new PathEnd<>(0,
                null)); // Path info for the source vertex initialized with 0 distance

        while (!frontier.isEmpty()) {
            V vertex = frontier.remove(); // Get the vertex with the smallest known distance
            double vertexDistance =
                    pathInfo.containsKey(vertex) ? pathInfo.get(vertex).distance() : 0.0;
            settled.put(vertex, vertexDistance); // Mark this vertex as settled

            // Get the last edge that led to this vertex (null for the source vertex)
            E lastEdge = vertex.equals(src) ? null : pathInfo.get(vertex).lastEdge();

            // Explore all outgoing edges from the current vertex
            for (E edge : vertex.outgoingEdges()) {
                V neighbor = edge.dst(); // Neighbor vertex through this edge

                // Skip backtracking if the current edge leads to a backtracking vertex
                if (lastEdge != null && lastEdge.src().equals(neighbor)) {
                    continue;
                }

                // Skip if this is the first edge and it's backtracking the previous edge
                if (vertex.equals(src) && previousEdge != null && previousEdge.src()
                        .equals(neighbor)) {
                    continue;
                }

                // Calculate the new distance to this neighbor
                double newDistance = vertexDistance + edge.weight();

                // If the neighbor is already settled with a shorter path, skip it
                if (settled.containsKey(neighbor) && settled.get(neighbor) <= newDistance) {
                    continue;
                }

                // If we found a shorter path to the neighbor, update the path info and frontier
                if (!pathInfo.containsKey(neighbor)
                        || pathInfo.get(neighbor).distance() > newDistance) {
                    pathInfo.put(neighbor, new PathEnd<>(newDistance, edge));
                    frontier.addOrUpdate(neighbor, newDistance);
                }
            }
        }
        return pathInfo; // Return the map of paths to all reachable vertices
    }

    /**
     * Return the list of edges in the shortest non-backtracking path from `src` to `dst`, as
     * summarized by the given `pathInfo` map. Requires `pathInfo` conforms to the specification as
     * documented by the `pathInfo` method; it must contain backpointers for the shortest
     * non-backtracking paths from `src` to all reachable vertices.
     */
    static <V, E extends Edge<V>> List<E> pathTo(Map<V, PathEnd<E>> pathInfo, V src, V dst) {
        // Prefer a linked list for efficient prepend (alternatively, could append, then reverse
        // before returning)
        LinkedList<E> path = new LinkedList<>();

        // If dst is not reachable from src, return an empty path
        if (!pathInfo.containsKey(dst)) {
            return path;
        }

        // Follow backpointers from dst to src
        V current = dst;
        while (!current.equals(src)) {
            PathEnd<E> pathEnd = pathInfo.get(current);
            E edge = pathEnd.lastEdge();

            // If we reached a null edge, that means we are at the source, so break out of the loop
            if (edge == null) {
                break;
            }

            // Add the edge to the beginning of the path (prepend)
            path.addFirst(edge);

            // Move to the previous vertex in the path (source of the current edge)
            current = edge.src();
        }

        // If we reach the source vertex and we haven't added any edges, we return the empty path
        if (current.equals(src) && !path.isEmpty()) {
            return path;
        }

        // If no valid path is found, return an empty path
        return new LinkedList<>();
    }
}
