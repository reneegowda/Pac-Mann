package model;

import model.MazeGraph.MazeVertex;

/**
 * Represents Inky, the cyan ghost in the PacMann game.
 * <p>
 * Inky's behavior is more complex than the other ghosts: - In the CHASE state, Inky targets a
 * position that is a reflection of Blinky's position across PacMann's position. This is calculated
 * as: (2 * PacMann position) - Blinky position. - In the FLEE state, Inky retreats to a fixed
 * location near the southwest corner of the maze at (2, height - 3).
 * <p>
 * Inky begins moving after an initial delay of 6 seconds.
 */
public class Inky extends Ghost {

    /**
     * Creates a new instance of Inky.
     *
     * @param model the game model that provides access to the maze, PacMann, and other ghosts.
     */
    public Inky(GameModel model) {
        super(model, java.awt.Color.CYAN, 6);
    }

    /**
     * Determines Inky's target based on its current state.
     *
     * @return the vertex Inky is currently targeting: - In CHASE state, a position calculated by
     * reflecting Blinky's position across PacMann's position. - In FLEE state, the vertex at (2,
     * height - 3).
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            MazeVertex pacVertex = model.pacMann().nearestVertex();
            MazeVertex blinkyVertex = model.blinky().nearestVertex();

            int targetX = 2 * pacVertex.loc().i() - blinkyVertex.loc().i();
            int targetY = 2 * pacVertex.loc().j() - blinkyVertex.loc().j();

            return model.graph().closestTo(targetX, targetY);
        } else {
            return model.graph().closestTo(2, model.height() - 3);
        }
    }
}
