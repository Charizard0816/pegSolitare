package pegSolitare;

import java.util.Random;

/**
 * Board model.
 * Cell values:
 *   -1 = not part of the board
 *    0 = empty hole
 *    1 = peg
 */
public class Board {

    public static final int OUT  = -1;
    public static final int HOLE =  0;
    public static final int PEG  =  1;

    private int[][] grid;
    private int size;
    private String type;

    public Board(int size, String type) {
        this.size = size;
        this.type = type;
        grid = new int[size][size];
        initBoard();
    }

    // ── Board initialization ──────────────────────────────────────────

    private void initBoard() {
        switch (type) {
            case "English":  buildEnglish();  break;
            case "Hexagon":  buildHexagon();  break;
            case "Diamond":  buildDiamond();  break;
            default:         buildEnglish();  break;
        }
    }

    private void buildEnglish() {
        int third = size / 3;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                boolean inMiddleRows = r >= third && r < size - third;
                boolean inMiddleCols = c >= third && c < size - third;
                grid[r][c] = (inMiddleRows || inMiddleCols) ? PEG : OUT;
            }
        }
        grid[size / 2][size / 2] = HOLE;
    }

    private void buildHexagon() {
        int cut = size / 3;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int distFromEdgeRow = Math.min(r, size - 1 - r);
                int distFromEdgeCol = Math.min(c, size - 1 - c);
                grid[r][c] = (distFromEdgeRow + distFromEdgeCol < cut) ? OUT : PEG;
            }
        }
        grid[size / 2][size / 2] = HOLE;
    }

    private void buildDiamond() {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = (Math.abs(r - mid) + Math.abs(c - mid) <= mid) ? PEG : OUT;
            }
        }
        grid[mid][mid] = HOLE;
    }

    // ── Randomize ─────────────────────────────────────────────────────

    /**
     * Randomize the board by playing random valid moves forward from the
     * current state. The result is always reachable from wherever the game
     * currently is -- no illegal peg patterns are ever produced.
     *
     * A random number of moves between 1 and the number of pegs currently
     * on the board is played. If the random walk gets stuck before reaching
     * the target (no moves left), we stop early -- that is still a valid
     * game state, just a game-over one.
     */
    public void randomize() {
        Random rng = new Random();

        // Pick a random number of moves to play from the current state
        int totalPegs   = countPegs();
        int targetMoves = 1 + rng.nextInt(Math.max(1, totalPegs - 1));

        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};

        for (int step = 0; step < targetMoves; step++) {
            // Collect all valid moves from the current state
            java.util.List<int[]> moves = new java.util.ArrayList<>();
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (grid[r][c] != PEG) continue;
                    for (int[] d : dirs) {
                        int tr = r + d[0], tc = c + d[1];
                        if (isValidMove(r, c, tr, tc))
                            moves.add(new int[]{r, c, tr, tc});
                    }
                }
            }

            if (moves.isEmpty()) break; // no moves left -- valid game-over state

            // Pick and apply a random move
            int[] m = moves.get(rng.nextInt(moves.size()));
            applyMove(m[0], m[1], m[2], m[3]);
        }
    }

    // ── Move logic ────────────────────────────────────────────────────

    public boolean applyMove(int fromR, int fromC, int toR, int toC) {
        if (!isValidMove(fromR, fromC, toR, toC)) return false;
        int midR = (fromR + toR) / 2;
        int midC = (fromC + toC) / 2;
        grid[fromR][fromC] = HOLE;
        grid[midR][midC]   = HOLE;
        grid[toR][toC]     = PEG;
        return true;
    }

    /** Undo a previously applied move (used by the solver for look-ahead). */
    public void undoMove(int fromR, int fromC, int toR, int toC) {
        int midR = (fromR + toR) / 2;
        int midC = (fromC + toC) / 2;
        grid[fromR][fromC] = PEG;
        grid[midR][midC]   = PEG;
        grid[toR][toC]     = HOLE;
    }

    public boolean isValidMove(int fromR, int fromC, int toR, int toC) {
        if (!inBounds(fromR, fromC) || !inBounds(toR, toC)) return false;
        int dr = toR - fromR, dc = toC - fromC;
        if (!((Math.abs(dr) == 2 && dc == 0) || (dr == 0 && Math.abs(dc) == 2))) return false;
        int midR = (fromR + toR) / 2;
        int midC = (fromC + toC) / 2;
        return grid[fromR][fromC] == PEG
            && grid[midR][midC]   == PEG
            && grid[toR][toC]     == HOLE;
    }

    public boolean hasValidMoves() {
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != PEG) continue;
                for (int[] d : dirs)
                    if (isValidMove(r, c, r + d[0], c + d[1])) return true;
            }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < size && c >= 0 && c < size;
    }

    public int countPegs() {
        int count = 0;
        for (int[] row : grid)
            for (int cell : row)
                if (cell == PEG) count++;
        return count;
    }

    public int getCell(int r, int c) { return grid[r][c]; }
    public int getSize()             { return size; }
    public String getType()          { return type; }
}
