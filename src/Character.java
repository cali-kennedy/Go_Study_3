import models.ObjectModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Character {
    private int x;
    private int y;
    private BufferedImage sprite;
    private int width;
    private int height;
    private int XP;
    private static final int MAX_HEALTH = 100;
    private int level;
    private int levelProgression;
    private int levelCap;
    private int health;
    private int study_stud_count;
    private boolean defending = false;
    private Graphics g;
    private List<ObjectModel> objects;
    private List<Item> inventory;
    private TmxRenderer tmxRenderer;
    private String spritePath;
    private boolean isMoving = false;  // Tracks if the character is moving
    private Timer idleCheckTimer;     // Timer for checking idle state

    // Constructor
    public Character(String spritePath, int startX, int startY, int width, int height, TmxRenderer tmxRenderer) {
        this.x = startX; // Sprite starting position
        this.y = startY;
        this.width = width;
        this.height = height;
        this.XP = 0; // Initial XP
        this.level = 1; // Starting level
        this.levelProgression = 0; // Starting level progression
        this.levelCap = 500; // XP needed for the first level up
        this.health = MAX_HEALTH; // Start with full health
        this.study_stud_count = 0;
        this.inventory = new ArrayList<>();
        this.tmxRenderer= tmxRenderer;
        this.spritePath = spritePath;
        // Loads character model
        try {
            this.sprite = ImageIO.read(new File("resources/B_witch.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Initialize timer to check if the character is idle
        idleCheckTimer = new Timer(500, e -> {
            // Set isMoving to false if no movement detected for a while
            isMoving = false;
        });
        idleCheckTimer.setRepeats(false); // Only run once per movement event

    }
    // Draws character model
    public void draw(Graphics g) {
        if (isMoving) {
            tmxRenderer.renderIndividualAnimation(spritePath +"_run",x,y, g);

        }else{
            g.drawImage(sprite, x, y, width, height, null);
        }
    }

    // Movement Method
    public void move(int dx, int dy) {
        if (dx != 0 || dy != 0) {
            this.isMoving = true;
            // Restart the idle timer whenever there's movement
            idleCheckTimer.restart();
        }

        this.x += dx;
        this.y += dy;
    }

    // Health Management
    public void addHealth(int healthToAdd) {
        this.health = Math.min(this.health + healthToAdd, MAX_HEALTH); // Ensure health doesn't exceed max
        System.out.println("You gained " + healthToAdd + " health");
    }
    // Removes player health
    public void removeHealth(int healthToRemove) {
        this.health = Math.max(this.health - healthToRemove, 0); // Ensure health doesn't go below zero
    }
    // Sets player health
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, MAX_HEALTH)); // Health within range
    }
    // Returns health of player
    public int getHealth() {
        return this.health;
    }

    // XP Management
    public void addXP(int XPToAdd) {
        // Add to both total XP and level progression
        this.XP += XPToAdd;
        this.levelProgression += XPToAdd;

        // Check if level up is needed
        while (this.levelProgression >= this.levelCap) {
            // Level up
            this.level++;

            // Calculate remaining XP after level up
            this.levelProgression = this.levelProgression - this.levelCap;

            // Increase XP requirement for next level
            this.levelCap += 50;

            // Notify of level up
            System.out.println("Level up! Now level: " + this.level);
            System.out.println("XP needed for next level: " + this.levelCap);

            // Restore full health on level up (optional - remove if you don't want this)
            this.health = 100;
        }
    }
    // Returns player Xp
    public int getXP() {
        return this.XP;
    }

    public int LevelUp(int XPToAdd) {
        addXP(XPToAdd);
        return this.levelProgression;
    }
    // Returns player level
    public int getLevel() {
        return this.level;
    }
    // Returns xp progression
    public int getLevelProgression() {
        return this.levelProgression;
    }
    // Returns level cap
    public int getLevelCap() {
        return this.levelCap;
    }

    // Position Getters and Setters
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }


    public int getStudyStudCount() {
        return study_stud_count;
    }

    public void addStudyStud(int study_stud_count) {
        this.study_stud_count = this.study_stud_count + study_stud_count;
    }

    public void removeStudyStud(int study_stud_count) {
        this.study_stud_count = this.study_stud_count - study_stud_count;
    }

    // Inventory Methods
    public void addItem(Item item) {
        // Check if item is stackable and already exists in inventory
        if (item.isStackable()) {
            for (Item invItem : inventory) {
                if (invItem.getName().equals(item.getName())) {
                    invItem.setQuantity(invItem.getQuantity() + item.getQuantity());
                    return;
                }
            }
        }
        // If item is not stackable or doesn't exist in inventory
        inventory.add(item);
    }
    // Removes item from inventory
    public void removeItem(Item item) {
        inventory.remove(item);
    }
    // Uses an item
    public void useItem(Item item) {
        switch (item.getName()) {
            case "Apple":
                addHealth(10);
                System.out.println("You gained 10 health!");
                break;
            case "Study Stud":
                // Define what happens when using a Study Stud, or maybe it's not usable directly
                System.out.println("You can't use Study Stud directly.");
                return; // Early exit if the item is not consumed
            // Add more cases for other items
            default:
                System.out.println("Item has no use effect.");
                return; // Early exit if the item is not consumed
        }

        // Decrease quantity and remove if necessary
        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() <= 0) {
            removeItem(item);
        }
    }
    // Displays player inventory on screen
    public void displayInventory() {
        SwingUtilities.invokeLater(() -> {
            InventoryScreen inventoryScreen = new InventoryScreen( this);
            inventoryScreen.setVisible(true);
        });
    }
    // Returns player inventory
    public List<Item> getInventory() {
        return inventory;
    }

    // Getter and Setter for defending
    public boolean isDefending() {
        return defending;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }
}

