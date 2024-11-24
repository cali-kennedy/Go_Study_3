import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
public class FightScreen extends JDialog {

    // Constants for health and attack values
    private static final int ENEMY_MAX_HEALTH = 100;
    private static final int PLAYER_ATTACK_DAMAGE = 20;
    private static final int ENEMY_ATTACK_DAMAGE = 15;
    private static final int ANIMATION_REFRESH_RATE_MS = 100;
    private JLayeredPane layeredPane;
    private JPanel mainPanel;
    // Player and enemy attributes
    private final Character player;
    private int enemyHealth;
    private boolean showAttackEffect = false;

    private boolean questionIsCorrect = false;
    // Player character image
    private BufferedImage playerImage;

    // UI components for health display and attack functionality
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JButton attackButton;
    private JButton runButton;


    // External components for rendering and collision detection
    private final CollisionDetector collisionDetector;
    private final TmxRenderer tmxRenderer;
    private String enemyName;
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
    public FightScreen(JFrame parent, Character player, CollisionDetector collisionDetector, TmxRenderer tmxRenderer, java.util.List<Question> questions, GameQuestionPanel questionPanel) {
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
            playerImage = ImageIO.read(new File("resources/rabbit2.png"));  // Ensure path is correct
            if (playerImage == null) {
                System.err.println("Player image is null after loading.");
            }
        } catch (IOException e) {
            System.err.println("Player image could not be loaded: " + e.getMessage());
        }
    }

    private void setupUI() {
        setSize(500, 350);
        setLocationRelativeTo(getParent());

        mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(createHealthPanel(), BorderLayout.NORTH);
        mainPanel.add(createAnimationPanel(), BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton attackButton = createAttackButton();
        JButton defendButton = createDefendButton();
        JButton runButton = createRunButton();

        buttonPanel.add(attackButton);
        buttonPanel.add(defendButton);
        buttonPanel.add(runButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel playerImagePanel = createPlayerImagePanel();
        playerImagePanel.setPreferredSize(new Dimension(100, 100));
        mainPanel.add(playerImagePanel, BorderLayout.LINE_START);

        Color customColor = new Color(255, 169, 178);
        mainPanel.setBackground(customColor);

        layeredPane = new JLayeredPane();
        layeredPane.setBackground(customColor);
        layeredPane.setPreferredSize(new Dimension(500, 300));
        setContentPane(layeredPane);

        mainPanel.setBounds(0, 0, 500, 300);
        layeredPane.add(mainPanel, Integer.valueOf(0));

        questionPanel.setBounds(100, 75, questionPanel.getPreferredSize().width, questionPanel.getPreferredSize().height);
        layeredPane.add(questionPanel, Integer.valueOf(1));
        questionPanel.setVisible(false);
    }

    /**
     * Creates a JPanel containing labels to display player and enemy health.
     *
     * @return JPanel with health labels.
     */
    private JPanel createHealthPanel() {
        JPanel healthPanel = new JPanel(new GridLayout(1, 2));
        playerHealthLabel = new JLabel("Player Health: " + player.getHealth());
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        playerHealthLabel.setFont(customFont);

        Color customColor = new Color(7, 69, 8);

        playerHealthLabel.setForeground(customColor);
        enemyHealthLabel = new JLabel("Enemy Health: " + enemyHealth);
        enemyHealthLabel.setFont(customFont);
        enemyHealthLabel.setForeground(Color.red);
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
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        attackButton.setFont(customFont);
        attackButton.setForeground(Color.RED);
        attackButton.setBackground(Color.pink);
        attackButton.addActionListener(e -> handleAttackAction());
        return attackButton;
    }

    private JButton createDefendButton() {
        JButton defendButton = new JButton("Defend");
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        defendButton.setFont(customFont);
        defendButton.setForeground(Color.BLUE);
        defendButton.setBackground(Color.CYAN);
        defendButton.addActionListener(e -> handleDefendAction());
        return defendButton;
    }

    private JButton createRunButton() {
        runButton = new JButton("Run");
               Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        runButton.setFont(customFont);
        Color customBgColor = new Color(79, 164, 209);
        Color customFgColor = new Color(8, 41, 62);

        runButton.setForeground(customFgColor);
        runButton.setBackground(customBgColor);
        runButton.addActionListener(e -> handleRunAction());
                     return runButton;
    }

    /**
     * Creates a JPanel for rendering the enemy animation.
     *
     * @return JPanel for animation display.s
     */
    private JPanel createAnimationPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayEnemyAnimation(g);

                if (showAttackEffect) {
                    // Draw an overlay effect (e.g., a slash image)
                    tmxRenderer.renderIndividualAnimation("rabbit2_attack",-5,40,g);
                    tmxRenderer.renderIndividualAnimation(enemyName+"_hurt",200,40,g);
                }
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
                if (!showAttackEffect) {
                    if (playerImage != null) {
                        g.drawImage(playerImage, 10, 40, 128, 128, null);  // Draw image with padding
                    } else {
                        System.err.println("Player image is not available for rendering.");
                    }
                }
            }
        };

        return imagePanel;
    }

    private void handleRunAction() {
        showFightResult("Too scared?", "Loser!");
    }

    /**
     * Executes the player attack action, reducing enemy health and initiating an enemy counterattack.
     * Refreshes the screen to update health displays and animations.
     */
    private void handleAttackAction() {
        showRandomQuestion(); // Display question and wait for an answer
        playSound("resources/attack_sound.wav");


        showAttackEffect = true;
        repaint();
        // Schedule to turn off the effect after 200ms
        Timer effectTimer = new Timer(5000, e -> {
            showAttackEffect = false;
            repaint();
        });
        effectTimer.setRepeats(false);
        effectTimer.start();
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
                JOptionPane.showMessageDialog(FightScreen.this, answerFeedback, "Answer Result", JOptionPane.INFORMATION_MESSAGE);
                int damage = 0;
                // 2. Calculate and show damage dealt
                if(player.getLevel() > 1) {
                    damage = isCorrect ? PLAYER_ATTACK_DAMAGE * (new Random().nextInt(6) + 1) : PLAYER_ATTACK_DAMAGE;
                    JOptionPane.showMessageDialog(FightScreen.this, "You dealt " + damage + " damage.", "Damage Dealt", JOptionPane.INFORMATION_MESSAGE);
                    attackEnemy(damage);
                }else{
                    damage = isCorrect ? PLAYER_ATTACK_DAMAGE * (new Random().nextInt(4) + 1) : PLAYER_ATTACK_DAMAGE;
                    JOptionPane.showMessageDialog(FightScreen.this, "You dealt " + damage + " damage.", "Damage Dealt", JOptionPane.INFORMATION_MESSAGE);
                    attackEnemy(damage);
                }



                if (enemyHealth > 0) {
                    enemyTurn();
                }
                // 3. Check and show fight status
                checkFightStatus();
                repaint();
            }
        };

        worker.execute();

    }
    private void handleDefendAction() {
        // Set player's defending state
        player.setDefending(true);

        // Provide feedback to the player
        JOptionPane.showMessageDialog(this, "You brace yourself for the next attack!", "Defend", JOptionPane.INFORMATION_MESSAGE);

        // Enemy attacks after the player defends
        enemyTurn();
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
    private void enemyAttack(int damage) {
        if (player.isDefending()) {
            damage = damage / 2; // Reduce damage by 50%
            JOptionPane.showMessageDialog(this, "Your defense reduces the damage by half!", "Defend", JOptionPane.INFORMATION_MESSAGE);
        }
        player.removeHealth(damage);
        playerHealthLabel.setText("Player Health: " + player.getHealth());
    }

    private void enemyTurn() {
        int damage = ENEMY_ATTACK_DAMAGE;
        enemyAttack(damage);
        JOptionPane.showMessageDialog(this, "The enemy dealt " + damage + " damage!", "Enemy Attack", JOptionPane.INFORMATION_MESSAGE);

        // Reset player's defending state
        player.setDefending(false);

        // Check fight status
        checkFightStatus();
        repaint();
    }


    /**
     * Checks if the fight has been won or lost and displays the appropriate result.
     * Stops the animation timer and closes the dialog if the fight is over.
     */
    private void checkFightStatus() {
        if (isFightWon()) {
            //enemyName = collisionDetector.getEnemyName();
            if (enemyName != null) {
                // tmxRenderer.markEnemyAsDefeated(enemyName);            }
            }
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
        if(enemyHealth <= 0) {
            tmxRenderer.markObjectAsEncountered(enemyName);
        }
        System.out.println("Enemy name from Fightscreen passed to tmx renderer: " + enemyName);
        tmxRenderer.repaintMap();
        stopAnimationTimer();
        dispose();
    }

    /**
     * Draws the enemy animation on the fight screen, using the enemy's name to identify the correct animation.
     *
     * @param g The Graphics context for rendering the animation.
     */
    private void displayEnemyAnimation(Graphics g) {
        enemyName = collisionDetector.getEnemyName();
        if (enemyName != null) {
            tmxRenderer.renderIndividualAnimation(enemyName, 200, 40, g);
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
    private void playSound(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
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