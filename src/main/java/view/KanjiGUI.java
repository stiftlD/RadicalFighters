package view;

import javax.swing.*;
import java.awt.*;
import view.*;
import controller.*;

public class KanjiGUI {
    private JFrame frame;
    private JPanel taskPanel;
    private JPanel buttonPanel;
    private JButton nextButton;
    private JButton exitButton;
    private Controller controller;
    private boolean battling = false;

    public KanjiGUI(Controller controller) {
        this.controller = controller;
        frame = new JFrame();
        frame.setSize(500, 500);
        frame.setTitle("model.kanji.Kanji Battle");

        // Create task and button panels
        // Create the top panel and add a BattleWindow to it
        JPanel topPanel = new JPanel(new BorderLayout());
        //battleWindow = new BattleWindow();
        //topPanel.add(battleWindow, BorderLayout.CENTER);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // Create and add next and exit buttons to button panel
        nextButton = new JButton("Next Battle");
        exitButton = new JButton("Exit");
        buttonPanel.add(nextButton);
        buttonPanel.add(exitButton);

        // Add task and button panels to frame
        frame.add(topPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Exit button action
        exitButton.addActionListener(e -> System.exit(0));

        // Next button action
        nextButton.addActionListener(e -> {
            if (battling) {
                return;
            }
            battling = true;
            // Start battle
            BattleWindow battleWindow = new BattleWindow();
            controller.startBattle(battleWindow);
            battleWindow.run();
            battling = false;
        });

        // Show frame
        frame.setVisible(true);
    }

    public void run() {
        nextButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Next battle!");
        });

        exitButton.addActionListener(e -> {
            System.exit(0);
        });
    }
}
