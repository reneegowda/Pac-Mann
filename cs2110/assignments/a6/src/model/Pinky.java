package model;

import model.MazeGraph.Direction;
import model.MazeGraph.MazeVertex;

/**
 * Represents Pinky, the pink ghost in the PacMann game.
 * <p>
 * Pinky behaves as follows: - In the CHASE state, Pinky targets a tile 3 units ahead of PacMann in
 * the direction PacMann is currently facing. If the direction is unavailable, it defaults to facing
 * RIGHT. - In the FLEE state, Pinky targets a fixed location near the northeast corner of the maze
 * (specifically at (width - 3, 2)).
 * <p>
 * Pinky has an initial movement delay of 4 seconds.
 */
public class Pinky extends Ghost {

    /**
     * Creates a new instance of Pinky.
     *
     * @param model the game model that provides access to the maze and PacMann.
     */
    public Pinky(GameModel model) {
        super(model, java.awt.Color.PINK, 4);
    }

    /**
     * Determines Pinky's target based on its current state.
     *
     * @return the vertex Pinky is currently targeting: - If in CHASE state, the vertex 3 tiles
     * ahead of PacMann in the current direction of movement. - If in FLEE state, the vertex near
     * the northeast corner of the maze at position (width - 3, 2).
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            MazeVertex pacVertex = model.pacMann().nearestVertex();
            Direction facing = model.playerCommand();

            // Default direction is RIGHT if unavailable
            if (facing == null) {
                facing = Direction.RIGHT;
            }

            int targetX = pacVertex.loc().i();
            int targetY = pacVertex.loc().j();

            switch (facing) {
                case UP -> targetY -= 3;
                case DOWN -> targetY += 3;
                case LEFT -> targetX -= 3;
                case RIGHT -> targetX += 3;
            }

            return model.graph().closestTo(targetX, targetY);
        } else {
            return model.graph().closestTo(model.width() - 3, 2);
        }
    }
}
