import models.AnimationModel;
import models.FrameModel;
import models.TilesetModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses an XML tileset file and extracts data including tile properties, images, and animations.
 */
public class TilesetParser {

    private static final String RESOURCE_PATH = "resources/";
    private static final String FILE_EXTENSION = ".tsx";

    private String tilesetName;
    private List<AnimationModel> animationsList = new ArrayList<>();
    private List<TilesetModel> tilesets = new ArrayList<>();

    /**
     * Constructs a TilesetParser and initiates parsing of the specified tileset XML file.
     *
     * @param tilesetName  The name of the tileset file (e.g., "tileset.tsx").
     * @param tilesetModel The models.TilesetModel instance to populate with parsed data.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws IOException                  If an error occurs during file access.
     * @throws SAXException                 If an error occurs during XML parsing.
     */
    public TilesetParser(String tilesetName, TilesetModel tilesetModel)
            throws ParserConfigurationException, IOException, SAXException {
        this.tilesetName = tilesetName;
        parseTileset(RESOURCE_PATH + tilesetName, tilesetModel);
    }

    /**
     * Parses the tileset XML file to populate the models.TilesetModel with tile attributes,
     * image details, and animation sequences.
     *
     * @param tilesetPath  The path to the tileset XML file.
     * @param tilesetModel The model to populate with parsed data.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws IOException                  If an error occurs during file access.
     * @throws SAXException                 If an error occurs during XML parsing.
     */
    private void parseTileset(String tilesetPath, TilesetModel tilesetModel)
            throws ParserConfigurationException, IOException, SAXException {

        // Load and parse the XML file
        File file = new File(tilesetPath);
        System.out.println("------------- tilesetparser invoked -------------  ");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        // Root element representing the tileset data
        Element tilesetRoot = document.getDocumentElement();

        // Set basic tile properties
        tilesetModel.setHeight(Double.parseDouble(tilesetRoot.getAttribute("tileheight")));
        tilesetModel.setWidth(Double.parseDouble(tilesetRoot.getAttribute("tilewidth")));
        tilesetModel.setTileCount(Integer.parseInt(tilesetRoot.getAttribute("tilecount")));
        tilesetModel.setColumns(Integer.parseInt(tilesetRoot.getAttribute("columns")));

        // Parse and set image properties within the tileset
        NodeList imageNodes = tilesetRoot.getElementsByTagName("image");
        Element imageElement = (Element) imageNodes.item(0);
        tilesetModel.setImageWidth(Integer.parseInt(imageElement.getAttribute("width")));
        tilesetModel.setImageHeight(Integer.parseInt(imageElement.getAttribute("height")));
        tilesetModel.setImageSource(imageElement.getAttribute("source"));
        System.out.println("TILESET SOURCE: "+tilesetModel.getTilesetSource());


        tilesets.add(tilesetModel);

        // Parse and set animations in the tileset
        NodeList animationNodes = tilesetRoot.getElementsByTagName("animation");
        for (int j = 0; j < animationNodes.getLength(); j++) {
            System.out.println("---PARSING AN ANIMATION---");
            Element animationElement = (Element) animationNodes.item(j);

            // Initialize animation with the base tile GID (first global tile ID)
            AnimationModel animationModel = new AnimationModel(tilesetModel.getFirstGid());
            animationModel.setName(tilesetModel.getTilesetSource().replace(FILE_EXTENSION, ""));
            System.out.println("ANIMATION MODEL: " + animationModel.getName()); // Debugging output
            System.out.println("ANIMATION GID: " + animationModel.getFirstGid());

            // Parse frames for each animation
            NodeList frameNodes = animationElement.getElementsByTagName("frame");
            for (int i = 0; i < frameNodes.getLength(); i++) {
                System.out.println("--PARSING A FRAME--");
                Element frameElement = (Element) frameNodes.item(i);

                // Calculate global tile ID by adding base GID to the frame's tile ID
                int tileId = Integer.parseInt(frameElement.getAttribute("tileid")) + tilesetModel.getFirstGid();
                int duration = Integer.parseInt(frameElement.getAttribute("duration"));

                // Create frame with calculated tile ID and duration, then add to animation
                FrameModel frameModel = new FrameModel(tileId, duration);
                animationModel.addFrame(frameModel);

                System.out.println("Adding frame to animation: tileId=" + tileId + ", duration=" + duration); // Debug output
            }

            // Add complete animation to the tileset
            tilesetModel.addAnimation(animationModel);
        }
    }

    /**
     * Retrieves the list of animations parsed from the tileset.
     *
     * @return A list of models.AnimationModel instances.
     */
    public List<AnimationModel> getAnimationModel() {
        System.out.println("----TilesetParser.java : getAnimationModel invoked ----");
        System.out.println("LIST RETURNED: ");
        if (animationsList != null && !animationsList.isEmpty()) {
            System.out.println("Animation Models List:");
            for (AnimationModel animation : animationsList) {
                System.out.println(animation); // Ensure AnimationModel has a meaningful toString() method for proper output
            }
        } else {
            System.out.println("Animation Models List is empty or null.");
        }
        return animationsList;
    }

    /**
     * Retrieves the list of parsed tileset models.
     *
     * @return A list of models.TilesetModel instances.
     */
    public List<TilesetModel> getTilesets() {
        System.out.println("----TilesetParser.java : getTilesets invoked ----");
        System.out.println("LIST RETURNED: ");
        if (tilesets != null && !tilesets.isEmpty()) {
            System.out.println("Tilesets List:");
            for (TilesetModel tileset : tilesets) {
                System.out.println(tileset); // Ensure TilesetModel has a meaningful toString() method for proper output
            }
        } else {
            System.out.println("Tilesets List is empty or null.");
        }
        return tilesets;
    }
}