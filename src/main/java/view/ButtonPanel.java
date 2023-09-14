package view;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonPanel extends JPanel {
    private List<JButton> actionButtons;
    private JFrame parent;

    public ButtonPanel(JFrame parent) {
        super();
        actionButtons = new ArrayList<JButton>();
        this.parent = parent;

        setLayout(new FlowLayout()); // Adjust layout as needed.
    }

    public void addButton(JButton button) {
        add(button);
        actionButtons.add(button);
        parent.revalidate();
        parent.repaint();
    }

    public void removeButton(JButton button) {
        actionButtons.remove(button);
        parent.revalidate();
        parent.repaint();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        actionButtons.clear();
        parent.revalidate();
        parent.repaint();
    }
}

