import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionInputScreen extends JFrame {
    private List<Question> questions;  // List to store the input questions

    public QuestionInputScreen() {
        // Set up the frame
        setTitle("Question Input");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        questions = new ArrayList<>();  // Initialize the question list

        // Create components
        JLabel questionLabel = new JLabel("Enter Question:");
        JTextField questionField = new JTextField();
        JLabel answerLabel = new JLabel("Enter Answer:");
        JTextField answerField = new JTextField();
        JButton saveButton = new JButton("Save Question");
        JButton finishButton = new JButton("Finish");

        // Set layout and add components
        setLayout(new GridLayout(5, 1));
        add(questionLabel);
        add(questionField);
        add(answerLabel);
        add(answerField);
        add(saveButton);
        add(finishButton);

        // Action for the "Save Question" button
        saveButton.addActionListener(e -> {
            String questionText = questionField.getText();
            String answerText = answerField.getText();
            if (!questionText.isEmpty() && !answerText.isEmpty()) {
                questions.add(new Question(questionText, answerText));  // Add question to the list
                JOptionPane.showMessageDialog(null, "Question saved!");
                questionField.setText("");  // Clear input fields
                answerField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Please enter both question and answer.");
            }
        });

        // Action for the "Finish" button
        finishButton.addActionListener(e -> {
            // Close the input screen
            dispose();
        });
    }

    // Getter to return the list of questions
    public List<Question> getQuestions() {
        return questions;
    }
}
