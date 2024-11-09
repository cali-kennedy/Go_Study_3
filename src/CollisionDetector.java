import java.awt.Rectangle;
import java.util.List;

public class CollisionDetector {
    private Character character;
    private List<ObjectModel> objects;
    private boolean isAnsweringQuestion = false;
    private ObjectModel lastCollidedEnemy = null;
    private String enemyName;

    public static class CollisionResult {
        private boolean wallCollision;
        private boolean enemyCollision;

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

    public CollisionDetector(Character character, List<ObjectModel> objects) {
        this.character = character;
        this.objects = objects;
    }

    // Detect collision with objects based on type and handle accordingly
    public CollisionResult checkCollisions() {
        boolean collidedWithWall = false;
        boolean enemyCollision = false;

        if (!isAnsweringQuestion) {
            for (ObjectModel object : objects) {
                // Check if the character is colliding with this object
                if (isColliding(character, object)) {
                    // Get object type from its properties
                    String objectType = getObjectType(object);
                    switch (objectType) {
                        case "wall" -> collidedWithWall = true;
                        case "enemy" -> {
                            // Only trigger for a different enemy than the last one
                            if (lastCollidedEnemy != object) {
                                enemyCollision = true;
                                isAnsweringQuestion = true;
                                lastCollidedEnemy = object;
                            }
                        }
                        case "apple" -> character.addHealth(10);
                    }
                }
            }
        }

        return new CollisionResult(collidedWithWall, enemyCollision);
    }

    public void resetCollisionState() {
        isAnsweringQuestion = false;
    }

    // Helper to determine if the character's bounds intersect an object's bounds
    private boolean isColliding(Character character, ObjectModel object) {
        Rectangle charBounds = new Rectangle(
                character.getX(),
                character.getY(),
                character.getWidth(),
                character.getHeight()
        );
        Rectangle objectBounds = new Rectangle(
                (int)object.getX(),
                (int)object.getY(),
                (int)object.getWidth(),
                (int)object.getHeight()
        );
        return charBounds.intersects(objectBounds);
    }

    // Retrieve the type of the object based on its properties
    private String getObjectType(ObjectModel object) {
        for (ObjectPropertiesModel property : object.getProperties()) {
            if (property.getPropertyName().equalsIgnoreCase("is_enemy") &&
                    property.getValue().equalsIgnoreCase("true")) {
                   setEnemyName(object.getName());
                return "enemy"; // Treat as enemy if is_enemy is true
            } else if (property.getPropertyName().equalsIgnoreCase("type")) {
                return property.getValue(); // Return the value of 'type' if present
            }
        }
        if(object.getName().equalsIgnoreCase("wall")){
            return "wall";
        }
        return "unknown"; // Default if no relevant type is found
    }

    public void setEnemyName(String enemyName){
        this.enemyName = enemyName;
    }

    public String getEnemyName() {
        return enemyName;
    }

    public ObjectModel checkEnemyCollision() {
        if (!isAnsweringQuestion) {
            for (ObjectModel object : objects) {
                if (isColliding(character, object)) {
                    String objectType = getObjectType(object);
                    if ("enemy".equals(objectType)) {
                        if (lastCollidedEnemy != object) {
                            lastCollidedEnemy = object;
                            isAnsweringQuestion = true;
                            return object; // Return the specific enemy object
                        }
                    }
                }
            }
        }
        return null; // No collision with enemy
    }
}