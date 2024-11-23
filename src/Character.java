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
    private int study_stud_count;
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
        this.levelCap = 500; // XP needed for the first level up
        this.health = MAX_HEALTH; // Start with full health
        this.study_stud_count = 0;

        try {
            this.sprite = ImageIO.read(new File("resources/rabbit2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    // Movement Method
    public void move(int dx, int dy){ // TmxParser parser, JFrame parentFrame, Question question, List<Question> questions) {
             this.x += dx;
             this.y += dy;
       //  }

    }

    // Health Management
    public void addHealth(int healthToAdd) {
        this.health = Math.min(this.health + healthToAdd, MAX_HEALTH); // Ensure health doesn't exceed max
        System.out.println("You gained " + healthToAdd + " health");
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

    public int getXP() {
        return this.XP;
    }

    public int LevelUp(int XPToAdd) {
        addXP(XPToAdd);
        return this.levelProgression;
    }

    public int getLevel() {
        return this.level;
    }

    public int getLevelProgression() {
        return this.levelProgression;
    }

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
}

