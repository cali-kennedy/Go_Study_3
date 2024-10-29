import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Main extends JPanel {

    private TmxRenderer tmxRenderer;
    private Camera camera;
    public Main() {
        try {
            // Parse .tmx file and tileset files to populate models
            TmxParser tmxParser = new TmxParser("resources/large_test.tmx");
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();
            camera = new Camera(400, 400, 2.0f);
            // Initialize the renderer with parsed map data
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.err.println("Error initializing TmxRenderer: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (tmxRenderer != null) {
          //  tmxRenderer.render(g);
            camera.update(20*16,20*16);
            camera.applyTransform(g2d);
            tmxRenderer.render(g);

        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TMX Map Renderer");
        Main mainPanel = new Main();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);  // Adjust based on map size
        frame.add(mainPanel);
        frame.setVisible(true);

        // Timer to refresh the display, simulating animation
        Timer timer = new Timer(16, e -> mainPanel.repaint());  // roughly 60 FPS
        timer.start();
    }
}
