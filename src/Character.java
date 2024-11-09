import models.ObjectModel;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
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
    private List<ObjectModel> objects;

    // Constructor
    public Character(String spritePath, int startX, int startY, int width, int height) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.XP = 0; // Initial XP
        this.level = 1; // Starting level
        this.levelProgression = 0; // Starting level progression
        this.levelCap = 100; // XP needed for the first level up
        this.health = MAX_HEALTH; // Start with full health


        try {
            this.sprite = ImageIO.read(new File("resources/rabbit.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    // Movement Method
    public void move(int dx, int dy){ // TmxParser parser, JFrame parentFrame, Question question, List<Question> questions) {
        // Update position based on movement delta
        this.x += dx;
        this.y += dy;
        CollisionDetector collisionDetector = new CollisionDetector(this,objects);

        // input collision detection here
    }

    // Health Management
    public void addHealth(int healthToAdd) {
        this.health = Math.min(this.health + healthToAdd, MAX_HEALTH); // Ensure health doesn't exceed max
    }

    public void removeHealth(int healthToRemove) {
        this.health = Math.max(this.health - healthToRemove, 0); // Ensure health doesn't go below zero
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, MAX_HEALTH)); // Health within range
    }

    public int getHealth() {
        return this.health;
    }

    // XP Management
    public void addXP(int XPToAdd) {
        this.XP += XPToAdd;
        this.levelProgression += XPToAdd;

        // Check if level up is needed
        if (this.levelProgression >= this.levelCap) {
            this.level++;
            this.levelProgression = 0; // Reset progression for the new level
            this.levelCap += 50; // Increase XP cap for next level (could be dynamic)
            System.out.println("Level up! Now level: " + this.level);
        }
    }

    public int getXP() {
        return this.XP;
    }

    public int LevelUp(int XPToAdd) {
        addXP(XPToAdd); // Reuse addXP method
        return this.levelProgression; // Return progression for display
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


}

