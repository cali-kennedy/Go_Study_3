import models.ObjectModel;
import models.ObjectPropertiesModel;

import java.awt.Rectangle;
import java.util.List;

/**
 * CollisionDetector class handles collision detection between a character and various game objects.
 * It identifies different object types and applies appropriate logic for each type.
 */
public class CollisionDetector {

    private static final int HEALTH_REWARD = 10;

    // Character and objects for collision detection
    private final Character character;
    private final List<ObjectModel> objects;

    // Collision state management
    private boolean isAnsweringQuestion = false;
    private ObjectModel lastCollidedEnemy = null;
    private String enemyName;

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
        boolean collidedWithWall = false;
        boolean enemyCollision = false;

        if (!isAnsweringQuestion) {
            for (ObjectModel object : objects) {
                if (isColliding(character, object)) {
                    String objectType = getObjectType(object);
                    switch (objectType) {
                        case "wall" -> collidedWithWall = true;
                        case "enemy" -> {
                            if (isNewEnemyCollision(object)) {
                                enemyCollision = true;
                                isAnsweringQuestion = true;
                                lastCollidedEnemy = object;
                            }
                        }
                        case "apple" -> character.addHealth(HEALTH_REWARD);
                    }
                }
            }
        }
        return new CollisionResult(collidedWithWall, enemyCollision);
    }

    /**
     * Resets the collision state to allow further interactions with objects.
     */
    public void resetCollisionState() {
        isAnsweringQuestion = false;
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
                setEnemyName(object.getName());
                return "enemy";
            } else if (property.getPropertyName().equalsIgnoreCase("type")) {
                return property.getValue();
            }
        }
        return object.getName().equalsIgnoreCase("wall") ? "wall" : "unknown";
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
                    if (isNewEnemyCollision(object)) {
                        lastCollidedEnemy = object;
                        isAnsweringQuestion = true;
                        return object;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the current collision is with a new enemy, distinct from the last encountered enemy.
     *
     * @param object The object to check against the last collided enemy.
     * @return true if the object is a new enemy, otherwise false.
     */
    private boolean isNewEnemyCollision(ObjectModel object) {
        return lastCollidedEnemy != object;
    }
}
