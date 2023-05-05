package controller;

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

    public void startBattle() {
        /*model.radicals.RadicalFighter player = kanjiScheduler.getNextFighter();
        model.radicals.RadicalFighter enemy = kanjiScheduler.getNextFighter();
        kanjiBattle.startBattle(player, enemy);
        view.showBattleWindow(player, enemy);*/
    }
}