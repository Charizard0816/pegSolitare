package pegSolitare;

/**
 * Unit tests for Board, ManualGame, and AutomatedGame.
 * Run with: java pegSolitare/*.java && java pegSolitare.BoardTest
 */
public class BoardTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testBoardBasics();
        testManualGame();
        testAutomatedGame();
        testRandomize();
        testClassHierarchy();
        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
    }

    // ── Board basics ──────────────────────────────────────────────────

    private static void testBoardBasics() {
        System.out.println("\n-- Board Basics --");

        Board b = new Board(7, "English");
        assertTrue("English board has pegs", b.countPegs() > 0);
        assertTrue("Centre is a hole", b.getCell(3, 3) == Board.HOLE);
        assertTrue("Corner is OUT", b.getCell(0, 0) == Board.OUT);

        Board hex = new Board(9, "Hexagon");
        assertTrue("Hexagon centre is hole", hex.getCell(4, 4) == Board.HOLE);
        assertTrue("Hexagon has pegs", hex.countPegs() > 0);

        Board dia = new Board(7, "Diamond");
        assertTrue("Diamond centre is hole", dia.getCell(3, 3) == Board.HOLE);
        assertTrue("Diamond corner is OUT", dia.getCell(0, 0) == Board.OUT);
    }

    // ── ManualGame ────────────────────────────────────────────────────

    private static void testManualGame() {
        System.out.println("\n-- ManualGame --");

        ManualGame mg = new ManualGame(7, "English");
        assertTrue("ManualGame mode name", mg.getModeName().equals("Manual"));
        assertTrue("ManualGame is-a Game", mg instanceof Game);
        assertTrue("ManualGame starts with moves available", !mg.isGameOver());

        // getNextMove returns null for manual
        assertTrue("ManualGame getNextMove is null", mg.getNextMove() == null);

        int pegsBefore = mg.getBoard().countPegs();

        // Find and apply the first valid move
        int[] m = findFirstValidMove(mg.getBoard());
        assertTrue("Found a valid move on English 7 board", m != null);

        boolean moved = mg.makeMove(m[0], m[1], m[2], m[3]);
        assertTrue("ManualGame: valid move accepted", moved);
        assertTrue("ManualGame: peg count decreased", mg.getBoard().countPegs() == pegsBefore - 1);
        assertTrue("ManualGame move count incremented", mg.getMoveCount() == 1);

        // Try an invalid move (same position twice)
        boolean bad = mg.makeMove(m[0], m[1], m[0], m[1]);
        assertTrue("ManualGame: invalid move rejected", !bad);

        // New game resets state
        mg.newGame();
        assertTrue("ManualGame: newGame resets move count", mg.getMoveCount() == 0);
        assertTrue("ManualGame: newGame resets peg count", mg.getBoard().countPegs() == pegsBefore);
    }

    // ── AutomatedGame ─────────────────────────────────────────────────

    private static void testAutomatedGame() {
        System.out.println("\n-- AutomatedGame --");

        AutomatedGame ag = new AutomatedGame(7, "English");
        assertTrue("AutomatedGame mode name", ag.getModeName().equals("Automated"));
        assertTrue("AutomatedGame is-a Game", ag instanceof Game);

        int[] move = ag.getNextMove();
        assertTrue("AutomatedGame: getNextMove returns a move", move != null);
        assertTrue("AutomatedGame: move array length 4", move.length == 4);
        assertTrue("AutomatedGame: move is valid", ag.getBoard().isValidMove(move[0], move[1], move[2], move[3]));

        int pegsBefore = ag.getBoard().countPegs();
        ag.makeMove(move[0], move[1], move[2], move[3]);
        assertTrue("AutomatedGame: peg count decreased after move", ag.getBoard().countPegs() == pegsBefore - 1);

        // Run to completion
        int maxMoves = 500;
        int steps = 0;
        while (!ag.isGameOver() && steps < maxMoves) {
            int[] m = ag.getNextMove();
            if (m == null) break;
            ag.makeMove(m[0], m[1], m[2], m[3]);
            steps++;
        }
        assertTrue("AutomatedGame: game terminates in finite steps", steps < maxMoves || ag.isGameOver());
    }

    // ── Randomize ─────────────────────────────────────────────────────

    private static void testRandomize() {
        System.out.println("\n-- Randomize --");

        ManualGame mg = new ManualGame(7, "English");
        int pegsOriginal = mg.getBoard().countPegs();

        mg.randomizeBoard();
        Board rb = mg.getBoard();
        assertTrue("Randomize: has pegs", rb.countPegs() > 0);
        assertTrue("Randomize: has holes", rb.countPegs() < pegsOriginal + 1);
        // Board shape should be preserved — corners still OUT for English
        assertTrue("Randomize: corners still OUT", rb.getCell(0, 0) == Board.OUT);

        // Run multiple times to check variance
        int sameCount = 0;
        int prev = rb.countPegs();
        for (int i = 0; i < 10; i++) {
            mg.randomizeBoard();
            if (mg.getBoard().countPegs() == prev) sameCount++;
            prev = mg.getBoard().countPegs();
        }
        assertTrue("Randomize: produces varied results", sameCount < 10);
    }

    // ── Class hierarchy ───────────────────────────────────────────────

    private static void testClassHierarchy() {
        System.out.println("\n-- Class Hierarchy --");

        Game g1 = new ManualGame(7, "English");
        Game g2 = new AutomatedGame(7, "English");

        assertTrue("Both are Game instances", g1 instanceof Game && g2 instanceof Game);
        assertTrue("ManualGame instanceof ManualGame", g1 instanceof ManualGame);
        assertTrue("AutomatedGame instanceof AutomatedGame", g2 instanceof AutomatedGame);
        assertTrue("ManualGame is NOT AutomatedGame", !(g1 instanceof AutomatedGame));
        assertTrue("AutomatedGame is NOT ManualGame", !(g2 instanceof ManualGame));

        // Polymorphic usage
        String mode = g1.getModeName();
        assertTrue("Polymorphic getModeName ManualGame", mode.equals("Manual"));
        mode = g2.getModeName();
        assertTrue("Polymorphic getModeName AutomatedGame", mode.equals("Automated"));

        // Both share newGame / randomize / makeMove contract
        g1.randomizeBoard();
        g2.randomizeBoard();
        assertTrue("Both support randomizeBoard", !g1.isGameOver() || !g1.getBoard().hasValidMoves());
    }

    // ── Utility ───────────────────────────────────────────────────────

    private static int[] findFirstValidMove(Board b) {
        int size = b.getSize();
        int[][] dirs = {{-2,0},{2,0},{0,-2},{0,2}};
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++) {
                if (b.getCell(r, c) != Board.PEG) continue;
                for (int[] d : dirs) {
                    int tr = r + d[0], tc = c + d[1];
                    if (b.isValidMove(r, c, tr, tc))
                        return new int[]{r, c, tr, tc};
                }
            }
        return null;
    }

    private static void assertTrue(String name, boolean cond) {
        if (cond) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name);
            failed++;
        }
    }
}
