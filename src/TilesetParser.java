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

public class TilesetParser {
    private String tilesetName;
    private AnimationModel animationModel;
    private FrameModel frameModel;
    private List<AnimationModel> animationsList = new ArrayList<>();
    private List<TilesetModel> tilesets = new ArrayList<>();

    public TilesetParser(String tilesetName, TilesetModel tilesetModel) throws ParserConfigurationException, IOException, SAXException {
        this.tilesetName = tilesetName;
        parseTileset("resources/" + tilesetName, tilesetModel);
    }

    private void parseTileset(String tilesetName, TilesetModel tilesetModel) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(tilesetName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        Element tilesetRoot = document.getDocumentElement();

        tilesetModel.setHeight(Double.parseDouble(tilesetRoot.getAttribute("tileheight")));
        tilesetModel.setWidth(Double.parseDouble(tilesetRoot.getAttribute("tilewidth")));
        tilesetModel.setTileCount(Integer.parseInt(tilesetRoot.getAttribute("tilecount")));
        tilesetModel.setColumns(Integer.parseInt(tilesetRoot.getAttribute("columns")));

        NodeList imageNodes = tilesetRoot.getElementsByTagName("image");
        Element imageElement = (Element) imageNodes.item(0);
        tilesetModel.setImageWidth(Integer.parseInt(imageElement.getAttribute("width")));
        tilesetModel.setImageHeight(Integer.parseInt(imageElement.getAttribute("height")));
        tilesetModel.setImageSource(imageElement.getAttribute("source"));

        tilesets.add(tilesetModel);
        NodeList animationNode = tilesetRoot.getElementsByTagName("animation");
        for (int j = 0; j < animationNode.getLength(); j++) {
            Element animationElement = (Element) animationNode.item(j);
            AnimationModel animationModel = new AnimationModel(tilesetModel.getFirstGid());

            NodeList frameNodes = animationElement.getElementsByTagName("frame");
            for (int i = 0; i < frameNodes.getLength(); i++) {
                Element frameElement = (Element) frameNodes.item(i);
                int id = Integer.parseInt(frameElement.getAttribute("tileid"));
                int duration = Integer.parseInt(frameElement.getAttribute("duration"));
                FrameModel frameModel = new FrameModel(id, duration);
                animationModel.addFrame(frameModel);
            }
            animationsList.add(animationModel);
        }
    }

    public List<AnimationModel> getAnimationModel() {
        return animationsList;
    }

    public List<TilesetModel> getTilesets() {
        return tilesets;
    }
}