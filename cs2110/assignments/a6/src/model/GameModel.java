package model;

import java.util.HashSet;
import model.MazeGraph.IPair;
import model.MazeGraph.MazeEdge;
import model.MazeGraph.MazeVertex;
import util.ElevationGenerator;
import util.MazeGenerator;
import util.MazeGenerator.TileType;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.SwingPropertyChangeSupport;
import model.Ghost.GhostState;
import model.MazeGraph.Direction;
import util.GameMap;
import util.Randomness;

/**
 * The underlying state representation of a PacMann game, including the game graph and actors.
 */
public class GameModel {

    /**
     * During its lifetime, the game transitions from READY -> PLAYING -> either VICTORY/DEFEAT
     */
    public enum GameState {READY, PLAYING, VICTORY, DEFEAT}

    /**
     * The current state of this model
     */
    private GameState state;

    /**
     * The map associated with this model
     */
    private final GameMap map;

    /**
     * Each path tile contains a dot, contains a pellet, or is empty.
     */
    public enum Item {DOT, PELLET, NONE}

    /**
     * A map from the vertices in the game graph to items positioned on these vertices. `item` only
     * includes the vertices containing DOTs and PELLETs, not NONE.
     */
    private final Map<MazeVertex, Item> items;

    /**
     * The graph representation of the game's maze
     */
    private final MazeGraph graph;

    /**
     * The current score
     */
    private int score;

    /**
     * The amount of time that has elapsed
     */
    private double time;

    /**
     * The number of cells across the maze is
     */
    private final int width;

    /**
     * The number of cells tall the maze is
     */
    private final int height;

    /**
     * The actors in this game, PacMann will be in index 0 and the ghosts will be in indices 1-4
     */
    private final List<Actor> actors;

    /**
     * The number of ghosts that were caught during the current FLEE cycle
     */
    private int numGhostsCaught;

    /**
     * The number of lives remaining
     */
    private int numLives;

    /**
     * The direction associated with the most recent player input. This field tracks the player's
     * intended movement direction.
     */
    private Direction playerDirection;

    /**
     * Updates the player's movement direction based on the most recent input.
     *
     * @param newDirection the new direction the player intends to move
     */
    public void updatePlayerCommand(Direction newDirection) {
        this.playerDirection = newDirection;
    }

    /**
     * Retrieves the most recent movement direction input by the player.
     *
     * @return the current direction the player intends to move
     */
    public Direction playerCommand() {
        return this.playerDirection;
    }

    /**
     * Helper object for managing property change notifications.
     */
    protected SwingPropertyChangeSupport propSupport;

    /* ****************************************************************
     * Game construction                                              *
     **************************************************************** */

    /**
     * Construct a new game model using the given arrays of tile types and elevations
     */
    public GameModel(GameMap map, Randomness randomness, boolean withAI) {
        this.map = map;
        width = map.types().length;
        height = map.types()[0].length;
        graph = new MazeGraph(map);

        items = new HashMap<>();
        placeDotsAndPellets();

        score = 0;
        time = 0;
        numLives = 3;
        state = GameState.READY;

        actors = new ArrayList<>();
        actors.add(new PacMannManual(this));
        //actors.add(withAI ? new PacMannAI(this) : new PacMannManual(this));
        actors.add(new Blinky(this));
        actors.add(new Pinky(this));
        actors.add(new Inky(this));
        actors.add(new Clyde(this, randomness.generatorFor("Clyde")));

        boolean notifyOnEdt = false; // no threads, so false is okay
        propSupport = new SwingPropertyChangeSupport(this, notifyOnEdt);
    }

    /**
     * Static method to construct a GameModel object associated with a new random maze
     */
    public static GameModel newGame(int width, int height, boolean withAI, Randomness randomness) {
        TileType[][] types = new MazeGenerator(width, height,
                randomness.generatorFor("MazeGenerator")).generateMaze();
        int tilesAcross = 3 * width + 2;
        int tilesHigh = 3 * height + 2;
        double[][] elevations = ElevationGenerator.generateElevations(tilesAcross, tilesHigh,
                randomness.generatorFor("ElevationGenerator"));
        return new GameModel(new GameMap(types, elevations), randomness.randomnessFor("GameModel"),
                withAI);
    }

    /**
     * Adds all dots and pellets to `items` during the construction of this game.
     */
    private void placeDotsAndPellets() {
        // build set of pellet locations
        HashSet<IPair> pelletLocs = new HashSet<>();
        for (int i = 5; i < width / 2 - 7; i += 15) {
            for (int j = 5; j < height / 2 - 7; j += 15) {
                pelletLocs.add(new IPair(i, j));
                pelletLocs.add(new IPair(width - 1 - i, j));
                pelletLocs.add(new IPair(i, height - 1 - j));
                pelletLocs.add(new IPair(width - 1 - i, height - 1 - j));
            }
        }

        for (MazeVertex v : graph.vertices()) {
            if (pelletLocs.contains(v.loc())) {
                items.put(v, Item.PELLET);
                continue;
            }

            int i = v.loc().i();
            int j = v.loc().j();

            // place pellets at all interior vertices
            if (i >= 2 && i < width - 2 && j >= 2 && j < height - 2) {
                items.put(v, Item.DOT);
            }
        }
    }

    /* ****************************************************************
     * Accessor methods                                               *
     **************************************************************** */

    /**
     * Return the GameMap that was used to construct this model
     */
    public GameMap map() {
        return map;
    }

    /**
     * Return the width of the maze in this game instance
     */
    public int width() {
        return width;
    }

    /**
     * Return the height of the maze in this game instance
     */
    public int height() {
        return height;
    }

    /**
     * Return a reference to this game's graph
     */
    public MazeGraph graph() {
        return graph;
    }

    /**
     * Return the actors associated with this game instance
     */
    public Iterable<Actor> actors() {
        return Collections.unmodifiableList(actors);
    }

    /**
     * Return a reference to this game instance's PacMann Actor object
     */
    public PacMann pacMann() {
        return (PacMann) actors.getFirst();
    }

    /**
     * Return a reference to this game instance's Blinky Actor object
     */
    public Ghost blinky() {
        return (Ghost) actors.get(1);
    }

    /**
     * Return a reference to this game instance's Pinky Actor object
     */
    public Ghost pinky() {
        return (Ghost) actors.get(2);
    }

    /**
     * Return a reference to this game instance's Inky Actor object
     */
    public Ghost inky() {
        return (Ghost) actors.get(3);
    }

    /**
     * Return a reference to this game instance's Clyde Actor object
     */
    public Ghost clyde() {
        return (Ghost) actors.get(4);
    }

    /**
     * Return the current score
     */
    public int score() {
        return score;
    }

    /**
     * Return the amount of elapsed time
     */
    public double time() {
        return time;
    }

    /**
     * Return the item located at the given Vertex `v`, possibly NONE. This method will never return
     * null.
     */
    public Item itemAt(MazeVertex v) {
        return items.getOrDefault(v, Item.NONE);
    }


    /**
     * Return the current state of this game.
     */
    public GameState state() {
        return state;
    }

    /**
     * Return the number of remaining lives
     */
    public int numLives() {
        return numLives;
    }

    /* ****************************************************************
     * Methods that update the game state                             *
     **************************************************************** */

    /**
     * Increment the current score by `points` points and notify "score" observers.
     */
    private void addToScore(int points) {
        int oldScore = score;
        score += points;
        propSupport.firePropertyChange("score", oldScore, score);
    }

    /**
     * Initiate the FLEE sequence by resetting the number of caught ghosts and transitioning all
     * non-WAITING ghosts to their FLEE state.
     */
    private void startFlee() {
        numGhostsCaught = 0;
        for (int i = 1; i < actors.size(); i++) {
            ((Ghost) actors.get(i)).startFlee();
        }
    }

    /**
     * Update the model to reflect PacMann's arrival at a vertex.
     */
    public void processPacMannArrival() {
        MazeVertex v = pacMann().nearestVertex();

        // Check if there is an item at this vertex
        Item itemAtVertex = itemAt(v);

        if (itemAtVertex == Item.DOT) {
            items.remove(v);
            addToScore(10);
        }

        if (itemAtVertex == Item.PELLET) {
            items.remove(v);
            addToScore(50);
            startFlee();
        }
    }

    /**
     * Transition to `newState` and notify observers.
     */
    private void setState(GameState newState) {
        GameState oldState = state;
        state = newState;
        propSupport.firePropertyChange("game_state", oldState, state);
    }

    /**
     * Resets all ghosts to their WAIT state and PacMann to his starting position, then transitions
     * to the READY state.
     */
    public void resetActors() {
        for (Actor a : actors) {
            a.reset();
        }
        setState(GameState.READY);
    }

    /**
     * Decrease the number of remaining lives by one, notifying "lives" observers.
     */
    public void useLife() {
        int oldLives = numLives;
        numLives -= 1;
        propSupport.firePropertyChange("lives", oldLives, numLives);
    }

    /**
     * Handle the end of the round (i.e., when PacMann has been caught).  Consume a life and reset
     * if at least one life still remains.  Otherwise, transition to the DEFEAT state and notify
     * "game_result" observers.
     */
    private void defeat() {
        assert state == GameState.PLAYING;

        useLife();
        if (numLives > 0) {
            resetActors();
        } else {
            setState(GameState.DEFEAT);
            propSupport.firePropertyChange("game_result", null, GameState.DEFEAT);
        }
    }

    /**
     * Handle a victorious end of the game (i.e., when PacMann has eaten all of the dots and
     * pellets).  Transition to the VICTORY state and notify "game_result" observers.
     */
    private void victory() {
        assert state == GameState.PLAYING;
        setState(GameState.VICTORY);
        propSupport.firePropertyChange("game_result", null, GameState.VICTORY);
    }

    /* ****************************************************************
     * Observational interface                                        *
     **************************************************************** */

    /**
     * Register `listener` to be notified whenever the property named `propertyName` of this model
     * is changed.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Stop notifying `listener` of changes to the property named `propertyName` for this model
     * (assuming it was added no more than once).  Does not affect listeners who were not registered
     * with `propertyName`.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(propertyName, listener);
    }

    /* ****************************************************************
     * Code for managing actor locations : We do not expect you to    *
     * read, understand, or modify the rest of this file              *
     **************************************************************** */

    /**
     * Handle a collision detected between two actors.
     */
    private void processCollision(Actor a1, Actor a2) throws PacMannCaught {
        // Perform runtime type checking to determine whether one of the actors was PacMann.
        //  Alternatively, could compare with our known PacMann actor.
        if (a1 instanceof PacMann) {
            collideWithGhost((Ghost) a2);
        } else if (a2 instanceof PacMann) {
            collideWithGhost((Ghost) a1);
        }

        // Ignore ghost-ghost collisions
    }

    /**
     * Handle a PacMann-Ghost collision.  Score and respawn ghosts if they are fleeing; otherwise,
     * throw `PacMannCaught`.  Fleeing ghosts earn points exponential in the number of ghosts caught
     * since the last pellet was consumed.
     */
    private void collideWithGhost(Ghost g) throws PacMannCaught {
        if (g.state() == GhostState.FLEE) {
            numGhostsCaught += 1;
            addToScore((int) (100 * Math.pow(2, numGhostsCaught)));
            g.respawn();
        } else if (g.state() == GhostState.CHASE) {
            throw new PacMannCaught();
        }
    }


    /**
     * Propagate the game forward in time by `totalDt` ms.  Process all actor collisions and vertex
     * visitations.  Update actors' traversed edges upon reaching a vertex.  Handle round-end and
     * game-end conditions, notifying observers.  Notify "board_state" observers after propagation
     * has concluded.
     */
    public void updateActors(double totalDt) {
        if (state == GameState.READY) {
            setState(GameState.PLAYING);
        }

        try {
            double t = 0;
            while (t < totalDt) {
                navAndGuide();
                double dt = nextDt(totalDt - t);
//                System.err.println("Stepping " + dt + " from " + t + " (goal: " + ms + ")");

                // Propagate actors
                t += dt;
                time += dt;
                for (Actor a : actors) {
                    a.propagate(dt);
                }

                // Check for collisions
                for (int i = 0; i < actors.size(); i += 1) {
                    Actor a = actors.get(i);
                    for (int j = i + 1; j < actors.size(); j += 1) {
                        Actor b = actors.get(j);
                        if (a.location().collidesWith(b.location())) {
                            processCollision(a, b);
                        }
                    }
                }

                // Vertex events
                for (Actor a : actors) {
                    if (a.location().progress() == 1) {
                        a.visitVertex(a.location().edge().dst());
                    }
                }
                // Check for end game condition
                if (items.isEmpty()) {
                    victory();
                    break;
                }
            }
        } catch (PacMannCaught e) {
            defeat();
        }

        propSupport.firePropertyChange("board_state", null, null);
    }

    /**
     * Give any actor currently standing on a vertex an opportunity to decide which edge to traverse
     * next.  Enforces that their next edge starts at their current location.
     */
    private void navAndGuide() {
        for (Actor a : actors) {
            if (a.location().atVertex()) {
                MazeVertex start = a.location().nearestVertex();
                MazeEdge e = a.nextEdge();
                if (e != null) {
                    if (!e.src().equals(start)) {
                        throw new RuntimeException("Illegal next edge");
                    }
                    a.traverseEdge(e);
                }
            }
        }
    }

    /**
     * Return the largest timestep that the engine can propagate the actors by, up to `maxDt`.
     * Timestep is constrained by actor vertex arrivals, actor state changes, and actor collisions.
     * A minimum timestep is imposed to ensure forward progress.
     */
    private double nextDt(double maxDt) {
        double minDt = maxDt;
        for (Actor a : actors) {
            minDt = Math.min(minDt, a.maxPropagationTime());
        }
        minDt = Math.min(minDt, nextCollisionTime());
        final double minAllowedDt = 1e-7;
        return Math.max(minDt, minAllowedDt);
    }

    /**
     * Return the earliest timestep at which two actors will collide, given their current
     * trajectories.  Actors may cross each other along an edge or may meet at a vertex. Returns
     * POSITIVE_INFINITY if no actors will collide along their current edge trajectories.
     */
    private double nextCollisionTime() {
        double minDt = Double.POSITIVE_INFINITY;

        // The current location and velocity of each actor, associated with the _undirected_ edge
        //  they are currently traversing.
        Map<UnorderedPair<MazeVertex>, List<Trajectory>> movements = new HashMap<>();

        for (Actor a : actors) {
            // Determine the unordered edge that `a` is currently traversing, and query any
            //  previously seen actors traversing that same edge (in either direction)
            MazeEdge e = a.location().edge();
            UnorderedPair<MazeVertex> ue = new UnorderedPair<>(e.src(), e.dst());
            List<Trajectory> edgemates = movements.computeIfAbsent(ue,
                    k -> new LinkedList<>());

            // Compute the intersection time between this actor and any already-seen actor
            //  traversing the same edge
            var myAm = Trajectory.fromActor(a);
            for (Trajectory am : edgemates) {
                // Don't trigger if starting from the same place
//                if (myAm.p() == am.p()) { continue; }
                double s = (myAm.p() - am.p()) / (am.v() - myAm.v());
                // Note: inequality skips NaNs
                if (s > 0 && s < minDt) {
                    minDt = s;
                }
            }

            // Add our trajectory to the map, to be seen by subsequent actors
            edgemates.add(myAm);
        }
        return minDt;
    }

    /**
     * The position and velocity of an actor on an associated _undirected_ edge.  RIGHT and DOWN are
     * considered the "positive" directions.
     */
    private record Trajectory(double p, double v) {

        public static Trajectory fromActor(Actor actor) {
            MazeEdge e = actor.location().edge();
            if (e.direction() == Direction.RIGHT || e.direction() == Direction.DOWN) {
                return new Trajectory(actor.location().progress(), actor.edgeSpeed());
            } else {
                return new Trajectory(1 - actor.location().progress(), -actor.edgeSpeed());
            }
        }
    }

    /**
     * Indicates that a collision between PacMann and a CHASING ghost was detected, meaning that the
     * current round should end.
     */
    private static class PacMannCaught extends Exception {

    }

    /**
     * An unordered pair of values of type `T`.  Equality and hash code are independent of the order
     * of constructor parameters or fields.
     */
    private static final class UnorderedPair<T> {

        // elem1.hashCode() <= elem2.hashCode()
        private final T elem1;
        private final T elem2;

        public UnorderedPair(T a, T b) {
            // Impose a preferred ordering to make toString more consistent
            if (a.hashCode() <= b.hashCode()) {
                elem1 = a;
                elem2 = b;
            } else {
                elem1 = b;
                elem2 = a;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof UnorderedPair<?> op) {
                // Still need to check both orderings in case of hash collisions
                return elem1.equals(op.elem1) && elem2.equals(op.elem2) ||
                        elem1.equals(op.elem2) && elem2.equals(op.elem1);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return elem1.hashCode() ^ elem2.hashCode();
        }

        @Override
        public String toString() {
            return "{" + elem1 + "," + elem2 + "}";
        }
    }
}
