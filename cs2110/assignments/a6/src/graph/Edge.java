package graph;

/**
 * Represents a weighted, directed edge between two vertices of type `VertexType` in a graph.
 */
public interface Edge<VertexType> {

    /**
     * Return the vertex that this edge leaves from (the "source" vertex).
     */
    VertexType src();

    /**
     * Return the vertex that this edge leads to (the "destination" vertex).
     */
    VertexType dst();

    /**
     * Return the weight of this edge.
     */
    double weight();
}
