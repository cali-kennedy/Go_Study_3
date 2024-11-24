public class Item {
    private String name;
    private String description;
    private int quantity;
    private boolean stackable;

    public Item(String name, String description, int quantity, boolean stackable) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.stackable = stackable;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public boolean isStackable() { return stackable; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Optional: Override toString for better display in lists
    @Override
    public String toString() {
        return name + " x" + quantity;
    }
}
