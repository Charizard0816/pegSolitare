package pegSolitare;

import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.Component;

import org.junit.jupiter.api.Test;

public class guiTest {

    @Test
    public void testFrameExists() {
        JFrame frame = gui.createGUI();

        assertNotNull(frame);
        assertTrue(frame.isVisible());

        boolean foundButton = false;
        boolean foundCheckBox = false;

        Component[] components = frame.getContentPane().getComponents();

        for (int i = 0; i < components.length; i++) {
            Component c = components[i];

            if (c instanceof JButton) {
                JButton button = (JButton) c;
                if ("New Game".equals(button.getText())) {
                    foundButton = true;
                }
            }

            if (c instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) c;
                if ("Record Game".equals(checkBox.getText())) {
                    foundCheckBox = true;
                }
            }
        }

        assertTrue(foundButton, "New Game button should exist");
        assertTrue(foundCheckBox, "Record Game checkbox should exist");
    }

    @Test
    public void testRadioButtons() {
        JFrame frame = gui.createGUI();

        JRadioButton[] radios = new JRadioButton[4];
        int index = 0;

        Component[] components = frame.getContentPane().getComponents();

        for (int i = 0; i < components.length; i++) {
            Component c = components[i];

            if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                Component[] innerComponents = panel.getComponents();

                for (int j = 0; j < innerComponents.length; j++) {
                    Component inner = innerComponents[j];

                    if (inner instanceof JRadioButton) {
                        radios[index++] = (JRadioButton) inner;
                    }
                }
            }
        }

        assertEquals(4, index, "There should be 4 radio buttons");

        // Test ButtonGroup exclusivity
        radios[0].setSelected(true);
        radios[1].setSelected(true);

        assertFalse(radios[0].isSelected());
        assertTrue(radios[1].isSelected());
    }
}
