import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Main extends JPanel {
    private TmxRenderer tmxRenderer;
    private Camera camera;
    private Character character;
    private InputHandler inputHandler;
    private CollisionDetector collisionDetector;
    private GameQuestionPanel questionPanel;
    private boolean isPaused = false;
    private List<Question> questions;
    private static JFrame gameFrame;

    public Main() {
        showQuestionInputDialog();

        if (questions != null && !questions.isEmpty()) {
            initializeGame();
        } else {
            JOptionPane.showMessageDialog(null, "No questions added. Game will exit.");
            System.exit(0);
        }
    }

    private void showQuestionInputDialog() {
        questions = new ArrayList<>();
        JDialog dialog = new JDialog(gameFrame, "Input Questions", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField questionField = new JTextField(20);
        JTextField answerField = new JTextField(20);
        JButton addButton = new JButton("Add Question");

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> questionList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(questionList);

        inputPanel.add(new JLabel("Question:"), gbc);
        inputPanel.add(questionField, gbc);
        inputPanel.add(new JLabel("Answer:"), gbc);
        inputPanel.add(answerField, gbc);
        inputPanel.add(addButton, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Start Game");
        JButton removeButton = new JButton("Remove Selected");
        buttonPanel.add(removeButton);
        buttonPanel.add(startButton);

        addButton.addActionListener(e -> {
            String q = questionField.getText().trim();
            String a = answerField.getText().trim();
            if (!q.isEmpty() && !a.isEmpty()) {
                questions.add(new Question(q, a));
                listModel.addElement(q);
                questionField.setText("");
                answerField.setText("");
                questionField.requestFocus();
            }
        });

        removeButton.addActionListener(e -> {
            int selectedIndex = questionList.getSelectedIndex();
            if (selectedIndex != -1) {
                questions.remove(selectedIndex);
                listModel.remove(selectedIndex);
            }
        });

        startButton.addActionListener(e -> {
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please add at least one question before starting.",
                        "No Questions",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                dialog.dispose();
            }
        });

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        dialog.setVisible(true);
    }

    private void initializeGame() {
        setLayout(null);

        try {
            // Parse .tmx file and tileset files to populate models
            TmxParser tmxParser = new TmxParser("resources/small_test.tmx");
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            character = new Character("resources/rabbit.png", 10, 10, 20, 20);
            camera = new Camera(400, 400, 2.0f, character);

            questionPanel = new GameQuestionPanel(character, this);
            questionPanel.setLocation(50, 50);
            add(questionPanel);

            addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent e) {
                    if (questionPanel.isVisible()) {
                        int x = (getWidth() - questionPanel.getPreferredSize().width) / 2;
                        int y = (getHeight() - questionPanel.getPreferredSize().height) / 2;
                        questionPanel.setLocation(x, y);
                    }
                }
            });

            inputHandler = new InputHandler(character, this);
            collisionDetector = new CollisionDetector(character, objects);

            setFocusable(true);
            requestFocusInWindow();

            // Initialize the renderer with parsed map data
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets, camera);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.err.println("Error initializing TmxRenderer: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tmxRenderer != null) {
            Graphics2D g2d = (Graphics2D) g;

            AffineTransform oldTransform = g2d.getTransform();

            camera.update(20*16, 20*16);
            camera.applyTransform(g2d);
            tmxRenderer.render(g);
            //   character = new Character("resources/rabbit.png",10,10,20,20);
            character.draw(g); // draw the character on the map

            g2d.setTransform(oldTransform);

            if (!isPaused) {
                checkCollisions();
            }
        }
    }

    private void checkCollisions() {
        CollisionDetector.CollisionResult result = collisionDetector.checkCollisions();
        if (result.hasEnemyCollision() && !questionPanel.isQuestionVisible()) {
            showRandomQuestion();
        }
    }

    private void showRandomQuestion() {
        if (!questions.isEmpty()) {
            int randomIndex = (int)(Math.random() * questions.size());
            Question randomQuestion = questions.get(randomIndex);

            int x = (getWidth() - questionPanel.getPreferredSize().width) / 2;
            int y = (getHeight() - questionPanel.getPreferredSize().height) / 2;
            questionPanel.setBounds(x, y,
                    questionPanel.getPreferredSize().width,
                    questionPanel.getPreferredSize().height);

            questionPanel.showQuestion(randomQuestion);
            revalidate();
            repaint();
        }
    }

    public void togglePause(boolean pause) {
        isPaused = pause;
        if (!pause) {
            requestFocusInWindow();
        }
    }

    public void resetCollisionState() {
        collisionDetector.resetCollisionState();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gameFrame = new JFrame("TMX Map Renderer");
            Main mainPanel = new Main();

            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(400, 400); // Adjust based on map size
            gameFrame.add(mainPanel);
            gameFrame.setVisible(true);
            mainPanel.requestFocusInWindow();

            // Timer to refresh the display, simulating animation
            Timer timer = new Timer(16, e -> mainPanel.repaint()); // roughly 60 FPS
            timer.start();
        });
    }
}