/**
 * The classes below provide a simple implementation of the Graph interfaces where edge weights are
 * intrinsic to the Edge objects.  For convenience, a mutable set is used to store each vertex's
 * outgoing edges.  These have been factored out from the version of ShortestPathsTest that was part
 * of the student release code, in order to make them available to other test suites.
 */

package graph;

import java.util.*;

public class SimpleGraph {

    public static class SimpleVertex implements Vertex<SimpleEdge> {

        String label;
        Set<SimpleEdge> outgoingEdges;

        public SimpleVertex(String label) {
            this.label = label;
            outgoingEdges = new HashSet<>();
        }

        public String label() {
            return label;
        }

        @Override
        public Set<SimpleEdge> outgoingEdges() {
            return outgoingEdges;
        }

        @Override
        public boolean equals(Object o) {
            if (getClass() != o.getClass()) {
                return false;
            }
            return ((SimpleVertex) o).label.equals(label);
        }

        @Override
        public int hashCode() {
            return label.hashCode();
        }
    }

    public record SimpleEdge(SimpleVertex src, SimpleVertex dst, double weight) implements
            Edge<SimpleVertex> {

    }

    private final Map<String, SimpleVertex> vertices = new HashMap<>();

    public int vertexCount() {
        return vertices.size();
    }

    public SimpleVertex addVertex(String label) {
        SimpleVertex v = new SimpleVertex(label);
        vertices.put(label, v);
        return v;
    }

    public SimpleVertex getVertex(String label) {
        if (!vertices.containsKey(label)) {
            return addVertex(label);
        }
        return vertices.get(label);
    }

    public SimpleEdge getEdge(SimpleVertex src, SimpleVertex dst) {
        for (SimpleEdge e : src.outgoingEdges()) {
            if (e.dst().equals(dst)) {
                return e;
            }
        }
        throw new NoSuchElementException("Edge not found");
    }

    public void addEdge(SimpleVertex src, SimpleVertex dst, double weight) {
        src.outgoingEdges().add(new SimpleEdge(src, dst, weight));
    }

    public static SimpleGraph fromText(String text) {
        SimpleGraph g = new SimpleGraph();
        Scanner lines = new Scanner(text);
        while (lines.hasNextLine()) {
            // Tokenize line
            String[] tokens = lines.nextLine().trim().split("\\s+");
            if (tokens.length == 0) {
                // Skip blank lines
                continue;
            }
            String startLabel = tokens[0];
            String edgeType = tokens[1];
            String endLabel = tokens[2];
            // If no weight token, default weight is 1
            double weight = (tokens.length > 3) ? Double.parseDouble(tokens[3]) : 1;

            // Look up vertex IDs from labels, adding new vertices as necessary
            SimpleVertex v = g.getVertex(startLabel);
            SimpleVertex w = g.getVertex(endLabel);

            // Add edge(s)
            if ("->".equals(edgeType)) {
                g.addEdge(v, w, weight);
            } else if ("--".equals(edgeType)) {
                g.addEdge(v, w, weight);
                g.addEdge(w, v, weight);
            } else {
                throw new IllegalArgumentException("Unexpected edge type: " + edgeType);
            }
        }
        return g;
    }
}
