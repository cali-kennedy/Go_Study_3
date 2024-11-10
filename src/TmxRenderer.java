import models.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class TmxRenderer {
    private static final int FRAME_DURATION_MS = 300;      // Default duration for animation frames in milliseconds
    private static final int TILE_RENDER_SIZE = 32;        // Size in pixels for rendering tiles
    private static final int ENEMY_RENDER_SIZE = 128;      // Size in pixels for rendering enemies
    private Set<String> defeatedEnemies = new HashSet<>();
    private TmxMapModel mapModel;
    private HashMap<Integer, BufferedImage> tileImages;
    private List<LayerModel> layers;
    private List<ObjectModel> objects;
    private List<AnimationModel> animations;
    private List<TilesetModel> tilesets;
    private Camera camera;
    private Map<Integer, BufferedImage> animationFrameCache = new HashMap<>();
    private Component displayComponent;
    private Graphics2D g2d;
    /**
     * Initializes the renderer with the necessary models, layers, and camera view.
     *
     * @param mapModel   Map model containing map properties like width, height, tile size.
     * @param layers     Layers in the map to be rendered.
     * @param objects    Objects on the map, such as enemies or items.
     * @param animations Animation definitions for animatable objects.
     * @param tilesets   Collection of tilesets used by the map.
     * @param camera     Camera to control which part of the map is visible.
     */
    public TmxRenderer(TmxMapModel mapModel, List<LayerModel> layers, List<ObjectModel> objects,
                       List<AnimationModel> animations, List<TilesetModel> tilesets, Camera camera) {
        this.mapModel = mapModel;
        this.layers = layers;
        this.objects = objects;
        this.animations = new ArrayList<>();
        this.tilesets = tilesets;
        this.tileImages = new HashMap<>();
        this.camera = camera;
        loadTilesetImages();
        initializeAnimations();
    }

    /**
     * Loads all tileset images, splits them into individual tiles, and caches them for fast access.
     */
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

                // Calculate x and y positions of the tile within the tileset image grid:
                for (int i = 0; i < tileset.getTileCount(); i++) {

                    // `i % columns` gives the tile’s column position in its row,
                    // and multiplying by `tileWidth` gives the exact x-coordinate in pixels.
                    int x = (i % columns) * tileWidth;

                    // `i / columns` gives the row number of tile `i`,
                    // and multiplying by `tileHeight` gives the exact y-coordinate in pixels.
                    int y = (i / columns) * tileHeight;

                    // Extract the tile as a subimage and store it in `tileImages` with its GID key.
                    tileImages.put(tileset.getFirstGid() + i, tilesetImage.getSubimage(x, y, tileWidth, tileHeight));
                }
            } catch (IOException e) {
                System.err.println("Error loading tileset image: " + e.getMessage());
            }
        }
    }

    /**
     * Initializes animations for each animatable object, setting its frames from the appropriate tileset.
     */
    private void initializeAnimations() {
        for (ObjectModel object : objects) {
            int gid = object.getGid();

            if (tileImages.containsKey(gid)) {

                AnimationModel animation = new AnimationModel(gid);
                animation.setX(object.getX());
                animation.setY(object.getY());

                // Set name or default if object name is not provided
                animation.setName(object.getName() != null ? object.getName().replace(".tsk", "") : "UnnamedEnemy");

                // Set up frames for animations based on tileset properties
                TilesetModel tileset = findTilesetForGid(gid);
                if (tileset != null) {
                    int tileCount = tileset.getTileCount();
                    int firstGid = tileset.getFirstGid();

                    // Add frames, cycling through tile IDs to stay within `tileCount` limit.
                    for (int i = 0; i < Math.min(16, tileCount); i++) {
                        // `(i % tileCount)` cycles `i` from 0 to tileCount-1, creating a looping effect.
                        // Adding `firstGid` adjusts each tile ID to be within the tileset's unique ID range.
                        FrameModel frame = new FrameModel(firstGid + (i % tileCount), FRAME_DURATION_MS);

                        // Add the frame to the animation, maintaining a sequence within tile bounds.
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

    /**
     * Finds the tileset that corresponds to a given GID (global tile ID).
     *
     * @param gid The GID of the tile.
     * @return The matching models.TilesetModel or null if not found.
     */
    private TilesetModel findTilesetForGid(int gid) {
        for (TilesetModel tileset : tilesets) {
            if (gid >= tileset.getFirstGid() && gid < tileset.getFirstGid() + tileset.getTileCount()) {
                return tileset;
            }
        }
        return null;
    }
    public void markEnemyAsDefeated(String enemyName) {
        defeatedEnemies.add(enemyName);
        //objects.removeIf(object -> object.getName().equalsIgnoreCase(enemyName));
        for (AnimationModel animation : animations) {
            if (animation.getName().equalsIgnoreCase(enemyName)) {
                animation.setDefeated(true);
                break;
            }
        }
        System.out.println("\ntmxrenderer ENEMY NAME : " + enemyName);
        for (ObjectModel object : objects) {
            if(object.isDefeated()) {continue;}
            System.out.println("looping through tmxrenderer OBJECT NAME" + object.getName());
            if (object.getName().equalsIgnoreCase(enemyName)) {

                System.out.println("tmxrender OBJECT NAME MATCH : " + object.getName());
                object.setDefeated(true); // Mark the enemy as defeated
                break;
            }
            updateDefeatedEnemies();
        }
    }
    /**
     * Renders the map, including static tiles and animations, based on the camera's position.
     *
     * @param g The graphics context used for drawing.
     */
    public void render(Graphics g) {
         g2d = (Graphics2D) g;

        // Render static tiles layer by layer
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

        // Render animated objects, skipping defeated ones
        for (AnimationModel animation : animations) {
            if (animation.isDefeated()){ continue;} // Skip defeated animations

            animation.update();  // Update animation to get the current frame
            int tileId = animation.getCurrentTileId();
            BufferedImage frame = animationFrameCache.get(tileId);

            if (frame == null) {
                frame = tileImages.get(tileId);
                if (frame != null) {
                    animationFrameCache.put(tileId, frame);
                }
            }

            if (frame != null) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY(), TILE_RENDER_SIZE, TILE_RENDER_SIZE, null);
            }
        }
    }


    /**
     * Renders a specific enemy animation at given screen coordinates.
     *
     * @param enemyName The name of the enemy animation to render.
     * @param x         The x-coordinate for rendering.
     * @param y         The y-coordinate for rendering.
     * @param g         The graphics context.
     */
    public void renderEnemyAnimation(String enemyName, int x, int y, Graphics g) {
        updateDefeatedEnemies();
        AnimationModel enemyAnimation = null;
        for (AnimationModel animation : animations) {
            if (animation.getName().equalsIgnoreCase(enemyName)) {
                enemyAnimation = animation;
                break;
            }
        }

        if (enemyAnimation == null) {
            System.out.println("Enemy animation not found for: " + enemyName);
            return;
        }

        // Update and get current frame of the enemy animation
        enemyAnimation.update();
        int tileId = enemyAnimation.getCurrentTileId();
        BufferedImage frame = animationFrameCache.get(tileId);

        if (frame == null) {
            frame = tileImages.get(tileId);
            if (frame != null) {
                animationFrameCache.put(tileId, frame);
            }
        }

        // Draw the frame if it exists
        if (frame != null) {
            g.drawImage(frame, x, y, ENEMY_RENDER_SIZE, ENEMY_RENDER_SIZE, null);
        }

    }
    // Method to trigger repaint
    public void repaintMap() {
        if (displayComponent != null) {
            displayComponent.repaint(); // Forces a full repaint
        }
    }

    public void updateDefeatedEnemies() {
            animations.removeIf(AnimationModel::isDefeated);
            objects.removeIf(ObjectModel::isDefeated); // Clean up objects as well
        }

    }

