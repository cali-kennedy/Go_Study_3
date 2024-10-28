
public class TmxMapModel {
        // Map parameters
        private int mapWidth;
        private int mapHeight;
        private int tileWidth;
        private int tileHeight;
        // LayerModel parameters
        private int layerId;
        private int layerWidth;
        private int layerHeight;
        private String layerName;
        // Tileset parameters
        private String tileSetName;
        private int tileSetFirstGid;
        private String tileSetSource = "resources/assets";
        private static final int NONE = 0;

        public TmxMapModel()
        {
            this.mapWidth = NONE;
            this.mapHeight = NONE;
            this.tileWidth = NONE;
            this.tileHeight = NONE;
            this.layerId = NONE;
            this.layerName = "";
            this.tileSetName = "";
            this.tileSetFirstGid = 0;
            this.tileSetSource = "";
        }


    // Getters and Setters
    public void setMapWidth(int map_width){
        this.mapWidth = map_width;
    }

    public int getMapWidth(){
        return this.mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getLayerId() {
        return layerId;
    }

    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    public int getLayerHeight() {
        return layerHeight;
    }

    public int getLayerWidth() {
        return layerWidth;
    }

    public void setLayerHeight(int layerHeight) {
        this.layerHeight = layerHeight;
    }

    public void setLayerWidth(int layerWidth) {
        this.layerWidth = layerWidth;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getTileSetName() {
        return tileSetName;
    }

    public void setTileSetName(String tileSetName) {
        this.tileSetName = tileSetName;
    }

    public int getTileSetFirstGid() {
        return tileSetFirstGid;
    }

    public void setTileSetFirstGid(int tileSetFirstGid) {
        this.tileSetFirstGid = tileSetFirstGid;
    }

    public String getTileSetSource() {
        return tileSetSource;
    }

    public void setTileSetSource(String tileSetSource) {
        this.tileSetSource = tileSetSource;
    }
}
