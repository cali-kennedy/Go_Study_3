import java.awt.Rectangle;
import java.util.List;

public class CollisionDetector {

    // Detect collision with objects based on type and handle accordingly
    public static boolean checkCollisions(Character character, List<ObjectModel> objects) {
        boolean collidedWithWall = false;

        for (ObjectModel object : objects) {
            // Check if the character is colliding with this object
            if (isColliding(character, object)) {
                // Get object type from its properties
                String objectType = getObjectType(object);

                switch (objectType) {
                    case "wall" -> {
                        collidedWithWall = true;
                        System.out.println("Collision with wall at (" + object.getX() + ", " + object.getY() + ")");
                    }
                    case "apple" -> {
                        character.addHealth(10);
                        System.out.println("Collected an apple! Health: " + character.getHealth());
                        // Optionally remove the object from the map if it's a one-time collectible
                    }
                    case "enemy" -> {
                        System.out.println("Encountered an enemy!");
                        initiateCombat(character, object);
                    }
                    default -> System.out.println("Unknown object type encountered.");
                }
            }
        }
        return collidedWithWall;  // Return if a wall collision occurred, blocking movement
    }

    // Helper to determine if the character's bounds intersect an object's bounds
    private static boolean isColliding(Character character, ObjectModel object) {
        Rectangle charBounds = new Rectangle(character.getX(), character.getY(), character.getWidth(), character.getHeight());
        Rectangle objectBounds = new Rectangle((int) object.getX(), (int) object.getY(), (int) object.getWidth(), (int) object.getHeight());
        return charBounds.intersects(objectBounds);
    }

    // Retrieve the type of the object based on its properties
    private static String getObjectType(ObjectModel object) {
        for (String property : object.getProperties()) {
            if (property.equalsIgnoreCase("type")) {
                return property;  // Extract the 'type' property (e.g., "wall", "apple", "enemy")
            }
        }
        return "unknown";  // Default if no type is found
    }

    // Initiates combat or interaction when encountering an enemy
    private static void initiateCombat(Character character, ObjectModel enemy) {
        // Placeholder for combat logic or opening a combat screen
        System.out.println("Initiating combat with enemy at (" + enemy.getX() + ", " + enemy.getY() + ")");
        // Actual combat system or interaction logic would go here
    }
}
