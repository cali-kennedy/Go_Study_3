import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Main extends JPanel {

    private TmxRenderer tmxRenderer;

    public Main() {
        try {
            // Parse .tmx file and tileset files to populate models
            TmxParser tmxParser = new TmxParser("resources/small_test.tmx");
            TmxMapModel mapModel = tmxParser.getMapModel();
            List<LayerModel> layers = tmxParser.getLayers();
            List<ObjectModel> objects = tmxParser.getObjects();
            List<AnimationModel> animations = tmxParser.getAnimations();
            List<TilesetModel> tilesets = tmxParser.getTilesets();

            // Initialize the renderer with parsed map data
            tmxRenderer = new TmxRenderer(mapModel, layers, objects, animations, tilesets);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.err.println("Error initializing TmxRenderer: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tmxRenderer != null) {
            tmxRenderer.render(g);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TMX Map Renderer");
        Main mainPanel = new Main();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);  // Adjust based on map size
        frame.add(mainPanel);
        frame.setVisible(true);

        // Timer to refresh the display, simulating animation
        Timer timer = new Timer(16, e -> mainPanel.repaint());  // roughly 60 FPS
        timer.start();
    }
}
