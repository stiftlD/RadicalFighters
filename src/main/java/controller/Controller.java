package controller;

import controller.task.IGameController;
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

public class Controller implements IBattleController {

    private IGameController gameController; // this should be common between all controller interfaces
    private IBattleController battleController;
    // TODO seperate dex concern

    private ServiceLocator serviceLocator; // we inject it so we can mock services
    private KanjiGUI view;
    private KanjiScheduler kanjiScheduler;
    private KanjiDatabase db;
    private StudyService studyService;
    private KanjiDex kanjiDex;
    private DexHandler dexHandler;
    //private KanjiBattle kanjiBattle;

    public Controller(ServiceLocator serviceLocator, IBattleController battleController) {
        this.battleController = battleController;

        // get serviceLocator from one of the interfaces
        this.serviceLocator = battleController.getServiceLocator();
        this.view = new KanjiGUI(this);
        this.db = serviceLocator.getDB();
        this.studyService = serviceLocator.getStudyService();
        this.kanjiScheduler = serviceLocator.getKanjiScheduler();
        this.kanjiDex = new KanjiDex(this);
        this.dexHandler = new DexHandler(this);
        dexHandler.setDex(kanjiDex);
        //this.kanjiBattle = new KanjiBattle();
    }

    public void start() {
        view.run();
    }

    public void startBattle(BattleWindow window) {
        updateKanjiDex();
        battleController.startBattle(window);
    }

    public void updateKanjiDex() {
        // Pass the ranked kanji list to the KanjiDex
        System.out.println("updating");
        studyService.updateKanjiProficiency();
        // TODO streaming kanji over there would be cool
        kanjiDex.updateKanjiListAndNotify(
                studyService.getKanjiRankedByProficiency().stream().collect(Collectors.toList()));
        if (dexHandler != null) dexHandler.displayStudyStatisticChart();
    }

    public void addDexWindowToHandler(DexWindow window) {
        dexHandler.setWindow(window);
        window.setDexHandler(dexHandler);
    }

    public void addDexWindow(DexWindow window) {
        addDexWindowToHandler(window);
        subscribeToDex(window);
    }

    /*public KanjiDatabase getDB() {
        return db;
    }*/

    public KanjiDex getKanjiDex() { return kanjiDex; }

    public DexHandler getDexHandler() { return dexHandler; }

    public void endBattle() {
        view.closeBattleWindow();
        battleController.endBattle();
        updateKanjiDex();
    }

    public void subscribeToDex(DexWindow dexWindow) {
        System.out.println("subbing");
        updateKanjiDex();
        kanjiDex.subscribe(dexWindow);
    }

    /*public StudyService getStudyService() {
        return this.studyService;
    }*/

    public ServiceLocator getServiceLocator() { return serviceLocator; }
}