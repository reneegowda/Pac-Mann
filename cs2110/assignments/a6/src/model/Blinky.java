package model;

import model.MazeGraph.MazeVertex;

/**
 * Represents Blinky, the red ghost in the PacMann game.
 * <p>
 * Blinky has the following behavior: - In the CHASE state, Blinky targets the vertex closest to
 * PacMann. - In the FLEE state, Blinky targets the fixed location at coordinates (2, 2), typically
 * the northwest corner of the maze.
 * <p>
 * Blinky starts moving after an initial delay of 2 seconds.
 */
public class Blinky extends Ghost {

    /**
     * Creates a new instance of Blinky.
     *
     * @param model the game model that provides access to the maze and PacMann.
     */
    public Blinky(GameModel model) {
        super(model, java.awt.Color.RED, 2); // initialDelay of 2 seconds
    }

    /**
     * Determines the current target for Blinky based on its state.
     *
     * @return the vertex Blinky is currently targeting: - If in CHASE state, the vertex closest to
     * PacMann. - If in FLEE state, the vertex closest to (2, 2).
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            return model.pacMann().nearestVertex();
        } else {
            return model.graph().closestTo(2, 2);
        }
    }
}
