package controller;

import view.BattleWindow;
import view.DexWindow;
import view.KanjiGUI;
import model.radicals.RadicalFighter;
import model.radicals.Radical;
import model.kanji.KanjiScheduler;
import model.kanji.Kanji;
import model.kanji.KanjiDex;

import java.util.List;
import java.util.List.*;
import java.util.stream.Collectors;

import controller.KanjiBattle;
import data.KanjiDatabase;
import data.StudyService;

public class Controller {
    private KanjiGUI view;
    private KanjiScheduler kanjiScheduler;
    private KanjiDatabase db;
    private StudyService studyService;
    private KanjiDex kanjiDex;
    private DexHandler dexHandler;
    //private KanjiBattle kanjiBattle;

    public Controller() {
        this.view = new KanjiGUI(this);
        this.db = new KanjiDatabase();
        this.studyService = new StudyService();
        this.kanjiScheduler = new KanjiScheduler(studyService); // TODO access through controller
        this.kanjiDex = new KanjiDex(this);
        this.dexHandler = new DexHandler(this);
        dexHandler.setDex(kanjiDex);
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
        Thread battleThread = new Thread(battle);
        battleThread.start();

    }

    public void updateKanjiDex() {
        // Pass the ranked kanji list to the KanjiDex
        System.out.println("updating");
        studyService.updateKanjiProficiency();
        kanjiDex.updateKanjiListAndNotify(
                studyService.getKanjiRankedByProficiency().stream().collect(Collectors.toList()));
    }

    public void addDexWindowToHandler(DexWindow window) {
        dexHandler.setWindow(window);
        window.setDexHandler(dexHandler);
    }

    public void addDexWindow(DexWindow window) {
        addDexWindowToHandler(window);
        subscribeToDex(window);
    }

    // TODO these should be injected
    public KanjiDatabase getDB() {
        return db;
    }

    public KanjiDex getKanjiDex() { return kanjiDex; }

    public DexHandler getDexHandler() { return dexHandler; }

    public void endBattle() {
        view.closeBattleWindow();
    }

    public void subscribeToDex(DexWindow dexWindow) {
        System.out.println("subbing");
        updateKanjiDex();
        kanjiDex.subscribe(dexWindow);
    }
}