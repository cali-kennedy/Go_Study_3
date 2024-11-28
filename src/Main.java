import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.border.Border;
import javax.xml.parsers.ParserConfigurationException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
    private boolean isShopDialogOpen = false;
    private Clip backgroundClip;

    private Map<String, MapState> mapStates = new HashMap<>();

    private long lastNpcCollisionTime = 0;   // Tracks the last time an NPC collision occurred
    private long lastShopCollisionTime = 0;   // Tracks the last time an NPC collision occurred

    private static final int COOLDOWN_TIME_MS = 90000; // Cooldown period in milliseconds
    private String currentMapFileName;

    public Main() {
        try {
            // import look and feel from faltlaf
            UIManager.setLookAndFeel(new FlatLightLaf());



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

    private void playBackgroundMusic(String musicFilePath) {
        try {
            // Open an audio input stream.
            File musicFile = new File(musicFilePath);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicFile);

            // Get a sound clip resource.
            backgroundClip = AudioSystem.getClip();

            // Open audio clip and load samples from the audio input stream.
            backgroundClip.open(audioIn);

            // Set the clip to loop continuously.
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);

            // Start playing the background music.
            backgroundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showQuestionInputDialog() {
        questions = new ArrayList<>();
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2); // Thickness of 2 pixels
        Border thinBlackBorder = BorderFactory.createLineBorder(Color.BLACK, 1); // Thickness of 2 pixels

        JDialog dialog = new JDialog(gameFrame, "Input Questions", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(null);

        Color customorangeColor = new Color(237, 203, 181);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(customorangeColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dialog.setBackground(customorangeColor);


        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(customorangeColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);


        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        Color customgreenColor = new Color(145, 175, 156);
        Color customLightgreenColor = new Color(195, 213, 200);
        Color customRedColor = new Color(174, 141, 137);
        Color customLightOrangeColor = new Color(250, 234, 224);


        JTextField questionField = new JTextField(20);
        questionField.setFont(customFont);
        questionField.setBorder(thinBlackBorder);
        questionField.setBackground(customLightOrangeColor);


        JTextField answerField = new JTextField(20);
        answerField.setBorder(thinBlackBorder);
        answerField.setBackground(customLightOrangeColor);


        answerField.setFont(customFont);
        JButton addButton = new JButton("Add Question");
        addButton.setFont(customFont);
        addButton.setBackground(customLightgreenColor);
        addButton.setBorder(blackBorder);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> questionList = new JList<>(listModel);
        questionList.setFont(customFont);

        questionList.setBackground(customLightOrangeColor);
        JScrollPane scrollPane = new JScrollPane(questionList);

        JLabel inputquestionLabel = new JLabel("Question: ");
        inputquestionLabel.setFont(customFont);

        inputPanel.add(inputquestionLabel, gbc);
        inputPanel.add(questionField, gbc);
        JLabel inputAnswerLabel = new JLabel("Answer: ");
        inputAnswerLabel.setFont(customFont);
        inputPanel.add(inputAnswerLabel, gbc);
        inputPanel.add(answerField, gbc);
        inputPanel.add(addButton, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Start Game");
        startButton.setFont(customFont);
        startButton.setBorder(blackBorder);
        startButton.setBackground(customgreenColor);
        JButton removeButton = new JButton("Remove Selected");
        removeButton.setFont(customFont);
        removeButton.setBackground(customRedColor);
        removeButton.setBorder(blackBorder);
        buttonPanel.add(removeButton);
        buttonPanel.add(startButton);
        buttonPanel.setBackground(customorangeColor);
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
        Color customColor = new Color(208, 222, 117);
        setBackground(customColor);
        try {
            // Set the current map file name
            currentMapFileName = "large_test.tmx";
            String initialMapFilePath = "resources/" + currentMapFileName;

            // Parse .tmx file and tileset files to populate models
            tmxParser = new TmxParser(initialMapFilePath);
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            // Initialize MapState for the initial map
            MapState mapState = mapStates.get(currentMapFileName);
            if (mapState == null) {
                mapState = new MapState(currentMapFileName);
                mapStates.put(currentMapFileName, mapState);
            } else {
                // Remove encountered objects
                MapState finalMapState = mapState;
                objects.removeIf(obj -> finalMapState.getEncounteredObjects().contains(obj.getName().toLowerCase()));
            }

            // Initialize the renderer with parsed map data and encountered objects
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets, camera, mapState.getEncounteredObjects());
            tmxRenderer.printObjectNames();


            // Initialize character and camera
            character = new Character("B_witch", 50, 250, 32, 32,tmxRenderer) ;
            camera = new Camera(700, 700,  3.0f, character);

            // Initialize question panel
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



            setFocusable(true);
            requestFocusInWindow();



            // Initialize collision detector
            collisionDetector = new CollisionDetector(character, objects, this.tmxRenderer, this);
            // Initialize input handler
            inputHandler = new InputHandler(character, this);

            playBackgroundMusic("resources/2016_ Clement Panchout_ Life is full of Joy.wav");

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
            camera.drawStudyStudCount(g);
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

        // Handle enemy collision
        if (result.hasEnemyCollision() && !questionPanel.isQuestionVisible()) {
            System.out.println("---- Main.java CREATING A NEW FIGHT SCREEN ----");
            FightScreen fightScreen = new FightScreen(gameFrame, character, collisionDetector, tmxRenderer, questions, questionPanel);
            fightScreen.setVisible(true);
        }

        // Handle wall collision
        if (result.hasWallCollision()) {
            // Revert to last "safe" position if a wall collision is detected
            character.setX(old_x);
            character.setY(old_y);
        } else {
            // Update old_x and old_y only if there’s no collision
            old_x = previousX;
            old_y = previousY;
        }

        // Handle shop collision
        if (result.hasShopCollision() && !isShopDialogOpen) {
            System.out.println("----------main shop");
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShopCollisionTime > COOLDOWN_TIME_MS/10) {
                lastShopCollisionTime = currentTime; // Update the last collision time

                isShopDialogOpen = true;

                ShopScreen shopScreen = new ShopScreen(gameFrame, character, collisionDetector, tmxRenderer, questions, questionPanel);
                shopScreen.setVisible(true);

                isShopDialogOpen = false; // Reset flag after ShopScreen is closed
                System.out.println("----------------------HAD SHOP COLLISION ----------------------------------");
            }
        }

        if (result.hasTransitionCollision()) {
            ObjectModel transitionObject = result.getTransitionObject();
            handleMapTransition(transitionObject);
        }


        // Handle NPC collision
        if (result.hasNPCCollision() && !isNpcDialogOpen) {
            System.out.println("----------main NPC");

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastNpcCollisionTime > 3500) {
                lastNpcCollisionTime = currentTime; // Update the last collision time
                isNpcDialogOpen = true;

                NPCScreen npcScreen = new NPCScreen(gameFrame, character, collisionDetector, tmxRenderer, questions, questionPanel);
                npcScreen.setVisible(true);

                isNpcDialogOpen = false; // Reset flag after NPCScreen is closed
                System.out.println("----------------------HAD NPC COLLISION -----------------------------------");

            }
        }
    }

    public void loadMap(String mapFileName, int spawnX, int spawnY) {
        currentMapFileName = mapFileName;
        try {
            // Parse the new .tmx file
            tmxParser = new TmxParser("resources/" + mapFileName);
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            // Check if a MapState exists for this map
            MapState mapState = mapStates.get(mapFileName);
            if (mapState == null) {
                mapState = new MapState(mapFileName);
                mapStates.put(mapFileName, mapState);
            } else {
                // Remove encountered objects
                MapState finalMapState = mapState;
                objects.removeIf(obj -> finalMapState.getEncounteredObjects().contains(obj.getName().toLowerCase()));
            }

            // Update the renderer and collision detector with new map data
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets, camera,  mapState.getEncounteredObjects());
            collisionDetector.updateObjects(objects);
            tmxRenderer.printObjectNames();

            // Set character position to spawn point
            character.setX(spawnX);
            character.setY(spawnY);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.err.println("Error loading new map: " + e.getMessage());
        }
    }
    public MapState getCurrentMapState() {
        return mapStates.get(currentMapFileName);
    }


    private void handleMapTransition(ObjectModel transitionObject) {
        String destinationMap = null;
        int spawnX = 0;
        int spawnY = 0;

        // Extract properties from the transition object
        for (ObjectPropertiesModel property : transitionObject.getProperties()) {
            if (property.getPropertyName().equalsIgnoreCase("destinationMap")) {
                destinationMap = property.getValue();
            } else if (property.getPropertyName().equalsIgnoreCase("spawnX")) {
                spawnX = Integer.parseInt(property.getValue());
            } else if (property.getPropertyName().equalsIgnoreCase("spawnY")) {
                spawnY = Integer.parseInt(property.getValue());
            }
        }

        if (destinationMap != null) {
            loadMap(destinationMap, spawnX, spawnY);
        } else {
            System.err.println("Transition object missing destinationMap property.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gameFrame = new JFrame("Go Study!");
            Main mainPanel = new Main();

            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setSize(700, 700); // Adjust based on map size
            gameFrame.add(mainPanel);
            gameFrame.setVisible(true);
            mainPanel.requestFocusInWindow();

            // Timer to refresh the display, simulating animation
            Timer timer = new Timer(16, e -> mainPanel.repaint()); // roughly 60 FPS
            timer.start();
        });
    }

}