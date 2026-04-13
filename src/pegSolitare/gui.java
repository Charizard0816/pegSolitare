package pegSolitare;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Main GUI for Peg Solitaire.
 *
 * Supports:
 *  - Game mode selection: Manual or Automated
 *  - Board type: English, Hexagon, Diamond
 *  - Board size spinner (odd numbers only)
 *  - New Game, Randomize Board
 *  - Automated step-through with a Swing Timer
 *  - Status bar and move counter
 */
public class gui {

    // ── State ─────────────────────────────────────────────────────────
    private static Game        currentGame;
    private static BoardPanel  boardPanel;
    private static JFrame      frame;

    // Controls
    private static JSpinner     sizeSpinner;
    private static JRadioButton rbEnglish, rbHexagon, rbDiamond;
    private static JRadioButton rbManual, rbAutomated;
    private static JLabel       statusLabel;
    private static JLabel       modeLabel;
    private static JButton      newGameBtn;
    private static JButton      randomizeBtn;
    private static JButton      stepBtn;      // step one move (automated)
    private static JButton      runBtn;       // run automatically
    private static JButton      stopBtn;      // stop auto-run

    // Automated timer
    private static Timer autoTimer;

    // Record / Replay
    private static JCheckBox   recordCheckBox;
    private static JButton     replayBtn;
    private static GameRecorder recorder    = new GameRecorder();
    private static GameReplayer replayer    = new GameReplayer();
    private static Timer        replayTimer;
    private static boolean      replayMode  = false;

    // ── GUI Creation ──────────────────────────────────────────────────

    public static JFrame createGUI() {
        frame = new JFrame("Peg Solitaire");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(920, 680);
        frame.setLayout(new BorderLayout(8, 8));
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        frame.add(buildControlPanel(), BorderLayout.WEST);
        boardPanel = new BoardPanel(() -> onMoveCompleted());
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        // Automated timer (fires every AUTO_DELAY_MS ms)
        autoTimer = new Timer(AutomatedGame.AUTO_DELAY_MS, e -> autoStep());
        autoTimer.setRepeats(true);

        // Replay timer (same speed as autoplay)
        replayTimer = new Timer(AutomatedGame.AUTO_DELAY_MS, e -> replayStep());
        replayTimer.setRepeats(true);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        startNewGame();
        return frame;
    }

    // ── Control panel ─────────────────────────────────────────────────

    private static JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 14));
        p.setPreferredSize(new Dimension(170, 0));

        // ── Game Mode ──
        p.add(sectionLabel("Game Mode"));
        rbManual    = radio("Manual",    true);
        rbAutomated = radio("Automated", false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(rbManual);
        modeGroup.add(rbAutomated);
        p.add(rbManual);
        p.add(rbAutomated);
        p.add(gap(14));

        // ── Board Type ──
        p.add(sectionLabel("Board Type"));
        rbEnglish = radio("English", true);
        rbHexagon = radio("Hexagon", false);
        rbDiamond = radio("Diamond", false);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(rbEnglish);
        typeGroup.add(rbHexagon);
        typeGroup.add(rbDiamond);
        p.add(rbEnglish);
        p.add(rbHexagon);
        p.add(rbDiamond);
        p.add(gap(14));

        // ── Board Size ──
        p.add(sectionLabel("Board Size"));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 5, 99, 2));
        sizeSpinner.addChangeListener(e -> {
            int v = (int) sizeSpinner.getValue();
            if (v % 2 == 0) sizeSpinner.setValue(v + 1);
        });
        sizeSpinner.setMaximumSize(new Dimension(80, 28));
        sizeSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sizeSpinner);
        p.add(gap(18));

        // ── Buttons ──
        newGameBtn   = button("New Game",      e -> startNewGame());
        randomizeBtn = button("Randomize",     e -> randomizeBoard());
        stepBtn      = button("Step",          e -> manualStep());
        runBtn       = button("Run Auto",      e -> startAutoRun());
        stopBtn      = button("Stop",          e -> stopAutoRun());

        stopBtn.setEnabled(false);
        updateAutomatedButtonsVisible(false);

        p.add(newGameBtn);
        p.add(gap(6));
        p.add(randomizeBtn);
        p.add(gap(14));
        p.add(stepBtn);
        p.add(gap(6));
        p.add(runBtn);
        p.add(gap(6));
        p.add(stopBtn);
        p.add(gap(14));

        // ── Record / Replay ──
        recordCheckBox = new JCheckBox("Record game", false);
        recordCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        recordCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(recordCheckBox);
        p.add(gap(6));

        replayBtn = button("Replay", e -> startReplay());
        p.add(replayBtn);
        p.add(Box.createVerticalGlue());

        // ── Current mode display ──
        modeLabel = new JLabel("Mode: Manual");
        modeLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        modeLabel.setForeground(new Color(100, 100, 120));
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(modeLabel);

        // Update automated controls visibility when mode changes
        rbManual.addActionListener(e    -> updateAutomatedButtonsVisible(false));
        rbAutomated.addActionListener(e -> updateAutomatedButtonsVisible(true));

        return p;
    }

    private static JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        statusLabel = new JLabel("Select options and click New Game.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bar.add(statusLabel, BorderLayout.CENTER);
        return bar;
    }

    // ── Game actions ──────────────────────────────────────────────────

    private static void startNewGame() {
        stopAutoRun();
        stopReplay();
        recorder.close(); // close any active recording

        int    size = (int) sizeSpinner.getValue();
        String type = selectedBoardType();
        boolean auto = rbAutomated.isSelected();

        currentGame = auto ? new AutomatedGame(size, type)
                           : new ManualGame(size, type);

        // Start recording if checkbox is ticked
        if (recordCheckBox.isSelected()) {
            try {
                File f = chooseRecordFile();
                if (f != null) {
                    recorder.start(f, type, size, currentGame.getModeName());
                    currentGame.setRecorder(recorder);
                } else {
                    recordCheckBox.setSelected(false);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Could not open record file:\n" + ex.getMessage(),
                    "Record Error", JOptionPane.ERROR_MESSAGE);
                recordCheckBox.setSelected(false);
            }
        }

        boardPanel.setGame(currentGame);
        boardPanel.clearLastMove();
        boardPanel.repaint();
        modeLabel.setText("Mode: " + currentGame.getModeName());
        updateStatus("New " + type + " game — " + currentGame.getModeName()
                     + " (size " + size + ")  |  Pegs: " + currentGame.getBoard().countPegs());
        updateButtonStates();
    }

    private static File chooseRecordFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Recording As");
        fc.setSelectedFile(new File("game_recording.txt"));
        int result = fc.showSaveDialog(frame);
        return (result == JFileChooser.APPROVE_OPTION) ? fc.getSelectedFile() : null;
    }

    private static void randomizeBoard() {
        if (currentGame == null) return;
        stopAutoRun();
        currentGame.randomizeBoard();
        boardPanel.clearLastMove();
        boardPanel.clearSelection();
        boardPanel.repaint();
        if (currentGame.isGameOver()) {
            onMoveCompleted(); // show game-over dialog if randomize left no moves
        } else {
            updateStatus("Board randomized.  |  " + currentGame.getStatusMessage());
            updateButtonStates();
        }
    }

    /** Perform one automated step manually (Step button). */
    private static void manualStep() {
        if (currentGame == null || !(currentGame instanceof AutomatedGame)) return;
        if (currentGame.isGameOver()) return;
        doAutoStep();
    }

    private static void startAutoRun() {
        if (currentGame == null || !(currentGame instanceof AutomatedGame)) return;
        runBtn.setEnabled(false);
        stepBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        autoTimer.start();
    }

    private static void stopAutoRun() {
        if (autoTimer != null) autoTimer.stop();
        if (stopBtn      != null) stopBtn.setEnabled(false);
        if (newGameBtn   != null) newGameBtn.setEnabled(true);
        if (randomizeBtn != null) randomizeBtn.setEnabled(true);
        boolean over = currentGame == null || currentGame.isGameOver();
        if (runBtn  != null) runBtn.setEnabled(currentGame instanceof AutomatedGame && !over);
        if (stepBtn != null) stepBtn.setEnabled(currentGame instanceof AutomatedGame && !over);
    }

    /** Timer callback: perform one step and stop if game over. */
    private static void autoStep() {
        doAutoStep();
        if (currentGame.isGameOver()) stopAutoRun();
    }

    private static void doAutoStep() {
        if (currentGame == null || currentGame.isGameOver()) return;
        int[] move = currentGame.getNextMove();
        if (move == null) {
            // No moves left — game is over; onMoveCompleted handles the UI update
            onMoveCompleted();
            return;
        }
        currentGame.makeMove(move[0], move[1], move[2], move[3]);
        boardPanel.setLastMove(move[0], move[1], move[2], move[3]);
        boardPanel.repaint();
        onMoveCompleted();
    }

    // ── Called by BoardPanel after every manual move ──────────────────

    public static void onMoveCompleted() {
        if (currentGame == null) return;
        updateStatus(currentGame.getStatusMessage());
        updateButtonStates();

        if (currentGame.isGameOver()) {
            stopAutoRun();
            recorder.close(); // finalise the recording file
            int pegs = currentGame.getBoard().countPegs();
            String msg = pegs == 1
                ? "Congratulations — you won with 1 peg left!"
                : "Game over. Pegs remaining: " + pegs;
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(frame, msg, "Game Over",
                    pegs == 1 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE));
        }
    }

    // ── Replay ───────────────────────────────────────────────────────

    private static void startReplay() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open Recording");
        fc.setSelectedFile(new File("game_recording.txt"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        String err = replayer.load(fc.getSelectedFile());
        if (err != null) {
            JOptionPane.showMessageDialog(frame, "Could not load recording:\n" + err,
                "Replay Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Set up a fresh game matching the recording's header
        stopAutoRun();
        recorder.close();
        replayMode = true;

        String type = replayer.getBoardType();
        int    size = replayer.getBoardSize();
        String mode = replayer.getGameMode();

        currentGame = mode.equals("Automated") ? new AutomatedGame(size, type)
                                               : new ManualGame(size, type);
        boardPanel.setGame(currentGame);
        boardPanel.clearLastMove();
        boardPanel.repaint();
        modeLabel.setText("Replay: " + mode);
        updateStatus("Replaying " + type + " " + mode + " game (size " + size + ")…");
        updateButtonStates();

        replayTimer.start();
    }

    private static void replayStep() {
        if (!replayer.hasMore()) {
            stopReplay();
            onMoveCompleted();
            return;
        }

        GameReplayer.ReplayEvent ev = replayer.nextEvent();

        switch (ev.type) {
            case MOVE:
                currentGame.makeMove(ev.fromR, ev.fromC, ev.toR, ev.toC);
                boardPanel.setLastMove(ev.fromR, ev.fromC, ev.toR, ev.toC);
                boardPanel.repaint();
                updateStatus(currentGame.getStatusMessage()
                    + "  [replay " + replayer.currentIndex()
                    + "/" + replayer.totalEvents() + "]");
                break;

            case RANDOMIZE:
                currentGame.getBoard().setGrid(ev.grid);
                currentGame.getBoard(); // force re-read
                boardPanel.clearLastMove();
                boardPanel.repaint();
                updateStatus("Replaying randomize…  [" + replayer.currentIndex()
                    + "/" + replayer.totalEvents() + "]");
                break;

            case END:
                stopReplay();
                onMoveCompleted();
                break;

            default:
                break;
        }

        if (currentGame.isGameOver()) {
            stopReplay();
            onMoveCompleted();
        }
    }

    private static void stopReplay() {
        if (replayTimer != null) replayTimer.stop();
        replayMode = false;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static String selectedBoardType() {
        return rbHexagon.isSelected() ? "Hexagon"
             : rbDiamond.isSelected() ? "Diamond"
             : "English";
    }

    private static void updateAutomatedButtonsVisible(boolean auto) {
        stepBtn.setVisible(auto);
        runBtn.setVisible(auto);
        stopBtn.setVisible(auto);
    }

    private static void updateButtonStates() {
        boolean auto    = currentGame instanceof AutomatedGame;
        boolean running = autoTimer != null && autoTimer.isRunning();
        boolean replaying = replayTimer != null && replayTimer.isRunning();
        boolean over    = currentGame == null || currentGame.isGameOver();
        boolean blocked = running || replaying;

        newGameBtn.setEnabled(!blocked);
        randomizeBtn.setEnabled(!blocked && !replayMode);
        recordCheckBox.setEnabled(!blocked && !replayMode);
        replayBtn.setEnabled(!blocked);

        stopBtn.setEnabled(running);
        stepBtn.setEnabled(auto && !over && !running && !replaying);
        runBtn.setEnabled(auto && !over && !running && !replaying);
    }

    private static void updateStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ── Widget helpers ────────────────────────────────────────────────

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JRadioButton radio(String label, boolean selected) {
        JRadioButton r = new JRadioButton(label, selected);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return r;
    }

    private static JButton button(String label, ActionListener al) {
        JButton b = new JButton(label);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(140, 30));
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.addActionListener(al);
        return b;
    }

    private static Component gap(int height) {
        return Box.createVerticalStrut(height);
    }

    // ── Entry point ───────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(gui::createGUI);
    }
}
