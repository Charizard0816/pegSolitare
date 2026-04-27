package pegSolitare;

/**
 * Factory for creating Game instances by mode name.
 * Centralises the logic for selecting ManualGame vs AutomatedGame,
 * removing the need for string comparisons scattered across the GUI.
 */
public class GameFactory {

    /**
     * Creates a new Game of the given mode.
     * @param mode  "Manual" or "Automated" (case-sensitive)
     * @param size  board size (odd number)
     * @param type  board type ("English", "Hexagon", or "Diamond")
     * @return a fresh ManualGame or AutomatedGame
     * @throws IllegalArgumentException for unrecognised mode strings
     */
    public static Game create(String mode, int size, String type) {
        switch (mode) {
            case "Manual":    return new ManualGame(size, type);
            case "Automated": return new AutomatedGame(size, type);
            default: throw new IllegalArgumentException("Unknown game mode: " + mode);
        }
    }
}
