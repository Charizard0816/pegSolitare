package pegSolitare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class gui {

    private static Board board;
    private static BoardPanel boardPanel;
    private static JFrame frame;
    private static JSpinner sizeSpinner;
    private static JRadioButton rbEnglish, rbHexagon, rbDiamond;
    private static JLabel statusLabel;

    public static JFrame createGUI() {
        frame = new JFrame("Peg Solitaire");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setLayout(new BorderLayout());

        // ── Left control panel ──────────────────────────────────────
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        controlPanel.setPreferredSize(new Dimension(160, 0));

        // Board size spinner
        JLabel sizeLabel = new JLabel("Board Size");
        sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 7, 99, 2));
        sizeSpinner.addChangeListener(e -> {
            int val = (int) sizeSpinner.getValue();
            if (val % 2 == 0) {
                sizeSpinner.setValue(val - 1); // snap to next odd
            }
        });
        sizeSpinner.setMaximumSize(new Dimension(80, 30));
        sizeSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Board type radio buttons
        JLabel typeLabel = new JLabel("Board Type");
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbEnglish  = new JRadioButton("English",  true);
        rbHexagon  = new JRadioButton("Hexagon",  false);
        rbDiamond  = new JRadioButton("Diamond",  false);
        rbEnglish.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbHexagon.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbDiamond.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(rbEnglish);
        group.add(rbHexagon);
        group.add(rbDiamond);

        // New Game button
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newGameBtn.addActionListener(e -> startNewGame());

        // Assemble control panel
        controlPanel.add(sizeLabel);
        controlPanel.add(Box.createVerticalStrut(4));
        controlPanel.add(sizeSpinner);
        controlPanel.add(Box.createVerticalStrut(16));
        controlPanel.add(typeLabel);
        controlPanel.add(Box.createVerticalStrut(4));
        controlPanel.add(rbEnglish);
        controlPanel.add(rbHexagon);
        controlPanel.add(rbDiamond);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(newGameBtn);

        // ── Status bar ───────────────────────────────────────────────
        statusLabel = new JLabel("Select options and click New Game.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // ── Board panel (center) ─────────────────────────────────────
        boardPanel = new BoardPanel();

        frame.add(controlPanel, BorderLayout.WEST);
        frame.add(boardPanel,   BorderLayout.CENTER);
        frame.add(statusLabel,  BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Start with a default English-7 board
        startNewGame();
        return frame;
    }

    // ── Start / reset game ────────────────────────────────────────────
    private static void startNewGame() {
        int size = (int) sizeSpinner.getValue();
        String type = rbEnglish.isSelected() ? "English"
                    : rbHexagon.isSelected()  ? "Hexagon"
                    : "Diamond";

        board = new Board(size, type);
        boardPanel.setBoard(board);
        boardPanel.repaint();
        statusLabel.setText("New " + type + " game started (size " + size + "). Pegs: " + board.countPegs());
    }

    // ── Called by BoardPanel after every move ─────────────────────────
    public static void onMoveCompleted() {
        int pegs = board.countPegs();
        if (pegs == 1) {
            statusLabel.setText("You win! Only 1 peg remaining!");
            JOptionPane.showMessageDialog(frame, "Congratulations! You won!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (!board.hasValidMoves()) {
            statusLabel.setText("No more moves. Game over. Pegs remaining: " + pegs);
            JOptionPane.showMessageDialog(frame, "No valid moves left.\nPegs remaining: " + pegs, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText("Pegs remaining: " + pegs);
        }
        boardPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(gui::createGUI);
    }
}
