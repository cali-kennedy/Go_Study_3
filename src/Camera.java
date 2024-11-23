import java.awt.*;

/**
 * The Camera class manages the viewport for displaying a specific area of the game map
 * based on the character's position and a configurable zoom level.
 */
public class Camera {
    private int x, y;             // Camera's top-left position on the map
    private int width, height;    // Dimensions of the viewport (typically screen dimensions)
    private float zoomLevel;      // Zoom level of the camera, default should be 1.0f for normal scale
    private Character character;  // The character object the camera should follow

    /**
     * Initializes the Camera with specified viewport dimensions, zoom level, and target character.
     *
     * @param width      Width of the viewport in pixels.
     * @param height     Height of the viewport in pixels.
     * @param zoomLevel  Initial zoom level, where 1.0f represents no zoom.
     * @param character  The character the camera should follow.
     */
    public Camera(int width, int height, float zoomLevel, Character character) {
        this.width = width;
        this.height = height;
        this.zoomLevel = zoomLevel;
        this.character = character;

    }

    /**
     * Updates the camera's position based on the character's location.
     * The camera is centered on the character while remaining within the map boundaries.
     *
     * @param mapWidth   Width of the entire map in pixels.
     * @param mapHeight  Height of the entire map in pixels.
     */
    public void update(int mapWidth, int mapHeight) {
        // Center the camera on the character's current position
        x = (int) (character.getX() - width / (2 * zoomLevel));
        y = (int) (character.getY() - height / (2 * zoomLevel));

        // Clamp the camera position to ensure it stays within the map bounds
        // Horizontal bounds: 0 <= x <= (mapWidth - viewportWidth)
        x = Math.max(0, Math.min(x, mapWidth - (int) (width / zoomLevel)));
        // Vertical bounds: 0 <= y <= (mapHeight - viewportHeight)
        y = Math.max(0, Math.min(y, mapHeight - (int) (height / zoomLevel)));

    }

    /**
     * Applies the camera transformation to a Graphics2D object. This involves scaling
     * the graphics context based on the zoom level and translating it to align the
     * viewport with the camera's position.
     *
     * @param g2d The Graphics2D context to apply the camera transformation on.
     */
    public void applyTransform(Graphics2D g2d) {
        g2d.scale(zoomLevel, zoomLevel);  // Apply scaling to handle zoom
        g2d.translate(-x, -y);            // Move the viewport to follow the camera

    }

    // Getters for camera position, dimensions, and zoom level

    /**
     * Gets the camera's x-coordinate (top-left corner of the viewport).
     *
     * @return The x-coordinate of the camera.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the camera's y-coordinate (top-left corner of the viewport).
     *
     * @return The y-coordinate of the camera.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the camera's current zoom level.
     *
     * @return The zoom level.
     */
    public float getZoomLevel() {
        return zoomLevel;
    }

    /**
     * Sets a new zoom level for the camera.
     *
     * @param zoomLevel The new zoom level.
     */
    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    /**
     * Gets the height of the viewport in pixels.
     *
     * @return The viewport height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the width of the viewport in pixels.
     *
     * @return The viewport width.
     */
    public int getWidth() {
        return width;
    }
    public void drawHealth(Graphics g) {
        int barWidth = 80;
        int barHeight = 10;

        // Draw health bar background
        g.setColor(Color.GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Calculate health percentage (max health is 100)
        float healthPercentage = (float) character.getHealth() / 100;
        int currentHealthWidth = (int) (barWidth * healthPercentage);

        // Draw current health
        g.setColor(Color.RED);
        g.fillRect(x, y, currentHealthWidth, barHeight);

        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barWidth, barHeight);

        // Draw health text
        g.setColor(Color.WHITE);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 8);

        g.setFont(customFont);
        String healthText = character.getHealth() + "/100";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(healthText)) / 2;
        int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(healthText, textX, textY);
    }
    public void drawStudyStudCount(Graphics g) {
        int barWidth = 100;
        int barHeight = 10;

        // Draw health bar background
        Color customPurpleColor = new Color(207, 187, 232);
        g.setColor(customPurpleColor);
        g.fillRect(this.x+125, y, barWidth, barHeight);

        // Calculate health percentage (max health is 100)
        float healthPercentage = (float) character.getHealth() / 100;
        int currentHealthWidth = (int) (barWidth * healthPercentage);

        // Draw current health
        g.setColor(Color.WHITE);
     //   g.fillRect(x, y, currentHealthWidth, barHeight);

        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(this.x+125, y, barWidth, barHeight);

        // Draw health text
        g.setColor(Color.WHITE);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 8);

        g.setFont(customFont);
        String studyStudText = "Study Stud Count: " + character.getStudyStudCount();
        FontMetrics fm = g.getFontMetrics();
        int textX = x+130;
        int textY = y + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();

      //  g.drawString(studyStudText, textX, textY);
        Color customColor = new Color(154, 82, 241);
        g.setColor(customColor);
      //  String studyStudCountText = "Study Studs: " + character.getStudy_stud_count();
        g.drawString(studyStudText, textX, textY);
    }

    public void drawXP(Graphics g) {
        int barWidth = 80;
        int barHeight = 10;


        // Draw XP bar background
        g.setColor(Color.GRAY);
        g.fillRect(x, y+15, barWidth, barHeight);

        // Calculate XP percentage using levelProgression and levelCap
        float xpPercentage = (float) character.getLevelProgression() / character.getLevelCap();
        int currentXPWidth = (int) (barWidth * xpPercentage);

        // Draw current XP
        g.setColor(Color.BLUE);
        g.fillRect(x, y+15, currentXPWidth, barHeight);

        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y+15, barWidth, barHeight);

        // Draw XP text
        g.setColor(Color.WHITE);
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 8);

        g.setFont(customFont);
        String xpText = "Level " + character.getLevel() + " (" +
                character.getLevelProgression() + "/" + character.getLevelCap() + ")";
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(xpText)) / 2;
        int textY = y + 15 + ((barHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(xpText, textX, textY);
    }

    }


