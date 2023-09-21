package model.kanji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.*;
import java.util.stream.Collectors;

import controller.Controller;
import data.StudyService.Tuple;

public class KanjiDex implements Publisher<DexData> {
    private Controller controller;
    private List<Kanji> rankedKanjiList;
    private List<Subscriber<? super DexData>> subscribers;
    // TODO maybe do sth with ListDataEvents

    public KanjiDex(Controller controller) {
        rankedKanjiList = new ArrayList<Kanji>();
        subscribers = new ArrayList<Subscriber<? super DexData>>();
    }

    public void setRankedKanjiList(List<Tuple<Kanji, Double>> rankedKanjiList) {
        this.rankedKanjiList = rankedKanjiList.stream().map(t -> t.getX()).collect(Collectors.toList());
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
    public void subscribe(Subscriber<? super DexData> subscriber) {

        subscribers.add(subscriber);

    }

    // Add a method to update the Kanji list and deliver the update event.
    public void updateKanjiListAndNotify(List<Tuple<Kanji, Double>> newKanjiEntries) {
        //setRankedKanjiList(newKanjiEntries.stream().map(t -> t.getX()).collect(Collectors.toList()));
        List<Tuple<String, Double>> eventData = newKanjiEntries.stream().map(t -> new Tuple<String, Double>(t.getX().getCharacter(), t.getY())).collect(Collectors.toList());
        DexData data = new DexData();
        data.setKanjiEntries(eventData);

        System.out.println("publishing " + eventData.size());
        // Deliver the update event to subscribers.
        publish(data);
    }

    public void publish(DexData data) {
        System.out.println("publishing to " + subscribers.size());
        subscribers.forEach(subscriber -> subscriber.onNext(data));
    }

}

