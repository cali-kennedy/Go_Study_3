import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class InputHandler {
    private final int MOVE_DISTANCE = 10;
    private Character character;
    private JPanel panel;

    public InputHandler(Character character, JPanel panel) {
        this.character = character;
        this.panel = panel;
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // Set up the input map and action map for when the panel has focus
        InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = panel.getActionMap();

        // Setup key bindings for WASD
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "move_up");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "move_down");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "move_left");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "move_right");

        // Add actions for each movement
        am.put("move_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(0, -MOVE_DISTANCE);
                panel.repaint();
            }
        });

        am.put("move_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(0, MOVE_DISTANCE);
                panel.repaint();
            }
        });

        am.put("move_left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(-MOVE_DISTANCE, 0);
                panel.repaint();
            }
        });

        am.put("move_right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(MOVE_DISTANCE, 0);
                panel.repaint();
            }
        });
    }
}