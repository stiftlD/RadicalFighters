package view;

import data.StudyService.Tuple;
import utils.UpdateEvent;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Flow.*;
import model.kanji.DexData;

public class DexWindow extends JPanel implements Subscriber<DexData>  {
    // Add the necessary components and logic for displaying dex information.
    // You can include JLabels, JTextAreas, or any other components you need.
    // Implement the constructor and methods as per your requirements.
    private JFrame parent;
    private JTabbedPane tabbedPane;
    private JList<JPanel> kanjiRiderList;
    private DefaultListModel<JPanel> listModel;
    private Subscription subscription;

    public DexWindow(JFrame parent) {
        this.parent = parent;
        initializeUI();
    }

    // TODO do something similar in battlewindow during refacture
    private void initializeUI() {
        // Create a DefaultListModel to store the Kanji entries
        listModel = new DefaultListModel<JPanel>();

        // Create a JList and set the model
        kanjiRiderList = new JList<JPanel>(listModel);
        kanjiRiderList.setCellRenderer(new KanjiCharacterAndProficiencyPanelListCellRenderer());

        // Set the visible row count to control how many entries are displayed at once
        kanjiRiderList.setVisibleRowCount(10);
        kanjiRiderList.setModel(listModel);

        // Create a JScrollPane to make the list scrollable
        JScrollPane scrollPane = new JScrollPane(kanjiRiderList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Set layout for this panel
        setLayout(new BorderLayout());

        // Add the scroll pane to the panel
        add(scrollPane, BorderLayout.CENTER);
    }

    // You can add methods to update the list with Kanji data.
    /*public void updateKanjiList(List<String> kanjiEntries) {
        // Clear the current entries
        listModel.clear();

        // Add new Kanji entries
        for (String kanji : kanjiEntries) {
            listModel.addElement(kanji);
        }
    }*/

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        System.out.println("Dexwindow subscribed");
        subscription.request(1);
    }

    @Override
    public void onNext(DexData item) {
        System.out.println("updating list");
        listModel.removeAllElements();
         for (Tuple<String, Double> tuple : (List<Tuple<String, Double>>) item.getKanjiEntries()) {
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
        public Component getListCellRendererComponent(JList<? extends JPanel> list, JPanel value, int index, boolean isSelected, boolean cellHasFocus) {
            // Your rendering logic for each JPanel goes here.
            // You can customize the appearance of the JPanel as needed.
            value=list.getModel().getElementAt(index);

            // For example, you can set the background color based on the selection state.
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            // Add the JPanel to the renderer.
            // Ensure that the JPanel has the preferred size set.
            if (value != null) {
                value.setPreferredSize(new Dimension(200, 75));
            }
            removeAll();
            add(value, BorderLayout.WEST);

            return this;
        }
    }
}
