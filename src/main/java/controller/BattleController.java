package controller;

import controller.task.IGameController;
import data.StudyService;
import model.kanji.KanjiScheduler;
import model.radicals.Radical;
import model.radicals.RadicalFighter;
import view.BattleWindow;

public class BattleController implements IBattleController {

    private final StudyService studyService;
    private final KanjiScheduler kanjiScheduler;
    private final IGameController gameController;

    public BattleController(IGameController gameController) {
        this.gameController = gameController;
        ServiceLocator serviceLocator = gameController.getServiceLocator();
        this.studyService = serviceLocator.getStudyService();
        this.kanjiScheduler = serviceLocator.getKanjiScheduler();
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
        KanjiBattle battle = new KanjiBattle(window, this, new RadicalFighter[]{playerFighter}, new RadicalFighter[]{opponentFighter});
        Thread battleThread = new Thread(battle);
        battleThread.start();
    }

    public void endBattle() {

    }

    public ServiceLocator getServiceLocator() {
        return gameController.getServiceLocator();
    }
}
