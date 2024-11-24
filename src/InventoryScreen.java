import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class InventoryScreen extends JDialog {
    private Character character;
    private JList<Item> itemList;
    private DefaultListModel<Item> listModel;

    public InventoryScreen(Character character) {
        super((Frame) null, "Inventory", true);
        this.character = character;
        setupUI();
    }

    private void setupUI() {
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create the list model and populate it with the character's inventory items
        listModel = new DefaultListModel<>();
        for (Item item : character.getInventory()) {
            listModel.addElement(item);
        }

        // Create the JList to display items and set a custom renderer
        itemList = new JList<>(listModel);
        itemList.setCellRenderer(new ItemCellRenderer());

        JScrollPane scrollPane = new JScrollPane(itemList);
        add(scrollPane, BorderLayout.CENTER);

        // Create the button panel
        JPanel buttonPanel = new JPanel();
        JButton useButton = new JButton("Use Item");
        useButton.addActionListener(e -> handleUseItem());
        buttonPanel.add(useButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleUseItem() {
        Item selectedItem = itemList.getSelectedValue();
        if (selectedItem != null) {
            if (selectedItem.getQuantity() > 0) {
                switch (selectedItem.getName().toLowerCase()) {
                    case "apple":
                        character.addHealth(10);
                        JOptionPane.showMessageDialog(this, "You used an Apple and restored 10 health!");
                        break;
                    // Handle other items...
                    default:
                        JOptionPane.showMessageDialog(this, "This item cannot be used.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
                selectedItem.setQuantity(selectedItem.getQuantity() - 1);
                if (selectedItem.getQuantity() <= 0) {
                    character.removeItem(selectedItem);
                    listModel.removeElement(selectedItem);
                } else {
                    itemList.repaint();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No item selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom cell renderer to display item details
    class ItemCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Item item = (Item) value;
            String display = item.getName() + " x" + item.getQuantity() + " - " + item.getDescription();
            return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
        }
    }
}
