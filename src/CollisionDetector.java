import java.awt.Rectangle;
import java.util.List;

public class CollisionDetector {
    private Character character;
    private List<ObjectModel> objects;

    public CollisionDetector(Character character, List<ObjectModel> objects) {
        this.character = character;
        this.objects = objects;
    }

    // Detect collision with objects based on type and handle accordingly
    public boolean checkCollisions() {
        boolean collidedWithWall = false;

        for (ObjectModel object : objects) {
            // Check if the character is colliding with this object
            if (isColliding(character, object)) {
                // Get object type from its properties
                String objectType = getObjectType(object);
                System.out.println("-----" + objectType);
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
    private boolean isColliding(Character character, ObjectModel object) {
        Rectangle charBounds = new Rectangle(character.getX(), character.getY(), character.getWidth(), character.getHeight());
        Rectangle objectBounds = new Rectangle((int) object.getX(), (int) object.getY(), (int) object.getWidth(), (int) object.getHeight());
        return charBounds.intersects(objectBounds);
    }

    // Retrieve the type of the object based on its properties
    private String getObjectType(ObjectModel object) {
        System.out.println(object.getName());
        for (ObjectPropertiesModel property : object.getProperties()) {
            if (property.getPropertyName().equalsIgnoreCase("is_enemy") &&
                    property.getValue().equalsIgnoreCase("true")) {
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


    // Initiates combat or interaction when encountering an enemy
    private void initiateCombat(Character character, ObjectModel enemy) {
        // Placeholder for combat logic or opening a combat screen
        System.out.println("Initiating combat with enemy at (" + enemy.getX() + ", " + enemy.getY() + ")");
        // Actual combat system or interaction logic would go here
    }
}
