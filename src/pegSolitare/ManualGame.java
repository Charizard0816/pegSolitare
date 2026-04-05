package pegSolitare;

/**
 * Manual game mode.
 * Moves are applied by the player via mouse clicks in the GUI.
 * getNextMove() is not used in this mode (returns null).
 */
public class ManualGame extends Game {

    public ManualGame(int size, String boardType) {
        super(size, boardType);
    }

    @Override
    protected void onNewGame() {
    }

    @Override
    protected void onRandomize() {
    }

    @Override
    public int[] getNextMove() {
        return null;
    }

    @Override
    public String getModeName() {
        return "Manual";
    }
}
