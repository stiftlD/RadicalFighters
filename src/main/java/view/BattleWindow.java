package view;

import controller.KanjiBattle;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.util.*;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class BattleWindow extends JFrame implements Observer {

    private JLabel opponentSpriteLabel;
    private JLabel opponentNameLabel;
    private JLabel opponentHealthLabel;
    private JLabel playerSpriteLabel;
    private JLabel playerNameLabel;
    private JLabel playerHealthLabel;
    private JLabel outputLabel;

    public BattleWindow() {


        setTitle("Battle Window");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

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
            opponentSprite = ImageIO.read(new File("C:\\Users\\David Stiftl\\git\\RadicalFighters\\RadicalFighters\\src\\images\\devil.png"));
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
            playerSprite = ImageIO.read(new File("C:\\Users\\David Stiftl\\git\\RadicalFighters\\RadicalFighters\\src\\images\\penguin.png"));
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
        outputLabel = new JLabel("Output");
    }

    public void run() {
        EventQueue.invokeLater(() -> {
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

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof KanjiBattle) {
            KanjiBattle battle = (KanjiBattle) o;

            opponentHealthLabel.setText("Health: " + battle.getOpponentHealth());
            playerHealthLabel.setText("Health: " + battle.getPlayerHealth());

            // add more code here to update other parts of the UI as needed
        }
    }

}
