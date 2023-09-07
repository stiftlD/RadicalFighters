package controller;

import view.BattleWindow;
import view.KanjiGUI;
import model.radicals.RadicalFighter;
import model.radicals.Radical;
import model.kanji.KanjiScheduler;
import controller.KanjiBattle;
import data.KanjiDatabase;

public class Controller {
    private KanjiGUI view;
    private KanjiScheduler kanjiScheduler;
    private KanjiDatabase db;
    //private KanjiBattle kanjiBattle;

    public Controller() {
        this.view = new KanjiGUI(this);
        this.db = new KanjiDatabase();
        this.kanjiScheduler = new KanjiScheduler(db);
        //this.kanjiBattle = new KanjiBattle();
    }

    public void start() {
        view.run();
    }

    public void startBattle(BattleWindow window) {
        RadicalFighter playerFighter = new RadicalFighter(
                "iiko",
                new Radical(0, "iiko", "uiiko", "test player rad", null),
                100,
                50,
                50
        );
        RadicalFighter opponentFighter = new RadicalFighter(
                "waruiko",
                new Radical(1, "waruiko", "uwaruiko", "test opponent rad", null),
                100,
                50,
                50
        );
        KanjiBattle battle = new KanjiBattle(window, this, kanjiScheduler, new RadicalFighter[]{playerFighter}, new RadicalFighter[]{opponentFighter});
        battle.start();

    }

    // not sure we should do it this way
    public KanjiDatabase getDB() {
        return db;
    }
}