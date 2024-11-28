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
    private static final int TILE_RENDER_SIZE = 64;        // Size in pixels for rendering tiles
    private static final int ENEMY_RENDER_SIZE = 128;      // Size in pixels for rendering enemies
    private Set<String> encounteredObjects = new HashSet<>();
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
    private Set<String> sixtyFourBitObjects;
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
                       List<AnimationModel> animations, List<TilesetModel> tilesets, Camera camera, Set<String> encounteredObjects) {
        System.out.println("---- TMX RENDERER INVOKED ----");
        this.mapModel = mapModel;
        this.layers = layers;
        this.objects = objects;
        this.animations = new ArrayList<>();
        this.tilesets = tilesets;
        this.tileImages = new HashMap<>();
        this.camera = camera;
        sixtyFourBitObjects = new HashSet<>(Arrays.asList("pink_shop", "brown_shop", "shop","gnome","gnome_1","gnome_2"));
        this.encounteredObjects = encounteredObjects;


        loadTilesetImages();
        initializeAnimations();

    }

    /**
     * Loads all tileset images, splits them into individual tiles, and caches them for fast access.
     */
    private void loadTilesetImages() {
        System.out.println("--- TmxRenderer.java : loadTilesetImages invoked ---");
        for (TilesetModel tileset : tilesets) {
            try {
                System.out.println("\nTmxRenderer.java loading tilesetimage : resources/"+  tileset.getImageSource());
                File tilesetFile = new File("resources/" + tileset.getImageSource());
                if (!tilesetFile.exists()) {
                    System.err.println("Tileset image file not found: " + tileset.getImageSource());
                    continue;
                }

                BufferedImage tilesetImage = ImageIO.read(tilesetFile);
                int tileWidth = (int) tileset.getWidth();
                int tileHeight = (int) tileset.getHeight();
                int columns = tileset.getColumns();

                System.out.println("TmxRenderer.java - loadTilesetImages:  calculating x and y positions of the tile within the tileset image grid: ");
                // Calculate x and y positions of the tile within the tileset image grid:
                for (int i = 0; i < tileset.getTileCount(); i++) {

                    // `i % columns` gives the tile’s column position in its row,
                    // and multiplying by `tileWidth` gives the exact x-coordinate in pixels.
                    int x = (i % columns) * tileWidth;


                    // `i / columns` gives the row number of tile `i`,
                    // and multiplying by `tileHeight` gives the exact y-coordinate in pixels.
                    int y = (i / columns) * tileHeight;


                    // Calculate GID
                    int gid = tileset.getFirstGid() + i; // Ensure this is consistent

                    System.out.println("TmxRenderer.java - loadTilesetImages: TileSet firstGid being loaded into tileImages: " + gid);
                    System.out.println("x: " + x + ", y: " + y);
                    if (x + tileWidth > tilesetImage.getWidth() || y + tileHeight > tilesetImage.getHeight()) {
                        System.err.println("Skipping tile: (" + x + ", " + y + ") is out of bounds for tileset image " + tileset.getImageSource());
                        continue;
                    }

                    // Add to tileImages map
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
        System.out.println("\n--- TmxRenderer.java initializeAnimations invoked ---");
        System.out.println("TmxRenderer.java - initalizeAnimations : looping through objects to initialize animations: ");
        for (ObjectModel object : objects) {
            System.out.println("\nTmxRenderer.java - initalizeAnimations : Object name: " + object.getName());
            int gid = object.getGid();

            if (tileImages.containsKey(gid)) {
                System.out.println("TmxRenderer.java - initalizeAnimations: Making an animation model for object " + object.getName());
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
                        int tileId = firstGid + (i % tileCount); // Calculate the correct tile ID
                        FrameModel frame = new FrameModel(tileId, FRAME_DURATION_MS);
                        // Add the frame to the animation, maintaining a sequence within tile bounds.
                        animation.addFrame(frame);
                    }
                    if (encounteredObjects.contains(animation.getName().toLowerCase())) {
                        animation.setDefeated(true);
                    }

                    animations.add(animation);
                    System.out.println("TmxRenderer.java - initalizeAnimations: Initialized animation for GID " + gid + " with frames at position (" +
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
            int firstGid = tileset.getFirstGid();
            int lastGid = firstGid + tileset.getTileCount() - 1;

            if (gid >= firstGid && gid <= lastGid) {
                return tileset;
            }
        }

        return null;
    }

    public void markObjectAsEncountered(String objectName) {
        encounteredObjects.add(objectName.toLowerCase());

        // Update animations and mark as defeated
        for (AnimationModel animation : animations) {
            if (animation.getName().equalsIgnoreCase(objectName)) {
                animation.setDefeated(true);
                break;
            }
        }

        // Remove object from objects list
        Iterator<ObjectModel> iterator = objects.iterator();
        while (iterator.hasNext()) {
            ObjectModel object = iterator.next();
            if (object.getName().equalsIgnoreCase(objectName)) {
                object.setDefeated(true);
                iterator.remove();
                break;
            }
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
            if (animation.isDefeated()|| encounteredObjects.contains(animation.getName())){ continue;} // Skip defeated animations

            animation.update();  // Update animation to get the current frame
            int tileId = animation.getCurrentTileId();
            BufferedImage frame = animationFrameCache.get(tileId);

            if (frame == null) {

                frame = tileImages.get(tileId);
                if (frame != null) {
                    animationFrameCache.put(tileId, frame);
                }
            }
         // for me to think about later - cali
            if ((frame != null) && animation.getName().matches("(?i)apple.*")) {
                g2d.drawImage(frame, (int) animation.getX()+10, (int) animation.getY() - 15, TILE_RENDER_SIZE / 4, TILE_RENDER_SIZE / 4, null);
            }
            if ((frame != null) && animation.getName().matches("(?i)study_stud.*")) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() - 15, TILE_RENDER_SIZE / 4, TILE_RENDER_SIZE / 4, null);
            }
            if ((frame != null) && animation.getName().matches("brown_shop") || animation.getName().matches("pink_shop"))
            {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() -30, TILE_RENDER_SIZE, TILE_RENDER_SIZE , null);
            }
            if ((frame != null) && animation.getName().matches("fountain")) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() -95, 96, 96 , null);
            }
            if ((frame != null) && animation.getName().matches("bus")) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() - 120, TILE_RENDER_SIZE * 2, TILE_RENDER_SIZE * 2, null);
            }


            if((frame != null) && animation.getName().matches("(?i)slime.*") ) {
                g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() - 30, TILE_RENDER_SIZE/2, TILE_RENDER_SIZE/2 , null);

            }

            if ((frame != null) && animation.getName().matches("(?i)help_npc(_\\d+)?") ) {

              //  System.out.println("TMX RENDERER CHECK IF HELPED -------------------" + checkIfHelped(animation.getName()));
                if(checkIfHelped(animation.getName()) == false) {
                    g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() - 30, TILE_RENDER_SIZE/2, TILE_RENDER_SIZE/2, null);
                }
                else if(checkIfHelped(animation.getName())== true){
                    System.out.println(checkIfHelped(animation.getName()));
                    animation.setY(53);
                    animation.setX(88);
                    setObjectXandY(animation.getName(), 53, 88);
                    g2d.drawImage(frame, (int) animation.getX(), (int) (int) animation.getY(), TILE_RENDER_SIZE/2, TILE_RENDER_SIZE/2, null);
                }
            }

            if (frame != null && !animation.getName().matches("(?i)apple.*") &&  !animation.getName().matches("bus") && !animation.getName().matches("brown_shop") && !animation.getName().matches("pink_shop") && !animation.getName().matches("(?i)gnome_.*") && !animation.getName().matches("(?i)help_npc(_\\d+)?") && !animation.getName().matches("fountain") && !animation.getName().matches("(?i)study_stud.*") && !animation.getName().matches("(?i)slime.*")) {
                String animationName = animation.getName().toLowerCase();
                    g2d.drawImage(frame, (int) animation.getX(), (int) animation.getY() - 30,
                            TILE_RENDER_SIZE/2, TILE_RENDER_SIZE/2, null);
                }



        }
    }

    public boolean checkIfHelped(String animationName) {
    //    System.out.println("checkIfHelped called with animationName: " + animationName);
        for (ObjectModel object : objects) {
        //    System.out.println("checkIfHelped: checking object name: " + object.getName());
            if (object.getName().equalsIgnoreCase(animationName)) {
          //      System.out.println("CHECK IF HELPED: " + object.isHelped());
        //        System.out.println("Object instance in TmxRenderer: " + System.identityHashCode(object));
                return object.isHelped();
            }
        }
      //  System.out.println("Object with name " + animationName + " not found.");
        return false;
    }

    private void setObjectXandY(String animationName, int x, int y){
        for (ObjectModel object : objects) {
            if(object.getName().equalsIgnoreCase(animationName)){
                object.setX(x);
                object.setY(y);
            }
            System.out.println(" Changed x and y of Object name: " + object.getName());

        }
    }



    /**
     * Renders a specific enemy animation at given screen coordinates.
     *
     * @param objectName The name of the enemy animation to render.
     * @param x         The x-coordinate for rendering.
     * @param y         The y-coordinate for rendering.
     * @param g         The graphics context.
     */
    public void renderIndividualAnimation(String objectName, int x, int y, Graphics g) {
        AnimationModel enemyAnimation = null;
        for (AnimationModel animation : animations) {

            if (animation.getName().equalsIgnoreCase(objectName)) {
                enemyAnimation = animation;
                break;
            }
        }

        if (enemyAnimation == null) {
            System.out.println("Enemy animation not found for: " + objectName);
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
        if (frame != null && objectName.equalsIgnoreCase("shop")) {
            g.drawImage(frame, x/2 + 25, y/2, ENEMY_RENDER_SIZE*2, ENEMY_RENDER_SIZE*2, null);
        }
        if (frame != null && objectName.equalsIgnoreCase("B_witch_attack")) {
            g.drawImage(frame, x-80, y, 316, 124, null);
        }



        // Draw the frame if it exists
        else if (frame != null) {
            g.drawImage(frame, x, y, ENEMY_RENDER_SIZE, ENEMY_RENDER_SIZE, null);
        }

    }
    // Method to trigger repaint
    public void repaintMap() {
        if (displayComponent != null) {
            displayComponent.repaint(); // Forces a full repaint
        }
    }

    public void printObjectNames() {
        System.out.println("Objects in TmxRenderer:");
        for (ObjectModel object : objects) {
            System.out.println("Object name: " + object.getName());
        }
    }



    }

