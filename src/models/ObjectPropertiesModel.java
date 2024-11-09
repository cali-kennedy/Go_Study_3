package models;

public class ObjectPropertiesModel {
    private String propertyName;
    private String type;
    private String value;
    private String objectName;
    private Object Object;

    public ObjectPropertiesModel(){
        this.propertyName = "";
        this.type = "";
        this.value = "";
        this.objectName = "";
    }
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return "models.ObjectPropertiesModel{" +
                "propertyName='" + propertyName + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", objectName='" + objectName + '\'' +
                '}';
    }
}
