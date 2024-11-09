package models;

import java.util.ArrayList;

public class ObjectModel {
    private int id;
    private String name;
    private int gid;
    private double x;
    private double y;
    private double width;
    private double height;
    private ArrayList<ObjectPropertiesModel> properties;
    private String layerName;
    public ObjectModel(){
        this.id =0;
        this.name = "";
        this.gid = 0;
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
     //   this.properties = null;
        this.properties = new ArrayList<ObjectPropertiesModel>(); // Initialize as an empty list to avoid NullPointerException
    }

    public ArrayList<ObjectPropertiesModel> getProperties() {
        return properties;
    }

    public void addProperty(ObjectPropertiesModel property) {
        this.properties.add(property); // Add individual property
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
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

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
}
