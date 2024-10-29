import java.awt.*;

public class Camera {
    private int x, y;  // Camera position
    private int width, height;  // Viewport size (screen dimensions)
    private float zoomLevel;  // Zoom level (default = 1.0f)

    public Camera(int width, int height, float zoomLevel) {
        this.width = width;
        this.height = height;
        this.zoomLevel = zoomLevel;
    }

    // Update the camera to follow the character
    public void update( int mapWidth, int mapHeight) {
        // Center the camera on the character
        //character.getX, character.getY
        x = (int) (1 - width / (2 * zoomLevel));
        y = (int) (1 - height / (2 * zoomLevel));

        // Clamp the camera within the bounds of the map
        x = Math.max(0, Math.min(x, mapWidth - (int) (width / zoomLevel)));
        y = Math.max(0, Math.min(y, mapHeight - (int) (height / zoomLevel)));
    }

    // Apply the camera transform (translation + zoom)
    public void applyTransform(Graphics2D g2d) {
        g2d.scale(zoomLevel, zoomLevel);
        g2d.translate(-x, -y);
    }

    // Getters for the camera's position
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}
