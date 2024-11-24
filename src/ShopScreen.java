import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShopScreen extends JDialog {

    private static final int ANIMATION_REFRESH_RATE_MS = 100;

    private JLayeredPane layeredPane;
    private JPanel mainPanel;

    private final Character player;

    // External components for rendering and collision detection
    private final CollisionDetector collisionDetector;
    private final TmxRenderer tmxRenderer;
    private final List<Question> questions;
    private final GameQuestionPanel questionPanel;

    // Timer for updating NPC animation
    private Timer animationTimer;

    /**
     * Constructor initializes the ShopScreen dialog with specified parameters and starts the animation.
     *
     * @param parent            The parent JFrame for this modal dialog.
     * @param player            The player's Character object with player-specific stats and actions.
     * @param collisionDetector CollisionDetector instance for managing and retrieving NPC data.
     * @param tmxRenderer       Renderer instance for displaying the NPC's animation.
     * @param questions         List of questions for the game.
     * @param questionPanel     Panel for displaying game questions.
     */
    public ShopScreen(JFrame parent, Character player, CollisionDetector collisionDetector,
                      TmxRenderer tmxRenderer, List<Question> questions, GameQuestionPanel questionPanel) {
        super(parent, "NPC Screen", true);
        this.player = player;
        this.collisionDetector = collisionDetector;
        this.tmxRenderer = tmxRenderer;
        this.questions = questions;
        this.questionPanel = questionPanel;
        setupUI();
        startAnimationTimer();
    }

    private void setupUI() {
        setSize(570, 350);
        setLocationRelativeTo(getParent());

        // Create a main panel with BorderLayout
        mainPanel = new JPanel(new BorderLayout());

            mainPanel.add(createMessagePanel(), BorderLayout.NORTH);


        // Add animation panel and button panel
        mainPanel.add(createAnimationPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        // Initialize the layered pane as content pane
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(500, 300));
        setContentPane(layeredPane);

        // Add mainPanel to the base layer of layeredPane
        mainPanel.setBounds(0, 0, 560, 300);
        layeredPane.add(mainPanel, Integer.valueOf(0));

        questionPanel.setVisible(false); // Initially hidden
    }

    private JPanel createMessagePanel() {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false); // Make it transparent if desired

        // Load custom fonts using FontUtils
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        System.out.println("shopname: "+ collisionDetector.getShopName());
        if(collisionDetector.getShopName().equalsIgnoreCase("pink_shop")) {
            // Add messages with custom styling
            messagePanel.add(createStyledLabel("Hello! :)", customFont, new Color(21, 97, 50)));
            messagePanel.add(createStyledLabel("Spend 1 Study Stud for 10 XP.", customFont, new Color(21, 97, 83)));
            messagePanel.add(createStyledLabel("Spend 3 Study Studs for 10 health.", customFont, new Color(21, 97, 83)));
            return messagePanel;

        }
        if(collisionDetector.getShopName().equalsIgnoreCase("brown_shop")) {
            // Add messages with custom styling

            messagePanel.add(createStyledLabel("Hello! :)", customFont, new Color(21, 97, 50)));
            messagePanel.add(createStyledLabel("Spend 10 Study Stud for 50 XP.", customFont, new Color(21, 97, 83)));
            messagePanel.add(createStyledLabel("Spend 30 Study Studs for 150 XP.", customFont, new Color(21, 97, 83)));
            return messagePanel;
        }
        return messagePanel;
    }

    private JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font != null ? font : new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        return label;
    }

    private JPanel createButtonPanel() {
        if (collisionDetector.getShopName().equalsIgnoreCase("pink_shop")) {
            JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            buttonPanel.add(createBuyXPButton());
            buttonPanel.add(createBuyHealthButton());
            buttonPanel.add(createLeaveButton());
            return buttonPanel;
        }
        if (collisionDetector.getShopName().equalsIgnoreCase("brown_shop")) {
            JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
            buttonPanel.add(createBuyMediumXPButton());
            buttonPanel.add(createBuyLargeXPButton());
            buttonPanel.add(createLeaveButton());
            return buttonPanel;
        }else{
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        buttonPanel.add(createBuyMediumXPButton());
        buttonPanel.add(createBuyLargeXPButton());
        buttonPanel.add(createLeaveButton());
        return buttonPanel; }
    }

    private JPanel createAnimationPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayShopAnimation(g);
            }
        };
    }

    private JButton createBuyHealthButton() {
        JButton button = new JButton("Buy 10 Health");
        button.setBackground(new Color(134, 156, 143));
        button.setFont(loadCustomFont(17));
        button.addActionListener(e -> handleBuyHealthAction());
        return button;
    }



    private JButton createBuyXPButton() {
        JButton button = new JButton("Buy 10 XP");
        button.setBackground(new Color(134, 156, 143));
        button.setFont(loadCustomFont(17));
        button.addActionListener(e -> handleBuyXPAction());
        return button;
    }
    private JButton createBuyMediumXPButton() {
        JButton button = new JButton("Buy 50 XP");
        button.setBackground(new Color(134, 156, 143));
        button.setFont(loadCustomFont(17));
        button.addActionListener(e -> handleBuyXPAction());
        return button;
    }
    private JButton createBuyLargeXPButton() {
        JButton button = new JButton("Buy 150 XP");
        button.setBackground(new Color(134, 156, 143));
        button.setFont(loadCustomFont(17));
        button.addActionListener(e -> handleBuyXPAction());
        return button;
    }
    private void handleBuyHealthPotionAction() {
        Item studyStud = findItemInInventory("Study Stud");
        if (studyStud != null && studyStud.getQuantity() >= 3) {
            // Remove Study Studs from inventory
            studyStud.setQuantity(studyStud.getQuantity() - 3);
            if (studyStud.getQuantity() <= 0) {
                player.removeItem(studyStud);
            }

            // Add Health Potion to inventory
            Item healthPotion = new Item("Health Potion", "Restores health when used", 1, true);
            player.addItem(healthPotion);

            JOptionPane.showMessageDialog(this, "You bought a Health Potion for 3 Study Studs.");
        } else {
            JOptionPane.showMessageDialog(this, "You don't have enough Study Studs!", "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Item findItemInInventory(String itemName) {
        for (Item item : player.getInventory()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }


    private JButton createLeaveButton() {
        JButton button = new JButton("Leave");
        button.setBackground(new Color(207, 156, 156));
        button.setFont(loadCustomFont(17));
        button.addActionListener(e -> handleLeaveAction());
        return button;
    }

    private Font loadCustomFont(int size) {
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", size);
        return customFont != null ? customFont : new Font("Arial", Font.PLAIN, size);
    }

    private void handleBuyXPAction() {
        Item studyStud = findItemInInventory("Study Stud");
        if (studyStud != null && studyStud.getQuantity() >= 1) {
            // Remove Study Studs from inventory
            studyStud.setQuantity(studyStud.getQuantity() - 1);
            if (studyStud.getQuantity() <= 0) {
                player.removeItem(studyStud);
                player.addXP(10);
                JOptionPane.showMessageDialog(this, "You bought 10 XP for 1 Study Stud.");

            }

        } else {
            JOptionPane.showMessageDialog(this, "You don't have enough Study Studs!", "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleBuyHealthAction() {
        Item studyStud = findItemInInventory("Study Stud");
        if (studyStud != null && studyStud.getQuantity() >= 3) {
            // Remove Study Studs from inventory
            studyStud.setQuantity(studyStud.getQuantity() - 3);
            if (studyStud.getQuantity() <= 0) {
                player.removeItem(studyStud);
                JOptionPane.showMessageDialog(this, "You bought 10 health for 3 Study Stud.");

            }
        } else {
            JOptionPane.showMessageDialog(this, "You don't have enough Study Studs!", "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleLeaveAction() {
        closeScreen("Goodbye!", "Nice to meet you!");
    }

    private void closeScreen(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        tmxRenderer.repaintMap();
        stopAnimationTimer();
        dispose();
    }

    private void displayShopAnimation(Graphics g) {
        String shopName = collisionDetector.getShopName();
        if (shopName != null) {
            tmxRenderer.renderIndividualAnimation(shopName, 200, 40, g);
        } else {
            System.err.println("No shop name found for animation rendering.");
        }
    }

    private void startAnimationTimer() {
        animationTimer = new Timer(ANIMATION_REFRESH_RATE_MS, e -> repaint());
        animationTimer.start();
    }

    private void stopAnimationTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

}
