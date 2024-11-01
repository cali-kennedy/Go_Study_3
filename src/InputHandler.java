import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private final int MOVE_DISTANCE = 10;
    private final int COOLDOWN_PERIOD = 1;
    private Character character;
    private double lastMoveTime;

    public InputHandler(Character character){
        this.character = character;

        this.lastMoveTime = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime >= COOLDOWN_PERIOD) {
            handleMovement(e.getKeyCode());
            lastMoveTime = currentTime;
        }
    }

    private void handleMovement(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                character.move(0, -MOVE_DISTANCE);;
                break;
            case KeyEvent.VK_S:
                character.move(0, MOVE_DISTANCE);
                break;
            case KeyEvent.VK_A:
                character.move(-MOVE_DISTANCE, 0);
                break;
            case KeyEvent.VK_D:
                character.move(MOVE_DISTANCE, 0);
                break;
        }
    }
}
