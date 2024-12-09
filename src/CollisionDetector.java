import models.ObjectModel;
import models.ObjectPropertiesModel;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CollisionDetector class handles collision detection between a character and various game objects.
 * It identifies different object types and applies appropriate logic for each type.
 */
public class CollisionDetector {

    private static final int HEALTH_REWARD = 10;
    private boolean collidedWithWall;
    private boolean wallFlag;
    // Character and objects for collision detection
    private final Character character;
    private final List<ObjectModel> objects;
    private TmxRenderer tmxRenderer;
    private Main main; // Reference to Main class to access mapStates

    // Collision state management
    private boolean isAnsweringQuestion = false;
    private ObjectModel lastCollidedEnemy = null;
    private String enemyName;
    private String shopName;
    private int old_x;
    private int old_y;
    private String npcName;
    private Rectangle2D objectBounds;

    public boolean isWallFlag() {
        return wallFlag;
    }

    public void setWallFlag(boolean wallFlag) {
        this.wallFlag = wallFlag;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    public String getShopName(){
        return this.shopName;
    }

    /**
     * Inner class to store collision results for walls and enemies.
     */
    public static class CollisionResult {
        private final boolean wallCollision;
        private final boolean enemyCollision;
        private boolean npcCollision;
        private boolean shopCollision;
        private boolean transitionCollision;
        private ObjectModel transitionObject;



        public CollisionResult(boolean wallCollision, boolean enemyCollision, boolean npcCollision, boolean shopCollision,
                               boolean transitionCollision, ObjectModel transitionObject) {
            this.wallCollision = wallCollision;
            this.enemyCollision = enemyCollision;
            this.npcCollision = npcCollision;
            this.shopCollision = shopCollision;
            this.transitionCollision = transitionCollision;
            this.transitionObject = transitionObject;
        }

        public boolean hasWallCollision() {
            return wallCollision;
        }

        public boolean hasEnemyCollision() {
            return enemyCollision;
        }
        public boolean hasNPCCollision() {
            return npcCollision;
        }

        public boolean hasShopCollision() {
            return shopCollision;
        }

        public boolean hasTransitionCollision() { return transitionCollision; }

        public ObjectModel getTransitionObject() { return transitionObject; }
    }

    /**
     * Constructor initializes the CollisionDetector with a character and list of objects.
     *
     * @param character Character involved in the collision detection.
     * @param objects   List of objects that the character may collide with.
     */
    public CollisionDetector(Character character, List<ObjectModel> objects, TmxRenderer tmxRenderer, Main main) {
        System.out.println("----COLLISION DETECTOR INVOKED-----");
        System.out.println("Character: " + character);
        this.character = character;
        this.objects = objects;
        this.tmxRenderer = tmxRenderer;
        this.main = main;
    }

    /**
     * Checks collisions with all objects and applies specific behavior for each object type.
     *
     * @return CollisionResult indicating if the character has collided with a wall or enemy.
     */
    public CollisionResult checkCollisions() {
        collidedWithWall = false;
        boolean enemyCollision = false;
        boolean npcCollision = false;
        boolean shopCollision = false;
        boolean transitionCollision = false;
        ObjectModel transitionObject = null;

        // Lists to keep track of objects and enemies to process after iteration
        List<ObjectModel> objectsToRemove = new ArrayList<>();
        List<String> enemiesToDefeat = new ArrayList<>();
        List<String> itemsToCollect = new ArrayList<>();

        // Iterate through a copy of the objects list to avoid concurrent modification
        for (ObjectModel object : new ArrayList<>(objects)) {
            if (isColliding(character, object)) {
                if (main.getCurrentMapState().getEncounteredObjects().contains(object.getName().toLowerCase())) {
                    continue;
                }
                String objectType = getObjectType(object);
                switch (objectType) {
                    case "wall" -> {
                        setWallFlag(true);
                        collidedWithWall = true;
                    }
                    case "enemy" -> {
                        if (!isAnsweringQuestion || !object.equals(lastCollidedEnemy)) {
                            enemyCollision = true;
                            isAnsweringQuestion = true;
                            lastCollidedEnemy = object;
                            setEnemyName(object.getName());
                            // Add to list to process after iteration
                            enemiesToDefeat.add(object.getName());
                        }
                    }
                    case "apple" -> {
                        // Add to character's health after iteration
                        itemsToCollect.add(object.getName());
                        objectsToRemove.add(object);
                    }
                    case "npc" -> {
                        setNPCName(object.getName());
                        System.out.println("npc Name: " + object.getName());
                        npcCollision = true;
                    }
                    case "needsHelpNpc" -> {
                        setNPCName(object.getName());
                        //object.setIsHelped(false);
                        System.out.println("npc Name: " + object.getName());
                        npcCollision = true;
                    }
                    case "study_stud" -> {
                        itemsToCollect.add(object.getName());
                        objectsToRemove.add(object);
                    }
                    case "shop" -> {
                        setShopName(object.getName());
                        shopCollision = true;
                    }
                    case "transition" -> {
                        System.out.println("----------------------------- COLLISION DETECTOR FOUND A TRANSITION ------------------------");
                        transitionCollision = true;
                        transitionObject = object;
                    }
                    case "lowXPChest" ->{
                        setNPCName(object.getName());
                        npcCollision = true;
                        objectsToRemove.add(object);
                    }
                }
            }
        }

        // Process items to collect
        for (String itemName : itemsToCollect) {
            if (main.getCurrentMapState().getEncounteredObjects().contains(itemName.toLowerCase())) {
                continue;
            }
            // Find the object by name

            ObjectModel itemObject = findObjectByName(itemName);
            if (itemObject != null) {
                String objectType = getObjectType(itemObject);
                switch (objectType) {
                    case "apple" -> {
                        Item apple = new Item("Apple", "Restores health when used", 1, true);
                        character.addItem(apple);
                        System.out.println("Collected an apple.");
                    }
                    case "study_stud" -> {
                        playSound("resources/collect_study_stud.wav");
                        Item studyStud = new Item("Study Stud", "A valuable item used for trading", 1, true);
                        character.addItem(studyStud);
                        System.out.println("Collected a Study Stud.");
                    }
                }
                // Add to MapState
                MapState currentMapState = main.getCurrentMapState();
                if (currentMapState != null) {
                    currentMapState.addEncounteredObject(itemName);
                }
                tmxRenderer.markObjectAsEncountered(itemName);
            }
        }

        // Process enemies to defeat
        for (String enemyName : enemiesToDefeat) {
            tmxRenderer.markObjectAsEncountered(enemyName);
            // Add to MapState
            MapState currentMapState = main.getCurrentMapState();
            if (currentMapState != null) {
                currentMapState.addEncounteredObject(enemyName);
            }
        }

        // Process objects to remove
        for (ObjectModel obj : objectsToRemove) {
            String objName = obj.getName().toLowerCase();
            if (main.getCurrentMapState().getEncounteredObjects().contains(objName)) {
                continue;
            }
            // Add to MapState
            MapState currentMapState = main.getCurrentMapState();
            if (currentMapState != null) {
                currentMapState.addEncounteredObject(objName);
                System.out.println("Added to encounteredObjects: " + objName);
            }
            tmxRenderer.markObjectAsEncountered(objName);
            objects.remove(obj);
        }
        objects.removeAll(objectsToRemove);

        return new CollisionResult(collidedWithWall, enemyCollision, npcCollision, shopCollision, transitionCollision, transitionObject);
    }

    // Helper method to find an object by name
    private ObjectModel findObjectByName(String name) {
        for (ObjectModel object : objects) {
            if (object.getName().equalsIgnoreCase(name)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Resets the collision state to allow further interactions with objects.
     * Removes the last collided enemy from the map after the fight is over.
     */
    public void resetCollisionState() {
        isAnsweringQuestion = false;

        // Remove lastCollidedEnemy from the objects list if it exists and was an enemy
        if (lastCollidedEnemy != null && "enemy".equals(getObjectType(lastCollidedEnemy))) {
            objects.remove(lastCollidedEnemy);
            lastCollidedEnemy = null; // Reset after removal
        }
        enemyName = null; // Clear enemy name to register new collisions
    }

    public void markNpcAsHelped(String npcName) {
        for (ObjectModel object : objects) {
            if (object.getName().equalsIgnoreCase(npcName)) {
                object.setIsHelped(true); // NPC has been helped
                System.out.println("NPC " + npcName + " isHelped set to: " + object.isHelped());
                System.out.println("Object instance in CollisionDetector: " + System.identityHashCode(object));
                break;
            }
        }
    }

    public void updateObjects(List<ObjectModel> newObjects) {
        this.objects.clear();
        this.objects.addAll(newObjects);
    }

    /**
     * Checks if there is a collision between the character and a specific object.
     *
     * @param character The character in the game.
     * @param object    The object to check collision with.
     * @return true if there is a collision, otherwise false.
     */
    private boolean isColliding(Character character, ObjectModel object) {
        // System.out.println("Colliding w: " + object.getName());
      double object_width = object.getWidth()/4;
      double object_height = object.getHeight()/4;

        Rectangle charBounds = new Rectangle(
                character.getX(),
                character.getY(),
                character.getWidth(),
                character.getHeight()
        );
        if(object.getName().toLowerCase().startsWith("wall")){
        objectBounds = null;
            objectBounds = new Rectangle2D.Double(
                    object.getX(),
                    object.getY(),
                    object.getWidth(),
                    object.getHeight()
            );} else {
            objectBounds = null;
            objectBounds = new Rectangle2D.Double(
                    object.getX(),
                    object.getY(),
                    object_width,
                    object_height);
        }


        return charBounds.intersects(objectBounds);
    }



    /**
     * Retrieves the type of the object by checking its properties.
     *
     * @param object The object whose type is to be determined.
     * @return The type of the object as a string ("wall", "enemy", or "unknown").
     */
    private String getObjectType(ObjectModel object) {
        for (ObjectPropertiesModel property : object.getProperties()) {
             if (property.getPropertyName().equalsIgnoreCase("type")) {
                return property.getValue();
     //       } else if (object.getName().startsWith("transition_") || object.getName().startsWith("transition")) {
     //            return "transition";
            }
        }
        return object.getName().equalsIgnoreCase("wall") ? "wall" : "unknown";
    }


    public boolean CollidedWithWall(){
        return this.collidedWithWall;
    }

    /**
     * Sets the name of the enemy involved in a collision.
     *
     * @param enemyName The name of the enemy.
     */
    public void setEnemyName(String enemyName) {
        this.enemyName = enemyName;
    }

    /**
     * Retrieves the name of the enemy involved in a collision.
     *
     * @return The name of the enemy.
     */
    public String getEnemyName() {
        return enemyName;
    }

    public void setNPCName(String npcName){
        this.npcName = npcName;
    }

    public String getNPCName(){
        return npcName;
    }

    /**
     * Checks if the character collides with a new enemy and returns the enemy object if true.
     *
     * @return The enemy models.ObjectModel if there is a new collision, otherwise null.
     */
    public ObjectModel checkEnemyCollision() {
        if (!isAnsweringQuestion) {
            for (ObjectModel object : objects) {
                if (isColliding(character, object) && "enemy".equals(getObjectType(object))) {
                    lastCollidedEnemy = object;
                    isAnsweringQuestion = true;
                    object.isDefeated();
                    return object;
                }
            }
        }
        return null;
    }

    private void playSound(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
