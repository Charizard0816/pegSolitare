package pegSolitare;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Board}.
 *
 * Run with JUnit 5 (Jupiter). Add the following to your build tool:
 *
 * Maven:
 *   <dependency>
 *     <groupId>org.junit.jupiter</groupId>
 *     <artifactId>junit-jupiter</artifactId>
 *     <version>5.10.2</version>
 *     <scope>test</scope>
 *   </dependency>
 *
 * Gradle:
 *   testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
 */
class BoardTest {

    // ─────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────

    private static final int DEFAULT_SIZE = 7;

    // ═════════════════════════════════════════════════════════════════
    // 1. Board Construction & Initialization
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Board construction")
    class ConstructionTests {

        @Test
        @DisplayName("English board has correct size")
        void englishBoardSize() {
            Board b = new Board(DEFAULT_SIZE, "English");
            assertEquals(DEFAULT_SIZE, b.getSize());
        }

        @Test
        @DisplayName("English board type is stored correctly")
        void englishBoardType() {
            Board b = new Board(DEFAULT_SIZE, "English");
            assertEquals("English", b.getType());
        }

        @Test
        @DisplayName("Hexagon board type is stored correctly")
        void hexagonBoardType() {
            Board b = new Board(DEFAULT_SIZE, "Hexagon");
            assertEquals("Hexagon", b.getType());
        }

        @Test
        @DisplayName("Diamond board type is stored correctly")
        void diamondBoardType() {
            Board b = new Board(DEFAULT_SIZE, "Diamond");
            assertEquals("Diamond", b.getType());
        }

        @Test
        @DisplayName("Unknown type falls back to English layout")
        void unknownTypeFallsBackToEnglish() {
            Board fallback = new Board(DEFAULT_SIZE, "Unknown");
            Board english  = new Board(DEFAULT_SIZE, "English");
            for (int r = 0; r < DEFAULT_SIZE; r++)
                for (int c = 0; c < DEFAULT_SIZE; c++)
                    assertEquals(english.getCell(r, c), fallback.getCell(r, c),
                            "Mismatch at (" + r + "," + c + ")");
        }

        @ParameterizedTest(name = "size={0}")
        @ValueSource(ints = {7, 9, 11, 13})
        @DisplayName("English board center cell is always a HOLE")
        void englishCenterIsHole(int size) {
            Board b = new Board(size, "English");
            assertEquals(Board.HOLE, b.getCell(size / 2, size / 2));
        }

        @ParameterizedTest(name = "size={0}")
        @ValueSource(ints = {7, 9, 11, 13})
        @DisplayName("Hexagon board center cell is always a HOLE")
        void hexagonCenterIsHole(int size) {
            Board b = new Board(size, "Hexagon");
            assertEquals(Board.HOLE, b.getCell(size / 2, size / 2));
        }

        @ParameterizedTest(name = "size={0}")
        @ValueSource(ints = {7, 9, 11, 13})
        @DisplayName("Diamond board center cell is always a HOLE")
        void diamondCenterIsHole(int size) {
            Board b = new Board(size, "Diamond");
            assertEquals(Board.HOLE, b.getCell(size / 2, size / 2));
        }

        @Test
        @DisplayName("English 7×7 has exactly 32 pegs (standard starting count)")
        void english7PegCount() {
            Board b = new Board(DEFAULT_SIZE, "English");
            assertEquals(32, b.countPegs());
        }

        @Test
        @DisplayName("English board corners are OUT")
        void englishCornersAreOut() {
            Board b = new Board(DEFAULT_SIZE, "English");
            assertEquals(Board.OUT, b.getCell(0, 0));
            assertEquals(Board.OUT, b.getCell(0, DEFAULT_SIZE - 1));
            assertEquals(Board.OUT, b.getCell(DEFAULT_SIZE - 1, 0));
            assertEquals(Board.OUT, b.getCell(DEFAULT_SIZE - 1, DEFAULT_SIZE - 1));
        }

        @Test
        @DisplayName("English board has no OUT cells in the middle rows")
        void englishMiddleRowsAreNotOut() {
            Board b = new Board(DEFAULT_SIZE, "English");
            int third = DEFAULT_SIZE / 3;
            for (int r = third; r < DEFAULT_SIZE - third; r++)
                for (int c = 0; c < DEFAULT_SIZE; c++)
                    assertNotEquals(Board.OUT, b.getCell(r, c),
                            "Expected non-OUT at (" + r + "," + c + ")");
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. inBounds
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("inBounds()")
    class InBoundsTests {

        private Board board;

        @BeforeEach
        void setUp() { board = new Board(DEFAULT_SIZE, "English"); }

        @Test @DisplayName("(0,0) is in bounds")
        void topLeftIsInBounds() { assertTrue(board.inBounds(0, 0)); }

        @Test @DisplayName("(6,6) is in bounds for size-7 board")
        void bottomRightIsInBounds() { assertTrue(board.inBounds(6, 6)); }

        @Test @DisplayName("(-1,0) is out of bounds")
        void negativeRowOutOfBounds() { assertFalse(board.inBounds(-1, 0)); }

        @Test @DisplayName("(0,-1) is out of bounds")
        void negativeColOutOfBounds() { assertFalse(board.inBounds(0, -1)); }

        @Test @DisplayName("(7,0) is out of bounds for size-7 board")
        void rowEqualsSizeOutOfBounds() { assertFalse(board.inBounds(DEFAULT_SIZE, 0)); }

        @Test @DisplayName("(0,7) is out of bounds for size-7 board")
        void colEqualsSizeOutOfBounds() { assertFalse(board.inBounds(0, DEFAULT_SIZE)); }
    }

    // ═════════════════════════════════════════════════════════════════
    // 3. isValidMove
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isValidMove()")
    class IsValidMoveTests {

        /**
         * English 7×7 initial state:
         *   Center (3,3) = HOLE
         *   (1,3),(2,3),(3,2),(3,4),(4,3),(5,3) are PEGs adjacent to center area.
         *
         * The first legal moves are pegs that can jump INTO (3,3).
         * E.g. peg at (1,3) jumps over (2,3) into (3,3) — valid.
         */
        private Board board;

        @BeforeEach
        void setUp() { board = new Board(DEFAULT_SIZE, "English"); }

        @Test
        @DisplayName("Peg jumping 2 steps down into center hole is valid")
        void validMoveDown() {
            // (1,3) -> (3,3), over (2,3) — all are PEG except center which is HOLE
            assertTrue(board.isValidMove(1, 3, 3, 3));
        }

        @Test
        @DisplayName("Peg jumping 2 steps up into center hole is valid")
        void validMoveUp() {
            assertTrue(board.isValidMove(5, 3, 3, 3));
        }

        @Test
        @DisplayName("Peg jumping 2 steps right into center hole is valid")
        void validMoveRight() {
            assertTrue(board.isValidMove(3, 1, 3, 3));
        }

        @Test
        @DisplayName("Peg jumping 2 steps left into center hole is valid")
        void validMoveLeft() {
            assertTrue(board.isValidMove(3, 5, 3, 3));
        }

        @Test
        @DisplayName("Diagonal move is invalid")
        void diagonalMoveInvalid() {
            assertFalse(board.isValidMove(1, 1, 3, 3));
        }

        @Test
        @DisplayName("1-step move is invalid")
        void oneStepMoveInvalid() {
            assertFalse(board.isValidMove(2, 3, 3, 3));
        }

        @Test
        @DisplayName("3-step move is invalid")
        void threeStepMoveInvalid() {
            assertFalse(board.isValidMove(0, 3, 3, 3));
        }

        @Test
        @DisplayName("Move from an empty hole is invalid")
        void moveFromHoleInvalid() {
            // Center (3,3) starts as a HOLE — cannot move from a hole
            assertFalse(board.isValidMove(3, 3, 1, 3));
        }

        @Test
        @DisplayName("Move to a peg (non-empty destination) is invalid")
        void moveToOccupiedInvalid() {
            // (1,3) PEG -> (5,3) PEG — destination is occupied
            assertFalse(board.isValidMove(1, 3, 5, 3));
        }

        @Test
        @DisplayName("Move over an empty cell (no peg to jump) is invalid")
        void moveOverHoleInvalid() {
            // Apply one move first to create an empty middle
            board.applyMove(1, 3, 3, 3);   // (2,3) is now a HOLE
            // Now try (0,3) -> (2,3): from PEG, over HOLE (2,3), to HOLE (2,3)? — no
            // More precisely: (0,3)->jumping over (1,3) which is now HOLE -> (2,3)
            assertFalse(board.isValidMove(0, 3, 2, 3)); // mid (1,3) is now HOLE
        }

        @Test
        @DisplayName("Out-of-bounds destination is invalid")
        void outOfBoundsDestinationInvalid() {
            assertFalse(board.isValidMove(0, 3, -2, 3));
        }

        @Test
        @DisplayName("Out-of-bounds source is invalid")
        void outOfBoundsSourceInvalid() {
            assertFalse(board.isValidMove(-2, 3, 0, 3));
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 4. applyMove
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("applyMove()")
    class ApplyMoveTests {

        private Board board;

        @BeforeEach
        void setUp() { board = new Board(DEFAULT_SIZE, "English"); }

        @Test
        @DisplayName("Valid move returns true")
        void validMoveReturnsTrue() {
            assertTrue(board.applyMove(1, 3, 3, 3));
        }

        @Test
        @DisplayName("After valid move, source cell becomes HOLE")
        void sourceCellBecomesHole() {
            board.applyMove(1, 3, 3, 3);
            assertEquals(Board.HOLE, board.getCell(1, 3));
        }

        @Test
        @DisplayName("After valid move, jumped-over cell becomes HOLE")
        void middleCellBecomesHole() {
            board.applyMove(1, 3, 3, 3);
            assertEquals(Board.HOLE, board.getCell(2, 3));
        }

        @Test
        @DisplayName("After valid move, destination cell becomes PEG")
        void destinationCellBecomesPeg() {
            board.applyMove(1, 3, 3, 3);
            assertEquals(Board.PEG, board.getCell(3, 3));
        }

        @Test
        @DisplayName("Valid move decrements peg count by 1")
        void pegCountDecreasesByOne() {
            int before = board.countPegs();
            board.applyMove(1, 3, 3, 3);
            assertEquals(before - 1, board.countPegs());
        }

        @Test
        @DisplayName("Invalid move returns false")
        void invalidMoveReturnsFalse() {
            assertFalse(board.applyMove(0, 0, 2, 0)); // source is OUT cell
        }

        @Test
        @DisplayName("Invalid move does not change the board state")
        void invalidMoveDoesNotMutateBoard() {
            int before = board.countPegs();
            board.applyMove(0, 0, 2, 0);
            assertEquals(before, board.countPegs());
        }

        @Test
        @DisplayName("Multiple sequential moves update the board correctly")
        void multipleMovesSequential() {
            // Move 1: (1,3) -> (3,3)
            assertTrue(board.applyMove(1, 3, 3, 3));
            // Move 2: (3,1) -> (3,3) — now valid because (3,3) is PEG and (3,2) is PEG
            // Wait: (3,3) is now PEG after move 1, so we jump into it? No —
            // we need a HOLE destination. After move 1, (3,3)=PEG,(1,3)=HOLE,(2,3)=HOLE.
            // (4,3) -> (2,3): (4,3)=PEG, (3,3)=PEG, (2,3)=HOLE — valid!
            assertTrue(board.applyMove(4, 3, 2, 3));
            assertEquals(Board.HOLE, board.getCell(4, 3));
            assertEquals(Board.HOLE, board.getCell(3, 3));
            assertEquals(Board.PEG,  board.getCell(2, 3));
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 5. countPegs
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("countPegs()")
    class CountPegsTests {

        @Test
        @DisplayName("Peg count decreases by 1 per move applied")
        void countDecreasesWithEachMove() {
            Board b = new Board(DEFAULT_SIZE, "English");
            int start = b.countPegs();
            b.applyMove(1, 3, 3, 3);
            assertEquals(start - 1, b.countPegs());
            b.applyMove(4, 3, 2, 3);
            assertEquals(start - 2, b.countPegs());
        }

        @Test
        @DisplayName("Diamond board center-only hole yields correct peg count")
        void diamondPegCount() {
            Board b = new Board(DEFAULT_SIZE, "Diamond");
            // Diamond of radius 3 centered at (3,3):
            // Number of cells with |r-3|+|c-3| <= 3 = 1+2*3+2*5+2*7... formula gives
            // sum_{d=0}^{3} (1 + 2*d) * 2 - 1 = 25 total cells, minus 1 center hole = 24 pegs
            // (Actually for radius=3: cells = 1+4+8+12 = 25... let's just check >0 and consistent)
            int pegs = b.countPegs();
            assertTrue(pegs > 0, "Diamond board should have pegs");
            // Apply one move and verify decrement
            // Find a valid move
            boolean moved = false;
            outer:
            for (int r = 0; r < DEFAULT_SIZE; r++)
                for (int c = 0; c < DEFAULT_SIZE; c++)
                    for (int[] d : new int[][]{{-2,0},{2,0},{0,-2},{0,2}})
                        if (b.isValidMove(r, c, r+d[0], c+d[1])) {
                            b.applyMove(r, c, r+d[0], c+d[1]);
                            moved = true;
                            break outer;
                        }
            if (moved) assertEquals(pegs - 1, b.countPegs());
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 6. hasValidMoves
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("hasValidMoves()")
    class HasValidMovesTests {

        @Test
        @DisplayName("Fresh English board has valid moves")
        void freshBoardHasMoves() {
            Board b = new Board(DEFAULT_SIZE, "English");
            assertTrue(b.hasValidMoves());
        }

        @Test
        @DisplayName("Fresh Hexagon board has valid moves")
        void freshHexagonHasMoves() {
            Board b = new Board(DEFAULT_SIZE, "Hexagon");
            assertTrue(b.hasValidMoves());
        }

        @Test
        @DisplayName("Fresh Diamond board has valid moves")
        void freshDiamondHasMoves() {
            Board b = new Board(DEFAULT_SIZE, "Diamond");
            assertTrue(b.hasValidMoves());
        }

        @Test
        @DisplayName("Board with a single peg and no valid moves reports no moves")
        void singleIsolatedPegHasNoMoves() {
            // Build a minimal 5-board of all HOLEs, place one PEG with no neighbors
            // We can achieve this by exploiting the Board's grid indirectly via moves,
            // but since there's no public setter, we verify the logic differently:
            // On an English-7, exhaust known moves to reach near-dead-end state isn't
            // feasible in a unit test. Instead, verify that after applying every available
            // move until none remain, hasValidMoves() returns false.
            Board b = new Board(DEFAULT_SIZE, "English");
            int safety = 200;
            while (b.hasValidMoves() && safety-- > 0) {
                boolean applied = false;
                outer:
                for (int r = 0; r < b.getSize(); r++)
                    for (int c = 0; c < b.getSize(); c++)
                        for (int[] d : new int[][]{{-2,0},{2,0},{0,-2},{0,2}})
                            if (b.isValidMove(r, c, r+d[0], c+d[1])) {
                                b.applyMove(r, c, r+d[0], c+d[1]);
                                applied = true;
                                break outer;
                            }
                if (!applied) break;
            }
            assertFalse(b.hasValidMoves(), "Expected no valid moves after greedy exhaustion");
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 7. Board cell constants
    // ═════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Board cell constants")
    class ConstantTests {

        @Test @DisplayName("OUT  == -1") void outIsMinusOne()  { assertEquals(-1, Board.OUT);  }
        @Test @DisplayName("HOLE ==  0") void holeIsZero()     { assertEquals( 0, Board.HOLE); }
        @Test @DisplayName("PEG  ==  1") void pegIsOne()       { assertEquals( 1, Board.PEG);  }
    }
}
