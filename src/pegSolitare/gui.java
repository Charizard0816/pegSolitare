package pegSolitare;

import javax.swing.*;

// Main class
public class gui {

 // Main driver method
 public static void main(String[] args)
 {
     // Creating instance of JFrame
     JFrame frame = new JFrame();

     // Creating instances of JElements
     JButton button = new JButton("Button");
     JCheckBox checkBox = new JCheckBox("Checkbox");
     JRadioButton radio1 = new JRadioButton("Radio1");
     JRadioButton radio2 = new JRadioButton("Radio2");

     // Make a JPanel and add all JElements to it
     JPanel panel = new JPanel();
     panel.add(button);
     panel.add(checkBox);
     panel.add(radio1);
     panel.add(radio2);
     
     panel.setBounds(150, 200, 220, 100);
     // Button Group
     ButtonGroup radioGroup = new ButtonGroup();
     radioGroup.add(radio1);
     radioGroup.add(radio2);
     
     // JFrame JElements
     frame.add(panel);
     // JFrame properties
     frame.setSize(500, 600);
     frame.setLayout(null);
     frame.setVisible(true);
 }
}
