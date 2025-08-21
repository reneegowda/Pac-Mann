package ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import ui.InteractiveGameController.GameState;

/**
 * A button that facilitates starting and pausing an interactive game of PacMann.  The text of the
 * button will reflect the next action (play vs. pause) that will be performed, and the button will
 * be disabled (with appropriate text) when the game has ended.
 */
public class PlayPauseButton extends JButton implements PropertyChangeListener {

    /**
     * The interaction controller whose state determines this button's text and enabled status and
     * who will receive commands from this button.
     */
    private InteractiveGameController controller;

    /**
     * Create a new button providing an interface to `controller`.
     */
    public PlayPauseButton(InteractiveGameController controller) {
        super("Play / Pause");

        // Don't request focus when clicked; this allows the game board to keep receiving user input
        setRequestFocusEnabled(false);

        // Customize appearance
        setFont(getFont().deriveFont(20.0f));

        // When activated, toggle our current interaction controller
        addActionListener(e -> {
            if (controller != null) {
                controller.processStartPause();
            }
        });

        setController(controller);
    }

    /**
     * Connect this button to `newController` instead of any previous interaction controller.
     */
    public void setController(InteractiveGameController newController) {
        if (controller != null) {
            controller.removePropertyChangeListener("game_state", this);
        }
        controller = newController;
        if (controller != null) {
            controller.addPropertyChangeListener("game_state", this);
            reflectState(controller.state());
        }
    }

    /**
     * Update our text and enabled status to reflect the action that we will perform when the
     * current interaction state is `state`.
     */
    private void reflectState(GameState state) {
        switch (state) {
            case GameState.PAUSED -> {
                setEnabled(true);
                setText("Play");
            }
            case GameState.RUNNING -> {
                setEnabled(true);
                setText("Pause");
            }
            case GameState.LIFESTART -> {
                setEnabled(true);
                setText("Start");
            }
            case GameState.GAMEOVER -> {
                setEnabled(false);
                setText("Game Over");
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("game_state".equals(evt.getPropertyName())) {
            reflectState((GameState) evt.getNewValue());
        }
    }
}
