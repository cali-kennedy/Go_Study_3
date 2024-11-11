import models.ObjectModel;
import models.ObjectPropertiesModel;
import java.awt.Rectangle;
import java.util.Iterator;
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

    // Collision state management
    private boolean isAnsweringQuestion = false;
    private ObjectModel lastCollidedEnemy = null;
    private String enemyName;

    private int old_x;
    private int old_y;

    public boolean isWallFlag() {
        return wallFlag;
    }

    public void setWallFlag(boolean wallFlag) {
        this.wallFlag = wallFlag;
    }

    /**
     * Inner class to store collision results for walls and enemies.
     */
    public static class CollisionResult {
        private final boolean wallCollision;
        private final boolean enemyCollision;

        public CollisionResult(boolean wallCollision, boolean enemyCollision) {
            this.wallCollision = wallCollision;
            this.enemyCollision = enemyCollision;
        }

        public boolean hasWallCollision() {
            return wallCollision;
        }


        public boolean hasEnemyCollision() {
            return enemyCollision;
        }
    }

    /**
     * Constructor initializes the CollisionDetector with a character and list of objects.
     *
     * @param character Character involved in the collision detection.
     * @param objects   List of objects that the character may collide with.
     */
    public CollisionDetector(Character character, List<ObjectModel> objects) {
        this.character = character;
        this.objects = objects;
    }

    /**
     * Checks collisions with all objects and applies specific behavior for each object type.
     *
     * @return CollisionResult indicating if the character has collided with a wall or enemy.
     */
    public CollisionResult checkCollisions() {
        collidedWithWall = false;
        boolean enemyCollision = false;

        // Iterate through the objects and check for collisions
        Iterator<ObjectModel> iterator = objects.iterator();
        while (iterator.hasNext()) {
            ObjectModel object = iterator.next();
            if (isColliding(character, object)) {
                String objectType = getObjectType(object);
                switch (objectType) {
                    case "wall" -> {
                        setWallFlag(true);
                        collidedWithWall = true;
                    }
                    case "enemy" -> {
                        // Register collision with a new enemy only if not answering a question
                        if (!isAnsweringQuestion || !object.equals(lastCollidedEnemy)) {
                            enemyCollision = true;
                            isAnsweringQuestion = true;
                            lastCollidedEnemy = object; // Set the current enemy for fight
                            setEnemyName(object.getName()); // Update the enemy
                            System.out.println("Collision Detecor enemy name: " + enemyName);
                            object.isDefeated();
                        }
                    }
                    case "apple" -> character.addHealth(HEALTH_REWARD);
                }
                System.out.println("Collided w wall: " + collidedWithWall);
            }
        }
        return new CollisionResult(collidedWithWall, enemyCollision);
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

    /**
     * Checks if there is a collision between the character and a specific object.
     *
     * @param character The character in the game.
     * @param object    The object to check collision with.
     * @return true if there is a collision, otherwise false.
     */
    private boolean isColliding(Character character, ObjectModel object) {
        Rectangle charBounds = new Rectangle(
                character.getX(),
                character.getY(),
                character.getWidth(),
                character.getHeight()
        );
        Rectangle objectBounds = new Rectangle(
                (int) object.getX(),
                (int) object.getY(),
                (int) object.getWidth(),
                (int) object.getHeight()
        );
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
            if (property.getPropertyName().equalsIgnoreCase("is_enemy") &&
                    property.getValue().equalsIgnoreCase("true")) {
                return "enemy";
            } else if (property.getPropertyName().equalsIgnoreCase("type")) {
                return property.getValue();
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
}
