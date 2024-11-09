import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FightScreen extends JDialog {
    private Character player;
    private int enemyHealth;
    private final int ENEMY_MAX_HEALTH = 100;
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private JButton attackButton;
    private CollisionDetector collisionDetector;
    private TmxRenderer tmxRenderer;

    public FightScreen(JFrame parent, Character player, CollisionDetector collisionDetector, TmxRenderer tmxRenderer) {
        super(parent, "Fight Screen", true);
        this.player = player;
        this.enemyHealth = ENEMY_MAX_HEALTH;
        this.collisionDetector = collisionDetector;
        this.tmxRenderer = tmxRenderer;

        setupUI(parent);
    }

    // Setup UI components for the fight
    private void setupUI(JFrame parent) {
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel for health indicators
        JPanel healthPanel = new JPanel(new GridLayout(1, 2));
        playerHealthLabel = new JLabel("Player Health: " + player.getHealth());
        enemyHealthLabel = new JLabel("Enemy Health: " + enemyHealth);
        healthPanel.add(playerHealthLabel);
        healthPanel.add(enemyHealthLabel);

        // Attack button
        attackButton = new JButton("Attack");
        attackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attackEnemy(20); // Arbitrary damage value
                if (enemyHealth > 0) {
                    enemyAttack();
                }
                checkFightStatus();
                repaint();  // Refresh the screen to update animations
            }
        });

        // Custom panel for rendering enemy animation
        JPanel animationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayEnemyAnimation(g);  // Call custom method to render enemy animation
            }
        };

        // Add components to the dialog
        add(healthPanel, BorderLayout.NORTH);
        add(attackButton, BorderLayout.SOUTH);
        add(animationPanel, BorderLayout.CENTER);
    }

    // Deduct health points from the enemy when the player attacks
    public void attackEnemy(int damage) {
        enemyHealth = Math.max(0, enemyHealth - damage);
        enemyHealthLabel.setText("Enemy Health: " + enemyHealth);
    }

    // Deduct health points from the player when the enemy attacks
    public void enemyAttack() {
        int damage = 15; // Arbitrary enemy damage value
        player.removeHealth(damage);
        playerHealthLabel.setText("Player Health: " + player.getHealth());
    }

    // Check if the fight has been won or lost
    public void checkFightStatus() {
        if (isFightWon()) {
            JOptionPane.showMessageDialog(this, "You have won the fight!", "Victory", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else if (player.getHealth() <= 0) {
            JOptionPane.showMessageDialog(this, "You have been defeated!", "Defeat", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    // Method to display enemy animation in the fight screen
    public void displayEnemyAnimation(Graphics g) {
        String enemyName = collisionDetector.getEnemyName();
        tmxRenderer.renderEnemyAnimation(enemyName, 10, 10, g);  // Pass Graphics and coordinates
    }

    // Returns true if the player has won the fight
    public boolean isFightWon() {
        return enemyHealth <= 0;
    }
}
