import javax.swing.*;
import java.awt.*;


public class NPCScreen extends JDialog {


    private static final int ANIMATION_REFRESH_RATE_MS = 100;
    private JLayeredPane layeredPane;

    private JPanel mainPanel;


    private final Character player;

    private boolean XPCollected;
    private boolean questionIsCorrect = false;


    private JButton interactButton;
    private JButton okayButton;


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
     * @param collisionDetector CollisionDetector instance for managing and retrieving npc data.
     * @param tmxRenderer       Renderer instance for displaying the npc's animation.
     */
    public NPCScreen(JFrame parent, Character player, CollisionDetector collisionDetector, TmxRenderer tmxRenderer, java.util.List<Question> questions, GameQuestionPanel questionPanel) {
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

        // Create a main panel with BorderLayout to retain original layout structure
        mainPanel = new JPanel(new BorderLayout());

        if (collisionDetector.getNPCName().equalsIgnoreCase("frog")) { // Change message depending on NPC shown
            // Create a container panel for stacked messages
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setOpaque(false); // Make it transparent if desired

            // Load custom fonts using FontUtils
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);

            // Add the first message with custom styling
            JLabel primaryMessage = new JLabel("Hello! I'm here to help :) ", SwingConstants.CENTER);
            primaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            Color customColor = new Color(21, 97, 50);

            primaryMessage.setForeground(customColor);



            customColor = new Color(121, 147, 202);

            // Add the second message with custom styling
            JLabel thirdMessage = new JLabel("Fight enemies around the map to gain more XP!", SwingConstants.CENTER);
            thirdMessage.setFont(customFont != null ? customFont.deriveFont(18f) : new Font("Arial", Font.ITALIC, 10));
            thirdMessage.setForeground(customColor);
            JLabel fifthMessage = new JLabel("Press i to open your inventory.", SwingConstants.CENTER);
            fifthMessage.setFont(customFont != null ? customFont.deriveFont(18f) : new Font("Arial", Font.ITALIC, 10));
            fifthMessage.setForeground(customColor);

            // Add both labels to the message panel
            messagePanel.add(primaryMessage);

            messagePanel.add(thirdMessage);

            messagePanel.add(fifthMessage);



            // Add the message panel to the top of the main panel
            mainPanel.add(messagePanel, BorderLayout.NORTH);
        }
        if (collisionDetector.getNPCName().equalsIgnoreCase("buy_info_frog")) { // Change message depending on NPC shown
            // Create a container panel for stacked messages
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setOpaque(false); // Make it transparent if desired

            // Load custom fonts using FontUtils
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);

            // Add the first message with custom styling
            JLabel primaryMessage = new JLabel("Collect Study Studs to buy potions, health, and XP. ", SwingConstants.CENTER);
            primaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            Color customColor = new Color(110, 60, 163);

            primaryMessage.setForeground(customColor);

            // Add the first message with custom styling
            JLabel secondaryMessage = new JLabel("Good luck!", SwingConstants.CENTER);
            secondaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            customColor = new Color(160, 106, 218);
            secondaryMessage.setForeground(customColor);

            // Add both labels to the message panel
            messagePanel.add(primaryMessage);
            messagePanel.add(secondaryMessage);


            // Add the message panel to the top of the main panel
            mainPanel.add(messagePanel, BorderLayout.NORTH);
        }

        if(collisionDetector.getNPCName().equalsIgnoreCase("LowXPChest")) {
            // Create a container panel for stacked messages
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setOpaque(false); // Make it transparent if desired

            // Load custom fonts using FontUtils
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 20);

            // Add the first message with custom styling
            JLabel primaryMessage = new JLabel("Congrats! You Found a Chest", SwingConstants.CENTER);
            primaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            Color customColor = new Color(94, 119, 50);

            primaryMessage.setForeground(customColor);

            if (!NPCInteractionTracker.isXpCollected("chest")) {
                JLabel secondaryMessage = new JLabel("Here's 100 XP!", SwingConstants.CENTER);
                player.addXP(100);
                secondaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
                secondaryMessage.setForeground(customColor);
                messagePanel.add(secondaryMessage);
                NPCInteractionTracker.setXpCollected("chest");
            }
            // Add both labels to the message panel
            messagePanel.add(primaryMessage);



            // Add the message panel to the top of the main panel
            mainPanel.add(messagePanel, BorderLayout.NORTH);
        }

        if(collisionDetector.getNPCName().equalsIgnoreCase("help_npc") && (tmxRenderer.checkIfHelped("help_npc") == true)) {
            // Create a container panel for stacked messages
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setOpaque(false); // Make it transparent if desired

            // Load custom fonts using FontUtils
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 20);

            // Add the first message with custom styling
            JLabel primaryMessage = new JLabel("Thank you so Much! ", SwingConstants.CENTER);
            primaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            Color customColor = new Color(94, 119, 50);

            primaryMessage.setForeground(customColor);

            if (!NPCInteractionTracker.isXpCollected("help_npc")) {
                JLabel secondaryMessage = new JLabel("Here's 100 XP!", SwingConstants.CENTER);
                player.addXP(100);
                secondaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
                secondaryMessage.setForeground(customColor);
                messagePanel.add(secondaryMessage);
                NPCInteractionTracker.setXpCollected("help_npc");
            }
            // Add both labels to the message panel
            messagePanel.add(primaryMessage);



            // Add the message panel to the top of the main panel
            mainPanel.add(messagePanel, BorderLayout.NORTH);
        }
        System.out.println("NPC SCREEN CHECK IF HELPED ----------------------------------------------" + tmxRenderer.checkIfHelped("help_npc"));
        if(collisionDetector.getNPCName().equalsIgnoreCase("help_npc") && (tmxRenderer.checkIfHelped("help_npc") == false)) {

            // Create a container panel for stacked messages
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setOpaque(false); // Make it transparent if desired

            // Load custom fonts using FontUtils
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 20);

            // Add the first message with custom styling
            JLabel primaryMessage = new JLabel("The onion is blocking my way home! ", SwingConstants.CENTER);
            primaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            Color customColor = new Color(232, 108, 108);

            primaryMessage.setForeground(customColor);

            // Add the first message with custom styling
            JLabel secondaryMessage = new JLabel("Can you Help?", SwingConstants.CENTER);
            secondaryMessage.setFont(customFont != null ? customFont : new Font("Arial", Font.BOLD, 12));
            secondaryMessage.setForeground(customColor);

            // Add both labels to the message panel
            messagePanel.add(primaryMessage);
            messagePanel.add(secondaryMessage);


            // Add the message panel to the top of the main panel
            mainPanel.add(messagePanel, BorderLayout.NORTH);
        }

        // Add interact button and animation panel
        mainPanel.add(createAnimationPanel(), BorderLayout.CENTER);

        if(collisionDetector.getNPCName().equalsIgnoreCase("frog")) {
            // Create a button panel to hold both buttons side by side
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns, with horizontal spacing
            buttonPanel.add(createOkayButton());
            // Add the button panel to the bottom of the main panel
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        if(collisionDetector.getNPCName().equalsIgnoreCase("help_npc")) {
            // Create a button panel to hold both buttons side by side
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns, with horizontal spacing
            buttonPanel.add(createOkayButton());
            // Add the button panel to the bottom of the main panel
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        if(collisionDetector.getNPCName().equalsIgnoreCase("buy_info_frog")) {
            // Create a button panel to hold both buttons side by side
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 row, 2 columns, with horizontal spacing
            buttonPanel.add(createOkayButton());
            // Add the button panel to the bottom of the main panel
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }



        // Initialize the layered pane as content pane
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(500, 300));
        setContentPane(layeredPane);

        // Add mainPanel to the base layer of layeredPane
        mainPanel.setBounds(0, 0, 560, 300);
        layeredPane.add(mainPanel, Integer.valueOf(0)); // Base layer
        // Add GameQuestionPanel on a higher layer for overlay
        questionPanel.setBounds(100, 75, questionPanel.getPreferredSize().width, questionPanel.getPreferredSize().height);
        layeredPane.add(questionPanel, Integer.valueOf(1)); // Higher layer
        questionPanel.setVisible(false); // Initially hidden
    }


    /**
     * Creates a JPanel for rendering the npc animation.
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
    private JButton createOkayButton() {
        okayButton = new JButton("Okay!");
        Color custombgColor = new Color(134, 156, 143);
        okayButton.setBackground(custombgColor);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 17);
        okayButton.setFont(customFont);
        okayButton.addActionListener(e -> handleDontInteractAction());
        return okayButton;
    }


    private JButton createInteractButton() {
        interactButton = new JButton("Interact");
        Color custombgColor = new Color(134, 156, 143);
        interactButton.setBackground(custombgColor);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 17);
        interactButton.setFont(customFont);
        interactButton.addActionListener(e -> handleInteractAction());
        return interactButton;
    }
    private JButton createDontInteractButton() {
        interactButton = new JButton("Don't Interact");
        Color custombgColor = new Color(207, 156, 156);
        interactButton.setBackground(custombgColor);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 17);
        interactButton.setFont(customFont);
        interactButton.addActionListener(e -> handleDontInteractAction());
        return interactButton;
    }



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


    private void CloseScreen(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        tmxRenderer.repaintMap();
        stopAnimationTimer();
        dispose();
    }


    private void displayNPCAnimation(Graphics g) {
        npcName = collisionDetector.getNPCName();
        if (npcName != null) {
            tmxRenderer.renderIndividualAnimation(npcName, 200, 40, g);
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
     * Stops the animation timer to prevent further updates after the interaction ends.
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



