package controller;

import java.util.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Flow.Publisher;
import java.util.stream.Collectors;


import model.kanji.Kanji;
import model.kanji.KanjiScheduler;
import model.radicals.RadicalFighter;
import model.battleaction.*;

import utils.UpdateEvent;
import view.BattleWindow; //TODO access via controllerx


class FighterUpdateEvent implements UpdateEvent<RadicalFighter[]> {
    private RadicalFighter fighter1;
    private RadicalFighter fighter2;

    public FighterUpdateEvent(RadicalFighter f1, RadicalFighter f2) {
        this.fighter1 = f1;
        this.fighter2 = f2;
    }

    public RadicalFighter[] getData() {
        return new RadicalFighter[]{fighter1, fighter2};
    }
}

public class KanjiBattle implements Publisher<FighterUpdateEvent> {
    private RadicalFighter[] team1;
    private RadicalFighter[] team2;
    private int currentTurn;

    private BattleWindow battleWindow;
    private KanjiScheduler kanjiScheduler;

    private List<Subscriber<FighterUpdateEvent>> subscribers;

    public KanjiBattle(BattleWindow battleWindow, KanjiScheduler kanjiScheduler, RadicalFighter[] team1, RadicalFighter[] team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.currentTurn = 0;
        this.battleWindow = battleWindow;
        this.kanjiScheduler = kanjiScheduler;
        this.subscribers = new ArrayList<Subscriber<FighterUpdateEvent>>();
    }

    public void start() {
        subscribe(battleWindow);
        battleWindow.run();

        //System.out.println(observers);
        //performTurn();
        while (!isBattleOver()) {
            //notifyObservers();
            System.out.println("----- Turn " + (currentTurn + 1) + " -----");
            battleWindow.writeToOutput(("----- Turn " + (currentTurn + 1) + " -----"));

            performTurn();

            //boolean playerLost = Arrays.asList(team1).stream().filter(r -> r.getHP() > 0).noneMatch();
            //boolean opponentLost = Arrays.asList(team2).stream().filter(r -> r.getHP() > 0).noneMatch();


            currentTurn++;
        }
    }

    // TODO use actual kanji
    // TODO inject progress data gathering
    // TODO extract Task logic and write different tasks

    private void performTurn() {
        System.out.println(subscribers.size());
        // select 4 players the player is proficient with as possible attacks and 4 they are not proficient with as enemy attacks
        List<Kanji> proficientKanji = new ArrayList<Kanji>();
        List<Kanji> inproficientKanji = new ArrayList<Kanji>();
        for (int i = 0; i < 4; i++) {
            proficientKanji.add(kanjiScheduler.getProficientKanji());
            inproficientKanji.add(kanjiScheduler.getProficientKanji());
        }

        // TODO implement different tasks in view, have them scheduled here and pass them what they need
        // TODO prepare kanji/radical/action data to display to the player
        // TODO for example query kanji based on different criteria as wrong answers

        // have player choose a kanji as attack, set task and then perform attack with updated dmg
        int rightAnswer = (int) (Math.random() * 4.0); // select random kanji to be the right one
        String chosenAttackKanji = battleWindow.choose1OutOf4((String[]) proficientKanji.stream().map(Kanji::getCharacter).toArray(String[]::new), "Choose a kanji action:", "Attack selection");
        //battleWindow.waitContinueCommand();
        String[] meanings = proficientKanji.stream().map(k -> String.join(",", k.getTranslations())).toArray(String[]::new);
        String expectedMeaning = meanings[rightAnswer];
        Collections.shuffle(Arrays.asList(meanings));
        String answer = battleWindow.choose1OutOf4(meanings, "What is the meaning of " + chosenAttackKanji, "Kanji Attack");
        boolean attackSuccessful = expectedMeaning.equals(answer);
        battleWindow.writeToOutput(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);
        int damage = 30;
        if (attackSuccessful) damage *= 2;
        team2[0].takeDamage(damage);

        //battleWindow.waitContinueCommand();

        System.out.println(chosenAttackKanji);

        rightAnswer = (int) (Math.random() * 4.0);
        String chosenDefenseKanji = battleWindow.choose1OutOf4(inproficientKanji.stream().map(Kanji::getCharacter).toArray(String[]::new), "Choose a kanji action:", "Defense selection");
        //battleWindow.waitContinueCommand();
        meanings = inproficientKanji.stream().map(k -> String.join(",", k.getTranslations())).toArray(String[]::new);
        expectedMeaning = meanings[rightAnswer];
        Collections.shuffle(Arrays.asList(meanings));
        answer = battleWindow.choose1OutOf4(meanings, "What is the meaning of " + chosenDefenseKanji, "Kanji Defense");
        boolean defenseSuccessful = expectedMeaning.equals(answer);
        damage = 30;
        if (defenseSuccessful) damage /= 2;
        team1[0].takeDamage(damage);

        publish(new FighterUpdateEvent(team1[0], team2[0]));
        //notifyObservers();

        //battleWindow.waitContinueCommand();
    }

    private RadicalFighter getNextAttacker() {
        return currentTurn % 2 == 0 ? team1[currentTurn / 2] : team2[currentTurn / 2];
    }

    private RadicalFighter getNextDefender(RadicalFighter attacker) {
        RadicalFighter[] defenders = attacker == team1[currentTurn / 2] ? team2 : team1;
        return defenders[currentTurn / 2];
    }

    public void printBattleState() {
        System.out.println("Current Battle State:");
        System.out.println("Team 1:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team1[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println("Team 2:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team2[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println();
    }

    private boolean isBattleOver() {
        for (RadicalFighter fighter : team1) {
            if (fighter.getHP() > 0) {
                boolean allAlliesDefeated = true;
                for (RadicalFighter opponent : team2) {
                    if (opponent.getHP() > 0) {
                        allAlliesDefeated = false;
                        break;
                    }
                }
                if (allAlliesDefeated) {
                    System.out.println("Team 1 wins!");
                    return true;
                } else {
                    break;
                }
            }
        }
        for (RadicalFighter fighter : team2) {
            if (fighter.getHP() > 0) {
                boolean allOpponentsDefeated = true;
                for (RadicalFighter opponent : team1) {
                    if (opponent.getHP() > 0) {
                        allOpponentsDefeated = false;
                        break;
                    }
                }
                if (allOpponentsDefeated) {
                    System.out.println("Team 2 wins!");
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    public int getOpponentHealth() {
        return team1[0].getHP();
    }

    public int getPlayerHealth() {
        return team2[0].getHP();
    }

    //Publisher interface
    @Override
    public void subscribe(Subscriber<? super FighterUpdateEvent> subscriber) {
        subscribers.add((Subscriber<FighterUpdateEvent>) subscriber);
    }
    public void publish(FighterUpdateEvent event) {
        subscribers.forEach(subscriber -> subscriber.onNext(event));
    }
}
