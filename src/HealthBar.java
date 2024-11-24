import javax.swing.*;
import java.awt.*;

public class HealthBar extends JComponent {
    private int maxHealth;
    private int currentHealth;
    private Color foregroundColor;
    private Color backgroundColor;

    public HealthBar(int maxHealth, int currentHealth, Color foregroundColor, Color backgroundColor) {
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        setPreferredSize(new Dimension(200, 20));
        setOpaque(false);
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = (int) ((currentHealth / (double) maxHealth) * getWidth());
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(foregroundColor);
        g.fillRect(0, 0, width, getHeight());
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
}
