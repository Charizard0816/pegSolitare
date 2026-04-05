package pegSolitare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Automated game mode.
 * The computer selects moves automatically using a heuristic solver.
 *
 * Strategy (greedy heuristic):
 *   1. Prefer moves that jump toward the center of the board.
 *   2. Among equal-distance moves, pick one that leaves the most future
 *      moves available (one-step look-ahead).
 *   3. Fall back to the first valid move if the heuristic is tied.
 */
public class AutomatedGame extends Game {

    /** Delay between automated moves in milliseconds (used by GUI timer). */
    public static final int AUTO_DELAY_MS = 600;

    public AutomatedGame(int size, String boardType) {
        super(size, boardType);
    }

    @Override
    protected void onNewGame() { /* nothing extra */ }

    @Override
    protected void onRandomize() { /* nothing extra */ }

    @Override
    public String getModeName() { return "Automated"; }

    // ── Solver ────────────────────────────────────────────────────────

    /**
     * Returns the best next move as int[]{fromR, fromC, toR, toC},
     * or null if no valid move exists.
     */
    @Override
    public int[] getNextMove() {
        List<int[]> candidates = getAllValidMoves();
        if (candidates.isEmpty()) return null;

        // Shuffle for variety when scores are tied
        Collections.shuffle(candidates);

        int   boardSize = board.getSize();
        int   mid       = boardSize / 2;
        int[] best      = null;
        int   bestScore = Integer.MIN_VALUE;

        for (int[] m : candidates) {
            int score = scoreMove(m, mid);
            if (score > bestScore) {
                bestScore = score;
                best      = m;
            }
        }
        return best;
    }

    /**
     * Heuristic score for a candidate move.
     * Higher is better.
     */
    private int scoreMove(int[] m, int mid) {
        int fromR = m[0], fromC = m[1], toR = m[2], toC = m[3];

        // Reward landing closer to the center
        int distBefore = Math.abs(fromR - mid) + Math.abs(fromC - mid);
        int distAfter  = Math.abs(toR   - mid) + Math.abs(toC   - mid);
        int centrality = distBefore - distAfter;   // positive = moved toward center

        // One-step look-ahead: count moves available after this move
        board.applyMove(fromR, fromC, toR, toC);
        int futureMoves = countAllValidMoves();
        // undo
        board.undoMove(fromR, fromC, toR, toC);

        return centrality * 10 + futureMoves;
    }

    private List<int[]> getAllValidMoves() {
        List<int[]> moves = new ArrayList<>();
        int size = board.getSize();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCell(r, c) != Board.PEG) continue;
                for (int[] d : dirs) {
                    int tr = r + d[0], tc = c + d[1];
                    if (board.isValidMove(r, c, tr, tc))
                        moves.add(new int[]{r, c, tr, tc});
                }
            }
        }
        return moves;
    }

    private int countAllValidMoves() {
        int size = board.getSize();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        int count = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCell(r, c) != Board.PEG) continue;
                for (int[] d : dirs) {
                    if (board.isValidMove(r, c, r + d[0], c + d[1])) count++;
                }
            }
        }
        return count;
    }
}
