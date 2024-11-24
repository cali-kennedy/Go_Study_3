// MessageOverlayPanel.java
import javax.swing.*;
import java.awt.*;

public class MessageOverlayPanel extends JPanel {
    private JLabel messageLabel;

    public MessageOverlayPanel() {
        setOpaque(false); // Allow transparency
        setLayout(new GridBagLayout()); // Center the message
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 17);
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(customFont);
        messageLabel.setForeground(Color.WHITE);
        add(messageLabel);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    // Paint semi-transparent background
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paint child components first
        Graphics2D g2d = (Graphics2D) g.create();
        // Set a semi-transparent black background
        g2d.setColor(new Color(0, 0, 0, 170)); // 128 is 50% transparency
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}
