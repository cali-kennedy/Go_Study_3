package models;

public class FrameModel {
    private int tileId;
    private int duration;

    public FrameModel(int tileId, int duration) {
        this.tileId = tileId;
        this.duration = duration;
    }

    public int getTileId() {
        return tileId;
    }

    public int getDuration() {
        return duration;
    }
}
