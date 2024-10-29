import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Character {
    private int x, y;
    private BufferedImage sprite;
    private int width, height;
    private int health;
    private int XP;
    private static final int MAX_HEALTH = 100;

    public Character(String spritePath, int startX, int startY, int width, int height) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.health = MAX_HEALTH;

        try {
            sprite = ImageIO.read(new File(spritePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    public void move(int dx, int dy, TmxParser parser, JFrame parentFrame, List<Question> questions) {
        int oldX = x;
        int oldY = y;

        x += dx;
        y += dy;

        boolean collidedWithWall = CollisionDetector.checkCollisions(this, parser, parentFrame, questions);
        if (collidedWithWall) {
            x = oldX;
            y = oldY;
        }
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }

    public void addHealth(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
        System.out.println("Health: " + health);
    }

    public void addXP(int xp) {
        XP += xp;
        System.out.println("XP: " + XP);
    }
}
