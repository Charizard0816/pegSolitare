package pegSolitare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * BoardPanel draws the peg solitaire board and handles mouse clicks
 * for selecting a peg and choosing a destination.
 */
public class BoardPanel extends JPanel implements MouseListener {

    private Board board;

    // Selection state
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Colors
    private static final Color COLOR_PEG        = new Color(40,  80, 160);
    private static final Color COLOR_PEG_SEL    = new Color(220, 80,  40);
    private static final Color COLOR_HOLE        = new Color(200, 200, 200);
    private static final Color COLOR_VALID_DEST  = new Color(100, 200, 100);
    private static final Color COLOR_OUT         = new Color(245, 245, 245);
    private static final Color COLOR_GRID        = new Color(160, 160, 160);

    public BoardPanel() {
        setBackground(Color.WHITE);
        addMouseListener(this);
    }

    public void setBoard(Board board) {
        this.board = board;
        selectedRow = -1;
        selectedCol = -1;
    }

    // ── Painting ──────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size    = board.getSize();
        int cellSize = Math.min(getWidth(), getHeight()) / (size + 1);
        int pegR    = cellSize * 2 / 5;

        int offsetX = (getWidth()  - size * cellSize) / 2;
        int offsetY = (getHeight() - size * cellSize) / 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int cell = board.getCell(r, c);
                int cx   = offsetX + c * cellSize + cellSize / 2;
                int cy   = offsetY + r * cellSize + cellSize / 2;

                if (cell == Board.OUT) continue;

                // Cell background square
                g2.setColor(COLOR_OUT);
                g2.fillRect(offsetX + c * cellSize, offsetY + r * cellSize, cellSize, cellSize);
                g2.setColor(COLOR_GRID);
                g2.drawRect(offsetX + c * cellSize, offsetY + r * cellSize, cellSize, cellSize);

                if (cell == Board.HOLE) {
                    // Highlight valid destination if a peg is selected
                    if (selectedRow >= 0 && board.isValidMove(selectedRow, selectedCol, r, c)) {
                        g2.setColor(COLOR_VALID_DEST);
                        g2.fillOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                        g2.setColor(COLOR_GRID);
                        g2.drawOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                    } else {
                        // Empty hole
                        g2.setColor(COLOR_HOLE);
                        g2.fillOval(cx - pegR / 2, cy - pegR / 2, pegR, pegR);
                    }
                } else { // PEG
                    boolean isSelected = (r == selectedRow && c == selectedCol);
                    g2.setColor(isSelected ? COLOR_PEG_SEL : COLOR_PEG);
                    g2.fillOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                    g2.setColor(isSelected ? COLOR_PEG_SEL.darker() : COLOR_PEG.darker());
                    g2.drawOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                }
            }
        }
    }

    // ── Mouse interaction ─────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        if (board == null) return;

        int size     = board.getSize();
        int cellSize = Math.min(getWidth(), getHeight()) / (size + 1);
        int offsetX  = (getWidth()  - size * cellSize) / 2;
        int offsetY  = (getHeight() - size * cellSize) / 2;

        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;

        if (!board.inBounds(row, col)) return;
        if (board.getCell(row, col) == Board.OUT) return;

        if (selectedRow < 0) {
            // First click — select a peg
            if (board.getCell(row, col) == Board.PEG) {
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            // Second click — attempt move
            if (row == selectedRow && col == selectedCol) {
                // Clicked same peg → deselect
                selectedRow = -1;
                selectedCol = -1;
            } else if (board.getCell(row, col) == Board.PEG) {
                // Clicked a different peg → switch selection
                selectedRow = row;
                selectedCol = col;
            } else {
                // Clicked a hole → try to move
                if (board.applyMove(selectedRow, selectedCol, row, col)) {
                    selectedRow = -1;
                    selectedCol = -1;
                    gui.onMoveCompleted();
                } else {
                    // Invalid move
                    selectedRow = -1;
                    selectedCol = -1;
                }
            }
        }
        repaint();
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
