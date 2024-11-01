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
import java.util.List;
import java.util.Comparator;

public class TmxParser {
    private TmxMapModel mapModel;
    private List<LayerModel> layers = new ArrayList<>();      // Initialize as ArrayList
    private List<ObjectModel> objects = new ArrayList<>();    // Initialize as ArrayList
    private TilesetParser tilesetParser;
    private List<TilesetModel> tilesets = new ArrayList<>(); // Tilesets initialized as ArrayList
    private List<ObjectPropertiesModel> objectPropertiesModels = new ArrayList<>(); // Tilesets initialized as ArrayList
    public TmxParser(String tmxFilePath) throws IOException, ParserConfigurationException, SAXException {
        parseTmxFile(tmxFilePath);
    }

    // Method to parse the .tmx file and extract the tileset sources and firstGids
    private void parseTmxFile(String tmxFilePath) throws IOException, ParserConfigurationException, SAXException {
        File file = new File(tmxFilePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        this.mapModel = new TmxMapModel();

        Element tmxMap = document.getDocumentElement();

        // Set attributes of tmxMap model
        mapModel.setMapWidth(Integer.parseInt(tmxMap.getAttribute("width")));
        mapModel.setMapHeight(Integer.parseInt(tmxMap.getAttribute("height")));
        mapModel.setTileWidth(Integer.parseInt(tmxMap.getAttribute("tilewidth")));
        mapModel.setTileHeight(Integer.parseInt(tmxMap.getAttribute("tileheight")));
        // Debugging
        System.out.println("Map Height:   " + mapModel.getMapHeight());
        System.out.println("Map Width:    " + mapModel.getMapWidth());
        System.out.println("Tile Height:  " + mapModel.getTileHeight());
        System.out.println("Tile Width:   " + mapModel.getTileWidth());

        // Parse tilesets
        NodeList tileSetNodes = tmxMap.getElementsByTagName("tileset");
        for (int j = 0; j < tileSetNodes.getLength(); j++) {
            Element tilesetElement = (Element) tileSetNodes.item(j);
            TilesetModel tileSetModel = new TilesetModel();
            tileSetModel.setFirstGid(Integer.parseInt(tilesetElement.getAttribute("firstgid")));
            tileSetModel.setTilesetSource(tilesetElement.getAttribute("source"));
            System.out.println("First gid:    " + tileSetModel.getFirstGid());
            System.out.println("Tileset source: " + tileSetModel.getTilesetSource());

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

        // Parse object groups and objects
        NodeList objectGroupNodes = tmxMap.getElementsByTagName("objectgroup");
        for (int j = 0; j < objectGroupNodes.getLength(); j++) {
            Element objectGroupElement = (Element) objectGroupNodes.item(j);
            objectGroupModel objectGroup = new objectGroupModel();
            objectGroup.setId(Integer.parseInt(objectGroupElement.getAttribute("id")));
            objectGroup.setName(objectGroupElement.getAttribute("name"));
            System.out.println("Object Group Model ID: " + objectGroup.getId());
            System.out.println("Object Group Model Name: " + objectGroup.getName());

            NodeList objectNodes = objectGroupElement.getElementsByTagName("object");
            for (int i = 0; i < objectNodes.getLength(); i++) {
                Element objectElement = (Element) objectNodes.item(i);
                ObjectModel object = new ObjectModel();
                // Set object attributes
                object.setLayerName(objectGroupElement.getAttribute("name"));
                object.setName(objectElement.getAttribute("name"));
                object.setId(Integer.parseInt(objectElement.getAttribute("id")));
                if (!objectElement.getAttribute("gid").isEmpty()) {
                    object.setGid(Integer.parseInt(objectElement.getAttribute("gid")));
                }
                if (objectElement.hasAttribute("width") && objectElement.hasAttribute("height")) {
                    object.setHeight(Double.parseDouble(objectElement.getAttribute("height")));
                    object.setWidth(Double.parseDouble(objectElement.getAttribute("width")));
                } else {
                    continue; // Skip objects without width and height
                }
                object.setX(Double.parseDouble(objectElement.getAttribute("x")));
                object.setY(Double.parseDouble(objectElement.getAttribute("y")));

                // Print object attributes for debugging
                System.out.println("Object Name:   " + object.getName());
                System.out.println("Object id:     " + object.getId());
                System.out.println("Object gid:    " + object.getGid());
                System.out.println("Object height: " + object.getHeight());
                System.out.println("Object width:  " + object.getWidth());
                System.out.println("Object x:      " + object.getX());
                System.out.println("Object y:      " + object.getY());

                // Parse object properties
                NodeList propertyNodes = objectElement.getElementsByTagName("property");
                for (int k = 0; k < propertyNodes.getLength(); k++) {
                    Element propertyElement = (Element) propertyNodes.item(k);
                    ObjectPropertiesModel property = new ObjectPropertiesModel();
                    property.setPropertyName(propertyElement.getAttribute("name"));
                    property.setObjectName(objectElement.getAttribute("name"));
                    property.setType(propertyElement.getAttribute("type"));
                    property.setValue(propertyElement.getAttribute("value"));

                    System.out.println("Object Property Name: " + property.getPropertyName());
                    System.out.println("Object Type: " + property.getType());
                    System.out.println("Object Value: " + property.getValue());
                   // objectPropertiesModels.add(property);
                    object.addProperty(property);
                    System.out.println(object.getProperties().toString());
                }

                objects.add(object); // Add the object to the objects list

            }
        }
    }

    // Getters for parsed data
    public TmxMapModel getMapModel() {
        return this.mapModel;
    }

    public List<LayerModel> getLayers() {
        return this.layers;
    }

    public List<ObjectModel> getObjects() {
        return this.objects;
    }

    public List<AnimationModel> getAnimations() {
        return this.tilesetParser.getAnimationModel();
    }

    public List<TilesetModel> getTilesets() {
        sortTilesetsByFirstGid();
        return this.tilesets;
    }
    // Sort tilesets by firstGid
    private void sortTilesetsByFirstGid() {
        tilesets.sort(Comparator.comparingInt(TilesetModel::getFirstGid));
    }
    public List<ObjectPropertiesModel> getObjectProperties(){
        return objectPropertiesModels;
    }

}