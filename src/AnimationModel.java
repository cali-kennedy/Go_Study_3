import java.util.ArrayList;
import java.util.List;

public class AnimationModel {
    private List<FrameModel> frames;
    private int currentFrameIndex;
    private long lastFrameTime;
    private int firstGid;
    private double x;
    private double y;

    public AnimationModel(int firstGid) {
        this.frames = new ArrayList<>();
        this.currentFrameIndex = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.firstGid = firstGid;
        this.x = 0;
        this.y = 0;
    }

    public void addFrame(FrameModel frame) {
        if (frame != null) { // Ensure frames are not null
            frames.add(frame);
        }
    }

    public void update() {
        if (frames.isEmpty()) return; // Avoid updating if no frames are present
        long currentTime = System.currentTimeMillis();
        FrameModel currentFrame = frames.get(currentFrameIndex);

        if (currentTime - lastFrameTime >= currentFrame.getDuration()) {
            currentFrameIndex = (currentFrameIndex + 1) % frames.size(); // Cycle through frames
            lastFrameTime = currentTime;
          //  System.out.println("Updating frame for GID " + firstGid + " to tile ID " + getCurrentTileId());
        }
    }

    public int getCurrentTileId() {
        return frames.isEmpty() ? -1 : frames.get(currentFrameIndex).getTileId(); // Return -1 if frames are empty
    }

    public void setFrames(List<FrameModel> frames) {
        this.frames = frames != null ? frames : new ArrayList<>(); // Ensure frames list is not null
    }

    public List<FrameModel> getFrames() {
        return frames;
    }

    public int getFirstGid() {
        return firstGid;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }
    public void setFirstGid(int gid){
        this.firstGid = gid;
    }
}