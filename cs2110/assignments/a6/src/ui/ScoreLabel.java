package ui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.GameModel;

/**
 * A label that displays the current score and number of lives remaining in a game of PacMann.
 */
public class ScoreLabel extends JLabel implements PropertyChangeListener {

    /**
     * The model whose score and lives remaining we should display.  May be null, in which case no
     * score or lives values are displayed.
     */
    private GameModel model;

    /**
     * Create a new label that will display the state of `model` (may be null).
     */
    public ScoreLabel(GameModel model) {
        super("Score  |  Lives", SwingConstants.CENTER);

        // Customize appearance
        setOpaque(true);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(30.0f));

        setModel(model);
    }

    /**
     * Start displaying the state of `newModel`, instead of any model we may have been displaying
     * before.  Model must publish PropertyChangeEvents for "score" and "lives" properties.
     * `newModel` may be null, in which case no score or lives values are displayed.
     */
    public void setModel(GameModel newModel) {
        // Stop listening to old model
        if (model != null) {
            model.removePropertyChangeListener("score", this);
            model.removePropertyChangeListener("lives", this);
        }

        model = newModel;

        // Start observing new model
        if (model != null) {
            model.addPropertyChangeListener("score", this);
            model.addPropertyChangeListener("lives", this);
        }

        showModelState();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("score".equals(evt.getPropertyName()) ||
                "lives".equals(evt.getPropertyName())) {
            showModelState();
        }
    }

    /**
     * Update our text to display the score and lives remaining in our current model.
     */
    private void showModelState() {
        if (model != null) {
            setText("Score: " + model.score() + "  |  Lives: " + model.numLives());
        } else {
            setText("Score  |  Lives");
        }
    }
}
