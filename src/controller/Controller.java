package controller;

import view.BattleWindow;
import view.KanjiGUI;
import model.radicals.RadicalFighter;
import model.radicals.Radical;

public class Controller {
    private KanjiGUI view;
    //private KanjiScheduler kanjiScheduler;
    //private KanjiBattle kanjiBattle;

    public Controller() {
        this.view = new KanjiGUI(this);
        //this.kanjiScheduler = new KanjiScheduler();
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
        KanjiBattle battle = new KanjiBattle(window, new RadicalFighter[]{playerFighter}, new RadicalFighter[]{opponentFighter});
        battle.start();
        /*model.radicals.RadicalFighter player = kanjiScheduler.getNextFighter();
        model.radicals.RadicalFighter enemy = kanjiScheduler.getNextFighter();
        kanjiBattle.startBattle(player, enemy);
        view.showBattleWindow(player, enemy);*/
    }
}