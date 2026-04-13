package pegSolitare;

/**
 * Abstract base class for both Manual and Automated peg solitaire games.
 * Defines the common contract that all game modes must fulfil.
 */
public abstract class Game {

    protected Board        board;
    protected int          moveCount;
    protected boolean      gameOver;
    protected GameRecorder recorder; // null when recording is off

    public Game(int size, String boardType) {
        this.board     = new Board(size, boardType);
        this.moveCount = 0;
        this.gameOver  = false;
    }

    // ── Template / common operations ─────────────────────────────────

    /** Reset the game with a fresh board of the same size and type. */
    public void newGame() {
        board     = new Board(board.getSize(), board.getType());
        moveCount = 0;
        gameOver  = false;
        onNewGame();
    }

    /** Randomise the board state: scatter pegs randomly, keeping the shape. */
    public void randomizeBoard() {
        board.randomize();
        moveCount = 0;
        gameOver  = !board.hasValidMoves();
        if (recorder != null) recorder.recordRandomize(board);
        onRandomize();
    }

    /**
     * Attempt to apply a move from (fromR,fromC) → (toR,toC).
     * Returns true if the move was valid and applied.
     */
    public boolean makeMove(int fromR, int fromC, int toR, int toC) {
        if (gameOver) return false;
        boolean ok = board.applyMove(fromR, fromC, toR, toC);
        if (ok) {
            moveCount++;
            checkGameOver();
            if (recorder != null) recorder.recordMove(fromR, fromC, toR, toC);
        }
        return ok;
    }

    /** Check and update gameOver flag. */
    void checkGameOver() {
        if (board.countPegs() == 1 || !board.hasValidMoves()) {
            gameOver = true;
        }
    }

    // ── Abstract hooks ────────────────────────────────────────────────

    /** Called after newGame() — subclasses can do extra reset work. */
    protected abstract void onNewGame();

    /** Called after randomizeBoard() — subclasses can react. */
    protected abstract void onRandomize();

    /**
     * Compute the next move for automated play.
     * Returns int[]{fromR,fromC,toR,toC}, or null if none available.
     */
    public abstract int[] getNextMove();

    /** Human-readable name for the game mode. */
    public abstract String getModeName();

    // ── Recorder ─────────────────────────────────────────────────────

    public void setRecorder(GameRecorder r) { this.recorder = r; }

    // ── Accessors ─────────────────────────────────────────────────────

    public Board   getBoard()     { return board; }
    public int     getMoveCount() { return moveCount; }
    public boolean isGameOver()   { return gameOver; }

    public String getStatusMessage() {
        int pegs = board.countPegs();
        if (pegs == 1)              return "You win! Only 1 peg left!";
        if (!board.hasValidMoves()) return "No moves left. Pegs: " + pegs;
        return "Pegs: " + pegs + "  |  Moves: " + moveCount;
    }
}
