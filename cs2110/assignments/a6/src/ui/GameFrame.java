package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.GameModel;
import model.GameModel.GameState;
import util.Randomness;

/**
 * A window that allows playing an interactive game of PacMann.  Consists of a score/lives label,
 * buttons for pausing the current game and creating a new game, and a game board view that supports
 * keyboard input.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

    /**
     * The state and logic of the current game being played in this window.
     */
    private GameModel model;

    /**
     * The component for displaying the current game state and responding to user input related to
     * game actions.
     */
    private final GameBoard gameBoard;

    /**
     * The label for showing the score and lives remaining of the current game being played in this
     * window.
     */
    private final ScoreLabel scoreLabel;

    /**
     * The source of reproducible randomness to use when creating new games via this window.
     */
    private Randomness randomness;

    /**
     * The number of columns in maps played in this window.
     */
    private final int width;

    /**
     * The number of rows in maps played in this window.
     */
    private final int height;

    /**
     * Whether the player actor should be controlled by user input or by an AI in games played in
     * this window.
     */
    private final boolean withAI;


    /**
     * Create a new window for playing interactive games of PacMann.  All games will have boards
     * with `height` rows and `width` columns.  If `withAI` is true, the player actor will be
     * controlled by AI; otherwise, it will be controlled by user input.  If `showPaths` is true,
     * then the "guidance paths" of actors will be displayed (for debugging purposes).  `seed`
     * determines the sequence of random values used in map creation and AI logic.
     */
    public GameFrame(int width, int height, boolean withAI, boolean showPaths, long seed) {
        super("PacMann");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Save configuration parameters
        this.width = width;
        this.height = height;
        this.withAI = withAI;
        randomness = new Randomness(seed);

        // Create initial game
        model = GameModel.newGame(width, height, withAI, randomness);
        model.addPropertyChangeListener("game_result", this);

        // Create and arrange widgets
        scoreLabel = new ScoreLabel(model);
        add(scoreLabel, BorderLayout.PAGE_START);

        gameBoard = new GameBoard(model, showPaths);
        add(gameBoard, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Color.BLACK);
        add(buttonPanel, BorderLayout.PAGE_END);

        JButton newGameButton = new JButton("New game");
        newGameButton.setFont(newGameButton.getFont().deriveFont(20.0f));
        newGameButton.setRequestFocusEnabled(false);
        newGameButton.addActionListener(e -> newGame());
        buttonPanel.add(newGameButton);

        JButton playPauseButton = new PlayPauseButton(gameBoard.controller());
        buttonPanel.add(playPauseButton);

        // Give the game board keyboard focus initially
        gameBoard.requestFocusInWindow();

        pack();
    }

    /**
     * Replace the current game with a new game corresponding to the next source of randomness. Map
     * size and player control are determined by our configuration parameter fields.
     */
    private void newGame() {
        randomness = randomness.next();
        setGameModel(GameModel.newGame(width, height, withAI, randomness));
    }

    /**
     * Update all UI widgets to reflect the new game model `newModel` instead of any previous game.
     * The model is expected to publish a "game_result" property.
     */
    private void setGameModel(GameModel newModel) {
        if (model != null) {
            model.removePropertyChangeListener("game_result", this);
        }
        model = newModel;
        if (newModel != null) {
            model.addPropertyChangeListener("game_result", this);
        }
        scoreLabel.setModel(newModel);
        gameBoard.setModel(newModel);
    }

    /**
     * Show a modal dialog indicating that the current game has been won.
     */
    private void showWinMessage() {
        int finalScore = model.score();
        JOptionPane.showMessageDialog(
                this,
                "🎉 Congratulations! You won!\nFinal Score: " + finalScore,
                "Victory!",
                JOptionPane.INFORMATION_MESSAGE
        );

        newGame();
    }

    /**
     * Show a modal dialog indicating that the current game has been lost.
     */
    private void showLoseMessage() {
        int finalScore = model.score();
        JOptionPane.showMessageDialog(
                this,
                "💀 Game Over.\nFinal Score: " + finalScore,
                "Defeat",
                JOptionPane.ERROR_MESSAGE
        );
        newGame();
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("game_result".equals(evt.getPropertyName())) {
            GameState result = (GameState) evt.getNewValue();
            if (result == GameModel.GameState.VICTORY) {
                showWinMessage();
            } else if (result == GameModel.GameState.DEFEAT) {
                showLoseMessage();
            }
        }
    }
}
