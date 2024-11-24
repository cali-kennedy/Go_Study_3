import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Handles keyboard input for character movement in a specified panel.
 * Configures key bindings for moving a character up, down, left, and right.
 */
public class InputHandler {
    private static final int MOVE_DISTANCE = 10;  // Distance the character moves with each key press
    private final Character character;            // The character object to control
    private final JPanel panel;                   // The panel to repaint after movement

    /**
     * Constructs an InputHandler for a character in a given panel.
     *
     * @param character The character to be controlled by the input handler.
     * @param panel     The JPanel on which the character is displayed.
     */
    public InputHandler(Character character, JPanel panel) {
        this.character = character;
        this.panel = panel;
        setupKeyBindings();
    }

    /**
     * Configures key bindings for character movement using the WASD keys.
     * W, A, S, and D keys are mapped to actions that move the character in
     * the corresponding direction and repaint the panel to reflect changes.
     */
    private void setupKeyBindings() {
        // Set up input and action maps for when the panel is focused
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        // Associate key strokes with specific action names
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "move_up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "move_down");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "move_left");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "move_right");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0, false), "open_inventory");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0, false), "use_item");



        // Map each action name to an action that moves the character
        actionMap.put("use_item", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Use the first item in the inventory as an example
                if (!character.getInventory().isEmpty()) {
                    Item itemToUse = character.getInventory().get(0);
                    character.useItem(itemToUse);
                    System.out.println("Used item: " + itemToUse.getName());
                } else {
                    System.out.println("Inventory is empty!");
                }            // Refresh panel display after movement
            }
        });

        // Map each action name to an action that moves the character
        actionMap.put("open_inventory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.displayInventory();               // Refresh panel display after movement
            }
        });

        // Map each action name to an action that moves the character
        actionMap.put("move_up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(0, -MOVE_DISTANCE);  // Move character up by MOVE_DISTANCE
                panel.repaint();                    // Refresh panel display after movement
            }
        });

        actionMap.put("move_down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(0, MOVE_DISTANCE);   // Move character down by MOVE_DISTANCE
                panel.repaint();
            }
        });

        actionMap.put("move_left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(-MOVE_DISTANCE, 0);  // Move character left by MOVE_DISTANCE
                panel.repaint();
            }
        });

        actionMap.put("move_right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                character.move(MOVE_DISTANCE, 0);   // Move character right by MOVE_DISTANCE
                panel.repaint();
            }
        });
    }
}
