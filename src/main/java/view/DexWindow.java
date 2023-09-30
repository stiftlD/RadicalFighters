package view;

import controller.DexHandler;
import data.StudyService.Tuple;
import utils.UpdateEvent;

import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.Flow.*;
import model.kanji.DexData;

public class DexWindow extends JPanel implements Subscriber<DexData>  {
    private JFrame parent;
    // TODO mb by using sth more versatile than a list like SoAs so we can reduce data traffic
    private JList<JPanel> kanjiRiderList;
    private DefaultListModel<JPanel> listModel;
    private KanjiInfoPanel kanjiInfoPanel;
    private DexHandler dexHandler;
    // TODO dexhandler should probably be subscriber instead of this class
    private Subscription subscription;
    private Map<Integer, Integer> riderIndexToKanjiIdMap;

    public DexWindow(JFrame parent) {
        this.parent = parent;
        initializeUI();
    }

    // TODO do something similar in battlewindow during refactor
    private void initializeUI() {
        riderIndexToKanjiIdMap = new HashMap<Integer,Integer>();

        // Set layout for this panel
        setLayout(new BorderLayout());

        // Create a DefaultListModel to store the Kanji entries currently on display
        listModel = new DefaultListModel<JPanel>();

        // Create a JList and set the model
        kanjiRiderList = new JList<JPanel>(listModel);
        kanjiRiderList.setCellRenderer(new KanjiCharacterAndProficiencyPanelListCellRenderer());
        // if a kanji's rider is selected in the list more details are displayed
        kanjiRiderList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Check if the selection has changed
                if (!e.getValueIsAdjusting()) {
                    int selectedIndex = kanjiRiderList.getSelectedIndex();
                    if (dexHandler != null) {
                        Integer id = riderIndexToKanjiIdMap.get(selectedIndex);
                        if (id != null) dexHandler.displayKanjiDexEntry(id.intValue());
                    }
                }
            }
        });

        // Set the visible row count to control how many entries are displayed at once
        kanjiRiderList.setVisibleRowCount(10);
        kanjiRiderList.setModel(listModel);

        // Create a JScrollPane to make the list scrollable
        JScrollPane scrollPane = new JScrollPane(kanjiRiderList);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add the scroll pane to the dex view
        add(scrollPane, BorderLayout.WEST);

        // Create a panel next to the kanji list that displays one selected kanji
        kanjiInfoPanel = new KanjiInfoPanel(parent);
        //kanjiInfoPanel.setBackground(new Color(0x3838A1));

        // Add the info panel to the dex view
        add(kanjiInfoPanel, BorderLayout.CENTER);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        System.out.println("Dexwindow subscribed");
        subscription.request(1);
    }

    @Override
    public void onNext(DexData item) {
        System.out.println("updating list");
        riderIndexToKanjiIdMap.clear();
        listModel.removeAllElements();
        // TODO dont use index, prepare a better hook so we get kanji id
        int index = 0;
         for (Tuple<String, Double> tuple : (List<Tuple<String, Double>>) item.getKanjiProfRanking()) {
             riderIndexToKanjiIdMap.put(index, item.getKanjiEntries().get(index).getId());
             JPanel listElementPanel = new JPanel();
             JLabel characterLabel = new JLabel();
             characterLabel.setText(tuple.getX());
             characterLabel.setFont(new Font("Gothic", Font.PLAIN , 20));
             JLabel proficiencyLabel = new JLabel();
             proficiencyLabel.setText(Double.toString(tuple.getY()));
             proficiencyLabel.setFont(new Font("Gothic", Font.PLAIN , 20));
             listElementPanel.add(characterLabel, BorderLayout.WEST);
             listElementPanel.add(proficiencyLabel, BorderLayout.EAST);
             listModel.addElement(listElementPanel);
             index++;
         }
         kanjiRiderList.setModel(listModel);
        System.out.println("upated size: " + listModel.getSize());
        if (subscription != null) subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Error in DexWindow");
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("DexUpdate completed");
    }

    private class KanjiCharacterAndProficiencyPanelListCellRenderer extends JPanel implements ListCellRenderer<JPanel> {

        public KanjiCharacterAndProficiencyPanelListCellRenderer() {
            setLayout(new BorderLayout()); // Ensure that the renderer uses BorderLayout.
            setOpaque(true); // Make sure the renderer is opaque.
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends JPanel> list, JPanel elementPanel, int index, boolean isSelected, boolean cellHasFocus) {
            // Your rendering logic for each JPanel goes here.
            // You can customize the appearance of the JPanel as needed.
            elementPanel = list.getModel().getElementAt(index);

            // For example, you can set the background color based on the selection state.
            if (isSelected) {
                elementPanel.setBackground(new Color(0xEFC044));
                elementPanel.setForeground(new Color(0xEFC044));
            } else {
                elementPanel.setBackground(list.getBackground());
                elementPanel.setForeground(list.getBackground());
            }

            // Add the JPanel to the renderer.
            // Ensure that the JPanel has the preferred size set.
            // TODO would be cool to have kanji proficiency visualised by a rim on the side, width ~ prof and see the transition
            if (elementPanel != null) {
                //elementPanel.setPreferredSize(new Dimension(200, 75));
            }
            removeAll();
            add(elementPanel, BorderLayout.CENTER);

            return this;
        }
    }

    private void initializeKanjiInfoPanel(JPanel infoPanel){}

    // dexhandler passes data for display to the window
    public void showKanjiEntry(String character, String on_reading, String kun_reading,
                               String meaning, int grade, int strokes,
                               double proficiency) {
        System.out.println(character + "\t" + on_reading + "\t" + meaning + "\t" + grade + "\t" + strokes + "\t" + proficiency);
        kanjiInfoPanel.setKanjiCharacter(character);
        kanjiInfoPanel.setMeaning(meaning);
        kanjiInfoPanel.setOnReading(on_reading);
        kanjiInfoPanel.setKunReading(kun_reading);
        kanjiInfoPanel.setProficiency(proficiency);

        parent.revalidate();
        parent.repaint();
    }

    public void setDexHandler(DexHandler dexHandler) {this.dexHandler = dexHandler;}

    private class KanjiInfoPanel extends JPanel {
        private JFrame frame;
        private JPanel kanjiCharacterPanel;
        private JPanel meaningPanel;
        private JPanel kunReadingPanel;
        private JPanel onReadingPanel;
        private JPanel proficiencyPanel;

        public KanjiInfoPanel(JFrame frame) {
            super();
            this.frame = frame;

            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(5, 5, 5, 5);

            // set up the panel which shows the kanji character
            //makes takes up top left corner
            this.kanjiCharacterPanel = new JPanel();
            kanjiCharacterPanel.setBackground(new Color(0x37AF37));
            kanjiCharacterPanel.setLayout(new BorderLayout());
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            constraints.gridheight = 2;
            layout.setConstraints(kanjiCharacterPanel, constraints);
            add(kanjiCharacterPanel);

            // set up the panel displaying one broad proficiency grade
            this.proficiencyPanel = new JPanel();
            proficiencyPanel.setBackground(new Color(0xDE435C));
            proficiencyPanel.setLayout(new BorderLayout());
            constraints.gridx = 2;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            constraints.gridheight = 1;
            layout.setConstraints(proficiencyPanel, constraints);
            add(proficiencyPanel);

            // set up the panel which displays the meanings of the kanji
            this.meaningPanel = new JPanel();
            meaningPanel.setBackground(new Color(0xD27B38));
            meaningPanel.setLayout(new BorderLayout());
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.gridwidth = 4;
            constraints.gridheight = 1;
            layout.setConstraints(meaningPanel, constraints);
            add(meaningPanel);

            // write onyomi and kunyomi next to the character, separately
            this.kunReadingPanel = new JPanel();
            kunReadingPanel.setBackground(new Color(0x37AFAF));
            kunReadingPanel.setLayout(new BorderLayout());
            constraints.gridx = 2;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            layout.setConstraints(kunReadingPanel, constraints);
            add(kunReadingPanel);

            this.onReadingPanel = new JPanel();
            onReadingPanel.setBackground(new Color(0xEEDC37));
            onReadingPanel.setLayout(new BorderLayout());
            constraints.gridx = 3;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            layout.setConstraints(onReadingPanel, constraints);
            add(onReadingPanel);

            frame.revalidate();

        }

        public void setKanjiCharacter(String character) {
            kanjiCharacterPanel.removeAll();
            JLabel characterLabel = new JLabel(character, SwingConstants.CENTER);
            characterLabel.setFont(new Font("Gothic", Font.PLAIN, 100));
            kanjiCharacterPanel.add(characterLabel, BorderLayout.CENTER);
            frame.revalidate();
        }

        // TODO here we can have some cool way of reporting multidimensional kanji proficiency graphically
        public void setProficiency(double proficiency) {
            proficiencyPanel.removeAll();
            JLabel proficiencyLabel = new JLabel(Double.toString(proficiency), SwingConstants.CENTER);
            proficiencyLabel.setLayout(new BorderLayout());
            proficiencyLabel.setFont(new Font("Gothic", Font.PLAIN, 30));
            proficiencyPanel.add(proficiencyLabel, BorderLayout.CENTER);
            frame.revalidate();
        }

        // try to make this a scrollable list or sth to rotate through
        public void setMeaning(String meaning) {
            meaningPanel.removeAll();
            JLabel meaningLabel = new JLabel(meaning, SwingConstants.CENTER);
            meaningLabel.setLayout(new BorderLayout());
            meaningLabel.setFont(new Font("Gothic", Font.PLAIN, 30));
            meaningPanel.add(meaningLabel, BorderLayout.CENTER);
            frame.revalidate();
        }

        public void setKunReading(String reading) {
            kunReadingPanel.removeAll();
            JLabel kunReadingLabel = new JLabel(reading, SwingConstants.CENTER);
            kunReadingLabel.setLayout(new BorderLayout());
            kunReadingLabel.setFont(new Font("Gothic", Font.PLAIN, 30));
            kunReadingPanel.add(kunReadingLabel, BorderLayout.CENTER);
            frame.revalidate();
        }

        public void setOnReading(String reading) {
            onReadingPanel.removeAll();
            JLabel onReadingLabel = new JLabel(reading, SwingConstants.CENTER);
            onReadingLabel.setLayout(new BorderLayout());
            onReadingLabel.setFont(new Font("Gothic", Font.PLAIN, 30));
            onReadingPanel.add(onReadingLabel, BorderLayout.CENTER);
            frame.revalidate();
        }

    }
}
