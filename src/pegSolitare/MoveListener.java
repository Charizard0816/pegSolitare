package pegSolitare;

/**
 * Callback interface used by BoardPanel to notify the application
 * that a move has been completed, without creating a direct dependency
 * on the gui class.
 */
public interface MoveListener {
    void onMoveCompleted();
}
