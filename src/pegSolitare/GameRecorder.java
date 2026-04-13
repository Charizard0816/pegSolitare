package pegSolitare;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Records a peg solitaire game to a plain-text file.
 *
 * File format:
 *   # Peg Solitaire recording
 *   # <timestamp>
 *   GAME  <boardType> <boardSize> <gameMode>
 *   MOVE  <fromR> <fromC> <toR> <toC>
 *   RAND                              <- randomize event; board state follows
 *   STATE <size>                      <- dumps the full grid row by row
 *   <row of space-separated ints>
 *   ...
 *   END
 *
 * RAND records the exact board state after randomizing so replay can
 * reproduce it perfectly without re-running the random walk.
 */
public class GameRecorder {

    private PrintWriter writer;
    private boolean     active;

    // ── Open / close ──────────────────────────────────────────────────

    /**
     * Opens a new recording file.
     * @param file  destination file
     * @param type  board type string (English / Hexagon / Diamond)
     * @param size  board size
     * @param mode  game mode string (Manual / Automated)
     */
    public void start(File file, String type, int size, String mode) throws IOException {
        close(); // close any previous recording
        writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        writer.println("# Peg Solitaire recording");
        writer.println("# " + LocalDateTime.now()
                               .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.println("GAME " + type + " " + size + " " + mode);
        writer.flush();
        active = true;
    }

    /** Finishes the recording and closes the file. */
    public void close() {
        if (writer != null) {
            if (active) writer.println("END");
            writer.flush();
            writer.close();
            writer = null;
        }
        active = false;
    }

    public boolean isActive() { return active; }

    // ── Event recording ───────────────────────────────────────────────

    /** Record a single jump move. */
    public void recordMove(int fromR, int fromC, int toR, int toC) {
        if (!active) return;
        writer.println("MOVE " + fromR + " " + fromC + " " + toR + " " + toC);
        writer.flush();
    }

    /**
     * Record a randomize event by dumping the full board state.
     * This lets replay reproduce the exact post-randomize layout.
     */
    public void recordRandomize(Board board) {
        if (!active) return;
        writer.println("RAND");
        writeState(board);
        writer.flush();
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private void writeState(Board board) {
        int size = board.getSize();
        writer.println("STATE " + size);
        for (int r = 0; r < size; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < size; c++) {
                if (c > 0) sb.append(' ');
                sb.append(board.getCell(r, c));
            }
            writer.println(sb.toString());
        }
    }
}
