package view;

import utils.UpdateEvent;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Flow.*;

public class DexWindow extends JPanel implements Subscriber<UpdateEvent>  {
    // Add the necessary components and logic for displaying dex information.
    // You can include JLabels, JTextAreas, or any other components you need.
    // Implement the constructor and methods as per your requirements.
    private JFrame parent;
    private JTabbedPane tabbedPane;
    private JList<String> kanjiList;
    private DefaultListModel<String> listModel;
    private Subscription subscription;

    public DexWindow(JFrame parent) {
        this.parent = parent;
        initializeUI();
    }

    // TODO do something similar in battlewindow during refacture
    private void initializeUI() {
        // Create a DefaultListModel to store the Kanji entries
        listModel = new DefaultListModel<>();

        // Create a JList and set the model
        kanjiList = new JList<>(listModel);

        // Set the visible row count to control how many entries are displayed at once
        kanjiList.setVisibleRowCount(10);

        // Create a JScrollPane to make the list scrollable
        JScrollPane scrollPane = new JScrollPane(kanjiList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Set layout for this panel
        setLayout(new BorderLayout());

        // Add the scroll pane to the panel
        add(scrollPane, BorderLayout.CENTER);
    }

    // You can add methods to update the list with Kanji data.
    public void updateKanjiList(List<String> kanjiEntries) {
        // Clear the current entries
        listModel.clear();

        // Add new Kanji entries
        for (String kanji : kanjiEntries) {
            listModel.addElement(kanji);
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        System.out.println("Dexwindow subscribed");
        subscription.request(1);
    }

    @Override
    public void onNext(UpdateEvent item) {//kanjiList = new JList<String>((String[]) item.getData(), listModel);
        listModel.removeAllElements();
         for (String string : (List<String>) item.getData()) {
             listModel.addElement(string);
         }
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
}
