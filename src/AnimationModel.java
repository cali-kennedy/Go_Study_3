import java.util.ArrayList;
import java.util.List;

public class AnimationModel {
    private List<FrameModel> frames;
    private int currentFrameIndex;
    private long lastFrameTime;
    private int firstGid; // Add this to store the tileset's first GID
    private double x;
    private double y;
    public AnimationModel(int firstGid) {
        this.frames = new ArrayList<>();
        this.currentFrameIndex = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public void addFrame(FrameModel frame) {
        frames.add(frame);
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        FrameModel currentFrame = frames.get(currentFrameIndex);

        if (currentTime - lastFrameTime >= currentFrame.getDuration()) {
            currentFrameIndex = (currentFrameIndex + 1) % frames.size();
            lastFrameTime = currentTime;
        }
    }

    public int getCurrentTileId() {
        return frames.get(currentFrameIndex).getTileId();
    }
}
