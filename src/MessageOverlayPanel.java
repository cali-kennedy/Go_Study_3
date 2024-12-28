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

        // Setting the label to allow HTML-based formatting
        // This will help in text wrapping once we set HTML content.
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(messageLabel);
    }

    public void setMessage(String message) {
        // We can specify a maximum width for wrapping. Here, we pick a fixed width.
        // You can adjust the width based on your requirements or panel size.
        int maxWidth = 300; // for example, 300px wide
        Font customFont = FontUtils.loadFont("/fonts/Bungee-Regular.ttf", 15);
        String htmlMessage = "<html><body style='width:" + maxWidth + "px; text-align:center;'>"
                + message
                + "</body></html>";
        messageLabel.setText(htmlMessage);
        messageLabel.setFont(customFont);
    }

    // Paint semi-transparent background
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Paint child components first
        Graphics2D g2d = (Graphics2D) g.create();
        // Set a semi-transparent black background
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}
