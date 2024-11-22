import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * FightScreen class represents a dialog where the player engages in a fight.
 * It displays health indicators for both the player and the enemy,
 * provides an attack option, and manages the enemy's animation during the fight.
 */
public class NPCScreen extends JDialog {

    // Constants for health and attack values
    private static final int ENEMY_MAX_HEALTH = 100;
    private static final int PLAYER_ATTACK_DAMAGE = 20;
    private static final int ENEMY_ATTACK_DAMAGE = 15;
    private static final int ANIMATION_REFRESH_RATE_MS = 100;
    private JLayeredPane layeredPane;
    private JLabel messageLabel;
    private JPanel mainPanel;
    // Player and enemy attributes
    private final Character player;
    private int enemyHealth;

    private boolean questionIsCorrect = false;
    // Player character image
    private BufferedImage playerImage;

    // UI components for health display and attack functionality
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JButton attackButton;

    // External components for rendering and collision detection
    private final CollisionDetector collisionDetector;
    private final TmxRenderer tmxRenderer;
   private String npcName;
    private java.util.List<Question> questions;
    // Timer for updating enemy animation
    private Timer animationTimer;
    private GameQuestionPanel questionPanel;

    /**
     * Constructor initializes the FightScreen dialog with specified parameters and starts the animation.
     *
     * @param parent            The parent JFrame for this modal dialog.
     * @param player            The player's Character object with player-specific stats and actions.
     * @param collisionDetector CollisionDetector instance for managing and retrieving enemy data.
     * @param tmxRenderer       Renderer instance for displaying the enemy's animation.
     */
    public NPCScreen(JFrame parent, Character player, CollisionDetector collisionDetector, TmxRenderer tmxRenderer, java.util.List<Question> questions, GameQuestionPanel questionPanel) {
        super(parent, "Fight Screen", true);
        this.player = player;
        this.enemyHealth = ENEMY_MAX_HEALTH;
        this.collisionDetector = collisionDetector;
        this.tmxRenderer = tmxRenderer;
        this.questions = questions;
        this.questionPanel = questionPanel;
        loadPlayerImage();  // Ensure player image is loaded
        setupUI();
        startAnimationTimer();
    }

    /**
     * Loads the player image from the resources folder and checks if loading was successful.
     */
    private void loadPlayerImage() {
        try {
            playerImage = ImageIO.read(new File("resources/rabbit.png"));  // Ensure path is correct
            if (playerImage == null) {
                System.err.println("Player image is null after loading.");
            }
        } catch (IOException e) {
            System.err.println("Player image could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Sets up the user interface components for health display, attack button, and animation rendering.
     */
    private void setupUI() {
        setSize(500, 350);
        setLocationRelativeTo(getParent());

        // Create a main panel with BorderLayout to retain original layout structure
        mainPanel = new JPanel(new BorderLayout());

        // Add the JLabel for messages
        messageLabel = new JLabel("Hello! I'm friendly :) Answer the provided question correctly to gain 100XP.", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setVisible(true); // Initially visible
        mainPanel.add(messageLabel, BorderLayout.NORTH);

        // Add interact button and animation panel
        mainPanel.add(createAnimationPanel(), BorderLayout.CENTER);

        // Create a button panel to hold both buttons side by side
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns, with horizontal spacing
        buttonPanel.add(createInteractButton());
        buttonPanel.add(createDontInteractButton());

        // Add the button panel to the bottom of the main panel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Initialize the layered pane as content pane
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(500, 300));
        setContentPane(layeredPane);

        // Add mainPanel to the base layer of layeredPane
        mainPanel.setBounds(0, 0, 500, 300);
        layeredPane.add(mainPanel, Integer.valueOf(0)); // Base layer

        // Add GameQuestionPanel on a higher layer for overlay
        questionPanel.setBounds(100, 75, questionPanel.getPreferredSize().width, questionPanel.getPreferredSize().height);
        layeredPane.add(questionPanel, Integer.valueOf(1)); // Higher layer
        questionPanel.setVisible(false); // Initially hidden
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
                displayNPCAnimation(g);

            }
        };
    }

    /**
     * Creates a JPanel to display the player image on the left side of the fight screen.
     *
     * @return JPanel with player image.
     */
    private JPanel createPlayerImagePanel() {
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (playerImage != null) {
                    g.drawImage(playerImage, 10, 40, 128, 128, null);  // Draw image with padding
                } else {
                    System.err.println("Player image is not available for rendering.");
                }
            }
        };

        return imagePanel;
    }
    private JButton createInteractButton() {
        attackButton = new JButton("Interact");
        attackButton.addActionListener(e -> handleInteractAction());
        return attackButton;
    }
    private JButton createDontInteractButton() {
        attackButton = new JButton("Don't Interact");
        attackButton.addActionListener(e -> handleDontInteractAction());
        return attackButton;
    }
    /**
     * Executes the player attack action, reducing enemy health and initiating an enemy counterattack.
     * Refreshes the screen to update health displays and animations.
     */
    private void handleInteractAction() {

        showRandomQuestion(); // Display question and wait for an answer

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                while (!questionPanel.isAnswered()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                boolean isCorrect = questionPanel.isCorrect();
                int xpGain = isCorrect ? 100 : 0; // XP for correct answer
                String answerFeedback = isCorrect ? "Correct! +100 XP" : "Incorrect! The answer was: " + questionPanel.currentQuestion.getAnswer();

                // 1. Show feedback on answer correctness
                JOptionPane.showMessageDialog(NPCScreen.this, answerFeedback, "Answer Result", JOptionPane.INFORMATION_MESSAGE);


                CloseScreen("Goodbye!", "Nice to meet you!");
                repaint();
            }
        };

        worker.execute();
    }
    private void handleDontInteractAction(){
        CloseScreen("Goodbye!", "Nice to meet you!");
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
     * Displays the fight result in a dialog and stops the animation.
     *
     * @param message The result message to display.
     * @param title   The title of the result dialog.
     */
    private void CloseScreen(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        tmxRenderer.repaintMap();
        stopAnimationTimer();
        dispose();
    }

    /**
     * Draws the enemy animation on the fight screen, using the enemy's name to identify the correct animation.
     *
     * @param g The Graphics context for rendering the animation.
     */
    private void displayNPCAnimation(Graphics g) {
        npcName = collisionDetector.getNPCName();
        if (npcName != null) {
            tmxRenderer.renderEnemyAnimation(npcName, 200, 40, g);
        } else {
            System.err.println("No enemy name found for animation rendering.");

        }


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

    // Modify showRandomQuestion to display GameQuestionPanel in the layered pane
    private void showRandomQuestion() {
        if (!questions.isEmpty()) {
            int randomIndex = (int) (Math.random() * questions.size());
            Question randomQuestion = questions.get(randomIndex);

            // Set question and display the panel on the top layer
            questionPanel.showQuestion(randomQuestion);
            questionPanel.setVisible(true);
            questionIsCorrect = questionPanel.isCorrect();
        }
    }


}



