package model.kanji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.*;
import java.util.stream.Collectors;

import controller.Controller;
import data.StudyService.Tuple;

public class KanjiDex implements Publisher<DexData> {
    // TODO we need structures of arrays here that monitor the db a little better
    private Controller controller;
    private List<Kanji> kanjiRanking;
    // TODO maybe do sth with ListDataEvents
    private List<Subscriber<? super DexData>> subscribers;

    public KanjiDex(Controller controller) {
        kanjiRanking = new ArrayList<Kanji>();
        subscribers = new ArrayList<Subscriber<? super DexData>>();
    }

    public void setKanjiRanking(List<Kanji> kanjiRanking) {
        this.kanjiRanking = kanjiRanking;
        //updateKanjiListAndNotify(this.kanjiRanking);
        //rankedKanjiList.stream().map(t -> t.getX()).collect(Collectors.toList());
        //updateKanjiListAndNotify(rankedKanjiList);
    }

    public void printRankedKanjiList() {
        System.out.println("Studied Kanji ranked by proficiency (total " + kanjiRanking.size() + "): ");
        kanjiRanking.stream().forEach(k -> {
            System.out.print(k.getCharacter() + " ");
        });
        System.out.print("\n");
    }

    public List<Kanji> getKanjiRanking() { return this.kanjiRanking; }

    @Override
    public void subscribe(Subscriber<? super DexData> subscriber) {

        subscribers.add(subscriber);

    }

    // Add a method to update the Kanji list and deliver the update event.
    public void updateKanjiListAndNotify(List<Kanji> newKanjiEntries) {
        setKanjiRanking(newKanjiEntries.stream().collect(Collectors.toList()));
        DexData data = new DexData();
        data.setKanjiEntries(newKanjiEntries);

        System.out.println("publishing " + newKanjiEntries.size());
        // Deliver the update event to subscribers.
        publish(data);
    }

    public void publish(DexData data) {
        System.out.println("publishing to " + subscribers.size());
        subscribers.forEach(subscriber -> subscriber.onNext(data));
    }

}

