package controller;

import view.*;
import model.radicals.*;


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
        /*model.radicals.RadicalFighter player = kanjiScheduler.getNextFighter();
        model.radicals.RadicalFighter enemy = kanjiScheduler.getNextFighter();
        kanjiBattle.startBattle(player, enemy);
        view.showBattleWindow(player, enemy);*/

        // Mock fighters
        RadicalFighter[] team1 = {new RadicalFighter("Kanji1", 50, 50), new RadicalFighter("Kanji2", 40, 60)};
        RadicalFighter[] team2 = {new RadicalFighter("Kanji3", 60, 40), new RadicalFighter("Kanji4", 30, 70)};
        // Create battle object
        KanjiBattle battle = new KanjiBattle(team1, team2);
        battle.registerObserver(window);
    }
}