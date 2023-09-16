package model.kanji;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Flow.*;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

import controller.Controller;
import utils.UpdateEvent;

public class KanjiDex implements Publisher<DexUpdateEvent> {
    private Controller controller;
    private List<Kanji> rankedKanjiList;
    private List<Subscriber<? super DexUpdateEvent>> subscribers;

    public KanjiDex(Controller controller) {
        rankedKanjiList = new ArrayList<Kanji>();
        subscribers = new ArrayList<Subscriber<? super DexUpdateEvent>>();
    }

    public void setRankedKanjiList(List<Kanji> rankedKanjiList) {
        this.rankedKanjiList = rankedKanjiList;
        updateKanjiListAndNotify(rankedKanjiList);
    }

    public void printRankedKanjiList() {
        System.out.println("Studied Kanji ranked by proficiency (total " + rankedKanjiList.size() + "): ");
        rankedKanjiList.stream().forEach(k -> {
            System.out.print(k.getCharacter() + " ");
        });
        System.out.print("\n");
    }

    public List<Kanji> getRankedKanjiList() { return this.rankedKanjiList; }

    @Override
    public void subscribe(Subscriber<? super DexUpdateEvent> subscriber) {

        subscribers.add(subscriber);

    }

    // Add a method to update the Kanji list and deliver the update event.
    public void updateKanjiListAndNotify(List<Kanji> newKanjiEntries) {
        List<String> eventData = rankedKanjiList.stream().map(k -> k.getCharacter()).collect(Collectors.toList());
        DexUpdateEvent updateEvent = new DexUpdateEvent(eventData);

        // Deliver the update event to subscribers.
        publish(updateEvent);
    }

    public void publish(DexUpdateEvent event) {
        subscribers.forEach(subscriber -> subscriber.onNext(event));
    }

}
// TODO write some layer in controler model to access and prepare all data the view needs
// TODO replace list with a smart class containing data references
class DexUpdateEvent implements UpdateEvent<List<String>> {
    private ArrayList<String> kanjiEntries;
    // Add other fields and references as needed.

    public DexUpdateEvent(List<String> kanjiEntries) {
        this.kanjiEntries = (ArrayList<String>) kanjiEntries;
        // Initialize other fields and references as needed.
    }

    public void setKanjiEntries(List<String> kanjiEntries) {
        this.kanjiEntries = (ArrayList<String>) kanjiEntries;
    }

    @Override
    public List<String> getData() {
        return kanjiEntries;
    }

    // Add getters and setters for other fields and references.
}


