package models;

import java.util.ArrayList;
import java.util.List;

public class TilesetModel {
    private String name;
    private int firstGid;
    private String tilesetSource;
    private double width;
    private double height;
    private int tileCount;
    private int columns;
    private String imageSource;
    private int imageWidth;
    private int imageHeight;
    private List<AnimationModel> animations = new ArrayList<>();  // New field for animations
    public TilesetModel(){
        this.name = "";
        this.firstGid = 0;
        this.tilesetSource = "";
    }
    // Getter and setter for animations
    public List<AnimationModel> getAnimations() {
        return animations;
    }

    public void addAnimation(AnimationModel animation) {
        animations.add(animation);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFirstGid() {
        return firstGid;
    }

    public void setFirstGid(int firstGid) {
        this.firstGid = firstGid;
    }

    public String getTilesetSource() {
        return tilesetSource;
    }
    public void setTilesetSource(String tilesetSource) {
        this.tilesetSource = tilesetSource;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TilesetModel Details:\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("First GID: ").append(firstGid).append("\n");
        sb.append("Tileset Source: ").append(tilesetSource).append("\n");
        sb.append("Width: ").append(width).append("\n");
        sb.append("Height: ").append(height).append("\n");
        sb.append("Tile Count: ").append(tileCount).append("\n");
        sb.append("Columns: ").append(columns).append("\n");
        sb.append("Image Source: ").append(imageSource).append("\n");
        sb.append("Image Width: ").append(imageWidth).append("\n");
        sb.append("Image Height: ").append(imageHeight).append("\n");
        sb.append("Animations: ").append(animations).append("\n"); // Will call toString on each AnimationModel
        return sb.toString();
    }
}
