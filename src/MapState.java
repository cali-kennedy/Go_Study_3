import java.util.HashSet;
import java.util.Set;

public class MapState {
    private String mapName;
    private Set<String> encounteredObjects;

    public MapState(String mapName) {
        this.mapName = mapName;
        this.encounteredObjects = new HashSet<>();
    }

    public String getMapName() {
        return mapName;
    }

    public Set<String> getEncounteredObjects() {
        return encounteredObjects;
    }

    public void addEncounteredObject(String objectName) {
        encounteredObjects.add(objectName.toLowerCase());
    }
}
