package models;

import java.util.List;
import java.util.ArrayList;

public class LayerModel {
    private String name;
    private int width, height;
    private List<Integer> tileData; // Store tile IDs in a list
    private int layerId;
    public LayerModel(String name, int width, int height, String data, int layerId) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileData = parseTileData(data);
        this.layerId = layerId;
    }

    public int getLayerWidth(){
        return this.width;
    }
    public int getLayerHeight(){
        return this.height;
    }

    // Method to parse and set tile data from CSV format
    private List<Integer> parseTileData(String data) {
        List<Integer> tileIds = new ArrayList<>();
        String[] dataEntries = data.split(",");
        for (String entry : dataEntries) {
            tileIds.add(Integer.parseInt(entry.trim())); // Add each tile ID as an Integer
        }
        return tileIds;
    }

    public int getTileIdAt(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IndexOutOfBoundsException("Coordinates are out of layer bounds.");
        }
        int index = y * width + x;  // Calculate index in 1D array
        return tileData.get(index);
    }

    public List<Integer> getTileData() {
        return tileData;
    }

    public String getLayerName() {
        return this.name;
    }
}
