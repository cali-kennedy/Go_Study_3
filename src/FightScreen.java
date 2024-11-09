import javax.swing.*;
import java.awt.*;

/**
 * FightScreen class represents a dialog where the player engages in a fight.
 * It displays health indicators for both the player and the enemy,
 * provides an attack option, and manages the enemy's animation during the fight.
 */
public class FightScreen extends JDialog {

    // Constants for health and attack values
    private static final int ENEMY_MAX_HEALTH = 100;
    private static final int PLAYER_ATTACK_DAMAGE = 20;
    private static final int ENEMY_ATTACK_DAMAGE = 15;
    private static final int ANIMATION_REFRESH_RATE_MS = 100;

    // Player and enemy attributes
    private final Character player;
    private int enemyHealth;

    // UI components for health display and attack functionality
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JButton attackButton;

    // External components for rendering and collision detection
    private final CollisionDetector collisionDetector;
    private final TmxRenderer tmxRenderer;

    // Timer for updating enemy animation
    private Timer animationTimer;

    /**
     * Constructor initializes the FightScreen dialog with specified parameters and starts the animation.
     *
     * @param parent            The parent JFrame for this modal dialog.
     * @param player            The player's Character object with player-specific stats and actions.
     * @param collisionDetector CollisionDetector instance for managing and retrieving enemy data.
     * @param tmxRenderer       Renderer instance for displaying the enemy's animation.
     */
    public FightScreen(JFrame parent, Character player, CollisionDetector collisionDetector, TmxRenderer tmxRenderer) {
        super(parent, "Fight Screen", true);
        this.player = player;
        this.enemyHealth = ENEMY_MAX_HEALTH;
        this.collisionDetector = collisionDetector;
        this.tmxRenderer = tmxRenderer;

        setupUI();
        startAnimationTimer();
    }

    /**
     * Sets up the user interface components for health display, attack button, and animation rendering.
     */
    private void setupUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        add(createHealthPanel(), BorderLayout.NORTH);
        add(createAttackButton(), BorderLayout.SOUTH);
        add(createAnimationPanel(), BorderLayout.CENTER);
    }

    /**
     * Creates a JPanel containing labels to display player and enemy health.
     *
     * @return JPanel with health labels.
     */
    private JPanel createHealthPanel() {
        JPanel healthPanel = new JPanel(new GridLayout(1, 2));
        playerHealthLabel = new JLabel("Player Health: " + player.getHealth());
        enemyHealthLabel = new JLabel("Enemy Health: " + enemyHealth);
        healthPanel.add(playerHealthLabel);
        healthPanel.add(enemyHealthLabel);
        return healthPanel;
    }

    /**
     * Creates and configures the attack button with an action listener
     * that decreases enemy health and checks the fight status.
     *
     * @return Configured JButton for attacking.
     */
    private JButton createAttackButton() {
        attackButton = new JButton("Attack");
        attackButton.addActionListener(e -> handleAttackAction());
        return attackButton;
    }

    /**
     * Creates a JPanel for rendering the enemy animation.
     *
     * @return JPanel for animation display.
     */
    private JPanel createAnimationPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayEnemyAnimation(g);
            }
        };
    }

    /**
     * Executes the player attack action, reducing enemy health and initiating an enemy counterattack.
     * Refreshes the screen to update health displays and animations.
     */
    private void handleAttackAction() {
        attackEnemy(PLAYER_ATTACK_DAMAGE);
        if (enemyHealth > 0) {
            enemyAttack();
        }
        checkFightStatus();
        repaint();  // Refresh the screen to update animations
    }

    /**
     * Reduces the enemy's health by a specified damage amount and updates the health label.
     *
     * @param damage Amount of damage inflicted on the enemy.
     */
    private void attackEnemy(int damage) {
        enemyHealth = Math.max(0, enemyHealth - damage);
        enemyHealthLabel.setText("Enemy Health: " + enemyHealth);
    }

    /**
     * Executes the enemy's attack, reducing the player's health and updating the player's health label.
     */
    private void enemyAttack() {
        player.removeHealth(ENEMY_ATTACK_DAMAGE);
        playerHealthLabel.setText("Player Health: " + player.getHealth());
    }

    /**
     * Checks if the fight has been won or lost and displays the appropriate result.
     * Stops the animation timer and closes the dialog if the fight is over.
     */
    private void checkFightStatus() {
        if (isFightWon()) {
            showFightResult("You have won the fight!", "Victory");
        } else if (player.getHealth() <= 0) {
            showFightResult("You have been defeated!", "Defeat");
        }
    }

    /**
     * Displays the fight result in a dialog and stops the animation.
     *
     * @param message The result message to display.
     * @param title   The title of the result dialog.
     */
    private void showFightResult(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        stopAnimationTimer();
        dispose();
    }

    /**
     * Draws the enemy animation on the fight screen, using the enemy's name to identify the correct animation.
     *
     * @param g The Graphics context for rendering the animation.
     */
    private void displayEnemyAnimation(Graphics g) {
        String enemyName = collisionDetector.getEnemyName();
        if (enemyName != null) {
            tmxRenderer.renderEnemyAnimation(enemyName, 200, 40, g);
        } else {
            System.err.println("No enemy name found for animation rendering.");
        }
    }

    /**
     * Checks if the player has won the fight by reducing enemy health to zero.
     *
     * @return true if the enemy's health is zero or below.
     */
    private boolean isFightWon() {
        return enemyHealth <= 0;
    }

    /**
     * Starts a timer that repeatedly calls repaint to refresh the animation panel at a specified rate.
     */
    private void startAnimationTimer() {
        animationTimer = new Timer(ANIMATION_REFRESH_RATE_MS, e -> repaint());
        animationTimer.start();
    }

    /**
     * Stops the animation timer to prevent further updates after the fight ends.
     */
    private void stopAnimationTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
