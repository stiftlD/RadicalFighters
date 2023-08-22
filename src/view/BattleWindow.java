package view;

import controller.KanjiBattle;
import utils.UpdateEvent;

import model.radicals.RadicalFighter; //TODO only pass the hp etc values from kanjibattle

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.util.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class BattleWindow extends JDialog implements Subscriber<UpdateEvent>, KeyListener {

    private Subscription subscription;

    private JLabel opponentSpriteLabel;
    private JLabel opponentNameLabel;
    private JLabel opponentHealthLabel;
    private JLabel playerSpriteLabel;
    private JLabel playerNameLabel;
    private JLabel playerHealthLabel;
    private JLabel outputLabel;
    private JPanel bottomPanel;

    private char userInput = '\0';
    private JFrame parent;

    private String selectedOption;

    public BattleWindow(JFrame parent) {
        super(parent, "Battle Window", Dialog.ModalityType.MODELESS);
        this.parent = parent;

        setTitle("Battle Window");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        // Add the KeyListener to the JFrame
        addKeyListener(this);

        // Set focus to the JFrame to ensure it receives keyboard events
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Create layout manager and constraints
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);

        // Load the opponent's sprite image
        Image opponentSprite = null;
        try {
            opponentSprite = ImageIO.read(new File("C:\\Users\\david\\projects\\RadicalFighters\\src\\images\\devil.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image scaledOpponentSprite = opponentSprite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        opponentSprite = new BufferedImage(scaledOpponentSprite.getWidth(null), scaledOpponentSprite.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ((BufferedImage) opponentSprite).createGraphics();
        g.drawImage(scaledOpponentSprite, 0, 0, null);
        g.dispose();
        // Create the opponent's sprite label
        opponentSpriteLabel = new JLabel(new ImageIcon(scaledOpponentSprite));
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 2;
        layout.setConstraints(opponentSpriteLabel, constraints);
        add(opponentSpriteLabel);

        // Add opponent's name and health
        opponentNameLabel = new JLabel("Opponent");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        layout.setConstraints(opponentNameLabel, constraints);
        add(opponentNameLabel);
        opponentHealthLabel = new JLabel("Health: 100");
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        layout.setConstraints(opponentHealthLabel, constraints);
        add(opponentHealthLabel);

        // Load the opponent's sprite image
        Image playerSprite = null;
        try {
            playerSprite = ImageIO.read(new File("C:\\Users\\david\\projects\\RadicalFighters\\src\\images\\penguin.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image scaledPlayerSprite = playerSprite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        opponentSprite = new BufferedImage(scaledPlayerSprite.getWidth(null), scaledPlayerSprite.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = ((BufferedImage) playerSprite).createGraphics();
        g2.drawImage(scaledPlayerSprite, 0, 0, null);
        g2.dispose();
        // Create the opponent's sprite label
        playerSpriteLabel = new JLabel(new ImageIcon(scaledPlayerSprite));
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 2;
        layout.setConstraints(playerSpriteLabel, constraints);
        add(playerSpriteLabel);

        // Add player's name and health
        playerNameLabel = new JLabel("Player");
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        layout.setConstraints(playerNameLabel, constraints);
        add(playerNameLabel);
        playerHealthLabel = new JLabel("Health: 100");
        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        layout.setConstraints(playerHealthLabel, constraints);
        add(playerHealthLabel);

        // Add output label
        bottomPanel = new JPanel();
        outputLabel = new JLabel("Output");
        bottomPanel.add(outputLabel, BorderLayout.CENTER);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 4;
        constraints.gridheight = 2;
        layout.setConstraints(bottomPanel, constraints);
        add(bottomPanel);
    }

    public void run() { EventQueue.invokeLater(() -> {
            try {
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public String choose1OutOf4(String[] choices, String message, String title) {
        if (choices.length != 4) return null;

        String choice = (String) JOptionPane.showInputDialog(
                this,
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (choice != null) {
            writeToOutput("You chose " + choice);
        } else {
            writeToOutput("No choice was made.");
        }

        return choice;
    }

    private void handleOptionChosen(String option) {
        selectedOption = option;
        synchronized (this) {
            notify(); // Notify waiting thread
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        userInput = e.getKeyChar();
        System.out.println("User input: " + userInput);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // This method is called when a key is pressed down
        int keyCode = e.getKeyCode();
        System.out.println("Key pressed: " + keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // This method is called when a key is released after being pressed
        int keyCode = e.getKeyCode();
        System.out.println("Key released: " + keyCode);
    }

    public char getUserInput() {
        return userInput;
    }

    // have the user press ENTER once in order to advance
    public void waitContinueCommand() {
        CountDownLatch latch = new CountDownLatch(1);

        // Create a separate thread to wait for Enter
        Thread enterThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            writeToOutput("Press Enter to continue...");

            scanner.nextLine(); // wait for newline
            latch.countDown();
            scanner.close();
        });

        enterThread.start();

        System.out.println("Thread waiting for Enter command has been started");

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writeToOutput("Continuing...");

        try {
            enterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /*@Override
    public void update(Observable o, Object arg) {
        if (o instanceof KanjiBattle) {
            KanjiBattle battle = (KanjiBattle) o;

            opponentHealthLabel.setText("Health: " + battle.getOpponentHealth());
            playerHealthLabel.setText("Health: " + battle.getPlayerHealth());

            // add more code here to update other parts of the UI as needed
        }
    }*/

    public void writeToOutput(String message) {
        SwingUtilities.invokeLater(() -> {
            outputLabel.setText(message);
            parent.repaint();
        });
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        System.out.println(this.subscription);
        this.subscription = subscription;
        System.out.println(this.subscription);
        subscription.request(1);
    }
    @Override
    public void onNext(UpdateEvent event) {
        System.out.println(this.subscription);
        model.radicals.RadicalFighter[] fighters = (model.radicals.RadicalFighter[]) event.getData();
        if (fighters == null) return;
        // ... update labels etc
        opponentHealthLabel.setText("Health: " + fighters[1].getHP());
        playerHealthLabel.setText("Health: " + fighters[0].getHP());


        if (subscription != null) subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        System.out.println("Error in subscriber");
    }

    @Override
    public void onComplete() {
        System.out.println("Subscriber completed");
    }

}
