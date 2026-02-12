package pegSolitare;

import java.awt.Frame;

import javax.swing.*;

// Main class
public class gui {
// Creates GUI
	public static JFrame createGUI() {
		 // Creating JFrame
	     JFrame frame = new JFrame();

	     // Creating GUI Elements and Setting Bounds
	     JButton button = new JButton("New Game");
	     button.setBounds(0, 0, 100, 30);
	     JCheckBox recordBox = new JCheckBox("Record Game");
	     recordBox.setBounds(0, 160, 150, 25);
	     JRadioButton board1 = new JRadioButton("English");
	     JRadioButton board2 = new JRadioButton("European");
	     JRadioButton board3 = new JRadioButton("Diamond");
	     JRadioButton board4 = new JRadioButton("Triangle");
	     JLabel boardTypes = new JLabel("Board Type");
	     
	     // Boards JPanel and Their Elements
	     JPanel boards = new JPanel();
	     boards.add(boardTypes);
	     boards.add(board1);
	     boards.add(board2);
	     boards.add(board3);
	     boards.add(board4);
	     boards.setBounds(0, 50, 100, 120);
	     boards.setLayout(new BoxLayout(boards, BoxLayout.Y_AXIS));

	     // Board Button Group
	     ButtonGroup boardGroup = new ButtonGroup();
	     boardGroup.add(board1);
	     boardGroup.add(board2);
	     boardGroup.add(board3);
	     boardGroup.add(board4);
	     
	     // JFrame Elements
	     frame.add(button);
	     frame.add(recordBox);
	     frame.add(boards);
	     
	     // JFrame Properties
	     frame.setExtendedState(Frame.MAXIMIZED_BOTH);
	     frame.setLayout(null);
	     frame.setVisible(true);
	return frame;
	}
 public static void main(String[] args)
 {
    createGUI();
 }
}
