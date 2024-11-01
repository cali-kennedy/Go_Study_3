import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TmxRenderer {
    private TmxMapModel mapModel;
    private HashMap<Integer, BufferedImage> tileImages;
    private List<LayerModel> layers;
    private List<ObjectModel> objects;
    private List<AnimationModel> animations;
    private List<TilesetModel> tilesets;
    private Camera camera;
    private Map<Integer, BufferedImage> animationFrameCache = new HashMap<>();

    public TmxRenderer(TmxMapModel mapModel, List<LayerModel> layers, List<ObjectModel> objects,
                       List<AnimationModel> animations, List<TilesetModel> tilesets, Camera camera) {
        this.mapModel = mapModel;
        this.layers = layers;
        this.objects = objects;
        this.animations = new ArrayList<>();  // Ensure animations list is initialized
        this.tilesets = tilesets;
        this.tileImages = new HashMap<>();
        this.camera = camera;
        loadTilesetImages();
        initializeAnimations(); // Initialize animations if not already populated
    }

    private void loadTilesetImages() {
        for (TilesetModel tileset : tilesets) {
            try {
                File tilesetFile = new File("resources/" + tileset.getImageSource());
                if (!tilesetFile.exists()) {
                    System.err.println("Tileset image file not found: " + tileset.getImageSource());
                    continue;
                }

                BufferedImage tilesetImage = ImageIO.read(tilesetFile);
                int tileWidth = (int) tileset.getWidth();
                int tileHeight = (int) tileset.getHeight();
                int columns = tileset.getColumns();

                for (int i = 0; i < tileset.getTileCount(); i++) {
                    int x = (i % columns) * tileWidth;
                    int y = (i / columns) * tileHeight;
                    tileImages.put(tileset.getFirstGid() + i, tilesetImage.getSubimage(x, y, tileWidth, tileHeight));
                }
            } catch (IOException e) {
                System.err.println("Error loading tileset image: " + e.getMessage());
            }
        }
    }

    // Generalized method to initialize animations for any object with an animation in its tileset
    private void initializeAnimations() {
        for (ObjectModel object : objects) {
            int gid = object.getGid();

            if (tileImages.containsKey(gid)) {  // Check if this object has associated tileset images
                AnimationModel animation = new AnimationModel(gid);  // Use object's GID as first GID
                animation.setX(object.getX());
                animation.setY(object.getY());

                // Assume frames in range from object's first gid (78) up to tile count, loop around if needed
                TilesetModel tileset = findTilesetForGid(gid);
                if (tileset != null) {
                    int tileCount = tileset.getTileCount();
                    int firstGid = tileset.getFirstGid();

                    // Loop animation frames within the tile count range for the tileset
                    for (int i = 0; i < Math.min(16, tileCount); i++) {  // Avoid going out of bounds
                        FrameModel frame = new FrameModel(firstGid + (i % tileCount), 300);
                        animation.addFrame(frame);
                    }

                    animations.add(animation);
                    System.out.println("Initialized animation for GID " + gid + " with frames at position (" +
                            object.getX() + ", " + object.getY() + ")");
                }
            } else {
                System.out.println("Object with GID " + gid + " does not have an animated tileset; skipping animation setup.");
            }
        }
    }

    private TilesetModel findTilesetForGid(int gid) {
        for (TilesetModel tileset : tilesets) {
            if (gid >= tileset.getFirstGid() && gid < tileset.getFirstGid() + tileset.getTileCount()) {
                return tileset;
            }
        }
        return null;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // First, render all static tiles in one batch
        for (LayerModel layer : layers) {
            for (int y = 0; y < layer.getLayerHeight(); y++) {
                for (int x = 0; x < layer.getLayerWidth(); x++) {
                    int tileId = layer.getTileIdAt(x, y);
                    if (tileId > 0 && tileImages.containsKey(tileId)) {
                        BufferedImage tile = tileImages.get(tileId);
                        g2d.drawImage(tile, x * mapModel.getTileWidth(), y * mapModel.getTileHeight(), null);
                    }
                }
            }
        }

        // Then, render all animated objects in one batch
        for (AnimationModel animation : animations) {
            animation.update();
            int tileId = animation.getCurrentTileId();

            // Check if the frame is cached
            BufferedImage frame = animationFrameCache.get(tileId);

            if (frame == null) {
                // If not cached, retrieve and cache it
                frame = tileImages.get(tileId);
                if (frame != null) {
                    animationFrameCache.put(tileId, frame);
                }
            }

            if (frame != null) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY(), 32, 32, null);
            }
        }




        // Render animated objects dynamically based on animations list
        Rectangle cameraBounds = new Rectangle((int) camera.getX(), (int) camera.getY(), camera.getWidth(), camera.getHeight());

        for (AnimationModel animation : animations) {
            Rectangle objectBounds = new Rectangle((int) animation.getX(), (int) animation.getY(), 32, 32);
            if (!cameraBounds.intersects(objectBounds)) {
                continue;  // Skip objects outside the camera view
            }

            animation.update();  // Only update and render if visible
            int tileId = animation.getCurrentTileId();
            if (tileId > 0 && tileImages.containsKey(tileId)) {
                BufferedImage tile = tileImages.get(tileId);
                g.drawImage(tile, (int) animation.getX(), (int) animation.getY(), 32, 32, null);
            }
        }

    }
}