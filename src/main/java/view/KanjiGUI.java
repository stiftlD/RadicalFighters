package view;

import controller.Controller;
import view.BattleWindow;

import javax.swing.*;
import java.awt.*;

public class KanjiGUI {
    private Controller controller;
    private JFrame frame;
    private JPanel taskPanel;
    private ButtonPanel buttonPanel;
    private JPanel topPanel;
    private JButton nextButton;
    private JButton exitButton;
    private JButton toggleDexButton;
    private boolean showingDex = false;

    private BattleWindow battleWindow;
    private DexWindow dexWindow;
    private boolean battling = false;

    // TODO this could be Runnable
    public KanjiGUI(Controller controller) {
        this.controller = controller;
        frame = new JFrame();
        frame.setSize(500, 500);
        frame.setTitle("Kanji Battle");

        // Create task and button panels
        // Create the top panel and add a BattleWindow to it
        topPanel = new JPanel(new BorderLayout());
        //battleWindow = new BattleWindow();
        //topPanel.add(battleWindow, BorderLayout.CENTER);

        buttonPanel = new ButtonPanel(frame);

        // Create and add buttons to button panel
        nextButton = new JButton("Next Battle");
        // Next button action
        nextButton.addActionListener(e -> {
            if (battling) {
                return;
            }
            battling = true;
            // Start battle
            showBattleWindow();
            battling = false;
        });
        buttonPanel.addButton(nextButton);

        // toggle Dex view on or world/battle window
        toggleDexButton = new JButton("Dex");
        toggleDexButton.addActionListener(e -> toggleDex());
        buttonPanel.addButton(toggleDexButton);

        exitButton = new JButton("Exit");
        // Exit button action
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.addButton(exitButton);

        // Add task and button panels to frame
        frame.add(topPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

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

    /*private void showBattleWindow() {
        SwingUtilities.invokeLater(() -> {
            // Create a new BattleWindow instance
            BattleWindow battleWindow = new BattleWindow(frame);
            battleWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            battleWindow.pack();

            // Display the BattleWindow as a modal dialog
            //battleWindow.setModalityType(Dialog.ModalityType.MODELESS);
            controller.startBattle(battleWindow);
            //battleWindow.setVisible(true);
        });
    }*/

    public void toggleDex() {
        if (!showingDex) {
            showingDex = !showingDex;
            showDexWindow();
        } else {
            showingDex = !showingDex;
            topPanel.remove(dexWindow);
            topPanel.add(battleWindow);
            frame.revalidate();
            frame.repaint();
        }
    }
    private void showDexWindow() {
        SwingUtilities.invokeLater(() -> {
            if (battleWindow != null) topPanel.remove(battleWindow);
            if (dexWindow == null) dexWindow = new DexWindow(frame); // Pass the parent frame.
            topPanel.add(dexWindow, BorderLayout.CENTER); // Add BattleWindow to the top panel.
            frame.revalidate(); // Refresh the layout.
            frame.repaint();
            //controller.startBattle(battleWindow);
        });
    }

    private void showBattleWindow() {
        SwingUtilities.invokeLater(() -> {
            battleWindow = new BattleWindow(frame); // Pass the parent frame.
            topPanel.add(battleWindow, BorderLayout.CENTER); // Add BattleWindow to the top panel.
            frame.revalidate(); // Refresh the layout.
            frame.repaint();
            controller.startBattle(battleWindow);
        });
    }

    public void closeBattleWindow() {
        if (battleWindow != null) {
            topPanel.remove(battleWindow);
            topPanel.revalidate();
            topPanel.repaint();
            battleWindow = null; // Set the reference to null to release resources.
        }
    }
}
