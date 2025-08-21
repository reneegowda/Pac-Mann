package graph;

/**
 * Represents a vertex in a graph whose edges have type `EdgeType`.
 */
public interface Vertex<EdgeType extends Edge<?>> {

    /**
     * Return an object supporting iteration over all the edges connecting this vertex to another
     * vertex in the graph.  This vertex serves as the "source" vertex for each such edge.
     */
    Iterable<EdgeType> outgoingEdges();
}
