package model;

import model.MazeGraph.Direction;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;

/**
 * A manually-controlled implementation of PacMann that responds to player input.
 */
public class PacMannManual extends PacMann {

    /**
     * Constructs a PacMannManual instance.
     *
     * @param model the game model this PacMann belongs to
     */
    public PacMannManual(GameModel model) {
        super(model);  // Call the constructor of the superclass (PacMann)
    }

    /**
     * Determines the next edge for PacMann to traverse based on player input. First tries to move
     * in the direction of the most recent player command. If that's not possible, tries to continue
     * in the current direction. If neither is possible, returns null (stay in place).
     *
     * @return the next edge PacMann will take or null if no valid move exists
     */
    @Override
    public MazeEdge nextEdge() {
        MazeVertex currentVertex = nearestVertex();  // Get PacMann's current vertex
        Direction playerDirection = model.playerCommand();  // Get the most recent player command
        MazeEdge edge;

        // Step 1: Try to move in the direction of the most recent player command
        if (playerDirection != null) {
            edge = currentVertex.edgeInDirection(
                    playerDirection);  // Check if the edge exists in the command direction
            if (edge != null) {
                return edge;  // If an edge is found, return it
            }
        }

        // Step 2: If that fails, try to continue in the same direction PacMann was last moving
        if (location().edge() != null) {
            Direction currentDirection = location().edge()
                    .direction();  // Get current direction from PacMann's location
            edge = currentVertex.edgeInDirection(
                    currentDirection);  // Check if there's an edge in the same direction
            if (edge != null) {
                return edge;  // If valid, return the edge
            }
        }

        // Step 3: If neither direction is valid, return null (PacMann stays in place)
        return null;
    }
}
