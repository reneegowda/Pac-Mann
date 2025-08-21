package model;

import java.util.List;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;

/**
 * The yellow protagonist of the game who seeks to eat the dots in the board maze while evading
 * capture by the ghosts. Subclasses are responsible to PacMann's navigation, whether this is from
 * user inputs or an AI.
 */
public abstract class PacMann extends Actor {

    /**
     * Construct a PacMann character associated to the given `model`.
     */
    public PacMann(GameModel model) {
        // Remember, since our Actor superclass doesn't define a default constructor, we need a
        // constructor here that propagates the `model` argument upward to the Actor constructor.
        super(model);
        reset();
    }

    /**
     * Alert the model that PacMann has arrived at a vertex so it can update its state accordingly.
     */
    @Override
    public void visitVertex(MazeVertex v) {
        model.processPacMannArrival();
    }

    /**
     * Set PacMann's current location to the end of his "starting edge" returned by the
     * `pacMannStartingEdge()` method in the `MazeGraph` class.
     */
    @Override
    public void reset() {
        // Starting at the end allows PacMann to begin the game walking along any outgoing edge
        // from its `dst` vertex.
        location = new Location(model.graph().pacMannStartingEdge(), 1);
    }

    /**
     * Default to the single-edge path that PacMann is currently traversing.
     */
    @Override
    public List<MazeEdge> guidancePath() {
        // Note: If you attempt the challenge extension, you may wish to override this in
        // PacMannAI to help visualize the behavior of your AI as you develop it.
        return List.of(location.edge());
    }

    @Override
    public double baseSpeed() {
        return 1.0 / 200.0;
    }
}
