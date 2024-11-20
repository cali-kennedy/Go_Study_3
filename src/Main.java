import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import models.*;
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
    private TmxParser tmxParser;
    private int old_x;
    private int old_y;

    public Main() {
        try {
            // Create a file to store the output
           // File outputFile = new File("output.log");
           // FileOutputStream fos = new FileOutputStream(outputFile);
           // PrintStream ps = new PrintStream(fos);

            // Redirect System.out and System.err
           // System.setOut(ps);
           // System.setErr(ps);


        } catch (Exception e) {
            e.printStackTrace();
        }
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
            tmxParser = new TmxParser("resources/small_test.tmx");
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            character = new Character("resources/rabbit.png", 10, 10, 20, 20);
            camera = new Camera(400, 400,  3.0f, character);

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


            setFocusable(true);
            requestFocusInWindow();

            // Initialize the renderer with parsed map data
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets, camera);
            collisionDetector = new CollisionDetector(character, objects, this.tmxRenderer);
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

            drawHealth(g);
            drawXP(g);

            g2d.setTransform(oldTransform);


            if (!isPaused) {

                checkCollisions();
            }
        }
    }

    // this check collisions calls the collisiondetectors class check collisions
    private void checkCollisions() {
        // Only update old_x and old_y before movement, not every time checkCollisions is called
        int previousX = character.getX();
        int previousY = character.getY();

        CollisionDetector.CollisionResult result = collisionDetector.checkCollisions();

        if (result.hasEnemyCollision() && !questionPanel.isQuestionVisible()) {
            System.out.println("---- Main.java CREATING A NEW FIGHT SCREEN ----");
            FightScreen fightScreen = new FightScreen(gameFrame, character, collisionDetector, tmxRenderer, questions, questionPanel);
            fightScreen.setVisible(true);
        }

        if (result.hasWallCollision()) {
            // Revert to last "safe" position if a wall collision is detected
            character.setX(old_x);
            character.setY(old_y);
        } else {
            // Update old_x and old_y only if there’s no collision
            old_x = previousX;
            old_y = previousY;
        }
    }


    private void drawHealth(Graphics g) {
        int barWidth = 150;
        int barHeight = 20;
        int x = 10;
        int y = 270 ;

        // Draw health bar background
        g.setColor(Color.GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Calculate health percentage (max health is 100)
        float healthPercentage = (float) character.getHealth() / 100;
        int currentHealthWidth = (int) (barWidth * healthPercentage);

        // Draw current health
        g.setColor(Color.RED);
        g.fillRect(x, y, currentHealthWidth, barHeight);

        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barWidth, barHeight);

        // Draw health text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String healthText = character.getHealth() + "/100";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(healthText)) / 2;
        int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(healthText, textX, textY);
    }

    private void drawXP(Graphics g) {
        int barWidth = 150;
        int barHeight = 20;
        int x = 10;
        int y = 290;  // Position below health bar

        // Draw XP bar background
        g.setColor(Color.GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Calculate XP percentage using levelProgression and levelCap
        float xpPercentage = (float) character.getLevelProgression() / character.getLevelCap();
        int currentXPWidth = (int) (barWidth * xpPercentage);

        // Draw current XP
        g.setColor(Color.BLUE);
        g.fillRect(x, y, currentXPWidth, barHeight);

        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barWidth, barHeight);

        // Draw XP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String xpText = "Level " + character.getLevel() + " (" +
                character.getLevelProgression() + "/" + character.getLevelCap() + ")";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(xpText)) / 2;
        int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(xpText, textX, textY);
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