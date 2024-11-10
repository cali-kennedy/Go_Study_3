import models.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Parses a .tmx file to extract information about map properties, layers, tilesets, and objects.
 * The extracted information is stored in various model classes.
 */
public class TmxParser {

    private TmxMapModel mapModel;
    private List<LayerModel> layers = new ArrayList<>();
    private List<ObjectModel> objects = new ArrayList<>();
    private TilesetParser tilesetParser;
    private List<TilesetModel> tilesets = new ArrayList<>();
    private List<ObjectPropertiesModel> objectPropertiesModels = new ArrayList<>();
    // Map to track names and ensure unique names for duplicate objects
    private HashMap<String, Integer> nameCountMap = new HashMap<>();

    /**
     * Initializes the parser by loading and parsing the specified .tmx file.
     *
     * @param tmxFilePath Path to the .tmx file to parse.
     * @throws IOException                  If file access fails.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws SAXException                 If an error occurs during XML parsing.
     */
    public TmxParser(String tmxFilePath) throws IOException, ParserConfigurationException, SAXException {
        parseTmxFile(tmxFilePath);
    }

    /**
     * Parses the .tmx file and extracts properties for map, layers, tilesets, and objects.
     *
     * @param tmxFilePath Path to the .tmx file to parse.
     * @throws IOException                  If file access fails.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws SAXException                 If an error occurs during XML parsing.
     */
    private void parseTmxFile(String tmxFilePath) throws IOException, ParserConfigurationException, SAXException {
        File file = new File(tmxFilePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        this.mapModel = new TmxMapModel();
        Element tmxMap = document.getDocumentElement();

        // Set map attributes
        mapModel.setMapWidth(Integer.parseInt(tmxMap.getAttribute("width")));
        mapModel.setMapHeight(Integer.parseInt(tmxMap.getAttribute("height")));
        mapModel.setTileWidth(Integer.parseInt(tmxMap.getAttribute("tilewidth")));
        mapModel.setTileHeight(Integer.parseInt(tmxMap.getAttribute("tileheight")));

        // Parse tilesets
        NodeList tileSetNodes = tmxMap.getElementsByTagName("tileset");
        for (int j = 0; j < tileSetNodes.getLength(); j++) {
            Element tilesetElement = (Element) tileSetNodes.item(j);
            TilesetModel tileSetModel = new TilesetModel();

            tileSetModel.setFirstGid(Integer.parseInt(tilesetElement.getAttribute("firstgid")));
            tileSetModel.setTilesetSource(tilesetElement.getAttribute("source"));

            // Initialize and parse tileset details using TilesetParser
            this.tilesetParser = new TilesetParser(tileSetModel.getTilesetSource(), tileSetModel);
            tilesets.add(tileSetModel);
        }

        // Parse layers
        NodeList layerNodes = tmxMap.getElementsByTagName("layer");
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element layerElement = (Element) layerNodes.item(i);
            LayerModel layer = new LayerModel(
                    layerElement.getAttribute("name"),
                    Integer.parseInt(layerElement.getAttribute("width")),
                    Integer.parseInt(layerElement.getAttribute("height")),
                    layerElement.getElementsByTagName("data").item(0).getTextContent().trim(),
                    Integer.parseInt(layerElement.getAttribute("id"))
            );
            layers.add(layer);
        }

        // Parse object groups and individual objects within each group
        NodeList objectGroupNodes = tmxMap.getElementsByTagName("objectgroup");
        for (int j = 0; j < objectGroupNodes.getLength(); j++) {
            Element objectGroupElement = (Element) objectGroupNodes.item(j);
            objectGroupModel objectGroup = new objectGroupModel();

            objectGroup.setId(Integer.parseInt(objectGroupElement.getAttribute("id")));
            objectGroup.setName(objectGroupElement.getAttribute("name"));

            NodeList objectNodes = objectGroupElement.getElementsByTagName("object");
            for (int i = 0; i < objectNodes.getLength(); i++) {
                Element objectElement = (Element) objectNodes.item(i);
                ObjectModel object = new ObjectModel();

                String baseName = objectElement.getAttribute("name");
                String uniqueName = getUniqueObjectName(baseName);  // Get unique name

                object.setName(uniqueName);  // Assign unique name to the object
                System.out.println("Object ID: " + objectElement.getAttribute("id") + " - Name: " + uniqueName);

                // Set other attributes as before
                object.setLayerName(objectGroupElement.getAttribute("name"));
                object.setId(Integer.parseInt(objectElement.getAttribute("id")));

                if (!objectElement.getAttribute("gid").isEmpty()) {
                    object.setGid(Integer.parseInt(objectElement.getAttribute("gid")));
                }
                if (objectElement.hasAttribute("width") && objectElement.hasAttribute("height")) {
                    object.setHeight(Double.parseDouble(objectElement.getAttribute("height")));
                    object.setWidth(Double.parseDouble(objectElement.getAttribute("width")));
                } else {
                    continue; // Skip objects without width and height attributes
                }

                object.setX(Double.parseDouble(objectElement.getAttribute("x")));
                object.setY(Double.parseDouble(objectElement.getAttribute("y")));

                // Parse object properties
                NodeList propertyNodes = objectElement.getElementsByTagName("property");
                for (int k = 0; k < propertyNodes.getLength(); k++) {
                    Element propertyElement = (Element) propertyNodes.item(k);
                    ObjectPropertiesModel property = new ObjectPropertiesModel();

                    property.setPropertyName(propertyElement.getAttribute("name"));
                    property.setObjectName(uniqueName);  // Set unique name for property association
                    property.setType(propertyElement.getAttribute("type"));
                    property.setValue(propertyElement.getAttribute("value"));

                    object.addProperty(property);
                }
                objects.add(object);
            }
        }
    }

    /**
     * Generates a unique object name by appending a number if the base name already exists.
     *
     * @param baseName The base name to make unique.
     * @return A unique name based on the base name.
     */
    private String getUniqueObjectName(String baseName) {
        if (baseName == null || baseName.isEmpty()) {
            baseName = "Unnamed_Object";
        }

        // Check if the name already exists in the map
        int count = nameCountMap.getOrDefault(baseName, 0);
        String uniqueName = baseName;

        // If the base name exists, append a count to make it unique
        if (count > 0) {
            uniqueName = baseName + "_" + count;
        }

        // Update the name count in the map
        nameCountMap.put(baseName, count + 1);

        return uniqueName;
    }

    /**
     * @return The models.TmxMapModel containing parsed map dimensions and tile properties.
     */
    public TmxMapModel getMapModel() {
        return this.mapModel;
    }

    /**
     * @return List of models.LayerModel objects representing map layers.
     */
    public List<LayerModel> getLayers() {
        return this.layers;
    }

    /**
     * @return List of models.ObjectModel objects representing map objects.
     */
    public List<ObjectModel> getObjects() {
        return this.objects;
    }

    /**
     * @return List of models.AnimationModel objects parsed from the tilesets.
     */
    public List<AnimationModel> getAnimations() {
        return this.tilesetParser.getAnimationModel();
    }

    /**
     * Sorts tilesets by firstGid and returns the sorted list.
     *
     * @return List of models.TilesetModel objects.
     */
    public List<TilesetModel> getTilesets() {
        sortTilesetsByFirstGid();
        return this.tilesets;
    }

    /**
     * Sorts the tilesets list by firstGid.
     */
    private void sortTilesetsByFirstGid() {
        tilesets.sort(Comparator.comparingInt(TilesetModel::getFirstGid));
    }

    /**
     * @return List of models.ObjectPropertiesModel representing properties of objects.
     */
    public List<ObjectPropertiesModel> getObjectProperties(){
        return objectPropertiesModels;
    }

    /**
     * Finds the models.TilesetModel associated with a given GID by checking ranges based on
     * firstGid and tile count.
     *
     * @param gid The global tile ID to locate.
     * @return The corresponding models.TilesetModel, or null if not found.
     */
    public TilesetModel findTilesetForGid(int gid) {
        sortTilesetsByFirstGid(); // Ensure sorted order for range checks

        for (TilesetModel tileset : tilesets) {
            int firstGid = tileset.getFirstGid();
            int tileCount = tileset.getTileCount();

            // Check if gid is within the range for this tileset
            if (gid >= firstGid && gid < firstGid + tileCount) {
                return tileset;
            }
        }
        return null; // No tileset found for this gid
    }
}
