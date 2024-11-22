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

import com.formdev.flatlaf.FlatLightLaf;
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
    private boolean isNpcDialogOpen = false;
    private long lastNpcCollisionTime = 0;   // Tracks the last time an NPC collision occurred
    private static final int COOLDOWN_TIME_MS = 90000; // Cooldown period in milliseconds
    public Main() {
        try {
            // import look and feel from faltlaf
            UIManager.setLookAndFeel(new FlatLightLaf());

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
            tmxParser = new TmxParser("resources/large_test.tmx");
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            character = new Character("resources/rabbit.png", 50, 250, 20, 20);
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

            camera.update(50*50, 50*50);
            camera.applyTransform(g2d);
            tmxRenderer.render(g);
            camera.drawXP(g);
            camera.drawHealth(g);
            //   character = new Character("resources/rabbit.png",10,10,20,20);
            character.draw(g); // draw the character on the map


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

        if (result.hasNPCCollision() && !isNpcDialogOpen) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastNpcCollisionTime > COOLDOWN_TIME_MS) {
                isNpcDialogOpen = true;
                lastNpcCollisionTime = currentTime; // Update the last collision time

                NPCScreen npcScreen = new NPCScreen(gameFrame, character, collisionDetector, tmxRenderer, questions, questionPanel);
                npcScreen.setVisible(true);

                isNpcDialogOpen = false; // Reset flag after NPCScreen is closed
                System.out.println("----------------------HAD NPC COLL ----------------------------------------s");
            }
        }
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