package pegSolitare;

import java.io.*;
import java.util.*;

/**
 * Reads a recording produced by GameRecorder and replays it one event
 * at a time.  The GUI calls nextEvent() on a timer to animate the replay.
 */
public class GameReplayer {

    /** Types of replay event returned to the GUI. */
    public enum EventType { MOVE, RANDOMIZE, END, ERROR }

    public static class ReplayEvent {
        public final EventType type;
        // For MOVE:
        public final int fromR, fromC, toR, toC;
        // For RANDOMIZE: the restored board grid
        public final int[][] grid;
        // Header info (populated once after load())
        public String boardType;
        public int    boardSize;
        public String gameMode;

        ReplayEvent(EventType t) {
            this.type = t; fromR = fromC = toR = toC = -1; grid = null;
        }
        ReplayEvent(int fr, int fc, int tr, int tc) {
            this.type = EventType.MOVE;
            fromR = fr; fromC = fc; toR = tr; toC = tc; grid = null;
        }
        ReplayEvent(int[][] grid) {
            this.type = EventType.RANDOMIZE;
            fromR = fromC = toR = toC = -1;
            this.grid = grid;
        }
    }

    private List<ReplayEvent> events = new ArrayList<>();
    private int               cursor = 0;

    // Header fields exposed after load
    private String boardType;
    private int    boardSize;
    private String gameMode;

    // ── Load ──────────────────────────────────────────────────────────

    /**
     * Parses the recording file.
     * @return error message, or null on success.
     */
    public String load(File file) {
        events.clear();
        cursor = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerFound = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                switch (parts[0]) {
                    case "GAME":
                        if (parts.length < 4) return "Corrupt GAME header.";
                        boardType   = parts[1];
                        boardSize   = Integer.parseInt(parts[2]);
                        gameMode    = parts[3];
                        headerFound = true;
                        break;

                    case "MOVE":
                        if (parts.length < 5) return "Corrupt MOVE line.";
                        events.add(new ReplayEvent(
                            Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]), Integer.parseInt(parts[4])));
                        break;

                    case "RAND":
                        // Next line must be STATE <size>
                        String stateLine = br.readLine();
                        if (stateLine == null) return "Missing STATE after RAND.";
                        String[] sp = stateLine.trim().split("\\s+");
                        if (!sp[0].equals("STATE")) return "Expected STATE after RAND.";
                        int sz = Integer.parseInt(sp[1]);
                        int[][] grid = new int[sz][sz];
                        for (int r = 0; r < sz; r++) {
                            String row = br.readLine();
                            if (row == null) return "Truncated STATE block.";
                            String[] cells = row.trim().split("\\s+");
                            for (int c = 0; c < sz; c++)
                                grid[r][c] = Integer.parseInt(cells[c]);
                        }
                        events.add(new ReplayEvent(grid));
                        break;

                    case "END":
                        // Done
                        break;

                    default:
                        // Unknown line — skip silently
                        break;
                }
            }
            if (!headerFound) return "No GAME header found in file.";
        } catch (IOException e) {
            return "IO error: " + e.getMessage();
        } catch (NumberFormatException e) {
            return "Parse error: " + e.getMessage();
        }
        return null; // success
    }

    // ── Playback ──────────────────────────────────────────────────────

    public boolean hasMore()   { return cursor < events.size(); }
    public int     totalEvents() { return events.size(); }
    public int     currentIndex() { return cursor; }

    /** Returns the next event and advances the cursor. */
    public ReplayEvent nextEvent() {
        if (!hasMore()) return new ReplayEvent(EventType.END);
        return events.get(cursor++);
    }

    public void reset() { cursor = 0; }

    // ── Header accessors ──────────────────────────────────────────────

    public String getBoardType() { return boardType; }
    public int    getBoardSize() { return boardSize; }
    public String getGameMode()  { return gameMode; }
}
