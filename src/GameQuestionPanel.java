import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;

// Panel that displays and handles the question-answer interaction when colliding with enemies
public class GameQuestionPanel extends JPanel {
    Question currentQuestion;
    private JTextField answerField;
    private Character character;
    private boolean isVisible = false;
    private JLabel questionLabel;
    private JPanel mainPanel;
    private JPanel centerPanel;
    private JButton submitButton;
    private boolean isCorrect;
    private boolean isAnswered;

    // Initialize the question panel with necessary components and styling
    public GameQuestionPanel(Character character, JPanel mainPanel) {
        this.character = character;
        this.mainPanel = mainPanel;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 250));
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2); // Thickness of 2 pixels

        // Set semi-transparent white background
        setBackground(new Color(255, 255, 255, 130));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and style the question text label
        questionLabel = new JLabel();
        questionLabel.setHorizontalAlignment(JLabel.CENTER);



        // Create input field and submit button
        answerField = new JTextField(20);
        submitButton = new JButton("Submit");
        Font customFont = FontUtils.loadFont("/fonts/RubikMonoOne-Regular.ttf", 15);
        submitButton.setFont(customFont);
        submitButton.setBorder(blackBorder);
        Color customColor = new Color(200, 255, 223);
        submitButton.setBackground(customColor);
        questionLabel.setFont(customFont);
        answerField.setFont(customFont);
        submitButton.setPreferredSize(new Dimension(100, 30));

        // Set up the panel layout for centering components
        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add components to the center panel
        centerPanel.add(questionLabel, gbc);
        centerPanel.add(answerField, gbc);
        centerPanel.add(submitButton, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Add action listeners for submit button and enter key
        submitButton.addActionListener(e -> checkAnswer());
        answerField.addActionListener(e -> checkAnswer());

        // Initial visibility and focus settings
        setVisible(false);
        setOpaque(true);

        answerField.setFocusable(true);
        submitButton.setFocusable(true);
        setFocusable(true);
    }

    // Override contains method to prevent mouse events when panel is invisible
    @Override
    public boolean contains(int x, int y) {
        if (!isVisible) return false;
        return super.contains(x, y);
    }

    // Display a new question and pause the game
    public void showQuestion(Question question) {
        if (!isVisible) {
            isAnswered = false;
            this.currentQuestion = question;
            Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);

            // Set a width for wrapping, adjust as desired
            int maxWidth = 250;
            String wrappedQuestion = "<html><body style='width:" + maxWidth + "px; text-align:center;'>"
                    + question.getQuestion()
                    + "</body></html>";
            questionLabel.setText(wrappedQuestion);
            questionLabel.setFont(customFont);
            answerField.setText("");
            isVisible = true;
            setVisible(true);
            // mainPanel.togglePause(true);
            answerField.requestFocus();
        }
    }


    // Verify the user's answer and provide feedback
    private void checkAnswer() {
        String userAnswer = answerField.getText().trim();
        isCorrect = userAnswer.equalsIgnoreCase(currentQuestion.getAnswer());
        isAnswered = true; // Set isAnswered to true after submitting an answer

        // Award XP for correct answers, deduct health for incorrect ones (deducting not fully implemented yet)
        if (isCorrect) {
            character.addXP(100);

        } else {
            character.removeHealth(10);

        }

        hidePanel();

    }

    public boolean isCorrect(){
        return isCorrect;
    }

    // Hide the question panel and resume the game
    private void hidePanel() {
        isVisible = false;
        setVisible(false);
     //   mainPanel.togglePause(false);
        SwingUtilities.invokeLater(() -> {
            mainPanel.requestFocusInWindow();
        });
    }

    // Check if the question panel is currently visible
    public boolean isQuestionVisible() {
        return isVisible;
    }

    // Custom painting for rounded panel background
    @Override
    protected void paintComponent(Graphics g) {
        if (isVisible) {
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
        }
    }

    // Set the preferred size of the question panel
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 250);
    }

    public boolean isAnswered() {
        return isAnswered;
    }
}