package model;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeVertex;
import graph.Pathfinding;

// TODO 13-16: Extend this class by defining (non-abstract) subclasses `Blinky`, `Pinky`, `Inky`,
//  and `Clyde`, each in separate files "model/<Ghost name>.java", that model these ghosts unique
//  properties and behaviors as described in the assignment handout.

/**
 * A ghost in the game. This abstract class models the common behaviors and attributes of all ghosts
 * including their appearance and different behavioral states:
 * <p>
 * - WAIT: The ghost is not currently present in the maze, its `delay` specifies how long before it
 * enters.
 * <p>
 * - CHASE: The ghost is actively seeking PacMann. Catching up to PacMann will stop the round.
 * <p>
 * - FLEE: The ghost is running away to its designated location on the board. If PacMann catches up
 * to this ghost, it will leave the board and enter its WAIT state.
 * <p>
 * Subclasses are responsible for specifying the `target()` vertices of that ghost in its CHASE and
 * FLEE states.
 */
public abstract class Ghost extends Actor {

    /**
     * The three possible behavior states of ghosts
     */
    public enum GhostState {WAIT, CHASE, FLEE}

    /**
     * The current behavioral state of this ghost
     */
    protected GhostState state;

    /**
     * The total number of milliseconds before this ghost enters the maze in its CHASE state
     */
    private final int initialDelay;

    /**
     * When in the FLEE state, the number of milliseconds remaining before this ghost returns to its
     * CHASE state
     */
    private double waitTimeRemaining;

    /**
     * When in the FLEE state, the number of milliseconds remaining before this ghost returns to its
     * CHASE state
     */
    protected double fleeTimeRemaining;

    /**
     * The color of this ghost in its CHASE state
     */
    private final Color ghostColor;

    /**
     * The edges comprising the most recently calculated path to this ghosts' `target()`
     */
    private List<MazeEdge> guidancePath;

    /**
     * Construct a ghost associated to the given `model` with specified color and initial delay
     */
    public Ghost(GameModel model, Color ghostColor, int initialDelay) {
        super(model);
        this.ghostColor = ghostColor;
        this.initialDelay = initialDelay;
        guidancePath = List.of(); // initially empty
        reset();
    }

    /* ****************************************************************
     * Abstract Ghost method                                          *
     **************************************************************** */

    /**
     * Return the vertex that this ghost is targeting
     */
    protected abstract MazeVertex target();

    /* ****************************************************************
     * Accessor methods                                               *
     **************************************************************** */

    /**
     * Return the current state of this ghost
     */
    public GhostState state() {
        return state;
    }

    /**
     * Return this ghost's color (for painting)
     */
    public Color color() {
        return ghostColor;
    }

    /**
     * Return the amount of time remaining on this ghost's FLEE timer
     */
    public double fleeTimeRemaining() {
        return fleeTimeRemaining;
    }

    /**
     * Return the amount of time remaining on this ghost's WAIT timer
     */
    public double waitTimeRemaining() {
        return waitTimeRemaining;
    }

    /**
     * In their WAIT state, the ghosts move from side to side in their box, otherwise, their
     * bounding box location is delegated up to `Actor.getBoundingBoxUL`.
     */
    @Override
    public DPair getBoundingBoxUL() {
        if (state == GhostState.WAIT) {
            IPair gStartLoc = location.edge().src().loc();
            double x = gStartLoc.i() + 0.25 + (1.5 * Math.sin(waitTimeRemaining() / 500));
            double y = gStartLoc.j() + 2.75;
            return new DPair(x, y);
        } else {
            return super.getBoundingBoxUL();
        }
    }

    /* ****************************************************************
     * Implemented abstract methods of Actor                          *
     **************************************************************** */

    /**
     * Ghosts do not take any actions when visiting vertices
     */
    @Override
    public void visitVertex(MazeVertex v) {
    }

    /**
     * Returns the first edge along the shortest path from this ghost's `currentVertex()` to its
     * `target()`.
     */
    @Override
    public MazeEdge nextEdge() {
        MazeEdge prevEdge = (location.progress() == 1) ? location.edge() : null;
        guidancePath = Pathfinding.shortestNonBacktrackingPath(nearestVertex(), target(),
                prevEdge);
        return guidancePath == null || guidancePath.isEmpty() ? null : guidancePath.getFirst();
    }

    @Override
    public List<MazeEdge> guidancePath() {
        return Collections.unmodifiableList(guidancePath);
    }

    /**
     * Transition this ghost to its WAIT state and reset the `waitTimeRemaining` to this ghost's
     * `INITIAL_DELAY`
     */
    @Override
    public void reset() {
        state = GhostState.WAIT;
        waitTimeRemaining = initialDelay;
        location = new Location(model.graph().ghostStartingEdge(), 0);
        guidancePath = List.of();
    }

    @Override
    public double baseSpeed() {
        double baseSpeed = 1.0 / 200.0;
        if (state == GhostState.FLEE) {
            return 0.5 * baseSpeed;
        }
        return baseSpeed;
    }

    /* ****************************************************************
     * Additional methods to handle ghost state transitions           *
     **************************************************************** */

    /**
     * Transition this ghost to its FLEE state and reset the `fleeTimeRemaining`. Has no effect if
     * the ghost is in its WAIT state.
     */
    public void startFlee() {
        if (state != GhostState.WAIT) {
            state = GhostState.FLEE;
            fleeTimeRemaining = 8000;

            location = location.reversed();
        }
    }

    /**
     * Transition this ghost to its WAIT state when it is caught in its FLEE state, and resets
     * `waitTimeRemaining`. Requires that this ghost is currently in the FLEE state.
     */
    public void respawn() {
        assert state == GhostState.FLEE;
        state = GhostState.WAIT;
        waitTimeRemaining = 3000;
        fleeTimeRemaining = 0;
        location = new Location(model.graph().ghostStartingEdge(), 0);
    }

    /* ****************************************************************
     * Additional navigation methods                                  *
     **************************************************************** */

    /**
     * Calculate the new position for this actor after `ms` milliseconds of game time have elapsed.
     * In the WAIT state, this updates its delay and spawns the ghost into the maze in its CHASE
     * state when the delay reaches 0. In the FLEE state, the `fleeTimeRemaining` is updated and the
     * ghost reverts to its CHASE state when this reaches 0. Otherwise, delegates to
     * `Actor.updateLocation()`.
     */
    @Override
    public void propagate(double dt) {
        if (state == GhostState.WAIT) {
            waitTimeRemaining -= dt;
            if (waitTimeRemaining > 0) {
                return; // no location update is necessary yet
            }
            state = GhostState.CHASE;
            location = new Location(model.graph().ghostStartingEdge(), 0);
            dt = -1 * waitTimeRemaining;
        }
        if (state == GhostState.FLEE) {
            fleeTimeRemaining -= dt;
            if (fleeTimeRemaining <= 0) {
                state = GhostState.CHASE;
            }
        }

        super.propagate(dt);
    }

    @Override
    public double maxPropagationTime() {
        double ans = super.maxPropagationTime();
        if (state == GhostState.WAIT) {
            return Math.min(ans, waitTimeRemaining());
        } else if (state == GhostState.FLEE) {
            return Math.min(ans, fleeTimeRemaining());
        } else {
            return ans;
        }
    }

}
