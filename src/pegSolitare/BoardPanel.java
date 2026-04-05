package pegSolitare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * BoardPanel draws the peg solitaire board and handles mouse clicks.
 * Works for both Manual and Automated game modes.
 */
public class BoardPanel extends JPanel implements MouseListener {

    private Game game;

    // Selection state (manual mode)
    private int selectedRow = -1;
    private int selectedCol = -1;

    // Last-move highlight
    private int lastFromR = -1, lastFromC = -1;
    private int lastToR   = -1, lastToC   = -1;

    // Colors
    private static final Color COLOR_PEG        = new Color(30,  70, 160);
    private static final Color COLOR_PEG_SEL    = new Color(220, 60,  30);
    private static final Color COLOR_PEG_LAST   = new Color(60, 180,  80);
    private static final Color COLOR_HOLE        = new Color(190, 190, 195);
    private static final Color COLOR_VALID_DEST  = new Color(80, 200, 100);
    private static final Color COLOR_OUT         = new Color(240, 240, 243);
    private static final Color COLOR_GRID        = new Color(150, 150, 155);
    private static final Color COLOR_BG          = new Color(250, 250, 252);

    private MoveListener moveListener;

    public BoardPanel(MoveListener moveListener) {
        this.moveListener = moveListener;
        setBackground(COLOR_BG);
        addMouseListener(this);
    }

    public void setGame(Game game) {
        this.game = game;
        clearSelection();
        clearLastMove();
    }

    public void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    public void setLastMove(int fr, int fc, int tr, int tc) {
        lastFromR = fr; lastFromC = fc;
        lastToR   = tr; lastToC   = tc;
    }

    public void clearLastMove() {
        lastFromR = lastFromC = lastToR = lastToC = -1;
    }

    // ── Painting ──────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (game == null) return;

        Board board = game.getBoard();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int size     = board.getSize();
        int cellSize = Math.min(getWidth(), getHeight()) / (size + 1);
        int pegR     = cellSize * 2 / 5;
        int offsetX  = (getWidth()  - size * cellSize) / 2;
        int offsetY  = (getHeight() - size * cellSize) / 2;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int cell = board.getCell(r, c);
                int cx   = offsetX + c * cellSize + cellSize / 2;
                int cy   = offsetY + r * cellSize + cellSize / 2;

                if (cell == Board.OUT) continue;

                // Cell background
                g2.setColor(COLOR_OUT);
                g2.fillRoundRect(offsetX + c * cellSize + 1, offsetY + r * cellSize + 1,
                                 cellSize - 2, cellSize - 2, 6, 6);
                g2.setColor(COLOR_GRID);
                g2.drawRoundRect(offsetX + c * cellSize + 1, offsetY + r * cellSize + 1,
                                 cellSize - 2, cellSize - 2, 6, 6);

                if (cell == Board.HOLE) {
                    if (selectedRow >= 0 && board.isValidMove(selectedRow, selectedCol, r, c)) {
                        // Valid destination highlight
                        g2.setColor(COLOR_VALID_DEST);
                        g2.fillOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                        g2.setColor(COLOR_VALID_DEST.darker());
                        g2.drawOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                    } else {
                        g2.setColor(COLOR_HOLE);
                        g2.fillOval(cx - pegR / 2, cy - pegR / 2, pegR, pegR);
                    }
                } else { // PEG
                    boolean isSelected = (r == selectedRow && c == selectedCol);
                    boolean isLastDest = (r == lastToR   && c == lastToC);
                    Color pegColor = isSelected ? COLOR_PEG_SEL
                                   : isLastDest ? COLOR_PEG_LAST
                                   : COLOR_PEG;

                    // Shadow
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fillOval(cx - pegR + 2, cy - pegR + 3, pegR * 2, pegR * 2);

                    g2.setColor(pegColor);
                    g2.fillOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);
                    g2.setColor(pegColor.darker());
                    g2.drawOval(cx - pegR, cy - pegR, pegR * 2, pegR * 2);

                    // Highlight glint
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillOval(cx - pegR / 2, cy - pegR + 2, pegR / 2, pegR / 3);
                }
            }
        }
    }

    // ── Mouse interaction (Manual mode only) ─────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        if (game == null || game.isGameOver()) return;
        if (!(game instanceof ManualGame)) return;

        Board board    = game.getBoard();
        int size       = board.getSize();
        int cellSize   = Math.min(getWidth(), getHeight()) / (size + 1);
        int offsetX    = (getWidth()  - size * cellSize) / 2;
        int offsetY    = (getHeight() - size * cellSize) / 2;

        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;

        if (!board.inBounds(row, col)) return;
        if (board.getCell(row, col) == Board.OUT) return;

        if (selectedRow < 0) {
            if (board.getCell(row, col) == Board.PEG) {
                selectedRow = row; selectedCol = col;
            }
        } else {
            if (row == selectedRow && col == selectedCol) {
                clearSelection();
            } else if (board.getCell(row, col) == Board.PEG) {
                selectedRow = row; selectedCol = col;
            } else {
                int fr = selectedRow, fc = selectedCol;
                clearSelection();
                if (game.makeMove(fr, fc, row, col)) {
                    setLastMove(fr, fc, row, col);
                    moveListener.onMoveCompleted();
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
