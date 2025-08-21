package ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;
import javax.swing.event.SwingPropertyChangeSupport;
import model.GameModel;
import model.MazeGraph.Direction;

public class InteractiveGameController implements KeyListener {

    public enum GameState {RUNNING, PAUSED, LIFESTART, GAMEOVER}

    private GameModel model;
    private final Timer timer;
    private GameState state;

    /**
     * Helper object for managing property change notifications.
     */
    protected SwingPropertyChangeSupport propSupport;

    public InteractiveGameController(GameModel model) {
        state = GameState.LIFESTART;
        timer = new Timer(16, e -> nextFrame());

        boolean notifyOnEdt = true;
        propSupport = new SwingPropertyChangeSupport(this, notifyOnEdt);

        setModel(model);
    }

    public void setModel(GameModel newModel) {
        reset();
        model = newModel;
        model.addPropertyChangeListener("game_state", e -> {
            if (model.state() != GameModel.GameState.PLAYING) {
                stopGame();
            }
        });
    }

    private void stopGame() {
        timer.stop();
        setState(model.state() == GameModel.GameState.READY ? GameState.LIFESTART
                : GameState.GAMEOVER);
    }

    private void nextFrame() {
        model.updateActors(16);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // Get the key code from the KeyEvent
        int keyCode = e.getKeyCode();

        // Directional keys (left, right, up, down, and their corresponding WASD keys)
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            model.updatePlayerCommand(Direction.LEFT);  // Set the direction to LEFT
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            model.updatePlayerCommand(Direction.RIGHT);  // Set the direction to RIGHT
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            model.updatePlayerCommand(Direction.UP);  // Set the direction to UP
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            model.updatePlayerCommand(Direction.DOWN);  // Set the direction to DOWN
        }

        // Handle spacebar for starting/pausing the game
        if (keyCode == KeyEvent.VK_SPACE) {
            if (state == GameState.PAUSED) {
                setState(GameState.RUNNING);
                timer.start();
            } else if (state == GameState.RUNNING) {
                timer.stop();
                setState(GameState.PAUSED);
            } else if (state == GameState.LIFESTART) {
                setState(GameState.RUNNING);
                timer.start();
            }
        }

        // If the game is PAUSED or in LIFESTART state, start the game when a directional command is given
        if (state == GameState.PAUSED || state == GameState.LIFESTART) {
            if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A ||
                    keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D ||
                    keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W ||
                    keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
                setState(GameState.RUNNING);  // Start the game
                timer.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

    /**
     * Processes a press of the start/pause button. Toggles between the RUNNING and PAUSED
     * GameStates.
     */
    public void processStartPause() {
        if (state == GameState.PAUSED) {
            setState(GameState.RUNNING);
            timer.start();
        } else if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        } else if (state == GameState.LIFESTART) {
//            model.useLife();
            setState(GameState.RUNNING);
            timer.start();
        }
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            timer.stop();
            setState(GameState.PAUSED);
        }
    }

    public void reset() {
        timer.stop();
        setState(GameState.LIFESTART);
    }

    public GameState state() {
        return state;
    }

    private void setState(GameState newState) {
        GameState oldState = state;
        state = newState;
        propSupport.firePropertyChange("game_state", oldState, state);
    }

    /* Observation interface */

    /**
     * Register `listener` to be notified whenever any property of this model is changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Register `listener` to be notified whenever the property named `propertyName` of this model
     * is changed.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Stop notifying `listener` of property changes for this model (assuming it was added no more
     * than once).  Does not affect listeners who were registered with a particular property name.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    /**
     * Stop notifying `listener` of changes to the property named `propertyName` for this model
     * (assuming it was added no more than once).  Does not affect listeners who were not registered
     * with `propertyName`.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(propertyName, listener);
    }
}
