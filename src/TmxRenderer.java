import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class TmxRenderer {
    private TmxMapModel mapModel;
    private HashMap<Integer, BufferedImage> tileImages;  // Mapping from tile ID to image
    private List<LayerModel> layers;
    private List<ObjectModel> objects;
    private List<AnimationModel> animations;
    private List<TilesetModel> tilesets;

    public TmxRenderer(TmxMapModel mapModel, List<LayerModel> layers, List<ObjectModel> objects,
                       List<AnimationModel> animations, List<TilesetModel> tilesets) {
        this.mapModel = mapModel;
        this.layers = layers;
        this.objects = objects;
        this.animations = animations;
        this.tilesets = tilesets;
        this.tileImages = new HashMap<>();
        loadTilesetImages();
    }

    private void loadTilesetImages() {
        for (TilesetModel tileset : tilesets) {
            try {
                // Load tileset image file and verify existence
                File tilesetFile = new File("resources/" + tileset.getImageSource());
                if (!tilesetFile.exists()) {
                    System.err.println("Tileset image file not found: " + tileset.getImageSource());
                    continue;
                }

                BufferedImage tilesetImage = ImageIO.read(tilesetFile);
                int tileWidth = (int) tileset.getWidth();
                int tileHeight = (int) tileset.getHeight();
                int firstGid = tileset.getFirstGid();
                int tileCount = tileset.getTileCount();

                System.out.println("Loading tileset: " + tileset.getImageSource() +
                        " with First GID: " + firstGid + ", Tile Count: " + tileCount);

                int tileId = firstGid;
                for (int y = 0; y < tileset.getImageHeight(); y += tileHeight) {
                    for (int x = 0; x < tileset.getImageWidth(); x += tileWidth) {
                        if (tileId <= firstGid + tileCount) {
                            BufferedImage tileImage = tilesetImage.getSubimage(x, y, tileWidth, tileHeight);
                            tileImages.put(tileId, tileImage);
                            System.out.println("Loaded tile for tile ID: " + tileId);  // Debugging output
                            tileId++;
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to load tileset image: " + tileset.getImageSource());
            }
        }

        System.out.println("Summary of loaded tile IDs:");
        tileImages.keySet().forEach(id -> System.out.println("Loaded tile ID: " + id));
        if (!tileImages.containsKey(13)) {
            System.err.println("Tile ID 13 is missing from tile images after loading.");
        } else {
            System.out.println("Tile ID 13 loaded successfully.");
        }
    }



    public void renderMap(Graphics g) {
        renderLayers(g);
        renderObjects(g);
        renderAnimations(g);
    }

    private void renderLayers(Graphics g) {
        for (LayerModel layer : layers) {
            System.out.println("Rendering layer: " + layer.getLayerName());
            int layerWidth = layer.getLayerWidth();
            int layerHeight = layer.getLayerHeight();

            for (int y = 0; y < layerHeight; y++) {
                for (int x = 0; x < layerWidth; x++) {
                    int tileId = getTileIdFromLayer(layer, x, y);

                    if (!tileImages.containsKey(tileId)) {
                        System.err.println("Missing tile image for tile ID: " + tileId);
                        continue;
                    }

                    BufferedImage tileImage = tileImages.get(tileId);
                    if (tileImage != null) {
                        g.drawImage(tileImage, x * mapModel.getTileWidth(), y * mapModel.getTileHeight(), null);
                    }
                }
            }
        }
    }

    private int getTileIdFromLayer(LayerModel layer, int x, int y) {
        return layer.getTileIdAt(x, y);
    }

    private void renderObjects(Graphics g) {
        for (ObjectModel object : objects) {
            BufferedImage objectImage = tileImages.get(object.getGid());
            if (objectImage != null) {
                g.drawImage(objectImage, (int) object.getX(), (int) object.getY(),
                        (int) object.getWidth(), (int) object.getHeight(), null);
            }
        }
    }

    private void renderAnimations(Graphics g) {
        long currentTime = System.currentTimeMillis();
        for (AnimationModel animation : animations) {
            animation.update();
            int localTileId = animation.getCurrentTileId();
            int globalTileId = localTileId + 78;

            if (!tileImages.containsKey(globalTileId)) {
                System.err.println("Animation frame missing for tile ID: " + globalTileId);
                continue;
            }

            BufferedImage frameImage = tileImages.get(globalTileId);
            if (frameImage != null) {
                g.drawImage(frameImage, 100, 100, null);
            }
        }
    }
}
