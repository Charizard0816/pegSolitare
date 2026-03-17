package pegSolitare;

/**
 * Board model.
 * Cell values:
 *   -1 = not part of the board (padding)
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
            case "English":
                buildEnglish();
                break;
            case "Hexagon":
                buildHexagon();
                break;
            case "Diamond":
                buildDiamond();
                break;
            default:
                buildEnglish();
                break;
        }
    }

    /**
     * English (plus-shaped) board.
     */
    private void buildEnglish() {
        int third = size / 3;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                boolean inMiddleRows = r >= third && r < size - third;
                boolean inMiddleCols = c >= third && c < size - third;
                if (inMiddleRows || inMiddleCols) {
                    grid[r][c] = PEG;
                } else {
                    grid[r][c] = OUT;
                }
            }
        }
        // Center hole
        grid[size / 2][size / 2] = HOLE;
    }

    /**
     * Hexagon board — full rectangular grid with corners cut.
     */
    private void buildHexagon() {
        int cut = size / 4;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int distFromEdgeRow = Math.min(r, size - 1 - r);
                int distFromEdgeCol = Math.min(c, size - 1 - c);
                if (distFromEdgeRow + distFromEdgeCol < cut) {
                    grid[r][c] = OUT;
                } else {
                    grid[r][c] = PEG;
                }
            }
        }
        grid[size / 2][size / 2] = HOLE;
    }

    /**
     * Diamond board — rotated square.
     */
    private void buildDiamond() {
        int mid = size / 2;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (Math.abs(r - mid) + Math.abs(c - mid) <= mid) {
                    grid[r][c] = PEG;
                } else {
                    grid[r][c] = OUT;
                }
            }
        }
        grid[mid][mid] = HOLE;
    }

    // ── Move logic ────────────────────────────────────────────────────

    /**
     * Attempt a move: peg at (fromR, fromC) jumps over (midR, midC) into (toR, toC).
     * Returns true if the move was valid and applied.
     */
    public boolean applyMove(int fromR, int fromC, int toR, int toC) {
        int midR = (fromR + toR) / 2;
        int midC = (fromC + toC) / 2;

        if (!isValidMove(fromR, fromC, toR, toC)) return false;

        grid[fromR][fromC] = HOLE;
        grid[midR][midC]   = HOLE;
        grid[toR][toC]     = PEG;
        return true;
    }

    public boolean isValidMove(int fromR, int fromC, int toR, int toC) {
        // Both cells must be in bounds
        if (!inBounds(fromR, fromC) || !inBounds(toR, toC)) return false;

        // Must be a 2-step jump in exactly one cardinal direction
        int dr = toR - fromR;
        int dc = toC - fromC;
        if (!((Math.abs(dr) == 2 && dc == 0) || (dr == 0 && Math.abs(dc) == 2))) return false;

        int midR = (fromR + toR) / 2;
        int midC = (fromC + toC) / 2;

        return grid[fromR][fromC] == PEG
            && grid[midR][midC]   == PEG
            && grid[toR][toC]     == HOLE;
    }

    /** Returns true if any valid move exists on the board. */
    public boolean hasValidMoves() {
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != PEG) continue;
                for (int[] d : dirs) {
                    if (isValidMove(r, c, r + d[0], c + d[1])) return true;
                }
            }
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
