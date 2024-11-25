import java.util.HashMap;
import java.util.Map;

public class NPCInteractionTracker {
    private static Map<String, Boolean> xpCollectedMap = new HashMap<>();

    public static boolean isXpCollected(String npcName) {
        return xpCollectedMap.getOrDefault(npcName, false);
    }

    public static void setXpCollected(String npcName) {
        xpCollectedMap.put(npcName, true);
    }
}