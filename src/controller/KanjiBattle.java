package controller;

import java.util.*;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Flow.Publisher;


import model.radicals.RadicalFighter;

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

    private List<Subscriber<FighterUpdateEvent>> subscribers;

    public KanjiBattle(BattleWindow battleWindow, RadicalFighter[] team1, RadicalFighter[] team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.currentTurn = 0;
        this.battleWindow = battleWindow;
        this.subscribers = new ArrayList<Subscriber<FighterUpdateEvent>>();
    }

    public void start() {
        subscribe(battleWindow);
        battleWindow.run();

        //System.out.println(observers);
        //performTurn();
        while (!isBattleOver()) {
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
        String[] proficientKanji;
        proficientKanji = new String[]{"1", "2", "3", "4"};
        String[] inproficientKanji;
        inproficientKanji = new String[]{"10", "9", "8", "7"};

        String chosenAttackKanji = battleWindow.choose1OutOf4(proficientKanji, "Choose a kanji action:", "Attack selection");
        //battleWindow.waitContinueCommand();
        String expectedMeaning = "who knows";
        String answer = battleWindow.choose1OutOf4(new String[]{"who knows", "42", "saa", "idgaf"}, "What is the meaning of " + chosenAttackKanji, "Kanji Task");
        boolean attackSuccessful = expectedMeaning.equals(answer);
        battleWindow.writeToOutput(attackSuccessful ? "That's right!" : "Wrong, it's " + expectedMeaning);
        int damage = 30;
        if (attackSuccessful) damage *= 2;
        team2[0].takeDamage(damage);

        //battleWindow.waitContinueCommand();

        System.out.println(chosenAttackKanji);
        String chosenDefenseKanji = battleWindow.choose1OutOf4(inproficientKanji, "Choose a kanji action:", "Defense selection");
        //battleWindow.waitContinueCommand();
        expectedMeaning = "ich weiss dass ich nichts weiss";
        answer = battleWindow.choose1OutOf4(new String[]{"ich weiss dass ich nichts weiss", "25", "fuu", "omgrly"}, "What is the meaning of " + chosenAttackKanji, "Kanji Task");
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

    // Implementation of Observable interface

    //@Override
    /*public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    //@Override
    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        if (observers == null) return;
        for (Observer observer : observers) {
            observer.update(this, null);
        }
    }*/


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
