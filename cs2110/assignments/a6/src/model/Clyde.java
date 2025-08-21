package model;

import model.MazeGraph.MazeVertex;

/**
 * Represents Clyde, the orange ghost in the PacMann game.
 * <p>
 * Clyde has a unique CHASE behavior that depends on distance: - If Clyde is 10 or more units away
 * from PacMann (Euclidean distance), he behaves like Blinky and targets PacMann’s current position.
 * - If Clyde is closer than 10 units, he targets a random location in the maze.
 * <p>
 * In the FLEE state, Clyde always retreats to a fixed location near the southeast corner of the
 * maze at (width - 3, height - 3).
 * <p>
 * Clyde starts moving after an initial delay of 8 seconds.
 */
public class Clyde extends Ghost {

    private final java.util.Random random;

    /**
     * Creates a new instance of Clyde.
     *
     * @param model  the game model containing the maze and actors.
     * @param random a Random object used for generating random targets when close to PacMann.
     */
    public Clyde(GameModel model, java.util.Random random) {
        super(model, java.awt.Color.ORANGE, 8);
        this.random = random;
    }

    /**
     * Determines Clyde's target based on its current state.
     *
     * @return the vertex Clyde is currently targeting: - In CHASE state: - If Clyde is 10 or more
     * units away from PacMann, targets PacMann directly. - If closer than 10 units, targets a
     * random location in the maze. - In FLEE state, targets the corner at (width - 3, height - 3).
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            MazeVertex clydeVertex = nearestVertex();
            MazeVertex pacVertex = model.pacMann().nearestVertex();

            double dx = pacVertex.loc().i() - clydeVertex.loc().i();
            double dy = pacVertex.loc().j() - clydeVertex.loc().j();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance >= 10) {
                return pacVertex;
            } else {
                int targetX = random.nextInt(model.width());
                int targetY = random.nextInt(model.height());
                return model.graph().closestTo(targetX, targetY);
            }
        } else {
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        }
    }
}
